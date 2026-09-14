package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserProfile
import com.example.ui.screens.onboarding.ALL_INTERESTS
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditProfileDialog(
    user: UserProfile,
    onDismiss: () -> Unit,
    onSave: (name: String, username: String, bio: String, avatarUrl: String, interests: List<String>) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var username by remember { mutableStateOf(user.username) }
    var bio by remember { mutableStateOf(user.bio) }
    var avatarUrl by remember { mutableStateOf(user.avatarUrl) }
    val selectedInterests = remember { mutableStateListOf(*user.interests.toTypedArray()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = SochSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SochBorder, RoundedCornerShape(22.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SochTextPrimary
                    )

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SochTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it.filter { c -> c.isLetterOrDigit() || c == '.' || c == '_' } },
                    label = { Text("Username") },
                    prefix = { Text("@", color = SochTextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = avatarUrl,
                    onValueChange = { avatarUrl = it },
                    label = { Text("Avatar Image URL") },
                    placeholder = { Text("https://...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Interests",
                    style = MaterialTheme.typography.labelMedium,
                    color = SochTextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ALL_INTERESTS.take(12).forEach { tag ->
                        val isSelected = selectedInterests.contains(tag)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SochPrimary else SochSurfaceElevated)
                                .border(1.dp, if (isSelected) SochPrimary else SochBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    if (isSelected) selectedInterests.remove(tag)
                                    else selectedInterests.add(tag)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) SochTextPrimary else SochTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        onSave(name.trim(), username.trim(), bio.trim(), avatarUrl.trim(), selectedInterests.toList())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SochPrimary)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
