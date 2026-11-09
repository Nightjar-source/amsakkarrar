package com.karrar.amsakiah.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.ui.player.DuaPlayerController
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararTokens
import kotlin.math.roundToInt

/**
 * مشغّل الأدعية الموحّد — **مشغل واحد** يتنقّل بين حالتين:
 *
 * 1. `Docked`: شريط مدمج ارتفاعه [KararTokens.Size.playerDockHeight] (28dp)
 *    فوق شريط التنقل، سطر واحد: زر تشغيل + عنوان + مؤشر تقدم رفيع **تحت النص**.
 * 2. `Floating`: كرة عائمة تُسحب إلى أي مكان على الشاشة (مع حدود آمنة)،
 *    وتختفي من مكانها المدمج — لا يظهر مشغلان في آنٍ واحد.
 *
 * الضغط على الشريط يفتح لوحة موسّعة فيها شريط تمرير حقيقي.
 */
@Composable
fun DuaPlayerOverlay(
    controller: DuaPlayerController,
    bottomReserve: Dp,
    horizontalPadding: Dp,
    modifier: Modifier = Modifier
) {
    if (!controller.hasTrack) return

    var floating by rememberSaveable { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val target = if (controller.durationMs > 0L) {
        controller.positionMs.toFloat() / controller.durationMs.toFloat()
    } else {
        0f
    }
    val progress by animateFloatAsState(
        targetValue = target.coerceIn(0f, 1f),
        animationSpec = KararTokens.Motion.effects(),
        label = "dua-progress"
    )

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val pillPx = with(density) { 46.dp.toPx() }
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { maxHeight.toPx() }
        val bottomPx = with(density) { (bottomReserve + 12.dp).toPx() }
        val sidePx = with(density) { horizontalPadding.toPx() }

        // إزاحة الكرة من مرساة الزاوية السفلية (سالب = أعلى/يسار).
        var offsetX by rememberSaveable { mutableFloatStateOf(0f) }
        var offsetY by rememberSaveable { mutableFloatStateOf(0f) }
        var dragging by remember { mutableStateOf(false) }

        if (!floating) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        start = horizontalPadding,
                        end = horizontalPadding,
                        bottom = bottomReserve + 8.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn(KararTokens.Motion.effects()) +
                        slideInVertically(KararTokens.Motion.spatial()) { it / 2 } +
                        scaleIn(KararTokens.Motion.spatial(), initialScale = 0.94f),
                    exit = fadeOut(KararTokens.Motion.effects()) +
                        slideOutVertically(KararTokens.Motion.spatial()) { it / 3 } +
                        scaleOut(KararTokens.Motion.effects(), targetScale = 0.94f)
                ) {
                    ExpandedDuaPlayer(
                        controller = controller,
                        progress = progress,
                        onDock = { floating = false; expanded = false },
                        onClose = { expanded = false; controller.close() }
                    )
                }
                if (expanded) Spacer(Modifier.height(8.dp))

                DockedDuaBar(
                    controller = controller,
                    progress = progress,
                    onExpand = { expanded = !expanded },
                    onDetach = { expanded = false; floating = true }
                )
            }
        } else {
            // الكرة العائمة — تُسحب بحرية داخل حدود آمنة (لا تخرج ولا تغطي الشريط).
            val minX = -(maxWidthPx - pillPx - sidePx * 2)
            val minY = -(maxHeightPx - bottomPx - pillPx - sidePx)

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = horizontalPadding, bottom = bottomReserve + 12.dp)
                    .padding(end = 4.dp)
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .size(46.dp)
                    .shadow(
                        elevation = if (dragging) 24.dp else 14.dp,
                        shape = CircleShape,
                        ambientColor = Color.Black.copy(alpha = 0.5f),
                        spotColor = Color.Black.copy(alpha = 0.5f)
                    )
                    .background(
                        Brush.linearGradient(listOf(KararColors.Teal, KararColors.Violet)),
                        CircleShape
                    )
                    .border(1.dp, KararColors.Gold.copy(alpha = 0.45f), CircleShape)
                    .pointerInput(minX, minY) {
                        detectDragGestures(
                            onDragStart = { dragging = true },
                            onDragEnd = { dragging = false },
                            onDragCancel = { dragging = false }
                        ) { change, dragAmount ->
                            change.consume()
                            offsetX = (offsetX + dragAmount.x).coerceIn(minX, 0f)
                            offsetY = (offsetY + dragAmount.y).coerceIn(minY, 0f)
                        }
                    }
                    .clickable { controller.toggle() },
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize().padding(3.dp)) {
                    val stroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    drawCircle(color = Color.White.copy(alpha = 0.22f), style = stroke)
                    drawArc(
                        color = Color.White,
                        startAngle = -90f,
                        sweepAngle = 360f * progress.coerceIn(0f, 1f),
                        useCenter = false,
                        style = stroke
                    )
                }
                Icon(
                    imageVector = if (controller.playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (controller.playing) "إيقاف مؤقت" else "تشغيل",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // زر تثبيت صغير أسفل الكرة — يعيدها إلى الشريط المدمج (مشغل واحد).
            if (expanded) {
                ExpandedDuaPlayer(
                    controller = controller,
                    progress = progress,
                    onDock = { floating = false; expanded = false },
                    onClose = { expanded = false; controller.close() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = horizontalPadding, end = horizontalPadding, bottom = bottomReserve + 66.dp)
                )
            }
        }
    }
}

@Composable
private fun DockedDuaBar(
    controller: DuaPlayerController,
    progress: Float,
    onExpand: () -> Unit,
    onDetach: () -> Unit
) {
    // تُقرأ الألوان قبل Canvas: لا يجوز استدعاء getters@Composable داخل draw.
    val trackColor = KararColors.TextMuted.copy(alpha = 0.35f)
    val progressColor = KararColors.Gold
    Surface(
        modifier = Modifier
            .widthIn(max = 560.dp)
            .fillMaxWidth()
            .height(KararTokens.Size.playerDockHeight),
        shape = RoundedCornerShape(14.dp),
        color = KararColors.Card,
        border = BorderStroke(1.dp, KararColors.Gold.copy(alpha = 0.22f)),
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 4.dp, end = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(KararColors.Gold, KararColors.Pink)))
                    .clickable { controller.toggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (controller.playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (controller.playing) "إيقاف مؤقت" else "تشغيل",
                    tint = KararColors.OnGold,
                    modifier = Modifier.size(13.dp)
                )
            }
            Spacer(Modifier.width(6.dp))
            // العنوان + مؤشر التقدم أسفله مباشرة (تأشير على النص).
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onExpand)
            ) {
                Text(
                    text = controller.title.orEmpty(),
                    color = KararColors.TextPrimary,
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .padding(end = 2.dp)
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawLine(
                            color = trackColor,
                            start = androidx.compose.ui.geometry.Offset(0f, size.height / 2f),
                            end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2f),
                            strokeWidth = size.height
                        )
                        drawLine(
                            color = progressColor,
                            start = androidx.compose.ui.geometry.Offset(0f, size.height / 2f),
                            end = androidx.compose.ui.geometry.Offset(size.width * progress, size.height / 2f),
                            strokeWidth = size.height,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
            // أزرار صغيرة (22dp) بـBox لا IconButton: الأخير يفرض حدًا أدنى
            // للمس 48dp فيرفع ارتفاع الشريط فوق 28dp المطلوب.
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onDetach),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.PictureInPictureAlt,
                    contentDescription = "فصل المشغل وتحريكه",
                    tint = KararColors.TextSecondary,
                    modifier = Modifier.size(13.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .clickable { controller.close() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "إغلاق المشغل",
                    tint = KararColors.TextSecondary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun ExpandedDuaPlayer(
    controller: DuaPlayerController,
    progress: Float,
    onDock: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dragValue by remember { mutableStateOf<Float?>(null) }
    val sliderValue = dragValue ?: progress

    Surface(
        modifier = modifier
            .widthIn(max = 520.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = KararColors.Card,
        border = BorderStroke(1.dp, KararColors.Gold.copy(alpha = 0.28f)),
        shadowElevation = 20.dp
    ) {
        Column(
            Modifier.padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.DragIndicator,
                    contentDescription = null,
                    tint = KararColors.TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = controller.title.orEmpty(),
                        color = KararColors.TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${formatPlayerTime(controller.positionMs)} / ${formatPlayerTime(controller.durationMs)}",
                        color = KararColors.TextMuted,
                        fontSize = 9.sp
                    )
                }
                IconButton(onClick = onDock, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Filled.PushPin,
                        contentDescription = "تثبيت في الشريط",
                        tint = KararColors.TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "إغلاق",
                        tint = KararColors.TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Slider(
                value = sliderValue,
                onValueChange = { value ->
                    dragValue = value
                    controller.seekToFraction(value)
                },
                onValueChangeFinished = { dragValue = null },
                colors = SliderDefaults.colors(
                    thumbColor = KararColors.Gold,
                    activeTrackColor = KararColors.Gold,
                    inactiveTrackColor = KararColors.TextMuted.copy(alpha = 0.25f)
                ),
                modifier = Modifier.height(24.dp)
            )
        }
    }
}

internal fun formatPlayerTime(ms: Long): String {
    if (ms <= 0L) return "00:00"
    val total = ms / 1000
    val hours = total / 3600
    val minutes = (total % 3600) / 60
    val seconds = total % 60
    return if (hours > 0) {
        "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }
}
