package com.example.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.SochRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

val ALL_INTERESTS = listOf(
    "AI & Tech",
    "Study",
    "Science",
    "Gaming",
    "Coding",
    "Business",
    "Creativity",
    "Fitness",
    "Movies",
    "Music",
    "Books",
    "Entrepreneurship",
    "Technology",
    "Education",
    "Productivity",
    "Design",
    "Space",
    "Psychology",
    "Startups"
)

@Composable
fun ProfileSetupScreen(
    repository: SochRepository,
    onComplete: () -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    var step by remember { mutableIntStateOf(1) } // 1 or 2

    var name by remember(currentUser) { mutableStateOf(currentUser?.name.orEmpty()) }
    var username by remember(currentUser) { mutableStateOf(currentUser?.username.orEmpty()) }
    var bio by remember(currentUser) { mutableStateOf(currentUser?.bio.orEmpty()) }

    val selectedInterests = remember { mutableStateListOf<String>() }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SochBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Step Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "STEP 0$step / 02",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = SochPrimary,
                    letterSpacing = 1.5.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (step >= 1) SochPrimary else SochBorder)
                    )
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (step >= 2) SochPrimary else SochBorder)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier.weight(1f),
                label = "onboarding_step"
            ) { currentStep ->
                if (currentStep == 1) {
                    // STEP 1: Create Profile
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Let's know you better.",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = SochTextPrimary
                        )

                        Text(
                            text = "Your identity in the SOCH knowledge and ideas community.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SochTextSecondary,
                            modifier = Modifier.padding(top = 6.dp, bottom = 28.dp)
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("What should we call you?") },
                            placeholder = { Text("Your full name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SochSurface,
                                unfocusedContainerColor = SochSurface,
                                focusedBorderColor = SochPrimary,
                                unfocusedBorderColor = SochBorder,
                                focusedTextColor = SochTextPrimary,
                                unfocusedTextColor = SochTextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it.filter { c -> c.isLetterOrDigit() || c == '.' || c == '_' } },
                            label = { Text("Username") },
                            placeholder = { Text("username") },
                            prefix = { Text("@", color = SochTextSecondary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SochSurface,
                                unfocusedContainerColor = SochSurface,
                                focusedBorderColor = SochPrimary,
                                unfocusedBorderColor = SochBorder,
                                focusedTextColor = SochTextPrimary,
                                unfocusedTextColor = SochTextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Short Bio (Optional)") },
                            placeholder = { Text("What are you building or curious about?") },
                            minLines = 3,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SochSurface,
                                unfocusedContainerColor = SochSurface,
                                focusedBorderColor = SochPrimary,
                                unfocusedBorderColor = SochBorder,
                                focusedTextColor = SochTextPrimary,
                                unfocusedTextColor = SochTextPrimary
                            )
                        )
                    }
                } else {
                    // STEP 2: Interests
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "What interests your SOCH?",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = SochTextPrimary
                        )

                        Text(
                            text = "Select topics you love to explore, discuss, and learn about.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SochTextSecondary,
                            modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
                        )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(ALL_INTERESTS) { interest ->
                                val isSelected = selectedInterests.contains(interest)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isSelected) SochSurfaceElevated else SochSurface)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) SochPrimary else SochBorder,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            if (isSelected) selectedInterests.remove(interest)
                                            else selectedInterests.add(interest)
                                        }
                                        .padding(horizontal = 14.dp, vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = interest,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) SochTextPrimary else SochTextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = SochError,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Bottom Navigation CTA
            Button(
                onClick = {
                    if (step == 1) {
                        if (name.isBlank() || username.isBlank()) {
                            errorMessage = "Please enter your name and username"
                            return@Button
                        }
                        errorMessage = null
                        step = 2
                    } else {
                        if (selectedInterests.isEmpty()) {
                            errorMessage = "Please choose at least one interest"
                            return@Button
                        }
                        errorMessage = null
                        isSubmitting = true
                        scope.launch {
                            val res = repository.completeProfile(
                                name = name.trim(),
                                username = username.trim(),
                                interests = selectedInterests.toList(),
                                bio = bio.trim()
                            )
                            isSubmitting = false
                            if (res.isSuccess) {
                                onComplete()
                            } else {
                                errorMessage = res.exceptionOrNull()?.message ?: "Failed to save profile"
                            }
                        }
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SochPrimary,
                    contentColor = SochTextPrimary
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = SochTextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (step == 1) "Continue to Interests" else "Enter SOCH",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
