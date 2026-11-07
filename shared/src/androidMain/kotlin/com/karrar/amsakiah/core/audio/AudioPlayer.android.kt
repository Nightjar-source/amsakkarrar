package com.karrar.amsakiah.core.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import com.karrar.amsakiah.core.AndroidAppContext

class AndroidAudioPlayer : AudioPlayer {

    private var player: MediaPlayer? = null
    private var completion: (() -> Unit)? = null

    override fun play(resourceName: String, loop: Boolean) {
        stop()
        val ctx = AndroidAppContext.optional() ?: return
        val resId = ctx.resources.getIdentifier(resourceName, "raw", ctx.packageName)
        if (resId == 0) return

        runCatching {
            val mp = MediaPlayer()
            val afd = ctx.resources.openRawResourceFd(resId) ?: return
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mp.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
            }
            mp.isLooping = loop
            mp.setOnCompletionListener {
                if (!loop) completion?.invoke()
            }
            mp.prepare()
            mp.start()
            player = mp
        }.onFailure { player = null }
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
        runCatching {
            player?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
        }
        player = null
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
