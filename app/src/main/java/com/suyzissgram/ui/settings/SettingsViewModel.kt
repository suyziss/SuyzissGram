package com.suyzissgram.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suyzissgram.domain.model.AppLanguage
import com.suyzissgram.domain.model.AppSettings
import com.suyzissgram.domain.model.ThemeChoice
import com.suyzissgram.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val isLoading: Boolean = true
)

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeSettings().collect { settings ->
                _uiState.update { it.copy(settings = settings, isLoading = false) }
            }
        }
    }

    fun setLanguage(value: AppLanguage) = viewModelScope.launch {
        repository.setLanguage(value)
    }

    fun setTheme(value: ThemeChoice) = viewModelScope.launch {
        repository.setTheme(value)
    }

    fun setTextScale(value: Float) = viewModelScope.launch {
        repository.setTextScale(value)
    }

    fun setChatTheme(value: Int) = viewModelScope.launch {
        repository.setChatTheme(value)
    }

    fun setOnlyPremiumMessages(value: Boolean) = viewModelScope.launch {
        repository.setOnlyPremiumMessages(value)
    }

    fun clearMessages(onComplete: () -> Unit) = viewModelScope.launch {
        onComplete()
    }
}
