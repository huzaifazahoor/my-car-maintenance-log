package com.example.carlog.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light theme only, same blue as the app icon.
private val AppColors = lightColorScheme(
    primary = Color(0xFF1E6FD9),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE8FA),
    onPrimaryContainer = Color(0xFF0F3F80),
    secondary = Color(0xFF0F3F80),
    secondaryContainer = Color(0xFFDCE8FA),
    onSecondaryContainer = Color(0xFF0F3F80),
    background = Color(0xFFF7F8FA),
    surface = Color(0xFFF7F8FA),
    surfaceVariant = Color(0xFFECEFF3),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color(0xFFECEFF3),
    outlineVariant = Color(0xFFD5DAE1),
)

@Composable
fun CarLogTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColors, typography = Typography, content = content)
}
