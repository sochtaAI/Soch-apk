package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ChatBubbleOutline
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
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PublicUserProfileScreen(
    username: String,
    repository: SochRepository,
    onBackClick: () -> Unit,
    onCommentClick: (String) -> Unit,
    onMessageClick: (String) -> Unit // conversationId
) {
    val user = remember(username) { repository.getProfileByUsername(username) }
    val followedUsers by repository.followedUserIds.collectAsState()
    val currentUser by repository.currentUser.collectAsState()
    val scope = rememberCoroutineScope()

    if (user == null) {
        Scaffold(
            containerColor = SochBackground,
            topBar = { SochTopBar(title = "Profile", onBackClick = onBackClick) }
        ) { padding ->
            EmptyStateView(
                icon = Icons.Outlined.ChatBubbleOutline,
                title = "Thinker not found",
                subtitle = "The profile @$username could not be located.",
                actionLabel = "Go Back",
                onActionClick = onBackClick,
                modifier = Modifier.padding(padding)
            )
        }
        return
    }

    val isFollowing = followedUsers.contains(user.id)
    val isMe = currentUser?.id == user.id
    val userPosts = repository.getPostsByUserId(user.id)
    val isOfficial = user.username == "dipak.dev"

    Scaffold(
        containerColor = SochBackground,
        topBar = {
            SochTopBar(
                title = "@${user.username}",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Avatar & Info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        UserAvatar(
                            name = user.name,
                            avatarUrl = user.avatarUrl,
                            size = 72.dp,
                            showOnlineIndicator = true,
                            isOnline = user.isOnline
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = SochTextPrimary
                                )
                                if (user.isVerified) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    VerifiedBadge(size = 18.dp)
                                }
                            }

                            Text(
                                text = "@${user.username}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SochTextMuted
                            )

                            if (isOfficial) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Official SOCH Developer",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SochPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Bio
                    if (user.bio.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = user.bio,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = SochTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SochSurface)
                            .border(1.dp, SochBorder, RoundedCornerShape(16.dp))
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = userPosts.size.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SochTextPrimary
                            )
                            Text(
                                text = "Posts",
                                style = MaterialTheme.typography.bodySmall,
                                color = SochTextMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isOfficial) "1M" else "${user.followersCount}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SochTextPrimary
                            )
                            Text(
                                text = "Followers",
                                style = MaterialTheme.typography.bodySmall,
                                color = SochTextMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${user.followingCount}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SochTextPrimary
                            )
                            Text(
                                text = "Following",
                                style = MaterialTheme.typography.bodySmall,
                                color = SochTextMuted
                            )
                        }
                    }

                    // Interests
                    if (user.interests.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Interests",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = SochTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            user.interests.forEach { tag ->
                                Surface(
                                    color = SochSurfaceElevated,
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SochBorder)
                                ) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SochTextSecondary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Action Buttons (Follow + Message)
                    if (!isMe) {
                        Spacer(modifier = Modifier.height(22.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { repository.toggleFollow(user.id) },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFollowing) SochSurface else SochPrimary,
                                    contentColor = if (isFollowing) SochTextSecondary else SochTextPrimary
                                ),
                                border = if (isFollowing) androidx.compose.foundation.BorderStroke(1.dp, SochBorder) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                if (isFollowing) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Following", fontWeight = FontWeight.SemiBold)
                                } else {
                                    Text("Follow", fontWeight = FontWeight.SemiBold)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    val conv = repository.getOrCreateConversationWith(user)
                                    onMessageClick(conv.id)
                                },
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SochBorder),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = SochSurface,
                                    contentColor = SochTextPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = "Message",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Message", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "Thoughts & Posts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SochTextPrimary
                    )
                }
            }

            if (userPosts.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        title = "No posts yet",
                        subtitle = "${user.name} hasn't shared any thoughts yet."
                    )
                }
            } else {
                items(userPosts, key = { it.id }) { post ->
                    PostCard(
                        post = post,
                        currentUserId = currentUser?.id,
                        onLikeClick = { repository.toggleLike(post.id) },
                        onCommentClick = { onCommentClick(post.id) },
                        onAuthorClick = { },
                        onDeleteClick = { scope.launch { repository.deletePost(post.id) } },
                        onReportClick = { scope.launch { repository.reportContent("post", post.id, "Report") } },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
