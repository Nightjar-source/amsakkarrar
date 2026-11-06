package com.karrar.amsakiah.core.audio

import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSBundle
import platform.Foundation.NSURL

class IosAudioPlayer : AudioPlayer {

    private var player: AVAudioPlayer? = null
    private var completion: (() -> Unit)? = null

    override fun play(resourceName: String, loop: Boolean) {
        stop()
        val path = NSBundle.mainBundle.pathForResource(resourceName, ofType = "mp3") ?: return
        val url = NSURL.fileURLWithPath(path)
        runCatching {
            AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, null)
            AVAudioSession.sharedInstance().setActive(true, null)
            val p = AVAudioPlayer(contentsOfURL = url, error = null)
            p.numberOfLoops = if (loop) -1 else 0
            p.prepareToPlay()
            p.play()
            player = p
        }
    }

    override fun pause() {
        player?.pause()
    }

    override fun stop() {
        player?.stop()
        player = null
    }

    override fun isPlaying(): Boolean = player?.playing ?: false

    override fun positionMs(): Long = ((player?.currentTime ?: 0.0) * 1000).toLong()

    override fun seekTo(positionMs: Long) {
        player?.currentTime = positionMs.coerceAtLeast(0L) / 1000.0
    }

    override fun durationMs(): Long = ((player?.duration ?: 0.0) * 1000).toLong()

    override fun setOnCompletion(callback: () -> Unit) {
        completion = callback
    }
}

actual fun createAudioPlayer(): AudioPlayer = IosAudioPlayer()
