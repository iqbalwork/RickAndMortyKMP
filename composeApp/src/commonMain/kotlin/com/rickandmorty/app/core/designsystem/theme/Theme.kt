package com.rickandmorty.app.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PortalGreen,
    onPrimary = SpaceBlack,
    primaryContainer = PortalGreenDark,
    onPrimaryContainer = TextPrimary,
    secondary = ElectricCyan,
    onSecondary = SpaceBlack,
    secondaryContainer = BorderSlate,
    onSecondaryContainer = TextPrimary,
    tertiary = CyberYellow,
    onTertiary = SpaceBlack,
    background = SpaceBlack,
    onBackground = TextPrimary,
    surface = DarkVoid,
    onSurface = TextPrimary,
    surfaceVariant = CardSurface,
    onSurfaceVariant = TextSecondary,
    outline = BorderSlate,
    outlineVariant = DarkVoid,
    error = StatusDead,
    onError = SpaceBlack
)

private val LightColorScheme = lightColorScheme(
    primary = PortalGreenDark,
    onPrimary = LightSurface,
    primaryContainer = PortalGreen,
    onPrimaryContainer = LightTextPrimary,
    secondary = ElectricCyan,
    onSecondary = LightSurface,
    secondaryContainer = LightBorderSlate,
    onSecondaryContainer = LightTextPrimary,
    tertiary = CyberYellow,
    onTertiary = LightTextPrimary,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightCardSurface,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorderSlate,
    outlineVariant = LightBackground,
    error = StatusDead,
    onError = LightSurface
)

@Immutable
data class RickMortyCustomColors(
    val portalGreen: Color = PortalGreen,
    val portalGreenDark: Color = PortalGreenDark,
    val electricCyan: Color = ElectricCyan,
    val cyberYellow: Color = CyberYellow,
    val spaceBlack: Color = SpaceBlack,
    val darkVoid: Color = DarkVoid,
    val cardSurface: Color = CardSurface,
    val borderSlate: Color = BorderSlate,
    val textPrimary: Color = TextPrimary,
    val textSecondary: Color = TextSecondary,
    val textMuted: Color = TextMuted,
    val statusAlive: Color = StatusAlive,
    val statusDead: Color = StatusDead,
    val statusUnknown: Color = StatusUnknown
)

val LocalRickMortyColors = staticCompositionLocalOf { RickMortyCustomColors() }

@Composable
fun RickMortyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val customColors = if (darkTheme) {
        RickMortyCustomColors()
    } else {
        RickMortyCustomColors(
            spaceBlack = LightBackground,
            darkVoid = LightSurface,
            cardSurface = LightCardSurface,
            borderSlate = LightBorderSlate,
            textPrimary = LightTextPrimary,
            textSecondary = LightTextSecondary,
            textMuted = LightTextMuted
        )
    }

    CompositionLocalProvider(LocalRickMortyColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = RickMortyTypography,
            content = content
        )
    }
}

object RickMortyThemeExtensions {
    val colors: RickMortyCustomColors
        @Composable
        get() = LocalRickMortyColors.current
}
