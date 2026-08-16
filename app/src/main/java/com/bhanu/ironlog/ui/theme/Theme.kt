package com.bhanu.ironlog.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val IronLogColorScheme = darkColorScheme(
    primary = IronLogPrimary,
    onPrimary = IronLogTextPrimary,
    primaryContainer = IronLogPrimary,
    onPrimaryContainer = IronLogTextPrimary,
    secondary = IronLogPrimaryLight,
    onSecondary = IronLogSurface0,
    secondaryContainer = IronLogSurface2,
    onSecondaryContainer = IronLogTextPrimary,
    tertiary = IronLogAccent,
    onTertiary = IronLogSurface0,
    tertiaryContainer = IronLogSurface2,
    onTertiaryContainer = IronLogTextPrimary,
    background = IronLogSurface0,
    onBackground = IronLogTextPrimary,
    surface = IronLogSurface1,
    onSurface = IronLogTextPrimary,
    surfaceVariant = IronLogSurface2,
    onSurfaceVariant = IronLogTextSecondary,
    outline = IronLogTextMuted,
    outlineVariant = IronLogSurface2,
    error = IronLogDanger,
    onError = IronLogTextPrimary,
    errorContainer = IronLogSurface2,
    onErrorContainer = IronLogDanger
)

@Composable
fun IronLogTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Dynamic Android colors are deliberately disabled: the Phase 6 Figma tokens are the
    // product source of truth and must render consistently across supported Android devices.
    MaterialTheme(
        colorScheme = IronLogColorScheme,
        typography = Typography,
        content = content
    )
}
