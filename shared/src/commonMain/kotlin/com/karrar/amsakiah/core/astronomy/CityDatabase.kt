package com.karrar.amsakiah.core.astronomy

import com.karrar.amsakiah.resources.Res
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
    @SerialName("k") val kind: Int = 0
) {
    val isProvince: Boolean get() = kind == 1

    val display: String
        get() = if (isProvince) "$name (محافظة/ولاية)، $country" else "$name، $country"

    /** فرق التوقيت بالساعات عن UTC في اللحظة الحالية */
    fun offsetHoursNow(): Double = runCatching {
        TimeZone.of(timeZoneId).offsetAt(Clock.System.now()).totalSeconds / 3600.0
    }.getOrDefault(0.0)

    fun toCity(): City = City(country, name, lat, lon, offsetHoursNow())
}

data class NearbyCity(val city: OfflineCity, val distanceKm: Double)

/**
 * قاعدة المدن العالمية — تُقرأ من مورد أوفلاين (files/cities.json) مرة واحدة.
 */
object CityDatabase {

    private const val RES_PATH = "files/cities.json"

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private var cache: List<OfflineCity>? = null
    private var sortedByPop: List<OfflineCity>? = null

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
        val loaded = runCatching {
            val bytes = Res.readBytes(RES_PATH)
            json.decodeFromString<List<OfflineCity>>(bytes.decodeToString())
        }.getOrNull()
        val result = if (loaded.isNullOrEmpty()) fallback else loaded
        cache = result
        sortedByPop = result.sortedByDescending { it.population }
        return result
    }

    /**
     * القائمة مرتبة حسب عدد السكان (لعرض أهم المدن أولًا).
     *
     * عند تمرير دولة المستخدم تُعرض مدنها أولًا ثم بقية العالم،
     * لأن الترتيب العالمي يبدأ بمدن ضخمة لا تخص المستخدم (الصين، الهند…).
     */
    suspend fun popular(limit: Int = 4000, preferredCountry: String? = null): List<OfflineCity> {
        load()
        val all = sortedByPop.orEmpty()
        val country = preferredCountry?.trim()
        if (country.isNullOrEmpty()) return all.take(limit)
        val local = all.filter { it.country == country }
        val rest = all.filter { it.country != country }
        return (local + rest).take(limit)
    }

    /**
     * أقرب مدينة لإحداثيات معينة — يعمل أوفلاين بالكامل.
     *
     * تُستبعد صفوف المحافظات (`k=1`) لأن إحداثياتها هي إحداثيات مركز
     * أكبر مدينة فيها؛ ضمّها يجعل GPS يختار «محافظة» بدل المدينة نفسها.
     */
    suspend fun nearest(lat: Double, lon: Double): NearbyCity? {
        val cities = load()
        if (cities.isEmpty()) return null
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
        val found = best ?: return null
        return NearbyCity(found, bestDist)
    }

    /** بحث نصي بالعربية أو الإنجليزية */
    suspend fun search(query: String, limit: Int = 40, preferredCountry: String? = null): List<OfflineCity> {
        val q = normalize(query).trim()
        if (q.length < 2) return emptyList()
        val all = load()
        val country = preferredCountry?.trim().orEmpty()
        return all.asSequence()
            .filter {
                normalize(it.name).contains(q) ||
                    normalize(it.country).contains(q) ||
                    normalize(it.region).contains(q)
            }
            .sortedWith(compareByDescending<OfflineCity> { normalize(it.name).startsWith(q) }
                .thenByDescending { country.isNotEmpty() && it.country == country }
                .thenByDescending { it.population })
            .take(limit)
            .toList()
    }

    /** كل مدن دولة معينة */
    suspend fun byCountry(country: String, limit: Int = 300): List<OfflineCity> =
        load().asSequence()
            .filter { it.country == country }
            .sortedByDescending { it.population }
            .take(limit)
            .toList()

    suspend fun countries(): List<String> =
        load().map { it.country }.distinct().sorted()

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
                'أ', 'إ', 'آ' -> sb.append('ا')
                'ى' -> sb.append('ي')
                'ة' -> sb.append('ه')
                'ؤ' -> sb.append('و')
                'ئ' -> sb.append('ي')
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
}
