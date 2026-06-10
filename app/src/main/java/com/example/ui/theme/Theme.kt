package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AmberPrimaryDark = Color(0xFFE09535)
val AmberPrimaryLight = Color(0xFFC47F22)
val IncognitoPrimary = Color(0xFF7C7CF0)

private val LightColorScheme = lightColorScheme(
    primary = AmberPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFBEAD2),
    onPrimaryContainer = Color(0xFF3B2400),
    secondary = Color(0xFF6E5D4A),
    background = Color(0xFFF4F4F5),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF18181B),
    onSurface = Color(0xFF18181B),
    outline = Color(0xFFD4D4DC)
)

private val DarkColorScheme = darkColorScheme(
    primary = AmberPrimaryDark,
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF382306),
    onPrimaryContainer = Color(0xFFFDD8A5),
    secondary = Color(0xFF8B8B97),
    background = Color(0xFF111114),
    surface = Color(0xFF1C1C22),
    onBackground = Color(0xFFEAEAEF),
    onSurface = Color(0xFFEAEAEF),
    outline = Color(0xFF28282F)
)

private val IncognitoColorScheme = darkColorScheme(
    primary = IncognitoPrimary,
    onPrimary = Color.White,
    background = Color(0xFF12121E),
    surface = Color(0xFF1A1A30),
    onBackground = Color(0xFFEAEAEF),
    onSurface = Color(0xFFEAEAEF),
    outline = Color(0xFF24243C)
)

@Composable
fun ChromiumTheme(
    themeMode: String = "dark", // "light", "dark", "incognito"
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        "light" -> LightColorScheme
        "incognito" -> IncognitoColorScheme
        else -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

