package com.karrar.amsakiah.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** لوحة ألوان V7 — ليل أزرق عميق + ذهبي رمضاني */
object KararColors {
    val BackgroundTop = Color(0xFF050A15)
    val BackgroundBottom = Color(0xFF0A1120)
    val Card = Color(0xFF1A2030)
    val CardSoft = Color(0x14FFFFFF)
    val Gold = KararTokens.Accent.gold
    val GoldDeep = KararTokens.Accent.goldDeep
    val GoldSoft = KararTokens.Accent.goldSoft
    val Purple = Color(0xFFA18CD1)
    val Cyan = Color(0xFF00F2FE)
    val Pink = Color(0xFFFBC2EB)
    val TextPrimary = Color(0xFFF5F5F7)
    val TextSecondary = Color(0xFFB9C0D4)
    val TextMuted = Color(0xFF7C8598)
    val Border = KararTokens.Glass.border
    val Success = Color(0xFF4CAF50)
    val Danger = Color(0xFFEF5350)

    // أسطح إضافية للثيم الفاتح والداكن
    val SurfaceDark = Color(0xFF111827)
    val SurfaceDarkHigh = Color(0xFF1B2436)
    val SurfaceLight = Color(0xFFFFFBF2)
    val OnLight = Color(0xFF1C1B16)
}

/** المخطط الداكن — التصميم الأساسي للتطبيق. */
private val KararDarkScheme = darkColorScheme(
    primary = KararColors.Gold,
    onPrimary = Color(0xFF241A00),
    primaryContainer = Color(0xFF5C4300),
    onPrimaryContainer = Color(0xFFFFE08A),
    secondary = KararColors.Purple,
    onSecondary = Color(0xFF1A1030),
    secondaryContainer = Color(0xFF3B2A63),
    onSecondaryContainer = Color(0xFFE9DDFF),
    tertiary = KararTokens.Accent.teal,
    onTertiary = Color(0xFF00201B),
    tertiaryContainer = Color(0xFF004D44),
    onTertiaryContainer = Color(0xFFB8FFEF),
    background = KararColors.BackgroundTop,
    onBackground = KararColors.TextPrimary,
    surface = KararColors.Card,
    onSurface = KararColors.TextPrimary,
    surfaceVariant = KararColors.SurfaceDarkHigh,
    onSurfaceVariant = KararColors.TextSecondary,
    surfaceContainerLowest = Color(0xFF070B13),
    surfaceContainerLow = Color(0xFF0B1120),
    surfaceContainer = Color(0xFF111827),
    surfaceContainerHigh = Color(0xFF1A2334),
    surfaceContainerHighest = Color(0xFF232D42),
    outline = KararColors.Border,
    outlineVariant = Color(0x14FFFFFF),
    error = KararColors.Danger,
    onError = Color(0xFF2B0000),
    inverseSurface = Color(0xFFF3EFE6),
    inverseOnSurface = Color(0xFF1C1B16),
    scrim = Color.Black
)

/** المخطط الفاتح — بديل اختياري مع الحفاظ على الهوية الذهبية. */
private val KararLightScheme = lightColorScheme(
    primary = Color(0xFF7A5900),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE08A),
    onPrimaryContainer = Color(0xFF261A00),
    secondary = Color(0xFF5B4A7E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9DDFF),
    onSecondaryContainer = Color(0xFF1A1030),
    tertiary = Color(0xFF00695E),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB8FFEF),
    onTertiaryContainer = Color(0xFF00201B),
    background = KararColors.SurfaceLight,
    onBackground = KararColors.OnLight,
    surface = Color(0xFFFFFDF7),
    onSurface = KararColors.OnLight,
    surfaceVariant = Color(0xFFEFE8DA),
    onSurfaceVariant = Color(0xFF4C4639),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFFF8EC),
    surfaceContainer = Color(0xFFF7F0E2),
    surfaceContainerHigh = Color(0xFFF0E8D9),
    surfaceContainerHighest = Color(0xFFE8E0D0),
    outline = Color(0xFF7C7668),
    outlineVariant = Color(0xFFD5CDBD),
    error = Color(0xFFB3261E),
    onError = Color.White
)

private val KararTypography = Typography(
    displayLarge = TextStyle(fontSize = 48.sp, lineHeight = 58.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.sp),
    displayMedium = TextStyle(fontSize = 38.sp, lineHeight = 46.sp, fontWeight = FontWeight.Bold),
    displaySmall = TextStyle(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 18.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 28.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 24.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium)
)

/** أشكال M3 Expressive — زوايا كبيرة ناعمة وكبسولات. */
private val KararShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

/**
 * ثيم التطبيق الموحّد.
 *
 * @param darkTheme التصميم الأساسي داكن؛ يُمرَّر `false` عند إضافة الوضع الفاتح
 *   الكامل للشاشات لاحقًا.
 */
@Composable
fun KararTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) KararDarkScheme else KararLightScheme,
        typography = KararTypography,
        shapes = KararShapes,
        content = content
    )
}
