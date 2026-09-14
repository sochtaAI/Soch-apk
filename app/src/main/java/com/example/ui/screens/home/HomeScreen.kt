package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
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
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PostCard
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    repository: SochRepository,
    onCreatePostClick: () -> Unit,
    onCommentClick: (String) -> Unit, // postId
    onProfileClick: () -> Unit,
    onUserClick: (String) -> Unit, // username
    onNotificationsClick: () -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    val posts by repository.posts.collectAsState()
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    val firstName = currentUser?.name?.split(" ")?.firstOrNull() ?: "Thinker"

    Scaffold(
        containerColor = SochBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SochBackground)
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "GOOD TO SEE YOU",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SochPrimary,
                                letterSpacing = 1.8.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Hey, $firstName 👋",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = SochTextPrimary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconButton(
                                onClick = onNotificationsClick,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SochSurface)
                                    .border(1.dp, SochBorder, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = SochTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { onProfileClick() }
                            ) {
                                UserAvatar(
                                    name = currentUser?.name ?: "User",
                                    avatarUrl = currentUser?.avatarUrl.orEmpty(),
                                    size = 40.dp,
                                    showOnlineIndicator = true,
                                    isOnline = true
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "What's happening in your SOCH today?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SochTextSecondary
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Create Post Trigger Card
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SochSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SochBorder, RoundedCornerShape(18.dp))
                            .clickable { onCreatePostClick() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            UserAvatar(
                                name = currentUser?.name ?: "User",
                                avatarUrl = currentUser?.avatarUrl.orEmpty(),
                                size = 36.dp
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "Share what's on your mind...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SochTextMuted,
                                modifier = Modifier.weight(1f)
                            )

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(SochPrimary)
                                    .border(1.dp, SochPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Create post",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Feed Title & Refresh
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Your Feed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SochTextPrimary
                        )

                        IconButton(
                            onClick = {
                                isRefreshing = true
                                repository.refreshFeed()
                                scope.launch {
                                    kotlinx.coroutines.delay(600)
                                    isRefreshing = false
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh feed",
                                tint = if (isRefreshing) SochPrimary else SochTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Feed Items or Empty State
            if (posts.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.ChatBubble,
                        title = "Your SOCH starts here.",
                        subtitle = "Be the first to share an idea, question, or reflection with the community.",
                        actionLabel = "Create your first post",
                        onActionClick = onCreatePostClick,
                        modifier = Modifier.padding(top = 40.dp)
                    )
                }
            } else {
                items(posts, key = { it.id }) { post ->
                    PostCard(
                        post = post,
                        currentUserId = currentUser?.id,
                        onLikeClick = { repository.toggleLike(post.id) },
                        onCommentClick = { onCommentClick(post.id) },
                        onAuthorClick = { username -> onUserClick(username) },
                        onDeleteClick = {
                            scope.launch { repository.deletePost(post.id) }
                        },
                        onReportClick = {
                            scope.launch {
                                repository.reportContent("post", post.id, "Inappropriate content")
                            }
                        },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
