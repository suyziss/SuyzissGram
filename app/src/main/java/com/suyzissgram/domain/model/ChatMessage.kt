package com.suyzissgram.domain.model

enum class AttachmentKind {
    IMAGE,
    VIDEO,
    FILE,
    LOCATION
}

enum class MediaQuality {
    COMPRESSED,
    ORIGINAL
}

data class ChatAttachment(
    val kind: AttachmentKind,
    val localPath: String? = null,
    val displayName: String = "",
    val mimeType: String? = null,
    val sizeBytes: Long = 0L,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val quality: MediaQuality? = null
)

data class ChatMessage(
    val id: Long,
    val text: String,
    val time: String,
    val outgoing: Boolean,
    val attachment: ChatAttachment? = null
)

data class ChatPreview(
    val id: String,
    val title: String,
    val subtitle: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int = 0
)
