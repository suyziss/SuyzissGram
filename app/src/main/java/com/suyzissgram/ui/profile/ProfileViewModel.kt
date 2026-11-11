package com.suyzissgram.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suyzissgram.domain.model.UserProfile
import com.suyzissgram.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(
    private val repository: ProfileRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeProfile().collect { profile ->
                _uiState.update {
                    it.copy(profile = profile, isLoading = false, error = null)
                }
            }
        }
    }

    fun onDisplayNameChanged(value: String) = updateProfile {
        it.copy(displayName = value)
    }

    fun onUsernameChanged(value: String) = updateProfile {
        it.copy(username = value)
    }

    fun onBioChanged(value: String) = updateProfile {
        it.copy(bio = value)
    }

    fun onAvatarChanged(uri: String?) = updateProfile {
        it.copy(avatarUri = uri)
    }

    fun save() = viewModelScope.launch {
        val current = _uiState.value.profile
        _uiState.update { it.copy(isSaving = true, saved = false, error = null) }
        runCatching { repository.updateProfile(current) }
            .onSuccess {
                _uiState.update {
                    it.copy(isSaving = false, saved = true)
                }
            }
            .onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saved = false,
                        error = throwable.message ?: "Unable to save profile"
                    )
                }
            }
    }

    private fun updateProfile(transform: (UserProfile) -> UserProfile) {
        _uiState.update {
            it.copy(profile = transform(it.profile), saved = false, error = null)
        }
    }
}
