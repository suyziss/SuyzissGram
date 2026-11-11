package com.suyzissgram.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import com.suyzissgram.domain.model.AttachmentKind
import com.suyzissgram.domain.model.ChatAttachment
import com.suyzissgram.domain.model.ChatMessage
import com.suyzissgram.domain.model.ChatPreview
import com.suyzissgram.domain.model.MediaQuality
import com.suyzissgram.domain.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FakeChatRepository(
    context: Context,
    private val preferences: SharedPreferences
) : ChatRepository {
    private val appContext = context.applicationContext
    private val mutex = Mutex()
    private val messages = MutableStateFlow(loadMessages())

    override fun observeMessages(): Flow<List<ChatMessage>> = messages.asStateFlow()

    override fun observeChats(): Flow<List<ChatPreview>> = messages.map { items ->
        listOf(
            ChatPreview(
                id = SAVED_CHAT_ID,
                title = "Favorite",
                subtitle = "Your personal space",
                lastMessage = items.lastOrNull()?.text ?: "Your personal space",
                lastMessageTime = items.lastOrNull()?.time.orEmpty()
            )
        )
    }

    override suspend fun sendMessage(text: String) {
        val normalized = text.trim()
        if (normalized.isEmpty()) return

        mutex.withLock {
            appendMessage(normalized, null)
        }
    }

    override suspend fun sendAttachment(
        sourceUri: String,
        displayName: String,
        mimeType: String?,
        kind: AttachmentKind,
        quality: MediaQuality
    ) {
        val attachment = withContext(Dispatchers.IO) {
            storeAttachment(
                sourceUri = sourceUri,
                displayName = displayName,
                mimeType = mimeType,
                kind = kind,
                requestedQuality = quality
            )
        }

        mutex.withLock {
            val messageText = when (kind) {
                AttachmentKind.IMAGE -> "Photo"
                AttachmentKind.VIDEO -> "Video"
                AttachmentKind.FILE -> attachment.displayName
                AttachmentKind.LOCATION -> "Location"
            }
            appendMessage(messageText, attachment)
        }
    }

    override suspend fun sendLocation(latitude: Double, longitude: Double) {
        require(latitude in -90.0..90.0) { "Latitude is outside the valid range" }
        require(longitude in -180.0..180.0) { "Longitude is outside the valid range" }

        mutex.withLock {
            val attachment = ChatAttachment(
                kind = AttachmentKind.LOCATION,
                displayName = "Location",
                latitude = latitude,
                longitude = longitude
            )
            appendMessage("Location shared", attachment)
        }
    }

    override suspend fun clearMessages() {
        mutex.withLock {
            messages.value = emptyList()
            preferences.edit().remove(KEY_MESSAGES).apply()
            withContext(Dispatchers.IO) {
                attachmentsDirectory().listFiles()?.forEach { file ->
                    runCatching { file.delete() }
                }
            }
        }
    }

    private suspend fun storeAttachment(
        sourceUri: String,
        displayName: String,
        mimeType: String?,
        kind: AttachmentKind,
        requestedQuality: MediaQuality
    ): ChatAttachment {
        val source = Uri.parse(sourceUri)
        val directory = attachmentsDirectory()
        if (!directory.exists() && !directory.mkdirs()) {
            throw IllegalStateException("Unable to create attachment storage")
        }

        val safeName = displayName
            .substringAfterLast('/')
            .substringAfterLast('\\')
            .replace(Regex("[^A-Za-z0-9._() -]"), "_")
            .take(100)
            .ifBlank { "attachment" }

        val mayCompress = kind == AttachmentKind.IMAGE &&
            requestedQuality == MediaQuality.COMPRESSED
        val extension = when {
            mayCompress && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> "webp"
            mayCompress -> "jpg"
            safeName.contains('.') -> safeName.substringAfterLast('.').take(12)
            else -> "bin"
        }
        val destination = File(directory, "${UUID.randomUUID()}.$extension")

        val compressed = mayCompress && runCatching {
            compressImage(source, destination)
        }.getOrDefault(false)

        if (!compressed) {
            if (destination.exists()) destination.delete()
            appContext.contentResolver.openInputStream(source)?.use { input ->
                FileOutputStream(destination).use { output ->
                    input.copyTo(output)
                }
            } ?: throw IllegalArgumentException("The selected file cannot be opened")
        }

        val actualMimeType = when {
            compressed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> "image/webp"
            compressed -> "image/jpeg"
            else -> mimeType
        }
        val actualQuality = if (compressed) MediaQuality.COMPRESSED else MediaQuality.ORIGINAL
        val resultName = when {
            compressed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
                "${safeName.substringBeforeLast('.', safeName)}.webp"
            compressed ->
                "${safeName.substringBeforeLast('.', safeName)}.jpg"
            else -> safeName
        }

        return ChatAttachment(
            kind = kind,
            localPath = destination.absolutePath,
            displayName = resultName,
            mimeType = actualMimeType,
            sizeBytes = destination.length(),
            quality = actualQuality
        )
    }

    private fun compressImage(source: Uri, destination: File): Boolean {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        appContext.contentResolver.openInputStream(source)?.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        } ?: return false

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return false

        val maxDimension = maxOf(bounds.outWidth, bounds.outHeight)
        var sampleSize = 1
        while (maxDimension / sampleSize > MAX_IMAGE_DIMENSION) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        val bitmap = appContext.contentResolver.openInputStream(source)?.use { input ->
            BitmapFactory.decodeStream(input, null, options)
        } ?: return false

        return try {
            val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                Bitmap.CompressFormat.JPEG
            }
            FileOutputStream(destination).use { output ->
                bitmap.compress(format, COMPRESSED_IMAGE_QUALITY, output)
            }
        } finally {
            bitmap.recycle()
        }
    }

    private fun appendMessage(text: String, attachment: ChatAttachment?) {
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val nextId = maxOf(
            System.currentTimeMillis(),
            (messages.value.lastOrNull()?.id ?: 0L) + 1L
        )
        messages.value = messages.value + ChatMessage(
            id = nextId,
            text = text,
            time = time,
            outgoing = true,
            attachment = attachment
        )
        persistMessages()
    }

    private fun attachmentsDirectory(): File = File(appContext.filesDir, ATTACHMENTS_DIRECTORY)

    private fun loadMessages(): List<ChatMessage> = runCatching {
        val array = JSONArray(preferences.getString(KEY_MESSAGES, "[]"))
        buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                val attachmentObject = item.optJSONObject("attachment")
                add(
                    ChatMessage(
                        id = item.getLong("id"),
                        text = item.getString("text"),
                        time = item.getString("time"),
                        outgoing = item.optBoolean("outgoing", true),
                        attachment = attachmentObject?.toAttachment()
                    )
                )
            }
        }
    }.getOrDefault(emptyList())

    private fun JSONObject.toAttachment(): ChatAttachment? {
        val kind = runCatching {
            AttachmentKind.valueOf(optString("kind"))
        }.getOrNull() ?: return null
        val quality = runCatching {
            MediaQuality.valueOf(optString("quality"))
        }.getOrNull()
        return ChatAttachment(
            kind = kind,
            localPath = optString("localPath").takeIf { it.isNotBlank() && it != "null" },
            displayName = optString("displayName"),
            mimeType = optString("mimeType").takeIf { it.isNotBlank() && it != "null" },
            sizeBytes = optLong("sizeBytes", 0L),
            latitude = if (has("latitude") && !isNull("latitude")) optDouble("latitude") else null,
            longitude = if (has("longitude") && !isNull("longitude")) optDouble("longitude") else null,
            quality = quality
        )
    }

    private fun persistMessages() {
        val array = JSONArray()
        messages.value.forEach { message ->
            val item = JSONObject()
                .put("id", message.id)
                .put("text", message.text)
                .put("time", message.time)
                .put("outgoing", message.outgoing)

            message.attachment?.let { attachment ->
                val attachmentObject = JSONObject()
                    .put("kind", attachment.kind.name)
                    .put("displayName", attachment.displayName)
                    .put("sizeBytes", attachment.sizeBytes)
                attachment.localPath?.let { attachmentObject.put("localPath", it) }
                attachment.mimeType?.let { attachmentObject.put("mimeType", it) }
                attachment.latitude?.let { attachmentObject.put("latitude", it) }
                attachment.longitude?.let { attachmentObject.put("longitude", it) }
                attachment.quality?.let { attachmentObject.put("quality", it.name) }
                item.put("attachment", attachmentObject)
            }
            array.put(item)
        }
        preferences.edit().putString(KEY_MESSAGES, array.toString()).apply()
    }

    private companion object {
        const val KEY_MESSAGES = "messages"
        const val SAVED_CHAT_ID = "saved"
        const val ATTACHMENTS_DIRECTORY = "attachments"
        const val MAX_IMAGE_DIMENSION = 1920
        const val COMPRESSED_IMAGE_QUALITY = 82
    }
}
