package com.karrar.amsakiah.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.ui.player.DuaPlayerController
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararTokens

/**
 * مشغّل الأدعية — نسخة واحدة بسيطة وأنيقة:
 *
 * - **شريط واحد** عائم فوق شريط التنقل (`rounded-full`): غلاف ألبوم
 *   متدرج، اسم الدعاء، زر تشغيل/إيقاف، زر إغلاق، ومؤشر تقدّم رفيع
 *   أسفل المحتوى داخل الشريط نفسه.
 * - بالضغط على وسط الشريط ينزلق فوقه **لوح توسعة** فيه شريط تمرير حقيقي
 *   (`Slider`) مع الوقت الحال/الكلي.
 * - **لا كرة عائمة** ولا سحب حر — مصدر إحباط وحالة فوضية بلا فائدة.
 */
@Composable
fun DuaPlayerOverlay(
    controller: DuaPlayerController,
    bottomReserve: Dp,
    horizontalPadding: Dp,
    modifier: Modifier = Modifier
) {
    if (!controller.hasTrack) return

    var expanded by rememberSaveable { mutableStateOf(false) }

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

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    bottom = bottomReserve + 10.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(KararTokens.Motion.effects()) +
                    slideInVertically(KararTokens.Motion.spatial()) { it / 2 },
                exit = fadeOut(KararTokens.Motion.effects()) +
                    slideOutVertically(KararTokens.Motion.spatial()) { it / 2 }
            ) {
                ExpandedDuaPlayer(
                    controller = controller,
                    progress = progress,
                    onClose = { expanded = false }
                )
            }
            if (expanded) Spacer(Modifier.height(8.dp))

            DockedDuaBar(
                controller = controller,
                progress = progress,
                onExpand = { expanded = !expanded },
                onClose = { expanded = false; controller.close() }
            )
        }
    }
}

@Composable
private fun DockedDuaBar(
    controller: DuaPlayerController,
    progress: Float,
    onExpand: () -> Unit,
    onClose: () -> Unit
) {
    val pill = Color(0xF0121A2C)
    val onPill = Color(0xFFF2F5FA)
    val accent = KararColors.Gold

    Surface(
        modifier = Modifier
            .widthIn(max = 560.dp)
            .fillMaxWidth()
            .height(64.dp)
            .shadow(16.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        color = pill,
        border = BorderStroke(1.dp, KararColors.Teal.copy(alpha = 0.30f)),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // غلاف ألبوم: تدرج دافئ (ذهبي → وردي)، يفتح التوسعة، ويشغّل/يوقف بالضغط المطوّل عليه.
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(accent, KararColors.Pink)))
                    .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape)
                    .clickable(onClick = { controller.toggle() }),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (controller.playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (controller.playing) "إيقاف مؤقت" else "تشغيل",
                    tint = KararColors.OnGold,
                    modifier = Modifier.size(25.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onExpand)
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    text = controller.title.orEmpty(),
                    color = onPill,
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(3.dp)
                                .background(accent, CircleShape)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = formatPlayerTime(controller.positionMs),
                        color = onPill.copy(alpha = 0.55f),
                        fontSize = 9.sp
                    )
                }
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "إغلاق المشغل",
                    tint = onPill.copy(alpha = 0.7f),
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
private fun ExpandedDuaPlayer(
    controller: DuaPlayerController,
    progress: Float,
    onClose: () -> Unit
) {
    var dragValue by remember { mutableStateOf<Float?>(null) }
    val sliderValue = dragValue ?: progress

    Surface(
        modifier = Modifier
            .widthIn(max = 520.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xF0121A2C),
        border = BorderStroke(1.dp, KararColors.Teal.copy(alpha = 0.30f)),
        shadowElevation = 18.dp
    ) {
        Column(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = controller.title.orEmpty(),
                        color = Color(0xFFF2F5FA),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "مشغّل التلاوة — ${formatPlayerTime(controller.positionMs)} · ${formatPlayerTime(controller.durationMs)}",
                        color = Color(0xFFF2F5FA).copy(alpha = 0.55f),
                        fontSize = 10.sp
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "طي اللوح",
                        tint = Color(0xFFF2F5FA).copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
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
                    inactiveTrackColor = Color.White.copy(alpha = 0.18f)
                ),
                modifier = Modifier.height(28.dp)
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
