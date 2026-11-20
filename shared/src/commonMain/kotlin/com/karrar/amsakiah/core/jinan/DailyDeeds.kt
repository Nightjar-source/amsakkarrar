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
    val source: DailyDeedSource,
    /**
     * عمل «عام للشهر» بلا يوم محدّد — يظهر في كل أيام شهره فقط
     * («أعمال شهر رمضان العامة»). يُجمَّع تحت «عامة <الشهر>» لا تحت «اليوم».
     */
    val monthWide: Boolean = false
)

/** عنصر مصنّف بفصله وقسمه وسياق سره الشهري — يُبنى مرة واحدة بالذاكرة. */
data class JinanIndexedItem(
    val item: JinanItem,
    val chapterTitle: String?,
    val sectionTitle: String,
    /** شهر القسم إن كان قسمًا من باب «أعمال أشهر السنة» — سياق مساعد لإسناد الأعمال اليتيمة. */
    val contextMonth: Int? = null
)

/**
 * كل عناصر «مفاتيح الجنان» بسياقاتها — بما في ذلك **شهر القسم**:
 * «مفاتيح الجنان» يضع أعمال كل شهر داخل قسم باسم ذلك الشهر، فأي عنوانٍ في
 * قسم «شهر شعبان» بلا اسم شهر صريح هو من «شعبان» لا من أي شهر آخر.
 */
val jinanIndexed: List<JinanIndexedItem> by lazy {
    val out = mutableListOf<JinanIndexedItem>()
    for (part in JinanContentData.bookParts) {
        for (chapter in part.chapters) {
            for (section in chapter.sections) {
                val context = monthContextOfSection(section.title)
                for (item in section.items) {
                    out.add(JinanIndexedItem(item, chapter.title, section.title, context))
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

/** أسماء الأشهر الهجرية (variants النص في مفاتيح الجنان) بترتيب 1..12. */
private fun monthNeedles(monthNum: Int): List<String> = when (monthNum) {
    1 -> listOf("محرم", "المحرم")
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

/**
 * يستنتج «شهر القسم» من عنوانه — مفيد لأقسام «أعمال أشهر السنة»:
 * «شهر رجب» و«شهر ذي الحجة» و«عامة الشهور».
 */
private fun monthContextOfSection(sectionTitle: String): Int? {
    val t = norm(sectionTitle)
    for (m in 1..12) {
        if (monthNeedles(m).any { t.contains(norm(it)) }) return m
    }
    return null
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
    29 -> listOf("التاسعة والعشرون", "التاسعة والعشرين", "التاسع والعشرون", "التاسع والعشرين")
    30 -> listOf("الثلاثون", "الثلاثين")
    else -> emptyList()
}

/** ترتيب الاختبار: الليلة أولًا (الوقت بعد المغرب) أم اليوم. */
private val weekDays = listOf("الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت", "الأحد")

/**
 * اليوم السابق في دورة الأسبوع — لحساب الليالي الشرعية (الليلة تسبق النهار):
 * ليلة الجمعة تبدأ عصر/مغرب الخميس، ليلة السبت تبدأ عصر/مغرب الجمعة، وهكذا.
 */
private fun previousWeekday(day: String): String = when (norm(day)) {
    norm("الاثنين") -> "الأحد"
    norm("الثلاثاء") -> "الاثنين"
    norm("الأربعاء") -> "الثلاثاء"
    norm("الخميس") -> "الأربعاء"
    norm("الجمعة") -> "الخميس"
    norm("السبت") -> "الجمعة"
    norm("الأحد") -> "السبت"
    else -> day
}

/**
 * أعلام الأيام المسمّاة — كل علم مرتبط بتاريخه الهجري الثابت، فلا يظهر عملُ عرفة
 * في شعبان ولا عملُ الغدير في شوال. المفتاح = كلمات العنوان، القيمة = (الشهر، اليوم).
 */
private val specialEventDates: Map<String, Pair<Int, Int>> = mapOf(
    "تاسوعا" to (1 to 9), "تاسوعاء" to (1 to 9),
    "عاشوراء" to (1 to 10),
    "الاربعين" to (2 to 20), "الأربعين" to (2 to 20),
    "المبعث" to (7 to 27),
    "التروية" to (12 to 8), "تروية" to (12 to 8),
    "عرفة" to (12 to 9),
    "الفطر" to (10 to 1),
    "الاضحى" to (12 to 10), "الأضحى" to (12 to 10),
    "الغدير" to (12 to 18),
    "المباهلة" to (12 to 24),
    "دحو الارض" to (11 to 25), "دحو الأرض" to (11 to 25),
    "الاسراء" to (7 to 27),
    "المعراج" to (7 to 27),
    "المولد" to (3 to 12), "ميلاد النبي" to (3 to 17)
)

/** يوم 0 = «عامة الشهر» — كل أيام الشهر المسمى. يوم 99 = «اليوم الأخير من الشهر». */
private const val GENERAL_MONTH = 0
private const val LAST_DAY = 99

private val digitDay = Regex("\\d{1,2}")

/** «اليوم الأخير» / «الليلة الأخيرة» في النص — تُطابق نهائي الشهر سواء كان 29 أو 30. */
private val lastDayNeedles = listOf("الاخير", "اخير", "الاخيرة")

/**
 * يستنتج كل «التواريخ الموعودة» من العنوان مع **سياق قسم** من «مفاتيح الجنان»:
 *
 * - اسم شهر + يوم (مكتوبـة أو هندية، أو «النصف»/«الأخير») ⇒ شهر × يوم فقط.
 * - يوم فقط بلا شهر ⇒ **شهر القسم** (من باب «أعمال أشهر السنة») إن وُجد؛
 *   وإلا ليس «عمل اليوم» — فلا يتسرّب إلى شهر آخر (كانت تتسرب أعمال كل الأشهر).
 * - شهر فقط بلا يوم ⇒ «عامة الشهر» — تُعرض في كل أيام ذلك الشهر فقط.
 * - علم مسمّى (عرفة/عاشوراء/الغدير…) ⇒ يومه الثابت في الشهر الثابت؛
 *   ولو ذُكر معه شهر في العنوان يُطبَّق اليوم المسّمى في ذلك الشهر.
 */
private fun impliedHijriDates(title: String, contextMonth: Int?): Set<Pair<Int, Int>> {
    val t = norm(title)
    val titleMonths = (1..12).filter { m -> monthNeedles(m).any { t.contains(norm(it)) } }
    val days = buildList {
        (1..30).filter { d -> dayOrdinals(d).any { t.contains(norm(it)) } }.forEach { add(it) }
        digitDay.findAll(t).map { it.value.toInt() }.filter { it in 1..30 }.forEach { add(it) }
        if (lastDayNeedles.any { t.contains(norm(it)) }) add(LAST_DAY)
    }.distinct()
    val events = specialEventDates.filterKeys { t.contains(norm(it)) }.values.toSet()

    val result = linkedSetOf<Pair<Int, Int>>()

    // 1) الأعلام المسمّاة — يومها الثابت، ضمن شهرها أو شهر العنوان/القسم.
    for ((eMonth, eDay) in events) {
        if (titleMonths.isNotEmpty()) {
            titleMonths.forEach { m -> result.add(m to eDay) }
        } else {
            result.add(eMonth to eDay)
        }
        // «ليلة المبعث» في قسم رجب — ربطها بالقسم أيضًا ما دام لا شهر صريح.
        if (contextMonth != null) result.add(contextMonth to eDay)
    }

    // 2) الشهر (من العنوان أو من القسم) + اليوم
    val months = if (titleMonths.isNotEmpty()) titleMonths else listOfNotNull(contextMonth)
    for (m in months) {
        if (days.isEmpty()) {
            // «عامة الشهر» — كل أيام شهرها فقط.
            result.add(m to GENERAL_MONTH)
        } else {
            for (d in days) result.add(m to d)
        }
    }

    // 3) علم مسمّى فقط بلا أي شهر ولا قسم
    if (result.isEmpty()) result.addAll(events)

    // 4) أعمال روتينية شهرية عامة: أول كل شهر (يوم 1)، والأيام والليالي البيض (13، 14، 15)
    if (t.contains("البيض") && titleMonths.isEmpty() && contextMonth == null) {
        for (m in 1..12) {
            result.add(m to 13)
            result.add(m to 14)
            result.add(m to 15)
        }
    }
    if ((t.contains("اول كل شهر") || t.contains("غرة كل شهر") || (t.contains("اول الشهر") && contextMonth == null)) && titleMonths.isEmpty()) {
        for (m in 1..12) {
            result.add(m to 1)
        }
    }

    return result
}

/**
 * فهرس «مفاتيح الجنان» المسبق — يُبنى مرة واحدة على خيط خلفية.
 *
 * لماذا؟ كانت `dailyDeeds` تعيد اشتقاق التواريخ الضمنية لكل عنوان عند كل
 * يوم، و`buildMonthAlmanac` يستدعيها 30 مرة ⇒ 30 × آلاف العناوين × عشرات
 * عمليات تطبيع النص = تجمّد «جارٍ فحص مفاتيح الجنان». الآن يُحسب التاريخ
 * الضمني لكل عنوان **مرة واحدة** ويُخزَّن في دلو حسب الشهر، فيصبح جلب أعمال
 * يوم بعينه مسحًا لقائمة شهر واحد بلا أي تطبيع نص.
 */
object DeedIndex {

    private class Entry(val deed: DailyDeed, val dates: Set<Pair<Int, Int>>)

    /** كل عنوان مرتبط بشهر، في ترتيب `jinanIndexed` نفسه (يحفظ ترتيب العرض). */
    private val byMonth: Map<Int, List<Entry>> by lazy {
        val out = HashMap<Int, MutableList<Entry>>(16)
        for (indexed in jinanIndexed) {
            val dates = impliedHijriDates(indexed.item.title, indexed.contextMonth)
            if (dates.isEmpty()) continue
            val source = if (norm(indexed.item.title).contains("ليلة")) {
                DailyDeedSource.MONTH_NIGHT
            } else {
                DailyDeedSource.MONTH_DAY
            }
            val wide = dates.all { (_, d) -> d == GENERAL_MONTH }
            val entry = Entry(
                DailyDeed(
                    indexed.item,
                    indexed.chapterTitle,
                    indexed.sectionTitle,
                    source,
                    monthWide = wide
                ),
                dates
            )
            // الشهر صريح دائمًا (GENERAL_MONTH هو يوم 0 لا شهر 0).
            for (month in dates.mapTo(HashSet()) { it.first }) {
                out.getOrPut(month) { mutableListOf() }.add(entry)
            }
        }
        out
    }

    /**
     * دلاء أيام الأسبوع السبعة — تطبيع عنوان كل عنصر مرة واحدة.
     *
     * مراعاة التوقيت الإسلامي الشرعي:
     * الليلة تسبق النهار؛ فأعمال «ليلة الجمعة» (مثل دعاء كميل وأعمال ليلة الجمعة وفضلها)
     * تبدأ شرعاً واستحباباً من عصر/مغرب يوم الخميس؛ لذا تُدرج أعمال ليلة X في دلو اليوم السابق
     * (الخميس لليلة الجمعة) وكذلك في دلو اليوم نفسه (الجمعة) لمن يبحث عنها نهاراً.
     */
    private val weekdayBuckets: Map<String, List<DailyDeed>> by lazy {
        val buckets = weekDays.associateWith { mutableListOf<DailyDeed>() }
        for (indexed in jinanIndexed) {
            val title = norm(indexed.item.title)
            val isNightDeed = title.contains("ليلة")
            for (day in weekDays) {
                if (title.contains(norm(day))) {
                    val deed = DailyDeed(
                        indexed.item,
                        indexed.chapterTitle,
                        indexed.sectionTitle,
                        DailyDeedSource.WEEKDAY
                    )
                    // إدراج العمل في يومه
                    buckets.getValue(day) += deed

                    // إدراج أعمال الليلة في مساء اليوم السابق (عصر/مغرب)
                    if (isNightDeed) {
                        val prevDay = previousWeekday(day)
                        if (prevDay != day) {
                            val prevList = buckets.getValue(prevDay)
                            if (prevList.none { it.item.id == indexed.item.id }) {
                                prevList += deed
                            }
                        }
                    }
                }
            }
        }
        buckets
    }

    /** أعمال الهجري فقط (بلا يوم الأسبوع) لتاريخ (شهر، يوم). */
    fun forHijriDate(monthNum: Int, day: Int): List<DailyDeed> {
        val entries = byMonth[monthNum] ?: return emptyList()
        val out = linkedMapOf<Int, DailyDeed>()
        for (entry in entries) {
            val matches = entry.dates.any { (m, d) ->
                m == monthNum &&
                    (d == GENERAL_MONTH || d == day || (d == LAST_DAY && day >= 29))
            }
            if (matches) out[entry.deed.item.id] = entry.deed
        }
        return out.values.toList()
    }

    fun weekday(weekdayName: String): List<DailyDeed> =
        if (weekdayName.isBlank()) emptyList() else weekdayBuckets[weekdayName].orEmpty()

    /** عدد أعمال الشهر (distinct) — لرقاقة الشهر وشريط الأشهر. */
    fun monthCount(monthNum: Int): Int = byMonth[monthNum]?.size ?: 0

    /** كل أعمال الشهر — تُعرض في «مفاتيح الجنان» (المصدر تاريخي لا ليلي). */
    fun monthWorks(monthNum: Int): List<DailyDeed> =
        byMonth[monthNum].orEmpty().map {
            it.deed.copy(source = DailyDeedSource.MONTH_DAY)
        }

    /** تحميل ساخن للفهرس من خيط خلفي قبل فتح الشاشات. */
    fun warmUp() {
        byMonth // يفعّل lazy
        weekdayBuckets
    }
}

/** أعمال يوم الأسبوع (اسم اليوم هو المطابقة). */
fun weekdayDeeds(weekdayName: String): List<DailyDeed> = DeedIndex.weekday(weekdayName)

/** أعمال الهجرية (الشهر/اليوم/عامة الشهر/الليلة) لتاريخ هجري بعينه — بدون يوم الأسبوع. */
private fun hijriWorksForDate(monthNum: Int, day: Int): List<DailyDeed> =
    DeedIndex.forHijriDate(monthNum, day)

/**
 * أعمال اليوم المطابقة لتاريخه الهجري **فقط** — كل عمل مرتبط بيوم هجري بعينه
 * أو بعامّه العام أو بيوم الأسبوع:
 *
 * 1) **الشهر + اليوم** — «الليلة الثالثة عشرة من شعبان» في 13 شعبان فقط.
 * 2) **عامة الشهر** — «ما يعم ليالي وأيام شهر رمضان» في كل أيام رمضان فقط.
 * 3) **الأعلام** — عرفة/الغدير/عاشوراء/المبعث… في يومها فقط.
 * 4) **الأخير** — «اليوم الأخير» في 29 و30 من شهره.
 * 5) **يوم الأسبوع** — «أعمال يوم الجمعة» كل يوم جمعة.
 *
 * لا تظهر أعمال شهر رمضان في غيره، ولا أعمال «ليلة مجردة» في كل الأشهر —
 * كل عمل مرتبط بشهره (من العنوان أو من قسم «أعمال أشهر السنة»).
 *
 * @param today التاريخ المدني الحالي (المنطقة الزمنية للتطبيق).
 * @param hijri التاريخ الهجري الحالي.
 */
fun dailyDeeds(today: LocalDate, hijri: HijriDate): List<DailyDeed> {
    val weekdayName = weekDays.getOrNull(today.dayOfWeek.ordinal).orEmpty()
    val out = linkedMapOf<Int, DailyDeed>()

    hijriWorksForDate(hijri.monthNum, hijri.day).forEach { out[it.item.id] = it }
    // getOrPut: إن كان العمل موجودًا بالسياق الهجري يبقى بموضعه ونوعه،
    // وإن كان أسبوعيًا فقط يُضاف في النهاية — نفس ترتيب النسخة القديمة.
    weekdayDeeds(weekdayName).forEach { out.getOrPut(it.item.id) { it } }

    return out.values.toList()
}

/**
 * عدّاد أعمال كل شهر هجري (يظهر على «شريط الأشهر») — يُبنى مرة واحدة.
 * لا تعتمد على سنة معيّنة: «أعمال هذا الشهر» هي نفسها في كل سنة.
 */
val monthWorkCount: Map<Int, Int> by lazy {
    (1..12).associateWith { DeedIndex.monthCount(it) }
}

/**
 * كل أعمال الشهر (المخصوصة + العامة) — تُستخدم لعرض «تعداد الشهر»
 * داخل «مفاتيح الجنان»، وهي نفسها مهما مرّت السنة.
 */
fun monthDeeds(monthNum: Int): List<DailyDeed> = DeedIndex.monthWorks(monthNum)
