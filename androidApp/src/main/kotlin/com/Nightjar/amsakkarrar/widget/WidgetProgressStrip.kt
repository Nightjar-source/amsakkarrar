package com.Nightjar.amsakkarrar.widget

import android.graphics.Bitmap

/**
 * غلاف مفوَّض لشريط التقدم — المصدر الوحيد للرسم أصبح
 * [ArcBackground.progressBitmap] حتى يتطابق الودجت والإشعار الحي
 * في الاتجاه (تعبئة من اليمين RTL) والتدرّج والزوايا والحواف بالبكسل.
 *
 * كان هذا الملف نسخة ثانية من منطق الرسم، فانحرف اتجاه التعبئة بين
 * الودجت (يمين) والإشعار (يسار). الآن لا منطق رسم هنا إطلاقًا.
 */
object WidgetProgressStrip {

    /** مقاس الرسم نفسه المستخدم في الإشعارات — تتمدد الصورة عبر `fitXY`. */
    private const val WIDTH = 480
    private const val HEIGHT = 48

    /**
     * يرسم صورة الشريط كاملة بنسبة [percent] (0..100).
     * [daytime] يختار تدرّج النهار أو الليل.
     * [fillFromEnd] يبدأ التعبئة من نهاية اتجاه RTL (اليمين) — وهو الافتراضي
     * في كل تخطيطاتنا العربية.
     */
    fun render(percent: Float, daytime: Boolean, fillFromEnd: Boolean = true): Bitmap {
        return ArcBackground.progressBitmap(
            isDay = daytime,
            percent = percent.coerceIn(0f, 100f),
            widthPx = WIDTH,
            heightPx = HEIGHT,
            fillFromEnd = fillFromEnd
        )
    }

    fun render(percent: Int, daytime: Boolean, fillFromEnd: Boolean = true): Bitmap {
        return render(percent.toFloat(), daytime, fillFromEnd)
    }
}
