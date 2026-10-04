package com.westly.wipuzzle.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.westly.wipuzzle.data.ThemeMode

@Immutable
class Palette(
    val isDark: Boolean,
    val bg: Color,
    val surface: Color,
    val raised: Color,
    val line: Color,
    val text: Color,
    val dim: Color,
    val accent: Color,
    val onAccent: Color,
    val danger: Color,
)

val DarkPalette = Palette(
    isDark = true,
    bg = Color(0xFF0F1012),
    surface = Color(0xFF17181B),
    raised = Color(0xFF212328),
    line = Color(0xFF2C2F35),
    text = Color(0xFFEDE9E2),
    dim = Color(0xFF9B978F),
    accent = Color(0xFFC9A45C),
    onAccent = Color(0xFF17140D),
    danger = Color(0xFFD98A7B),
)

val LightPalette = Palette(
    isDark = false,
    bg = Color(0xFFF5F2EC),
    surface = Color(0xFFFFFFFF),
    raised = Color(0xFFEBE7DF),
    line = Color(0xFFD9D4C9),
    text = Color(0xFF1B1A17),
    dim = Color(0xFF6E6A62),
    accent = Color(0xFF8A6A25),
    onAccent = Color(0xFFFFFFFF),
    danger = Color(0xFFA8432F),
)

val LocalPalette = staticCompositionLocalOf { DarkPalette }

object Wip {
    val c: Palette
        @Composable
        @ReadOnlyComposable
        get() = LocalPalette.current
}

object Type {
    private val serif = FontFamily.Serif

    val display = TextStyle(
        fontFamily = serif, fontWeight = FontWeight.Normal,
        fontSize = 46.sp, lineHeight = 50.sp, letterSpacing = (-0.8).sp,
    )
    val title = TextStyle(
        fontFamily = serif, fontWeight = FontWeight.Normal,
        fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.2).sp,
    )
    val heading = TextStyle(
        fontFamily = serif, fontWeight = FontWeight.Normal,
        fontSize = 21.sp, lineHeight = 27.sp,
    )
    val numeral = TextStyle(
        fontFamily = serif, fontWeight = FontWeight.Normal,
        fontSize = 24.sp, lineHeight = 28.sp,
    )
    val body = TextStyle(fontSize = 15.sp, lineHeight = 22.sp)
    val button = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.2.sp)
    val label = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.6.sp)
    val small = TextStyle(fontSize = 12.sp, lineHeight = 16.sp)
}

@Composable
fun WipTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val palette = if (dark) DarkPalette else LightPalette

    val scheme = if (dark) {
        darkColorScheme(
            primary = palette.accent, onPrimary = palette.onAccent,
            background = palette.bg, onBackground = palette.text,
            surface = palette.surface, onSurface = palette.text,
        )
    } else {
        lightColorScheme(
            primary = palette.accent, onPrimary = palette.onAccent,
            background = palette.bg, onBackground = palette.text,
            surface = palette.surface, onSurface = palette.text,
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !dark
                controller.isAppearanceLightNavigationBars = !dark
            }
        }
    }

    MaterialTheme(colorScheme = scheme) {
        CompositionLocalProvider(LocalPalette provides palette) {
            content()
        }
    }
}
