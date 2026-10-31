package com.karrar.amsakiah

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.karrar.amsakiah.ui.navigation.AdaptiveAppScaffold

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFD54F),
    secondary = Color(0xFF81C784),
    background = Color(0xFF0D0B14),
    surface = Color(0xFF1E1B2E),
    onPrimary = Color(0xFF261200),
    onSecondary = Color(0xFF003300),
    onBackground = Color(0xFFEDEDED),
    onSurface = Color(0xFFEDEDED)
)

@Composable
fun App() {
    MaterialTheme(
        colorScheme = DarkColorScheme
    ) {
        AdaptiveAppScaffold()
    }
}
