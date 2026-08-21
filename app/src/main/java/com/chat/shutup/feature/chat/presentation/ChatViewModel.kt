package com.chat.shutup.feature.chat.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chat.shutup.domain.repository.AuthRepository
import com.chat.shutup.domain.repository.ChatRepository
import com.chat.shutup.feature.chat.domain.model.ChatDetail
import com.chat.shutup.feature.chat.domain.model.Message
import com.chat.shutup.feature.chat.domain.model.MessageStatus
import com.chat.shutup.feature.chat.domain.model.MessageType
import com.chat.shutup.feature.chat.presentation.effect.ChatUiEffect
import com.chat.shutup.feature.chat.presentation.event.ChatUiEvent
import com.chat.shutup.feature.chat.presentation.state.ChatUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val chatId: String = checkNotNull(savedStateHandle["chatId"])

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<ChatUiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        _uiState.update { it.copy(currentUserId = authRepository.currentUser?.uid ?: "") }
        observeMessages()
        observeChatDetail()
    }

    private fun observeMessages() {
        chatRepository.getMessages(chatId)
            .onEach { messages ->
                _uiState.update { it.copy(messages = messages, isLoading = false) }
            }
            .catch { e ->
                _uiEffect.send(ChatUiEffect.ShowError("Failed to load messages: ${e.message}"))
                _uiState.update { it.copy(isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeChatDetail() {
        _uiState.update { it.copy(isLoading = true) }
        chatRepository.getChat(chatId)
            .onEach { chat ->
                chat?.let {
                    _uiState.update { state ->
                        state.copy(
                            chatDetail = ChatDetail(
                                id = it.participantId, // Use participantId here
                                participantName = it.participantName,
                                participantImageUrl = it.participantImageUrl,
                                online = it.online
                            ),
                            isLoading = false
                        )
                    }
                }
            }
            .catch { e ->
                _uiEffect.send(ChatUiEffect.ShowError("Failed to load chat details: ${e.message}"))
                _uiState.update { it.copy(isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: ChatUiEvent) {
        when (event) {
            is ChatUiEvent.OnMessageChange -> {
                _uiState.update { it.copy(messageInput = event.message) }
            }
            ChatUiEvent.OnSendMessage -> sendMessage()
            ChatUiEvent.OnBackClick -> {
                viewModelScope.launch { _uiEffect.send(ChatUiEffect.NavigateBack) }
            }
            ChatUiEvent.OnCallClick -> initiateCall(com.chat.shutup.domain.model.CallType.VOICE)
            ChatUiEvent.OnVideoCallClick -> initiateCall(com.chat.shutup.domain.model.CallType.VIDEO)
            is ChatUiEvent.OnReactionClick -> {

                val updatedMessages = uiState.value.messages.map { message ->

                    if (message.id == event.messageId) {
                        message.copy(
                            reaction = event.reaction
                        )
                    } else {
                        message
                    }
                }

                _uiState.update {
                    it.copy(
                        messages = updatedMessages
                    )
                }

                // Later:
                // repository.updateReaction(
                //     messageId = event.messageId,
                //     reaction = event.reaction
                // )
            }
            else -> {}
        }
    }

    private fun initiateCall(type: com.chat.shutup.domain.model.CallType) {
        val chatDetail = _uiState.value.chatDetail ?: return
        viewModelScope.launch {
            // In a real app, we'd find the participantId from the chat participants
            // For now, we assume chatDetail.id is the other user's ID if it's a 1-on-1 chat
            chatRepository.initiateCall(chatDetail.id, type).onFailure { e ->
                _uiEffect.send(ChatUiEffect.ShowError(e.message ?: "Failed to initiate call"))
            }
        }
    }

    private fun sendMessage() {
        val currentInput = _uiState.value.messageInput
        if (currentInput.isBlank()) return

        val currentUserId = authRepository.currentUser?.uid ?: return

        val newMessage = Message(
            id = UUID.randomUUID().toString(),
            text = currentInput,
            senderId = currentUserId,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT, // Realtime DB will update this
            type = MessageType.TEXT
        )

        viewModelScope.launch {
            _uiState.update { it.copy(messageInput = "") }
            chatRepository.sendMessage(chatId, newMessage).onFailure { e ->
                _uiEffect.send(ChatUiEffect.ShowError(e.message ?: "Failed to send message"))
            }
        }
    }


}
