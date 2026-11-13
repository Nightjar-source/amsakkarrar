package com.Nightjar.amsakkarrar.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.GradientDrawable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.karrar.amsakiah.ui.theme.SkyTheme

/**
 * خلفية «ألوان اليوم» — نفس خلفية شاشة «اليوم» القمرية (القوس) على مدار الدقيقة.
 * تُولَّد كتلة (Bitmap) في كل تحديث ⇒ تطابق حرفيًا تدرّج القوس الأصلي بلا أكواد ألوان ثابتة.
 *
 * Glance / RemoteViews لا يرسمان تدرجات لونيّة، لذلك تُولَّد الخلفية كصورة
 * تُحقَن عبر `setImageBitmap` خلف بقية المحتوى في كل ودجت وإشعار.
 *
 * المصدر الوحيد للتدرّج: [SkyTheme.getSkyColorsForSecond] — نفسها التي تستخدمها
 * شاشات «اليوم» والفوانيس (call it القوس).
 */
object ArcBackground {

    /** زوج الألوان للخلفية في لحظة زمنية محددة (top → bottom as ARGB). */
    data class SkyPair(val top: Int, val bottom: Int)

    fun skyPairFor(nowSeconds: Int): SkyPair {
        val (top, bottom, _) = SkyTheme.getSkyColorsForSecond(nowSeconds.toFloat())
        return SkyPair(top.toArgb(), bottom.toArgb())
    }

    /**
     * يبني صورة الخلفية 480×480 بكسل (تُمدّد على الودجت/الإشعار عبر scaleType="fitXY").
     * كلٌّ من الودجت والإشعارات تستخدم الخلفية نفسها — تزامن كلي حرفي.
     */
    fun renderFor(nowSeconds: Int, paddingPx: Int = 0): Bitmap {
        val (top, bottom, _) = SkyTheme.getSkyColorsForSecond(nowSeconds.toFloat())
        val out = Bitmap.createBitmap(480, 480, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val gd = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(top.toArgb(), bottom.toArgb())
        )
        gd.cornerRadius = 0f
        val pad = paddingPx.coerceAtLeast(0)
        gd.setBounds(pad, pad, canvas.width - pad, canvas.height - pad)
        gd.draw(canvas)
        return out
    }

    /** لون تُغلف طبقة ترفع نقطة مرور «glow» بلطف (يؤطّر الخلفية). */
    fun glowArgb(nowSeconds: Int): Int {
        val (_, _, glow) = SkyTheme.getSkyColorsForSecond(nowSeconds.toFloat())
        return glow.toArgb()
    }

    /**
     * كتلة شرائح (gradient) تُحقَن كـ RemoteViews خلف طبقة المحتوى.
     * صورة كاملة الحجم بخلفية vertical gradient مطابقة للقوس في الثواني الحالية.
     */
    fun backgroundRemoteViews(
        context: Context,
        nowSeconds: Int
    ): android.widget.RemoteViews {
        val views = android.widget.RemoteViews(context.packageName, com.Nightjar.amsakkarrar.R.layout.widget_background_arc)
        val bitmap = renderFor(nowSeconds)
        views.setImageViewBitmap(com.Nightjar.amsakkarrar.R.id.arc_bg_image, bitmap)
        return views
    }
}
