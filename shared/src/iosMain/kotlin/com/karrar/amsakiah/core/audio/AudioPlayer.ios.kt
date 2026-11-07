package com.karrar.amsakiah.core.audio

import com.karrar.amsakiah.resources.Res
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSURL

/**
 * مشغّل الصوت على iOS.
 *
 * الأولوية لقراءة الملف من Compose Resources (`Res.readBytes`) لأن ملفات
 * `composeResources/files` هي النسخة المشتركة المضمونة في حزمة التطبيق.
 * ثم نحاول `NSBundle` كاحتياط إن نسخ مشروع Xcode الملفات إلى الحزمة.
 * يُشغَّل الملف من الذاكرة مباشرة عبر AVAudioPlayer(data:error:) — بلا ملفات مؤقتة.
 */
class IosAudioPlayer : AudioPlayer {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var player: AVAudioPlayer? = null
    private var completion: (() -> Unit)? = null
    private var generation = 0

    override fun play(resourceName: String, loop: Boolean) {
        stop()
        val token = ++generation
        scope.launch {
            val p = createPlayer(resourceName, loop) ?: return@launch
            if (token != generation) return@launch
            runCatching {
                AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, null)
                AVAudioSession.sharedInstance().setActive(true, null)
                p.prepareToPlay()
                p.play()
                player = p
            }
        }
    }

    private suspend fun createPlayer(resourceName: String, loop: Boolean): AVAudioPlayer? {
        // 1) Compose Resources — الملفات المشتركة في commonMain/composeResources/files
        val bytes = runCatching { Res.readBytes("files/$resourceName.mp3") }.getOrNull()
        if (bytes != null && bytes.isNotEmpty()) {
            return runCatching {
                AVAudioPlayer(data = bytes.toNSData(), error = null).apply {
                    numberOfLoops = if (loop) -1 else 0
                }
            }.getOrNull()
        }
        // 2) احتياط: ملف داخل حزمة التطبيق (إن أضافه مشروع Xcode)
        val path = NSBundle.mainBundle.pathForResource(resourceName, ofType = "mp3") ?: return null
        return runCatching {
            AVAudioPlayer(contentsOfURL = NSURL.fileURLWithPath(path), error = null).apply {
                numberOfLoops = if (loop) -1 else 0
            }
        }.getOrNull()
    }

    override fun resume() {
        val p = player ?: return
        if (p.playing) return
        if (p.duration > 0.0 && p.currentTime >= p.duration - 0.25) p.currentTime = 0.0
        p.play()
    }

    override fun pause() {
        player?.pause()
    }

    override fun stop() {
        generation++
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

private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
}

actual fun createAudioPlayer(): AudioPlayer = IosAudioPlayer()
