package com.karrar.amsakiah.core.jinan

import com.karrar.amsakiah.core.astronomy.HijriDate
import kotlinx.datetime.LocalDate

/** مصدر العمل المطابق. */
enum class DailyDeedSource { WEEKDAY, MONTH_NIGHT, MONTH_DAY, MONTH_GENERAL }

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
    3 -> listOf("ربيع الاول", "ربيع الأول", "ربيع الاولى", "ربيع الأولى")
    4 -> listOf("ربيع الاخر", "ربيع الآخر", "ربيع الثاني", "ربيع آخر")
    5 -> listOf("جمادى الاولى", "جمادى الأولى", "جمادى الاول", "جمادى الأول", "جمادي الاولى", "جمادي الأولى")
    6 -> listOf("جمادى الاخرة", "جمادى الآخرة", "جمادى الثانية", "جمادى الآخر", "جمادى الاخر", "جمادي الاخر", "جمادي الآخر", "جمادي الثاني", "جمادى الثاني")
    7 -> listOf("رجب")
    8 -> listOf("شعبان")
    9 -> listOf("رمضان")
    10 -> listOf("شوال")
    11 -> listOf("ذو القعدة", "ذي القعدة", "القعدة", "القعده")
    12 -> listOf("ذو الحجة", "ذي الحجة", "الحجة", "الحجه")
    else -> emptyList()
}

/** جذور الأشهر الهجرية الحتمية — تُستخدم لاستبعاد مطابقة شهر آخر. */
private val otherMonthRoots = mapOf(
    1 to listOf("محرم"), 2 to listOf("صفر"),
    3 to listOf("ربيع الاول"), 4 to listOf("ربيع الاخر", "ربيع الثاني"),
    7 to listOf("رجب"), 8 to listOf("شعبان"),
    9 to listOf("رمضان"), 10 to listOf("شوال")
)

/** صحائف الليلة/اليوم الهجري (الأولى…الثلاثون) — الصيغة المؤنثة والمذكرة. */
private fun dayOrdinals(day: Int): List<String> = when (day) {
    1 -> listOf("الاولى", "الأولى", "الأول", "الأوّل")
    2 -> listOf("الثانية", "الثاني")
    3 -> listOf("الثالثة", "الثالث")
    4 -> listOf("الرابعة", "الرابع")
    5 -> listOf("الخامسة", "الخامس")
    6 -> listOf("السادسة", "السادس")
    7 -> listOf("السابعة", "السابع")
    8 -> listOf("الثامنة", "الثامن")
    9 -> listOf("التاسعة", "التاسع")
    10 -> listOf("العاشرة", "العاشر")
    11 -> listOf("الحادية عشرة", "الحادية عشر", "الحادي عشر")
    12 -> listOf("الثانية عشرة", "الثانية عشر", "الثاني عشر", "ثاني عشر")
    13 -> listOf("الثالثة عشرة", "الثالثة عشر", "الثالث عشر")
    14 -> listOf("الرابعة عشرة", "الرابعة عشر", "الرابع عشر")
    15 -> listOf("الخامسة عشرة", "الخامسة عشر", "الخامس عشر", "النصف", "نصف")
    16 -> listOf("السادسة عشرة", "السادسة عشر", "السادس عشر")
    17 -> listOf("السابعة عشرة", "السابعة عشر", "السابع عشر")
    18 -> listOf("الثامنة عشرة", "الثامنة عشر", "الثامن عشر")
    19 -> listOf("التاسعة عشرة", "التاسعة عشر", "التاسع عشر")
    20 -> listOf("العشرون", "العشرين")
    21 -> listOf("الواحدة والعشرون", "الواحدة والعشرين", "الحادية والعشرون", "الحادية والعشرين", "الحادي والعشرون", "الحادي والعشرين")
    22 -> listOf("الثانية والعشرون", "الثانية والعشرين", "الثاني والعشرون", "الثاني والعشرين")
    23 -> listOf("الثالثة والعشرون", "الثالثة والعشرين", "الثالث والعشرون", "الثالث والعشرين")
    24 -> listOf("الرابعة والعشرون", "الرابعة والعشرين", "الرابع والعشرون", "الرابع والعشرين")
    25 -> listOf("الخامسة والعشرون", "الخامسة والعشرين", "الخامس والعشرون", "الخامس والعشرين")
    26 -> listOf("السادسة والعشرون", "السادسة والعشرين", "السادس والعشرون", "السادس والعشرين")
    27 -> listOf("السابعة والعشرون", "السابعة والعشرين", "السابع والعشرون", "السابع والعشرين")
    28 -> listOf("الثامنة والعشرون", "الثامنة والعشرين", "الثامن والعشرون", "الثامن والعشرين")
    29 -> listOf("التاسعة والعشرون", "التاسعة والعشرين", "التاسع والعشرون", "التاسع والعشرين")
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

/** أيام خاصة معنونة بأعلامها (بلا رقم صراحةً) — (الشهر، اليوم) ← كلمات العنوان في الجنان. */
private val specialDays: Map<Pair<Int, Int>, List<String>> = mapOf(
    (1 to 9) to listOf("تاسوعا", "تاسوعاء"),
    (1 to 10) to listOf("عاشوراء"),
    (2 to 20) to listOf("الاربعين", "الأربعين"),
    (7 to 27) to listOf("المبعث"),
    (9 to 9) to listOf("عرفة"),
    (10 to 10) to listOf("عيد الأضحى", "الأضحى"),
    (10 to 18) to listOf("الغدير"),
    (5 to 13) to listOf("الثالث عشر، والرابع عشر"),
    (5 to 14) to listOf("الثالث عشر، والرابع عشر"),
)

/** أعمال عامة للشهر الهجري — لا تحمل رقم ليلة/يوم صراحةً (كل أيام الشهر). */
fun monthDeeds(monthNum: Int): List<DailyDeed> {
    val needles = monthNeedles(monthNum).map { norm(it) }
    if (needles.isEmpty()) return emptyList()
    val otherMonth = otherMonthRoots.entries
        .filter { it.key != monthNum }
        .flatMap { it.value }
        .map { norm(it) }
    val out = linkedMapOf<Int, DailyDeed>()
    for (indexed in jinanIndexed) {
        val t = norm(indexed.item.title)
        if (otherMonth.any { t.contains(it) }) continue
        if (needles.any { t.contains(it) }) {
            out.getOrPut(indexed.item.id) {
                DailyDeed(
                    indexed.item,
                    indexed.chapterTitle,
                    indexed.sectionTitle,
                    DailyDeedSource.MONTH_GENERAL
                )
            }
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
    val markers = monthNum?.let { specialDays[it to day].orEmpty() }
        ?: specialDays.entries.filter { it.key.second == day }.flatMap { it.value }
    val otherMonth = monthNum?.let { m ->
        otherMonthRoots.entries
            .filter { it.key != m }
            .flatMap { it.value }
            .map { norm(it) }
    }.orEmpty()
    val out = linkedMapOf<Int, DailyDeed>()
    for (indexed in jinanIndexed) {
        val t = norm(indexed.item.title)
        val markerHit = markers.any { t.contains(norm(it)) }
        val hasOrdinal = ordinals.any { t.contains(norm(it)) }
        if (!hasOrdinal && !markerHit) continue
        val mentionsNight = t.contains(norm("ليلة")) || t.contains(norm("الليلة"))
        val mentionsDay = t.contains(norm("اليوم")) || t.contains(norm("يوم"))
        if (!mentionsNight && !mentionsDay && !markerHit) continue
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
 * المطابقة رباعية المسار:
 * 1) **أعمال يوم الأسبوع** — عناوين تتضمن اسم اليوم (الجمعة/السبت…).
 * 2) **أعمال هذه الليلة** — تحتوي «ليلة/الليلة» + رقم الليلة الهجرية (+ اسم الشهر عندما تذكره).
 * 3) **أعمال اليوم** — تحتوي «اليوم» + رقم اليوم الهجري (+ اسم الشهر عندما تذكره).
 * 4) **أعمال الشهر العامة** — تحتوي اسم الشهر بلا رقم صراحةً (تسري على كل أيام الشهر).
 *
 * @param today التاريخ المدني الحالي (المنطقة الزمنية للتطبيق).
 * @param hijri التاريخ الهجري الحالي.
 */
fun dailyDeeds(today: LocalDate, hijri: HijriDate): List<DailyDeed> {
    val weekdayName = weekDays.getOrNull(today.dayOfWeek.ordinal).orEmpty()
    val month = monthDayDeeds(hijri.day, hijri.monthNum)
    val week = weekdayDeeds(weekdayName)
    val general = monthDeeds(hijri.monthNum)
    // ترتيب: ليلة ← يوم ← أسبوع ← أعمال الشهر العامة
    return (month + week + general).sortedBy {
        when (it.source) {
            DailyDeedSource.MONTH_NIGHT -> 0
            DailyDeedSource.MONTH_DAY -> 1
            DailyDeedSource.WEEKDAY -> 2
            DailyDeedSource.MONTH_GENERAL -> 3
        }
    }
}
