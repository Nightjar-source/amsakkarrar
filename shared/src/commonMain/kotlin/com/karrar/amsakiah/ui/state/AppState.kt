package com.karrar.amsakiah.ui.state

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
import com.karrar.amsakiah.core.settings.ThemeMode
import com.karrar.amsakiah.core.state.SettingsChangeBus
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

/**
 * نوع التنبيه العائم — تستخدمه الواجهة لاختيار اللون والأيقونة والحركة.
 *
 * الفصل هنا مقصود: النواة لا تعرف شيئًا عن الشكل، وتكتفي بإعلان «ماذا حدث»
 * (UDF: الحالة + الحدث)، بينما تختار الواجهة «كيف يُرسم».
 */
enum class ToastKind { INFO, ERROR, SUCCESS, LOCATION }

/**
 * تنبيه واحد عائم (كبسولة بيضاء أنيقة) — نموذج بيانات فقط، بلا أي رسم.
 *
 * @param id معرّف فريد يمنع إعادة استخدام نفس المفتاح عند تكرار الرسالة.
 * @param text نص التنبيه (سطر واحد).
 * @param kind النوع: يحدد التأكيد اللوني في الواجهة.
 * @param actionLabel نص زر إجراء اختياري (مثل «تشغيل الموقع»).
 */
data class ToastMessage(
    val id: Long,
    val text: String,
    val kind: ToastKind,
    val actionLabel: String? = null
)

/**
 * حالة التطبيق المركزية — مصدر الحقيقة الوحيد لكل الشاشات.
 *
 * **معمارية UDF:** كل ما هنا حالة قابلة للقراءة (state) ودوال أحداث (events)
 * تعدّل الحالة، ولا يوجد أي `@Composable` أو مرجع واجهة. الواجهة تقرأ الحالة
 * وترسل الأحداث فقط، لذلك يمكن اختبار المنطق بلا Compose وتشغيله على كل
 * المنصات بلا طبقة رسوميات.
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

    var jinanReady: Boolean by mutableStateOf(false)
        private set

    fun markJinanReady() {
        jinanReady = true
    }

    /** خدمة الموقع معطّلة في إعدادات النظام — تُعرض معها زر «تشغيل الموقع». */
    var locationServiceDisabled: Boolean by mutableStateOf(false)
        private set

    /**
     * التنبيه العائم الحالي (كبسولة بيضاء أنيقة) — حالة لا تملك أي رسم؛
     * يقرأها مضيف التنبيهات في الواجهة ويختار شكلها وحركتها.
     */
    var toast by mutableStateOf<ToastMessage?>(null)
        private set

    private var toastSeq = 0L

    /** حدث: إظهار تنبيه عائم جديد (معرّف متسلسل يمنع التكرار بنفس المفتاح). */
    fun showToast(text: String, kind: ToastKind = ToastKind.INFO, actionLabel: String? = null) {
        if (text.isBlank()) return
        toastSeq += 1
        toast = ToastMessage(toastSeq, text.trim(), kind, actionLabel)
    }

    /** حدث: إخفاء التنبيه الحالي. */
    fun dismissToast() {
        toast = null
    }

    /**
     * مزامنة التغييرات القادمة من خارج الواجهة (ودجت المسبحة، الإجراءات السريعة).
     * عند تعديل الإعدادات من الودجت تُحدَّث حالة التطبيق فورًا دون إعادة تشغيل.
     */
    private val externalSync: () -> Unit = {
        val stored = loadSettings()
        if (stored != settings) settings = stored
    }

    /**
     * تهيئة محتوى مفاتيح الجنان — تقرأ bytes JSON وتبني فهرسًا في الذاكرة.
     * يُستدعى مرة واحدة من شاشة «المفاتيح» عند فتح قسم الجنان أوفلاين.
     * نافع للذاكرة: 2.9MB ملف كاملًا لكن تم تحليله مرة فقط لكل عملية.
     */
    suspend fun prepareJinanContent() {
        if (jinanReady) return
        runCatching {
            val bytes = com.karrar.amsakiah.resources.Res.readBytes("files/content_texts.json")
            com.karrar.amsakiah.core.jinan.JinanContentTexts.load(bytes)
            markJinanReady()
        }
    }

    init {
        SettingsChangeBus.addListener(externalSync)
    }

    fun dispose() {
        SettingsChangeBus.removeListener(externalSync)
    }

    /**
     * تحميل الإعدادات مع ترحيل `darkTheme` القديم إلى [ThemeMode].
     *
     * الملفات المحفوظة قبل إضافة `themeMode` لا تحتوي المفتاح؛ إن كان
     * `darkTheme=true` (وهو الافتراضي القديم) نُرحّله إلى الوضع المظلم حتى لا
     * تتغيّر هوية المستخدمين القدامى فجأة، وإلا نتبع النظام.
     */
    private fun loadSettings(): AppSettings = runCatching {
        val raw = SettingsStorage.read() ?: return@runCatching null
        val decoded = json.decodeFromString<AppSettings>(raw)
        val hasThemeMode = runCatching {
            json.parseToJsonElement(raw).jsonObject.containsKey("themeMode")
        }.getOrDefault(false)
        val migrated = if (hasThemeMode) {
            decoded
        } else {
            // القيمة القديمة الافتراضية كانت darkTheme=true ⇒ الوضع المظلم.
            val wasDark = Regex("\"darkTheme\"\\s*:\\s*(true|false)")
                .find(raw)?.groupValues?.get(1) != "false"
            decoded.copy(themeMode = if (wasDark) ThemeMode.DARK else ThemeMode.SYSTEM)
        }
        // حذف البيانات القديمة المحفوظة: مفاتيح التذكير (إشعار ثالث أُلغي)
        // و`darkTheme` (استُبدل بـthemeMode وأُزيل من النموذج). نعيد كتابة
        // الملف نظيفًا مرة واحدة حتى لا تبقى أي بيانات ميتة عند المستخدم.
        val legacyKeys = listOf("reminderEnabled", "reminderPrayers", "reminderMinutes", "darkTheme")
        if (legacyKeys.any { raw.contains("\"$it\"") }) {
            runCatching { SettingsStorage.write(json.encodeToString(AppSettings.serializer(), migrated)) }
        }
        migrated
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
        locationServiceDisabled = false
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
                        locationSource = "gps"
                    )
                }
                showToast("تم تحديد الموقع: ${nearest.city.name}", ToastKind.LOCATION)
            } else {
                reportLocationError("لم تُطابَق المدينة من القاعدة الأوفلاين")
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
                    locationSource = "ip"
                )
            }
            showToast("تم تحديد الموقع عبر IP: $cityName", ToastKind.LOCATION)
        } finally {
            locating = false
        }
    }

    /** يُسجّل الخطأ في الحالة ويُظهره ككبسولة عائمة — لا إغلاق للتطبيق أبدًا. */
    private fun reportLocationError(message: String, actionLabel: String? = null) {
        lastLocationError = message
        showToast(message, ToastKind.ERROR, actionLabel)
    }

    /** يفتح إعدادات خدمة الموقع في النظام (أندرويد) عقب كشف تعطيلها. */
    fun openSystemLocationSettings() {
        runCatching { createLocationProvider().openLocationSettings() }
    }

    fun clearLocationError() {
        lastLocationError = null
        locationServiceDisabled = false
    }
}