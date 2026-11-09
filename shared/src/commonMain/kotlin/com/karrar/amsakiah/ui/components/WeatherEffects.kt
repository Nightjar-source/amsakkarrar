package com.karrar.amsakiah.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.sin

/**
 * تأثيرات الطقس الخلفية المستخرجة من `WeatherEffects` في `amsak3d.tsx`.
 *
 * - سحاب: طبقة ضباب علوية + عناقيد بيضوية شفافة تنجرف ببطء (85 ثانية).
 * - مطر: خطوط مائلة متكررة تسقط كل 0.65 ثانية.
 * - ثلج: حبيبات بيضاء تسقط ببطء كل 5.5 ثانية.
 *
 * كل الحركات تُقرأ داخل `Canvas` فقط (`State.value` داخل draw) حتى لا يُعاد تركيب
 * الشاشة كل إطار، ولا يوجد أي وميض أو إعادة إنشاء عند تغيّر الحالة.
 */
@Composable
fun WeatherEffects(
    code: Int?,
    isNight: Boolean,
    modifier: Modifier = Modifier
) {
    if (code == null) return
    val isCloudy = code in 1..48
    val isRain = (code in 51..67) || (code in 80..82) || code >= 95
    val isSnow = (code in 71..77) || code == 85 || code == 86
    if (!isCloudy && !isRain && !isSnow) return

    val transition = rememberInfiniteTransition(label = "weather")
    // الخفة: كل تأثير يُنشئ مؤشّره فقط عند الحاجة. قبل ذلك كانت مؤشّرات
    // المطر والثلج تعمل حتى في يوم صافٍ فيه غيوم فقط، فتُبطل رسم الطبقة
    // 60 مرة في الثانية بلا سبب.
    val drift: State<Float>? = if (isCloudy || isRain || isSnow) {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 85_000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "cloud-drift"
        )
    } else null
    val rain: State<Float>? = if (isRain) {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 650, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rain-fall"
        )
    } else null
    val snow: State<Float>? = if (isSnow) {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 5_500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "snow-fall"
        )
    } else null

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (isCloudy || isRain || isSnow) {
            // ضباب/توهج علوي (radial-gradient ellipse at 50% 0%)
            val haze = if (isNight) {
                listOf(
                    Color(0xFF94A3B8).copy(alpha = 0.12f),
                    Color(0xFF475569).copy(alpha = 0.04f),
                    Color.Transparent
                )
            } else {
                listOf(
                    Color.White.copy(alpha = 0.16f),
                    Color(0xFFE2E8F0).copy(alpha = 0.06f),
                    Color.Transparent
                )
            }
            drawRect(
                brush = Brush.radialGradient(
                    colors = haze,
                    center = Offset(width / 2f, 0f),
                    radius = height * 0.55f
                )
            )

            // غيوم ناعمة مبنية من بيضاويات متداخلة
            val cloudTop = if (isNight) Color(0xFF94A3B8) else Color.White
            val cloudMid = if (isNight) Color(0xFF64748B) else Color(0xFFE2E8F0)
            val cloudAlpha = if (isRain) 0.42f else 0.55f
            val cloudBrush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0f to cloudTop.copy(alpha = cloudAlpha),
                    0.65f to cloudMid.copy(alpha = 0.22f),
                    1f to Color.Transparent
                ),
                startY = 0f,
                endY = height * 0.42f
            )
            val hazeBrush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0f to cloudMid.copy(alpha = 0.38f),
                    1f to Color.Transparent
                ),
                startY = 0f,
                endY = height * 0.30f
            )

            val driftPx = ((drift?.value ?: 0.5f) - 0.5f) * 28.dp.toPx()
            val unit = width.coerceAtLeast(1f)
            val clusterWide = unit * 0.12f
            val clusterBodyW = unit * 0.26f
            val clusterBodyH = unit * 0.11f
            val clusterMidW = unit * 0.19f
            val clusterMidH = unit * 0.13f
            val clusterMidDx = unit * 0.05f
            val clusterMidDy = unit * 0.055f
            val clusterSideW = unit * 0.21f
            val clusterSideH = unit * 0.10f
            val clusterSideDx = unit * 0.04f
            val clusterSideDy = unit * 0.008f

            fun cloudCluster(cx: Float, baseY: Float, scale: Float, dx: Float, brush: Brush) {
                drawOval(
                    brush = brush,
                    topLeft = Offset(cx - clusterWide * scale + dx, baseY),
                    size = Size(clusterBodyW * scale, clusterBodyH * scale)
                )
                drawOval(
                    brush = brush,
                    topLeft = Offset(cx - clusterMidDx * scale + dx, baseY - clusterMidDy * scale),
                    size = Size(clusterMidW * scale, clusterMidH * scale)
                )
                drawOval(
                    brush = brush,
                    topLeft = Offset(cx + clusterSideDx * scale + dx, baseY - clusterSideDy * scale),
                    size = Size(clusterSideW * scale, clusterSideH * scale)
                )
            }

            cloudCluster(width * 0.20f, height * 0.12f, 1f, driftPx, cloudBrush)
            cloudCluster(width * 0.76f, height * 0.10f, 0.9f, -driftPx, cloudBrush)
            if (code >= 2 || isRain || isSnow) {
                cloudCluster(width * 0.50f, height * 0.21f, 0.78f, driftPx * 0.7f, hazeBrush)
                cloudCluster(width * 0.02f, height * 0.24f, 0.62f, -driftPx * 0.6f, hazeBrush)
            }
        }

        if (isRain) {
            // تبليط 60×90dp مطابق لخلفية المطر في المرجع
            val tileW = 60.dp.toPx()
            val tileH = 90.dp.toPx()
            val offsetY = (rain?.value ?: 0f) * tileH
            val cols = ceil(width / tileW).toInt().coerceAtLeast(1) + 1
            val rows = ceil(height / tileH).toInt().coerceAtLeast(1) + 2
            val light = Color(0xFFBAE6FD).copy(alpha = 0.55f)
            val pale = Color(0xFFE0F2FE).copy(alpha = 0.42f)
            val stroke1 = 1.1.dp.toPx()
            val stroke2 = 0.9.dp.toPx()
            val x1a = 18.dp.toPx()
            val y1a = 10.dp.toPx()
            val x1b = 12.dp.toPx()
            val y1b = 36.dp.toPx()
            val x2a = 48.dp.toPx()
            val y2a = 42.dp.toPx()
            val x2b = 42.dp.toPx()
            val y2b = 68.dp.toPx()
            for (col in 0 until cols) {
                val x = col * tileW
                for (row in -1 until rows) {
                    val y = row * tileH + offsetY
                    drawLine(
                        color = light,
                        start = Offset(x + x1a, y + y1a),
                        end = Offset(x + x1b, y + y1b),
                        strokeWidth = stroke1,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = pale,
                        start = Offset(x + x2a, y + y2a),
                        end = Offset(x + x2b, y + y2b),
                        strokeWidth = stroke2,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        if (isSnow) {
            // تبليط 80×80dp — ثلاث حبيبات لكل بلاطة
            val tile = 80.dp.toPx()
            val phase = snow?.value ?: 0f
            val offsetY = phase * tile
            val cols = ceil(width / tile).toInt().coerceAtLeast(1) + 1
            val rows = ceil(height / tile).toInt().coerceAtLeast(1) + 2
            val swayAmp = 4.dp.toPx()
            val r1 = 1.7.dp.toPx()
            val r2 = 1.2.dp.toPx()
            val r3 = 1.4.dp.toPx()
            val x1 = 20.dp.toPx()
            val x2 = 58.dp.toPx()
            val x3 = 35.dp.toPx()
            val y1 = 20.dp.toPx()
            val y2 = 48.dp.toPx()
            val y3 = 68.dp.toPx()
            val white78 = Color.White.copy(alpha = 0.78f)
            val pale62 = Color(0xFFE2E8F0).copy(alpha = 0.62f)
            val white70 = Color.White.copy(alpha = 0.70f)
            val angleBase = phase * 2f * PI.toFloat()
            for (col in 0 until cols) {
                val x = col * tile
                for (row in -1 until rows) {
                    val y = row * tile + offsetY
                    val sway = sin(angleBase + col + row) * swayAmp
                    drawCircle(
                        color = white78,
                        radius = r1,
                        center = Offset(x + x1 + sway, y + y1)
                    )
                    drawCircle(
                        color = pale62,
                        radius = r2,
                        center = Offset(x + x2 - sway, y + y2)
                    )
                    drawCircle(
                        color = white70,
                        radius = r3,
                        center = Offset(x + x3 + sway * 0.5f, y + y3)
                    )
                }
            }
        }
    }
}
