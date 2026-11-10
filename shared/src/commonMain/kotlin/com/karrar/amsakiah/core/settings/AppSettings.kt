package com.karrar.amsakiah.core.settings

import kotlinx.serialization.Serializable
import com.karrar.amsakiah.core.data.TasbihDhikr

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
    /**
     * التوقيت الصيفي:
     * - `auto`: قاعدة المنطقة لكل تاريخ (الافتراضي، يعمل في كل مدن العالم).
     * - `summer`: إجبار إزاحة الصيف (DST) طوال السنة.
     * - `winter`: إجبار التوقيت الشتوي/القياسي طوال السنة.
     */
    val dstMode: String = "auto",
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

    // ---------- الإشعارات (إشعاران فقط، وكلاهما اختياري) ----------
    /** بطاقة الأذان عند دخول الوقت — اختيارية. */
    val adhanEnabled: Boolean = true,
    /** الأوقات التي يُرفع عندها الأذان (اختيار محدّد أو الكل) */
    val adhanPrayers: List<String> = listOf("fajr", "dhuhr", "asr", "maghrib", "isha"),
    /** الإشعار الحي للعدّاد التنازلي بشريط التقدم — اختياري. */
    val liveNotification: Boolean = true,
    /** الأوقات التي يدور حولها العدّاد والإشعار الحي (اختيار محدّد أو الكل) */
    val livePrayers: List<String> = listOf("imsak", "fajr", "dhuhr", "asr", "maghrib", "isha", "midnight"),
    val vibration: Boolean = true,

    // ---------- المظهر ----------
    /**
     * وضع المظهر: `system` (تلقائي حسب النظام) / `light` / `dark`.
     * يُقرأ في `App.kt` → `KararTheme(mode)` فيتبدّل التطبيق كاملًا فورًا.
     */
    val themeMode: String = ThemeMode.SYSTEM,
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
    /** أذكار يضيفها المستخدم بنفسه (تسبيح مفرد أو ضمن مجموعة) */
    val customDhikrs: List<TasbihDhikr> = emptyList(),

    // ---------- أعمال اليوم (شاشة «المفاتيح» — تذكّر/ملاحظات) ----------
    /** معرفات الأعمال اليومية التي أكملت في اليوم الحالي (تُصفر مع تغيّر اليوم) */
    val todayTasksDone: List<String> = emptyList(),
    /** تاريخ اليوم الذي تُحسب له مهام `todayTasksDone` (ISO yyyy-MM-dd) */
    val todayTasksDate: String = "",
    /** ملاحظات حرة يضيفها المستخدم (الأحدث أولًا) */
    val notes: List<String> = emptyList(),

    val version: Int = 1
)

object SettingsDefaults {
    val instance = AppSettings()
}

/**
 * أوضاع المظهر الثلاثة — قيم نصية لتبقى قابلة للحفظ والترحيل.
 *
 * `SYSTEM`: يتبع النظام لحظيًا، `LIGHT`: فاتح دائمًا، `DARK`: مظلم دائمًا.
 */
object ThemeMode {
    const val SYSTEM = "system"
    const val LIGHT = "light"
    const val DARK = "dark"

    val all: List<String> = listOf(SYSTEM, LIGHT, DARK)

    fun label(mode: String): String = when (mode) {
        LIGHT -> "فاتح"
        DARK -> "مظلم"
        else -> "تلقائي"
    }
}
