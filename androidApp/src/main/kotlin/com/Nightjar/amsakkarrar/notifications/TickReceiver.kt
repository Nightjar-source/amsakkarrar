package com.Nightjar.amsakkarrar.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.Nightjar.amsakkarrar.widget.WidgetUpdater

/**
 * تحديث لحظي: يبث النظام TIME_TICK كل دقيقة، وDATE_CHANGED/TIME_SET (TIME_CHANGED)
 * وTIMEZONE_CHANGED عند تغيير التاريخ/الساعة/المنطقة — يُحدِّث الودجت والإشعار
 * الحي فورًا (بدون كاش وبلا انتظار 15 د).
 */
class TickReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_TIME_TICK &&
            action != Intent.ACTION_DATE_CHANGED &&
            action != Intent.ACTION_TIME_CHANGED &&
            action != Intent.ACTION_TIMEZONE_CHANGED
        ) return
        WidgetUpdater.updateAllAsync(context.applicationContext) { }
    }
}
