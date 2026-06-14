package com.leona.controlepagamentos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Blue,
    onPrimary = SurfaceWhite,
    secondary = Blue,
    onSecondary = SurfaceWhite,
    tertiary = Attention,
    onTertiary = SurfaceWhite,
    background = BackgroundLight,
    onBackground = TextStrong,
    surface = SurfaceWhite,
    onSurface = TextStrong,
    onSurfaceVariant = TextSecondary,
    surfaceVariant = BackgroundLight,
    outline = BorderLight,
    error = Alert,
    onError = SurfaceWhite
)

private val DarkColors = darkColorScheme(
    primary = Blue,
    onPrimary = SurfaceWhite,
    secondary = Blue,
    onSecondary = SurfaceWhite,
    tertiary = Attention,
    background = Navy,
    surface = Color(0xFF1E2B3D),
    onBackground = Color(0xFFF4F6FB),
    onSurface = Color(0xFFF4F6FB),
    onSurfaceVariant = TextTertiary,
    surfaceVariant = Color(0xFF243047),
    outline = Color(0xFF2D3D52),
    error = Alert
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(11.dp),
    small = RoundedCornerShape(11.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

@Composable
fun ControlePagamentosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
