package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    onPrimary = MainText,
    primaryContainer = CardBackgroundElevated,
    onPrimaryContainer = MainText,
    secondary = SecondaryBlue,
    onSecondary = MainText,
    secondaryContainer = CardBackground,
    onSecondaryContainer = MainText,
    tertiary = GoldRewards,
    onTertiary = MainBackground,
    background = MainBackground,
    onBackground = MainText,
    surface = SecondaryBackground,
    onSurface = MainText,
    surfaceVariant = CardBackground,
    onSurfaceVariant = SecondaryText,
    error = DangerRed,
    onError = MainText,
    outline = BorderColor
)

private val LightColorScheme = DarkColorScheme // Default dark gaming theme as required

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = MainBackground.toArgb()
                window.navigationBarColor = MainBackground.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
