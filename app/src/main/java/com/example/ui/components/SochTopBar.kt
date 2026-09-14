package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

@Composable
fun SochTopBar(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    actionIcon: ImageVector? = null,
    actionDescription: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SochBackground,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (onBackClick != null) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SochTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SochTextPrimary
                        )
                        if (!subtitle.isNullOrBlank()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = SochTextMuted
                            )
                        }
                    }
                }

                if (actionIcon != null && onActionClick != null) {
                    IconButton(
                        onClick = onActionClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = actionIcon,
                            contentDescription = actionDescription,
                            tint = SochTextPrimary
                        )
                    }
                }
            }

            HorizontalDivider(
                color = SochBorder.copy(alpha = 0.6f),
                thickness = 0.8.dp
            )
        }
    }
}
