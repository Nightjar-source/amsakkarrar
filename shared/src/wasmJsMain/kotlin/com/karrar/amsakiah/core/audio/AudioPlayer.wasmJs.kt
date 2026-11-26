package com.karrar.amsakiah.core.audio

actual fun createAudioPlayer(): AudioPlayer = object : AudioPlayer {
    override fun play(resourceName: String, loop: Boolean) = Unit
    override fun resume() = Unit
    override fun pause() = Unit
    override fun stop() = Unit
    override fun isPlaying(): Boolean = false
    override fun positionMs(): Long = 0L
    override fun seekTo(positionMs: Long) = Unit
    override fun durationMs(): Long = 0L
    override fun setOnCompletion(callback: () -> Unit) = Unit
}
