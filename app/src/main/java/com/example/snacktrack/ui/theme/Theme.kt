package com.example.snacktrack.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Green,
    onPrimary = Surface,
    primaryContainer = LightGreen,
    onPrimaryContainer = DarkGreen,
    secondary = Yellow,
    onSecondary = Surface,
    secondaryContainer = LightYellow,
    onSecondaryContainer = DarkYellow,
    tertiary = Red,
    onTertiary = Surface,
    tertiaryContainer = LightRed,
    onTertiaryContainer = DarkRed,
    background = Color(0xFF121212),
    onBackground = Surface,
    surface = Color(0xFF1E1E1E),
    onSurface = Surface
)

private val LightColorScheme = lightColorScheme(
    primary = Green,
    onPrimary = Surface,
    primaryContainer = LightGreen,
    onPrimaryContainer = DarkGreen,
    secondary = Yellow,
    onSecondary = Color(0xFF333333),
    secondaryContainer = LightYellow,
    onSecondaryContainer = DarkYellow,
    tertiary = Red,
    onTertiary = Surface,
    tertiaryContainer = LightRed,
    onTertiaryContainer = DarkRed,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface
)

@Composable
fun SnacktrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Deaktiviert, um konsistente Markenfarben zu gewährleisten
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
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}