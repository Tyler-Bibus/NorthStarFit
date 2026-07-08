package com.northstarfit.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = NavyOnPrimary,
    secondary = GoldSecondary,
    onSecondary = GoldOnSecondary,
    surface = DaySurface,
    onSurface = DayOnSurface,
)

private val DarkColors = darkColorScheme(
    primary = NavyPrimaryDark,
    onPrimary = NavyOnPrimaryDark,
    secondary = GoldSecondaryDark,
    onSecondary = GoldOnSecondaryDark,
    surface = NightSurface,
    onSurface = NightOnSurface,
)

@Composable
fun NorthStarFitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color follows the user's wallpaper on Android 12+.
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
