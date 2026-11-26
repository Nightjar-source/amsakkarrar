package com.karrar.amsakiah.core.astronomy

import com.karrar.amsakiah.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * مدينة أو محافظة أوفلاين من قاعدة GeoNames
 * (كل مدن العالم فوق 5000 نسمة + المحافظات/الولايات).
 *
 * الحقول المختصرة لتقليل حجم JSON:
 * n = الاسم، c = الدولة، r = المحافظة/المنطقة،
 * la/lo = خط العرض/الطول، t = المنطقة الزمنية، p = السكان،
 * k = النوع (0 مدينة، 1 محافظة/ولاية).
 */
@Serializable
data class OfflineCity(
    @SerialName("n") val name: String,
    @SerialName("c") val country: String,
    @SerialName("la") val lat: Double,
    @SerialName("lo") val lon: Double,
    @SerialName("t") val timeZoneId: String = "UTC",
    @SerialName("p") val population: Long = 0,
    @SerialName("r") val region: String = "",
    @SerialName("k") val kind: Int = 0,
    /**
     * أسماء بديلة للبحث فقط (الإنجليزية/الاسم الأصلي قبل التعريب) — لا تُعرض.
     * `@Transient` حتى لا تتضخم قاعدة JSON ولا تتأثر قراءتها.
     */
    @Transient val searchAliases: List<String> = emptyList()
) {
    val isProvince: Boolean get() = kind == 1

    /** هل النص عربي/فارسي (فلا نعرض مناطق لاتينية مثل England؟) */
    val hasArabicScript: Boolean get() = name.any { it in '\u0600'..'\u06FF' }

    val display: String
        get() = if (isProvince) "$name (محافظة/ولاية)، $country" else "$name، $country"

    /** مفتاح ثابت فريد للقوائم (بلا تكرار حتى مع أسماء متطابقة). */
    val stableId: String get() = "$name|$country|$lat|$lon|$kind"

    /** فرق التوقيت بالساعات عن UTC في اللحظة الحالية */
    fun offsetHoursNow(): Double = runCatching {
        TimeZone.of(timeZoneId).offsetAt(Clock.System.now()).totalSeconds / 3600.0
    }.getOrDefault(0.0)

    fun toCity(): City = City(country, name, lat, lon, offsetHoursNow())
}

data class NearbyCity(val city: OfflineCity, val distanceKm: Double)

/**
 * قاعدة المدن العالمية — تُقرأ من مورد أوفلاين (files/cities.json) مرة واحدة.
 *
 * **أمان الخيوط:** الملف 9.1MB ويُفكّ إلى عشرات آلاف الكائنات؛ لذلك كل القراءة
 * والفكّ والفرز والبحث تجري على [Dispatchers.Default] خلف [Mutex]، مع
 * `Deferred` ضمني عبر القفل: أول استدعاء يفكّ، والبقية تنتظر ثم تقرأ الكاش.
 * هذا يمنع تجمّد الخيط الرئيسي (سبب انهيار حوار المدينة سابقًا).
 */
object CityDatabase {

    private const val RES_PATH = "files/cities.json"

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val mutex = Mutex()

    private var cache: List<OfflineCity>? = null
    private var sortedByPop: List<OfflineCity>? = null

    /**
     * إصلاح أسماء مخزنة بترانسلتيرايشن أجنبي/أردي — تُعرض كعربية صحيحة (لا تمس الإحداثيات).
     * المفتاح: (الدولة كما في الملف) إلى (الاسم الأصلي كما في الملف).
     */
    private val DISPLAY_FIX: Map<Pair<String, String>, String> = mapOf(
        ("سوريا" to "د مشق") to "دمشق",
        ("إيران" to "تهران") to "طهران",
        ("إيران" to "سناباد") to "مشهد",
        ("تركيا" to "آنقره") to "أنقرة",
        ("تركيا" to "برصہ") to "بورصة",
        ("الأردن" to "ئاممان") to "عمّان",
        ("الجزائر" to "ئالجىر") to "الجزائر",
        ("المغرب" to "أنفا") to "الدار البيضاء",
        ("البحرين" to "اَلْمُحَرَّق") to "المحرق",
        ("الهند" to "بمبئی") to "مومباي",
        ("الهند" to "بمبئی نو") to "نافي مومباي",
        ("الولايات المتحدة" to "لاس اینجلس") to "لوس أنجلوس",
        ("الولايات المتحدة" to "شيكاجو") to "شيكاغو",
        ("الولايات المتحدة" to "فلاڈیلفیا") to "فيلادلفيا",
        ("بريطانيا" to "لأندأن") to "لندن",
        ("بريطانيا" to "برسٹل نگر") to "بريستول",
        ("بريطانيا" to "برمنگم") to "برمنغهام",
        ("فرنسا" to "استراسبورگ") to "ستراسبورغ",
        ("روسيا" to "ماسکو") to "موسكو",
        ("البرازيل" to "ساؤ پالو") to "ساو باولو",
        ("الأرجنتين" to "بوئنوس آیرس") to "بوينس آيرس",
        ("ميانمار" to "رنگون") to "يانغون",
        ("الصين" to "شینیانگ") to "شنيانغ",
        ("الصين" to "سیان") to "شيآن",
        ("الصين" to "خاربىن شەھىرى") to "هاربين",
        ("الصين" to "خاڭجۇ شەھىر") to "هانغجو",
        ("الصين" to "نانجنگ") to "نانجينغ",
        ("الصين" to "ناننینگ") to "ناننينغ",
        ("الصين" to "ویفانگ") to "ويفانغ",
        ("الصين" to "تانگشان") to "تانغشان",
        ("الصين" to "داتونگ") to "داتونغ",
        ("الصين" to "شاوشنگ") to "شاوتشينغ",
        ("الصين" to "نانتانگ") to "نانتونغ",
        ("الصين" to "گانژوو") to "غانتشو",
        ("الصين" to "جوهای") to "تشوهاي",
        ("الصين" to "کونشان") to "كونشان",
        ("الصين" to "زونئی") to "زونيي",
        ("الصين" to "لنیی") to "ليني",
        ("الصين" to "بائودینگ") to "باودينغ",
        ("الصين" to "چانگژو") to "تشانغجو",
        ("الصين" to "هویژو") to "هويجو",
        ("الصين" to "شییان") to "شييان",
        ("الصين" to "نانگبو") to "نينغبو",
        ("الصين" to "زایبو") to "زيبو",
        ("الصين" to "شجیا جھوانگ") to "شيجياتشوانغ",
        ("الصين" to "تائی یوآن") to "تاي يوان",
        ("هونغ كونغ" to "شياڭگاڭ") to "هونغ كونغ"
    )

    /**
     * مدن لاتينية الاسم في GeoNames — تُعرض بالعربية ويُحفظ الاسم اللاتيني
     * كاسم بحث بديل (فتظل «Queens» تجد «كوينز» والعكس).
     */
    private val LATIN_NAME_FIX: Map<String, String> = mapOf(
        "London" to "لندن",
        "Brent" to "برنت",
        "Queens" to "كوينز",
        "Manhattan" to "مانهاتن",
        "The Bronx" to "برونكس",
        "Staten Island" to "ستاتن آيلاند",
        "Brooklyn" to "بروكلين",
        "New York" to "نيويورك",
        "Quezon City" to "كيزون سيتي",
        "New Taipei City" to "تايبيه الجديدة",
        "New Territories" to "الأقاليم الجديدة",
        "Kowloon" to "كولون",
        "Santo Domingo" to "سانتو دومينغو",
        "Conakry" to "كوناكري",
        "Onitsha" to "أونيتشا",
        "Surat" to "سورات",
        "Kanpur" to "كانبور",
        "Ludhiana" to "لوديانا",
        "Pudong" to "بودونغ",
        "Shanghai" to "شنغهاي",
        "Beijing" to "بكين",
        "Guangzhou" to "غوانغجو",
        "Shenzhen" to "شنجن",
        "Hong Kong" to "هونغ كونغ",
        "Macau" to "ماكاو",
        "Taipei" to "تايبيه",
        "Tokyo" to "طوكيو",
        "Osaka" to "أوساكا",
        "Seoul" to "سيول",
        "Bangkok" to "بانكوك",
        "Singapore" to "سنغافورة",
        "Kuala Lumpur" to "كوالالمبور",
        "Jakarta" to "جاكرتا",
        "Manila" to "مانيلا",
        "Sydney" to "سيدني",
        "Melbourne" to "ملبورن",
        "Toronto" to "تورونتو",
        "Vancouver" to "فانكوفر",
        "Chicago" to "شيكاغو",
        "Houston" to "هيوستن",
        "Phoenix" to "فينيكس",
        "Philadelphia" to "فيلادلفيا",
        "San Antonio" to "سان أنطونيو",
        "San Diego" to "سان دييغو",
        "Dallas" to "دالاس",
        "Miami" to "ميامي",
        "Berlin" to "برلين",
        "Munich" to "ميونخ",
        "Frankfurt" to "فرانكفورت",
        "Hamburg" to "هامبورغ",
        "Paris" to "باريس",
        "Marseille" to "مرسيليا",
        "Lyon" to "ليون",
        "Rome" to "روما",
        "Milan" to "ميلانو",
        "Naples" to "نابولي",
        "Madrid" to "مدريد",
        "Barcelona" to "برشلونة",
        "Lisbon" to "لشبونة",
        "Amsterdam" to "أمستردام",
        "Brussels" to "بروكسل",
        "Vienna" to "فيينا",
        "Prague" to "براغ",
        "Warsaw" to "وارسو",
        "Budapest" to "بودابست",
        "Athens" to "أثينا",
        "Stockholm" to "ستوكهولم",
        "Oslo" to "أوسلو",
        "Copenhagen" to "كوبنهاغن",
        "Dublin" to "دبلن",
        "Manchester" to "مانشستر",
        "Birmingham" to "برمنغهام",
        "Glasgow" to "غلاسكو",
        "Liverpool" to "ليفربول",
        "Edinburgh" to "إدنبرة",
        "Cardiff" to "كارديف",
        "Bristol" to "بريستول",
        "Leeds" to "ليدز",
        "Sheffield" to "شيفيلد",
        "Newcastle upon Tyne" to "نيوكاسل",
        "Nottingham" to "نوتنغهام",
        "Leicester" to "ليستر",
        "Coventry" to "كوفنتري",
        "Belfast" to "بلفاست",
        "Bradford" to "برادفورد"
    )

    /** أسماء إنجليزية شائعة للمدن الكبرى — تُتيح البحث «London» عن «لندن». */
    private val EN_ALIASES: Map<String, List<String>> = mapOf(
        "لندن" to listOf("London"),
        "باريس" to listOf("Paris"),
        "روما" to listOf("Rome"),
        "مدريد" to listOf("Madrid"),
        "برلين" to listOf("Berlin"),
        "فيينا" to listOf("Vienna"),
        "موسكو" to listOf("Moscow"),
        "نيويورك" to listOf("New York", "NYC"),
        "لوس أنجلوس" to listOf("Los Angeles", "LA"),
        "شيكاغو" to listOf("Chicago"),
        "تورونتو" to listOf("Toronto"),
        "سيدني" to listOf("Sydney"),
        "طوكيو" to listOf("Tokyo"),
        "بكين" to listOf("Beijing"),
        "سيول" to listOf("Seoul"),
        "دبي" to listOf("Dubai"),
        "إسطنبول" to listOf("Istanbul"),
        "القاهرة" to listOf("Cairo"),
        "الرياض" to listOf("Riyadh"),
        "جدة" to listOf("Jeddah"),
        "مكة المكرمة" to listOf("Mecca", "Makkah"),
        "المدينة المنورة" to listOf("Medina", "Madinah"),
        "بغداد" to listOf("Baghdad"),
        "كربلاء" to listOf("Karbala"),
        "النجف" to listOf("Najaf"),
        "الكويت" to listOf("Kuwait"),
        "مسقط" to listOf("Muscat"),
        "الدوحة" to listOf("Doha"),
        "أبوظبي" to listOf("Abu Dhabi"),
        "عمّان" to listOf("Amman"),
        "بيروت" to listOf("Beirut"),
        "دمشق" to listOf("Damascus"),
        "تونس" to listOf("Tunis"),
        "الجزائر" to listOf("Algiers"),
        "الرباط" to listOf("Rabat"),
        "الدار البيضاء" to listOf("Casablanca"),
        "مراكش" to listOf("Marrakech"),
        "لشبونة" to listOf("Lisbon"),
        "أمستردام" to listOf("Amsterdam"),
        "بروكسل" to listOf("Brussels"),
        "ستوكهولم" to listOf("Stockholm"),
        "أوسلو" to listOf("Oslo"),
        "كوبنهاغن" to listOf("Copenhagen"),
        "أثينا" to listOf("Athens"),
        "براغ" to listOf("Prague"),
        "بودابست" to listOf("Budapest"),
        "وارسو" to listOf("Warsaw"),
        "ميونخ" to listOf("Munich"),
        "فرانكفورت" to listOf("Frankfurt"),
        "هامبورغ" to listOf("Hamburg"),
        "ميلانو" to listOf("Milan"),
        "نابولي" to listOf("Naples"),
        "برشلونة" to listOf("Barcelona"),
        "فالنسيا" to listOf("Valencia"),
        "إشبيلية" to listOf("Seville"),
        "دبلن" to listOf("Dublin"),
        "مانشستر" to listOf("Manchester"),
        "برمنغهام" to listOf("Birmingham"),
        "ليفربول" to listOf("Liverpool"),
        "إدنبرة" to listOf("Edinburgh"),
        "غلاسكو" to listOf("Glasgow"),
        "كارديف" to listOf("Cardiff"),
        "بريستول" to listOf("Bristol"),
        "ليدز" to listOf("Leeds"),
        "شيفيلد" to listOf("Sheffield"),
        "نيوكاسل" to listOf("Newcastle"),
        "نوتنغهام" to listOf("Nottingham"),
        "ليستر" to listOf("Leicester"),
        "كوفنتري" to listOf("Coventry"),
        "بلفاست" to listOf("Belfast"),
        "برادفورد" to listOf("Bradford")
    )

    private val EN_ALIASES_NORM: Map<String, List<String>> by lazy {
        EN_ALIASES.mapKeys { normalize(it.key) }
    }

    private val LATIN_NAME_FIX_CI: Map<String, String> by lazy {
        LATIN_NAME_FIX.mapKeys { it.key.lowercase() }
    }

    private val LATIN_NAME_FIX_REVERSE: Map<String, String> by lazy {
        LATIN_NAME_FIX.entries.associate { it.value to it.key }
    }

    /** أسماء إنجليزية للدول — لتجد «UK» و«USA» بالعربية. */
    private val COUNTRY_ALIASES: Map<String, List<String>> = mapOf(
        "بريطانيا" to listOf("UK", "United Kingdom", "Britain", "England"),
        "الولايات المتحدة" to listOf("USA", "United States", "US", "America"),
        "الإمارات" to listOf("UAE", "United Arab Emirates"),
        "السعودية" to listOf("Saudi Arabia", "KSA"),
        "مصر" to listOf("Egypt"),
        "العراق" to listOf("Iraq"),
        "إيران" to listOf("Iran"),
        "تركيا" to listOf("Turkey", "Türkiye"),
        "فرنسا" to listOf("France"),
        "ألمانيا" to listOf("Germany"),
        "إيطاليا" to listOf("Italy"),
        "إسبانيا" to listOf("Spain"),
        "كندا" to listOf("Canada"),
        "أستراليا" to listOf("Australia"),
        "الهند" to listOf("India"),
        "باكستان" to listOf("Pakistan"),
        "ماليزيا" to listOf("Malaysia"),
        "إندونيسيا" to listOf("Indonesia"),
        "روسيا" to listOf("Russia"),
        "الصين" to listOf("China"),
        "اليابان" to listOf("Japan"),
        "كوريا الجنوبية" to listOf("South Korea", "Korea")
    )

    /** تحويل الحروف الفارسية/الأردية إلى أقرب مقابل عربي — للعرض فقط. */
    private fun sanitizeScript(text: String): String = buildString(text.length) {
        for (ch in text) {
            when (ch) {
                'پ' -> append('ب')
                'چ' -> append("تش")
                'ژ' -> append('ز')
                'گ' -> append('غ')
                'ک' -> append('ك')
                'ی' -> append('ي')
                'ٹ' -> append('ت')
                'ڈ' -> append('د')
                'ڑ' -> append('ر')
                'ں' -> append('ن')
                'ھ' -> append('ه')
                'ے' -> append('ي')
                'ۇ' -> append('و')
                'ە' -> append('ه')
                'ڭ' -> append("نغ")
                'ۋ' -> append('و')
                else -> append(ch)
            }
        }
    }.replace(Regex("\\s+"), " ").trim()

    /** عواصم غائبة تمامًا من ملف GeoNames — تُضاف عند التحميل (أوفلاين) مرة واحدة. */
    private val INJECTED: List<OfflineCity> = listOf(
        OfflineCity("الكويت", "الكويت", 29.3759, 47.9774, "Asia/Kuwait", 2400000),
        OfflineCity("مسقط", "سلطنة عمان", 23.5880, 58.3829, "Asia/Muscat", 1600000)
    )

    private fun fixDisplayName(city: OfflineCity): OfflineCity {
        val original = city.name
        val fixed = DISPLAY_FIX[city.country to original]
            ?: LATIN_NAME_FIX_CI[original.lowercase()]
        val cleaned = sanitizeScript(fixed ?: original)
        val aliases = buildList {
            if (fixed != null) add(original)
            // الاسم الأصلي دائمًا صالح للبحث (عربي أو لاتيني)
            add(original)
            LATIN_NAME_FIX_REVERSE[cleaned]?.let { add(it) }
            EN_ALIASES_NORM[normalize(cleaned)].orEmpty().forEach(::add)
        }.map { it.trim() }
            .filter { it.isNotEmpty() && normalize(it) != normalize(cleaned) }
            .distinctBy { normalize(it) }
        return city.copy(name = cleaned, searchAliases = aliases)
    }

    /** قائمة احتياطية صغيرة إن تعذّر قراءة المورد */
    private val fallback = listOf(
        OfflineCity("كربلاء", "العراق", 32.6160, 44.0249, "Asia/Baghdad", 700000),
        OfflineCity("النجف", "العراق", 32.0286, 44.3436, "Asia/Baghdad", 900000),
        OfflineCity("بغداد", "العراق", 33.3152, 44.3661, "Asia/Baghdad", 7000000),
        OfflineCity("مكة المكرمة", "السعودية", 21.4225, 39.8262, "Asia/Riyadh", 1500000),
        OfflineCity("المدينة المنورة", "السعودية", 24.4672, 39.6112, "Asia/Riyadh", 1100000),
        OfflineCity("طهران", "إيران", 35.6892, 51.3890, "Asia/Tehran", 9000000)
    )

    suspend fun load(): List<OfflineCity> {
        cache?.let { return it }
        return mutex.withLock {
            cache?.let { return@withLock it }
            val result = withContext(Dispatchers.Default) {
                runCatching {
                    val bytes = Res.readBytes(RES_PATH)
                    json.decodeFromString<List<OfflineCity>>(bytes.decodeToString())
                }.getOrNull()
                    .let { loaded -> if (loaded.isNullOrEmpty()) fallback else loaded }
                    // إصلاح الأسماء العجمية (مرة واحدة عند الفك) + حقن العواصم المفقودة
                    .map { fixDisplayName(it) }
                    .plus(INJECTED)
                    // إزالة التكرار: GeoNames يحتوي صفوفًا متطابقة الاسم/الدولة/الإحداثيات.
                    .distinctBy { it.stableId }
            }
            cache = result
            sortedByPop = withContext(Dispatchers.Default) {
                result.sortedByDescending { it.population }
            }
            result
        }
    }

    /**
     * القائمة مرتبة حسب عدد السكان (لعرض أهم المدن أولًا).
     *
     * عند تمرير دولة المستخدم تُعرض مدنها أولًا ثم بقية العالم،
     * لأن الترتيب العالمي يبدأ بمدن ضخمة لا تخص المستخدم (الصين، الهند…).
     */
    suspend fun popular(limit: Int = 4000, preferredCountry: String? = null): List<OfflineCity> {
        load()
        return withContext(Dispatchers.Default) {
            val all = sortedByPop.orEmpty()
            val country = preferredCountry?.trim()
            val ordered = if (country.isNullOrEmpty()) {
                all
            } else {
                val local = all.filter { it.country == country }
                val rest = all.filter { it.country != country }
                local + rest
            }
            // ضمان عدم تكرار المفاتيح في LazyColumn (التكرار يسبب انهيار التركيب).
            ordered.distinctBy { it.stableId }.take(limit)
        }
    }

    /**
     * أقرب مدينة لإحداثيات معينة — يعمل أوفلاين بالكامل.
     *
     * تُستبعد صفوف المحافظات (`k=1`) لأن إحداثياتها هي إحداثيات مركز
     * أكبر مدينة فيها؛ ضمّها يجعل GPS يختار «محافظة» بدل المدينة نفسها.
     */
    suspend fun nearest(lat: Double, lon: Double): NearbyCity? {
        val cities = load()
        return withContext(Dispatchers.Default) {
            var best: OfflineCity? = null
            var bestDist = Double.MAX_VALUE
            for (city in cities) {
                if (city.isProvince) continue
                // فحص سريع لتقليل الحسابات
                val dLat = abs(city.lat - lat)
                if (dLat * 111.0 > bestDist) continue
                val d = haversine(lat, lon, city.lat, city.lon)
                if (d < bestDist) {
                    bestDist = d
                    best = city
                }
            }
            best?.let { NearbyCity(it, bestDist) }
        }
    }

    /** بحث نصي بالعربية أو الإنجليزية — على خيط خلفي مع تفضيل الدولة والبداية. */
    suspend fun search(query: String, limit: Int = 40, preferredCountry: String? = null): List<OfflineCity> {
        val q = normalize(query).trim()
        if (q.length < 2) return emptyList()
        val all = load()
        val country = preferredCountry?.trim().orEmpty()
        return withContext(Dispatchers.Default) {
            all.asSequence()
                .filter { city ->
                    val aliases = city.searchAliases
                    normalize(city.name).contains(q) ||
                        normalize(city.country).contains(q) ||
                        normalize(city.region).contains(q) ||
                        aliases.any { normalize(it).contains(q) } ||
                        COUNTRY_ALIASES[city.country].orEmpty().any { normalize(it).contains(q) }
                }
                .sortedWith(
                    compareByDescending<OfflineCity> {
                        normalize(it.name).startsWith(q) ||
                            it.searchAliases.any { alias -> normalize(alias).startsWith(q) }
                    }
                        .thenByDescending { country.isNotEmpty() && it.country == country }
                        .thenByDescending { it.population }
                )
                .distinctBy { it.stableId }
                .take(limit)
                .toList()
        }
    }

    /** كل مدن دولة معينة */
    suspend fun byCountry(country: String, limit: Int = 300): List<OfflineCity> =
        withContext(Dispatchers.Default) {
            load().asSequence()
                .filter { it.country == country }
                .sortedByDescending { it.population }
                .take(limit)
                .toList()
        }

    suspend fun countries(): List<String> =
        withContext(Dispatchers.Default) {
            load().map { it.country }.distinct().sorted()
        }

    fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = (lat2 - lat1) * PI_DIV_180
        val dLon = (lon2 - lon1) * PI_DIV_180
        val a = sin(dLat / 2).pow(2) +
            cos(lat1 * PI_DIV_180) * cos(lat2 * PI_DIV_180) * sin(dLon / 2).pow(2)
        return r * (2 * atan2(sqrt(a), sqrt(1 - a)))
    }

    private const val PI_DIV_180 = 0.017453292519943295

    fun normalize(text: String): String {
        val sb = StringBuilder(text.length)
        for (ch in text) {
            when (ch) {
                in '\u064B'..'\u065F', '\u0670', '\u0640' -> Unit
                'أ', 'إ', 'آ', 'ٱ' -> sb.append('ا')
                'ى', 'ئ', 'ے' -> sb.append('ي')
                'ؤ' -> sb.append('و')
                'ة', 'ہ', 'ھ', 'ە' -> sb.append('ه')
                'ء' -> Unit // همزة منفصلة تُسقَط: «كربلاء» تُوجد بـ«كربلا» و«كرباء»
                'ک' -> sb.append('ك') // كاف فارسية U+06A9
                'ی' -> sb.append('ي') // ياء فارسية U+06CC
                'گ' -> sb.append('غ') // گاف فارسية U+06AF
                'پ' -> sb.append('ب')
                'چ' -> sb.append("تش")
                'ژ' -> sb.append('ز')
                'ٹ' -> sb.append('ت')
                'ڈ' -> sb.append('د')
                'ڑ' -> sb.append('ر')
                'ں' -> sb.append('ن')
                'ۇ' -> sb.append('و')
                'ڭ' -> sb.append("نغ")
                'ۋ' -> sb.append('و')
                else -> sb.append(ch.lowercaseChar())
            }
        }
        return sb.toString()
    }

    /** وصف القرب: «بالقرب من كربلاء، العراق» */
    fun proximityLabel(nearby: NearbyCity?): String {
        if (nearby == null) return "موقع غير محدد"
        val c = nearby.city
        val km = nearby.distanceKm
        val distance = if (km < 1.0) "أقل من كم" else "${km.toInt()} كم"
        return "بالقرب من ${c.name}، ${c.country} ($distance)"
    }

    private val ISO_TO_COUNTRY_AR: Map<String, String> = mapOf(
        "iq" to "العراق",
        "sa" to "السعودية",
        "ir" to "إيران",
        "kw" to "الكويت",
        "bh" to "البحرين",
        "ae" to "الإمارات",
        "qa" to "قطر",
        "om" to "عمان",
        "ye" to "اليمن",
        "lb" to "لبنان",
        "sy" to "سوريا",
        "jo" to "الأردن",
        "ps" to "فلسطين",
        "eg" to "مصر",
        "ly" to "ليبيا",
        "tn" to "تونس",
        "dz" to "الجزائر",
        "ma" to "المغرب",
        "sd" to "السودان",
        "tr" to "تركيا",
        "gb" to "بريطانيا",
        "de" to "ألمانيا",
        "fr" to "فرنسا",
        "nl" to "هولندا",
        "se" to "السويد",
        "dk" to "الدنمارك",
        "us" to "الولايات المتحدة",
        "ca" to "كندا",
        "au" to "أستراليا",
        "pk" to "باكستان",
        "in" to "الهند",
        "af" to "أفغانستان",
        "az" to "أذربيجان"
    )

    /**
     * مطابقة المدينة الافتراضية/العاصمة من قاعدة البيانات المحلية بالمللي ثانية
     * اعتماداً على رمز دولة الشريحة (ISO) والمنطقة الزمنية للهاتف بدون أي صلاحيات أو إنترنت.
     */
    suspend fun findDefaultByTimeZoneOrCountry(
        countryIso: String,
        timeZoneId: String
    ): OfflineCity? = withContext(Dispatchers.Default) {
        load()
        val byPop = sortedByPop.orEmpty()
        val countryAr = ISO_TO_COUNTRY_AR[countryIso.lowercase()]
        if (countryAr != null) {
            byPop.firstOrNull { it.country == countryAr && it.timeZoneId.equals(timeZoneId, ignoreCase = true) }
                ?: byPop.firstOrNull { it.country == countryAr }
        } else if (timeZoneId.isNotBlank()) {
            byPop.firstOrNull { it.timeZoneId.equals(timeZoneId, ignoreCase = true) }
        } else {
            null
        }
    }

    /** اختيار معيار الحساب الأنسب تلقائياً حسب رمز الدولة والمنطقة الزمنية. */
    fun recommendedMethodForCountry(countryIso: String, timeZoneId: String): String {
        val iso = countryIso.lowercase()
        return when {
            iso == "iq" || timeZoneId == "Asia/Baghdad" -> "alkafeel"
            iso == "ir" || timeZoneId == "Asia/Tehran" -> "tehran"
            iso == "sa" || timeZoneId == "Asia/Riyadh" -> "ummalqura"
            iso == "gb" || timeZoneId == "Europe/London" -> "london_shia"
            iso == "eg" || timeZoneId == "Africa/Cairo" -> "egypt"
            iso == "pk" || timeZoneId == "Asia/Karachi" -> "karachi"
            iso == "fr" || timeZoneId == "Europe/Paris" -> "france_12"
            iso in setOf("de", "se", "nl", "dk", "at", "be", "ch", "no") || timeZoneId.startsWith("Europe/") -> "european_council"
            iso in setOf("us", "ca") || timeZoneId.startsWith("America/") -> "isna"
            else -> "alkafeel"
        }
    }
}
