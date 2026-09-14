package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.SochRepository
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    repository: SochRepository,
    onSettingsClick: () -> Unit,
    onCommentClick: (String) -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    val posts by repository.posts.collectAsState()
    val scope = rememberCoroutineScope()

    var showEditDialog by remember { mutableStateOf(false) }

    val user = currentUser
    if (user == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SochBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = SochPrimary)
        }
        return
    }

    val myPosts = posts.filter { it.userId == user.id }

    if (showEditDialog) {
        EditProfileDialog(
            user = user,
            onDismiss = { showEditDialog = false },
            onSave = { name, username, bio, avatarUrl, interests ->
                showEditDialog = false
                scope.launch {
                    repository.updateProfile(name, username, bio, avatarUrl, interests)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SochBackground),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Header Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Your Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SochTextPrimary
                    )

                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = SochTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // User Info Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    UserAvatar(
                        name = user.name,
                        avatarUrl = user.avatarUrl,
                        size = 72.dp,
                        showOnlineIndicator = true,
                        isOnline = true
                    )

                    Spacer(modifier = Modifier.width(18.dp))

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
                    }
                }

                // Bio
                if (user.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = SochTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SochSurface)
                        .border(1.dp, SochBorder, RoundedCornerShape(16.dp))
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = myPosts.size.toString(),
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
                            text = user.followersCount.toString(),
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
                            text = user.followingCount.toString(),
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
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Interests",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = SochTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        user.interests.forEach { tag ->
                            Surface(
                                color = SochSurfaceElevated,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SochBorder)
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SochTextSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Edit Profile Button
                OutlinedButton(
                    onClick = { showEditDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SochBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SochSurface,
                        contentColor = SochTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "My Posts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SochTextPrimary
                )
            }
        }

        if (myPosts.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    title = "No posts yet",
                    subtitle = "Share your first thought or question from the home feed."
                )
            }
        } else {
            items(myPosts, key = { it.id }) { post ->
                PostCard(
                    post = post,
                    currentUserId = user.id,
                    onLikeClick = { repository.toggleLike(post.id) },
                    onCommentClick = { onCommentClick(post.id) },
                    onAuthorClick = { },
                    onDeleteClick = { scope.launch { repository.deletePost(post.id) } },
                    onReportClick = { },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }
        }
    }
}
