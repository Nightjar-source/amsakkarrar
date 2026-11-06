package com.karrar.amsakiah.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** أبعاد الـ viewBox المطابقة لتصميم V7 */
private const val VB_W = 400f
private const val VB_H = 320f

/** مركز القوس ونصف القطرين (نفس مسار SVG: M 30 260 A 170 240 0 0 1 370 260) */
private const val ARC_CX = 200f
private const val ARC_CY = 260f
private const val ARC_RX = 170f
private const val ARC_RY = 240f

/** نقطة على القوس عند نسبة زمنية t (0 = الفجر، 1 = المغرب) */
private fun arcPointUnits(t: Float): Pair<Float, Float> {
    val angle = PI - (t.coerceIn(0f, 1f) * PI)
    return Pair(
        ARC_CX + ARC_RX * cos(angle).toFloat(),
        ARC_CY - ARC_RY * sin(angle).toFloat()
    )
}

/**
 * القوس 3D — نسخة حرفية من قوس V7 (SVG viewBox 400×320).
 *
 * - الشكل البيضوي نفسه: `M 30 260 A 170 240 0 0 1 370 260`.
 * - خلفية سوداء + مسار منقّط + مسار متدرّج حسب النهار/الليل.
 * - شمس ثلاثية الأبعاد أو قمر بطوره حسب اليوم الهجري.
 * - الوقت الحالي في المنتصف، و«الفجر» / «المغرب» عند الطرفين.
 */
@Composable
fun SkyArc(
    progress: Float,
    isNight: Boolean,
    moonPhase: Double,
    timeText: String,
    meridiem: String,
    modifier: Modifier = Modifier,
    startLabel: String = "الفجر",
    endLabel: String = "المغرب",
    bottomLabel: String = "الوقت الحالي"
) {
    val measurer = rememberTextMeasurer()
    val p = progress.coerceIn(0f, 1f)

    Box(modifier = modifier) {
        Canvas(Modifier.fillMaxSize()) {
            // ----- مقياس الـ viewBox داخل المساحة المتاحة (xMidYMid meet) -----
            val s = min(size.width / VB_W, size.height / VB_H)
            val ox0 = (size.width - VB_W * s) / 2f
            val oy0 = (size.height - VB_H * s) / 2f

            fun px(x: Float) = ox0 + x * s
            fun py(y: Float) = oy0 + y * s

            fun unitSp(units: Float): TextUnit =
                TextUnit(units * s / (density * fontScale), TextUnitType.Sp)

            fun paint(
                text: String,
                xUnits: Float,
                baselineUnits: Float,
                fontUnits: Float,
                color: Color,
                weight: FontWeight,
                letterSpacingUnits: Float = 0f,
                centered: Boolean = true
            ) {
                val style = TextStyle(
                    color = color,
                    fontSize = unitSp(fontUnits),
                    fontWeight = weight,
                    letterSpacing = unitSp(letterSpacingUnits)
                )
                val layout = measurer.measure(AnnotatedString(text), style)
                val left = if (centered) px(xUnits) - layout.size.width / 2f else px(xUnits)
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(left, py(baselineUnits) - layout.firstBaseline)
                )
            }

            // ----- مسار القوس -----
            fun buildArc(from: Float, to: Float): Path {
                val path = Path()
                val steps = 100
                val total = ((to - from) * steps).toInt().coerceAtLeast(2)
                for (i in 0..total) {
                    val t = from + (to - from) * (i.toFloat() / total)
                    val (ux, uy) = arcPointUnits(t)
                    if (i == 0) path.moveTo(px(ux), py(uy)) else path.lineTo(px(ux), py(uy))
                }
                return path
            }

            // ظل القوس
            drawPath(
                path = buildArc(0f, 1f),
                color = Color.Black.copy(alpha = 0.5f),
                style = Stroke(width = 8f * s, cap = StrokeCap.Round)
            )
            // خط متقطع رقيق
            drawPath(
                path = buildArc(0f, 1f),
                color = Color.White.copy(alpha = 0.15f),
                style = Stroke(
                    width = 2f * s,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f * s, 8f * s))
                )
            )

            if (p > 0.0005f) {
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
                val progressPath = buildArc(0f, p)
                // توهّج خفيف تحت المسار (بديل drop-shadow)
                drawPath(
                    path = progressPath,
                    brush = progressBrush,
                    style = Stroke(width = 14f * s, cap = StrokeCap.Round),
                    alpha = 0.18f
                )
                drawPath(
                    path = progressPath,
                    brush = progressBrush,
                    style = Stroke(width = 4.5f * s, cap = StrokeCap.Round)
                )
            }

            // ----- الشمس / القمر -----
            val (ux, uy) = arcPointUnits(p)
            val body = Offset(px(ux), py(uy))

            if (!isNight) {
                drawCircle(Color(0x26FF9F1C), radius = 32f * s, center = body)
                drawCircle(Color(0x66FFB703), radius = 18f * s, center = body)
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to Color(0xFFFFFFFF),
                            0.3f to Color(0xFFFFF4CC),
                            0.8f to Color(0xFFFFB703),
                            1f to Color(0xFFFF8C00)
                        ),
                        center = body,
                        radius = 12f * s
                    ),
                    radius = 12f * s,
                    center = body
                )
            } else {
                drawCircle(Color(0x2694A3B8), radius = 30f * s, center = body)
                drawCircle(Color(0x33E2E8F0), radius = 16f * s, center = body)

                // طور القمر: ظل بيضوي داخل قرص مقصوص (نفس معادلة V7)
                val phase = moonPhase.coerceIn(0.0, 1.0)
                val shadowX = if (phase <= 0.5) {
                    14.0 - (phase * 2.0 * 28.0)
                } else {
                    -14.0 + ((phase - 0.5) * 2.0 * 28.0)
                }
                val shadowRx = abs(shadowX)
                val r14 = 14f * s
                val clip = Path().apply { addOval(Rect(center = body, radius = r14)) }
                clipPath(clip) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0f to Color(0xFFFFFFFF),
                                0.4f to Color(0xFFE2E8F0),
                                0.8f to Color(0xFF94A3B8),
                                1f to Color(0xFF475569)
                            ),
                            center = body,
                            radius = r14
                        ),
                        radius = r14,
                        center = body
                    )
                    drawOval(
                        color = Color(0xE60D1117),
                        topLeft = Offset(
                            body.x + (shadowX.toFloat() - shadowRx.toFloat()) * s,
                            body.y - r14
                        ),
                        size = Size(shadowRx.toFloat() * 2f * s, r14 * 2f)
                    )
                }
                // لمعة صغيرة أعلى القمر
                drawCircle(
                    color = Color.White.copy(alpha = 0.6f),
                    radius = 2.5f * s,
                    center = Offset(body.x - 3f * s, body.y - 3f * s)
                )
            }

            // ----- الوقت الحالي في المنتصف (translate(200, 200)) -----
            paint(timeText, 200f, 200f + 4f, 46f, Color.Black.copy(alpha = 0.30f), FontWeight.Bold)
            paint(timeText, 200f, 200f, 46f, Color.White, FontWeight.Bold)
            paint(meridiem, 125f, 214f, 13f, Color.White.copy(alpha = 0.8f), FontWeight.Bold, centered = false)
            paint(bottomLabel, 200f, 240f, 11f, WhiteFaint, FontWeight.Bold, letterSpacingUnits = 4f)

            // ----- طرفا القوس -----
            val left = Offset(px(30f), py(260f))
            val right = Offset(px(370f), py(260f))
            drawCircle(Color.White, radius = 4f * s, center = left)
            drawCircle(Color.White, radius = 4f * s, center = right)
            paint(startLabel, 30f, 285f, 11f, Color.White.copy(alpha = 0.8f), FontWeight.SemiBold)
            paint(endLabel, 370f, 285f, 11f, Color.White.copy(alpha = 0.8f), FontWeight.SemiBold)
        }
    }
}

/** لون «الوقت الحالي» الثانوي (rgba(255,255,255,0.4)) */
private val WhiteFaint = Color(0x66FFFFFF)
