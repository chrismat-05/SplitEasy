package com.spliteasy.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

private val DarkColorScheme = darkColorScheme(
    primary = Emerald,
    secondary = DarkEmerald,
    tertiary = Rose,
    background = DeepNavy,
    surface = Slate,
    surfaceVariant = LightSlate,
    onPrimary = DeepNavy,
    onBackground = Pearl,
    onSurface = Pearl
)

private val LightColorScheme = lightColorScheme(
    primary = DeepNavy,
    secondary = Slate,
    tertiary = Emerald,
    background = Pearl,
    surface = Color.White,
    surfaceVariant = LightPearl,
    onPrimary = Color.White,
    onBackground = DeepNavy,
    onSurface = DeepNavy
)

@Composable
fun SplitEasyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
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
