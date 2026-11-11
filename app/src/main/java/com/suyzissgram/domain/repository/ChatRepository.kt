package com.suyzissgram.domain.repository

import com.suyzissgram.domain.model.ChatMessage
import com.suyzissgram.domain.model.ChatPreview
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeMessages(): Flow<List<ChatMessage>>
    fun observeChats(): Flow<List<ChatPreview>>
    suspend fun sendMessage(text: String)
    suspend fun clearMessages()
}
