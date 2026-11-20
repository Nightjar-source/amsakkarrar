package com.Nightjar.amsakkarrar.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * مستقبل «إيقاف الأذان» — يُستدعى من:
 *  1. زر «إيقاف الأذان» في بطاقة التشغيل.
 *  2. **سحب الإشعار** (deleteIntent) — وهو المطلوب صراحةً: سحب البطاقة يوقف
 *     الصوت فورًا ولا يبقى مشغّل عالقًا في شريط الحالة.
 *
 * سبب استخدام مستقبل بدل `PendingIntent.getService`:
 *  - `startService` من الخلفية مقيّد منذ أندرويد 8، وقد يرمي
 *    `BackgroundServiceStartNotAllowedException` على 12+ فيبقى الصوت يعمل.
 *  - `stopService` مسموح دائمًا من أي سياق، فيقطع الصوت ويُنزل الخدمة
 *    الأمامية (وعلى `onDestroy` تُلغى البطاقة).
 */
class AdhanStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        runCatching {
            context.stopService(Intent(context, AdhanService::class.java))
        }
        // شبكة أمان: لا تبقى بطاقة معلّقة إن كانت الخدمة قد أُوقفت مسبقًا.
        runCatching {
            context.getSystemService(NotificationManager::class.java)
                ?.cancel(NotificationDesign.PRAYER_ID)
        }
    }

    companion object {
        const val ACTION_STOP = "com.Nightjar.amsakkarrar.STOP_ADHAN_NOTIF"
    }
}
