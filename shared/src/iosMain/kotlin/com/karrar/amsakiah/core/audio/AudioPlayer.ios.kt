package com.karrar.amsakiah.core.audio

class IosAudioPlayer : AudioPlayer {
    override fun playAsset(assetPath: String) {}
    override fun playUrl(url: String) {}
    override fun pause() {}
    override fun stop() {}
    override fun isPlaying(): Boolean = false
}

actual fun createAudioPlayer(): AudioPlayer = IosAudioPlayer()
