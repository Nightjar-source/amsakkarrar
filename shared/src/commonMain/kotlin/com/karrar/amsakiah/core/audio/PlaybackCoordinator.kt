package com.karrar.amsakiah.core.audio

/**
 * منسّق تشغيل صوتي واحد على مستوى التطبيق.
 *
 * لماذا؟
 * - الأذان (AdhanService) والمناجاة (DuaPlayerController) مشغّلان مستقلان؛
 *   بدون قفل قد يعملان معًا فيتداخل الصوتان.
 * - عند بدء تشغيل جديد نوقف كل مشغّل آخر أولًا، تمامًا كما تفعل تطبيقات
 *   الوسائط: مالك واحد للصوت في أي لحظة.
 *
 * طريقة الاستخدام:
 * 1) قبل التشغيل: [requestPlayback] لإيقاف المالك السابق.
 * 2) بعد نجاح التشغيل: [register] لتسجيل دالة إيقاف تُستدعى لاحقًا.
 * 3) عند الإيقاف الطبيعي: [unregister].
 *
 * ملاحظة: يُستدعى من الخيط الرئيسي فقط (MediaPlayer/AVAudioPlayer يعيدان
 * النداءات على خيط الإنشاء)، لذلك لا يحتاج أقفالًا إضافية.
 */
object PlaybackCoordinator {

    private val stoppers = LinkedHashMap<Any, () -> Unit>()

    /** يسجّل مالكًا جديدًا مع دالة الإيقاف الخاصة به. */
    fun register(owner: Any, stop: () -> Unit) {
        stoppers[owner] = stop
    }

    /** يزيل مالكًا بعد انتهاء تشغيله أو تحريره. */
    fun unregister(owner: Any) {
        stoppers.remove(owner)
    }

    /**
     * يوقف كل المشغّلات الأخرى قبل بدء تشغيل جديد.
     * لا يوقف [owner] نفسه (حتى يبقى الاستئناف ممكنًا).
     */
    fun requestPlayback(owner: Any) {
        val others = stoppers.entries
            .filter { it.key !== owner }
            .map { it.value }
        others.forEach { runCatching { it() } }
    }

    /** عدد المشغّلات المسجّلة — للتشخيص والاختبار فقط. */
    fun registeredCount(): Int = stoppers.size
}
