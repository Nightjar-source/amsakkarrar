package com.karrar.amsakiah.core.jinan

/**
 * كاش ذاكرة للمنقصة الشهرية — يُبنى مرة واحدة لكل (السنة/الشهر/الإزاحة الهجرية)
 * ثم يُعاد استخدامه في نفس الجلسة. «أعمال اليوم» والمفاتيح تحتاج النتيجة فورًا
 * (<1ms)، ولا أحد يحب «جارٍ فحص مفاتيح الجنان» تتكرر عند كل دخول أو تجميد.
 *
 * أول إقلاع بعد التثبيت: [App] يبني منقصة الشهر الحالي على خيط خلفي عبر
 * [StartupGate]، فإذا فتحت «أعمال اليوم» لاحقًا تصل في لحظتها، وإلا تظهر
 * الفراغات/الشاشة تصاعديًا أثناء البناء الخلفي بدون تجميد الواجهة.
 *
 * سقف 36 شهرًا (٣ سنوات) — ذاكرة صغيرة، ويمسح عند امتلائه ويُعاد من الصفر.
 */
import com.karrar.amsakiah.ui.components.platformSync

object MonthAlmanacCache {

    private const val MAX_ENTRIES = 36
    private val lock = Any()
    private val cache = mutableMapOf<String, MonthAlmanac>()

    private fun key(year: Int, month: Int, hijriOffset: Int) = "$year-$month-$hijriOffset"

    fun get(year: Int, month: Int, hijriOffset: Int): MonthAlmanac? = platformSync(lock) {
        cache[key(year, month, hijriOffset)]
    }

    fun put(almanac: MonthAlmanac, hijriOffset: Int) {
        platformSync(lock) {
            if (cache.size >= MAX_ENTRIES) cache.clear()
            cache[key(almanac.year, almanac.month, hijriOffset)] = almanac
        }
    }

    /** يُستدعى عند تغيير إزاحة الهجري — نتخلص من كل المدخلات القديمة. */
    fun clear() {
        platformSync(lock) { cache.clear() }
    }
}
