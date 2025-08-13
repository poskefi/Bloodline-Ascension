package com.webdevry.bloodlineascension.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

// --- New: app-specific colors you want outside the stock M3 palette ---
data class ExtendedColors(
    val stamina: androidx.compose.ui.graphics.Color
)

private val LightExtended = ExtendedColors(
    stamina = AndroidGreen
)
private val DarkExtended = ExtendedColors(
    stamina = AndroidGreen // same in dark; change if you want a darker variant
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtended }

// (existing color schemes)
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

// Helper accessor so you can do MaterialTheme.extendedColors().stamina
@Composable
fun MaterialTheme.extendedColors(): ExtendedColors = LocalExtendedColors.current

@Composable
fun BloodlineAscensionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extended = if (darkTheme) DarkExtended else LightExtended

    CompositionLocalProvider(LocalExtendedColors provides extended) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
