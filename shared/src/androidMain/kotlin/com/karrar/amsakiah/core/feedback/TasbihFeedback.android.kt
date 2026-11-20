package com.karrar.amsakiah.core.feedback

import android.media.AudioManager
import android.media.ToneGenerator

private val toneGenerator: ToneGenerator? by lazy {
    runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 70) }.getOrNull()
}

actual fun playTasbihClick() {
    runCatching { toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 45) }
}

actual fun playTasbihComplete() {
    runCatching { toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 220) }
}
