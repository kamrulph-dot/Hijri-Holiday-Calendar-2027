package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NightEmeraldPrimary,
    onPrimary = Color(0xFF032215),
    primaryContainer = NightEmeraldSurfaceVariant,
    onPrimaryContainer = Color(0xFF90F0C7),
    secondary = NightGold,
    onSecondary = Color(0xFF382900),
    secondaryContainer = Color(0xFF554109),
    onSecondaryContainer = Color(0xFFFFE088),
    tertiary = GoldAccent,
    background = DarkMixBase,
    onBackground = TextPrimaryDark,
    surface = NightEmeraldSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = NightEmeraldSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    outlineVariant = Color(0xFF28483B)
)

private val LightColorScheme = lightColorScheme(
    primary = Emerald800,
    onPrimary = Color.White,
    primaryContainer = Emerald100,
    onPrimaryContainer = Emerald900,
    secondary = GoldPrimary,
    onSecondary = Color.White,
    secondaryContainer = GoldLight,
    onSecondaryContainer = GoldDark,
    tertiary = GoldDark,
    background = MixWhite,
    onBackground = TextPrimaryLight,
    surface = SandSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = SandSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    outlineVariant = Color(0xFFD6D0C3)
)

/**
 * Applies a balanced Green, White, and Red background canvas in equal proportions (1/3 each)
 * while preserving high readability for foreground cards and typography.
 */
fun Modifier.mixedQuadColorBackground(isDark: Boolean): Modifier = this.then(
    if (isDark) {
        Modifier
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to DarkMixGreen,
                        0.33f to Color(0xFF1E2925),
                        0.50f to Color(0xFF2B3036),
                        0.67f to Color(0xFF331B1F),
                        1.00f to DarkMixRed
                    )
                )
            )
            .drawBehind {
                // Balanced White luminance center band for dark mode
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x33FFFFFF), Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * 0.5f),
                        radius = size.maxDimension * 0.45f
                    ),
                    radius = size.maxDimension * 0.45f,
                    center = Offset(size.width * 0.5f, size.height * 0.5f)
                )
            }
    } else {
        Modifier
            .background(MixWhite)
            .drawBehind {
                // Equal-proportion 1/3 Green, 1/3 White, 1/3 Red background
                drawRect(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to Color(0xFF7ADCA6), // Top third: Green
                            0.25f to Color(0xFFC5F4DA),
                            0.36f to MixWhite,          // Middle third: Crisp White
                            0.64f to MixWhite,
                            0.75f to Color(0xFFFBC4C4),
                            1.00f to Color(0xFFF27D7D)  // Bottom third: Red
                        )
                    )
                )
                // Soft Green radial balance at top-left
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF5CD190).copy(alpha = 0.40f), Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.14f),
                        radius = size.maxDimension * 0.42f
                    ),
                    radius = size.maxDimension * 0.42f,
                    center = Offset(size.width * 0.15f, size.height * 0.14f)
                )
                // Crisp White radial balance across middle
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(MixWhite.copy(alpha = 0.90f), Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * 0.5f),
                        radius = size.maxDimension * 0.44f
                    ),
                    radius = size.maxDimension * 0.44f,
                    center = Offset(size.width * 0.5f, size.height * 0.5f)
                )
                // Soft Red radial balance at bottom-right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFEF6B6B).copy(alpha = 0.40f), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.86f),
                        radius = size.maxDimension * 0.42f
                    ),
                    radius = size.maxDimension * 0.42f,
                    center = Offset(size.width * 0.85f, size.height * 0.86f)
                )
            }
    }
)

@Composable
fun ArabCalendarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
