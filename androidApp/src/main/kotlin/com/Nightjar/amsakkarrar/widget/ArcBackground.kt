package com.Nightjar.amsakkarrar.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import androidx.compose.ui.graphics.toArgb
import com.karrar.amsakiah.ui.theme.SkyTheme
import kotlin.math.max
import kotlin.math.min

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
     * استخراج «اللون السائد» من تدرّج السماء نفسه — يغني عن مكتبة Palette،
     * ويُستخدم مع `setColorized(true)` لصبغ كامل بطاقة الإشعار (إخفاء الحواف).
     */
    fun dominantColorArgb(nowSeconds: Int): Int {
        val (top, bottom, glow) = SkyTheme.getSkyColorsForSecond(nowSeconds.toFloat())
        // اللون السائد = لون أعلى السماء (حيث يرسم النظام هيدر الإشعار في MIUI/One UI)
        // ممزوجًا بالوهج بنسبة 30% — فيصبغ الهيدر بنفس درجة البطاقة فلا تظهر حواف.
        val r = (top.red * 0.7f + glow.red * 0.3f).coerceIn(0f, 1f)
        val g = (top.green * 0.7f + glow.green * 0.3f).coerceIn(0f, 1f)
        val b = (top.blue * 0.7f + glow.blue * 0.3f).coerceIn(0f, 1f)
        return android.graphics.Color.rgb((r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt())
    }

    /**
     * لا نحتفظ بأي Bitmap في الذاكرة — كل استدعاء يُنشئ صورة صغيرة
     * (4×256 للخلفية، 660×54 للتقدم) ويُحقنها مباشرة في RemoteViews.
     * هذه الدالة موجودة لاستدعاء [android.app.Activity.onTrimMemory]،
     * ولا يوجد ما يُحرَّر فعليًا (امتثال لمعايير الذاكرة في Play).
     */
    fun releaseCaches() {
        // لا caches ثابتة — لا شيء لتحريره.
    }

    /**
     * شريط خلفي عمودي (4×256) بنفس تدرّج [renderFor] بالضبط — لأن التدرّج عمودي
     * بحت، توسيع العرض يضيف حمولة لا تفاصيل، ويتجاوز حمولة RemoteViews حد
     * المعاملة (~1MB) على سامسونغ فيسقط الإشعار/الودجت بصمت.
     * الصورة تُمدَّد عبر `scaleType="fitXY"` إلى كامل البطاقة، والفرق البصري صفر.
     */
    fun renderStrip(nowSeconds: Int, heightPx: Int = 256): Bitmap {
        val h = heightPx.coerceIn(64, 512)
        val (top, bottom, _) = SkyTheme.getSkyColorsForSecond(nowSeconds.toFloat())
        val out = Bitmap.createBitmap(4, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                intArrayOf(top.toArgb(), bottom.toArgb()),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, 4f, h.toFloat(), paint)
        return out
    }

    /**
     * يبني صورة الخلفية 480×480 بكسل (تُمدّد على الودجت/الإشعار عبر scaleType="fitXY").
     * كلٌّ من الودجت والإشعارات تستخدم الخلفية نفسها — تزامن كلي حرفي.
     *
     * ملاحظة: التدرّج عمودي بحت، لذا لا تغيّر زيادة العرض أي تفاصيل لونية؛
     * الحفاظ على 480×480 يعطي حافة حادة على الشاشات عالية الكثافة.
     */
    fun renderFor(nowSeconds: Int, paddingPx: Int = 0, sizePx: Int = 480): Bitmap {
        val (top, bottom, _) = SkyTheme.getSkyColorsForSecond(nowSeconds.toFloat())
        val side = sizePx.coerceIn(64, 960)
        val out = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
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

    /**
     * شريط التقدم البيضوي (Pill) — يُرسَم كصورة، لأن `ProgressBar`/`layer-list`
     * غير موثوقَين على Samsung One UI (يظهران كـ«حبة داكنة مصمتة» بلا تدرّج
     * ولا تعبئة مرئية).
     *
     * يطابق `widgetsnotifications.html` حرفيًا:
     * - المسار (Track): شكل بيضوي كامل — مصمت `#66000000` مع إطار رفيع `#14FFFFFF`
     *   (نفس `rgba(255,255,255,0.08)` في المرجع).
     * - التعبئة (Fill): تدرّج أفقي — نهارًا `#FFE066→#FF9F1C→#E71D36` (grad-day)،
     *   وليلًا `#80A1D4→#C0D6DF→#F2F5EA` (grad-night) — بنفس الترتيب والنسب.
     * - نهاية مستديرة كاملة (نصف قطر = نصف الارتفاع) لكلٍّ من المسار والتعبئة.
     * - هالة ضوئية خفيفة خلف التعبئة (مقابل `box-shadow … 0.6` في المرجع) ولمعة
     *   علوية ثابتة داخل التعبئة (مقابل الزجاج/shimmer) — بلا أي حركة.
     *
     * @param percent قيمة 0..100 (تُقصّ دائمًا داخل المدى).
     * @param widthPx العرض بالبكسل — يُمدَّد عبر `fitXY` على `match_parent`،
     *   فيكفي رسم بنسبة عريضة دون القلق من العرض الدقيق للبطاقة.
     * @param heightPx الارتفاع بالبكسل؛ نصفه = نصف قطر الطرفين.
     */
    fun progressBitmap(
        isDay: Boolean,
        percent: Float,
        widthPx: Int,
        heightPx: Int,
        fillFromEnd: Boolean = true
    ): Bitmap {
        val w = widthPx.coerceIn(64, 2048)
        val h = heightPx.coerceIn(8, 256)
        val wF = w.toFloat()
        val hF = h.toFloat()
        val radius = hF / 2f
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)

        // 1) المسار (Track) — مصمت داكن شبه شفاف كما في المرجع
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0x66000000.toInt()
        }
        val trackPath = Path().apply {
            addRoundRect(RectF(0f, 0f, wF, hF), radius, radius, Path.Direction.CW)
        }
        canvas.drawPath(trackPath, trackPaint)

        // ظل داخلي علوي خفيف — box-shadow: inset 0 3px 8px rgba(0,0,0,0.7) في المرجع
        canvas.save()
        canvas.clipPath(trackPath)
        val insetShadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, hF * 0.55f,
                intArrayOf(0x90000000.toInt(), 0x00000000),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, wF, hF, insetShadow)
        canvas.restore()

        // إطار رفيع شفاف (1px تقريبًا) فوق المسار
        val strokeWidth = max(1f, hF * 0.05f)
        val halfStroke = strokeWidth / 2f
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = 0x14FFFFFF.toInt()
            this.strokeWidth = strokeWidth
        }
        canvas.drawRoundRect(
            RectF(halfStroke, halfStroke, wF - halfStroke, hF - halfStroke),
            max(0f, radius - halfStroke),
            max(0f, radius - halfStroke),
            strokePaint
        )

        // 2) التعبئة (Fill) — التدرّج حسب الفترة (نهار/ليل) بنفس نسب المرجع.
        //    fillFromEnd=true هو الافتراضي لأن كل استخداماتنا داخل RTL: تبدأ
        //    التعبئة من اليمين (النهاية) وتنمو نحو اليسار، وتدرّجها يبدأ من
        //    طرفها الأيمن — فتتطابق Notification والودجت بالاتجاه نفسه تمامًا.
        val pct = (percent / 100f).coerceIn(0f, 1f)
        if (pct >= 0f) {
            // لا تدَع التعبئة تتقلص لأقل من قطر الحبة عند البداية أو نسب ضئيلة (0..8%)
            val fillW = (wF * pct).coerceAtLeast(min(hF * 0.9f, wF)).coerceAtMost(wF)
            val fillStart = if (fillFromEnd) wF - fillW else 0f
            val fillEnd = fillStart + fillW
            val fillR = min(radius, fillW / 2f)
            val fillColors = if (isDay) {
                intArrayOf(0xFFFFE066.toInt(), 0xFFFF9F1C.toInt(), 0xFFE71D36.toInt())
            } else {
                intArrayOf(0xFF80A1D4.toInt(), 0xFFC0D6DF.toInt(), 0xFFF2F5EA.toInt())
            }
            val glowColor = if (isDay) 0x40FF9F1C.toInt() else 0x4080A1D4.toInt()

            // هالة خفيفة تحيط بالتعبئة (بدل box-shadow — RemoteViews لا ترسم ظلال)
            val glowExpand = hF * 0.16f
            val glowHalf = glowExpand * 0.5f
            val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = glowColor
            }
            canvas.drawRoundRect(
                RectF(fillStart - glowHalf, -glowHalf, fillEnd + glowHalf, hF + glowHalf),
                fillR + glowHalf,
                fillR + glowHalf,
                glowPaint
            )

            // جسم التعبئة — تدرّج أفقي ينتهي عند نهاية التعبئة نفسها (كما في CSS)
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                isDither = true
                shader = LinearGradient(
                    fillStart, 0f, fillEnd, 0f,
                    fillColors,
                    floatArrayOf(0f, 0.5f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            val fillPath = Path().apply {
                addRoundRect(RectF(fillStart, 0f, fillEnd, hF), fillR, fillR, Path.Direction.CW)
            }
            canvas.drawPath(fillPath, fillPaint)

            // لمعة علوية ثابتة داخل التعبئة (زجاج) — مقصوصة على شكل التعبئة
            canvas.save()
            canvas.clipPath(fillPath)
            val sheenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, 0f, hF * 0.45f,
                    intArrayOf(0x4DFFFFFF.toInt(), 0x00FFFFFF),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(fillStart, 0f, fillEnd, hF * 0.45f, sheenPaint)
            canvas.restore()
        }
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
        // الشريط العمودي 4×256 (≈4KB عبر المعاملة) — لا يُسقط الودجت على سامسونغ كما يفعل 480×480.
        val bitmap = renderStrip(nowSeconds)
        views.setImageViewBitmap(com.Nightjar.amsakkarrar.R.id.arc_bg_image, bitmap)
        return views
    }
}
