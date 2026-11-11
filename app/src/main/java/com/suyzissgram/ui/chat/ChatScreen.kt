package com.suyzissgram.ui.chat

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.suyzissgram.domain.model.AttachmentKind
import com.suyzissgram.domain.model.ChatAttachment
import com.suyzissgram.domain.model.ChatMessage
import com.suyzissgram.domain.model.MediaQuality
import com.suyzissgram.ui.i18n.AppStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.concurrent.Executor

private data class PendingMedia(
    val uri: Uri,
    val displayName: String,
    val mimeType: String?,
    val kind: AttachmentKind
)

@Composable
fun ChatScreen(
    state: ChatUiState,
    labels: AppStrings,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onSendAttachment: (String, String, String?, AttachmentKind, MediaQuality) -> Unit,
    onSendLocation: (Double, Double) -> Unit,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val isEnglish = labels.local == "Stored on this device"
    val listState = rememberLazyListState()
    var attachmentMenuExpanded by remember { mutableStateOf(false) }
    var pendingMedia by remember { mutableStateOf(emptyList<PendingMedia>()) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var locationStatus by remember { mutableStateOf<String?>(null) }

    fun sendPendingMedia(quality: MediaQuality) {
        pendingMedia.forEach { item ->
            onSendAttachment(
                item.uri.toString(),
                item.displayName,
                item.mimeType,
                item.kind,
                quality
            )
        }
        pendingMedia = emptyList()
        showQualityDialog = false
    }

    fun sendPickedFiles(uris: List<Uri>) {
        uris.forEach { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            val item = queryPendingMedia(context, uri, AttachmentKind.FILE)
            onSendAttachment(
                item.uri.toString(),
                item.displayName,
                item.mimeType,
                AttachmentKind.FILE,
                MediaQuality.ORIGINAL
            )
        }
    }

    fun sendCurrentLocation() {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val hasFinePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarsePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFinePermission && !hasCoarsePermission) {
            locationStatus = if (isEnglish) "Location permission was not granted" else "Нет разрешения на геолокацию"
            return
        }

        val provider = when {
            hasFinePermission && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ->
                LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ->
                LocationManager.NETWORK_PROVIDER
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ->
                LocationManager.GPS_PROVIDER
            else -> null
        }

        if (provider == null) {
            locationStatus = if (isEnglish) {
                "Enable device location services and try again"
            } else {
                "Включите геолокацию устройства и повторите попытку"
            }
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val executor: Executor = Executor { command ->
                    Handler(Looper.getMainLooper()).post(command)
                }
                locationManager.getCurrentLocation(provider, null, executor) { location ->
                    if (location == null) {
                        locationStatus = if (isEnglish) {
                            "Could not determine the current location"
                        } else {
                            "Не удалось определить текущее местоположение"
                        }
                    } else {
                        onSendLocation(location.latitude, location.longitude)
                        locationStatus = null
                    }
                }
            } else {
                val location = getLastKnownLocation(locationManager, provider)
                if (location == null) {
                    locationStatus = if (isEnglish) {
                        "No recent location is available"
                    } else {
                        "Нет доступного недавнего местоположения"
                    }
                } else {
                    onSendLocation(location.latitude, location.longitude)
                    locationStatus = null
                }
            }
        } catch (securityException: SecurityException) {
            locationStatus = if (isEnglish) {
                "Location permission is required"
            } else {
                "Требуется разрешение на геолокацию"
            }
        } catch (exception: Exception) {
            locationStatus = exception.message ?: "Unable to read device location"
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) sendCurrentLocation()
        else locationStatus = if (isEnglish) {
            "Location permission was not granted"
        } else {
            "Разрешение на геолокацию не предоставлено"
        }
    }

    val mediaPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
            }
            pendingMedia = uris.map { uri ->
                val mime = context.contentResolver.getType(uri)
                val kind = if (mime?.startsWith("video/") == true) {
                    AttachmentKind.VIDEO
                } else {
                    AttachmentKind.IMAGE
                }
                queryPendingMedia(context, uri, kind)
            }
            showQualityDialog = true
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) sendPickedFiles(uris)
    }

    if (showQualityDialog) {
        val hasPhotos = pendingMedia.any { it.kind == AttachmentKind.IMAGE }
        val hasVideos = pendingMedia.any { it.kind == AttachmentKind.VIDEO }
        AlertDialog(
            onDismissRequest = {
                pendingMedia = emptyList()
                showQualityDialog = false
            },
            title = { Text(if (isEnglish) "Media quality" else "Качество медиа") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        if (isEnglish) {
                            "Choose how photos should be stored. Videos are kept in their original quality."
                        } else {
                            "Выберите качество фотографий. Видео сохраняется в исходном качестве."
                        }
                    )
                    if (hasPhotos && hasVideos) {
                        Text(
                            if (isEnglish) "This selection contains photos and videos." else "В выборе есть фото и видео.",
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                if (hasPhotos) {
                    TextButton(onClick = { sendPendingMedia(MediaQuality.COMPRESSED) }) {
                        Text(if (isEnglish) "Compress photos" else "Сжать фото")
                    }
                } else {
                    TextButton(onClick = { sendPendingMedia(MediaQuality.ORIGINAL) }) {
                        Text(if (isEnglish) "Send original" else "Отправить оригинал")
                    }
                }
            },
            dismissButton = {
                Row {
                    if (hasPhotos) {
                        TextButton(onClick = { sendPendingMedia(MediaQuality.ORIGINAL) }) {
                            Text(if (isEnglish) "Original" else "Оригинал")
                        }
                    }
                    TextButton(onClick = {
                        pendingMedia = emptyList()
                        showQualityDialog = false
                    }) {
                        Text(labels.cancel)
                    }
                }
            }
        )
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Column(Modifier.fillMaxSize().background(colors.background)) {
        Row(
            Modifier.fillMaxWidth().background(colors.surface).padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = labels.back, tint = colors.onSurface)
            }
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(colors.primary.copy(alpha = 0.2f))
                    .clickable(onClick = onOpenProfile),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = colors.primary)
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.clickable(onClick = onOpenProfile)) {
                Text(labels.favorite, color = colors.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(labels.local, color = colors.onSurfaceVariant, fontSize = 11.sp)
            }
        }

        if (state.isLoading) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Loading…", color = colors.onSurfaceVariant)
            }
        } else if (state.messages.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(66.dp).clip(CircleShape)
                            .background(colors.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(Modifier.size(14.dp))
                    Text(labels.empty, color = colors.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                    Spacer(Modifier.size(5.dp))
                    Text(labels.emptyHint, color = colors.onSurfaceVariant, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
                state = listState,
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.messages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        onOpenAttachment = { attachment ->
                            openAttachment(context, attachment)
                        }
                    )
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().background(colors.background).navigationBarsPadding()
                .imePadding().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Box {
                IconButton(
                    onClick = { attachmentMenuExpanded = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = if (isEnglish) "Attach" else "Прикрепить"
                    )
                }
                DropdownMenu(
                    expanded = attachmentMenuExpanded,
                    onDismissRequest = { attachmentMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (isEnglish) "Photo or video" else "Фото или видео") },
                        leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) },
                        onClick = {
                            attachmentMenuExpanded = false
                            mediaPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isEnglish) "File" else "Файл") },
                        leadingIcon = { Icon(Icons.Default.AttachFile, contentDescription = null) },
                        onClick = {
                            attachmentMenuExpanded = false
                            filePicker.launch(arrayOf("*/*"))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isEnglish) "Location" else "Геопозиция") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        onClick = {
                            attachmentMenuExpanded = false
                            val fineGranted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            val coarseGranted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            if (fineGranted || coarseGranted) {
                                sendCurrentLocation()
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                    )
                }
            }

            OutlinedTextField(
                value = state.draft,
                onValueChange = onDraftChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(labels.message, color = colors.onSurfaceVariant) },
                shape = RoundedCornerShape(24.dp),
                maxLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.surface,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    cursorColor = colors.primary
                )
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = onSend,
                enabled = state.draft.isNotBlank(),
                modifier = Modifier.size(50.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = labels.send)
            }
        }

        if (state.isSendingAttachment) {
            Text(
                if (isEnglish) "Saving attachment…" else "Сохранение вложения…",
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                fontSize = 12.sp
            )
        }
        locationStatus?.let {
            Text(it, color = colors.error, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), fontSize = 12.sp)
        }
        if (state.error != null) {
            Text(state.error, color = colors.error, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    onOpenAttachment: (ChatAttachment) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val attachment = message.attachment
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.outgoing) Arrangement.End else Arrangement.Start
    ) {
        Column(
            Modifier.widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (message.outgoing) 18.dp else 5.dp,
                        bottomEnd = if (message.outgoing) 5.dp else 18.dp
                    )
                )
                .background(if (message.outgoing) colors.primary.copy(alpha = 0.22f) else colors.surface)
                .padding(horizontal = 13.dp, vertical = 9.dp)
        ) {
            if (attachment != null) {
                AttachmentContent(attachment, onOpenAttachment)
            }
            if (message.text.isNotBlank() && (attachment == null || attachment.kind == AttachmentKind.LOCATION)) {
                Text(message.text, color = colors.onSurface, fontSize = 15.sp)
            }
            Spacer(Modifier.size(4.dp))
            Text(
                message.time,
                color = colors.onSurfaceVariant,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun AttachmentContent(
    attachment: ChatAttachment,
    onOpenAttachment: (ChatAttachment) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    when (attachment.kind) {
        AttachmentKind.IMAGE -> {
            val bitmap by produceState<Bitmap?>(initialValue = null, attachment.localPath) {
                value = withContext(Dispatchers.IO) {
                    attachment.localPath?.let { path ->
                        runCatching {
                            val options = BitmapFactory.Options().apply { inSampleSize = 2 }
                            BitmapFactory.decodeFile(path, options)
                        }.getOrNull()
                    }
                }
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = attachment.displayName,
                    modifier = Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenAttachment(attachment) },
                    contentScale = ContentScale.Crop
                )
            } else {
                AttachmentFileCard(attachment, onOpenAttachment)
            }
            Text(
                if (attachment.quality == MediaQuality.COMPRESSED) "Compressed photo" else "Original photo",
                color = colors.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
        AttachmentKind.VIDEO, AttachmentKind.FILE -> AttachmentFileCard(attachment, onOpenAttachment)
        AttachmentKind.LOCATION -> {
            Row(
                Modifier.fillMaxWidth().clickable { onOpenAttachment(attachment) }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = colors.primary, modifier = Modifier.size(30.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Location", color = colors.onSurface, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${attachment.latitude ?: 0.0}, ${attachment.longitude ?: 0.0}",
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AttachmentFileCard(
    attachment: ChatAttachment,
    onOpenAttachment: (ChatAttachment) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().clickable { onOpenAttachment(attachment) }.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (attachment.kind == AttachmentKind.VIDEO) Icons.Default.Movie else Icons.Default.AttachFile,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(34.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                attachment.displayName.ifBlank { "Attachment" },
                color = colors.onSurface,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )
            Text(
                formatFileSize(attachment.sizeBytes),
                color = colors.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

private fun queryPendingMedia(
    context: Context,
    uri: Uri,
    kind: AttachmentKind
): PendingMedia {
    var displayName = uri.lastPathSegment?.substringAfterLast('/') ?: "attachment"
    runCatching {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) displayName = cursor.getString(index) ?: displayName
            }
        }
    }
    return PendingMedia(
        uri = uri,
        displayName = displayName,
        mimeType = context.contentResolver.getType(uri),
        kind = kind
    )
}

private fun getLastKnownLocation(
    manager: LocationManager,
    provider: String
): Location? = runCatching { manager.getLastKnownLocation(provider) }.getOrNull()

private fun openAttachment(context: Context, attachment: ChatAttachment) {
    if (attachment.kind == AttachmentKind.LOCATION) {
        val latitude = attachment.latitude ?: return
        val longitude = attachment.longitude ?: return
        val mapUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
        runCatching {
            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_VIEW, mapUri), "Open map"))
        }
        return
    }

    val path = attachment.localPath ?: return
    val file = File(path)
    if (!file.exists()) return
    runCatching {
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(contentUri, attachment.mimeType ?: "*/*")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Open attachment"))
    }
}

private fun formatFileSize(sizeBytes: Long): String {
    if (sizeBytes <= 0L) return "Unknown size"
    return when {
        sizeBytes < 1024L -> "$sizeBytes B"
        sizeBytes < 1024L * 1024L ->
            String.format(Locale.getDefault(), "%.1f KB", sizeBytes / 1024.0)
        else -> String.format(Locale.getDefault(), "%.1f MB", sizeBytes / (1024.0 * 1024.0))
    }
}
