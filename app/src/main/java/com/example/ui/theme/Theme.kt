package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WarmHarvestColorScheme = lightColorScheme(
    primary = FarmGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = FarmGreenContainer,
    onPrimaryContainer = FarmGreenOnContainer,
    secondary = HarvestYellowBorder,
    onSecondary = Color.White,
    secondaryContainer = HarvestYellowBg,
    onSecondaryContainer = HarvestYellowDarkText,
    background = FarmCreamBg,
    onBackground = FarmTextPrimary,
    surface = FarmSurface,
    onSurface = FarmTextPrimary,
    surfaceVariant = FarmCreamSecondary,
    onSurfaceVariant = FarmTextSecondary,
    outline = FarmBorder,
    error = LossRed,
    errorContainer = LossBg,
    onError = Color.White
)

@Composable
fun KhetKhataTheme(
    content: @Composable () -> Unit
) {
    // Always enforce the high-contrast sunlit warm cream palette for outdoor farm legibility
    MaterialTheme(
        colorScheme = WarmHarvestColorScheme,
        typography = Typography,
        content = content
    )
}
