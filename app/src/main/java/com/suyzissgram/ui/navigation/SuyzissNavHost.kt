package com.suyzissgram.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.suyzissgram.di.AppContainer
import com.suyzissgram.domain.model.AppLanguage
import com.suyzissgram.ui.chat.ChatScreen
import com.suyzissgram.ui.chat.ChatViewModel
import com.suyzissgram.ui.chatlist.ChatListScreen
import com.suyzissgram.ui.chatlist.ChatListViewModel
import com.suyzissgram.ui.common.viewModelFactory
import com.suyzissgram.ui.i18n.AppStrings
import com.suyzissgram.ui.profile.ProfileScreen
import com.suyzissgram.ui.profile.ProfileViewModel
import com.suyzissgram.ui.settings.SettingsScreen
import com.suyzissgram.ui.settings.SettingsViewModel
import com.suyzissgram.ui.theme.SuyzissTheme
import kotlinx.coroutines.launch

private object Routes {
    const val CHATS = "chats"
    const val CHAT = "chat/{chatId}"
    const val SETTINGS = "settings"
    const val PROFILE = "profile"
    fun chat(chatId: String) = "chat/$chatId"
}

@Composable
fun SuyzissNavHost(container: AppContainer) {
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = viewModelFactory { SettingsViewModel(container.settingsRepository) }
    )
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val labels = AppStrings.forLanguage(settingsState.settings.language)
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    SuyzissTheme(
        themeChoice = settingsState.settings.theme,
        textScale = settingsState.settings.textScale
    ) {
        NavHost(navController = navController, startDestination = Routes.CHATS) {
            composable(Routes.CHATS) {
                val viewModel: ChatListViewModel = viewModel(
                    factory = viewModelFactory { ChatListViewModel(container.chatRepository) }
                )
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                ChatListScreen(
                    state = state,
                    labels = labels,
                    onOpenChat = { navController.navigate(Routes.chat("saved")) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) }
                )
            }

            composable(
                route = Routes.CHAT,
                arguments = listOf(navArgument("chatId") { type = NavType.StringType })
            ) {
                val viewModel: ChatViewModel = viewModel(
                    factory = viewModelFactory { ChatViewModel(container.chatRepository) }
                )
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                ChatScreen(
                    state = state,
                    labels = labels,
                    onDraftChange = viewModel::onDraftChanged,
                    onSend = viewModel::sendMessage,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    state = settingsState,
                    labels = labels,
                    onBack = { navController.popBackStack() },
                    onOpenProfile = { navController.navigate(Routes.PROFILE) },
                    onLanguage = settingsViewModel::setLanguage,
                    onTheme = settingsViewModel::setTheme,
                    onTextScale = settingsViewModel::setTextScale,
                    onClearMessages = {
                        scope.launch { container.chatRepository.clearMessages() }
                    }
                )
            }

            composable(Routes.PROFILE) {
                val viewModel: ProfileViewModel = viewModel(
                    factory = viewModelFactory { ProfileViewModel(container.profileRepository) }
                )
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                ProfileScreen(
                    state = state,
                    language = settingsState.settings.language,
                    onBack = { navController.popBackStack() },
                    onDisplayNameChange = viewModel::onDisplayNameChanged,
                    onUsernameChange = viewModel::onUsernameChanged,
                    onBioChange = viewModel::onBioChanged,
                    onAvatarChange = viewModel::onAvatarChanged,
                    onSave = viewModel::save
                )
            }
        }
    }
}
