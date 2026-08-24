package com.chat.shutup.ui.chat_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chat.shutup.domain.model.Chat
import com.chat.shutup.domain.model.User
import com.chat.shutup.ui.components.Avatar
import com.chat.shutup.ui.theme.ShutUpChatTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    viewModel: ChatListViewModel = hiltViewModel(),
    onChatClick: (String) -> Unit,
    onTripsClick: () -> Unit = {},
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats") },
                actions = {
                    IconButton(onClick = onTripsClick) {
                        Icon(Icons.Default.Groups, contentDescription = "Trips")
                    }
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = onProfileClick) {
                        Avatar(imageUrl = null, name = "Me", size = 32.dp)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onSearchClick) {
                Icon(Icons.Default.Add, contentDescription = "Start New Chat")
            }
        },
        snackbarHost = {
            uiState.error?.let { error ->
                Snackbar(
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(text = error)
                }
            }
        }
    ) { padding ->
        if (uiState.chats.isEmpty() && !uiState.isLoading) {
            EmptyChatListContent(
                availableUsers = uiState.availableUsers,
                currentUserId = uiState.currentUserId,
                onUserClick = { userId ->
                    viewModel.onUserClick(userId, onChatClick)
                },
                modifier = Modifier.padding(padding)
            )
        } else {
            ChatListContent(
                chats = uiState.chats,
                onChatClick = onChatClick,
                isLoading = uiState.isLoading,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun EmptyChatListContent(
    availableUsers: List<User>,
    currentUserId: String,
    onUserClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No chats yet. Start one now!",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        OutlinedButton(
            onClick = { onUserClick(currentUserId) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Person, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Chat with yourself (Saved Messages)")
        }

        if (availableUsers.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Or start a conversation:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(availableUsers) { user ->
                    AvailableUserItem(user = user, onClick = { onUserClick(user.id) })
                }
            }
        }
    }
}

@Composable
fun AvailableUserItem(
    user: User,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            imageUrl = user.imageUrl,
            name = user.name,
            size = 48.dp
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = user.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ChatListContent(
    chats: List<Chat>,
    onChatClick: (String) -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (chats.isEmpty() && !isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No chats yet. Start a new one!")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(chats) { chat ->
                    ChatListItem(chat = chat, onClick = { onChatClick(chat.id) })
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 72.dp))
                }
            }
        }
        
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun ChatListItem(
    chat: Chat,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            imageUrl = chat.participantImageUrl,
            name = chat.participantName,
            size = 56.dp,
            onlineStatus = chat.online
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.participantName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (chat.lastMessageTimestamp > 0) {
                    Text(
                        text = formatTimestamp(chat.lastMessageTimestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                if (chat.unreadCount > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.extraSmall,
                        modifier = Modifier.sizeIn(minWidth = 20.dp, minHeight = 20.dp)
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(4.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Preview(showBackground = true)
@Composable
fun ChatListScreenPreview() {
    ShutUpChatTheme {
        ChatListContent(
            chats = listOf(
                Chat(
                    id = "1",
                    participantName = "John Doe",
                    lastMessage = "Hey, how are you?",
                    lastMessageTimestamp = System.currentTimeMillis(),
                    unreadCount = 2,
                    online = true
                ),
                Chat(
                    id = "2",
                    participantName = "Jane Smith",
                    lastMessage = "See you later!",
                    lastMessageTimestamp = System.currentTimeMillis() - 3600000
                )
            ),
            onChatClick = {}
        )
    }
}
