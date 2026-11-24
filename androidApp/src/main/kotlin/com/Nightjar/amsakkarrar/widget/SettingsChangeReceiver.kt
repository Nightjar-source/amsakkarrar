package com.Nightjar.amsakkarrar.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.Nightjar.amsakkarrar.notifications.LiveNotificationManager

/**
 * مستقبل بث تغيير الإعدادات أو المدينة لحظيًا.
 * يحدث الودجتات والإشعار الحي فور قيام المستخدم بتغيير المدينة من داخل التطبيق
 * حتى لو كان التطبيق مغلقاً أو يعمل في الخلفية.
 */
class SettingsChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_SETTINGS_CHANGED || intent.action == Intent.ACTION_LOCALE_CHANGED) {
            val pendingResult = goAsync()
            LiveNotificationManager.update(context)
            WidgetUpdater.updateAllAsync(context) {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_SETTINGS_CHANGED = "com.Nightjar.amsakkarrar.ACTION_SETTINGS_CHANGED"
    }
}
