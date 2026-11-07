package com.Nightjar.amsakkarrar.notifications

import android.app.NotificationManager
import android.content.Context

/**
 * إدارة الإشعار الحي المستمر «رمضان مباشر».
 *
 * يُحدَّث عند فتح التطبيق، وكل 15 دقيقة عبر [LiveUpdateReceiver]،
 * وعند دخول وقت صلاة. العدّاد نفسه يتحدّث تلقائيًا داخل الإشعار
 * بواسطة Chronometer (لا حاجة لخدمة أمامية).
 */
object LiveNotificationManager {

    fun update(context: Context) {
        val appContext = context.applicationContext
        // القنوات قد لا تكون موجودة إذا نُودي من مستقبل/ودجت قبل أول فتح للتطبيق.
        NotificationChannels.createAll(appContext)
        val manager = appContext.getSystemService(NotificationManager::class.java) ?: return
        val settings = SettingsLoader.load(appContext)

        if (!settings.liveNotification) {
            cancel(appContext)
            return
        }

        val snapshot = PrayerSnapshotLoader.load(appContext)
        runCatching {
            manager.notify(
                NotificationDesign.LIVE_ID,
                NotificationDesign.buildLive(appContext, snapshot)
            )
        }
    }

    fun cancel(context: Context) {
        val manager = context.applicationContext.getSystemService(NotificationManager::class.java)
        runCatching { manager?.cancel(NotificationDesign.LIVE_ID) }
    }
}
