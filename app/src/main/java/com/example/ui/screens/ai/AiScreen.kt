package com.example.ui.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.SochRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

val AI_SHORTCUTS = listOf(
    "Explain like I'm a beginner",
    "Go deeper into this",
    "Give concrete examples",
    "Quiz me on key concepts",
    "Make concise revision notes",
    "Generate practice questions",
    "Brainstorm startup ideas",
    "Create a 7-day study plan"
)

@Composable
fun AiScreen(
    repository: SochRepository
) {
    val messages by repository.aiMessages.collectAsState()
    val isAiConfigured = repository.aiService.isConfigured
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = SochBackground,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SochSurface)
                    .border(0.8.dp, SochBorder, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .padding(bottom = 96.dp)
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Shortcut chips row
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(AI_SHORTCUTS) { shortcut ->
                        Surface(
                            color = SochSurfaceElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SochBorder),
                            modifier = Modifier.clickable {
                                inputText = shortcut
                            }
                        ) {
                            Text(
                                text = shortcut,
                                style = MaterialTheme.typography.bodySmall,
                                color = SochTextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Input row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Ask anything, brainstorm, or explore...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SochTextMuted
                            )
                        },
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
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
                            if (inputText.isNotBlank()) {
                                val query = inputText.trim()
                                inputText = ""
                                scope.launch {
                                    repository.sendAiQuery(query)
                                }
                            }
                        },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank()) SochPrimary else SochSurfaceElevated)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send to AI",
                            tint = if (inputText.isNotBlank()) Color.White else SochTextMuted,
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
            // Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SochPrimary.copy(alpha = 0.2f))
                                    .border(1.dp, SochPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = SochPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "SOCH AI",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = SochTextPrimary
                                )
                                Text(
                                    text = "Think better. Learn faster.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SochTextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = { repository.clearAiChat() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear chat",
                                tint = SochTextMuted
                            )
                        }
                    }

                    // If API key is missing, show clean setup status
                    if (!isAiConfigured) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SochSurfaceElevated),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SochBorder, RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = "Key",
                                    tint = SochWarning,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "AI Key Configuration",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SochTextPrimary
                                    )
                                    Text(
                                        text = "Configure GEMINI_API_KEY in the AI Studio Secrets panel to enable live responses from Google Gemini.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SochTextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Message Bubbles
            items(messages, key = { it.id }) { msg ->
                if (msg.isUser) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp),
                            color = SochPrimary,
                            modifier = Modifier.widthIn(max = 290.dp)
                        ) {
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SochTextPrimary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp),
                            color = SochSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, SochBorder),
                            modifier = Modifier.widthIn(max = 320.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                                if (msg.isGenerating) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = SochPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "SOCH AI is thinking...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SochTextMuted
                                        )
                                    }
                                } else {
                                    RenderFormattedAiText(msg.text)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RenderFormattedAiText(text: String) {
    // Splits code blocks or quotes if present
    if (text.contains("```")) {
        val parts = text.split("```")
        Column {
            parts.forEachIndexed { index, part ->
                if (index % 2 == 1) {
                    // Code block
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SochBackground)
                            .border(1.dp, SochBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = part.trim(),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = SochSecondary
                        )
                    }
                } else if (part.isNotBlank()) {
                    Text(
                        text = part.trim(),
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = SochTextPrimary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    } else {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = SochTextPrimary
        )
    }
}
