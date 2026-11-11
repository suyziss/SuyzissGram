package com.suyzissgram.ui.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suyzissgram.domain.model.ChatPreview
import com.suyzissgram.domain.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatListUiState(
    val chats: List<ChatPreview> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ChatListViewModel(
    repository: ChatRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeChats().collect { chats ->
                _uiState.update {
                    it.copy(chats = chats, isLoading = false, error = null)
                }
            }
        }
    }
}
