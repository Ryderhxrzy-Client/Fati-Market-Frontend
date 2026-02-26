package com.example.fati_market_frontend.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DarkGreen,
    onPrimary = White,
    secondary = Gold,
    onSecondary = DarkText,
    tertiary = GoldLight,
    background = OffWhite,
    surface = White,
    onBackground = DarkText,
    onSurface = DarkText,
    onSurfaceVariant = MutedText
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreenLight,
    onPrimary = White,
    secondary = Gold,
    onSecondary = DarkText,
    tertiary = GoldLight,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = White,
    onSurface = White,
    onSurfaceVariant = Color(0xFFB0B0B0)
)

@Composable
fun FatiMarketFrontendTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
