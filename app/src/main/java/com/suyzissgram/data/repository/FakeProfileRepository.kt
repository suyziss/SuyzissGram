package com.suyzissgram.data.repository

import android.content.SharedPreferences
import com.suyzissgram.domain.model.UserProfile
import com.suyzissgram.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeProfileRepository(
    private val preferences: SharedPreferences
) : ProfileRepository {
    private val profile = MutableStateFlow(loadProfile())

    override fun observeProfile(): Flow<UserProfile> = profile.asStateFlow()

    override suspend fun updateProfile(profile: UserProfile) {
        val normalized = profile.copy(
            displayName = profile.displayName.trim().take(MAX_NAME_LENGTH),
            username = profile.username.trim().removePrefix("@")
                .filter { it.isLetterOrDigit() || it == '_' }.take(MAX_USERNAME_LENGTH),
            bio = profile.bio.trim().take(MAX_BIO_LENGTH)
        )
        this.profile.value = normalized
        preferences.edit()
            .putString(KEY_DISPLAY_NAME, normalized.displayName)
            .putString(KEY_USERNAME, normalized.username)
            .putString(KEY_BIO, normalized.bio)
            .putString(KEY_AVATAR_URI, normalized.avatarUri)
            .apply()
    }

    private fun loadProfile(): UserProfile = UserProfile(
        displayName = preferences.getString(KEY_DISPLAY_NAME, "").orEmpty(),
        username = preferences.getString(KEY_USERNAME, "").orEmpty(),
        bio = preferences.getString(KEY_BIO, "").orEmpty(),
        avatarUri = preferences.getString(KEY_AVATAR_URI, null)
    )

    private companion object {
        const val KEY_DISPLAY_NAME = "profile_display_name"
        const val KEY_USERNAME = "profile_username"
        const val KEY_BIO = "profile_bio"
        const val KEY_AVATAR_URI = "profile_avatar_uri"
        const val MAX_NAME_LENGTH = 64
        const val MAX_USERNAME_LENGTH = 32
        const val MAX_BIO_LENGTH = 120
    }
}
