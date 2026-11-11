package com.suyzissgram.data.repository

import android.content.SharedPreferences
import com.suyzissgram.domain.model.ChatMessage
import com.suyzissgram.domain.model.ChatPreview
import com.suyzissgram.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FakeChatRepository(
    private val preferences: SharedPreferences
) : ChatRepository {
    private val messages = MutableStateFlow(loadMessages())

    override fun observeMessages(): Flow<List<ChatMessage>> = messages.asStateFlow()

    override fun observeChats(): Flow<List<ChatPreview>> =
        kotlinx.coroutines.flow.map(messages) { items ->
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

        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        messages.value = messages.value + ChatMessage(
            id = System.currentTimeMillis(),
            text = normalized,
            time = time,
            outgoing = true
        )
        persistMessages()
    }

    override suspend fun clearMessages() {
        messages.value = emptyList()
        preferences.edit().remove(KEY_MESSAGES).apply()
    }

    private fun loadMessages(): List<ChatMessage> = runCatching {
        val array = JSONArray(preferences.getString(KEY_MESSAGES, "[]"))
        buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    ChatMessage(
                        id = item.getLong("id"),
                        text = item.getString("text"),
                        time = item.getString("time"),
                        outgoing = item.optBoolean("outgoing", true)
                    )
                )
            }
        }
    }.getOrDefault(emptyList())

    private fun persistMessages() {
        val array = JSONArray()
        messages.value.forEach { message ->
            array.put(
                JSONObject()
                    .put("id", message.id)
                    .put("text", message.text)
                    .put("time", message.time)
                    .put("outgoing", message.outgoing)
            )
        }
        preferences.edit().putString(KEY_MESSAGES, array.toString()).apply()
    }

    private companion object {
        const val KEY_MESSAGES = "messages"
        const val SAVED_CHAT_ID = "saved"
    }
}
