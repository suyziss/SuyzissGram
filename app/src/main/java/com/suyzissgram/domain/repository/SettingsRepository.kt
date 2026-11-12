package com.suyzissgram.domain.repository

import com.suyzissgram.domain.model.AppLanguage
import com.suyzissgram.domain.model.AppSettings
import com.suyzissgram.domain.model.ThemeChoice
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setTheme(theme: ThemeChoice)
    suspend fun setTextScale(scale: Float)
    suspend fun setChatTheme(theme: Int)
    suspend fun setOnlyPremiumMessages(enabled: Boolean)
}
