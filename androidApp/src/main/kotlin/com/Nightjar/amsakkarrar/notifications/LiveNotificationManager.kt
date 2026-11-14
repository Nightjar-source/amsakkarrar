package com.Nightjar.amsakkarrar.notifications

import android.app.NotificationManager
import android.content.Context

/**
 * إدارة الإشعار الحي المستمر «العدّاد المباشر».
 *
 * يُحدَّث عند فتح التطبيق، وكل 15 دقيقة عبر WorkManager، وعند دخول وقت
 * صلاة. العدّاد نفسه يتحدّث تلقائيًا داخل الإشعار بواسطة Chronometer
 * (لا حاجة لخدمة أمامية).
 *
 * عند سحب المستخدم للإشعار نتوقف عن إعادة نشره حتى دخول وقت الصلاة
 * القادم (أو حتى يفعّل الخيار من جديد) — تطبيقًا لتوصية أندرويد بعدم
 * إعادة نشر الإشعارات الحية التي أغلقها المستخدم.
 */
object LiveNotificationManager {

    private const val PREFS = "karar_live_notification"
    private const val KEY_DISMISSED_UNTIL = "dismissed_until"
    private const val KEY_WAS_ENABLED = "was_enabled"

    /**
     * بصمة آخر محتوى نُشر. إعادة نشر نفسه كلما فتح المستخدم التطبيق أو تغيّر
     * إعداد كانت تُعيد رسم البطاقة (وإحساسًا بالإشعار مرتين). هنا نضمن أن
     * الإشعار يُعاد نشره فقط عندما يتغيّر المحتوى فعلًا (الصلاة، الوقت،
     * أو تغيّر النسبة بمقدار ملموس).
     */
    @Volatile
    private var lastSignature: String? = null

    /**
     * @param force أظهر الإشعار حتى لو سحبه المستخدم سابقًا. يُستخدم عند
     *   اختبار الإشعار من الضبط أو عند دخول وقت صلاة جديدة.
     */
    fun update(context: Context, force: Boolean = false) {
        val appContext = context.applicationContext
        // القنوات قد لا تكون موجودة إذا نُودي من مستقبل/ودجت قبل أول فتح للتطبيق.
        NotificationChannels.createAll(appContext)
        val manager = appContext.getSystemService(NotificationManager::class.java) ?: return
        val settings = SettingsLoader.load(appContext)
        val prefs = prefs(appContext)

        if (!settings.liveNotification && !force) {
            lastSignature = null
            prefs.edit()
                .putBoolean(KEY_WAS_ENABLED, false)
                .putLong(KEY_DISMISSED_UNTIL, 0L)
                .apply()
            cancelInternal(appContext)
            return
        }

        if (!settings.liveNotification && force) {
            // معاينة من الإعدادات: انشر فورًا بغض النظر عن المفتاح (حتى ولو كان مطفأ)،
            // ولا تُسجّل «سحبًا» أو تغيّر حالة «مطفأ» — هي تجربة بصرية لا بث دائم.
            val snapshot = PrayerSnapshotLoader.load(appContext)
            runCatching {
                manager.notify(
                    NotificationDesign.LIVE_ID,
                    NotificationDesign.buildLive(appContext, snapshot)
                )
            }
            return
        }

        // التبديل من «مطفأ» إلى «مشغّل» يعيد الإشعار فورًا.
        val wasEnabled = prefs.getBoolean(KEY_WAS_ENABLED, true)
        if (!wasEnabled) clearDismissed(prefs)
        prefs.edit().putBoolean(KEY_WAS_ENABLED, true).apply()

        if (!force && isDismissed(prefs)) return

        val snapshot = PrayerSnapshotLoader.load(appContext)
        val percent = (snapshot.periodProgress * 100f).toInt()
        val remaining = snapshot.remainingSeconds
        // بصمة المحتوى: لا نُعيد النشر إذا لم يتغيّر شيء يُرى في البطاقة.
        val signature = buildString {
            append(snapshot.nextLabel)
            append('|')
            append(snapshot.nextTime)
            append('|')
            append(percent)
            append('|')
            append(remaining / 30) // يتغيّر كل 30 ثانية فقط
        }
        if (!force && signature == lastSignature) return
        lastSignature = signature

        clearDismissed(prefs)
        runCatching {
            manager.notify(
                NotificationDesign.LIVE_ID,
                NotificationDesign.buildLive(appContext, snapshot)
            )
        }
    }

    /** إخفاء الإشعار نهائيًا (عند إيقاف الخيار من الإعدادات). */
    fun cancel(context: Context) {
        val appContext = context.applicationContext
        lastSignature = null
        clearDismissed(prefs(appContext))
        cancelInternal(appContext)
    }

    /**
     * سجّل إخفاء المستخدم للإشعار حتى دخول وقت الصلاة القادم.
     * الحد الأقصى 24 ساعة (حتى لا يبقى مخفيًا يومًا كاملًا لو تغيّرت الأوقات).
     */
    fun onUserDismissed(context: Context) {
        val appContext = context.applicationContext
        val now = System.currentTimeMillis()
        val until = runCatching {
            val remainingMs = PrayerSnapshotLoader.load(appContext).remainingSeconds * 1000L
            now + remainingMs.coerceIn(60_000L, 24 * 60 * 60 * 1000L)
        }.getOrDefault(now + 60 * 60 * 1000L)
        prefs(appContext).edit().putLong(KEY_DISMISSED_UNTIL, until).apply()
    }

    private fun cancelInternal(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        runCatching { manager?.cancel(NotificationDesign.LIVE_ID) }
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun isDismissed(prefs: android.content.SharedPreferences): Boolean =
        prefs.getLong(KEY_DISMISSED_UNTIL, 0L) > System.currentTimeMillis()

    private fun clearDismissed(prefs: android.content.SharedPreferences) {
        if (prefs.getLong(KEY_DISMISSED_UNTIL, 0L) != 0L) {
            prefs.edit().putLong(KEY_DISMISSED_UNTIL, 0L).apply()
        }
    }
}
