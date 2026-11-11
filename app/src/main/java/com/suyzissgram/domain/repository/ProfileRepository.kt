package com.suyzissgram.domain.repository

import com.suyzissgram.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeProfile(): Flow<UserProfile>
    suspend fun updateProfile(profile: UserProfile)
}
