package com.karrar.amsakiah.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.ui.theme.SkyTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SkyArcView(
    currentSeconds: Float,
    cityName: String,
    nextPrayerName: String,
    timeRemainingText: String,
    modifier: Modifier = Modifier
) {
    val (bgTop, bgBot, glowColor) = remember(currentSeconds) {
        SkyTheme.getSkyColorsForSecond(currentSeconds)
    }

    val infiniteTransition = rememberInfiniteTransition()
    val starTwinkle by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(290.dp)
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Brush.verticalGradient(listOf(bgTop, bgBot)))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val isNightOrDawn = currentSeconds < 6f * 3600f || currentSeconds > 18.5f * 3600f
            if (isNightOrDawn) {
                SkyTheme.celestialStars.forEach { star ->
                    val cx = (star.xPercent / 100f) * width
                    val cy = (star.yPercent / 100f) * height
                    val alpha = (star.maxAlpha * starTwinkle).coerceIn(0f, 1f)
                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = star.radius * 2.5f,
                        center = Offset(cx, cy)
                    )
                }
            }

            val arcPadding = 40.dp.toPx()
            val arcRect = androidx.compose.ui.geometry.Rect(
                offset = Offset(arcPadding, 70.dp.toPx()),
                size = Size(width - (arcPadding * 2), height * 1.5f)
            )

            drawArc(
                color = Color.White.copy(alpha = 0.25f),
                startAngle = 190f,
                sweepAngle = 160f,
                useCenter = false,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                ),
                topLeft = arcRect.topLeft,
                size = arcRect.size
            )

            val dayFraction = ((currentSeconds - (6f * 3600f)) / (12f * 3600f)).coerceIn(0f, 1f)
            val angleDeg = 190f + (dayFraction * 160f)
            val angleRad = (angleDeg * PI / 180f).toFloat()

            val rx = arcRect.width / 2f
            val ry = arcRect.height / 2f
            val cx = arcRect.center.x + rx * cos(angleRad)
            val cy = arcRect.center.y + ry * sin(angleRad)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    center = Offset(cx, cy),
                    radius = 45.dp.toPx()
                ),
                radius = 45.dp.toPx(),
                center = Offset(cx, cy)
            )

            val isDay = currentSeconds in (6f * 3600f)..(18f * 3600f)
            drawCircle(
                color = if (isDay) Color(0xFFFFD54F) else Color(0xFFF0F4F8),
                radius = 12.dp.toPx(),
                center = Offset(cx, cy)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.28f),
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = cityName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.28f),
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFF81C784),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "الأذان مفعل",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Black.copy(alpha = 0.35f),
                contentColor = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "الصلاة القادمة: $nextPrayerName",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = timeRemainingText,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFE082),
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
