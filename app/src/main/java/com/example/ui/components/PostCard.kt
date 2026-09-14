package com.example.ui.components

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Post
import com.example.ui.theme.*

@Composable
fun PostCard(
    post: Post,
    currentUserId: String?,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onAuthorClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onReportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    val author = post.author
    val authorName = author?.name ?: "SOCH Thinker"
    val authorUsername = author?.username?.let { "@$it" } ?: "@thinker"
    val isOwnPost = currentUserId != null && currentUserId == post.userId

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SochSurface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, SochBorder, RoundedCornerShape(18.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Author Avatar clickable
                Box(
                    modifier = Modifier.clickable {
                        onAuthorClick(author?.username ?: "")
                    }
                ) {
                    UserAvatar(
                        name = authorName,
                        avatarUrl = author?.avatarUrl.orEmpty(),
                        size = 42.dp,
                        showOnlineIndicator = true,
                        isOnline = author?.isOnline ?: false
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onAuthorClick(author?.username ?: "") }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = authorName,
                            style = MaterialTheme.typography.labelLarge,
                            color = SochTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (author?.isVerified == true) {
                            Spacer(modifier = Modifier.width(6.dp))
                            VerifiedBadge()
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = authorUsername,
                            style = MaterialTheme.typography.bodySmall,
                            color = SochTextMuted
                        )
                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.bodySmall,
                            color = SochTextMuted
                        )
                        Text(
                            text = TimeUtils.formatRelativeTime(post.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = SochTextMuted
                        )
                    }
                }

                // More Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More post options",
                            tint = SochTextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(SochSurfaceElevated)
                    ) {
                        if (isOwnPost) {
                            DropdownMenuItem(
                                text = { Text("Delete Post", color = SochError) },
                                onClick = {
                                    showMenu = false
                                    onDeleteClick()
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Report Post", color = SochTextPrimary) },
                                onClick = {
                                    showMenu = false
                                    onReportClick()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Post Content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
                color = SochTextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Actions Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Like Button
                val heartTint by animateColorAsState(
                    targetValue = if (post.isLikedByMe) SochError else SochTextSecondary,
                    label = "heartColor"
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onLikeClick() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (post.isLikedByMe) "Unlike post" else "Like post",
                        tint = heartTint,
                        modifier = Modifier.size(20.dp)
                    )
                    if (post.likesCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = post.likesCount.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = heartTint
                        )
                    }
                }

                // Comment Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onCommentClick() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "View comments",
                        tint = SochTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    if (post.commentsCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = post.commentsCount.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = SochTextSecondary
                        )
                    }
                }

                // Native Share Button
                IconButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Shared from SOCH")
                            putExtra(Intent.EXTRA_TEXT, "\"${post.content}\" — shared via SOCH")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share post"))
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share post",
                        tint = SochTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
