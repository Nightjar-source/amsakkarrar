package com.karrar.amsakiah.core.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.karrar.amsakiah.core.astronomy.CityDatabase
import com.karrar.amsakiah.core.astronomy.NearbyCity
import com.karrar.amsakiah.core.astronomy.OfflineCity
import com.karrar.amsakiah.core.location.createLocationProvider
import com.karrar.amsakiah.core.location.fetchIpLocation
import com.karrar.amsakiah.core.settings.AppSettings
import com.karrar.amsakiah.core.settings.SettingsStorage
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json

/**
 * حالة التطبيق المركزية — مصدر الحقيقة الوحيد لكل الشاشات.
 */
class AppState {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    var settings by mutableStateOf(loadSettings())
        private set

    var now: Instant by mutableStateOf(Clock.System.now())
        private set

    var nearby: NearbyCity? by mutableStateOf(null)
        private set

    var cityCount: Int by mutableStateOf(0)
        private set

    var locating: Boolean by mutableStateOf(false)
        private set

    var lastLocationError: String? by mutableStateOf(null)
        private set

    /**
     * مزامنة التغييرات القادمة من خارج الواجهة (ودجت المسبحة، الإجراءات السريعة).
     * عند تعديل الإعدادات من الودجت تُحدَّث حالة التطبيق فورًا دون إعادة تشغيل.
     */
    private val externalSync: () -> Unit = {
        val stored = loadSettings()
        if (stored != settings) settings = stored
    }

    init {
        SettingsChangeBus.addListener(externalSync)
    }

    fun dispose() {
        SettingsChangeBus.removeListener(externalSync)
    }

    private fun loadSettings(): AppSettings = runCatching {
        SettingsStorage.read()?.let { json.decodeFromString<AppSettings>(it) }
    }.getOrNull() ?: AppSettings()

    fun tick() {
        now = Clock.System.now()
        // تحديث الوقت كل ثانية دون إعادة حساب المواقيت
    }

    fun update(transform: (AppSettings) -> AppSettings) {
        settings = transform(settings)
        persist()
        SettingsChangeBus.notifyChanged()
    }

    fun reset() {
        settings = AppSettings()
        persist()
        SettingsChangeBus.notifyChanged()
    }

    private fun persist() {
        runCatching { SettingsStorage.write(json.encodeToString(AppSettings.serializer(), settings)) }
    }

    /** تهيئة قاعدة المدن (أوفلاين) */
    suspend fun prepareCities() {
        val cities = CityDatabase.load()
        cityCount = cities.size
    }

    /** اختيار مدينة يدويًا من القاعدة */
    fun selectCity(city: OfflineCity) {
        update {
            it.copy(
                cityName = city.name,
                countryName = city.country,
                latitude = city.lat,
                longitude = city.lon,
                timeZoneId = city.timeZoneId,
                useGps = false,
                locationSource = "manual"
            )
        }
    }

    /** تحديد الموقع عبر GPS ثم مطابقة أقرب مدينة من القاعدة الأوفلاين */
    suspend fun detectLocation() {
        if (locating) return
        locating = true
        lastLocationError = null
        try {
            val provider = createLocationProvider()
            if (!provider.isAvailable()) {
                lastLocationError = "خدمة الموقع غير متوفرة على هذا الجهاز"
                return
            }
            if (!provider.hasPermission()) {
                lastLocationError = "لم يُمنح إذن الموقع"
                return
            }
            val point = provider.currentLocation()
            if (point == null) {
                lastLocationError = "تعذّر الحصول على إحداثيات الجهاز"
                return
            }
            val nearest = CityDatabase.nearest(point.lat, point.lon)
            nearby = nearest
            if (nearest != null) {
                update {
                    it.copy(
                        cityName = nearest.city.name,
                        countryName = nearest.city.country,
                        latitude = point.lat,
                        longitude = point.lon,
                        timeZoneId = nearest.city.timeZoneId,
                        useGps = true,
                        locationSource = "gps"
                    )
                }
            }
        } finally {
            locating = false
        }
    }

    /**
     * استشعار الموقع عبر الشبكة (IP) — كما في تطبيق الكفيل.
     * يستخدم إحداثيات IP الدقيقة، ويستعير الاسم العربي والمنطقة الزمنية
     * من أقرب مدينة أوفلاين إن كانت قريبة (≤ 80 كم).
     */
    suspend fun detectLocationByIp() {
        if (locating) return
        locating = true
        lastLocationError = null
        try {
            val ip = fetchIpLocation()
            if (ip == null) {
                lastLocationError = "تعذّر استشعار الموقع عبر IP — تحقق من اتصال الإنترنت"
                return
            }
            val nearest = CityDatabase.nearest(ip.latitude, ip.longitude)
            nearby = nearest
            val close = nearest?.takeIf { it.distanceKm <= 80.0 }
            val cityName = close?.city?.name ?: ip.city.ifBlank { settings.cityName }
            val countryName = close?.city?.country ?: ip.country.ifBlank { settings.countryName }
            val zone = ip.timeZoneId ?: close?.city?.timeZoneId ?: settings.timeZoneId
            update {
                it.copy(
                    cityName = cityName,
                    countryName = countryName,
                    latitude = ip.latitude,
                    longitude = ip.longitude,
                    timeZoneId = zone,
                    useGps = false,
                    locationSource = "ip"
                )
            }
        } finally {
            locating = false
        }
    }

    fun clearLocationError() {
        lastLocationError = null
    }
}
