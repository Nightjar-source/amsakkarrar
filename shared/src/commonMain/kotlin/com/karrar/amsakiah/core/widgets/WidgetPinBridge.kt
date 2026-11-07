package com.karrar.amsakiah.core.widgets

/**
 * جسر طلب تثبيت الودجت على الشاشة الرئيسية.
 *
 * الوحدة المشتركة لا تعرف أصناف الودجت في `androidApp`، لذا يسجّل التطبيق
 * الأصلي معالجًا هنا عند بدء التشغيل. على iOS يبقى `supported=false`
 * وتُعرض إرشادات الإضافة اليدوية بدلًا من الطلب التلقائي.
 */
object WidgetPinBridge {

    /** معرّفات الودجت السبعة — يجب أن تطابق مفاتيح المعالج في androidApp. */
    const val TODAY = "today"
    const val TIMES = "times"
    const val COUNTDOWN = "countdown"
    const val HIJRI = "hijri"
    const val TASBIH = "tasbih"
    const val CLOCK = "clock"
    const val JOURNEY = "journey"

    /** أنماط خلفية الودجت: مادة / زجاجي / ضبابي / شفاف / ليلي متدرّج */
    const val STYLE_MATERIAL = "material"
    const val STYLE_GLASS = "glass"
    const val STYLE_BLUR = "blur"
    const val STYLE_TRANSPARENT = "transparent"
    const val STYLE_DEEP = "deep"

    /** هل يدعم النظام الحالي طلب التثبيت التلقائي؟ */
    var supported: Boolean = false
        private set

    private var pinHandler: ((String) -> Boolean)? = null
    private var openSettingsHandler: (() -> Boolean)? = null
    private var styleGetter: (() -> String)? = null
    private var styleSetter: ((String) -> Boolean)? = null

    fun install(handler: ((String) -> Boolean)?, isSupported: Boolean) {
        pinHandler = handler
        supported = isSupported
    }

    fun installSettings(
        open: (() -> Boolean)?,
        getStyle: (() -> String)?,
        setStyle: ((String) -> Boolean)?
    ) {
        openSettingsHandler = open
        styleGetter = getStyle
        styleSetter = setStyle
    }

    /** يُرجع true إذا فُتحت نافذة النظام لتأكيد إضافة الودجت. */
    fun requestPin(widgetKey: String): Boolean = pinHandler?.invoke(widgetKey) ?: false

    /** فتح شاشة إعدادات خلفية الودجتات (Material / Blur / شفاف). */
    fun openWidgetSettings(): Boolean = openSettingsHandler?.invoke() ?: false

    fun currentStyle(): String = styleGetter?.invoke() ?: STYLE_DEEP

    fun setStyle(key: String): Boolean = styleSetter?.invoke(key) ?: false
}
