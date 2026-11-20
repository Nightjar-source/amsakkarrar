package com.karrar.amsakiah.core.arc

import com.karrar.amsakiah.core.state.PrayerCalculator
import kotlin.math.roundToInt

/**
 * حسابات القوس — منطق فلكي خالص بلا أي مرجع إلى Compose أو الواجهة.
 *
 * يستهلكها أي مسار (الشاشة، الودجت، الإشعار) يريد معرفة موضع الشمس/القمر
 * على قوس اليوم وتسمية طرفيه وشارة المرحلة الفرعية.
 */

/** مرحلة القوس لحظة زمنية: النسبة على القوس + طرفاه واسماه وساعاتهما. */
data class ArcPhase(
    val progress: Float,
    val leftLabel: String,
    val leftHour: Double,
    val rightLabel: String,
    val rightHour: Double,
    val badge: String?
)

/** ساعات عشرية ⇒ ثوانٍ من منتصف الليل. */
fun secondsOf(hour: Double): Int = (hour * 3600.0).roundToInt()

/**
 * مرحلة القوس عند `nowSeconds` من منتصف الليل:
 * 1) `maghrib .. fajr` أو بعد منتصف الليل → الليل (من المغرب إلى الفجر).
 * 2) `fajr .. sunrise`                 → الفجر → بانتظار الشروق (شارة ذهبية).
 * 3) `sunrise .. sunset`                → النهار (الشيعي: الغروب الفلكي / السني: المغرب).
 * 4) بعد الغروب الفلكي وقبل المغرب      → شروق ← مغرب (شارة ذهاب الحمرة).
 */
fun computeArcPhase(
    nowSeconds: Int,
    times: Map<String, Double>,
    tomorrowTimes: Map<String, Double>,
    isShia: Boolean,
    use24Hour: Boolean = false
): ArcPhase {
    val fajr = times["fajr"] ?: 5.0
    val sunrise = times["sunrise"] ?: 6.5
    val sunset = times["sunset"] ?: 18.0
    val maghrib = times["maghrib"] ?: 18.25
    val fajrSec = secondsOf(fajr)
    val sunriseSec = secondsOf(sunrise)
    val sunsetSec = secondsOf(sunset)
    val maghribSec = secondsOf(maghrib)
    fun timeText(hour: Double): String = PrayerCalculator.formatHour(hour, use24Hour).first

    return when {
        // 1) الليل: من المغرب إلى الفجر
        nowSeconds >= maghribSec || nowSeconds < fajrSec -> {
            val rightHour = if (nowSeconds >= maghribSec) {
                tomorrowTimes["fajr"] ?: fajr
            } else {
                fajr
            }
            val span = ((86400 - maghribSec) + fajrSec).coerceAtLeast(1)
            val elapsed = if (nowSeconds >= maghribSec) {
                nowSeconds - maghribSec
            } else {
                nowSeconds + (86400 - maghribSec)
            }
            ArcPhase(
                progress = (elapsed.toFloat() / span).coerceIn(0f, 1f),
                leftLabel = "المغرب",
                leftHour = maghrib,
                rightLabel = "الفجر",
                rightHour = rightHour,
                badge = null
            )
        }

        // 2) بين الفجر والشروق — امتداد المسار الليلي نفسه (من المغرب ثم الفجر وحتى الشروق).
        nowSeconds < sunriseSec -> {
            val span = ((86400 - maghribSec) + sunriseSec).coerceAtLeast(1)
            val elapsed = nowSeconds + (86400 - maghribSec)
            ArcPhase(
                progress = (elapsed.toFloat() / span).coerceIn(0f, 1f),
                leftLabel = "المغرب",
                leftHour = maghrib,
                rightLabel = "الشروق",
                rightHour = sunrise,
                badge = "حلّ الفجر (${timeText(fajr)}) • بانتظار الشروق"
            )
        }

        // 3) النهار: من الشروق إلى الغروب (الشيعي) أو المغرب (السني)
        nowSeconds < sunsetSec -> {
            val endHour = if (isShia) sunset else maghrib
            val endSec = if (isShia) sunsetSec else maghribSec
            val span = (endSec - sunriseSec).coerceAtLeast(1)
            val elapsed = nowSeconds - sunriseSec
            ArcPhase(
                progress = (elapsed.toFloat() / span).coerceIn(0f, 1f),
                leftLabel = "الشروق",
                leftHour = sunrise,
                rightLabel = if (isShia) "الغروب" else "المغرب",
                rightHour = endHour,
                badge = null
            )
        }

        // 4) بين الغروب الفلكي والمغرب الشرعي (ذهاب الحمرة المشرقية)
        else -> {
            val span = (maghribSec - sunriseSec).coerceAtLeast(1)
            val elapsed = nowSeconds - sunriseSec
            ArcPhase(
                progress = (elapsed.toFloat() / span).coerceIn(0f, 1f),
                leftLabel = "الشروق",
                leftHour = sunrise,
                rightLabel = "المغرب",
                rightHour = maghrib,
                badge = "تم الغروب (${timeText(sunset)}) • ذهاب الحمرة المشرقية للمغرب"
            )
        }
    }
}
