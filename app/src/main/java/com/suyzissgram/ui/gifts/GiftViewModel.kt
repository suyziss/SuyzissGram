package com.suyzissgram.ui.gifts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suyzissgram.domain.model.GiftCatalog
import com.suyzissgram.domain.model.GiftWallet
import com.suyzissgram.domain.repository.GiftRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GiftUiState(
    val wallet: GiftWallet = GiftWallet(),
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val error: String? = null
)

class GiftViewModel(
    private val repository: GiftRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GiftUiState())
    val uiState = _uiState.asStateFlow()

    val gifts = GiftCatalog.items

    init {
        viewModelScope.launch {
            repository.observeWallet().collect { wallet ->
                _uiState.update { it.copy(wallet = wallet, isLoading = false) }
            }
        }
    }

    fun sendGift(giftId: String) {
        if (_uiState.value.isSending) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }
            runCatching { repository.sendGift(giftId) }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(error = error.message ?: "Unable to send demo gift")
                    }
                }
            _uiState.update { it.copy(isSending = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
