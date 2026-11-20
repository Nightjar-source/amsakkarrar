package com.karrar.amsakiah.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * لوحة الألوان الدلالية للتطبيق — الوضع الداكن والفاتح.
 *
 * المبدأ (Material 3 Expressive): لا تُستخدم ألوان حرفية داخل الشاشات؛ كل شيء
 * يقرأ من [LocalKararPalette] عبر [KararColors] حتى يتبدّل الوضعان فورًا
 * دون إعادة بناء الواجهات.
 */
@Immutable
data class KararPalette(
    val isDark: Boolean,

    // الخلفية العامة (خلف أشرطة النظام أيضًا)
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val tealGlow: Color,
    val indigoGlow: Color,

    // الأسطح
    val surface: Color,
    val surfaceSoft: Color,
    val surfaceHigh: Color,
    val glass: Color,
    val glassSoft: Color,
    val glassDeep: Color,
    val border: Color,
    val borderStrong: Color,
    val highlight: Color,
    val highlightStrong: Color,
    val scrim: Color,
    val shadow: Color,

    // النصوص
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,

    // الهوية
    val gold: Color,
    val goldDeep: Color,
    val goldSoft: Color,
    val onGold: Color,
    val purple: Color,
    val cyan: Color,
    val pink: Color,

    // الحالة
    val teal: Color,
    val sky: Color,
    val rose: Color,
    val violet: Color,
    val mint: Color,
    val amber: Color,
    val success: Color,
    val danger: Color,

    // خلفية شاشة «اليوم» (السماء) — تتغيّر مع الوضع
    val skyTop: Color,
    val skyBottom: Color,
    val skyText: Color,
    /** هل تستخدم شاشة اليوم نصوصًا داكنة؟ (الوضع الفاتح) */
    val onLightSky: Boolean
)

/**
 * الوضع الداكن — هوية V7 الليلية (أزرق عميق + ذهبي).
 * القيم مأخوذة من `appkarv/amsak3d.tsx` (themeClasses.night / bg-[#070b13]).
 */
val KararDarkPalette = KararPalette(
    isDark = true,
    backgroundTop = Color(0xFF070B13),
    backgroundBottom = Color(0xFF0B1020),
    tealGlow = Color(0x142DD4BF),
    indigoGlow = Color(0x146366F1),
    surface = Color(0xFF111827),
    surfaceSoft = Color(0x14FFFFFF),
    surfaceHigh = Color(0xFF1A2334),
    glass = Color(0xE60B1120),
    glassSoft = Color(0xCC0B1120),
    glassDeep = Color(0xF2070C18),
    border = Color(0x1AFFFFFF),
    borderStrong = Color(0x33FFFFFF),
    highlight = Color(0x14FFFFFF),
    highlightStrong = Color(0x24FFFFFF),
    scrim = Color(0x99000000),
    shadow = Color(0xCC000000),
    textPrimary = Color(0xFFF5F5F7),
    textSecondary = Color(0xFFB9C0D4),
    textMuted = Color(0xFF7C8598),
    gold = Color(0xFFFFD700),
    goldDeep = Color(0xFFFFA000),
    goldSoft = Color(0x33FFD700),
    onGold = Color(0xFF241A00),
    purple = Color(0xFFA18CD1),
    cyan = Color(0xFF00F2FE),
    pink = Color(0xFFFBC2EB),
    teal = Color(0xFF2DD4BF),
    sky = Color(0xFF38BDF8),
    rose = Color(0xFFF43F5E),
    violet = Color(0xFFA78BFA),
    mint = Color(0xFF34D399),
    amber = Color(0xFFFBBF24),
    success = Color(0xFF4CAF50),
    danger = Color(0xFFEF5350),
    skyTop = Color(0xFF050A15),
    skyBottom = Color(0xFF0A1120),
    skyText = Color(0xFFF5F5F7),
    onLightSky = false
)

/**
 * الوضع الفاتح — Material 3 Expressive: أسطح دافئة عالية التباين مع نفس ذهبي
 * الهوية (يُغمَّق إلى #7A5900 لتحقيق تباين 4.5:1 على الأسطح الفاتحة).
 */
val KararLightPalette = KararPalette(
    isDark = false,
    backgroundTop = Color(0xFFFFFBF4),
    backgroundBottom = Color(0xFFF7EFE1),
    tealGlow = Color(0x1A0F766E),
    indigoGlow = Color(0x144F46E5),
    surface = Color(0xFFFFFDF8),
    surfaceSoft = Color(0x0F000000),
    surfaceHigh = Color(0xFFF1E7D6),
    glass = Color(0xF2FFFFFF),
    glassSoft = Color(0xE6FFFFFF),
    glassDeep = Color(0xFAFFFDF7),
    border = Color(0x1F000000),
    borderStrong = Color(0x33000000),
    highlight = Color(0x66FFFFFF),
    highlightStrong = Color(0x99FFFFFF),
    scrim = Color(0x66000000),
    shadow = Color(0x33000000),
    textPrimary = Color(0xFF1B1B1F),
    textSecondary = Color(0xFF44464F),
    textMuted = Color(0xFF6F7280),
    gold = Color(0xFF7A5900),
    goldDeep = Color(0xFF5C4300),
    goldSoft = Color(0x29FFD700),
    onGold = Color(0xFFFFFFFF),
    purple = Color(0xFF5B4A7E),
    cyan = Color(0xFF00696E),
    pink = Color(0xFF8E4585),
    teal = Color(0xFF0F766E),
    sky = Color(0xFF0369A1),
    rose = Color(0xFFBE123C),
    violet = Color(0xFF6D28D9),
    mint = Color(0xFF047857),
    amber = Color(0xFF92400E),
    success = Color(0xFF2E7D32),
    danger = Color(0xFFB3261E),
    skyTop = Color(0xFFFFF7E8),
    skyBottom = Color(0xFFF3E7D2),
    skyText = Color(0xFF1B1B1F),
    onLightSky = true
)

val LocalKararPalette = staticCompositionLocalOf { KararDarkPalette }

/** هل الوضع الحالي فاتح؟ — للاستخدام في القرارات البصرية. */
val kararIsLight: Boolean
    @Composable
    @ReadOnlyComposable
    get() = !LocalKararPalette.current.isDark
