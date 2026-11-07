package com.Nightjar.amsakkarrar.notifications

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
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
 *
 * مسار الصوت الحقيقي:
 *   AdhanService → AdhanPlayback.createPlayer → res/raw/adhan.mp3
 * ويوجد مسار احتياطي عبر قناة إشعار تحمل صوت الأذان المخصص نفسه إذا فشل
 * تشغيل الخدمة (مثلاً منع النظام تشغيل خدمة أمامية في الخلفية).
 */
class AdhanService : Service() {

    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var audioManager: AudioManager? = null
    // Any? لتفادي تحميل AudioFocusRequest (API 26) على أجهزة 24/25 قبل الحاجة إليه.
    private var focusRequest: Any? = null
    private var preview = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopPlayback()
            return START_NOT_STICKY
        }

        val prayer = intent?.getStringExtra(EXTRA_PRAYER) ?: "الصلاة"
        val vibrate = intent?.getBooleanExtra(EXTRA_VIBRATE, false) ?: false
        preview = intent?.action == ACTION_PREVIEW

        // إنشاء القنوات مضمون حتى لو بدأت الخدمة قبل فتح التطبيق.
        NotificationChannels.createAll(this)
        startForegroundSafely(prayer, preview)

        if (vibrate && !preview) startVibration()
        startPlayback(prayer)

        return START_NOT_STICKY
    }

    private fun startPlayback(prayer: String) {
        val mp = AdhanPlayback.createPlayer(
            context = this,
            name = AdhanPlayback.ADHAN,
            usage = AudioAttributes.USAGE_ALARM
        )
        if (mp == null) {
            // لم نستطع فتح الملف — نعرض إشعارًا بقناة تحمل الصوت المخصص نفسه.
            PrayerNotifier.showAdhanFallback(this, prayer)
            stopPlayback()
            return
        }

        requestAudioFocus()
        player = mp
        mp.setOnCompletionListener { stopPlayback() }
        mp.setOnErrorListener { _, _, _ ->
            PrayerNotifier.showAdhanFallback(this, prayer)
            stopPlayback()
            true
        }

        runCatching { mp.start() }.onFailure {
            PrayerNotifier.showAdhanFallback(this, prayer)
            stopPlayback()
        }
    }

    private fun startForegroundSafely(prayer: String, isPreview: Boolean) {
        runCatching {
            startForeground(NOTIFICATION_ID, buildNotification(prayer, isPreview))
        }
    }

    private fun requestAudioFocus() {
        runCatching {
            audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(attributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setOnAudioFocusChangeListener { }
                    .build()
                focusRequest = request
                audioManager?.requestAudioFocus(request)
            } else {
                @Suppress("DEPRECATION")
                audioManager?.requestAudioFocus(
                    { },
                    AudioManager.STREAM_ALARM,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                )
            }
        }
    }

    private fun abandonAudioFocus() {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                (focusRequest as? AudioFocusRequest)?.let { audioManager?.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                audioManager?.abandonAudioFocus { }
            }
        }
        focusRequest = null
        audioManager = null
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
        abandonAudioFocus()
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

    private fun buildNotification(prayer: String, isPreview: Boolean): Notification {
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

        val title = if (isPreview) "تجربة صوت الأذان" else "الأذان — $prayer"
        return NotificationCompat.Builder(this, NotificationChannels.PLAYBACK)
            .setSmallIcon(R.drawable.ic_notif_small)
            .setContentTitle(title)
            .setContentText("الله أكبر — تقبل الله طاعتكم")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("حان الآن موعد $prayer. الله أكبر — تقبل الله طاعتكم.")
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(openIntent)
            .addAction(0, "إيقاف الأذان", stopIntent)
            .build()
    }

    override fun onDestroy() {
        runCatching {
            player?.release()
            player = null
        }
        abandonAudioFocus()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_PRAYER = "prayer"
        const val EXTRA_VIBRATE = "vibrate"
        const val ACTION_STOP = "com.Nightjar.amsakkarrar.STOP_ADHAN"
        const val ACTION_PREVIEW = "com.Nightjar.amsakkarrar.PREVIEW_ADHAN"
        const val NOTIFICATION_ID = 4001
    }
}
