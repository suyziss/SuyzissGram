package com.suyzissgram.domain.model

data class ChatMessage(
    val id: Long,
    val text: String,
    val time: String,
    val outgoing: Boolean
)

data class ChatPreview(
    val id: String,
    val title: String,
    val subtitle: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int = 0
)
