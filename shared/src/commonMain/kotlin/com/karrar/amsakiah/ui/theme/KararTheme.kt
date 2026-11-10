package com.karrar.amsakiah.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.settings.ThemeMode
import com.karrar.amsakiah.ui.platform.ApplySystemBarAppearance

/**
 * ألوان التطبيق الدلالية — تقرأ من اللوحة الحالية (فاتح/داكن).
 *
 * كل `val` هنا دالة `@Composable` تُعاد قراءتها عند تغيير الوضع، لذلك يكفي
 * تبديل `themeMode` في الإعدادات لتتبدّل كل الواجهة فورًا دون إعادة تشغيل.
 */
object KararColors {
    val BackgroundTop: Color @Composable @ReadOnlyComposable get() = LocalKararPalette.current.backgroundTop
    val BackgroundBottom: Color @Composable @ReadOnlyComposable get() = LocalKararPalette.current.backgroundBottom
    val Card: Color @Composable @ReadOnlyComposable get() = LocalKararPalette.current.surface
    val CardSoft: Color @Composable @ReadOnlyComposable get() = LocalKararPalette.current.surfaceSoft
    val SurfaceHigh: Color @Composable @ReadOnlyComposable get() = LocalKararPalette.current.surfaceHigh
    val Gold: Color @Composable @ReadOnlyComposable get() = LocalKararPalette.current.gold
    val GoldDeep: Color @Composable @ReadOnlyComposable get() = LocalKararPalette.current.goldDeep
    val GoldSoft: Color @Composable @ReadOnlyComposable get() = LocalKararPalette.current.goldSoft
    val OnGold: Color @Composable @ReadOnlyComposable get() = LocalKararPalette.current.onGold
    val Purple: Color @Composable @ReadOnlyComposable get() = LocalKararPalette.current.purple
    val Cyan: Color @Composable @ReadOnlyComposable get() = LocalKararPalette.current.cyan
    val Pink: Color @Composable @ReadOnlyComposable get() = LocalKararPalette.current.pink
    val Sky: Color @Composable @ReadOnlyComposable get() = LocalKararPalette.current.sky
    val Amber: Color @Composable @ReadOnlyComposable get() = LocalKararPalette.current.amber
    val Rose: Color @Composable @ReadOnlyComposable get() = LocalKararPalette.current.rose
    val Mint: Color @Composable @ReadOnlyComposable get() = LocalKararPalette.current.mint
    val Teal: Color @Composable @ReadOnlyComposable get() = LocalKararPalette.current.teal
    val Violet: Color @Composable @ReadOnlyComposable get() = LocalKararPalette.current.violet
    val TextPrimary: Color @Composable @ReadOnlyComposable get() = LocalKararPalette.current.textPrimary
    val TextSecondary: Color @Composable @ReadOnlyComposable get() = LocalKararPalette.current.textSecondary
    val TextMuted: Color @Composable @ReadOnlyComposable get() = LocalKararPalette.current.textMuted
    val Border: Color @Composable @ReadOnlyComposable get() = LocalKararPalette.current.border
    val Success: Color @Composable @ReadOnlyComposable get() = LocalKararPalette.current.success
    val Danger: Color @Composable @ReadOnlyComposable get() = LocalKararPalette.current.danger
    val IsDark: Boolean @Composable @ReadOnlyComposable get() = LocalKararPalette.current.isDark
}

/** المخطط الداكن — هوية V7 الليلية. */
private val KararDarkScheme = darkColorScheme(
    primary = KararDarkPalette.gold,
    onPrimary = KararDarkPalette.onGold,
    primaryContainer = Color(0xFF5C4300),
    onPrimaryContainer = Color(0xFFFFE08A),
    secondary = KararDarkPalette.purple,
    onSecondary = Color(0xFF1A1030),
    secondaryContainer = Color(0xFF3B2A63),
    onSecondaryContainer = Color(0xFFE9DDFF),
    tertiary = KararDarkPalette.teal,
    onTertiary = Color(0xFF00201B),
    tertiaryContainer = Color(0xFF004D44),
    onTertiaryContainer = Color(0xFFB8FFEF),
    background = KararDarkPalette.backgroundTop,
    onBackground = KararDarkPalette.textPrimary,
    surface = KararDarkPalette.surface,
    onSurface = KararDarkPalette.textPrimary,
    surfaceVariant = KararDarkPalette.surfaceHigh,
    onSurfaceVariant = KararDarkPalette.textSecondary,
    surfaceContainerLowest = Color(0xFF070B13),
    surfaceContainerLow = Color(0xFF0B1120),
    surfaceContainer = Color(0xFF111827),
    surfaceContainerHigh = Color(0xFF1A2334),
    surfaceContainerHighest = Color(0xFF232D42),
    outline = KararDarkPalette.border,
    outlineVariant = Color(0x14FFFFFF),
    error = KararDarkPalette.danger,
    onError = Color(0xFF2B0000),
    inverseSurface = Color(0xFFF3EFE6),
    inverseOnSurface = Color(0xFF1C1B16),
    scrim = Color.Black
)

/** المخطط الفاتح — M3 Expressive، أسطح دافئة ونفس ذهبي الهوية (مغمّق للتباين). */
private val KararLightScheme = lightColorScheme(
    primary = KararLightPalette.gold,
    onPrimary = KararLightPalette.onGold,
    primaryContainer = Color(0xFFFFE08A),
    onPrimaryContainer = Color(0xFF261A00),
    secondary = KararLightPalette.purple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9DDFF),
    onSecondaryContainer = Color(0xFF1A1030),
    tertiary = KararLightPalette.teal,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB8FFEF),
    onTertiaryContainer = Color(0xFF00201B),
    background = KararLightPalette.backgroundTop,
    onBackground = KararLightPalette.textPrimary,
    surface = KararLightPalette.surface,
    onSurface = KararLightPalette.textPrimary,
    surfaceVariant = Color(0xFFEFE8DA),
    onSurfaceVariant = KararLightPalette.textSecondary,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFFF8EC),
    surfaceContainer = Color(0xFFF7F0E2),
    surfaceContainerHigh = Color(0xFFF0E8D9),
    surfaceContainerHighest = Color(0xFFE8E0D0),
    outline = Color(0xFF7C7668),
    outlineVariant = Color(0xFFD5CDBD),
    error = KararLightPalette.danger,
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
 * @param mode `system` (تلقائي حسب النظام) / `light` / `dark`.
 */
@Composable
fun KararTheme(
    mode: String = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val dark = when (mode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        else -> isSystemInDarkTheme()
    }
    val palette = if (dark) KararDarkPalette else KararLightPalette

    // أيقونات شريط الحالة/التنقل تتبع الوضع الفعلي، لا وضع النظام فقط.
    ApplySystemBarAppearance(lightBackground = !dark)

    CompositionLocalProvider(LocalKararPalette provides palette) {
        MaterialTheme(
            colorScheme = if (dark) KararDarkScheme else KararLightScheme,
            typography = KararTypography,
            shapes = KararShapes,
            content = content
        )
    }
}
