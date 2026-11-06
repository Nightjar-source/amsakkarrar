package com.karrar.amsakiah.ui.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.resources.Res
import com.karrar.amsakiah.resources.app_icon
import com.karrar.amsakiah.ui.theme.KararColors
import org.jetbrains.compose.resources.painterResource
import kotlin.math.sin
import kotlin.random.Random

private data class Star(val x: Float, val y: Float, val r: Float, val phase: Float, val speed: Float)

/**
 * شاشة البداية — منقولة من appkarv/Startscreen.txt:
 * نجوم متلألئة، حلقات مدارية، الأيقونة، والتوقيع.
 */
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    val random = remember { Random(42) }
    val stars = remember {
        List(150) {
            Star(
                x = random.nextFloat(),
                y = random.nextFloat(),
                r = 0.6f + random.nextFloat() * 1.8f,
                phase = random.nextFloat() * 6.28f,
                speed = 1.2f + random.nextFloat() * 2.4f
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "splash")
    val twinkle by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Reverse),
        label = "twinkle"
    )
    val ringA by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "ringA"
    )
    val ringB by transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing)),
        label = "ringB"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF080514), Color(0xFF0B0718), Color(0xFF05030D))
                )
            )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            stars.forEachIndexed { index, star ->
                val wave = sin((star.phase + index * 0.17f).toDouble()).toFloat()
                val alpha = (0.35f + 0.65f * ((wave + 1f) / 2f)).coerceIn(0.2f, 1f)
                drawCircle(
                    color = Color.White.copy(alpha = alpha * (0.4f + twinkle * 0.6f)),
                    radius = star.r.dp.toPx(),
                    center = Offset(star.x * size.width, star.y * size.height)
                )
            }
        }

        // الحلقات
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(280.dp)
                .rotate(ringA)
                .alpha(0.55f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(Color(0x0000F2FE), Color(0xFF00F2FE), Color(0x0000F2FE))
                    ),
                    radius = size.minDimension / 2f - 4.dp.toPx(),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(340.dp)
                .rotate(ringB)
                .alpha(0.4f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(Color(0x00A18CD1), Color(0xFFA18CD1), Color(0x00A18CD1))
                    ),
                    radius = size.minDimension / 2f - 4.dp.toPx(),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }

        Column(
            modifier = Modifier.align(Alignment.Center).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.app_icon),
                contentDescription = "شعار إمساكية كرار",
                modifier = Modifier
                    .size(148.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.height(28.dp))
            Text(
                text = "إمساكية كرار",
                style = TextStyle(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF00F2FE), Color(0xFFFBC2EB), Color(0xFFA18CD1))
                    ),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "علي المرتضى والمهدي",
                color = Color(0xFF9AA4C0),
                fontSize = 13.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "رمضان ١٤٤٧ هـ — ٢٠٢٦ م",
                color = Color(0xFF6C7590),
                fontSize = 11.sp
            )
        }

        Text(
            text = "عبودي",
            color = Color(0xFF4A5068),
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
        )
    }
}
