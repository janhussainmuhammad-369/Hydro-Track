package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AquaPrimaryDark,
    onPrimary = AquaOnPrimaryDark,
    primaryContainer = AquaPrimaryContainerDark,
    onPrimaryContainer = AquaOnPrimaryContainerDark,
    secondary = AquaSecondaryDark,
    secondaryContainer = AquaSecondaryContainerDark,
    tertiary = AquaTertiaryDark,
    background = AquaBackgroundDark,
    surface = AquaSurfaceDark,
    surfaceVariant = AquaSurfaceVariantDark,
)

private val LightColorScheme = lightColorScheme(
    primary = AquaPrimaryLight,
    onPrimary = AquaOnPrimaryLight,
    primaryContainer = AquaPrimaryContainerLight,
    onPrimaryContainer = AquaOnPrimaryContainerLight,
    secondary = AquaSecondaryLight,
    secondaryContainer = AquaSecondaryContainerLight,
    tertiary = AquaTertiaryLight,
    background = AquaBackgroundLight,
    surface = AquaSurfaceLight,
    surfaceVariant = AquaSurfaceVariantLight,
)

@Composable
fun HydroTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded refreshing aqua palette by default
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
        content = content
    )
}
