package com.webdevry.bloodlineascension.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DarkColorScheme
import androidx.compose.material3.LightColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

private val DarkColors: ColorScheme = DarkColorScheme(
    primary = Crimson,
    onPrimary = Bone,
    secondary = Azure,
    onSecondary = Bone,
    background = Night,
    onBackground = Mist,
    surface = Charcoal,
    onSurface = Mist,
    error = BloodRed,
    onError = Bone
)

private val LightColors: ColorScheme = LightColorScheme(
    primary = Crimson,
    onPrimary = Bone,
    secondary = Azure,
    onSecondary = Bone,
    background = Bone,
    onBackground = Night,
    surface = Mist,
    onSurface = Night,
    error = BloodRed,
    onError = Bone
)

@Composable
fun BloodlineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
