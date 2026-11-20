package com.karrar.amsakiah.core.astronomy

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min

/**
 * بيانات طور القمر عند يوم هجري — مطابقة حرفيًا لخوارزمية `amsak3d.tsx`
 * (الدالة `getMoonDetailsByHijriDay`).
 */
data class MoonDetails(
    /** الطور 0..1 — 0 ولادة هلال، 0.5 بدر، ~1 محاق. */
    val phase: Double,
    /** نسبة إضاءة القمر 0..100 — تُحرض في Qibla/Shirines. */
    val illumination: Int,
    /** الاسم العربي للطور (هلال غرة الشهر … محاق آخر الشهر). */
    val phaseName: String
)

/** احسب تفاصيل القمر ليوم هجري (1..30). */
fun moonDetailsByHijriDay(hijriDay: Int): MoonDetails {
    val d = max(1, min(30, hijriDay))
    val phase = (d - 0.5) / 29.53
    val illumination = ((1.0 - cos(phase * 2 * PI)) / 2.0 * 100).toInt()
    val phaseName = when {
        d == 1 -> "هلال غرة الشهر"
        d in 2..5 -> "هلال متزايد"
        d in 6..9 -> "تربيع أول"
        d in 10..12 -> "أحدب متزايد"
        d in 13..16 -> "بدر التمام (الأيام البيض)"
        d in 17..20 -> "أحدب متناقص"
        d in 21..24 -> "تربيع أخير"
        d in 25..28 -> "هلال أواخر الشهر"
        else -> "محاق آخر الشهر"
    }
    return MoonDetails(phase, illumination, phaseName)
}

/**
 * الهندسة المستخدمة لرسم القمر بالكانفس — مطابقة:
 * `const RealisticMoonPhaseSVG` في المرجع:
 *
 * - `isWaxing` هل الطور في الجزء الأول (قبل البدر) أم الأخير.
 * - `k` منحنى الإضاءة: 0 في الولادة، 1 في البدر الكامل، 0 في المحاق — يُحدّد
 *   نصف قطر السطح غير المضاء (`rx = |1 - 2k| * r`).
 * - لا ترتيب sweep لتمكين الرسم الاتجاهي للهلال/البدر/المحاق.
 */
data class MoonGeometry(
    val hijriDay: Int,
    val isWaxing: Boolean,
    val rx: Float,
    val illuminationRatio: Float,
    /** true ⇒ لا يُرسم السطح المضاء (محاق/ولادة صفرية). */
    val isNewMoon: Boolean,
    /** true ⇒ يُرسم السطح المضاء كاملًا (بدر التمام). */
    val isFullMoon: Boolean
)

fun moonGeometry(hijriDay: Int, radiusUnits: Float): MoonGeometry {
    val d = max(1, min(30, hijriDay))
    val phase = (d - 0.5) / 29.53
    val isWaxing = phase <= 0.5
    val k = abs(if (isWaxing) phase * 2 else (1 - phase) * 2)
    val rx = max(0.2f, abs(1f - 2f * k.toFloat()) * radiusUnits)
    return MoonGeometry(
        hijriDay = d,
        isWaxing = isWaxing,
        rx = rx,
        illuminationRatio = min(1f, max(0f, k.toFloat())),
        isNewMoon = k <= 0.02,
        isFullMoon = k >= 0.98
    )
}
