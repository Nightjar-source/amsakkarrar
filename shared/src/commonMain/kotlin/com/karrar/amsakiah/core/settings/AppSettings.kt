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
    /** مصدر الموقع: manual (اختيار يدوي) / gps (إحداثيات الجهاز) / ip (استشعار عبر IP) */
    val locationSource: String = "manual",

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
    /**
     * وضع إظهار الإمساك (سلوك V7):
     * - `ramadan_auto`: يظهر تلقائيًا في شهر رمضان فقط.
     * - `always`: يظهر دائمًا.
     * - `hidden`: مخفي دائمًا.
     */
    val imsakMode: String = "ramadan_auto",

    // ---------- الإشعارات ----------
    val adhanEnabled: Boolean = true,
    /** الأوقات التي يُرفع عندها الأذان (اختيار محدّد أو الكل) */
    val adhanPrayers: List<String> = listOf("fajr", "dhuhr", "asr", "maghrib", "isha"),
    val reminderEnabled: Boolean = true,
    /** الأوقات التي يُرسل قبلها تنبيه التذكير */
    val reminderPrayers: List<String> = listOf("fajr", "dhuhr", "asr", "maghrib", "isha"),
    val reminderMinutes: Int = 15,
    val liveNotification: Boolean = true,
    /** الأوقات التي يدور حولها العدّاد والإشعار الحي (اختيار محدّد أو الكل) */
    val livePrayers: List<String> = listOf("imsak", "fajr", "dhuhr", "asr", "maghrib", "isha", "midnight"),
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
    /** فهرس الذكر المختار من قائمة تسبيح الزهراء والمأثورات */
    val tasbihDhikrIndex: Int = 0,
    /** عدد الدورات المكتملة في الجلسة الحالية */
    val tasbihRound: Int = 0,
    /** إجمالي التسبيحات (كل الأوقات) */
    val tasbihTotal: Int = 0,
    /** تسبيحات اليوم — تُصفَّر تلقائيًا عند تغيّر التاريخ */
    val tasbihDayCount: Int = 0,
    val tasbihDayDate: String = "",
    val tasbihHaptics: Boolean = true,
    val tasbihSound: Boolean = true,
    /** الانتقال تلقائيًا إلى الذكر التالي ضمن تسبيح الزهراء (ع) */
    val tasbihAutoAdvance: Boolean = true,
    /** إظهار شاشة التهنئة عند إتمام الهدف */
    val tasbihCompletionOverlay: Boolean = true,

    val version: Int = 1
)

object SettingsDefaults {
    val instance = AppSettings()
}
