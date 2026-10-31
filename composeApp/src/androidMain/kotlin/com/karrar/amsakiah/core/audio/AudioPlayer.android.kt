package com.karrar.amsakiah.core.audio

import android.media.MediaPlayer

class AndroidAudioPlayer : AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null

    override fun playAsset(assetPath: String) {
        stop()
        // يمكن تشغيل ملفات raw أو assets
    }

    override fun playUrl(url: String) {
        stop()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener { start() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) it.pause()
        }
    }

    override fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
    }

    override fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false
}

actual fun createAudioPlayer(): AudioPlayer = AndroidAudioPlayer()
