package com.Nightjar.amsakkarrar.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.karrar.amsakiah.core.audio.PlaybackCoordinator

/**
 * خدمة تشغيل الأذان في المقدمة — تعمل حتى لو كانت الشاشة مقفلة.
 *
 * مسار الصوت الحقيقي:
 *   AdhanService → AdhanPlayback.createPlayer → AndroidAudioSource
 *   → composeResources/files/adhan.mp3 (النسخة المشتركة مع iOS)
 * ويوجد مسار احتياطي عبر قناة إشعار تحمل صوت الأذان المخصص نفسه إذا فشل
 * تشغيل الخدمة (مثلاً منع النظام تشغيل خدمة أمامية في الخلفية).
 *
 * يسجّل نفسه في [PlaybackCoordinator] فيتوقف تلقائيًا إذا بدأ المستخدم
 * مناجاة، ويوقف أي صوت آخر قبل أن يبدأ.
 */
class AdhanService : Service() {

    private val audioOwner = Any()
    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var audioManager: AudioManager? = null
    // Any? لتفادي تحميل AudioFocusRequest (API 26) على أجهزة 24/25 قبل الحاجة إليه.
    private var focusRequest: Any? = null
    private var preview = false

    /** الصلاة التي يعزف أذانها الآن — لمنع إعادة التشغيل من الصفر. */
    private var currentPrayer: String? = null

    private val handler by lazy { Handler(Looper.getMainLooper()) }

    /**
     * حرس وقت: يوقف الخدمة بعد انتهاء مدة الملف + هامش، فلا يبقى إشعار أو
     * صوت عالقًا حتى لو فشل `setOnCompletionListener` (بعض الأجهزة لا تناديه
     * عند إخراج الصوت من مسار ALARM).
     */
    private val watchdog = Runnable { stopPlayback() }

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

        // إعادة إرسال الأمر نفسه (منبه مكرر أو إعادة تشغيل النظام للخدمة)
        // يجب ألا تقطع الأذان الجاري وتعيده من الثانية صفر.
        if (!preview && currentPrayer == prayer && player?.isPlaying == true) {
            // نؤكد حالة الخدمة الأمامية فقط (مطلوب بعد كل startForegroundService).
            startForegroundSafely(prayer, preview)
            return START_NOT_STICKY
        }

        if (!startForegroundSafely(prayer, preview)) {
            // بلا خدمة أمامية قد يقتل النظام الصوت بلا إشعار — الأفضل الاحتياط.
            PrayerNotifier.showAdhanFallback(this, prayer)
            stopPlayback()
            return START_NOT_STICKY
        }

        if (vibrate && !preview) startVibration()
        startPlayback(prayer)

        return START_NOT_STICKY
    }

    private fun startPlayback(prayer: String) {
        // أي تشغيل سابق في نفس الخدمة (معاينة أثناء أذان) يُحرّر أولًا:
        // وإلا بقي MediaPlayer القديم يعزف مع الجديد.
        releasePlayer()
        // صوت واحد فقط: أوقف المناجاة أو أي مشغّل آخر قبل الأذان.
        PlaybackCoordinator.requestPlayback(audioOwner)
        PlaybackCoordinator.register(audioOwner) { stopPlayback() }

        val mp = AdhanPlayback.createPlayer(
            context = this,
            name = AdhanPlayback.ADHAN,
            // USAGE_MEDIA (وليس ALARM): زر الصوت في الهاتف هو الذي يتحكم،
            // وكتم النظام/الصامت يُحترم — وهو المطلوب صراحةً.
            usage = AudioAttributes.USAGE_MEDIA
        )
        if (mp == null) {
            // لم نستطع فتح الملف — نعرض إشعارًا بقناة تحمل الصوت المخصص نفسه.
            PrayerNotifier.showAdhanFallback(this, prayer)
            stopPlayback()
            return
        }

        // احترام وضع صوت النظام: الصامت/الكتم لا يُشغّل صوتًا، والاهتزاز فقط إن طُلب.
        // (الطلب صريح: كتم الهاتف يُكتم معه الأذان، والاهتزاز لا يُصدر صوتًا.)
        val ringer = (getSystemService(Context.AUDIO_SERVICE) as? AudioManager)?.ringerMode
        if (ringer == AudioManager.RINGER_MODE_SILENT || ringer == AudioManager.RINGER_MODE_VIBRATE) {
            // لا صوت إطلاقًا: نُبقي البطاقة الصامتة فقط ولا نُشغل MediaPlayer.
            // في وضع الاهتزاز نُشغّل النمط إن لم يكن قد بدأ بعد.
            player = null
            runCatching { mp.release() }
            currentPrayer = prayer
            if (ringer == AudioManager.RINGER_MODE_VIBRATE && vibrator == null) startVibration()
            return
        }

        requestAudioFocus()
        player = mp
        currentPrayer = prayer
        mp.setOnCompletionListener { stopPlayback() }
        mp.setOnErrorListener { _, _, _ ->
            PrayerNotifier.showAdhanFallback(this, prayer)
            stopPlayback()
            true
        }

        runCatching { mp.start() }.onFailure {
            PrayerNotifier.showAdhanFallback(this, prayer)
            stopPlayback()
            return
        }

        // نجاح المسار الأساسي: أزل أي بطاقة احتياطية سبق نشرها حتى لا يبقى
        // إشعاران لنفس الحدث (كانت تظهر البطاقة الاحتياطية مع البطاقة الصامتة).
        PrayerNotifier.clearFallback(this)

        // حرس وقت: المدة الفعلية + 5 ثوانٍ كهامش.
        val durationMs = runCatching { mp.duration.toLong() }.getOrDefault(0L)
        handler.removeCallbacks(watchdog)
        if (durationMs > 0L) handler.postDelayed(watchdog, durationMs + 5_000L)
    }

    /**
     * إعلان الخدمة أمامية بنوع `mediaPlayback` صراحةً.
     *
     * أندرويد 14+ (وأشد في 16/17) يتحقق من النوع المعلن في البيان؛ تمريره
     * هنا عبر `ServiceCompat` يضمن عدم رمي `MissingForegroundServiceType`
     * ويوحّد السلوك من API 24 حتى 37+.
     */
    private fun startForegroundSafely(prayer: String, isPreview: Boolean): Boolean =
        runCatching {
            val notification = buildNotification(prayer, isPreview)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        }.isSuccess

    private fun requestAudioFocus() {
        runCatching {
            audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            // يجب أن يطابق مسار التشغيل: USAGE_MEDIA + STREAM_MUSIC.
            // كان ALARM هنا بينما التشغيل MEDIA — تعارض يجعل التركيز يُمنح
            // على مسار مختلف عن الصوت الفعلي فلا يُحترم كتم النظام.
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
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
                    AudioManager.STREAM_MUSIC,
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

    private fun releasePlayer() {
        runCatching {
            player?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        }
        player = null
    }

    private fun stopPlayback() {
        PlaybackCoordinator.unregister(audioOwner)
        handler.removeCallbacks(watchdog)
        currentPrayer = null
        releasePlayer()
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
        // ضمان إضافي: لا يبقى إشعار تشغيل بعد الإيقاف حتى لو تجاهله النظام.
        runCatching {
            getSystemService(NotificationManager::class.java)?.cancel(NOTIFICATION_ID)
        }
        stopSelf()
    }

    /**
     * بطاقة **واحدة** لكل حدث: خدمة الأذان تنشر بطاقة التشغيل بالمعرّف نفسه
     * [NotificationDesign.PRAYER_ID] الذي نشرته بطاقة الوصول، فتحل محلها بدل
     * أن تظهر بطاقتان متتاليتان (كان سابقًا 4001 + 3101).
     */
    private fun buildNotification(prayer: String, isPreview: Boolean): Notification {
        val snapshot = runCatching { PrayerSnapshotLoader.load(this) }.getOrNull()
        val entries = snapshot?.entries.orEmpty()
        val index = entries.indexOfFirst { it.label == prayer }.let { if (it < 0) 0 else it }
        val current = entries.getOrNull(index)?.time ?: snapshot?.nextTime ?: "--:--"
        val next = entries.getOrNull((index + 1) % entries.size.coerceAtLeast(1))

        return NotificationDesign.buildPlaying(
            context = this,
            prayerLabel = prayer,
            currentTime = current,
            nextLabel = next?.label ?: snapshot?.afterLabel.orEmpty(),
            nextTime = next?.time ?: snapshot?.afterTime ?: "--:--",
            preview = isPreview
        )
    }

    override fun onDestroy() {
        PlaybackCoordinator.unregister(audioOwner)
        handler.removeCallbacks(watchdog)
        currentPrayer = null
        runCatching { vibrator?.cancel() }
        vibrator = null
        runCatching {
            player?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
            player = null
        }
        abandonAudioFocus()
        // شبكة أمان: لا يبقى إشعار تشغيل بعد تدمير الخدمة بأي سبب.
        runCatching {
            getSystemService(NotificationManager::class.java)?.cancel(NOTIFICATION_ID)
        }
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // الحالة الوحيدة التي نوقف فيها الأذان عند إزالة المهمة: ألا يكون هناك
        // صوت فعلي (خدمة بلا مشغّل = إشعار عالق). الأذان الجاري يكمل حتى
        // نهايته ثم يوقف نفسه عبر onCompletion/الحرس.
        if (player?.isPlaying != true) stopPlayback()
        super.onTaskRemoved(rootIntent)
    }

    companion object {
        const val EXTRA_PRAYER = "prayer"
        const val EXTRA_VIBRATE = "vibrate"
        const val ACTION_STOP = "com.Nightjar.amsakkarrar.STOP_ADHAN"
        const val ACTION_PREVIEW = "com.Nightjar.amsakkarrar.PREVIEW_ADHAN"

        /** معرّف واحد موحّد مع بطاقة الوصول — بطاقة واحدة لكل حدث. */
        const val NOTIFICATION_ID = NotificationDesign.PRAYER_ID
    }
}
