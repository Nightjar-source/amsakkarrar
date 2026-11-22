package com.karrar.amsakiah.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.karrar.amsakiah.core.astronomy.NearbyCity
import com.karrar.amsakiah.core.astronomy.OfflineCity
import com.karrar.amsakiah.core.state.AppKernel

// الـUI facade تستورد أنواع التنبيه من النواة — يسمح ببقاء كود الشاشات كما هو.
typealias ToastKind = com.karrar.amsakiah.core.state.ToastKind
typealias ToastMessage = com.karrar.amsakiah.core.state.ToastMessage

/**
 * طبقة UI فقط: تستهلك من [AppKernel] (النواة التشغيلية بلا أي أثر للواجهات)
 * وتكشف حالة قابلة للقراءة بمفاتيح snapshot (Compose) لإعادة التركيب الذكية.
 */
class AppState {

    private val kernel = AppKernel()

    var settings by mutableStateOf(kernel.settings)
        private set

    var now by mutableStateOf(kernel.now)
        private set

    var nearby by mutableStateOf<NearbyCity?>(kernel.nearby)
        private set

    var cityCount by mutableStateOf(kernel.cityCount)
        private set

    var locating by mutableStateOf(kernel.locating)
        private set

    var lastLocationError by mutableStateOf(kernel.lastLocationError)
        private set

    var jinanReady by mutableStateOf(kernel.jinanReady)
        private set

    var locationServiceDisabled by mutableStateOf(kernel.locationServiceDisabled)
        private set

    var toast by mutableStateOf<ToastMessage?>(kernel.toast)
        private set

    /** حالة ظهور نافذة إذن الموقع الجغرافي الموحّدة عبر كافة الشاشات */
    var showLocationPrompt by mutableStateOf(false)

    init {
        // كل عدّة state→UI من داخل الـKernel تمر هنا — نُحدّث الحقول المقروءة من الواجهة.
        kernel.onStateChanged = { syncFromKernel() }
    }

    private fun syncFromKernel() {
        settings = kernel.settings
        now = kernel.now
        nearby = kernel.nearby
        cityCount = kernel.cityCount
        locating = kernel.locating
        lastLocationError = kernel.lastLocationError
        jinanReady = kernel.jinanReady
        locationServiceDisabled = kernel.locationServiceDisabled
        toast = kernel.toast
    }

    /** تفويض أحداث الأعمال إلى النواة — الواجهة لا تملك أي منطق تشغيلي. */
    fun tick() { kernel.tick() }
    fun update(transform: (com.karrar.amsakiah.core.settings.AppSettings) -> com.karrar.amsakiah.core.settings.AppSettings) { kernel.update(transform) }
    fun reset() { kernel.reset() }

    /** تهيئة قاعدة المدن (أوفلاين) */
    suspend fun prepareCities() { kernel.prepareCities() }

    /** اختيار مدينة يدويًا من القاعدة */
    fun selectCity(city: OfflineCity) { kernel.selectCity(city) }

    /** تحديد الموقع عبر GPS ثم مطابقة أقرب مدينة من القاعدة الأوفلاين */
    suspend fun detectLocation() { kernel.detectLocation() }

    /** استشعار الموقع عبر الشبكة (IP) */
    suspend fun detectLocationByIp() { kernel.detectLocationByIp() }

    /** أظهر تنبيهًا عائمًا */
    fun showToast(text: String, kind: ToastKind = ToastKind.INFO, actionLabel: String? = null) {
        kernel.showToast(text, kind, actionLabel)
    }

    /** اخفِ التنبيه الحالي */
    fun dismissToast() { kernel.dismissToast() }

    /** تزامن صامت لحالتبل الموقع في النظام */
    fun syncLocationSystemState(): Boolean = kernel.syncLocationSystemState()

    /** يفتح إعدادات خدمة الموقع في النظام (أندرويد) */
    fun openSystemLocationSettings() = kernel.openSystemLocationSettings()

    fun clearLocationError() = kernel.clearLocationError()

    /** تهيئة محتوى مفاتيح الجنان */
    suspend fun prepareJinanContent() { kernel.prepareJinanContent() }

    fun dispose() = kernel.dispose()
}
