package com.suyzissgram.domain.model

data class UserProfile(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val username: String = "",
    val bio: String = "",
    val avatarUri: String? = null,
    val accentColor: Long = 0xFF9B7BFFL,
    val nicknameColor: Long = 0xFFFFFFFFL,
    val profileBackgroundColor: Long = 0xFF17151F,
    val profileBackgroundImageUri: String? = null
)
