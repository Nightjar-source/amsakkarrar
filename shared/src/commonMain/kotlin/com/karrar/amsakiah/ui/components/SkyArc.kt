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
import com.karrar.amsakiah.ui.theme.KararTokens
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

    // انتقال سلس لحركة الشمس/القمر والمسار (نابض بطيء بلا ارتداد: يتبع التغيّر
    // في أي لحظة بدل إعادة تشغيل مدة ثابتة، فلا «قفزة» عند تقادم الوقت).
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = KararTokens.Motion.effects(
            stiffness = KararTokens.Motion.EffectsStiffnessSlow,
            dampingRatio = 1f
        ),
        label = "sky-arc-progress"
    )

    Box(modifier = modifier) {
        Canvas(Modifier.fillMaxSize()) {
            // ---- مقياس viewBox داخل المساحة (xMidYMid meet) ----
            val s = min(size.width / VB_W, size.height / VB_H)
            val ox0 = (size.width - VB_W * s) / 2f - VB_X * s
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
                val left = if (centered) px(xUnits) - layout.size.width / 2f else px(xUnits)
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
            fun buildArc(from: Float, to: Float): Path {
                val path = Path()
                val steps = 120
                val total = ((to - from).coerceIn(0f, 1f) * steps).toInt().coerceAtLeast(2)
                for (i in 0..total) {
                    val t = from + (to - from) * (i.toFloat() / total)
                    val (ux, uy) = arcPoint(t)
                    if (i == 0) path.moveTo(px(ux), py(uy)) else path.lineTo(px(ux), py(uy))
                }
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
                color = Color.White.copy(alpha = 0.14f),
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

            // 4) الشمس أو القمر عند نقطة التقدم
            val (ux, uy) = arcPoint(animatedProgress)
            val body = Offset(px(ux), py(uy))

            if (!isNight) {
                // هالة الشمس
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to Color(0x66FFD54F),
                            0.45f to Color(0x33FF9F1C),
                            1f to Color.Transparent
                        ),
                        center = body,
                        radius = 38f * s
                    ),
                    radius = 38f * s,
                    center = body
                )
                // قرص الشمس بتدرّج واقعي (نفس ألوان RealisticSunSVG)
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to Color(0xFFFFFFFF),
                            0.32f to Color(0xFFFFF4CC),
                            0.72f to Color(0xFFFFB703),
                            1f to Color(0xFFFF8C00)
                        ),
                        center = Offset(body.x - 4f * s, body.y - 4f * s),
                        radius = 17f * s
                    ),
                    radius = 16f * s,
                    center = body
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.55f),
                    radius = 4.5f * s,
                    center = Offset(body.x - 5f * s, body.y - 5f * s)
                )
            } else {
                // هالة القمر
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to Color(0x4DE2E8F0),
                            0.5f to Color(0x2694A3B8),
                            1f to Color.Transparent
                        ),
                        center = body,
                        radius = 34f * s
                    ),
                    radius = 34f * s,
                    center = body
                )

                // طور القمر: يوم 1 هلال رقيق، يوم 15 بدر، ثم يعود هلالًا.
                val phase = (((hijriDay - 1) % 30) / 29f).coerceIn(0f, 1f)
                val illumination = ((1f - cos(phase * 2f * PI.toFloat())) / 2f).coerceIn(0f, 1f)
                val r = 15f * s
                val shadowShift = 2f * r * illumination * (if (phase <= 0.5f) 1f else -1f)
                val moonClip = Path().apply { addOval(Rect(center = body, radius = r)) }
                clipPath(moonClip) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0f to Color(0xFFFFFFFF),
                                0.4f to Color(0xFFE2E8F0),
                                0.82f to Color(0xFF94A3B8),
                                1f to Color(0xFF475569)
                            ),
                            center = Offset(body.x - 4f * s, body.y - 4f * s),
                            radius = r
                        ),
                        radius = r,
                        center = body
                    )
                    drawCircle(
                        color = Color(0xE60D1117),
                        radius = r,
                        center = Offset(body.x + shadowShift, body.y)
                    )
                }
                // حبيبات خفيفة على سطح القمر
                drawCircle(
                    color = Color(0x1A0F172A),
                    radius = 2.6f * s,
                    center = Offset(body.x + 4f * s, body.y + 3f * s)
                )
                drawCircle(
                    color = Color(0x140F172A),
                    radius = 1.8f * s,
                    center = Offset(body.x - 3f * s, body.y + 6f * s)
                )
            }

            // 5) الوقت الحالي في المنتصف — translate(200, 198)
            paint(
                text = timeText,
                xUnits = 200f,
                baselineUnits = 198f,
                fontUnits = 45f,
                color = Color.White,
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
                    color = Color.White.copy(alpha = 0.8f),
                    weight = FontWeight.Bold,
                    centered = false
                )
            }
            paint(
                text = bottomLabel,
                xUnits = 200f,
                baselineUnits = 234f,
                fontUnits = 11f,
                color = Color.White.copy(alpha = 0.4f),
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
            drawCircle(Color.White, radius = 4.5f * s, center = left)
            drawCircle(Color.White, radius = 4.5f * s, center = right)

            paint(
                text = leftLabel,
                xUnits = 30f,
                baselineUnits = 281f,
                fontUnits = 11f,
                color = Color.White.copy(alpha = 0.92f),
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
                color = Color.White.copy(alpha = 0.92f),
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
