package com.karrar.amsakiah.core.audio

/**
 * مشغل الصوت الأصلي — يشغّل الأذان والمناجاة من موارد التطبيق (أوفلاين).
 */
interface AudioPlayer {
    /** يشغّل ملفًا من res/raw بالاسم بدون الامتداد، مثال: "adhan" */
    fun play(resourceName: String, loop: Boolean = false)
    /** يستأنف من موضع الإيقاف المؤقت (وليس من البداية) */
    fun resume()
    fun pause()
    fun stop()
    fun isPlaying(): Boolean
    /** موضع التشغيل الحالي بالمللي ثانية */
    fun positionMs(): Long
    /** الانتقال إلى موضع محدد بالمللي ثانية */
    fun seekTo(positionMs: Long)
    /** مدة الملف الحالي بالمللي ثانية */
    fun durationMs(): Long
    fun setOnCompletion(callback: () -> Unit)
}

expect fun createAudioPlayer(): AudioPlayer
