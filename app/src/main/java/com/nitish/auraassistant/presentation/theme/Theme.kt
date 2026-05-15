package com.nitish.auraassistant.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AuraColorScheme = darkColorScheme(
    primary          = AuraAccent,
    onPrimary        = AuraDeepBg,
    primaryContainer = AuraSurfaceVariant,
    onPrimaryContainer = AuraAccentBright,
    secondary        = AuraAccentBright,
    onSecondary      = AuraDeepBg,
    tertiary         = AuraSuccess,
    background       = AuraDeepBg,
    onBackground     = AuraOnSurface,
    surface          = AuraSurface,
    onSurface        = AuraOnSurface,
    surfaceVariant   = AuraSurfaceVariant,
    onSurfaceVariant = AuraOnSurfaceDim,
    error            = AuraError,
    onError          = AuraDeepBg,
)

@Composable
fun AuraAssistantTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AuraColorScheme,
        typography  = Typography,
        content     = content
    )
}