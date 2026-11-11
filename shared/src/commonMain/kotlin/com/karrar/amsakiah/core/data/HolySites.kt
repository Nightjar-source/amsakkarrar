package com.karrar.amsakiah.core.data

import com.karrar.amsakiah.core.astronomy.AstronomicalEngine
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.tan
import kotlin.math.atan2
import kotlin.math.PI

data class HolySite(
    val name: String,
    val city: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val emoji: String,
    val kind: SiteKind,
    /** سطر الأئمة/الأولياء المرتبطين بالمكان (يُعرض في بطاقة القبلة). */
    val imams: String = ""
)

enum class SiteKind(val title: String) {
    KAABA("الكعبة"),
    PROPHET("المسجد النبوي"),
    SHRINE("المراقد المقدسة"),
    MOSQUE("المساجد")
}

/** مراقد أهل البيت والأماكن المقدسة — إحداثيات ثابتة أوفلاين */
object HolySites {

    val all = listOf(
        HolySite("الكعبة المشرفة", "مكة المكرمة", "السعودية", 21.4225, 39.8262, "🕋", SiteKind.KAABA, "بيت الله الحرام — قبلة المسلمين"),
        HolySite("المسجد النبوي", "المدينة المنورة", "السعودية", 24.4672, 39.6112, "🕌", SiteKind.PROPHET, "النبي محمد (ص)"),
        HolySite("مرقد الإمام الحسين (ع)", "كربلاء", "العراق", 32.6164, 44.0322, "🕌", SiteKind.SHRINE, "الإمام الحسين (ع) وأصحابه"),
        HolySite("مرقد العباس (ع)", "كربلاء", "العراق", 32.6170, 44.0333, "🕌", SiteKind.SHRINE, "أبو الفضل العباس (ع)"),
        HolySite("مرقد الإمام علي (ع)", "النجف", "العراق", 32.0295, 44.3175, "🕌", SiteKind.SHRINE, "الإمام علي بن أبي طالب (ع)"),
        HolySite("الحضرة الكاظمية", "بغداد", "العراق", 33.3800, 44.3383, "🕌", SiteKind.SHRINE, "الإمامان الكاظم والجواد (ع)"),
        HolySite("الحضرة العسكرية", "سامراء", "العراق", 34.1989, 43.8733, "🕌", SiteKind.SHRINE, "الإمامان الهادي والعسكري (ع)"),
        HolySite("مرقد الإمام الرضا (ع)", "مشهد", "إيران", 36.2880, 59.6157, "🕌", SiteKind.SHRINE, "الإمام علي الرضا (ع)"),
        HolySite("مرقد السيدة المعصومة (ع)", "قم", "إيران", 34.6410, 50.8790, "🕌", SiteKind.SHRINE, "السيدة فاطمة المعصومة (ع)"),
        HolySite("مرقد السيدة زينب (ع)", "دمشق", "سوريا", 33.4444, 36.3392, "🕌", SiteKind.SHRINE, "السيدة زينب (ع)"),
        HolySite("مرقد السيدة رقية (ع)", "دمشق", "سوريا", 33.5100, 36.3080, "🕌", SiteKind.SHRINE, "السيدة رقية (ع)"),
        HolySite("مرقد السيدة نفيسة (ع)", "القاهرة", "مصر", 30.0240, 31.2520, "🕌", SiteKind.SHRINE, "السيدة نفيسة (ع)"),
        HolySite("مسجد الكوفة", "الكوفة", "العراق", 32.0290, 44.4000, "🕌", SiteKind.MOSQUE, "من المساجد الأربعة"),
        HolySite("مسجد السهلة", "الكوفة", "العراق", 32.0440, 44.3830, "🕌", SiteKind.MOSQUE, "مسجد السهلة المعظم"),
        HolySite("مسجد جمكران", "قم", "إيران", 34.5800, 50.9100, "🕌", SiteKind.MOSQUE, "مسجد جمكران"),
        HolySite("جنة البقيع", "المدينة المنورة", "السعودية", 24.4655, 39.6122, "🪦", SiteKind.MOSQUE, "أئمة وأولياء البقيع"),
        HolySite("مقبرة المعلاة", "مكة المكرمة", "السعودية", 21.4280, 39.8200, "🪦", SiteKind.MOSQUE, "مقبرة المعلاة")
    )

    // نستخدم ثابت المحرك الفلكي نفسه كي تبقى الحسابات موحدة، لا يُعاد تعريفها هنا.
    // private const val DEGREES_TO_RADIANS = PI / 180.0

    fun distanceKm(lat: Double, lon: Double, site: HolySite): Double =
        AstronomicalEngine.greatCircleDistanceKm(lat, lon, site.lat, site.lon)

    /** اتجاه الموقع من نقطة المستخدم (0 = شمال) */
    fun bearingFrom(lat: Double, lon: Double, site: HolySite): Double {
        val dLon = (site.lon - lon) * AstronomicalEngine.DEGREES_TO_RADIANS
        val lat1 = lat * AstronomicalEngine.DEGREES_TO_RADIANS
        val lat2 = site.lat * AstronomicalEngine.DEGREES_TO_RADIANS
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        val deg = atan2(y, x) * 180.0 / PI
        return (deg + 360.0) % 360.0
    }

    fun qiblaBearing(lat: Double, lon: Double): Double =
        AstronomicalEngine.calculateQiblaAndMoonPhase(lat, lon, AstronomicalEngine.calculateJulianDay(2026, 1, 1)).qiblaBearing

    fun qiblaDistance(lat: Double, lon: Double): Long =
        AstronomicalEngine.greatCircleDistanceKm(lat, lon, 21.4225, 39.8262).roundToLong()

    fun sortedByDistance(lat: Double, lon: Double): List<Pair<HolySite, Double>> =
        all.map { it to distanceKm(lat, lon, it) }.sortedBy { it.second }

    fun compassLabel(degrees: Double): String {
        val d = ((degrees % 360) + 360) % 360
        val dirs = listOf("شمال", "شمال شرق", "شرق", "جنوب شرق", "جنوب", "جنوب غرب", "غرب", "شمال غرب")
        val idx = ((d + 22.5) / 45.0).toInt() % 8
        return dirs[idx]
    }

    /**
     * لون مميّز لكل مرقد/مكان — يُستخدم في البوصلة وبطاقات المراقد،
     * كما طُلب: لكل مرقد لونه الخاص المرتبط بمكانه.
     */
    fun accentOf(site: HolySite): Long = when {
        site.kind == SiteKind.KAABA -> 0xFFFFD700
        site.kind == SiteKind.PROPHET -> 0xFF4CAF50
        site.name.contains("الحسين") -> 0xFFEF5350
        site.name.contains("العباس") -> 0xFF42A5F5
        site.name.contains("الإمام علي") -> 0xFF26C6DA
        site.name.contains("الكاظمية") -> 0xFFAB47BC
        site.name.contains("العسكرية") -> 0xFFFFB74D
        site.name.contains("الرضا") -> 0xFF26A69A
        site.name.contains("المعصومة") -> 0xFFF06292
        site.name.contains("زينب") -> 0xFFEC407A
        site.name.contains("رقية") -> 0xFF7E57C2
        site.name.contains("نفيسة") -> 0xFF00ACC1
        site.name.contains("الكوفة") -> 0xFF66BB6A
        site.name.contains("السهلة") -> 0xFF9CCC65
        site.name.contains("جمكران") -> 0xFF4DD0E1
        site.name.contains("البقيع") -> 0xFF90A4AE
        site.name.contains("المعلاة") -> 0xFFB0BEC5
        else -> 0xFF38BDF8
    }
}
