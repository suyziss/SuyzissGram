package com.suyzissgram.domain.repository

import com.suyzissgram.domain.model.AttachmentKind
import com.suyzissgram.domain.model.ChatMessage
import com.suyzissgram.domain.model.ChatPreview
import com.suyzissgram.domain.model.MediaQuality
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeMessages(): Flow<List<ChatMessage>>
    fun observeChats(): Flow<List<ChatPreview>>
    suspend fun sendMessage(text: String)
    suspend fun sendAttachment(
        sourceUri: String,
        displayName: String,
        mimeType: String?,
        kind: AttachmentKind,
        quality: MediaQuality
    )
    suspend fun sendLocation(latitude: Double, longitude: Double)
    suspend fun clearMessages()
}
