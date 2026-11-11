package com.suyzissgram.domain.model

data class UserProfile(
    val displayName: String = "",
    val username: String = "",
    val bio: String = "",
    val avatarUri: String? = null
)
