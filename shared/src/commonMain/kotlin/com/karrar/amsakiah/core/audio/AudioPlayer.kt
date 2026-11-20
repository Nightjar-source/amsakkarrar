package com.karrar.amsakiah.core.audio

/**
 * مشغل الصوت الأصلي — يشغّل الأذان والمناجاة من موارد التطبيق (أوفلاين).
 *
 * المصدر الموحّد على كل المنصات:
 * `shared/src/commonMain/composeResources/files/<name>.mp3`
 * (على أندرويد عبر [AndroidAudioSource]، وعلى iOS عبر `Res.readBytes`).
 */
interface AudioPlayer {
    /** اسم الملف في `composeResources/files` بدون الامتداد، مثال: "adhan" */
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
