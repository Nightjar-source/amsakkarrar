package com.karrar.amsakiah.core.astronomy

import kotlinx.serialization.Serializable

@Serializable
data class City(
    val country: String,
    val city: String,
    val lat: Double,
    val lon: Double,
    val tzOffset: Double
)

@Serializable
data class CalculationMethod(
    val id: Int,
    val code: String,
    val name: String,
    val fajr: Double,
    val maghribType: Int, // 0 = angle, 1 = minutes after sunset
    val maghribVal: Double,
    val ishaType: Int,    // 0 = angle, 1 = minutes after maghrib
    val ishaVal: Double,
    val isShia: Boolean
)

enum class HighLatitudeRule(val code: String, val titleAr: String) {
    NONE("none", "بدون تعديل (أوقات فلكية مباشرة)"),
    SAYEGH("sayegh", "قاعدة الصائغ الفقهية (منتصف الليل الفلكي)"),
    AQRAB_45("aqrab_45", "أقرب خط عرض 45° (المعيار المعتمد)"),
    AQRAB_48("aqrab_48", "أقرب خط عرض 48°"),
    SAB_UL_LAYL("sab_ul_layl", "سُبع الليل (1/7 من الليل)"),
    ANGLE_BASED("angle_based", "النسبة الزاوية إلى 60°"),
    FIXED_90("fixed_90", "زمن ثابت (90 دقيقة)"),
    NIGHT_MIDDLE("night_middle", "نصف الليل (50%)")
}

data class PrayerTimes(
    val imsak: Double,
    val fajr: Double,
    val sunrise: Double,
    val dhuhr: Double,
    val asr: Double,
    val sunset: Double,
    val maghrib: Double,
    val isha: Double,
    val midnight: Double,
    /** الإسفار — صلاة الفجر الفعلية وفق "تقويم الصائغ" في خطوط العرض العليا في
     *  الصيف الفلكي. يُملأ فقط في تلك الحالة (وإلا null). */
    val isfar: Double? = null,
    val isHighLatAnomaly: Boolean = false,
    val declination: String = "",
    val eqtMinutes: String = "",
    val dayLengthHours: String = "",
    val nightLengthHours: String = ""
)

data class HijriDate(
    val day: Int,
    val monthName: String,
    val year: Int,
    val monthNum: Int,
    val jd: Double = 0.0
)

data class QiblaAndMoon(
    val qiblaBearing: Double,
    val distanceKm: Long,
    val phaseName: String,
    val phaseSymbol: String,
    val illuminationPercent: Int,
    val normalizedPhase: Double,
    val moonAgeDays: String
)

data class PrayerDisplayItem(
    val id: String,
    val label: String,
    val rawHour: Double,
    val formattedTime: String,
    val amPm: String,
    val isNext: Boolean = false
)
