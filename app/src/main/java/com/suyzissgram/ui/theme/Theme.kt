package com.suyzissgram.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.Density
import com.suyzissgram.domain.model.ThemeChoice

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9B7BFF),
    onPrimary = Color.White,
    background = Color(0xFF0E0D14),
    onBackground = Color.White,
    surface = Color(0xFF191722),
    onSurface = Color.White,
    secondary = Color(0xFFB8A4FF)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF7250D5),
    onPrimary = Color.White,
    background = Color(0xFFF7F5FC),
    onBackground = Color(0xFF191622),
    surface = Color.White,
    onSurface = Color(0xFF191622),
    secondary = Color(0xFF6043B5)
)

@Composable
fun SuyzissTheme(
    themeChoice: ThemeChoice = ThemeChoice.DARK,
    textScale: Float = 1f,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeChoice) {
        ThemeChoice.SYSTEM -> isSystemInDarkTheme()
        ThemeChoice.LIGHT -> false
        ThemeChoice.DARK -> true
    }
    val baseDensity = LocalDensity.current
    val scaledDensity = Density(
        density = baseDensity.density,
        fontScale = baseDensity.fontScale * textScale.coerceIn(0.85f, 1.25f)
    )

    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        MaterialTheme(
            colorScheme = if (useDarkTheme) DarkColors else LightColors,
            content = content
        )
    }
}
