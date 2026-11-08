package com.karrar.amsakiah.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.resources.Res
import com.karrar.amsakiah.resources.app_icon
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class SplashStar(
    val x: Float,
    val y: Float,
    val radius: Float,
    val phase: Float,
    val speed: Float
)

private enum class FloatingKind { CRESCENT, STAR, LANTERN }

private data class FloatingIcon(
    val kind: FloatingKind,
    val x: Float,
    val y: Float,
    val size: Float,
    val phase: Float,
    val speed: Float,
    val alpha: Float
)

/**
 * شاشة البداية الثانية — منقولة حرفيًا من `appkarv/Startscreen .txt`:
 *
 * - 150 نجمة متلألئة بأحجام وسرعات عشوائية.
 * - 15 أيقونة إسلامية عائمة (هلال، نجمة ثمانية، فانوس) بلون بنفسجي شفاف.
 * - حلقات زحل الثلاث بميل ثلاثي الأبعاد (rotationX/rotationY) ودوران مستمر.
 * - شعار التطبيق الشفاف غير المربّع مع هالة بنفسجية.
 * - عنوان بتدرّج سماوي/وردي/بنفسجي مع ظل، ودخول fade-in-up متدرّج.
 * - توقيع «عبودي» يظهر أخيرًا.
 *
 * الخلفية `#080514` مطابقة تمامًا لخلفية System Splash في themes.xml.
 */
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    val random = remember { Random(2026) }
    val stars = remember {
        List(150) {
            SplashStar(
                x = random.nextFloat(),
                y = random.nextFloat(),
                radius = 0.7f + random.nextFloat() * 2.4f,
                phase = random.nextFloat() * 6.283f,
                speed = 0.35f + random.nextFloat() * 0.8f
            )
        }
    }
    val floating = remember {
        val kinds = FloatingKind.entries
        List(15) { index ->
            FloatingIcon(
                kind = kinds[index % kinds.size],
                x = 0.05f + random.nextFloat() * 0.9f,
                y = 0.05f + random.nextFloat() * 0.9f,
                size = 15f + random.nextFloat() * 22f,
                phase = random.nextFloat() * 6.283f,
                speed = 0.08f + random.nextFloat() * 0.16f,
                alpha = 0.10f + random.nextFloat() * 0.28f
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "splash")
    // زمن دوري 20 ثانية — تُشتق منه كل الحركات لتبقى متزامنة وخفيفة.
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(20_000, easing = LinearEasing)),
        label = "time"
    )
    val ringA by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9_000, easing = LinearEasing)),
        label = "ringA"
    )
    val ringB by transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(14_000, easing = LinearEasing)),
        label = "ringB"
    )
    val ringC by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(22_000, easing = LinearEasing)),
        label = "ringC"
    )

    val iconAnim = remember { Animatable(0f) }
    val titleAnim = remember { Animatable(0f) }
    val signatureAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch {
            kotlinx.coroutines.delay(80)
            iconAnim.animateTo(1f, tween(1200, easing = FastOutSlowInEasing))
        }
        launch {
            kotlinx.coroutines.delay(300)
            titleAnim.animateTo(1f, tween(1400, easing = FastOutSlowInEasing))
        }
        launch {
            kotlinx.coroutines.delay(1100)
            signatureAnim.animateTo(1f, tween(1000, easing = LinearEasing))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF080514), Color(0xFF0B0718), Color(0xFF05030D))
                )
            )
    ) {
        // ---------- النجوم ----------
        Canvas(Modifier.fillMaxSize()) {
            stars.forEach { star ->
                val wave = sin((time * star.speed * 2f).toDouble() * PI + star.phase.toDouble()).toFloat()
                val alpha = (0.20f + 0.80f * ((wave + 1f) / 2f)).coerceIn(0.12f, 1f)
                val radius = star.radius * (0.8f + 0.35f * ((wave + 1f) / 2f))
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = radius.dp.toPx(),
                    center = Offset(star.x * size.width, star.y * size.height)
                )
                // هالة خفيفة للنجوم الكبيرة
                if (star.radius > 2.2f) {
                    drawCircle(
                        color = Color(0xFFB9A7FF).copy(alpha = alpha * 0.18f),
                        radius = radius.dp.toPx() * 3.2f,
                        center = Offset(star.x * size.width, star.y * size.height)
                    )
                }
            }
        }

        // ---------- الأيقونات الإسلامية العائمة ----------
        Canvas(Modifier.fillMaxSize()) {
            floating.forEach { icon ->
                val drift = sin((time * icon.speed * 2f).toDouble() * PI + icon.phase.toDouble()).toFloat()
                val floatY = -22.dp.toPx() * ((drift + 1f) / 2f)
                val center = Offset(
                    icon.x * size.width,
                    icon.y * size.height + floatY
                )
                val px = icon.size.dp.toPx()
                rotate(degrees = drift * 18f, pivot = center) {
                    drawFloatingIcon(icon.kind, center, px, Color(0xFF8B5CF6).copy(alpha = icon.alpha))
                }
            }
        }

        // ---------- المحتوى ----------
        BoxWithConstraints(Modifier.fillMaxSize()) {
            // مقاسات نسبية بدل 310dp الثابتة: تعمل من 320dp حتى الشاشات الكبيرة،
            // وفي الوضع الأفقي، بلا قصّ للحلقات أو الشعار.
            val logoBox = minOf(maxWidth - 48.dp, maxHeight * 0.46f).coerceIn(170.dp, 310.dp)
            val unit = logoBox.value / 310f
            val titleSize = (36f * unit).coerceIn(22f, 36f).sp

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(logoBox),
                    contentAlignment = Alignment.Center
                ) {
                    // هالة بنفسجية خلف الشعار
                    Canvas(Modifier.size(logoBox * 0.71f)) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    Color(0x668B5CF6),
                                    Color(0x338B5CF6),
                                    Color.Transparent
                                )
                            ),
                            radius = size.minDimension / 2f
                        )
                    }

                    // حلقات زحل — ميل ثلاثي الأبعاد عبر graphicsLayer
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                rotationX = 72f
                                rotationY = -14f
                                cameraDistance = 18f
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Ring(rotation = ringA, diameter = logoBox, color = Color(0xFF00F2FE), width = 2.5f * unit)
                        Ring(rotation = ringB, diameter = logoBox * 0.823f, color = Color(0xFFA18CD1), width = 2f * unit)
                        Ring(rotation = ringC, diameter = logoBox * 0.677f, color = Color(0xFFFBC2EB), width = 1.2f * unit)
                    }

                    // الشعار الشفاف كما هو — دون قصّ دائري أو مربّع
                    Image(
                        painter = painterResource(Res.drawable.app_icon),
                        contentDescription = "شعار إمساكية كرار",
                        modifier = Modifier
                            .size(logoBox * 0.53f)
                            .graphicsLayer {
                                alpha = iconAnim.value
                                scaleX = 0.88f + 0.12f * iconAnim.value
                                scaleY = 0.88f + 0.12f * iconAnim.value
                            }
                    )
                }

                Spacer(Modifier.height(logoBox * 0.084f))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer {
                        alpha = titleAnim.value
                        translationY = (1f - titleAnim.value) * 40.dp.toPx()
                    }
                ) {
                    Text(
                        text = "إمساكية كرار",
                        style = TextStyle(
                            brush = Brush.linearGradient(
                                listOf(
                                    Color(0xFF00F2FE),
                                    Color(0xFFFBC2EB),
                                    Color(0xFFA18CD1)
                                )
                            ),
                            fontSize = titleSize,
                            fontWeight = FontWeight.Bold,
                            shadow = Shadow(
                                color = Color(0x6600F2FE),
                                blurRadius = 18f
                            )
                        )
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "علي المرتضى والمهدي",
                        color = Color(0xE6E5E7EB),
                        fontSize = (17f * unit).coerceIn(13f, 17f).sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "رمضان ١٤٤٧ هـ — ٢٠٢٦ م",
                        color = Color(0xFF6C7590),
                        fontSize = (11f * unit).coerceIn(9f, 11f).sp
                    )
                }
            }
        }

        // ---------- التوقيع ----------
        Text(
            text = "عبودي",
            color = Color(0xFF4A5068),
            fontSize = 12.sp,
            letterSpacing = 4.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 30.dp)
                .graphicsLayer { alpha = signatureAnim.value }
        )
    }
}

@Composable
private fun Ring(rotation: Float, diameter: androidx.compose.ui.unit.Dp, color: Color, width: Float) {
    Canvas(
        modifier = Modifier
            .size(diameter)
            .graphicsLayer { rotationZ = rotation }
    ) {
        val stroke = width.dp.toPx()
        // توهج خارجي خفيف
        drawCircle(
            color = color.copy(alpha = 0.18f),
            radius = size.minDimension / 2f - stroke,
            style = Stroke(width = stroke * 4f)
        )
        drawCircle(
            brush = Brush.sweepGradient(
                listOf(Color.Transparent, color, color.copy(alpha = 0.25f), Color.Transparent)
            ),
            radius = size.minDimension / 2f - stroke,
            style = Stroke(width = stroke)
        )
    }
}

private fun DrawScope.drawFloatingIcon(kind: FloatingKind, center: Offset, sizePx: Float, color: Color) {
    val r = sizePx / 2f
    when (kind) {
        FloatingKind.CRESCENT -> {
            val outer = Path().apply {
                addOval(Rect(center = center, radius = r))
            }
            val inner = Path().apply {
                addOval(
                    Rect(
                        center = Offset(center.x + r * 0.34f, center.y - r * 0.10f),
                        radius = r * 0.86f
                    )
                )
            }
            val crescent = Path().apply {
                op(outer, inner, PathOperation.Difference)
            }
            drawPath(crescent, color)
        }
        FloatingKind.STAR -> {
            val path = Path()
            val points = 8
            val outerR = r
            val innerR = r * 0.42f
            for (i in 0 until points * 2) {
                val radius = (if (i % 2 == 0) outerR else innerR).toDouble()
                val angle = (-PI / 2.0) + i * PI / points
                val x = center.x + (radius * cos(angle)).toFloat()
                val y = center.y + (radius * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, color)
        }
        FloatingKind.LANTERN -> {
            // غطاء
            drawRect(
                color = color,
                topLeft = Offset(center.x - r * 0.34f, center.y - r * 0.95f),
                size = Size(r * 0.68f, r * 0.20f)
            )
            // جسم الفانوس
            drawRoundRect(
                color = color,
                topLeft = Offset(center.x - r * 0.55f, center.y - r * 0.72f),
                size = Size(r * 1.10f, r * 1.30f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(r * 0.18f, r * 0.18f)
            )
            // قاعدة
            drawRect(
                color = color,
                topLeft = Offset(center.x - r * 0.34f, center.y + r * 0.60f),
                size = Size(r * 0.68f, r * 0.22f)
            )
            // فتحة الضوء
            drawRoundRect(
                color = Color(0xFF080514).copy(alpha = 0.55f),
                topLeft = Offset(center.x - r * 0.26f, center.y - r * 0.36f),
                size = Size(r * 0.52f, r * 0.66f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(r * 0.10f, r * 0.10f)
            )
        }
    }
}
