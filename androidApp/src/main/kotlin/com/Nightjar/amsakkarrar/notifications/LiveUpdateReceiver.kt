package com.Nightjar.amsakkarrar.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.Nightjar.amsakkarrar.widget.WidgetUpdater

/**
 * تحديث دوري (كل 15 دقيقة) للإشعار الحي والودجت.
 * العدّاد التنازلي داخل الإشعار يعمل تلقائيًا عبر Chronometer،
 * وهذه التحديثات تُبقي شريط التقدم والعناصر النصية متزامنة.
 */
class LiveUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        runCatching { LiveNotificationManager.update(context) }
        val pending = goAsync()
        WidgetUpdater.updateAllAsync(context) { pending.finish() }
    }

    companion object {
        const val ACTION_REFRESH = "com.Nightjar.amsakkarrar.LIVE_REFRESH"
    }
}
