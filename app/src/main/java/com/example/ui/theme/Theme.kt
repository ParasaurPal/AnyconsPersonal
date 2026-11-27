package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = FjordDarkPrimary,
    onPrimary = FjordDarkOnPrimary,
    primaryContainer = FjordDarkPrimaryContainer,
    onPrimaryContainer = FjordDarkOnPrimaryContainer,
    secondary = FjordDarkSecondary,
    onSecondary = FjordDarkOnSecondary,
    secondaryContainer = FjordDarkSecondaryContainer,
    onSecondaryContainer = FjordDarkOnSecondaryContainer,
    tertiary = FjordDarkTertiary,
    onTertiary = FjordDarkOnTertiary,
    tertiaryContainer = FjordDarkTertiaryContainer,
    onTertiaryContainer = FjordDarkOnTertiaryContainer,
    background = FjordDarkBackground,
    onBackground = FjordDarkOnBackground,
    surface = FjordDarkSurface,
    onSurface = FjordDarkOnSurface,
    surfaceVariant = FjordDarkSurfaceVariant,
    onSurfaceVariant = FjordDarkOnSurfaceVariant,
    surfaceContainerHighest = FjordDarkSurfaceHighest,
    outline = FjordDarkOutline,
    outlineVariant = FjordDarkOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = FjordLightPrimary,
    onPrimary = FjordLightOnPrimary,
    primaryContainer = FjordLightPrimaryContainer,
    onPrimaryContainer = FjordLightOnPrimaryContainer,
    secondary = FjordLightSecondary,
    onSecondary = FjordLightOnSecondary,
    secondaryContainer = FjordLightSecondaryContainer,
    onSecondaryContainer = FjordLightOnSecondaryContainer,
    tertiary = FjordLightTertiary,
    onTertiary = FjordLightOnTertiary,
    tertiaryContainer = FjordLightTertiaryContainer,
    onTertiaryContainer = FjordLightOnTertiaryContainer,
    background = FjordLightBackground,
    onBackground = FjordLightOnBackground,
    surface = FjordLightSurface,
    onSurface = FjordLightOnSurface,
    surfaceVariant = FjordLightSurfaceVariant,
    onSurfaceVariant = FjordLightOnSurfaceVariant,
    surfaceContainerHighest = FjordLightSurfaceHighest,
    outline = FjordLightOutline,
    outlineVariant = FjordLightOutlineVariant
)

val FjordShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun FjordIconsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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
        shapes = FjordShapes,
        content = content
    )
}
