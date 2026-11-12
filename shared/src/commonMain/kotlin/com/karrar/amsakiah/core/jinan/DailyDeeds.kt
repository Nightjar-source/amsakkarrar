package com.karrar.amsakiah.core.jinan

import com.karrar.amsakiah.core.astronomy.HijriDate
import kotlinx.datetime.LocalDate

/** مصدر العمل المطابق. */
enum class DailyDeedSource { WEEKDAY, MONTH_NIGHT, MONTH_DAY }

/** عمل من مفاتيح الجنان + سياقه (الفصل/القسم). */
data class DailyDeed(
    val item: JinanItem,
    val chapterTitle: String?,
    val sectionTitle: String,
    val source: DailyDeedSource
)

/** عنصر مصنّف بفصله وقسمه — يُبنى مرة واحدة بالذاكرة. */
data class JinanIndexedItem(
    val item: JinanItem,
    val chapterTitle: String?,
    val sectionTitle: String
)

/** كل عناصر «مفاتيح الجنان» بسياقاتها. */
val jinanIndexed: List<JinanIndexedItem> by lazy {
    val out = mutableListOf<JinanIndexedItem>()
    for (part in JinanContentData.bookParts) {
        for (chapter in part.chapters) {
            for (section in chapter.sections) {
                for (item in section.items) {
                    out.add(JinanIndexedItem(item, chapter.title, section.title))
                }
            }
        }
    }
    out
}

private val tashkeel = Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED\\u0640]")

/** يطبّع النص للمقارنة: إزالة التشكيل والمدتان والهمزات. */
private fun norm(s: String): String =
    s.replace(tashkeel, "")
        .replace("آ", "ا").replace("أ", "ا").replace("إ", "ا")
        .replace("ى", "ي")
        .trim()

private fun containsAny(haystack: String, needles: List<String>): Boolean {
    val n = norm(haystack)
    return needles.any { n.contains(norm(it)) }
}

/** أسماء الأشهر الهجرية المقبولة للمطابقة (variants النص في مفاتيح الجنان). */
private fun monthNeedles(monthNum: Int): List<String> = when (monthNum) {
    1 -> listOf("محرم")
    2 -> listOf("صفر")
    3 -> listOf("ربيع الاول", "ربيع الأول")
    4 -> listOf("ربيع الاخر", "ربيع الآخر", "ربيع الثاني")
    5 -> listOf("جمادى الاولى", "جمادى الأولى")
    6 -> listOf("جمادى الاخرة", "جمادى الآخرة", "جمادى الثانية")
    7 -> listOf("رجب")
    8 -> listOf("شعبان")
    9 -> listOf("رمضان")
    10 -> listOf("شوال")
    11 -> listOf("ذو القعدة", "القعدة")
    12 -> listOf("ذو الحجة", "الحجة")
    else -> emptyList()
}

/** جذور الأشهر الهجرية الحتمية — تُستخدم لاستبعاد مطابقة شهر آخر. */
private val otherMonthRoots = mapOf(
    1 to listOf("محرم"), 2 to listOf("صفر"),
    3 to listOf("ربيع الاول"), 4 to listOf("ربيع الاخر", "ربيع الثاني"),
    7 to listOf("رجب"), 8 to listOf("شعبان"),
    9 to listOf("رمضان"), 10 to listOf("شوال")
)

/** صحائف الليلة/اليوم الهجري (الأولى…الثلاثون). */
private fun dayOrdinals(day: Int): List<String> = when (day) {
    1 -> listOf("الاولى", "الأولى")
    2 -> listOf("الثانية")
    3 -> listOf("الثالثة")
    4 -> listOf("الرابعة")
    5 -> listOf("الخامسة")
    6 -> listOf("السادسة")
    7 -> listOf("السابعة")
    8 -> listOf("الثامنة")
    9 -> listOf("التاسعة")
    10 -> listOf("العاشرة")
    11 -> listOf("الحادية عشرة", "الحادية عشر", "الحادي عشر")
    12 -> listOf("الثانية عشرة", "الثانية عشر")
    13 -> listOf("الثالثة عشرة", "الثالثة عشر")
    14 -> listOf("الرابعة عشرة", "الرابعة عشر")
    15 -> listOf("الخامسة عشرة", "الخامسة عشر")
    16 -> listOf("السادسة عشرة", "السادسة عشر")
    17 -> listOf("السابعة عشرة", "السابعة عشر")
    18 -> listOf("الثامنة عشرة", "الثامنة عشر")
    19 -> listOf("التاسعة عشرة", "التاسعة عشر")
    20 -> listOf("العشرون", "العشرين")
    21 -> listOf("الواحدة والعشرون", "الواحدة والعشرين", "الحادية والعشرون", "الحادية والعشرين")
    22 -> listOf("الثانية والعشرون", "الثانية والعشرين")
    23 -> listOf("الثالثة والعشرون", "الثالثة والعشرين")
    24 -> listOf("الرابعة والعشرون", "الرابعة والعشرين")
    25 -> listOf("الخامسة والعشرون", "الخامسة والعشرين")
    26 -> listOf("السادسة والعشرون", "السادسة والعشرين")
    27 -> listOf("السابعة والعشرون", "السابعة والعشرين")
    28 -> listOf("الثامنة والعشرون", "الثامنة والعشرين")
    29 -> listOf("التاسعة والعشرون", "التاسعة والعشرين")
    30 -> listOf("الثلاثون", "الثلاثين")
    else -> emptyList()
}

/** ترتيب الاختبار: الليلة أولًا (الوقت بعد المغرب) أم اليوم. */
private val weekDays = listOf("الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت", "الأحد")

/** أعمال يوم الأسبوع (بالاسم العربي كما في العناوين). */
fun weekdayDeeds(weekdayName: String): List<DailyDeed> {
    if (weekdayName.isBlank()) return emptyList()
    val out = linkedMapOf<Int, DailyDeed>()
    for (indexed in jinanIndexed) {
        if (containsAny(indexed.item.title, listOf(weekdayName))) {
            out[indexed.item.id] = DailyDeed(
                indexed.item,
                indexed.chapterTitle,
                indexed.sectionTitle,
                DailyDeedSource.WEEKDAY
            )
        }
    }
    return out.values.toList()
}

/**
 * أعمال ليلة/يوم هجري محدّد (رقم 1..30).
 *
 * @param monthNum إن مررت شهرًا هجريًا محددًا (مثلًا الشهر الحالي) تُستبعد عناوين
 *   الأشهر الأخرى كي تظهر أعمال هذا الشهر فقط — كما في «أعمال اليوم».
 *   إن تُرك `null` تُعرض نتائج كل الأشهر التي لم يذكر فيها العنوان اسم شهر.
 */
fun monthDayDeeds(day: Int, monthNum: Int? = null): List<DailyDeed> {
    val ordinals = dayOrdinals(day)
    if (ordinals.isEmpty()) return emptyList()
    val otherMonth = monthNum?.let { m ->
        otherMonthRoots.entries
            .filter { it.key != m }
            .flatMap { it.value }
            .map { norm(it) }
    }.orEmpty()
    val out = linkedMapOf<Int, DailyDeed>()
    for (indexed in jinanIndexed) {
        val t = norm(indexed.item.title)
        val hasOrdinal = ordinals.any { t.contains(norm(it)) }
        if (!hasOrdinal) continue
        val mentionsNight = t.contains(norm("ليلة")) || t.contains(norm("الليلة"))
        val mentionsDay = t.contains(norm("اليوم")) || t.contains(norm("يوم"))
        if (!mentionsNight && !mentionsDay) continue
        if (otherMonth.any { t.contains(it) }) continue
        val src = if (mentionsNight) DailyDeedSource.MONTH_NIGHT else DailyDeedSource.MONTH_DAY
        out.getOrPut(indexed.item.id) {
            DailyDeed(indexed.item, indexed.chapterTitle, indexed.sectionTitle, src)
        }
    }
    return out.values.toList()
}

/**
 * الأعمال اليومية المطابقة لتاريخ اليوم (وفجره الليلة الماضية) من «مفاتيح الجنان».
 *
 * المطابقة ثلاثية المسار:
 * 1) **أعمال يوم الأسبوع** — عناوين تتضمن اسم اليوم (الجمعة/السبت…).
 * 2) **أعمال هذه الليلة** — تحتوي «ليلة/الليلة» + رقم الليلة الهجرية (+ اسم الشهر عندما تذكره).
 * 3) **أعمال اليوم** — تحتوي «اليوم» + رقم اليوم الهجري (+ اسم الشهر عندما تذكره).
 *
 * @param today التاريخ المدني الحالي (المنطقة الزمنية للتطبيق).
 * @param hijri التاريخ الهجري الحالي.
 */
fun dailyDeeds(today: LocalDate, hijri: HijriDate): List<DailyDeed> {
    val weekdayName = weekDays.getOrNull(today.dayOfWeek.ordinal).orEmpty()
    val month = monthDayDeeds(hijri.day, hijri.monthNum)
    val week = weekdayDeeds(weekdayName)
    // ترتيب: ليلة ← يوم ← أسبوع
    return (month + week).sortedBy {
        when (it.source) {
            DailyDeedSource.MONTH_NIGHT -> 0
            DailyDeedSource.MONTH_DAY -> 1
            DailyDeedSource.WEEKDAY -> 2
        }
    }
}
