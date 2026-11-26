package com.Nightjar.amsakkarrar.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
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
     * خلفية مخصصة للإشعارات (شاومي HyperOS / سامسونج One UI / أندرويد 14+):
     * - تمزج ألوان السماء الحية مع عمق الزجاج الليلي الداكن لضمان تباين عالي للنصوص.
     * - زوايا مستديرة ناعمة (Rounded Corners) مع إطار ذهبي رقيق لمنع أي قص فج وتندمج تماماً داخل بطاقة النظام.
     */
    fun renderNotifBackground(
        nowSeconds: Int,
        widthPx: Int = 720,
        heightPx: Int = 320,
        cornerRadiusPx: Float = 48f
    ): Bitmap {
        val (top, bottom, _) = SkyTheme.getSkyColorsForSecond(nowSeconds.toFloat())
        val out = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)

        val topArgb = top.toArgb()
        val bottomArgb = bottom.toArgb()

        val blendedTop = blendColors(topArgb, 0xFF141624.toInt(), 0.55f)
        val blendedBottom = blendColors(bottomArgb, 0xFF0D0E16.toInt(), 0.40f)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, heightPx.toFloat(),
                intArrayOf(blendedTop, blendedBottom),
                null,
                Shader.TileMode.CLAMP
            )
        }
        val rect = RectF(0f, 0f, widthPx.toFloat(), heightPx.toFloat())
        // ملء كامل المستطيل حتى حوافه تماماً ليتطابق مع بطاقة النظام دون أي إطار أو زوايا مقصوصة
        canvas.drawRect(rect, paint)

        return out
    }

    private fun blendColors(foreground: Int, background: Int, alpha: Float): Int {
        val a = alpha.coerceIn(0f, 1f)
        val r = (android.graphics.Color.red(foreground) * a + android.graphics.Color.red(background) * (1 - a)).toInt()
        val g = (android.graphics.Color.green(foreground) * a + android.graphics.Color.green(background) * (1 - a)).toInt()
        val b = (android.graphics.Color.blue(foreground) * a + android.graphics.Color.blue(background) * (1 - a)).toInt()
        return android.graphics.Color.rgb(r, g, b)
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
            // تدرج شريط التقدم:
            // في النهار: تدرج الشمس الذهبي الدافئ الموحد والمتناسق مع خطوط وأرقام التطبيق والودجت
            // تدرج متصل بين درجات الذهب والكهرمان دون أي لون أحمر
            val fillColors = if (isDay) {
                if (fillFromEnd) {
                    intArrayOf(0xFFFFB300.toInt(), 0xFFFFCA28.toInt(), 0xFFFFE082.toInt())
                } else {
                    intArrayOf(0xFFFFE082.toInt(), 0xFFFFCA28.toInt(), 0xFFFFB300.toInt())
                }
            } else {
                if (fillFromEnd) {
                    intArrayOf(0xFF7986CB.toInt(), 0xFF9FA8DA.toInt(), 0xFFC5CAE9.toInt())
                } else {
                    intArrayOf(0xFFC5CAE9.toInt(), 0xFF9FA8DA.toInt(), 0xFF7986CB.toInt())
                }
            }
            val glowColor = if (isDay) 0x33FFB300.toInt() else 0x3380A1D4.toInt()

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
     * تضيف لمسة الشفق ومنحنى المسار اللطيف من المرجع (الصورة الثانية) خلف العدّاد بشكل خفيف وأنيق.
     */
    fun backgroundRemoteViews(
        context: Context,
        nowSeconds: Int
    ): android.widget.RemoteViews {
        val views = android.widget.RemoteViews(context.packageName, com.Nightjar.amsakkarrar.R.layout.widget_background_arc)
        val bitmap = renderHeroBackdrop(nowSeconds)
        views.setImageViewBitmap(com.Nightjar.amsakkarrar.R.id.arc_bg_image, bitmap)
        return views
    }

    /**
     * خلفية الودجت مع لمسة المنحنى والغروب الشفقي الخفيف من الصورة الثانية خلف العدّاد.
     */
    fun renderHeroBackdrop(nowSeconds: Int, widthPx: Int = 360, heightPx: Int = 260): Bitmap {
        val (top, bottom, glow) = SkyTheme.getSkyColorsForSecond(nowSeconds.toFloat())
        val out = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val wF = widthPx.toFloat()
        val hF = heightPx.toFloat()

        // الخلفية العمودية الأساسية للقوس
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, hF,
                intArrayOf(top.toArgb(), bottom.toArgb()),
                null,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, wF, hF, bgPaint)

        // توهج شفقي خفيف خلف منطقة العدّاد (مستلهم من الصورة الثانية)
        val horizonY = hF * 0.44f
        val sunsetGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                wF * 0.5f, horizonY, wF * 0.65f,
                intArrayOf(0x38FF7043.toInt(), 0x1AE85D75.toInt(), 0x00000000),
                floatArrayOf(0f, 0.45f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, wF, hF, sunsetGlowPaint)

        // خط أفق رفيع شفاف + منحنى موجة الشمس اللطيف خلف العدّاد
        val horizonLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x28FFFFFF
            strokeWidth = 1.5f
        }
        canvas.drawLine(wF * 0.06f, horizonY, wF * 0.94f, horizonY, horizonLinePaint)

        val wavePath = Path()
        val steps = 48
        for (i in 0..steps) {
            val t = i / steps.toFloat()
            val x = wF * (0.06f + t * 0.88f)
            val y = horizonY - kotlin.math.sin((t * 2f - 0.5f) * kotlin.math.PI).toFloat() * (hF * 0.08f)
            if (i == 0) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
        }
        val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            strokeCap = Paint.Cap.ROUND
            shader = LinearGradient(
                wF * 0.06f, 0f, wF * 0.94f, 0f,
                intArrayOf(0x18F43F5E, 0x55FFB300, 0x55FFE066, 0x18F43F5E),
                floatArrayOf(0f, 0.35f, 0.65f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawPath(wavePath, wavePaint)
        return out
    }

    /**
     * حساب لون النص الأكثر تبايناً تلقائياً (WCAG / Material 3) فوق لون الخلفية المعطى.
     */
    fun contrastingTextColorArgb(
        bgArgb: Int,
        lightArgb: Int = 0xFFFFFFFF.toInt(),
        darkArgb: Int = 0xFF0F172A.toInt()
    ): Int {
        val r = android.graphics.Color.red(bgArgb) / 255.0
        val g = android.graphics.Color.green(bgArgb) / 255.0
        val b = android.graphics.Color.blue(bgArgb) / 255.0
        val lum = 0.2126 * r + 0.7152 * g + 0.0722 * b
        return if (lum > 0.42) darkArgb else lightArgb
    }

    /**
     * رسم مسار الشمس الجيبي مع نقاط وأوقات الصلوات الظاهرة والقمر الواقعي المضيء (مثل الصورة الأولى).
     */
    fun sunMoonArcBitmap(
        nowSeconds: Int,
        sunriseHour: Double,
        sunsetHour: Double,
        hijriDay: Int,
        entries: List<com.Nightjar.amsakkarrar.notifications.PrayerEntry> = emptyList(),
        widthPx: Int = 520,
        heightPx: Int = 150
    ): Bitmap {
        val out = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val wF = widthPx.toFloat()
        val hF = heightPx.toFloat()
        val horizonY = hF * 0.56f

        // 1) خط الأفق وظل الليل تحته
        val nightBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x22000000 }
        canvas.drawRect(0f, horizonY, wF, hF, nightBg)
        val horizonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x38FFFFFF
            strokeWidth = 1.5f
        }
        canvas.drawLine(0f, horizonY, wF, horizonY, horizonPaint)

        // 2) المنحنى الجيبي لمسار الشمس والصلوات
        val nowHour = nowSeconds / 3600.0
        val dayLen = (sunsetHour - sunriseHour).coerceAtLeast(1.0)
        val dayFrac = ((nowHour - sunriseHour) / dayLen).coerceIn(0.0, 1.0).toFloat()

        val inactivePath = Path()
        val activePath = Path()
        val steps = 60
        for (i in 0..steps) {
            val t = i / steps.toFloat()
            val x = wF * 0.05f + t * wF * 0.74f
            val wave = kotlin.math.cos((t - 0.5f) * 1.45f * kotlin.math.PI).toFloat()
            val y = horizonY - wave * (hF * 0.36f)
            if (i == 0) inactivePath.moveTo(x, y) else inactivePath.lineTo(x, y)
            if (t >= 0.16f && t <= 0.84f) {
                if (activePath.isEmpty) activePath.moveTo(x, y) else activePath.lineTo(x, y)
            }
        }
        canvas.drawPath(
            inactivePath,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3f
                color = 0x44FFFFFF
            }
        )
        canvas.drawPath(
            activePath,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 4.5f
                strokeCap = Paint.Cap.ROUND
                color = 0xFFFFC107.toInt()
            }
        )

        // نقاط وأوقات الصلوات على المنحنى (حسب ضبط الإعدادات)
        val shown = entries.take(6)
        if (shown.isNotEmpty()) {
            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFF8FAFC.toInt()
                textSize = hF * 0.11f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }
            val nextLabelPaint = Paint(labelPaint).apply {
                color = 0xFFFFD700.toInt()
            }
            shown.forEachIndexed { idx, entry ->
                val t = if (shown.size == 1) 0.5f else idx.toFloat() / (shown.size - 1).toFloat()
                val x = wF * 0.07f + t * wF * 0.70f
                val wave = kotlin.math.cos((t - 0.5f) * 1.45f * kotlin.math.PI).toFloat()
                val y = horizonY - wave * (hF * 0.36f)
                val dotColor = if (entry.isNext) 0xFFFFD700.toInt() else 0xCCF8FAFC.toInt()
                canvas.drawCircle(x, y, if (entry.isNext) 5.5f else 3.8f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = dotColor })
                val shortTime = entry.time.replace(" صباحاً", "").replace(" مساءً", "").replace(" ص", "").replace(" م", "")
                val textY = if (y < horizonY) y - 10f else y + hF * 0.14f
                canvas.drawText(shortTime, x, textY.coerceIn(14f, hF - 4f), if (entry.isNext) nextLabelPaint else labelPaint)
            }
        }

        // نقطة الشمس المتوهجة على المسار
        val sunT = 0.16f + dayFrac * (0.84f - 0.16f)
        val sunX = wF * 0.05f + sunT * wF * 0.74f
        val sunWave = kotlin.math.cos((sunT - 0.5f) * 1.45f * kotlin.math.PI).toFloat()
        val sunY = horizonY - sunWave * (hF * 0.36f)
        canvas.drawCircle(sunX, sunY, 13f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x44FFD54F })
        canvas.drawCircle(sunX, sunY, 7.5f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFCA28.toInt() })

        // 3) القمر الواقعي بملمس السطح وظل الهلال حسب اليوم الهجري (على اليمين/الطرف)
        val moonR = hF * 0.25f
        val moonCx = wF - moonR - 12f
        val moonCy = hF * 0.44f
        val moonPhase = ((hijriDay.coerceIn(1, 30) - 1) / 29.5f)
        canvas.drawCircle(moonCx, moonCy, moonR, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF334155.toInt() })
        val moonPath = Path().apply { addCircle(moonCx, moonCy, moonR, Path.Direction.CW) }
        canvas.save()
        canvas.clipPath(moonPath)
        val litOffset = (0.5f - moonPhase) * moonR * 2.2f
        canvas.drawCircle(
            moonCx + litOffset,
            moonCy,
            moonR * 1.05f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    moonCx + moonR * 0.2f, moonCy - moonR * 0.2f, moonR * 1.5f,
                    intArrayOf(0xFFF8FAFC.toInt(), 0xFFCBD5E1.toInt(), 0xFF94A3B8.toInt()),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
        )
        val craterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x24475569 }
        canvas.drawCircle(moonCx + moonR * 0.28f, moonCy - moonR * 0.18f, moonR * 0.18f, craterPaint)
        canvas.drawCircle(moonCx - moonR * 0.12f, moonCy + moonR * 0.32f, moonR * 0.14f, craterPaint)
        canvas.drawCircle(moonCx + moonR * 0.10f, moonCy + moonR * 0.10f, moonR * 0.10f, craterPaint)
        canvas.restore()

        return out
    }
}
