package com.example.ui.screens.post

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CreatePostScreen(
    repository: SochRepository,
    onDismiss: () -> Unit
) {
    var content by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val maxChars = 500

    val currentUser by repository.currentUser.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = SochBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = SochTextPrimary
                    )
                }

                Text(
                    text = "New Post",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SochTextPrimary
                )

                Button(
                    onClick = {
                        if (content.isBlank()) {
                            errorMessage = "Post content cannot be empty"
                            return@Button
                        }
                        isSubmitting = true
                        errorMessage = null
                        scope.launch {
                            val res = repository.createPost(content.trim())
                            isSubmitting = false
                            if (res.isSuccess) {
                                onDismiss()
                            } else {
                                errorMessage = res.exceptionOrNull()?.message ?: "Failed to publish post"
                            }
                        }
                    },
                    enabled = content.isNotBlank() && !isSubmitting && content.length <= maxChars,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SochPrimary,
                        contentColor = SochTextPrimary,
                        disabledContainerColor = SochPrimary.copy(alpha = 0.3f),
                        disabledContentColor = SochTextMuted
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = SochTextPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Post",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            HorizontalDivider(color = SochBorder.copy(alpha = 0.6f), thickness = 0.8.dp)

            // Author Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                UserAvatar(
                    name = currentUser?.name ?: "Thinker",
                    avatarUrl = currentUser?.avatarUrl.orEmpty(),
                    size = 44.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = currentUser?.name ?: "Thinker",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = SochTextPrimary
                    )
                    Text(
                        text = currentUser?.username?.let { "@$it" } ?: "@thinker",
                        style = MaterialTheme.typography.bodySmall,
                        color = SochTextMuted
                    )
                }
            }

            // Input Field
            TextField(
                value = content,
                onValueChange = {
                    if (it.length <= maxChars) {
                        content = it
                        errorMessage = null
                    }
                },
                placeholder = {
                    Text(
                        text = "What's on your mind? Share an idea, spark a discussion, or ask a question...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SochTextMuted
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = SochTextPrimary,
                    unfocusedTextColor = SochTextPrimary
                )
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = SochError,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
            }

            // Bottom Character Counter
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SochSurface)
                    .border(0.8.dp, SochBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Public to SOCH community",
                    style = MaterialTheme.typography.bodySmall,
                    color = SochTextSecondary
                )

                Text(
                    text = "${content.length}/$maxChars",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (content.length > 450) SochWarning else SochTextMuted
                )
            }
        }
    }
}
