package com.karrar.amsakiah.core.state

import com.karrar.amsakiah.core.astronomy.AstronomicalEngine
import com.karrar.amsakiah.core.astronomy.CalculationMethod
import com.karrar.amsakiah.core.astronomy.HighLatitudeRule
import com.karrar.amsakiah.core.astronomy.HijriDate
import com.karrar.amsakiah.core.astronomy.OfflineCities
import com.karrar.amsakiah.core.astronomy.PrayerTimes
import com.karrar.amsakiah.core.settings.AppSettings
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.floor
import kotlin.math.roundToInt

/** ترتيب الأوقات المستخدم في كل الشاشات */
object PrayerMeta {
    val order = listOf("imsak", "fajr", "isfar", "sunrise", "dhuhr", "asr", "sunset", "maghrib", "isha", "midnight")

    val labels = mapOf(
        "imsak" to "الإمساك",
        "fajr" to "الفجر",
        "isfar" to "الإسفار",
        "sunrise" to "الشروق",
        "dhuhr" to "الظهر",
        "asr" to "العصر",
        "sunset" to "الغروب",
        "maghrib" to "المغرب",
        "isha" to "العشاء",
        "midnight" to "منتصف الليل"
    )

    fun label(id: String): String = labels[id] ?: id
    fun fixed(id: String): Boolean = id == "imsak" || id == "fajr" || id == "sunrise" ||
        id == "dhuhr" || id == "sunset" || id == "maghrib" || id == "isha" || id == "midnight"
        // isfar وقت خاص يظهر فقط عند خطوط العرض العليا — ليست صلاة بذاتها للمهام
        || id == "isfar"
}

data class PrayerSlot(
    val id: String,
    val label: String,
    val rawHour: Double,
    val time12: String,
    val amPm: String,
    val time24: String,
    val isNext: Boolean = false,
    val isTomorrow: Boolean = false
)

object PrayerCalculator {

    fun methodFor(code: String): CalculationMethod =
        OfflineCities.methodsMatrix.firstOrNull { it.code == code } ?: OfflineCities.getDefaultMethod()

    fun highLatFor(code: String): HighLatitudeRule =
        HighLatitudeRule.entries.firstOrNull { it.code == code } ?: HighLatitudeRule.AQRAB_48

    fun zoneOf(settings: AppSettings): TimeZone =
        runCatching { TimeZone.of(settings.timeZoneId) }.getOrDefault(TimeZone.currentSystemDefault())

    /** إزاحة المنطقة عن UTC بالساعات في منتصف نهار تاريخ محدد. */
    private fun offsetAtNoon(zone: TimeZone, date: LocalDate): Double =
        zone.offsetAt(
            LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, 12, 0)
                .toInstant(zone)
        ).totalSeconds / 3600.0

    /**
     * أكبر/أصغر إزاحة خلال السنة — تُستخدم لوضعي «صيفي» و«شتوي» الإجباريين.
     * أخذ عيّنة من منتصف كل شهر يجعلها صحيحة في نصفي الكرة الأرضية وفي
     * المناطق التي تبدأ DST في تواريخ مختلفة (مثل المغرب).
     */
    private fun extremeOffset(zone: TimeZone, year: Int, max: Boolean): Double {
        var best = offsetAtNoon(zone, LocalDate(year, 1, 15))
        for (month in 2..12) {
            val value = offsetAtNoon(zone, LocalDate(year, month, 15))
            best = if (max) maxOf(best, value) else minOf(best, value)
        }
        return best
    }

    /**
     * إزاحة المنطقة الزمنية للتاريخ المطلوب — لا للحظة الحالية.
     *
     * لماذا؟ لو حُسبت مرة واحدة بـ`Clock.System.now()` ثم طُبّقت على كل
     * تواريخ التقويم، تصبح أوقات الأيام الواقعة على الجانب الآخر من تغيير
     * التوقيت الصيفي خاطئة بساعة كاملة. نحسب هنا إزاحة منتصف نهار `date`.
     */
    fun offsetHours(settings: AppSettings, date: LocalDate): Double {
        val zone = zoneOf(settings)
        return when (settings.dstMode) {
            "summer" -> extremeOffset(zone, date.year, max = true)
            "winter" -> extremeOffset(zone, date.year, max = false)
            else -> offsetAtNoon(zone, date)
        }
    }

    fun offsetHours(settings: AppSettings): Double = offsetHours(settings, todayDate(settings))

    fun timesFor(settings: AppSettings, date: LocalDate): PrayerTimes =
        AstronomicalEngine.calculatePrayerTimes(
            year = date.year,
            month = date.monthNumber,
            day = date.dayOfMonth,
            lat = settings.latitude,
            lon = settings.longitude,
            tzOffset = offsetHours(settings, date),
            method = methodFor(settings.methodCode),
            highLatRule = highLatFor(settings.highLatRule),
            asrMethod = settings.asrMethod
        )

    fun hijriFor(settings: AppSettings, date: LocalDate): HijriDate =
        AstronomicalEngine.calculateHijriDate(
            date.year, date.monthNumber, date.dayOfMonth,
            isShiaMode = methodFor(settings.methodCode).isShia,
            adjustmentDays = settings.hijriOffset
        )

    /** خريطة الأوقات بعد تطبيق التعديلات اليدوية */
    fun adjustedTimes(times: PrayerTimes, settings: AppSettings): Map<String, Double> {
        val base = mutableMapOf(
            "imsak" to times.imsak,
            "fajr" to times.fajr,
            "sunrise" to times.sunrise,
            "dhuhr" to times.dhuhr,
            "asr" to times.asr,
            "sunset" to times.sunset,
            "maghrib" to times.maghrib,
            "isha" to times.isha,
            "midnight" to times.midnight
        )
        // الإسفار يضاف فقط عندما يخلصّه المحرك الفلكي (شروق خاص بخطوط العرض العليا).
        times.isfar?.let { base["isfar"] = it }
        return base.mapValues { (id, hour) ->
            val offset = settings.offsets[id] ?: 0
            var value = hour + offset / 60.0
            if (value < 0) value += 24.0
            if (value >= 24) value -= 24.0
            value
        }
    }

    fun formatHour(rawHour: Double): Triple<String, String, String> =
        AstronomicalEngine.formatHourOfDay(rawHour)

    fun formatHour(rawHour: Double, use24Hour: Boolean): Triple<String, String, String> =
        AstronomicalEngine.formatHourOfDay(rawHour, use24Hour)

    /** هل الإمساك ظاهر في هذا التاريخ وفق وضع العرض المختار؟ */
    fun imsakVisible(settings: AppSettings, date: LocalDate = todayDate(settings)): Boolean =
        when (settings.imsakMode) {
            "always" -> true
            "hidden" -> false
            // السلوك الافتراضي (V7): رمضان فقط
            else -> hijriFor(settings, date).monthNum == 9
        }

    /**
     * الأوقات الظاهرة فقط حسب الإعدادات، مع تطبيق قواعد المذهب:
     * - «الغروب» يظهر في المعايير الشيعية فقط.
     * - «الإمساك» يتبع `imsakMode` (رمضان تلقائيًا / دائمًا / مخفي).
     */
    fun visibleIds(settings: AppSettings, date: LocalDate = todayDate(settings)): List<String> {
        val isShia = methodFor(settings.methodCode).isShia
        // الإسفار لا يظهر إلا عندما يستخرجه المحرك (خطوط العرض العليا صيفًا)
        val showsIsfar = AstronomicalEngine.calculatePrayerTimes(
            year = date.year,
            month = date.monthNumber,
            day = date.dayOfMonth,
            lat = settings.latitude,
            lon = settings.longitude,
            tzOffset = offsetHours(settings, date),
            method = methodFor(settings.methodCode),
            highLatRule = highLatFor(settings.highLatRule),
            asrMethod = settings.asrMethod
        ).isfar != null
        return PrayerMeta.order.filter { id ->
            id in settings.visiblePrayers &&
                (id != "sunset" || isShia) &&
                (id != "imsak" || imsakVisible(settings, date)) &&
                (id != "isfar" || showsIsfar)
        }
    }

    /** الأوقات التي يدور حولها العدّاد والإشعار الحي = الظاهرة ∩ المختارة للعد. */
    fun liveIds(settings: AppSettings, date: LocalDate = todayDate(settings)): List<String> =
        visibleIds(settings, date).filter { it in settings.livePrayers }

    fun slots(settings: AppSettings, date: LocalDate, nowSeconds: Int): List<PrayerSlot> {
        val times = timesFor(settings, date)
        val adjusted = adjustedTimes(times, settings)
        val ids = visibleIds(settings, date)
        val nextId = nextPrayerId(settings, date, nowSeconds)?.first
        return ids.map { id ->
            val raw = adjusted[id] ?: 0.0
            val (t12, ap, t24) = formatHour(raw)
            PrayerSlot(id, PrayerMeta.label(id), raw, t12, ap, t24, id == nextId)
        }
    }

    /** يعيد (معرّف الصلاة القادمة، الوقت بالثواني من منتصف الليل، هل هي غدًا) */
    fun nextPrayerId(settings: AppSettings, date: LocalDate, nowSeconds: Int): Pair<String, Int>? {
        val ids = visibleIds(settings, date).filter { it != "sunrise" && it != "sunset" && it != "isfar" }
        val today = adjustedTimes(timesFor(settings, date), settings)
        for (id in ids) {
            val raw = today[id] ?: continue
            val sec = (raw * 3600).roundToInt()
            if (sec > nowSeconds) return id to sec
        }
        val tomorrow = date.plus(DatePeriod(days = 1))
        val nextDay = adjustedTimes(timesFor(settings, tomorrow), settings)
        val firstId = ids.firstOrNull() ?: "fajr"
        val raw = nextDay[firstId] ?: nextDay["fajr"] ?: 0.0
        return firstId to (raw * 3600).roundToInt()
    }

    fun secondsOfDay(now: kotlinx.datetime.LocalDateTime): Int =
        now.hour * 3600 + now.minute * 60 + now.second

    fun nowLocal(settings: AppSettings): kotlinx.datetime.LocalDateTime =
        Clock.System.now().toLocalDateTime(zoneOf(settings))

    fun todayDate(settings: AppSettings): LocalDate = nowLocal(settings).date

    // ---------- تنسيقات عربية ----------

    val hijriMonths = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
        "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    val gregorianMonths = listOf(
        "كانون الثاني", "شباط", "آذار", "نيسان", "أيار", "حزيران",
        "تموز", "آب", "أيلول", "تشرين الأول", "تشرين الثاني", "كانون الأول"
    )

    val weekDays = listOf("الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت", "الأحد")

    fun formattedGregorian(date: LocalDate): String {
        val dayName = weekDays[date.dayOfWeek.ordinal]
        return "$dayName ${date.dayOfMonth} - ${date.monthNumber} - ${date.year} م"
    }

    fun hijriText(hijri: HijriDate): String = "${hijri.day} ${hijri.monthName} ${hijri.year} هـ"

    fun countdownText(totalSeconds: Int): Triple<String, String, String> {
        val s = totalSeconds.coerceAtLeast(0)
        val h = s / 3600
        val m = (s % 3600) / 60
        val sec = s % 60
        return Triple(
            h.toString().padStart(2, '0'),
            m.toString().padStart(2, '0'),
            sec.toString().padStart(2, '0')
        )
    }

    fun floorToMinute(hour: Double): String {
        val total = floor(hour * 60).toInt()
        return "${(total / 60).toString().padStart(2, '0')}:${(total % 60).toString().padStart(2, '0')}"
    }
}
