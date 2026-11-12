package com.suyzissgram.domain.model

enum class AppLanguage {
    EN, RU, JA, KK
}

enum class ThemeChoice {
    SYSTEM, LIGHT, DARK
}

data class AppSettings(
    val language: AppLanguage = AppLanguage.RU,
    val theme: ThemeChoice = ThemeChoice.DARK,
    val textScale: Float = 1f,
    val chatTheme: Int = 0,
    val onlyPremiumMessages: Boolean = false
)
