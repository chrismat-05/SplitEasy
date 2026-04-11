package com.spliteasy.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import com.spliteasy.ui.SettingsManager

private val DarkColorScheme = darkColorScheme(
    primary = White,
    secondary = Emerald,
    tertiary = MediumGrey,
    background = Black,
    surface = DarkGrey,
    surfaceVariant = MediumGrey,
    onPrimary = Black,
    onBackground = White,
    onSurface = White
)

private val LightColorScheme = lightColorScheme(
    primary = Black,
    secondary = MediumGrey,
    tertiary = LightGrey,
    background = White,
    surface = LightGrey,
    surfaceVariant = MediumGrey,
    onPrimary = White,
    onBackground = Black,
    onSurface = Black
)

@Composable
fun SplitEasyTheme(
    content: @Composable () -> Unit
) {
    val themeMode by SettingsManager.themeMode.collectAsState(initial = "Dark")
    val isSystemDark = isSystemInDarkTheme()
    
    val darkTheme = when(themeMode) {
        "Light" -> false
        "Dark" -> true
        else -> isSystemDark
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
