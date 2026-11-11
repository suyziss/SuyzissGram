package com.suyzissgram.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suyzissgram.domain.model.AuthUser
import com.suyzissgram.domain.repository.AuthRepository
import com.suyzissgram.domain.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode {
    LOGIN,
    REGISTER
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.REGISTER,
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val authenticatedUser: AuthUser? = null
)

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    fun setMode(mode: AuthMode) {
        _uiState.update { it.copy(mode = mode, error = null) }
    }

    fun setEmail(value: String) = update { it.copy(email = value, error = null) }
    fun setUsername(value: String) = update { it.copy(username = value.removePrefix("@").take(14), error = null) }
    fun setPassword(value: String) = update { it.copy(password = value, error = null) }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading) return
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = when (state.mode) {
                AuthMode.REGISTER -> repository.register(
                    state.email,
                    state.username,
                    state.password.toCharArray()
                )
                AuthMode.LOGIN -> repository.login(
                    state.email,
                    state.password.toCharArray()
                )
            }

            when (result) {
                is AuthResult.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        password = "",
                        error = null,
                        authenticatedUser = result.user
                    )
                }
                is AuthResult.Error -> _uiState.update {
                    it.copy(isLoading = false, password = "", error = result.message)
                }
            }
        }
    }

    private fun update(transform: (AuthUiState) -> AuthUiState) {
        _uiState.update(transform)
    }
}
