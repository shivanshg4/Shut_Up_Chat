package com.chat.shutup.ui.chat_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.shutup.domain.model.Chat
import com.chat.shutup.domain.model.User
import com.chat.shutup.domain.repository.AuthRepository
import com.chat.shutup.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(currentUserId = authRepository.currentUser?.uid ?: "") }
        observeChats()
        fetchAvailableUsers()
    }

    private fun observeChats() {
        repository.getChats()
            .onEach { chats ->
                _uiState.update { it.copy(chats = chats, isLoading = false) }
            }
            .catch { e ->
                android.util.Log.e("ChatListViewModel", "Error fetching chats", e)
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    private fun fetchAvailableUsers() {
        viewModelScope.launch {
            repository.getAllUsers().onSuccess { allUsers ->
                val currentUid = authRepository.currentUser?.uid
                val otherUsers = allUsers.filter { it.id != currentUid }
                _uiState.update { 
                    it.copy(
                        availableUsers = otherUsers, 
                        currentUserId = currentUid ?: ""
                    ) 
                }
            }.onFailure { e ->
                android.util.Log.e("ChatListViewModel", "Error fetching users", e)
            }
        }
    }

    fun onUserClick(userId: String, onChatStarted: (String) -> Unit) {
        viewModelScope.launch {
            repository.getOrCreateChat(userId).onSuccess { chatId ->
                onChatStarted(chatId)
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class ChatListUiState(
    val chats: List<Chat> = emptyList(),
    val availableUsers: List<User> = emptyList(),
    val currentUserId: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)
