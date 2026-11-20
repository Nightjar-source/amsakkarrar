package com.karrar.amsakiah.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
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
import com.karrar.amsakiah.ui.motion.ambientMotionEnabled
import com.karrar.amsakiah.ui.platform.rememberReduceMotion
import com.karrar.amsakiah.ui.startup.StartupGate
import com.karrar.amsakiah.ui.theme.KararFonts
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val TWO_PI = (2.0 * PI).toFloat()

/** منحنى المرجع `cubic-bezier(0.2, 0.8, 0.2, 1)` — نفس منحنى fadeInUp في Startscreen. */
private val ReferenceEasing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)

private data class SplashStar(
    val x: Float,
    val y: Float,
    val radius: Float,
    val phase: Float,
    val period: Float,
    /** تأخير الدخول (0–2s) كما في `animationDelay` في المرجع. */
    val delay: Float
)

private enum class FloatingKind { CRESCENT, STAR, LANTERN }

private data class FloatingIcon(
    val kind: FloatingKind,
    val x: Float,
    val y: Float,
    val size: Float,
    val phase: Float,
    val period: Float,
    val alpha: Float
)

/**
 * شاشة البداية — نسخة مطابقة بنيويًا لـ `appkarv/Startscreen .txt`:
 *
 * - 150 نجمة دائرية تومض (opacity ‏0.2→1، scale ‏0.8→1.2) بفترة 2–5s وتأخير 0–2s،
 *   والكبيرة منها وهّاجة (box-shadow أبيض في المرجع).
 * - 15 أيقونة إسلامية عائمة (هلال، نجمة خماسية، فانوس) بلون `#8B5CF6`، تطفو
 *   ‏-30px وتدور 15° بفترة 10–20s بتناوب.
 * - حلقات زحل الثلاث بميل `rotateX(75°) rotateY(-15°)`: خارجية سماوية `#00F2FE`
 *   (قوس 270°، فجوة عند القمة، تدور)، وسطى بنفسجية `#A18CD1` (قوس 270°، فجوة
 *   عند القاع، تدور عكسيًا)، داخلية وردية `#FBC2EB` (دائرة كاملة خافتة) —
 *   كلها بدوران بطيء هادئ.
 * - شعار التطبيق بهالة بنفسجية `#8B5CF6` (shadow ‏`0 0 50px rgba(139,92,246,0.6)` في المرجع).
 * - العنوان «إمساكية كرار» بتدرّج `#00F2FE → #FBC2EB → #A18CD1` مع
 *   `drop-shadow(0 0 10px rgba(255,255,255,0.3))` وخط Reem Kufi — بلا رقم
 *   إصدار، وبلا كلمة «رمضان».
 * - التوقيع «عبودي» يظهر أخيرًا بشفافية رمادية خافتة.
 *
 * التسلسل الزمني (يطابق المرجع):
 * t=0.1s دخول الكون (fade + translateY 40px، ‏cubic-bezier(0.2,0.8,0.2,1)‎) ←
 * t=0.3s دخول العنوان (fadeInUp ‏1.5s بمنحنى المرجع) ←
 * t=1.1s ظهور التوقيع (تلاشي ‏1s) — وتختفي الشاشة تلقائيًا عبر `showSplash`
 * في App.kt (‏~1.6s‎) مع fadeOut، فلا بقاء دائم — النجوم والحلقات لا تعيش
 * إلا ما دامت الشاشة معروضة.
 *
 * الخلفية `#080514` مطابقة تمامًا لخلفية System Splash، كما في المرجع.
 */
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    val random = remember { Random(2026) }
    val stars = remember {
        List(150) {
            SplashStar(
                x = random.nextFloat(),
                y = random.nextFloat(),
                radius = 0.8f + random.nextFloat() * 2.3f,
                phase = random.nextFloat() * TWO_PI,
                period = 2f + random.nextFloat() * 3f,   // 2–5s كما في المرجع
                delay = random.nextFloat() * 2f           // 0–2s animationDelay
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
                size = 15f + random.nextFloat() * 20f,   // 15–35px كما في المرجع
                phase = random.nextFloat() * TWO_PI,
                period = 10f + random.nextFloat() * 10f, // 10–20s duration كما في المرجع
                alpha = 0.10f + random.nextFloat() * 0.30f
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "splash")
    // «تقليل الحركة»: مشهد ساكن تمامًا — لا وميض نجوم ولا دوران حلقات،
    // والعناصر تظهر في مكانها النهائي فورًا.
    val ambient = ambientMotionEnabled()
    val reduceMotion = rememberReduceMotion()

    // ساعة واحدة بالثواني تغذّي النجوم والأيقونات العائمة. الدورة ساعتان —
    // طويلة لدرجة أن إعادة الضبط لا تظهر في جلسة واقعية، وقيم الدخول
    // (opacity 0.2، scale 0.8) مطابقة للإطار 0% في keyframes المرجع.
    val clockState: State<Float> = if (ambient) {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 7200f,
            animationSpec = infiniteRepeatable(tween(7_200_000, easing = LinearEasing)),
            label = "clock"
        )
    } else remember { mutableStateOf(0f) }

    // دوران بطيء وهادئ — لا دوخة: الخارجية 12s، والوسطى 18s عكسيًا، والداخلية 26s.
    val ringAState: State<Float> = if (ambient) {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(12_000, easing = LinearEasing)),
            label = "ringA"
        )
    } else remember { mutableStateOf(0f) }
    val ringBState: State<Float> = if (ambient) {
        transition.animateFloat(
            initialValue = 360f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(tween(18_000, easing = LinearEasing)),
            label = "ringB"
        )
    } else remember { mutableStateOf(0f) }
    val ringCState: State<Float> = if (ambient) {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(26_000, easing = LinearEasing)),
            label = "ringC"
        )
    } else remember { mutableStateOf(0f) }

    val universeAnim = remember { Animatable(0f) }   // دخول الكون (المرجع: t=0.1s، duration-1000)
    val titleAnim = remember { Animatable(0f) }      // دخول العنوان (المرجع: t=0.3s، fadeInUp 1.5s)
    val signatureAnim = remember { Animatable(0f) }  // التوقيع (المرجع: delay-1000 بعد isLoaded)

    // الوصل مع سبلاش النظام: بعد أول إطار مرسوم لهذه الشاشة نسمح للنظام
    // بإزالة سبلاشه، فلا يظهر فراغ/وميض بين الشاشتين.
    LaunchedEffect(Unit) {
        withFrameNanos { }
        StartupGate.markContentReady()
    }

    LaunchedEffect(Unit) {
        if (reduceMotion) {
            // «تقليل الحركة»: يظهر كل شيء في مكانه النهائي فورًا.
            universeAnim.snapTo(1f)
            titleAnim.snapTo(1f)
            signatureAnim.snapTo(1f)
            return@LaunchedEffect
        }
        launch {
            kotlinx.coroutines.delay(100) // isLoaded في المرجع: setTimeout(…, 100)
            universeAnim.animateTo(1f, tween(1000, easing = ReferenceEasing))
        }
        launch {
            kotlinx.coroutines.delay(300) // animationDelay: '0.3s' في المرجع
            titleAnim.animateTo(1f, tween(1500, easing = ReferenceEasing))
        }
        launch {
            kotlinx.coroutines.delay(1100) // delay-1000 بعد isLoaded في المرجع
            signatureAnim.animateTo(1f, tween(1000, easing = LinearEasing))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            // #080514 — خلفية المرجع حرفيًا
            .background(Color(0xFF080514))
    ) {
        // ---------- النجوم (twinkle في المرجع: 0.2→1 opacity، 0.8→1.2 scale) ----------
        Canvas(Modifier.fillMaxSize()) {
            val clock = clockState.value
            stars.forEach { star ->
                val t = (clock - star.delay).coerceAtLeast(0f)
                val wave = (1f - cos(TWO_PI * (t / star.period) + star.phase)) / 2f
                val alpha = (0.2f + 0.8f * wave).coerceIn(0.12f, 1f)
                val radius = star.radius * (0.8f + 0.4f * wave)
                val center = Offset(star.x * size.width, star.y * size.height)
                // توهّج الذروة للنجوم الكبيرة — ما يقابل box-shadow: 0 0 10px rgba(255,255,255,0.8)
                if (star.radius > 2.0f) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.35f * wave),
                        radius = radius.dp.toPx() * 3.4f,
                        center = center
                    )
                }
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = radius.dp.toPx(),
                    center = center
                )
            }
        }

        // ---------- الأيقونات الإسلامية العائمة (#8B5CF6، float في المرجع) ----------
        Canvas(Modifier.fillMaxSize()) {
            val clock = clockState.value
            floating.forEach { icon ->
                val wave = (1f - cos(TWO_PI * (clock / icon.period) + icon.phase)) / 2f
                // translateY(0 → -30px) و rotate(0° → 15°) كما في keyframes المرجع
                val floatY = -30.dp.toPx() * wave
                val center = Offset(
                    icon.x * size.width,
                    icon.y * size.height + floatY
                )
                val px = icon.size.dp.toPx()
                rotate(degrees = wave * 15f, pivot = center) {
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
            val titleSize = (48f * unit).coerceIn(28f, 48f).sp // text-5xl في المرجع

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp)
                    // دخول الكون: opacity + translateY(40px→0) خلال 1000ms — كما في transition-all duration-1000
                    .graphicsLayer {
                        alpha = universeAnim.value
                        translationY = (1f - universeAnim.value) * 40.dp.toPx()
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(logoBox),
                    contentAlignment = Alignment.Center
                ) {
                    // هالة بنفسجية خلف الشعار — المرجع: shadow-[0_0_50px_rgba(139,92,246,0.6)]
                    Canvas(Modifier.size(logoBox * 0.71f)) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(
                                    Color(0x998B5CF6),
                                    Color(0x4D8B5CF6),
                                    Color(0x1F8B5CF6),
                                    Color.Transparent
                                )
                            ),
                            radius = size.minDimension / 2f
                        )
                    }

                    // حلقات زحل — rotateX(75deg) rotateY(-15deg) preserve-3d في المرجع
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                rotationX = 75f
                                rotationY = -15f
                                cameraDistance = 18f
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val arcStroke = 3f * unit
                        // الحلقة الخارجية (#00F2FE، opacity 0.7): قوس 270° بفجوة عند القمة، تدور
                        SaturnRing(
                            rotation = { ringAState.value },
                            diameter = logoBox,
                            color = Color(0xFF00F2FE).copy(alpha = 0.7f),
                            glowColor = Color(0xFF00F2FE).copy(alpha = 0.30f),
                            strokeDp = arcStroke
                        )
                        // الحلقة الوسطى (#A18CD1، opacity 0.8): قوس 270° بفجوة عند القاع، تدور عكسيًا
                        SaturnRing(
                            rotation = { ringBState.value },
                            diameter = logoBox * 0.861f,
                            color = Color(0xFFA18CD1).copy(alpha = 0.8f),
                            glowColor = Color(0xFFA18CD1).copy(alpha = 0.35f),
                            strokeDp = 2f * unit,
                            gapAtBottom = true
                        )
                        // الحلقة الداخلية (#FBC2EB، opacity 0.4): دائرة كاملة، دوران بطيء
                        SaturnRing(
                            rotation = { ringCState.value },
                            diameter = logoBox * 0.75f,
                            color = Color(0xFFFBC2EB).copy(alpha = 0.4f),
                            glowColor = Color(0xFFFBC2EB).copy(alpha = 0.15f),
                            strokeDp = 1f * unit,
                            fullCircle = true
                        )
                    }

                    // الشعار الشفاف كما هو — دون قصّ دائري أو مربّع
                    Image(
                        painter = painterResource(Res.drawable.app_icon),
                        contentDescription = "شعار إمساكية كرار",
                        modifier = Modifier
                            .size(logoBox * 0.53f)
                            .graphicsLayer {
                                scaleX = 0.88f + 0.12f * universeAnim.value
                                scaleY = 0.88f + 0.12f * universeAnim.value
                            }
                    )
                }

                Spacer(Modifier.height(logoBox * 0.084f))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer {
                        // fadeInUp: translateY(40px→0) + opacity — كما في المرجع
                        alpha = titleAnim.value
                        translationY = (1f - titleAnim.value) * 40.dp.toPx()
                    }
                ) {
                    Text(
                        text = "إمساكية كرار",
                        style = TextStyle(
                            // تدرّج المرجع: from-[#00f2fe] via-[#fbc2eb] to-[#a18cd1]
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF00F2FE),
                                    Color(0xFFFBC2EB),
                                    Color(0xFFA18CD1)
                                )
                            ),
                            fontSize = titleSize,
                            fontWeight = FontWeight.Bold,
                            fontFamily = KararFonts.ReemKufi,
                            // المرجع: drop-shadow-[0_0_10px_rgba(255,255,255,0.3)]
                            shadow = Shadow(
                                color = Color(0x4DFFFFFF),
                                blurRadius = 10f
                            )
                        )
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "علي المرتضى والمهدي",
                        color = Color(0xE6E5E7EB), // gray-300/90 في المرجع
                        fontSize = (22f * unit).coerceIn(15f, 22f).sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = KararFonts.ReemKufi,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // ---------- التوقيع «عبودي» — يظهر أخيرًا (المرجع: bottom-8، gray-500/40) ----------
        Text(
            text = "عبودي",
            color = Color(0x666B7280), // gray-500/40 في المرجع
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = KararFonts.ReemKufi,
            letterSpacing = 4.sp, // tracking-widest
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp) // bottom-8 في المرجع
                .graphicsLayer { alpha = signatureAnim.value }
        )
    }
}

/**
 * حلقة زحل — قوس 270° (بفجوة 90°) أو دائرة كاملة.
 * في المرجع: الحلقة الخارجية بلا حد علوي (border-t-transparent) والوسطى بلا حد
 * سفلي (border-b-transparent)، مع `ring-glow` (توهج خارجي + داخلي).
 */
@Composable
private fun SaturnRing(
    rotation: () -> Float,
    diameter: androidx.compose.ui.unit.Dp,
    color: Color,
    glowColor: Color,
    strokeDp: Float,
    gapAtBottom: Boolean = false,
    fullCircle: Boolean = false
) {
    Canvas(
        modifier = Modifier
            .size(diameter)
            // القراءة داخل كتلة الطبقة (draw) لا في التركيب: لا إعادة تركيب لكل إطار.
            .graphicsLayer { rotationZ = rotation() }
    ) {
        val stroke = strokeDp.dp.toPx()
        val radius = size.minDimension / 2f - stroke
        // توهج ring-glow: هالة عريضة خلف القوس
        if (fullCircle) {
            drawCircle(
                color = glowColor.copy(alpha = glowColor.alpha * 0.6f),
                radius = radius,
                style = Stroke(width = stroke * 4f)
            )
            drawCircle(
                color = color,
                radius = radius,
                style = Stroke(width = stroke)
            )
        } else {
            val gapDeg = 90f
            // 0° عند الساعة 3، إيجابي باتجاه عقارب الساعة؛ القمة = ‏-90°
            val startAngle = if (gapAtBottom) 90f + gapDeg / 2f else -90f + gapDeg / 2f
            val sweep = 360f - gapDeg
            // توهج خارجي عريض خلف القوس (ما يقابل box-shadow الخارجي)
            drawArc(
                color = glowColor,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = stroke * 4f, cap = StrokeCap.Round)
            )
            // القوس نفسه
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * الأيقونات العائمة — نفس أشكال SVG في المرجع (viewBox 0 0 24 24):
 * هلال (دائرة ناقص دائرة مزاحة)، نجمة خماسية (المسار الحرفي M12 2L14.8 9.2H22…)،
 * وفانوس هندسي (ثلاثة شبه منحرفات: M10 2L14 2L15 6L9 6Z ثم الجسم ثم القاعدة).
 */
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
            val crescent = Path().apply { op(outer, inner, PathOperation.Difference) }
            drawPath(crescent, color)
        }
        FloatingKind.STAR -> {
            // نجمة خماسية كما في مسار المرجع — نسبة الداخل/الخارج ‏≈ 0.382
            val path = Path()
            val points = 5
            val outerR = r
            val innerR = r * 0.382f
            for (i in 0 until points * 2) {
                val radius = if (i % 2 == 0) outerR else innerR
                val angle = (-PI / 2.0) + i * PI / points
                val x = center.x + (radius * cos(angle)).toFloat()
                val y = center.y + (radius * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, color)
        }
        FloatingKind.LANTERN -> {
            // إحداثيات المرجع حرفيًا (viewBox 24×24) مع تطبيعها إلى [-1, 1] حول المركز
            fun pt(x: Float, y: Float) = Offset(center.x + (x - 12f) / 12f * r, center.y + (y - 12f) / 12f * r)
            // الغطاء: M10 2L14 2L15 6L9 6Z
            val roof = Path().apply {
                moveTo(pt(10f, 2f).x, pt(10f, 2f).y)
                lineTo(pt(14f, 2f).x, pt(14f, 2f).y)
                lineTo(pt(15f, 6f).x, pt(15f, 6f).y)
                lineTo(pt(9f, 6f).x, pt(9f, 6f).y)
                close()
            }
            // الجسم: M8 7L16 7L18 17L6 17Z
            val body = Path().apply {
                moveTo(pt(8f, 7f).x, pt(8f, 7f).y)
                lineTo(pt(16f, 7f).x, pt(16f, 7f).y)
                lineTo(pt(18f, 17f).x, pt(18f, 17f).y)
                lineTo(pt(6f, 17f).x, pt(6f, 17f).y)
                close()
            }
            // القاعدة: M9 18L15 18L14 22L10 22Z
            val base = Path().apply {
                moveTo(pt(9f, 18f).x, pt(9f, 18f).y)
                lineTo(pt(15f, 18f).x, pt(15f, 18f).y)
                lineTo(pt(14f, 22f).x, pt(14f, 22f).y)
                lineTo(pt(10f, 22f).x, pt(10f, 22f).y)
                close()
            }
            drawPath(roof, color)
            drawPath(body, color)
            drawPath(base, color)
        }
    }
}
