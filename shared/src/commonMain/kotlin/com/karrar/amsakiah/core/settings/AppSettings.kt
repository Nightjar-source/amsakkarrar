package com.karrar.amsakiah.core.settings

import kotlinx.serialization.Serializable

/**
 * إعدادات التطبيق المدمجة (V7 + الكفيل) — تُحفظ محليًا بالكامل أوفلاين.
 */
@Serializable
data class AppSettings(
    // ---------- الموقع ----------
    val cityName: String = "كربلاء",
    val countryName: String = "العراق",
    val latitude: Double = 32.6160,
    val longitude: Double = 44.0249,
    val timeZoneId: String = "Asia/Baghdad",
    val useGps: Boolean = false,

    // ---------- الحساب ----------
    val methodCode: String = "alkafeel",
    val asrMethod: String = "shafii",
    val highLatRule: String = "aqrab_48",
    val hijriOffset: Int = 0,

    /** تعديل يدوي بالدقائق لكل وقت (imsak, fajr, sunrise, dhuhr, asr, sunset, maghrib, isha, midnight) */
    val offsets: Map<String, Int> = emptyMap(),

    /** الأوقات الظاهرة في الشاشة الرئيسية والإمساكية */
    val visiblePrayers: List<String> = listOf(
        "imsak", "fajr", "sunrise", "dhuhr", "asr", "sunset", "maghrib", "isha", "midnight"
    ),

    // ---------- الإشعارات ----------
    val adhanEnabled: Boolean = true,
    val adhanPrayers: List<String> = listOf("fajr", "dhuhr", "asr", "maghrib", "isha"),
    val reminderEnabled: Boolean = true,
    val reminderMinutes: Int = 15,
    val liveNotification: Boolean = true,
    val vibration: Boolean = true,

    // ---------- المظهر ----------
    val darkTheme: Boolean = true,
    val fontScale: Float = 1f,

    // ---------- أخرى ----------
    val showWeather: Boolean = true,
    val weatherCityOverride: String = "",
    val onboarded: Boolean = false,

    // ---------- المسبحة ----------
    val tasbihCount: Int = 0,
    val tasbihTarget: Int = 33,

    val version: Int = 1
)

object SettingsDefaults {
    val instance = AppSettings()
}
