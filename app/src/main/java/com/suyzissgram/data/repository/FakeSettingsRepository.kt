package com.suyzissgram.data.repository

import android.content.SharedPreferences
import com.suyzissgram.domain.model.AppLanguage
import com.suyzissgram.domain.model.AppSettings
import com.suyzissgram.domain.model.ThemeChoice
import com.suyzissgram.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSettingsRepository(
    private val preferences: SharedPreferences
) : SettingsRepository {
    private val settings = MutableStateFlow(loadSettings())

    override fun observeSettings(): Flow<AppSettings> = settings.asStateFlow()

    override suspend fun setLanguage(language: AppLanguage) {
        update(settings.value.copy(language = language))
    }

    override suspend fun setTheme(theme: ThemeChoice) {
        update(settings.value.copy(theme = theme))
    }

    override suspend fun setTextScale(scale: Float) {
        update(settings.value.copy(textScale = scale.coerceIn(0.85f, 1.25f)))
    }

    override suspend fun setChatTheme(theme: Int) {
        update(settings.value.copy(chatTheme = theme.coerceIn(0, MAX_CHAT_THEME)))
    }

    private fun update(value: AppSettings) {
        settings.value = value
        preferences.edit()
            .putString(KEY_LANGUAGE, value.language.name)
            .putString(KEY_THEME, value.theme.name)
            .putFloat(KEY_TEXT_SCALE, value.textScale)
            .putInt(KEY_CHAT_THEME, value.chatTheme)
            .apply()
    }

    private fun loadSettings(): AppSettings {
        val language = runCatching {
            AppLanguage.valueOf(preferences.getString(KEY_LANGUAGE, AppLanguage.RU.name)!!)
        }.getOrDefault(AppLanguage.RU)
        val theme = runCatching {
            ThemeChoice.valueOf(preferences.getString(KEY_THEME, ThemeChoice.DARK.name)!!)
        }.getOrDefault(ThemeChoice.DARK)

        return AppSettings(
            language = language,
            theme = theme,
            textScale = preferences.getFloat(KEY_TEXT_SCALE, 1f).coerceIn(0.85f, 1.25f),
            chatTheme = preferences.getInt(KEY_CHAT_THEME, 0).coerceIn(0, MAX_CHAT_THEME)
        )
    }

    private companion object {
        const val KEY_LANGUAGE = "language"
        const val KEY_THEME = "theme"
        const val KEY_TEXT_SCALE = "text_scale"
        const val KEY_CHAT_THEME = "chat_theme"
        const val MAX_CHAT_THEME = 4
    }
}
