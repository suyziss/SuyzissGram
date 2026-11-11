package com.suyzissgram.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suyzissgram.domain.model.ChatMessage
import com.suyzissgram.domain.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val draft: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

class ChatViewModel(
    private val repository: ChatRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeMessages().collect { messages ->
                _uiState.update {
                    it.copy(messages = messages, isLoading = false, error = null)
                }
            }
        }
    }

    fun onDraftChanged(value: String) {
        _uiState.update { it.copy(draft = value) }
    }

    fun sendMessage() {
        val text = _uiState.value.draft.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            runCatching { repository.sendMessage(text) }
                .onSuccess { _uiState.update { it.copy(draft = "") } }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message ?: "Unable to send message") }
                }
        }
    }
}
