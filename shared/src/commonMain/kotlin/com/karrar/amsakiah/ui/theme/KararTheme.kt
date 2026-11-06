package com.karrar.amsakiah.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** لوحة ألوان V7 — ليل أزرق عميق + ذهبي رمضاني */
object KararColors {
    val BackgroundTop = Color(0xFF050A15)
    val BackgroundBottom = Color(0xFF0A1120)
    val Card = Color(0xFF1A2030)
    val CardSoft = Color(0x14FFFFFF)
    val Gold = Color(0xFFFFD700)
    val GoldDeep = Color(0xFFFFA000)
    val GoldSoft = Color(0x33FFD700)
    val Purple = Color(0xFFA18CD1)
    val Cyan = Color(0xFF00F2FE)
    val Pink = Color(0xFFFBC2EB)
    val TextPrimary = Color(0xFFF5F5F7)
    val TextSecondary = Color(0xFFB9C0D4)
    val TextMuted = Color(0xFF7C8598)
    val Border = Color(0x1FFFFFFF)
    val Success = Color(0xFF4CAF50)
    val Danger = Color(0xFFEF5350)
}

private val KararColorScheme = darkColorScheme(
    primary = KararColors.Gold,
    onPrimary = Color(0xFF241A00),
    secondary = KararColors.Purple,
    onSecondary = Color(0xFF1A1030),
    tertiary = KararColors.Cyan,
    background = KararColors.BackgroundTop,
    onBackground = KararColors.TextPrimary,
    surface = KararColors.Card,
    onSurface = KararColors.TextPrimary,
    surfaceVariant = KararColors.Card,
    onSurfaceVariant = KararColors.TextSecondary,
    outline = KararColors.Border,
    error = KararColors.Danger
)

private val KararTypography = Typography(
    displayLarge = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.sp),
    displayMedium = TextStyle(fontSize = 38.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium)
)

@Composable
fun KararTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KararColorScheme,
        typography = KararTypography,
        content = content
    )
}
