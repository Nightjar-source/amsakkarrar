package com.karrar.amsakiah.core.state

import com.karrar.amsakiah.core.astronomy.CityDatabase
import com.karrar.amsakiah.core.astronomy.NearbyCity
import com.karrar.amsakiah.core.astronomy.OfflineCity
import com.karrar.amsakiah.core.location.createLocationProvider
import com.karrar.amsakiah.core.location.fetchIpLocation
import com.karrar.amsakiah.core.settings.AppSettings
import com.karrar.amsakiah.core.settings.SettingsStorage
import com.karrar.amsakiah.core.settings.ThemeMode
import com.karrar.amsakiah.core.state.SettingsChangeBus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

/**
 * النواة التشغيلية للدولة والعمليات — BUSINESS LOGIC خالصة بلا أي اعتماديات UI.
 *
 * قواعد القطع (UI-Free Core):
 * - لا استورد `androidx.compose.*` ولا أي Compose runtime/foundation.
 * - لا يوجد هنا أي مرجع للألوان، الخطوط، الشاشات، Compose Icons.
 * - الأنواع والأساليب في هذا الملف قابلة للتعديل وقابلة للاختبار على كل
 *   المنصات (بما فيها iOS/Desktop) بناءً على kotlinx.coroutines لوحدها.
 * - الواجهة تقرأ الحالة فقط عبر [onStateChanged] (Callback بسيط) وترسل الأحداث؛
 *   هذا هو الحد الفاصل AL.
 *
 * حدث: ألزم الواجهة المشاهدة للحالة:
 *   kernel.onStateChanged = { sync() }
 *   وعند كل عدّة state→UI من داخل الـKernel يجمع [notifyStateChanged].
 */
class AppKernel {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    /** تُستدعى بعد أي تعديل داخلي على الحالة — تستخدمها طبقة Compose للربط (snapshot). */
    var onStateChanged: (() -> Unit)? = null

    var settings: AppSettings = loadSettings()
        private set

    var now: Instant = Clock.System.now()
        private set

    var nearby: NearbyCity? = null
        private set

    var cityCount: Int = 0
        private set

    var locating: Boolean = false
        private set

    var lastLocationError: String? = null
        private set

    var jinanReady: Boolean = false
        private set

    fun markJinanReady() {
        jinanReady = true
        notifyStateChanged()
    }

    /** خدمة الموقع معطّلة في إعدادات النظام — تُعرض معها زر «تشغيل الموقع». */
    var locationServiceDisabled: Boolean = false
        private set

    var toast: ToastMessage? = null
        private set

    private var toastSeq = 0L

    /** حدث: إظهار تنبيه عائم جديد (معرّف متسلسل يمنع التكرار بنفس المفتاح). */
    fun showToast(text: String, kind: ToastKind = ToastKind.INFO, actionLabel: String? = null) {
        if (text.isBlank()) return
        toastSeq += 1
        toast = ToastMessage(toastSeq, text.trim(), kind, actionLabel)
        notifyStateChanged()
    }

    /** حدث: إخفاء التنبيه الحالي. */
    fun dismissToast() {
        toast = null
        notifyStateChanged()
    }

    private val externalSync: () -> Unit = {
        val stored = loadSettings()
        if (stored != settings) {
            settings = stored
            notifyStateChanged()
        }
    }

    init {
        SettingsChangeBus.addListener(externalSync)
    }

    /** لا تستدعيها في الواجهة؛ تُحوَّل للدالة من الحاوية إن لزم. */
    fun dispose() {
        SettingsChangeBus.removeListener(externalSync)
    }

    private fun notifyStateChanged() {
        onStateChanged?.invoke()
    }

    private fun loadSettings(): AppSettings = runCatching {
        val raw = SettingsStorage.read() ?: return@runCatching null
        val decoded = json.decodeFromString<AppSettings>(raw)
        val hasThemeMode = runCatching {
            json.parseToJsonElement(raw).jsonObject.containsKey("themeMode")
        }.getOrDefault(false)
        val migrated = if (hasThemeMode) {
            decoded
        } else {
            val wasDark = Regex("\"darkTheme\"\\s*:\\s*(true|false)")
                .find(raw)?.groupValues?.get(1) != "false"
            decoded.copy(themeMode = if (wasDark) ThemeMode.DARK else ThemeMode.SYSTEM)
        }
        val legacyKeys = listOf("reminderEnabled", "reminderPrayers", "reminderMinutes", "darkTheme")
        if (legacyKeys.any { raw.contains("\"$it\"") }) {
            runCatching { SettingsStorage.write(json.encodeToString(AppSettings.serializer(), migrated)) }
        }
        migrated
    }.getOrNull() ?: AppSettings()

    fun tick(now: Instant = Clock.System.now()) {
        this.now = now
        notifyStateChanged()
    }

    fun update(transform: (AppSettings) -> AppSettings) {
        settings = transform(settings)
        persist()
        SettingsChangeBus.notifyChanged()
        notifyStateChanged()
    }

    fun reset() {
        settings = AppSettings()
        persist()
        SettingsChangeBus.notifyChanged()
        notifyStateChanged()
    }

    private fun persist() {
        runCatching { SettingsStorage.write(json.encodeToString(AppSettings.serializer(), settings)) }
    }

    /** تهيئة قاعدة المدن (أوفلاين) */
    suspend fun prepareCities() {
        val cities = CityDatabase.load()
        cityCount = cities.size
        notifyStateChanged()
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
                locationSource = "manual",
                locationResolved = true
            )
        }
    }

    /** تحديد الموقع عبر GPS ثم مطابقة أقرب مدينة من القاعدة الأوفلاين */
    suspend fun detectLocation() {
        if (locating) return
        locating = true
        lastLocationError = null
        locationServiceDisabled = false
        notifyStateChanged()
        try {
            val provider = createLocationProvider()
            if (!provider.isAvailable()) {
                reportLocationError("خدمة الموقع غير متوفرة على هذا الجهاز")
                return
            }
            if (!provider.isLocationEnabled()) {
                locationServiceDisabled = true
                reportLocationError("خدمة الموقع معطّلة — شغّلها من إعدادات الجهاز", "تشغيل الموقع")
                return
            }
            if (!provider.hasPermission()) {
                reportLocationError("لم يُمنح إذن الموقع", "الإعدادات")
                return
            }
            val point = provider.currentLocation()
            if (point == null) {
                reportLocationError("تعذّر الحصول على إحداثيات الجهاز")
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
                        locationSource = "gps",
                        locationResolved = true
                    )
                }
                showToast("تم تحديد الموقع: ${nearest.city.name}", ToastKind.LOCATION)
            } else {
                reportLocationError("لم تُطابَق المدينة من القاعدة الأوفلاين")
            }
        } finally {
            locating = false
            notifyStateChanged()
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
        notifyStateChanged()
        try {
            val ip = fetchIpLocation()
            if (ip == null) {
                reportLocationError("تعذّر استشعار الموقع عبر IP — تحقق من اتصال الإنترنت")
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
                    locationSource = "ip",
                    locationResolved = true
                )
            }
            showToast("تم تحديد الموقع عبر IP: $cityName", ToastKind.LOCATION)
        } finally {
            locating = false
            notifyStateChanged()
        }
    }

    /**
     * الاكتشاف الذكي التلقائي للمدينة عند التشغيل (أول تثبيت أو عند بقاء القيمة الافتراضية
     * بسبب انقطاع الإنترنت سابقاً):
     * - إذا سبق حفظ مدينة المستخدم (يدوياً / GPS / IP) وكان `locationResolved = true` -> يتوقف فوراً ولا يعيد الفحص أبداً.
     * - إذا كان `locationResolved = false` -> يجرب الاتصال بصمت بـ IP، وعند النجاح (حتى لو تطابق مع كربلاء)
     *   يحفظ المدينة ويثبّت `locationResolved = true` في الذاكرة الدائمة للأبد.
     * - عند غياب الإنترنت -> يبقى على القيمة الافتراضية دون كراش أو إزعاج، ليعيد المحاولة تلقائياً في الفتح القادم.
     */
    suspend fun autoDetectLocationOnStartup() {
        if (settings.locationResolved) return
        val ip = runCatching { fetchIpLocation() }.getOrNull() ?: return
        // تأكيد أن المستخدم لم يغيّر المدينة يدوياً أثناء انتظار استجابة الشبكة
        if (settings.locationResolved) return
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
                locationSource = "ip",
                locationResolved = true
            )
        }
        showToast("تم تحديد مدينتك تلقائياً: $cityName", ToastKind.LOCATION)
    }

    /** يُسجّل الخطأ في الحالة ويُظهره ككبسولة عائمة — لا إغلاق للتطبيق أبدًا. */
    private fun reportLocationError(message: String, actionLabel: String? = null) {
        lastLocationError = message
        showToast(message, ToastKind.ERROR, actionLabel)
    }

    /**
     * مزامنة صامتة لحالتبل الموقع في النظام — بلا واجهة وبلا toast:
     * تُستدعى عند عودة التطبيق للمقدمة، فإن صار الموقع مفعّلًا تزيل العلامة
     * والتنبيه القديم حتى لا تبقى كبسولة «خدمة الموقع معطّلة» معلقة بعد تفعيله.
     * تعيد true إن كان الموقع مفعّلًا الآن.
     */
    fun syncLocationSystemState(): Boolean {
        val provider = runCatching { createLocationProvider() }.getOrNull() ?: return false
        val enabled = provider.isLocationEnabled()
        if (enabled && locationServiceDisabled) {
            locationServiceDisabled = false
            lastLocationError = null
            if (toast?.kind == ToastKind.ERROR) toast = null
            notifyStateChanged()
        }
        return enabled
    }

    /** يفتح إعدادات خدمة الموقع في النظام (أندرويد) عقب كشف تعطيلها. */
    fun openSystemLocationSettings() {
        runCatching { createLocationProvider().openLocationSettings() }
    }

    fun clearLocationError() {
        lastLocationError = null
        locationServiceDisabled = false
        notifyStateChanged()
    }

    /** تهيئة محتوى مفاتيح الجنان — تُستدعى مرة واحدة من شاشة «المفاتيح». */
    private val jinanMutex = Mutex()

    suspend fun prepareJinanContent() {
        if (jinanReady) return
        // Single-flight: كل من يصل أثناء التحميل ينتظر النتيجة نفسها بدل
        // تشغيل فكّ JSON الضخم مرتين بالتوازي (سبب تجمّد الدخول المزدوج).
        jinanMutex.withLock {
            if (jinanReady) return
            runCatching {
                val bytes = com.karrar.amsakiah.resources.Res.readBytes("files/content_texts.json")
                com.karrar.amsakiah.core.jinan.JinanContentTexts.load(bytes)
                val quranBytes = com.karrar.amsakiah.resources.Res.readBytes("files/quran_texts.json")
                com.karrar.amsakiah.core.quran.QuranTexts.load(quranBytes)
                markJinanReady()
            }
        }
    }
}
