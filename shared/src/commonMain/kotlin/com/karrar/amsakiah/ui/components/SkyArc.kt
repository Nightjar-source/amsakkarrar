package com.karrar.amsakiah.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.ui.motion.motionEffects
import com.karrar.amsakiah.ui.theme.KararTokens
import com.karrar.amsakiah.ui.theme.LocalKararPalette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * أبعاد viewBox المطابقة حرفيًا لـ`amsak3d.tsx`:
 * `<svg viewBox="22 2 356 298" preserveAspectRatio="xMidYMid meet">`.
 */
private const val VB_X = 22f
private const val VB_Y = 2f
private const val VB_W = 356f
private const val VB_H = 298f

/**
 * مسار القوس من التصميم: `M 30 260 A 170 240 0 0 1 370 260`.
 * نرسمه عدديًا بنفس معادلة المرجع:
 * `ox = 200 + 170*cos(angle)` و`oy = 260 - 240*sin(angle)`.
 */
private const val ARC_CX = 200f
private const val ARC_CY = 260f
private const val ARC_RX = 170f
private const val ARC_RY = 240f

/**
 * هامش أفقي إضافي بوحدات `viewBox` يُضاف على جانبي مسرح القوس.
 *
 * في المرجع (`amsak3d.tsx`) يحمل الـ`<svg>` الصنف `overflow-visible`، لذلك يمتدّ
 * نصّا الطرفين (الشروق/الغروب/المغرب/الفجر) خارج حدود الـviewBox ولا يُقصّان.
 * في Compose نُوسّع المساحة الفعّالة بنفس المقدار، فيبقى النص كاملًا ومتوسّطًا
 * تحت نقطة طرفه، ولا يلمس حافة الشاشة (ثقب/شريط/DeX/نافذة حرة).
 */
const val SKY_ARC_EDGE_MARGIN_UNITS: Float = 16f

/**
 * نسبة أبعاد «مسرح» القوس بعد إضافة الهامش الجانبي:
 * `(356 + 2×16) / 298`. تُستخدم لحساب أبعاد المكوّن دون فراغ ميّت.
 */
const val SKY_ARC_STAGE_ASPECT: Float = 388f / 298f

/** محطة على القوس عند نسبة زمنية t (0 = اليسار/البداية، 1 = اليمين/النهاية). */
private fun arcPoint(t: Float): Pair<Float, Float> {
    val angle = PI - (t.coerceIn(0f, 1f) * PI)
    return Pair(
        ARC_CX + ARC_RX * cos(angle).toFloat(),
        ARC_CY - ARC_RY * sin(angle).toFloat()
    )
}

/**
 * القوس السماوي 3D — نسخة مطابقة لـ`amsak3d.tsx` (السطور 1807–1939).
 *
 * المكونات بالترتيب نفسه:
 * 1. مسار ظل أسود + مسار منقّط رقيق.
 * 2. هالة «mist» ضبابية خلف شريط التقدم (تدرّج نهاري/ليلي).
 * 3. شريط تقدم متدرّج + توهّج خارجي.
 * 4. الشمس أو القمر بطوره حسب اليوم الهجري، مع هالة.
 * 5. الوقت الحالي في المنتصف + «الوقت الحالي» + شارة المرحلة الفرعية.
 * 6. طرفا القوس مع اسم الوقت ووقته.
 *
 * @param progress نسبة التقدم 0..1 (من اليسار إلى اليمين).
 * @param isNight هل الفترة الحالية ليلية (تتحكم بالشمس/القمر وتدرّج القوس).
 * @param hijriDay اليوم الهجري (1..30) لحساب طور القمر.
 * @param leftLabel اسم الطرف الأيسر (الشروق/المغرب...).
 * @param leftTime وقت الطرف الأيسر بصيغة 12 ساعة.
 * @param rightLabel اسم الطرف الأيمن (الغروب/المغرب/الشروق/الفجر).
 * @param rightTime وقت الطرف الأيمن بصيغة 12 ساعة.
 * @param subPhaseBadge شارة ذهبية صغيرة تحت «الوقت الحالي» عند تغيّر المرحلة.
 */
@Composable
fun SkyArc(
    progress: Float,
    isNight: Boolean,
    hijriDay: Int,
    timeText: String,
    meridiem: String,
    leftLabel: String,
    leftTime: String,
    rightLabel: String,
    rightTime: String,
    modifier: Modifier = Modifier,
    subPhaseBadge: String? = null,
    bottomLabel: String = "الوقت الحالي"
) {
    val measurer = rememberTextMeasurer()
    val palette = LocalKararPalette.current
    val skyText = palette.skyText

    // انتقال سلس لحركة الشمس/القمر والمسار (نابض بطيء بلا ارتداد: يتبع التغيّر
    // في أي لحظة بدل إعادة تشغيل مدة ثابتة، فلا «قفزة» عند تقادم الوقت).
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = motionEffects(
            stiffness = KararTokens.Motion.EffectsStiffnessSlow,
            dampingRatio = 1f
        ),
        label = "sky-arc-progress"
    )

    Box(modifier = modifier) {
        Canvas(Modifier.fillMaxSize()) {
            // ---- مقياس viewBox داخل المساحة (xMidYMid meet) ----
            // نُدخل الهامش الجانبي داخل المعادلة نفسها (لا حشو خارجي)، فتنكمش
            // الهندسة قليلًا ويبقى للنصّين الطرفيين متّسع حقيقي داخل مساحة الرسم.
            // s صغيرة (1.12.2) + inset مكافئ لـ DrawScope.inset أفقياً.
            val edgeMargin = SKY_ARC_EDGE_MARGIN_UNITS
            val effectiveW = VB_W + edgeMargin * 2f
            val s = min(size.width / effectiveW, size.height / VB_H)
            val ox0 = (size.width - effectiveW * s) / 2f - (VB_X - edgeMargin) * s
            val oy0 = (size.height - VB_H * s) / 2f - VB_Y * s

            fun px(x: Float) = ox0 + x * s
            fun py(y: Float) = oy0 + y * s

            fun unitSp(units: Float): TextUnit =
                (units * s / (density * fontScale)).sp

            fun paint(
                text: String,
                xUnits: Float,
                baselineUnits: Float,
                fontUnits: Float,
                color: Color,
                weight: FontWeight,
                letterSpacingUnits: Float = 0f,
                centered: Boolean = true,
                ltr: Boolean = false,
                shadowUnits: Float = 0f
            ) {
                if (text.isBlank()) return
                val style = TextStyle(
                    color = color,
                    fontSize = unitSp(fontUnits),
                    fontWeight = weight,
                    letterSpacing = unitSp(letterSpacingUnits),
                    textDirection = if (ltr) TextDirection.Ltr else TextDirection.Rtl
                )
                val layout = measurer.measure(AnnotatedString(text), style)
                val rawLeft = if (centered) px(xUnits) - layout.size.width / 2f else px(xUnits)
                // حماية أخيرة: لا يخرج أي حرف عن مساحة الرسم مهما كان طول النص
                // (نص طويل/تغيير حجم خط/عرض نافذة ضيق في DeX أو النوافذ الحرة).
                val maxLeft = (size.width - layout.size.width).coerceAtLeast(0f)
                val left = rawLeft.coerceIn(0f, maxLeft)
                if (shadowUnits > 0f) {
                    drawText(
                        textLayoutResult = layout,
                        color = Color.Black.copy(alpha = 0.45f),
                        topLeft = Offset(left, py(baselineUnits) - layout.firstBaseline + shadowUnits * s)
                    )
                }
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(left, py(baselineUnits) - layout.firstBaseline)
                )
            }

            // ---- مسار القوس الكامل ----
            // بناء هندسي رسمي: `Path.addArc` على القطع الناقص نفسه
            // `M 30 260 A 170 240 0 0 1 370 260` بدل تقريب بـ120 خطًا.
            // الزاوية 180° = الطرف الأيسر، والزيادة حتى 360° تمرّ بالقمة وتنتهي يمينًا.
            fun buildArc(from: Float, to: Float): Path {
                val path = Path()
                if (to <= from) return path
                path.addArc(
                    oval = Rect(
                        left = px(ARC_CX - ARC_RX),
                        top = py(ARC_CY - ARC_RY),
                        right = px(ARC_CX + ARC_RX),
                        bottom = py(ARC_CY + ARC_RY)
                    ),
                    startAngleDegrees = 180f + 180f * from,
                    sweepAngleDegrees = 180f * (to - from)
                )
                return path
            }

            val fullPath = buildArc(0f, 1f)

            // 1) ظل أسود + خط منقّط (rgba(0,0,0,.45) / rgba(255,255,255,.14))
            drawPath(
                path = fullPath,
                color = Color.Black.copy(alpha = 0.45f),
                style = Stroke(width = 8f * s, cap = StrokeCap.Round)
            )
            drawPath(
                path = fullPath,
                color = skyText.copy(alpha = 0.18f),
                style = Stroke(
                    width = 2f * s,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f * s, 8f * s))
                )
            )

            val progressPath = buildArc(0f, animatedProgress)
            val progressBrush = Brush.linearGradient(
                colorStops = if (!isNight) {
                    arrayOf(
                        0f to Color(0xFFFFE066),
                        0.5f to Color(0xFFFF9F1C),
                        1f to Color(0xFFE71D36)
                    )
                } else {
                    arrayOf(
                        0f to Color(0xFF80A1D4),
                        0.5f to Color(0xFFC0D6DF),
                        1f to Color(0xFFF2F5EA)
                    )
                },
                start = Offset(px(30f), 0f),
                end = Offset(px(370f), 0f)
            )

            // 2) هالة mist خلف شريط التقدم (محاكاة feGaussianBlur بثلاث طبقات)
            if (animatedProgress > 0.001f) {
                val mistBrush = Brush.linearGradient(
                    colorStops = if (!isNight) {
                        arrayOf(
                            0f to Color(0xFFFDE047).copy(alpha = 0.55f),
                            0.5f to Color(0xFFFB923C).copy(alpha = 0.65f),
                            1f to Color(0xFFF43F5E).copy(alpha = 0.55f)
                        )
                    } else {
                        arrayOf(
                            0f to Color(0xFF38BDF8).copy(alpha = 0.50f),
                            0.5f to Color(0xFF818CF8).copy(alpha = 0.60f),
                            1f to Color(0xFFE2E8F0).copy(alpha = 0.50f)
                        )
                    },
                    start = Offset(px(30f), 0f),
                    end = Offset(px(370f), 0f)
                )
                drawPath(
                    path = progressPath,
                    brush = mistBrush,
                    alpha = 0.16f,
                    style = Stroke(width = 34f * s, cap = StrokeCap.Round)
                )
                drawPath(
                    path = progressPath,
                    brush = mistBrush,
                    alpha = 0.22f,
                    style = Stroke(width = 22f * s, cap = StrokeCap.Round)
                )
                drawPath(
                    path = progressPath,
                    brush = mistBrush,
                    alpha = 0.30f,
                    style = Stroke(width = 12f * s, cap = StrokeCap.Round)
                )
            }

            // 3) شريط التقدم الرئيسي 4.5 + توهّج خارجي
            if (animatedProgress > 0.001f) {
                drawPath(
                    path = progressPath,
                    brush = progressBrush,
                    alpha = 0.28f,
                    style = Stroke(width = 14f * s, cap = StrokeCap.Round)
                )
                drawPath(
                    path = progressPath,
                    brush = progressBrush,
                    style = Stroke(width = 4.5f * s, cap = StrokeCap.Round)
                )
            }

            // 4) الشمس أو القمر عند نقطة التقدم — طبق الأصل من `amsak3d.tsx`
            val (ux, uy) = arcPoint(animatedProgress)
            val body = Offset(px(ux), py(uy))

            if (!isNight) {
                // هالة الشمس الخارجية (r*2.1 ثم r*1.4) + 12 شعاعًا × 5° بعدها القرص
                val sr = 15f * s
                drawCircle(
                    color = Color(0x2EFFA500), // rgba(255,165,0,0.18) — هالة المرجع الأولى
                    radius = sr * 2.1f,
                    center = body
                )
                drawCircle(
                    color = Color(0x59FFC81E), // rgba(255,200,30,0.35) — الهالة الثانية
                    radius = sr * 1.4f,
                    center = body
                )
                // 12 شعاعًا — من r*1.15 إلى r*1.55 (نفس RealisticSunSVG)
                for (deg in 0 until 360 step 30) {
                    val rad = Math.toRadians(deg.toDouble())
                    val dx = sin(rad).toFloat() * sr
                    val dy = cos(rad).toFloat() * sr
                    drawLine(
                        color = Color(0xFFFDE047).copy(alpha = 0.75f),
                        start = Offset(body.x + dx * 1.15f, body.y - dy * 1.15f),
                        end = Offset(body.x + dx * 1.55f, body.y - dy * 1.55f),
                        strokeWidth = 1.4f * s,
                        cap = StrokeCap.Round
                    )
                }
                // قرص الشمس بتدرّج واقعي (الألوان والمركز من المرجع)
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to Color(0xFFFFFFFF),
                            0.35f to Color(0xFFFEF08A),
                            0.75f to Color(0xFFF59E0B),
                            1f to Color(0xFFEA580C)
                        ),
                        center = Offset(body.x - sr * 0.3f, body.y - sr * 0.3f),
                        radius = sr * 0.85f
                    ),
                    radius = sr * 0.85f,
                    center = body
                )
            } else {
                // القمر الواقعي — نفس المرجع: هالة مزدوجة + قرص داكن + طور بقوسين + فوهات
                val r = 15f * s
                val geom = com.karrar.amsakiah.core.astronomy.moonGeometry(hijriDay, 1f)
                val k = geom.illuminationRatio
                drawCircle(
                    color = Color(0x24FDE68A), // rgba(253,230,138,.137) ≈ 0x23
                    radius = r * 1.9f,
                    center = body
                )
                drawCircle(
                    color = Color(0x2EE2E8F0), // rgba(226,232,240,.18)
                    radius = r * 1.3f,
                    center = body
                )
                // القرص المظلم (radial من #1E293B @ .92 إلى #090D16 @ .96) + محيط رفيع
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to Color(0xEB1E293B),
                            1f to Color(0xF6090D16)
                        ),
                        center = body,
                        radius = r
                    ),
                    radius = r,
                    center = body
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.18f),
                    radius = r,
                    center = body,
                    style = Stroke(width = 0.6f * s)
                )
                // السطح المضاء — مساران قوسيان
                if (!geom.isNewMoon) {
                    val rx = geom.rx * r
                    val litPath = Path().apply {
                        moveTo(body.x, body.y - r)
                        if (geom.isFullMoon) {
                            addOval(Rect(body.x - r, body.y - r, body.x + r, body.y + r))
                        } else {
                            addArc(
                                Rect(body.x - r, body.y - r, body.x + r, body.y + r),
                                270f,
                                if (geom.isWaxing) 180f else -180f
                            )
                            val waxingCrescent = geom.isWaxing == (k < 0.5f)
                            addArc(
                                Rect(body.x - rx, body.y - r, body.x + rx, body.y + r),
                                90f,
                                if (waxingCrescent) -180f else 180f
                            )
                            close()
                        }
                    }
                    drawPath(
                        path = litPath,
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0f to Color(0xFFFFFDF7),
                                0.45f to Color(0xFFFDE68A),
                                0.80f to Color(0xFFE2E8F0),
                                1f to Color(0xFF94A3B8)
                            ),
                            center = Offset(body.x - r * 0.24f, body.y - r * 0.3f),
                            radius = r * 1.5f
                        )
                    )
                    // فوهات المرجع — شفافيتها تتبع الإضاءة
                    drawCircle(
                        color = Color(0xFF64748B).copy(alpha = if (k > 0.4f) 0.22f else 0.08f),
                        radius = r * 0.18f,
                        center = Offset(body.x - r * 0.28f, body.y - r * 0.2f)
                    )
                    drawCircle(
                        color = Color(0xFF64748B).copy(alpha = 0.20f),
                        radius = r * 0.15f,
                        center = Offset(body.x + r * 0.22f, body.y - r * 0.12f)
                    )
                    drawCircle(
                        color = Color(0xFF475569).copy(alpha = 0.18f),
                        radius = r * 0.22f,
                        center = Offset(body.x + r * 0.15f, body.y + r * 0.3f)
                    )
                }
            }

            // 5) الوقت الحالي في المنتصف — translate(200, 198)
            paint(
                text = timeText,
                xUnits = 200f,
                baselineUnits = 198f,
                fontUnits = 45f,
                color = skyText,
                weight = FontWeight.Bold,
                ltr = true,
                shadowUnits = 3f
            )
            if (meridiem.isNotBlank()) {
                paint(
                    text = meridiem,
                    xUnits = 122f,
                    baselineUnits = 211f,
                    fontUnits = 13f,
                    color = skyText.copy(alpha = 0.8f),
                    weight = FontWeight.Bold,
                    centered = false
                )
            }
            paint(
                text = bottomLabel,
                xUnits = 200f,
                baselineUnits = 234f,
                fontUnits = 11f,
                color = skyText.copy(alpha = 0.4f),
                weight = FontWeight.Bold,
                letterSpacingUnits = 4f
            )
            if (!subPhaseBadge.isNullOrBlank()) {
                paint(
                    text = subPhaseBadge,
                    xUnits = 200f,
                    baselineUnits = 252f,
                    fontUnits = 9.5f,
                    color = Color(0xFFFDE047).copy(alpha = 0.9f),
                    weight = FontWeight.Bold
                )
            }

            // 6) طرفا القوس (30,260) و(370,260)
            val left = Offset(px(30f), py(260f))
            val right = Offset(px(370f), py(260f))
            drawCircle(Color.Black.copy(alpha = 0.55f), radius = 6f * s, center = left)
            drawCircle(Color.Black.copy(alpha = 0.55f), radius = 6f * s, center = right)
            drawCircle(skyText, radius = 4.5f * s, center = left)
            drawCircle(skyText, radius = 4.5f * s, center = right)

            paint(
                text = leftLabel,
                xUnits = 30f,
                baselineUnits = 281f,
                fontUnits = 11f,
                color = skyText.copy(alpha = 0.92f),
                weight = FontWeight.Bold,
                shadowUnits = 1.5f
            )
            paint(
                text = leftTime,
                xUnits = 30f,
                baselineUnits = 295f,
                fontUnits = 9.5f,
                color = Color(0xFFFDE047).copy(alpha = 0.9f),
                weight = FontWeight.Bold,
                ltr = true
            )
            paint(
                text = rightLabel,
                xUnits = 370f,
                baselineUnits = 281f,
                fontUnits = 11f,
                color = skyText.copy(alpha = 0.92f),
                weight = FontWeight.Bold,
                shadowUnits = 1.5f
            )
            paint(
                text = rightTime,
                xUnits = 370f,
                baselineUnits = 295f,
                fontUnits = 9.5f,
                color = Color(0xFFFDE047).copy(alpha = 0.9f),
                weight = FontWeight.Bold,
                ltr = true
            )
        }
    }
}
