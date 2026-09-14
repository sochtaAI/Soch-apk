package com.example.ui.screens.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
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
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SochTopBar
import com.example.ui.components.TimeUtils
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CommentsScreen(
    postId: String,
    repository: SochRepository,
    onBackClick: () -> Unit,
    onUserClick: (String) -> Unit
) {
    val posts by repository.posts.collectAsState()
    val commentsMap by repository.comments.collectAsState()
    val currentUser by repository.currentUser.collectAsState()
    val post = posts.find { it.id == postId }
    val comments = commentsMap[postId] ?: emptyList()

    var newCommentText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = SochBackground,
        topBar = {
            SochTopBar(
                title = "Discussion",
                subtitle = "${post?.commentsCount ?: 0} comments",
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            // Fixed bottom comment input
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
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    UserAvatar(
                        name = currentUser?.name ?: "Me",
                        avatarUrl = currentUser?.avatarUrl.orEmpty(),
                        size = 36.dp
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    TextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = {
                            Text(
                                text = "Add a thoughtful comment...",
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
                            if (newCommentText.isNotBlank()) {
                                isSending = true
                                scope.launch {
                                    repository.addComment(postId, newCommentText.trim())
                                    newCommentText = ""
                                    isSending = false
                                }
                            }
                        },
                        enabled = newCommentText.isNotBlank() && !isSending,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (newCommentText.isNotBlank()) SochPrimary else SochSurfaceElevated)
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = SochTextPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send comment",
                                tint = if (newCommentText.isNotBlank()) Color.White else SochTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Post Preview Header
            if (post != null) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SochSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SochBorder, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                UserAvatar(
                                    name = post.author?.name ?: "Thinker",
                                    avatarUrl = post.author?.avatarUrl.orEmpty(),
                                    size = 38.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = post.author?.name ?: "Thinker",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SochTextPrimary
                                    )
                                    Text(
                                        text = TimeUtils.formatRelativeTime(post.createdAt),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SochTextMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = post.content,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                                color = SochTextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Responses",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SochTextPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }

            // Comments List
            if (comments.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Outlined.ChatBubbleOutline,
                        title = "No comments yet.",
                        subtitle = "Start the conversation with your perspective.",
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            } else {
                items(comments, key = { it.id }) { comment ->
                    val isOwnComment = currentUser != null && currentUser?.id == comment.userId

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SochSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .border(0.8.dp, SochBorder, RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        comment.author?.username?.let { onUserClick(it) }
                                    }
                                ) {
                                    UserAvatar(
                                        name = comment.author?.name ?: "Thinker",
                                        avatarUrl = comment.author?.avatarUrl.orEmpty(),
                                        size = 30.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = comment.author?.name ?: "Thinker",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SochTextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${TimeUtils.formatRelativeTime(comment.createdAt)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SochTextMuted
                                    )
                                }

                                if (isOwnComment) {
                                    IconButton(
                                        onClick = {
                                            scope.launch { repository.deleteComment(postId, comment.id) }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete comment",
                                            tint = SochTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = comment.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SochTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
