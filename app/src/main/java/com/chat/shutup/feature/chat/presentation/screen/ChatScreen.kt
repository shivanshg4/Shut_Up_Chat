package com.chat.shutup.feature.chat.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.chat.shutup.feature.chat.domain.model.ChatDetail
import com.chat.shutup.feature.chat.domain.model.Message
import com.chat.shutup.feature.chat.domain.model.MessageStatus
import com.chat.shutup.feature.chat.domain.model.MessageType
import com.chat.shutup.feature.chat.presentation.ChatViewModel
import com.chat.shutup.feature.chat.presentation.effect.ChatUiEffect
import com.chat.shutup.feature.chat.presentation.event.ChatUiEvent
import com.chat.shutup.feature.chat.presentation.state.ChatUiState
import com.chat.shutup.ui.components.chat.ChatTopBar
import com.chat.shutup.ui.components.chat.MessageBubble
import com.chat.shutup.ui.components.chat.MessageComposer
import com.chat.shutup.ui.components.chat.TypingIndicator
import com.chat.shutup.ui.theme.AppTheme
import com.chat.shutup.ui.theme.ShutUpChatTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is ChatUiEffect.NavigateBack -> onNavigateBack()
                is ChatUiEffect.ShowError -> {
                    // Show snackbar or toast
                }
            }
        }
    }

    ChatContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun ChatContent(
    uiState: ChatUiState,
    onEvent: (ChatUiEvent) -> Unit
) {
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size, uiState.isTyping) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size)
        }
    }

    Scaffold(
        topBar = {
            uiState.chatDetail?.let { chat ->
                ChatTopBar(
                    name = chat.participantName,
                    status = chat.lastSeen ?: "",
                    imageUrl = chat.participantImageUrl,
                    online = chat.online,
                    onBackClick = { onEvent(ChatUiEvent.OnBackClick) },
                    onCallClick = { onEvent(ChatUiEvent.OnCallClick) },
                    onVideoCallClick = { onEvent(ChatUiEvent.OnVideoCallClick) },
                    onMoreClick = { onEvent(ChatUiEvent.OnMoreClick) }
                )
            }
        },
        bottomBar = {
            MessageComposer(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding(),
                value = uiState.messageInput,
                onValueChange = { onEvent(ChatUiEvent.OnMessageChange(it)) },
                onSendClick = { onEvent(ChatUiEvent.OnSendMessage) },
                onAttachmentClick = { onEvent(ChatUiEvent.OnAttachmentClick) },
                onCameraClick = { onEvent(ChatUiEvent.OnCameraClick) },
                onEmojiClick = { onEvent(ChatUiEvent.OnEmojiClick) },
                onVoiceRecordClick = { onEvent(ChatUiEvent.OnVoiceMessageRecord) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(AppTheme.spacing.medium)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    MessageBubble(
                        text = message.text,
                        timestamp = message.timestamp,
                        isOutgoing = message.senderId == uiState.currentUserId,
                        status = message.status,
                        reaction = message.reaction,
                        onReactionSelected = { emoji ->
                            onEvent(
                                ChatUiEvent.OnReactionClick(
                                    messageId = message.id,
                                    reaction = emoji
                                )
                            )
                        }
                    )
                }

                if (uiState.isTyping) {
                    item(key = "typing_indicator") {
                        TypingIndicator()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatContentPreview() {
    ShutUpChatTheme {
        ChatContent(
            uiState = ChatUiState(
                chatDetail = ChatDetail(
                    id = "1",
                    participantName = "Elena Rossi",
                    participantImageUrl = null,
                    online = true,
                    lastSeen = "Online"
                ),
                messages = listOf(
                    Message(
                        id = "1",
                        text = "Hey! Are we still on for coffee later?",
                        senderId = "2",
                        timestamp = System.currentTimeMillis() - 3600000,
                        status = MessageStatus.READ,
                        type = MessageType.TEXT
                    ),
                    Message(
                        id = "2",
                        text = "Absolutely! Thinking of trying that new place on 5th.",
                        senderId = "1",
                        timestamp = System.currentTimeMillis() - 3300000,
                        status = MessageStatus.READ,
                        type = MessageType.TEXT
                    )
                ),
                isTyping = true
            ),
            onEvent = {}
        )
    }
}
