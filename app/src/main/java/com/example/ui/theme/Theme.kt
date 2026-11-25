package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val AutonomaLightColorScheme = lightColorScheme(
    primary = AutonomaOrange,
    onPrimary = Color.White,
    primaryContainer = AutonomaOrangeContainer,
    onPrimaryContainer = AutonomaOrangeDeep,
    secondary = CampusSlate,
    onSecondary = Color.White,
    secondaryContainer = CampusSurfaceVariant,
    onSecondaryContainer = CampusSlate,
    tertiary = CampusOchre,
    onTertiary = Color.White,
    tertiaryContainer = CampusOchreContainer,
    onTertiaryContainer = Color(0xFF472F00),
    background = CampusWarmBg,
    onBackground = CampusSlate,
    surface = CampusSurface,
    onSurface = CampusSlate,
    surfaceVariant = CampusSurfaceVariant,
    onSurfaceVariant = CampusMutedText,
    outline = CampusOutline,
    outlineVariant = Color(0xFFEBE3D8),
    error = CampusCrimson,
    onError = Color.White,
    errorContainer = CampusCrimsonContainer,
    onErrorContainer = Color(0xFF690005)
)

private val AutonomaDarkColorScheme = darkColorScheme(
    primary = AutonomaOrange,
    onPrimary = Color(0xFF2B1400),
    primaryContainer = DarkOrangeContainer,
    onPrimaryContainer = AutonomaOrangeLight,
    secondary = AutonomaOrangeLight,
    onSecondary = Color(0xFF2B1400),
    secondaryContainer = DarkCampusSurfaceVariant,
    onSecondaryContainer = Color(0xFFE5E8EC),
    tertiary = Color(0xFFF0C040),
    onTertiary = Color(0xFF2D1F00),
    tertiaryContainer = Color(0xFF4A3700),
    onTertiaryContainer = CampusOchreContainer,
    background = DarkCampusBg,
    onBackground = Color(0xFFF2F4F7),
    surface = DarkCampusSurface,
    onSurface = Color(0xFFF2F4F7),
    surfaceVariant = DarkCampusSurfaceVariant,
    onSurfaceVariant = Color(0xFFA7B0BC),
    outline = DarkCampusOutline,
    outlineVariant = Color(0xFF2B3039),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF490005),
    errorContainer = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6)
)

val CampusShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AulaVirtualTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AutonomaDarkColorScheme else AutonomaLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = CampusShapes,
        content = content
    )
}
