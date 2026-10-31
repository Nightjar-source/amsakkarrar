package com.karrar.amsakiah.core.audio

interface AudioPlayer {
    fun playAsset(assetPath: String)
    fun playUrl(url: String)
    fun pause()
    fun stop()
    fun isPlaying(): Boolean
}

expect fun createAudioPlayer(): AudioPlayer
