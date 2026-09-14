package com.example.ui.screens.connect

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.UserProfile
import com.example.data.repository.SochRepository
import com.example.ui.components.UserAvatar
import com.example.ui.components.VerifiedBadge
import com.example.ui.theme.*

@Composable
fun ConnectScreen(
    repository: SochRepository,
    onUserClick: (String) -> Unit
) {
    val context = LocalContext.current
    val followedUsers by repository.followedUserIds.collectAsState()
    val discoverProfiles = remember { repository.getDiscoverProfiles() }

    var isLocationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var showNearbyStatus by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isLocationPermissionGranted = isGranted
        showNearbyStatus = true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SochBackground),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Top Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Find your people.",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
                    fontWeight = FontWeight.Bold,
                    color = SochTextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Discover, connect and grow together.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SochTextSecondary
                )

                Spacer(modifier = Modifier.height(22.dp))

                // SECTION A: Nearby Discovery Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SochSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SochBorder, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(SochSurfaceElevated)
                                    .border(1.dp, SochBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NearMe,
                                    contentDescription = "Nearby",
                                    tint = SochSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = "Find people around you",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SochTextPrimary
                                )
                                Text(
                                    text = "Discover SOCH users nearby when they choose to be visible.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SochTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (showNearbyStatus) {
                            Surface(
                                color = SochSurfaceElevated,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isLocationPermissionGranted)
                                        "📍 Nearby radar active. Scanning for active SOCH thinkers in your area..."
                                    else
                                        "Location access denied. Enable in Settings to discover thinkers nearby.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isLocationPermissionGranted) SochTextPrimary else SochWarning,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Button(
                            onClick = {
                                if (isLocationPermissionGranted) {
                                    showNearbyStatus = true
                                } else {
                                    // Request ONLY when user explicitly clicks Explore Nearby per section 10
                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SochSecondary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isLocationPermissionGranted) "Radar Active" else "Explore Nearby",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // SECTION B: Discover People Header
                Text(
                    text = "Discover People",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SochTextPrimary
                )
            }
        }

        // List of Profiles (Dipak @dipak.dev PINNED first)
        items(discoverProfiles, key = { it.id }) { user ->
            val isOfficial = user.username == "dipak.dev"
            val isFollowing = followedUsers.contains(user.id)

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOfficial) SochSurfaceElevated else SochSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .border(
                        width = if (isOfficial) 1.2.dp else 1.dp,
                        color = if (isOfficial) SochPrimary.copy(alpha = 0.8f) else SochBorder,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable { onUserClick(user.username) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (isOfficial) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SochPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "OFFICIAL SOCH CREATOR",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SochPrimary,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            UserAvatar(
                                name = user.name,
                                avatarUrl = user.avatarUrl,
                                size = 46.dp,
                                showOnlineIndicator = true,
                                isOnline = user.isOnline
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = SochTextPrimary
                                    )
                                    if (user.isVerified) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        VerifiedBadge()
                                    }
                                }
                                Text(
                                    text = "@${user.username}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SochTextMuted
                                )
                                Text(
                                    text = if (isOfficial) "1M followers" else "${user.followersCount} followers",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SochTextSecondary
                                )
                            }
                        }

                        // Follow / Following Button
                        Button(
                            onClick = { repository.toggleFollow(user.id) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowing) SochSurface else SochPrimary,
                                contentColor = if (isFollowing) SochTextSecondary else SochTextPrimary
                            ),
                            border = if (isFollowing) androidx.compose.foundation.BorderStroke(1.dp, SochBorder) else null,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            if (isFollowing) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = SochTextSecondary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Following",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Text(
                                    text = "Follow",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    if (user.bio.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = user.bio,
                            style = MaterialTheme.typography.bodySmall,
                            color = SochTextSecondary,
                            maxLines = 2
                        )
                    }

                    if (user.interests.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            user.interests.take(3).forEach { tag ->
                                Surface(
                                    color = SochSurfaceElevated,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.6.dp, SochBorder)
                                ) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = SochTextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
