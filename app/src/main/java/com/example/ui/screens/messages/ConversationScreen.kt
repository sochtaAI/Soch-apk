package com.example.ui.screens.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.SochRepository
import com.example.ui.components.SochTopBar
import com.example.ui.components.TimeUtils
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ConversationScreen(
    conversationId: String,
    repository: SochRepository,
    onBackClick: () -> Unit
) {
    val conversations by repository.conversations.collectAsState()
    val messagesMap by repository.messages.collectAsState()
    val currentUser by repository.currentUser.collectAsState()

    val conv = conversations.find { it.id == conversationId }
    val messages = messagesMap[conversationId] ?: emptyList()

    var messageInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val otherUser = conv?.otherUser

    Scaffold(
        containerColor = SochBackground,
        topBar = {
            SochTopBar(
                title = otherUser?.name ?: "Chat",
                subtitle = if (otherUser?.isOnline == true) "Active now" else "@${otherUser?.username}",
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            Surface(
                color = SochSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.8.dp, SochBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    TextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        placeholder = {
                            Text(
                                text = "Message ${otherUser?.name ?: ""}...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SochTextMuted
                            )
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = SochTextPrimary,
                            unfocusedTextColor = SochTextPrimary
                        )
                    )

                    IconButton(
                        onClick = {
                            if (messageInput.isNotBlank()) {
                                val text = messageInput.trim()
                                messageInput = ""
                                scope.launch {
                                    repository.sendMessage(conversationId, text)
                                }
                            }
                        },
                        enabled = messageInput.isNotBlank(),
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (messageInput.isNotBlank()) SochPrimary else SochSurfaceElevated)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send message",
                            tint = if (messageInput.isNotBlank()) Color.White else SochTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isMe = msg.senderId == currentUser?.id

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (!isMe) 2.dp else 16.dp,
                            bottomEnd = if (isMe) 2.dp else 16.dp
                        ),
                        color = if (isMe) SochPrimary else SochSurfaceElevated,
                        border = if (isMe) null else androidx.compose.foundation.BorderStroke(0.8.dp, SochBorder),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Text(
                                text = msg.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SochTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = TimeUtils.formatRelativeTime(msg.createdAt),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = if (isMe) SochTextSecondary else SochTextMuted,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }
        }
    }
}
