package com.suyzissgram.di

import android.content.Context
import com.suyzissgram.data.repository.FakeChatRepository
import com.suyzissgram.data.repository.FakeProfileRepository
import com.suyzissgram.data.repository.FakeSettingsRepository
import com.suyzissgram.data.repository.LocalAuthRepository
import com.suyzissgram.domain.model.PremiumPolicy
import com.suyzissgram.domain.repository.AuthRepository
import com.suyzissgram.domain.repository.ChatRepository
import com.suyzissgram.domain.repository.ProfileRepository
import com.suyzissgram.domain.repository.SettingsRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    private val localAuthRepository = LocalAuthRepository(preferences)
    val authRepository: AuthRepository = localAuthRepository
    val chatRepository: ChatRepository = FakeChatRepository(appContext, preferences) {
        val userId = localAuthRepository.currentSession()?.id.orEmpty()
        val purchasedPremium = preferences.getBoolean("premium_active_$userId", false)
        PremiumPolicy.favoritesLimitGb(userId, purchasedPremium)
    }
    val settingsRepository: SettingsRepository = FakeSettingsRepository(preferences)
    val profileRepository: ProfileRepository = FakeProfileRepository(preferences)

    fun isPremiumUser(userId: String): Boolean {
        val purchasedPremium = preferences.getBoolean("premium_active_$userId", false)
        return PremiumPolicy.isPremium(userId, purchasedPremium)
    }

    fun favoritesLimitGb(userId: String): Int {
        val purchasedPremium = preferences.getBoolean("premium_active_$userId", false)
        return PremiumPolicy.favoritesLimitGb(userId, purchasedPremium)
    }

    private companion object {
        const val PREFERENCES_NAME = "suyzissgram_local"
    }
}
