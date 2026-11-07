package com.Nightjar.amsakkarrar.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import com.Nightjar.amsakkarrar.R

object NotificationChannels {

    const val ADHAN = "adhan_channel"
    const val REMINDER = "prayer_reminder"
    const val LIVE = "live_countdown"
    const val PLAYBACK = "adhan_playback"

    /** قناة احتياطية تحمل صوت adhan.mp3 نفسه إن تعذّر تشغيل AdhanService. */
    const val ADHAN_FALLBACK = "adhan_fallback_sound"

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

        val live = NotificationChannel(LIVE, "العدّاد الحي", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "إشعار مستمر يعرض العدّاد التنازلي للصلاة القادمة وشريط التقدم (متوافق مع Live Updates في أندرويد 16 وOne UI)"
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }

        val playback = NotificationChannel(PLAYBACK, "تشغيل الأذان", NotificationManager.IMPORTANCE_LOW).apply {
            description = "إشعار التحكم بتشغيل الأذان"
            setSound(null, null)
        }

        // صوت الأذان المخصص — يُستخدم فقط عند فشل تشغيل الخدمة،
        // حتى لا يتكرر الصوت مع MediaPlayer في الحالة الطبيعية.
        val fallbackSound = AdhanPlayback.soundUri(context)
        val fallback = NotificationChannel(
            ADHAN_FALLBACK,
            "الأذان (صوت احتياطي)",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "صوت adhan.mp3 المخصص عند تعذّر تشغيل خدمة الأذان"
            enableVibration(true)
            if (fallbackSound != null) {
                setSound(
                    fallbackSound,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
            }
        }

        manager.createNotificationChannels(listOf(adhan, reminder, live, playback, fallback))
    }
}
