package com.karrar.amsakiah.core.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import com.karrar.amsakiah.core.AndroidAppContext

/**
 * تنفيذ أندرويد لمشغّل الصوت — يقرأ من موارد Compose المشتركة عبر
 * [AndroidAudioSource]، ويسجّل نفسه في [PlaybackCoordinator] كي لا يتزامن
 * مع الأذان أو أي مشغّل آخر.
 */
class AndroidAudioPlayer : AudioPlayer {

    private var player: MediaPlayer? = null
    private var completion: (() -> Unit)? = null
    private val owner = Any()

    /** إيقاف داخلي لا يزيل التسجيل (يُستدعى من المنسّق أو من play). */
    private fun stopInternal() {
        runCatching {
            player?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        }
        player = null
    }

    override fun play(resourceName: String, loop: Boolean) {
        val ctx = AndroidAppContext.optional() ?: return

        // صوت واحد فقط: أوقف الأذان/أي مشغّل آخر قبل البدء.
        PlaybackCoordinator.requestPlayback(owner)
        stopInternal()

        val mp = AndroidAudioSource.createPlayer(
            context = ctx,
            name = resourceName,
            usage = AudioAttributes.USAGE_MEDIA
        ) ?: return

        mp.isLooping = loop
        mp.setOnCompletionListener {
            if (!loop) completion?.invoke()
            PlaybackCoordinator.unregister(owner)
        }

        if (runCatching { mp.start() }.isFailure) {
            runCatching { mp.release() }
            return
        }

        player = mp
        PlaybackCoordinator.register(owner) { stopInternal() }
    }

    override fun resume() {
        runCatching {
            val mp = player ?: return
            if (mp.isPlaying) return
            // إذا انتهى الملف نعيده للبداية، وإلا نكمل من نفس الموضع
            val duration = mp.duration
            if (duration > 0 && mp.currentPosition >= duration - 250) mp.seekTo(0)
            mp.start()
        }
    }

    override fun pause() {
        runCatching { player?.takeIf { it.isPlaying }?.pause() }
    }

    override fun stop() {
        PlaybackCoordinator.unregister(owner)
        stopInternal()
    }

    override fun isPlaying(): Boolean = runCatching { player?.isPlaying == true }.getOrDefault(false)

    override fun positionMs(): Long = runCatching {
        player?.takeIf { it.isPlaying || it.currentPosition > 0 }?.currentPosition?.toLong() ?: 0L
    }.getOrDefault(0L)

    override fun seekTo(positionMs: Long) {
        runCatching { player?.seekTo(positionMs.coerceAtLeast(0L).toInt()) }
    }

    override fun durationMs(): Long = runCatching { player?.duration?.toLong() ?: 0L }.getOrDefault(0L)

    override fun setOnCompletion(callback: () -> Unit) {
        completion = callback
    }
}

actual fun createAudioPlayer(): AudioPlayer = AndroidAudioPlayer()
