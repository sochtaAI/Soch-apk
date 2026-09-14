package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.*

@Composable
fun UserAvatar(
    name: String,
    avatarUrl: String = "",
    size: Dp = 44.dp,
    showOnlineIndicator: Boolean = false,
    isOnline: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (avatarUrl.isNotBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "$name's avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(1.dp, SochBorder, CircleShape)
            )
        } else {
            // Initial-based avatar with subtle gradient
            val initials = name.trim().split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .map { it.first().uppercaseChar() }
                .joinToString("")
                .ifEmpty { "S" }

            val gradient = Brush.linearGradient(
                colors = listOf(
                    SochSurfaceElevated,
                    SochCardHighlight
                )
            )

            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(gradient)
                    .border(1.dp, SochBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    fontSize = (size.value * 0.4f).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SochTextPrimary
                )
            }
        }

        if (showOnlineIndicator && isOnline) {
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(SochSuccess)
                    .border(1.5.dp, SochBackground, CircleShape)
            )
        }
    }
}

@Composable
fun VerifiedBadge(
    modifier: Modifier = Modifier,
    size: Dp = 15.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(SochSecondary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Verified",
            tint = Color.White,
            modifier = Modifier.size(size * 0.7f)
        )
    }
}
