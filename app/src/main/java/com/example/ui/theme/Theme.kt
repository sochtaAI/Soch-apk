package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val SochDarkColorScheme = darkColorScheme(
    primary = SochPrimary,
    onPrimary = SochTextPrimary,
    primaryContainer = SochSurfaceElevated,
    onPrimaryContainer = SochTextPrimary,
    secondary = SochSecondary,
    onSecondary = SochTextPrimary,
    secondaryContainer = SochSurface,
    onSecondaryContainer = SochTextSecondary,
    tertiary = SochSecondary,
    background = SochBackground,
    onBackground = SochTextPrimary,
    surface = SochSurface,
    onSurface = SochTextPrimary,
    surfaceVariant = SochSurfaceElevated,
    onSurfaceVariant = SochTextSecondary,
    outline = SochBorder,
    outlineVariant = SochBorder.copy(alpha = 0.5f),
    error = SochError,
    onError = SochTextPrimary
)

val SochShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp)
)

@Composable
fun SochTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SochDarkColorScheme,
        typography = Typography,
        shapes = SochShapes,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SochTheme(content = content)
}
