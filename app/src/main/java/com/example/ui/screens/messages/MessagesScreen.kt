package com.example.ui.screens.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.SochRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SochTopBar
import com.example.ui.components.TimeUtils
import com.example.ui.components.UserAvatar
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.*

@Composable
fun MessagesScreen(
    repository: SochRepository,
    onBackClick: () -> Unit,
    onConversationClick: (String) -> Unit // conversationId
) {
    val conversations by repository.conversations.collectAsState()

    Scaffold(
        containerColor = SochBackground,
        topBar = {
            SochTopBar(
                title = "Messages",
                subtitle = "Private discussions & direct messages",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        if (conversations.isEmpty()) {
            EmptyStateView(
                icon = Icons.Outlined.ChatBubbleOutline,
                title = "No messages yet",
                subtitle = "Your conversations will appear here. Start a chat by tapping Message on any profile.",
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(conversations, key = { it.id }) { conv ->
                    val user = conv.otherUser

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SochSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(0.8.dp, SochBorder, RoundedCornerShape(16.dp))
                            .clickable { onConversationClick(conv.id) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            UserAvatar(
                                name = user.name,
                                avatarUrl = user.avatarUrl,
                                size = 48.dp,
                                showOnlineIndicator = true,
                                isOnline = user.isOnline
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = user.name,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = SochTextPrimary
                                        )
                                        if (user.isVerified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            VerifiedBadge()
                                        }
                                    }

                                    Text(
                                        text = TimeUtils.formatRelativeTime(conv.lastMessageTime),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SochTextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = conv.lastMessage,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (conv.unreadCount > 0) SochTextPrimary else SochTextSecondary,
                                        fontWeight = if (conv.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (conv.unreadCount > 0) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(SochPrimary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = conv.unreadCount.toString(),
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
