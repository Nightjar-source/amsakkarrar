package com.Nightjar.amsakkarrar.notifications

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.Nightjar.amsakkarrar.MainActivity
import com.Nightjar.amsakkarrar.R

/**
 * خدمة تشغيل الأذان في المقدمة — تعمل حتى لو كانت الشاشة مقفلة.
 */
class AdhanService : Service() {

    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopPlayback()
            return START_NOT_STICKY
        }

        val prayer = intent?.getStringExtra(EXTRA_PRAYER) ?: "الصلاة"
        val vibrate = intent?.getBooleanExtra(EXTRA_VIBRATE, false) ?: false

        startForeground(NOTIFICATION_ID, buildNotification(prayer))

        if (vibrate) startVibration()
        playAdhan()

        return START_NOT_STICKY
    }

    private fun playAdhan() {
        stopPlayback()
        runCatching {
            // الملف موجود في الوحدة المشتركة (shared/src/androidMain/res/raw/adhan.mp3)
            val resId = resources.getIdentifier("adhan", "raw", packageName)
            if (resId == 0) return
            val afd = resources.openRawResourceFd(resId) ?: return
            val mp = MediaPlayer()
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            mp.isLooping = false
            mp.setOnCompletionListener {
                stopPlayback()
            }
            mp.prepare()
            mp.start()
            player = mp
        }
    }

    private fun startVibration() {
        runCatching {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            val pattern = longArrayOf(0, 500, 400, 500, 400, 800)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, -1)
            }
        }
    }

    private fun stopPlayback() {
        runCatching {
            player?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        }
        player = null
        runCatching { vibrator?.cancel() }
        vibrator = null
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
        }
        stopSelf()
    }

    private fun buildNotification(prayer: String): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            2001,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            2002,
            Intent(this, AdhanService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NotificationChannels.PLAYBACK)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("الأذان — $prayer")
            .setContentText("الله أكبر — تقبل الله طاعتكم")
            .setStyle(NotificationCompat.BigTextStyle().bigText("حان الآن موعد $prayer. الله أكبر — تقبل الله طاعتكم."))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(openIntent)
            .addAction(0, "إيقاف الأذان", stopIntent)
            .build()
    }

    override fun onDestroy() {
        runCatching {
            player?.release()
            player = null
        }
        super.onDestroy()
    }

    companion object {
        const val EXTRA_PRAYER = "prayer"
        const val EXTRA_VIBRATE = "vibrate"
        const val ACTION_STOP = "com.Nightjar.amsakkarrar.STOP_ADHAN"
        const val NOTIFICATION_ID = 4001
    }
}
