package com.karrar.amsakiah.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.ui.player.DuaPlayerController
import com.karrar.amsakiah.ui.theme.KararColors
import kotlinx.coroutines.delay

/**
 * مشغّل الأدعية العائم — نافذة بيضوية صغيرة فوق المحتوى.
 *
 * - مطويًا: كرة تشغيل/إيقاف مع حلقة تقدّم حقيقية.
 * - موسّعًا: نافذة بيضوية فيها العنوان وشريط التقدم وزر التصغير/الإغلاق.
 *
 * شريط التقدم يقرأ `positionMs` من المشغّل الأصلي (MediaPlayer/AVAudioPlayer)
 * عبر [DuaPlayerController.sync] كل 200ms، فهو مرتبط فعليًا بالمشغّل.
 */
@Composable
fun DuaFloatingPlayer(
    controller: DuaPlayerController,
    modifier: Modifier = Modifier
) {
    if (!controller.hasTrack) return

    // مزامنة دورية مع المشغّل الأصلي — مصدر الحقيقة لشريط التقدم
    LaunchedEffect(controller.currentId, controller.playing) {
        while (controller.playing) {
            controller.sync()
            delay(200)
        }
    }

    val target = if (controller.durationMs > 0L) {
        controller.positionMs.toFloat() / controller.durationMs.toFloat()
    } else {
        0f
    }
    val progress by animateFloatAsState(
        targetValue = target.coerceIn(0f, 1f),
        animationSpec = tween(220),
        label = "dua-progress"
    )

    AnimatedContent(
        targetState = controller.expanded,
        transitionSpec = {
            (fadeIn(tween(200)) + scaleIn(tween(240), initialScale = 0.92f)) togetherWith
                (fadeOut(tween(140)) + scaleOut(tween(140), targetScale = 0.92f))
        },
        label = "dua-player-window"
    ) { expanded ->
        if (expanded) {
            ExpandedDuaPlayer(controller, progress, modifier)
        } else {
            CollapsedDuaPlayer(controller, progress, modifier)
        }
    }
}

@Composable
private fun ExpandedDuaPlayer(
    controller: DuaPlayerController,
    progress: Float,
    modifier: Modifier
) {
    var dragValue by remember { mutableStateOf<Float?>(null) }
    val sliderValue = dragValue ?: progress

    Surface(
        modifier = modifier
            .widthIn(max = 520.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color(0xF2141A29),
        border = BorderStroke(1.dp, Color(0x33FFD700)),
        shadowElevation = 20.dp
    ) {
        Column(
            Modifier.padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlayerToggle(controller)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = controller.title.orEmpty(),
                        color = KararColors.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (controller.playing) {
                            "يعمل الآن من المشغّل الأصلي"
                        } else {
                            "متوقف مؤقتًا — اضغط للمتابعة"
                        },
                        color = if (controller.playing) KararColors.Gold else KararColors.TextMuted,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
                IconButton(
                    onClick = { controller.showExpanded(false) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.ExpandMore,
                        contentDescription = "تصغير المشغل",
                        tint = KararColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { controller.close() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "إغلاق المشغل",
                        tint = KararColors.TextSecondary,
                        modifier = Modifier.size(16.dp)
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
                    inactiveTrackColor = Color(0x33FFFFFF)
                ),
                modifier = Modifier.height(26.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatPlayerTime(controller.positionMs),
                    color = KararColors.TextMuted,
                    fontSize = 9.sp
                )
                Text(
                    text = formatPlayerTime(controller.durationMs),
                    color = KararColors.TextMuted,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
private fun CollapsedDuaPlayer(
    controller: DuaPlayerController,
    progress: Float,
    modifier: Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(Color(0xF2141A29))
                .border(1.dp, Color(0x33FFD700), CircleShape)
                .clickable { controller.toggle() },
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize().padding(3.dp)) {
                val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                drawCircle(color = Color(0x22FFFFFF), style = stroke)
                drawArc(
                    color = KararColors.Gold,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    style = stroke
                )
            }
            Icon(
                imageVector = if (controller.playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (controller.playing) "إيقاف مؤقت" else "تشغيل",
                tint = KararColors.Gold,
                modifier = Modifier.size(24.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xF2141A29))
                .border(1.dp, Color(0x33FFD700), CircleShape)
                .clickable { controller.showExpanded(true) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.ExpandLess,
                contentDescription = "تكبير المشغل",
                tint = KararColors.TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun PlayerToggle(controller: DuaPlayerController) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(KararColors.Gold, KararColors.Pink)))
            .clickable { controller.toggle() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (controller.playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = if (controller.playing) "إيقاف مؤقت" else "تشغيل",
            tint = Color(0xFF241A00),
            modifier = Modifier.size(22.dp)
        )
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
