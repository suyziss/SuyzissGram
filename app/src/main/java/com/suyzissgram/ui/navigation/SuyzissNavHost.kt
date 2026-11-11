package com.suyzissgram.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.suyzissgram.ui.auth.AuthScreen
import com.suyzissgram.ui.auth.AuthViewModel
import com.suyzissgram.ui.chat.ChatScreen
import com.suyzissgram.ui.chat.ChatViewModel
import com.suyzissgram.ui.chatlist.ChatListScreen
import com.suyzissgram.ui.chatlist.ChatListViewModel
import com.suyzissgram.ui.common.viewModelFactory
import com.suyzissgram.ui.i18n.AppStrings
import com.suyzissgram.ui.gifts.GiftViewModel
import com.suyzissgram.ui.gifts.GiftsScreen
import com.suyzissgram.ui.profile.ProfileScreen
import com.suyzissgram.ui.profile.ProfileViewModel
import com.suyzissgram.ui.settings.SettingsScreen
import com.suyzissgram.ui.settings.SettingsViewModel
import com.suyzissgram.ui.theme.SuyzissTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private object Routes {
    const val AUTH = "auth"
    const val CHATS = "chats"
    const val CHAT = "chat/{chatId}"
    const val SETTINGS = "settings"
    const val PROFILE = "profile"
    const val GIFTS = "gifts"
    fun chat(chatId: String) = "chat/$chatId"
}

@Composable
fun SuyzissNavHost(container: AppContainer) {
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = viewModelFactory { SettingsViewModel(container.settingsRepository) }
    )
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val session by container.authRepository.observeSession()
        .collectAsStateWithLifecycle(initialValue = container.authRepository.currentSession())
    val labels = AppStrings.forLanguage(settingsState.settings.language)
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val startDestination = if (session == null) Routes.AUTH else Routes.CHATS

    SuyzissTheme(
        themeChoice = settingsState.settings.theme,
        textScale = settingsState.settings.textScale
    ) {
        NavHost(navController = navController, startDestination = startDestination) {
            composable(Routes.AUTH) {
                val viewModel: AuthViewModel = viewModel(
                    factory = viewModelFactory { AuthViewModel(container.authRepository) }
                )
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(state.authenticatedUser) {
                    val user = state.authenticatedUser ?: return@LaunchedEffect
                    val currentProfile = container.profileRepository.observeProfile().first()
                    container.profileRepository.updateProfile(
                        currentProfile.copy(
                            id = user.id,
                            email = user.email,
                            displayName = currentProfile.displayName.ifBlank { user.username },
                            username = user.username
                        )
                    )
                    navController.navigate(Routes.CHATS) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                        launchSingleTop = true
                    }
                }

                AuthScreen(
                    state = state,
                    language = settingsState.settings.language,
                    onEmailChange = viewModel::setEmail,
                    onUsernameChange = viewModel::setUsername,
                    onPasswordChange = viewModel::setPassword,
                    onSubmit = viewModel::submit
                )
            }

            composable(Routes.CHATS) {
                val viewModel: ChatListViewModel = viewModel(
                    factory = viewModelFactory { ChatListViewModel(container.chatRepository) }
                )
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                ChatListScreen(
                    state = state,
                    labels = labels,
                    onOpenChat = { navController.navigate(Routes.chat("saved")) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onOpenProfile = { navController.navigate(Routes.PROFILE) }
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
                    onSendAttachment = viewModel::sendAttachment,
                    onSendLocation = viewModel::sendLocation,
                    chatTheme = settingsState.settings.chatTheme,
                    onChatThemeChange = settingsViewModel::setChatTheme,
                    onOpenGifts = { navController.navigate(Routes.GIFTS) },
                    onBack = { navController.popBackStack() },
                    onOpenProfile = { navController.navigate(Routes.PROFILE) }
                )
            }

            composable(Routes.GIFTS) {
                val giftViewModel: GiftViewModel = viewModel(
                    factory = viewModelFactory { GiftViewModel(container.giftRepository) }
                )
                val giftState by giftViewModel.uiState.collectAsStateWithLifecycle()
                GiftsScreen(
                    state = giftState,
                    language = settingsState.settings.language,
                    onBack = { navController.popBackStack() },
                    onSendGift = giftViewModel::sendGift,
                    onClearError = giftViewModel::clearError
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
                    onAccentColorChange = viewModel::onAccentColorChanged,
                    onSave = viewModel::save
                )
            }
        }
    }
}
