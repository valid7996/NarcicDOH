package com.narcic.doh.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NarcicColorScheme = darkColorScheme(
    primary = Cyan,
    secondary = Violet,
    tertiary = Mint,
    background = DeepSpace,
    surface = SurfaceNavy,
    onPrimary = DeepSpace,
    onSecondary = Snow,
    onTertiary = DeepSpace,
    onBackground = Snow,
    onSurface = Snow,
    surfaceVariant = SurfaceNavyHigh,
    onSurfaceVariant = Mist,
    outline = StrokeNavy
)

@Composable
fun NarcicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DeepSpace.toArgb()
            window.navigationBarColor = DeepSpace.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = NarcicColorScheme,
        typography = Typography,
        content = content
    )
}
