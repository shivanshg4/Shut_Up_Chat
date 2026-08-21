package com.chat.shutup.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.shutup.domain.model.User
import com.chat.shutup.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        if (query.length >= 3) {
            searchUsers(query)
        } else {
            _uiState.update { it.copy(users = emptyList()) }
        }
    }

    private fun searchUsers(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.searchUsers(query).fold(
                onSuccess = { users ->
                    _uiState.update { it.copy(users = users, isLoading = false) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun onUserClick(user: User, onChatStarted: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getOrCreateChat(user.id).fold(
                onSuccess = { chatId ->
                    _uiState.update { it.copy(isLoading = false) }
                    onChatStarted(chatId)
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }
}

data class SearchUiState(
    val query: String = "",
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
