package com.Nightjar.amsakkarrar.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.Nightjar.amsakkarrar.R

object NotificationChannels {

    const val ADHAN = "adhan_channel"
    const val REMINDER = "prayer_reminder"
    const val LIVE = "live_countdown"
    const val PLAYBACK = "adhan_playback"

    fun createAll(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        val adhan = NotificationChannel(ADHAN, "الأذان", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "صوت الأذان عند دخول وقت الصلاة"
            // الصوت يُشغَّل عبر AdhanService (ملف adhan.mp3 أوفلاين) — القناة صامتة لتفادي التكرار
            enableVibration(true)
            setSound(null, null)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }

        val reminder = NotificationChannel(REMINDER, "تذكير قبل الصلاة", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "تذكير قبل دخول وقت الصلاة"
            setSound(null, null)
        }

        val live = NotificationChannel(LIVE, "العدّاد الحي", NotificationManager.IMPORTANCE_LOW).apply {
            description = "إشعار مستمر يعرض الوقت المتبقي للصلاة القادمة"
            setSound(null, null)
            setShowBadge(false)
        }

        val playback = NotificationChannel(PLAYBACK, "تشغيل الأذان", NotificationManager.IMPORTANCE_LOW).apply {
            description = "إشعار التحكم بتشغيل الأذان"
            setSound(null, null)
        }

        manager.createNotificationChannels(listOf(adhan, reminder, live, playback))
    }
}
