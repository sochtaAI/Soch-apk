package com.example.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.NotificationType
import com.example.data.repository.SochRepository
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SochTopBar
import com.example.ui.components.TimeUtils
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*

@Composable
fun NotificationsScreen(
    repository: SochRepository,
    onBackClick: () -> Unit,
    onUserClick: (String) -> Unit
) {
    val notifications by repository.notifications.collectAsState()

    Scaffold(
        containerColor = SochBackground,
        topBar = {
            SochTopBar(
                title = "Notifications",
                subtitle = "Updates on your discussions & network",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        if (notifications.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.NotificationsNone,
                title = "No notifications yet",
                subtitle = "When people interact with your thoughts or follow you, you'll see it here.",
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (!notif.read) SochSurfaceElevated else SochSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(0.8.dp, SochBorder, RoundedCornerShape(16.dp))
                            .clickable {
                                notif.actor?.username?.let { onUserClick(it) }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Box {
                                UserAvatar(
                                    name = notif.actor?.name ?: "User",
                                    avatarUrl = notif.actor?.avatarUrl.orEmpty(),
                                    size = 44.dp
                                )

                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(
                                            when (notif.type) {
                                                NotificationType.LIKE -> SochError
                                                NotificationType.FOLLOW -> SochPrimary
                                                NotificationType.COMMENT -> SochSecondary
                                                else -> SochPrimary
                                            }
                                        )
                                        .border(1.dp, SochBackground, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (notif.type) {
                                            NotificationType.LIKE -> Icons.Default.Favorite
                                            NotificationType.FOLLOW -> Icons.Default.PersonAdd
                                            else -> Icons.Outlined.ChatBubbleOutline
                                        },
                                        contentDescription = null,
                                        tint = SochTextPrimary,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notif.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SochTextPrimary,
                                    fontWeight = if (!notif.read) FontWeight.SemiBold else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = TimeUtils.formatRelativeTime(notif.createdAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SochTextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
