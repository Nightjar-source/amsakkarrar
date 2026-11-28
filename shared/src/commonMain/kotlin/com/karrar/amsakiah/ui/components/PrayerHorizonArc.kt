package com.karrar.amsakiah.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.ui.theme.OnSky
import com.karrar.amsakiah.ui.motion.motionEffects
import com.karrar.amsakiah.ui.motion.motionSpatialFluid
import kotlin.math.PI
import kotlin.math.cos

/**
 * قوس مسار الصلوات والشمس والقمر الأفقي المتكيّف — يُعرض أسفل زر التقويم
 * وفوق بطاقات الصلوات في شاشة القوس الرئيسية، ويتمدد ويتقلص بسلاسة مائية.
 */
@Composable
fun PrayerHorizonArc(
    visibleIds: List<String>,
    todayTimes: Map<String, Double>,
    nowSeconds: Int,
    targetId: String?,
    hijriDay: Int,
    use24Hour: Boolean,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val targetHeight = if (compact) 74.dp else 92.dp
    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = motionSpatialFluid(),
        label = "horizon-arc-height"
    )

    val sunriseHour = todayTimes["sunrise"] ?: 6.0
    val sunsetHour = todayTimes["sunset"] ?: todayTimes["maghrib"] ?: 18.0
    val nowHour = nowSeconds / 3600.0
    val dayLen = (sunsetHour - sunriseHour).coerceAtLeast(1.0)
    val rawDayFrac = ((nowHour - sunriseHour) / dayLen).coerceIn(0.0, 1.0).toFloat()
    val animatedDayFrac by animateFloatAsState(
        targetValue = rawDayFrac,
        animationSpec = motionEffects(),
        label = "horizon-sun-frac"
    )

    val textMeasurer = rememberTextMeasurer()
    val shape = RoundedCornerShape(if (compact) 18.dp else 22.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (compact) 12.dp else 24.dp)
            .height(animatedHeight)
            .clip(shape)
            .background(OnSky.glass.copy(alpha = 0.55f))
            .border(1.dp, OnSky.borderStrong.copy(alpha = 0.45f), shape)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas

            val horizonY = h * 0.56f

            // ظل الليل أسفل خط الأفق + خط الأفق
            drawRect(
                color = Color(0x22000000),
                topLeft = Offset(0f, horizonY),
                size = androidx.compose.ui.geometry.Size(w, h - horizonY)
            )
            drawLine(
                color = Color.White.copy(alpha = 0.22f),
                start = Offset(0f, horizonY),
                end = Offset(w, horizonY),
                strokeWidth = 1.2.dp.toPx()
            )

            // المنحنى الجيبي لمسار الصلوات
            val curveLeft = w * 0.05f
            val curveSpan = w * 0.72f
            val amp = h * 0.34f
            val inactivePath = Path()
            val activePath = Path()
            val steps = 56
            for (i in 0..steps) {
                val t = i / steps.toFloat()
                val x = curveLeft + t * curveSpan
                val wave = cos((t - 0.5f) * 1.45f * PI).toFloat()
                val y = horizonY - wave * amp
                if (i == 0) inactivePath.moveTo(x, y) else inactivePath.lineTo(x, y)
                if (t in 0.16f..0.84f) {
                    if (activePath.isEmpty) activePath.moveTo(x, y) else activePath.lineTo(x, y)
                }
            }

            drawPath(
                path = inactivePath,
                color = Color.White.copy(alpha = 0.26f),
                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
            )
            drawPath(
                path = activePath,
                color = Color(0xFFFFC107),
                style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
            )

            // نقاط وأوقات الصلوات الظاهرة على المنحنى حسب إعدادات المستخدم
            val shown = visibleIds.take(6)
            if (shown.isNotEmpty()) {
                val timeStyle = TextStyle(
                    fontSize = if (compact) 8.5.sp else 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    textDirection = TextDirection.Ltr
                )
                shown.forEachIndexed { idx, id ->
                    val t = if (shown.size == 1) 0.5f else idx.toFloat() / (shown.size - 1).toFloat()
                    val x = w * 0.07f + t * w * 0.68f
                    val wave = cos((t - 0.5f) * 1.45f * PI).toFloat()
                    val y = horizonY - wave * amp
                    val isNext = id == targetId

                    drawCircle(
                        color = if (isNext) Color(0xFFFFD700) else Color.White.copy(alpha = 0.85f),
                        radius = (if (isNext) 3.4f else 2.2f).dp.toPx(),
                        center = Offset(x, y)
                    )

                    val rawHour = todayTimes[id] ?: 0.0
                    val formatted = PrayerCalculator.formatHour(rawHour, use24Hour).first
                    val measured = textMeasurer.measure(
                        text = formatted,
                        style = timeStyle.copy(color = if (isNext) Color(0xFFFFD700) else Color(0xFFE2E8F0))
                    )
                    val tx = (x - measured.size.width / 2f).coerceIn(4f, w - measured.size.width - 4f)
                    val ty = if (y < horizonY) {
                        (y - measured.size.height - 4.dp.toPx()).coerceAtLeast(2f)
                    } else {
                        (y + 4.dp.toPx()).coerceAtMost(h - measured.size.height - 2f)
                    }
                    drawText(measured, topLeft = Offset(tx, ty))
                }
            }

            // الشمس المتوهجة على المسار
            val sunT = 0.16f + animatedDayFrac * (0.84f - 0.16f)
            val sunX = curveLeft + sunT * curveSpan
            val sunWave = cos((sunT - 0.5f) * 1.45f * PI).toFloat()
            val sunY = horizonY - sunWave * amp
            drawCircle(
                color = Color(0x44FFD54F),
                radius = 9.dp.toPx(),
                center = Offset(sunX, sunY)
            )
            drawCircle(
                color = Color(0xFFFFCA28),
                radius = 4.8.dp.toPx(),
                center = Offset(sunX, sunY)
            )

            // القمر الواقعي بملمس السطح وطور الهلال على الطرف
            val moonR = (h * 0.27f).coerceAtMost(24.dp.toPx())
            val moonCx = w - moonR - 10.dp.toPx()
            val moonCy = h * 0.48f
            val moonPhase = (hijriDay.coerceIn(1, 30) - 1) / 29.5f

            drawCircle(
                color = Color(0xFF334155),
                radius = moonR,
                center = Offset(moonCx, moonCy)
            )
            val moonClip = Path().apply {
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        moonCx - moonR,
                        moonCy - moonR,
                        moonCx + moonR,
                        moonCy + moonR
                    )
                )
            }
            clipPath(moonClip) {
                val litOffset = (0.5f - moonPhase) * moonR * 2.2f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFF8FAFC), Color(0xFFCBD5E1), Color(0xFF94A3B8)),
                        center = Offset(moonCx + moonR * 0.2f, moonCy - moonR * 0.2f),
                        radius = moonR * 1.5f
                    ),
                    radius = moonR * 1.05f,
                    center = Offset(moonCx + litOffset, moonCy)
                )
                drawCircle(
                    color = Color(0x24475569),
                    radius = moonR * 0.18f,
                    center = Offset(moonCx + moonR * 0.28f, moonCy - moonR * 0.18f)
                )
                drawCircle(
                    color = Color(0x24475569),
                    radius = moonR * 0.14f,
                    center = Offset(moonCx - moonR * 0.12f, moonCy + moonR * 0.32f)
                )
            }
        }
    }
}
