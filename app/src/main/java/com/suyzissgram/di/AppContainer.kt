package com.suyzissgram.di

import android.content.Context
import com.suyzissgram.data.repository.FakeChatRepository
import com.suyzissgram.data.repository.FakeProfileRepository
import com.suyzissgram.data.repository.FakeSettingsRepository
import com.suyzissgram.domain.repository.ChatRepository
import com.suyzissgram.domain.repository.ProfileRepository
import com.suyzissgram.domain.repository.SettingsRepository

class AppContainer(context: Context) {
    private val preferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    val chatRepository: ChatRepository = FakeChatRepository(preferences)
    val settingsRepository: SettingsRepository = FakeSettingsRepository(preferences)
    val profileRepository: ProfileRepository = FakeProfileRepository(preferences)

    private companion object {
        const val PREFERENCES_NAME = "suyzissgram_local"
    }
}
