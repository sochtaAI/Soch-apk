package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
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
import com.example.ui.components.SochTopBar
import com.example.ui.components.UserAvatar
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    repository: SochRepository,
    onBackClick: () -> Unit,
    onSignOut: () -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    val scope = rememberCoroutineScope()

    var isOnlineVisible by remember { mutableStateOf(true) }
    var isNearbyDiscoverable by remember { mutableStateOf(true) }
    var showSignOutConfirm by remember { mutableStateOf(false) }

    val isSupabaseConnected = repository.supabase.isConfigured

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text("Sign Out of SOCH?", color = SochTextPrimary) },
            text = { Text("You will need to sign in again to access your feed and profile.", color = SochTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutConfirm = false
                        scope.launch {
                            repository.signOut()
                            onSignOut()
                        }
                    }
                ) {
                    Text("Sign Out", color = SochError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text("Cancel", color = SochTextSecondary)
                }
            },
            containerColor = SochSurface,
            shape = RoundedCornerShape(18.dp)
        )
    }

    Scaffold(
        containerColor = SochBackground,
        topBar = {
            SochTopBar(
                title = "Settings",
                subtitle = "Preferences, privacy & backend status",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Account Overview Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SochSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SochBorder, RoundedCornerShape(18.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    UserAvatar(
                        name = currentUser?.name ?: "Thinker",
                        avatarUrl = currentUser?.avatarUrl.orEmpty(),
                        size = 52.dp,
                        showOnlineIndicator = true,
                        isOnline = isOnlineVisible
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentUser?.name ?: "Thinker",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SochTextPrimary
                            )
                            if (currentUser?.isVerified == true) {
                                Spacer(modifier = Modifier.width(4.dp))
                                VerifiedBadge()
                            }
                        }
                        Text(
                            text = "@${currentUser?.username ?: "user"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SochTextMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "UID: ${currentUser?.id?.take(12)}...",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = SochTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Privacy & Discovery
            Text(
                text = "Privacy & Discovery",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = SochPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SochSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SochBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Show Online Status",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = SochTextPrimary
                            )
                            Text(
                                text = "Display a green indicator when you are active",
                                style = MaterialTheme.typography.bodySmall,
                                color = SochTextMuted
                            )
                        }
                        Switch(
                            checked = isOnlineVisible,
                            onCheckedChange = { isOnlineVisible = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SochPrimary
                            )
                        )
                    }

                    HorizontalDivider(color = SochBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Nearby Discovery",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = SochTextPrimary
                            )
                            Text(
                                text = "Allow nearby thinkers to discover your profile",
                                style = MaterialTheme.typography.bodySmall,
                                color = SochTextMuted
                            )
                        }
                        Switch(
                            checked = isNearbyDiscoverable,
                            onCheckedChange = { isNearbyDiscoverable = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SochPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Backend & Database Integration
            Text(
                text = "Backend & Security",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = SochPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SochSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SochBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isSupabaseConnected) SochSuccess else SochWarning)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSupabaseConnected) "Supabase Connected" else "Local Data Layer Active",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = SochTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isSupabaseConnected)
                            "PostgreSQL database connected with Row Level Security (RLS) policies for profiles, posts, likes, comments, and follows."
                        else
                            "To connect your live PostgreSQL project, configure SUPABASE_URL and SUPABASE_ANON_KEY in the AI Studio Secrets panel. All SQL schema and migrations are ready in /supabase/migrations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SochTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About SOCH
            Text(
                text = "About SOCH",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = SochPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SochSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SochBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SOCH — Think. Connect. Grow.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SochTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Version 1.0.0 (Build 2026.09)",
                        style = MaterialTheme.typography.bodySmall,
                        color = SochTextMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Created and architected by Dipak (@dipak.dev). A modern, calm, and intelligent social and knowledge community for genuine thinkers.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SochTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Sign Out Button
            Button(
                onClick = { showSignOutConfirm = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SochSurfaceElevated,
                    contentColor = SochError
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, SochError.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    tint = SochError,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = SochError
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
