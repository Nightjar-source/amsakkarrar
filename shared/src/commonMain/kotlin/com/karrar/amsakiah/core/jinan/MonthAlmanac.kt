package com.karrar.amsakiah.core.jinan

import com.karrar.amsakiah.core.settings.AppSettings
import com.karrar.amsakiah.core.state.PrayerCalculator
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.math.roundToInt

/**
 * مُنقّب الشهر الهجري — يحوّل «مفاتيح الجنان» (أسماء العناوين) إلى تنقّبٍ فعلي:
 * يقدّر بداية الشهر بالفرق بالأيام ثم يقرأ 30 يومًا منه مرورًا بالمحرك الهجري،
 * فلا نخمّن أطوال الأشهر بل نتحقق منها بأداتنا نفسها.
 */
internal object HijriMonthProbe {

    /** أول يوم ميلادي من الشهر الهجري المطلوب — بالتقدير ثم بالتفتيش. */
    fun firstCivilDate(
        settings: AppSettings,
        targetYear: Int,
        targetMonth: Int,
        today: LocalDate
    ): LocalDate {
        val current = PrayerCalculator.hijriFor(settings, today)
        val estimate = (
            (targetYear - current.year) * 354.367 +
                (targetMonth - current.monthNum) * 29.53
            ).roundToInt()
        var probe = today.plus(DatePeriod(days = estimate - 40))
        repeat(90) {
            val h = PrayerCalculator.hijriFor(settings, probe)
            if (h.year == targetYear && h.monthNum == targetMonth && h.day == 1) return probe
            probe = probe.plus(DatePeriod(days = 1))
        }
        // احتياطي: أول الشهر الهجري الحالي
        return today.plus(DatePeriod(days = -(current.day - 1)))
    }

    /** كل أيام الشهر المطلوب بمواعيدها (29 أو 30 يومًا فعلًا). */
    fun monthDates(
        settings: AppSettings,
        targetYear: Int,
        targetMonth: Int,
        today: LocalDate
    ): List<Pair<Int, LocalDate>> {
        var date = firstCivilDate(settings, targetYear, targetMonth, today)
        val out = mutableListOf<Pair<Int, LocalDate>>()
        repeat(31) {
            val h = PrayerCalculator.hijriFor(settings, date)
            if (h.year != targetYear || h.monthNum != targetMonth) return out
            out += h.day to date
            date = date.plus(DatePeriod(days = 1))
        }
        return out
    }
}

/** يوم واحد في «تنقّب الشهر» — يوم هجري + تاريخه + أعماله الهجرية والأسبوعية. */
data class AlmanacDay(
    /** اليوم الهجري (1..30). */
    val hijriDay: Int,
    val date: LocalDate,
    val weekday: String,
    /** أعمال الليلة السابقة (أعمال «ليلة X» — تُعرض كـ«هذه الليلة»). */
    val night: List<DailyDeed>,
    /** أعمال اليوم الهجري + العلم المسمى بذلك اليوم (عرفة/الغدير…). */
    val day: List<DailyDeed>,
    /** أعمال عامة الشهر — تظهر في كل أيام شهرها. */
    val general: List<DailyDeed>,
    /** أعمال يوم الأسبوع الموافق لهذا اليوم. */
    val weekdayWorks: List<DailyDeed>
) {
    /** مجموع الأعمال — يظهر شارةً على مربّع اليوم في المنقصة. */
    val total: Int get() = night.size + day.size + general.size + weekdayWorks.size
}

/** «تنقّب شهر هجري» كامل — يُبنى مرة واحدة عند فتح الشاشة/تغيير الشهر. */
data class MonthAlmanac(
    val year: Int,
    val month: Int,
    val monthName: String,
    /** الأيام المدورية الفعلية (29 أو 30) — كل يوم بأعماله. */
    val days: List<AlmanacDay>,
    /** إجمالي أعمال الشهر (distinct) — يظهر على رقاقة الشهر في الشريط. */
    val totalWorks: Int
)

/**
 * يبني «منقصة الشهر»: الأيام الفعلية + أعمال كل يوم (هجرية + أسبوعية).
 * البناء على خيط خلفية — لا يُستدعى على الواجهة مباشرة.
 */
fun buildMonthAlmanac(
    settings: AppSettings,
    year: Int,
    month: Int,
    today: LocalDate
): MonthAlmanac {
    val dayPairs = HijriMonthProbe.monthDates(settings, year, month, today)
    val weekdayNames = PrayerCalculator.weekDays

    val days = dayPairs.map { (hijriDay, date) ->
        val weekday = weekdayNames.getOrNull(date.dayOfWeek.ordinal).orEmpty()
        // فهرس مسبق ⇒ جلب أعمال اليوم مسحٌ لدلو الشهر فقط بلا تطبيع نص.
        val deeds = DeedIndex.forHijriDate(month, hijriDay)
        val weekdayWorks = DeedIndex.weekday(weekday)

        AlmanacDay(
            hijriDay = hijriDay,
            date = date,
            weekday = weekday,
            night = deeds.filter { !it.monthWide && it.source == DailyDeedSource.MONTH_NIGHT },
            day = deeds.filter { !it.monthWide && it.source == DailyDeedSource.MONTH_DAY },
            general = deeds.filter { it.monthWide },
            weekdayWorks = weekdayWorks
        )
    }

    return MonthAlmanac(
        year = year,
        month = month,
        monthName = PrayerCalculator.hijriMonths.getOrElse(month - 1) { "الشهر" },
        days = days,
        totalWorks = DeedIndex.monthCount(month)
    )
}
