package com.suyzissgram.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suyzissgram.domain.model.AttachmentKind
import com.suyzissgram.domain.model.ChatMessage
import com.suyzissgram.domain.model.MediaQuality
import com.suyzissgram.domain.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val draft: String = "",
    val isLoading: Boolean = true,
    val isSendingAttachment: Boolean = false,
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

    fun sendAttachment(
        sourceUri: String,
        displayName: String,
        mimeType: String?,
        kind: AttachmentKind,
        quality: MediaQuality
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSendingAttachment = true, error = null) }
            runCatching {
                repository.sendAttachment(sourceUri, displayName, mimeType, kind, quality)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(error = if (throwable.message == "FAVORITES_LIMIT_REACHED") "Достигнут лимит хранилища избранного. Удалите часть вложений или увеличьте лимит с Premium." else (throwable.message ?: "Unable to save attachment"))
                }
            }
            _uiState.update { it.copy(isSendingAttachment = false) }
        }
    }

    fun sendLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            runCatching { repository.sendLocation(latitude, longitude) }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(error = throwable.message ?: "Unable to attach location")
                    }
                }
        }
    }
}
