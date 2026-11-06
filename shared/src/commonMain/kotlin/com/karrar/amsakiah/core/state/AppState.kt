package com.karrar.amsakiah.core.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.karrar.amsakiah.core.astronomy.CityDatabase
import com.karrar.amsakiah.core.astronomy.NearbyCity
import com.karrar.amsakiah.core.astronomy.OfflineCity
import com.karrar.amsakiah.core.location.createLocationProvider
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
    }

    fun reset() {
        settings = AppSettings()
        persist()
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
                useGps = false
            )
        }
    }

    /** تحديد الموقع عبر GPS ثم مطابقة أقرب مدينة من القاعدة الأوفلاين */
    suspend fun detectLocation() {
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
                        useGps = true
                    )
                }
            }
        } finally {
            locating = false
        }
    }

    fun clearLocationError() {
        lastLocationError = null
    }
}
