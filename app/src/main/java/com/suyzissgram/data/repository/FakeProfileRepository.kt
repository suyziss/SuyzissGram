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
        val current = this.profile.value
        val normalized = profile.copy(
            id = profile.id.ifBlank { current.id },
            email = profile.email.ifBlank { current.email },
            displayName = profile.displayName.trim().take(MAX_NAME_LENGTH),
            username = profile.username.trim().removePrefix("@")
                .filter { it.isLetterOrDigit() || it == '_' }.take(MAX_USERNAME_LENGTH),
            bio = profile.bio.trim().take(MAX_BIO_LENGTH),
            accentColor = profile.accentColor
        )
        this.profile.value = normalized
        preferences.edit()
            .putString(KEY_PROFILE_ID, normalized.id)
            .putString(KEY_PROFILE_EMAIL, normalized.email)
            .putString(KEY_DISPLAY_NAME, normalized.displayName)
            .putString(KEY_USERNAME, normalized.username)
            .putString(KEY_BIO, normalized.bio)
            .putString(KEY_AVATAR_URI, normalized.avatarUri)
            .putLong(KEY_ACCENT_COLOR, normalized.accentColor)
            .putLong(KEY_NICKNAME_COLOR, normalized.nicknameColor)
            .putLong(KEY_PROFILE_BACKGROUND_COLOR, normalized.profileBackgroundColor)
            .putString(KEY_PROFILE_BACKGROUND_IMAGE_URI, normalized.profileBackgroundImageUri)
            .apply()
    }

    private fun loadProfile(): UserProfile = UserProfile(
        id = preferences.getString(KEY_PROFILE_ID, "").orEmpty(),
        email = preferences.getString(KEY_PROFILE_EMAIL, "").orEmpty(),
        displayName = preferences.getString(KEY_DISPLAY_NAME, "").orEmpty(),
        username = preferences.getString(KEY_USERNAME, "").orEmpty(),
        bio = preferences.getString(KEY_BIO, "").orEmpty(),
        avatarUri = preferences.getString(KEY_AVATAR_URI, null),
        accentColor = preferences.getLong(KEY_ACCENT_COLOR, DEFAULT_ACCENT_COLOR),
        nicknameColor = preferences.getLong(KEY_NICKNAME_COLOR, DEFAULT_NICKNAME_COLOR),
        profileBackgroundColor = preferences.getLong(KEY_PROFILE_BACKGROUND_COLOR, DEFAULT_PROFILE_BACKGROUND_COLOR),
        profileBackgroundImageUri = preferences.getString(KEY_PROFILE_BACKGROUND_IMAGE_URI, null)
    )

    private companion object {
        const val KEY_PROFILE_ID = "profile_account_id"
        const val KEY_PROFILE_EMAIL = "profile_email"
        const val KEY_DISPLAY_NAME = "profile_display_name"
        const val KEY_USERNAME = "profile_username"
        const val KEY_BIO = "profile_bio"
        const val KEY_AVATAR_URI = "profile_avatar_uri"
        const val KEY_ACCENT_COLOR = "profile_accent_color"
        const val KEY_NICKNAME_COLOR = "profile_nickname_color"
        const val KEY_PROFILE_BACKGROUND_COLOR = "profile_background_color"
        const val KEY_PROFILE_BACKGROUND_IMAGE_URI = "profile_background_image_uri"
        const val MAX_NAME_LENGTH = 64
        const val MAX_USERNAME_LENGTH = 14
        const val MAX_BIO_LENGTH = 120
        const val DEFAULT_ACCENT_COLOR = 0xFF9B7BFFL
        const val DEFAULT_NICKNAME_COLOR = 0xFFFFFFFFL
        const val DEFAULT_PROFILE_BACKGROUND_COLOR = 0xFF17151FL
    }
}
