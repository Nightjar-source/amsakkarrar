package com.Nightjar.amsakkarrar.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * يُستدعى عند سحب الإشعار الحي أو إخفائه من قبل النظام/المستخدم.
 *
 * الهدف: عدم إعادة نشر إشعار أغلقَه المستخدم كل 15 دقيقة. يُسجَّل وقت
 * الإخفاء حتى دخول وقت الصلاة القادم، ثم يعود الإشعار طبيعيًا في الفترة
 * الجديدة. لا يستهلك هذا المستقبل أي موارد: يعمل لحظيًا ثم ينتهي.
 */
class LiveDismissReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_DISMISS) {
            LiveNotificationManager.onUserDismissed(context.applicationContext)
        }
    }

    companion object {
        const val ACTION_DISMISS = "com.Nightjar.amsakkarrar.LIVE_DISMISSED"
    }
}
