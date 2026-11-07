package com.karrar.amsakiah.ui.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.karrar.amsakiah.core.audio.AudioPlayer
import com.karrar.amsakiah.core.audio.createAudioPlayer
import com.karrar.amsakiah.core.data.ShiaDuaItem

/**
 * متحكم مشغّل الأدعية.
 *
 * يربط الواجهة بالمشغّل الأصلي للنظام مباشرة:
 * - Android: android.media.MediaPlayer
 * - iOS: AVAudioPlayer
 *
 * الموضع والمدة يُقرآن من المشغّل الأصلي نفسه (currentPosition/duration)،
 * وليس من مؤقّت وهمي، لذلك شريط التقدم مطابق تمامًا لما يعزفه النظام.
 */
class DuaPlayerController {

    private val player: AudioPlayer = createAudioPlayer()
    private var resource: String? = null

    /** معرّف الدعاء الحالي (لعرض حالة التشغيل في شاشة التفاصيل) */
    var currentId by mutableStateOf<String?>(null)
        private set

    var title by mutableStateOf<String?>(null)
        private set

    var playing by mutableStateOf(false)
        private set

    var positionMs by mutableStateOf(0L)
        private set

    var durationMs by mutableStateOf(0L)
        private set

    /** هل النافذة العائمة موسّعة؟ */
    var expanded by mutableStateOf(false)
        private set

    val hasTrack: Boolean get() = title != null

    init {
        // عند انتهاء الملف الأصلي: تثبيت المؤشر في النهاية وإيقاف الحالة
        player.setOnCompletion {
            playing = false
            positionMs = durationMs.coerceAtLeast(0L)
        }
    }

    /** تشغيل دعاء، أو إيقاف/استئناف إذا كان هو نفسه الحالي */
    fun play(item: ShiaDuaItem) {
        val res = item.audioResource ?: return
        if (resource == res) {
            toggle()
            return
        }

        player.stop()
        resource = res
        currentId = item.id
        title = item.audioTitle ?: item.title
        positionMs = 0L
        durationMs = 0L

        player.play(res)
        playing = player.isPlaying()
        // المدة تُقرأ من المشغّل الأصلي بعد التجهيز مباشرة
        durationMs = player.durationMs().coerceAtLeast(0L)
        expanded = true
    }

    fun toggle() {
        if (resource == null) return
        if (playing) pause() else resume()
    }

    fun pause() {
        player.pause()
        playing = false
        sync()
    }

    fun resume() {
        if (resource == null) return
        player.resume()
        playing = player.isPlaying()
        sync()
    }

    /** الانتقال المباشر عبر المشغّل الأصلي (seekTo) */
    fun seekTo(position: Long) {
        if (resource == null) return
        val end = durationMs.coerceAtLeast(0L)
        val clamped = if (end > 0L) position.coerceIn(0L, end) else position.coerceAtLeast(0L)
        player.seekTo(clamped)
        positionMs = clamped
    }

    fun seekToFraction(fraction: Float) {
        if (durationMs <= 0L) return
        seekTo((fraction.coerceIn(0f, 1f) * durationMs).toLong())
    }

    /**
     * مزامنة الحالة مع المشغّل الأصلي — تُستدعى دوريًا كل 200ms أثناء التشغيل.
     * هذا هو مصدر الحقيقة الوحيد لشريط التقدم.
     */
    fun sync() {
        if (resource == null) return
        positionMs = player.positionMs()
        if (durationMs <= 0L) durationMs = player.durationMs().coerceAtLeast(0L)
        playing = player.isPlaying()

        if (durationMs > 0L && positionMs >= durationMs) {
            positionMs = durationMs
            playing = false
        }
    }

    fun showExpanded(value: Boolean) {
        expanded = value
    }

    fun close() {
        player.stop()
        resource = null
        currentId = null
        title = null
        playing = false
        positionMs = 0L
        durationMs = 0L
        expanded = false
    }

    fun release() {
        close()
    }
}
