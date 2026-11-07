package com.karrar.amsakiah.core.audio

/**
 * جسر تشخيص الصوت بين الواجهة المشتركة وأندرويد.
 *
 * الشاشة المشتركة لا تعرف خدمة الأذان، فيسجّل `MainActivity` معالجًا
 * يشغّل `AdhanService` بنفس مسار الصوت الحقيقي
 * (`composeResources/files/adhan.mp3` عبر منسّق التشغيل الموحّد).
 * على iOS يبقى غير مدعوم وتُستخدم أزرار المشغّل الأصلية هناك.
 */
object SoundTestBridge {

    var supported: Boolean = false
        private set

    private var playHandler: (() -> Boolean)? = null
    private var liveHandler: (() -> Boolean)? = null

    fun install(
        play: (() -> Boolean)?,
        showLive: (() -> Boolean)? = null,
        isSupported: Boolean = true
    ) {
        playHandler = play
        liveHandler = showLive
        supported = isSupported
    }

    /** يشغّل ملف الأذان المخصص عبر نفس خدمة الأذان الحقيقية. */
    fun playAdhan(): Boolean = playHandler?.invoke() ?: false

    /** يعيد نشر الإشعار الحي فورًا للتحقق من تصميمه. */
    fun showLiveNotification(): Boolean = liveHandler?.invoke() ?: false
}
