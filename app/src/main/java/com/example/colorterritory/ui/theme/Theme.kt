package com.example.colorterritory.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.colorterritory.data.BoardTheme

private val DarkScheme = darkColorScheme(
    primary = Ivory,
    onPrimary = Ink,
    secondary = TealAccent,
    onSecondary = Ink,
    background = GraphiteBackground,
    onBackground = IvoryText,
    surface = GraphiteSurface,
    onSurface = IvoryText,
    surfaceVariant = GraphiteSurfaceHigh,
    onSurfaceVariant = IvoryMuted,
    surfaceContainer = GraphiteSurface,
    surfaceContainerHigh = GraphiteSurfaceHigh,
    surfaceContainerHighest = GraphiteSurfaceHigh,
    outline = GraphiteOutline,
    outlineVariant = GraphiteOutlineSoft,
    error = ClayError
)

private val LightScheme = lightColorScheme(
    primary = Ink,
    onPrimary = PaperSurface,
    secondary = TealDeep,
    onSecondary = PaperSurface,
    background = PaperBackground,
    onBackground = InkText,
    surface = PaperSurface,
    onSurface = InkText,
    surfaceVariant = PaperSurfaceHigh,
    onSurfaceVariant = InkMuted,
    surfaceContainer = PaperSurface,
    surfaceContainerHigh = PaperSurfaceHigh,
    surfaceContainerHighest = PaperSurfaceHigh,
    outline = PaperOutline,
    outlineVariant = PaperOutlineSoft,
    error = ClayError
)

val LocalTerritoryPalette = staticCompositionLocalOf { ClassicPalette }

@Composable
fun ColorTerritoryTheme(
    darkTheme: Boolean,
    boardTheme: BoardTheme = BoardTheme.CLASSIC,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalTerritoryPalette provides paletteFor(boardTheme)) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = Typography,
            content = content
        )
    }
}
