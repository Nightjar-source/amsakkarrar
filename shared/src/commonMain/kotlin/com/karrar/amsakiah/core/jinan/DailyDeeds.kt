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
private val digits = mapOf(
    '٠' to '0', '١' to '1', '٢' to '2', '٣' to '3', '٤' to '4',
    '٥' to '5', '٦' to '6', '٧' to '7', '٨' to '8', '٩' to '9',
    '۰' to '0', '۱' to '1', '۲' to '2', '۳' to '3', '۴' to '4',
    '۵' to '5', '۶' to '6', '۷' to '7', '۸' to '8', '۹' to '9'
)

/** يطبّع النص للمقارنة: إزالة التشكيل والهمزات والمدد وتوحيد الألف/الياء والأرقام. */
private fun norm(s: String): String {
    val withLatinDigits = buildString(s.length) { s.forEach { append(digits[it] ?: it) } }
    return withLatinDigits
        .replace(tashkeel, "")
        .replace("آ", "ا").replace("أ", "ا").replace("إ", "ا")
        .replace("ى", "ي")
        .trim()
}

private fun containsAny(haystack: String, needles: List<String>): Boolean {
    val n = norm(haystack)
    return needles.any { n.contains(norm(it)) }
}

/** أسماء الأشهر الهجرية (variants النص في مفاتيح الجنان) بترتيب 1..12. */
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

/** صحائف الليلة/اليوم الهجري (الأولى…الثلاثون) — الصيغة المؤنثة والمذكرة. */
private fun dayOrdinals(day: Int): List<String> = when (day) {
    1 -> listOf("الاولى", "الأولى", "الأول", "الأوّل", "غرة", "غره", "مستهل")
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
    15 -> listOf("الخامسة عشرة", "الخامسة عشر", "الخامس عشر", "النصف", "نصف", "النصفي")
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
    29 -> listOf("التاسعة والعشرون", "التاسعة والعشرين", "التاسع والعشرون", "التاسع والعشرين", "الاخير", "اخير")
    30 -> listOf("الثلاثون", "الثلاثين", "الاخير", "اخير")
    else -> emptyList()
}

/** ترتيب الاختبار: الليلة أولًا (الوقت بعد المغرب) أم اليوم. */
private val weekDays = listOf("الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت", "الأحد")

/**
 * أعلام الأيام المسمّاة — كل علم مرتبط بتاريخه الهجري الثابت، فلا يظهر عملُ عرفة
 * في شعبان ولا عملُ الغدير في شوال. المفتاح = كلمات العنوان، القيمة = (الشهر، اليوم).
 */
private val specialEventDates: Map<String, Pair<Int, Int>> = mapOf(
    "تاسوعا" to (1 to 9), "تاسوعاء" to (1 to 9),
    "عاشوراء" to (1 to 10),
    "الاربعين" to (2 to 20), "الأربعين" to (2 to 20),
    "المبعث" to (7 to 27),
    "عرفة" to (12 to 9),
    "الفطر" to (10 to 1),
    "الاضحى" to (12 to 10), "الأضحى" to (12 to 10),
    "الغدير" to (12 to 18),
    "المباهلة" to (12 to 24),
    "دحو الارض" to (11 to 25), "دحو الأرض" to (11 to 25),
    "الاسراء" to (7 to 27),
    "المعراج" to (7 to 27),
    "المولد" to (3 to 12)
)

/**
 * التواريخ الهجرية الضمنية للعنوان — المطابقة الصارمة لـ«أعمال اليوم»:
 *
 * - اسم شهر + رقم (مكتوبـة أو هندية) ⇒ ذلك الشهر/اليوم فقط («الليلة الثالثة عشرة من رجب»).
 * - رقم بلا شهر ⇒ ذلك اليوم في كل الأشهر («الليلة الخامسة عشرة»).
 * - علم مسمّى (عرفة/عاشوراء/الفطر/الغدير/المبعث/الاسراء…) ⇒ يومه الثابت فقط.
 * - شهر بلا أي رقم ⇒ ليس «عمل اليوم» الموقوت، فلا تُطابق شيئًا.
 */
private val digitDay = Regex("\\d{1,2}")

private fun impliedHijriDates(title: String): Set<Pair<Int, Int>> {
    val t = norm(title)
    val months = (1..12).filter { m -> monthNeedles(m).any { t.contains(norm(it)) } }
    val days = buildList {
        (1..30).filter { d -> dayOrdinals(d).any { t.contains(norm(it)) } }.forEach { add(it) }
        digitDay.findAll(t).map { it.value.toInt() }.filter { it in 1..30 }.forEach { add(it) }
    }.distinct()
    val events = specialEventDates.filterKeys { t.contains(norm(it)) }.values.toSet()

    return when {
        months.isNotEmpty() -> {
            // «عمل عام من الشهر» بلا يوم محدّد ⇒ ليس «عمل اليوم»، فلا يطابق شيئًا.
            if (days.isEmpty()) return emptySet()
            months.flatMap { m -> days.map { m to it } }.toSet()
        }
        events.isNotEmpty() -> events + days.map { 0 to it }
        days.isNotEmpty() -> days.map { 0 to it }.toSet()
        else -> emptySet()
    }
}

/** هل يظهر العمل في التاريخ الهجري المعين (شهر، يوم)؟ */
private fun matchesHijriDate(title: String, monthNum: Int, day: Int): Boolean =
    impliedHijriDates(title).any { (m, d) -> d == day && (m == 0 || m == monthNum) }

/** أعمال يوم الأسبوع (اسم اليوم هو المطابقة). */
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
 * أعمال اليوم المطابقة لتاريخه الهجري **فقط** — لا أعمال رمضان ولا أيام من أشهر أخرى،
 * وكل أعمال الأشهر الموقوتة الموجودة في «مفاتيح الجنان» تُلتقط تلقائيًا:
 *
 * 1) **الليلة/اليوم** — أي عنوان في مفاتيح الجنان يُضمر تاريخًا هجريًا محددًا
 *    (مكتوبـة أو هندية) ويطابق تاريخ اليوم الهجري.
 * 2) **يوم الأسبوع** — اسم اليوم (الجمعة/السبت…).
 *
 * @param today التاريخ المدني الحالي (المنطقة الزمنية للتطبيق).
 * @param hijri التاريخ الهجري الحالي.
 */
fun dailyDeeds(today: LocalDate, hijri: HijriDate): List<DailyDeed> {
    val weekdayName = weekDays.getOrNull(today.dayOfWeek.ordinal).orEmpty()
    val out = linkedMapOf<Int, DailyDeed>()

    for (indexed in jinanIndexed) {
        // لا نفترض أن العنوان يحمل «ليلة/يوم» — أي عنوان يُضمر تاريخًا هجريًا يطابقه يُعرض.
        val imply = impliedHijriDates(indexed.item.title)
        if (imply.isEmpty()) continue
        if (!matchesHijriDate(indexed.item.title, hijri.monthNum, hijri.day)) continue
        val t = norm(indexed.item.title)
        val src = if (t.contains(norm("ليلة"))) DailyDeedSource.MONTH_NIGHT else DailyDeedSource.MONTH_DAY
        out.getOrPut(indexed.item.id) {
            DailyDeed(indexed.item, indexed.chapterTitle, indexed.sectionTitle, src)
        }
    }

    if (weekdayName.isNotBlank()) {
        for (indexed in jinanIndexed) {
            if (containsAny(indexed.item.title, listOf(weekdayName))) {
                out.getOrPut(indexed.item.id) {
                    DailyDeed(indexed.item, indexed.chapterTitle, indexed.sectionTitle, DailyDeedSource.WEEKDAY)
                }
            }
        }
    }

    return out.values.toList()
}
