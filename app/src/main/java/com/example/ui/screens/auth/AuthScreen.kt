package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.SochRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    repository: SochRepository,
    onAuthSuccess: (Boolean) -> Unit // hasProfile: Boolean
) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // SOCH Brand Logo & Symbol
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(SochPrimary, SochSecondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "S",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SOCH",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = SochTextPrimary
            )

            Text(
                text = "Think. Connect. Grow.",
                style = MaterialTheme.typography.bodyMedium,
                color = SochTextSecondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Login / Create Account Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SochSurface)
                    .border(1.dp, SochBorder, RoundedCornerShape(14.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isSignUp) SochSurfaceElevated else Color.Transparent)
                        .clickable {
                            isSignUp = false
                            errorMessage = null
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Login",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (!isSignUp) SochTextPrimary else SochTextSecondary,
                        fontWeight = if (!isSignUp) FontWeight.SemiBold else FontWeight.Normal
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSignUp) SochSurfaceElevated else Color.Transparent)
                        .clickable {
                            isSignUp = true
                            errorMessage = null
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Create account",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSignUp) SochTextPrimary else SochTextSecondary,
                        fontWeight = if (isSignUp) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Inputs
            if (isSignUp) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    placeholder = { Text("e.g. Maya Lin") },
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

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it.filter { ch -> ch.isLetterOrDigit() || ch == '.' || ch == '_' } },
                    label = { Text("Username") },
                    placeholder = { Text("e.g. mayalin") },
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

                Spacer(modifier = Modifier.height(14.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                placeholder = { Text("thinker@soch.app") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                placeholder = { Text("••••••••") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = SochTextSecondary
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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

            AnimatedVisibility(visible = errorMessage != null) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = SochError,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Button
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please provide both email and password"
                        return@Button
                    }
                    if (isSignUp && (name.isBlank() || username.isBlank())) {
                        errorMessage = "Please provide your name and username"
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null

                    coroutineScope.launch {
                        if (isSignUp) {
                            val res = repository.signUp(email.trim(), password.trim(), name.trim(), username.trim())
                            isLoading = false
                            if (res.isSuccess) {
                                onAuthSuccess(false) // Needs interests / profile onboarding step
                            } else {
                                errorMessage = res.exceptionOrNull()?.message ?: "Sign up failed"
                            }
                        } else {
                            val res = repository.signIn(email.trim(), password.trim())
                            isLoading = false
                            if (res.isSuccess) {
                                onAuthSuccess(repository.hasCompletedProfile.value)
                            } else {
                                errorMessage = res.exceptionOrNull()?.message ?: "Sign in failed"
                            }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SochPrimary,
                    contentColor = SochTextPrimary,
                    disabledContainerColor = SochPrimary.copy(alpha = 0.5f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = SochTextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isSignUp) "Create Account" else "Enter SOCH",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Subtitle footer
            Text(
                text = "Protected by Supabase Auth & PostgreSQL Row Level Security",
                style = MaterialTheme.typography.bodySmall,
                color = SochTextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}
