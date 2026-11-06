package com.karrar.amsakiah.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.state.AppState
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.theme.KararColors

@Composable
fun TasbihScreen(state: AppState, contentPadding: PaddingValues) {
    val count = state.settings.tasbihCount
    val target = state.settings.tasbihTarget
    val haptic = LocalHapticFeedback.current
    val progress by animateFloatAsState(
        targetValue = if (target > 0) (count % target).toFloat() / target else 0f,
        label = "tasbih"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 8.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("مسبحة الزهراء (ع)", color = KararColors.Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("اذكر الله بقلب حاضر", color = KararColors.TextMuted, fontSize = 12.sp)
        Spacer(Modifier.height(24.dp))

        Box(contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(280.dp)) {
                val stroke = 14.dp.toPx()
                val radius = size.minDimension / 2f - stroke
                drawCircle(Color(0x14FFFFFF), radius = radius, style = Stroke(stroke))
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(KararColors.Purple, KararColors.Gold, KararColors.Pink, KararColors.Purple)
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
            Box(
                Modifier
                    .size(220.dp)
                    .background(
                        Brush.radialGradient(listOf(Color(0x33FFD700), Color(0x11FFFFFF))),
                        CircleShape
                    )
                    .clickable(
                        interactionSource = androidx.compose.runtime.remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        state.update { s -> s.copy(tasbihCount = s.tasbihCount + 1) }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = count.toString(),
                        color = KararColors.TextPrimary,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("اضغط للتسبيح", color = KararColors.TextMuted, fontSize = 11.sp)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(33, 99, 100, 1000).forEach { value ->
                val selected = target == value
                Box(
                    Modifier
                        .background(if (selected) KararColors.Gold else Color(0x14FFFFFF), RoundedCornerShape(50))
                        .clickable { state.update { it.copy(tasbihTarget = value) } }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        value.toString(),
                        color = if (selected) Color(0xFF241A00) else KararColors.TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        GlassCard(modifier = Modifier.fillMaxWidth(), corner = 20.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("الدورات المكتملة", color = KararColors.TextSecondary, fontSize = 11.sp)
                    Text(
                        text = if (target > 0) "${count / target}" else "0",
                        color = KararColors.Gold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text("حتى الهدف", color = KararColors.TextSecondary, fontSize = 11.sp)
                    Text(
                        text = if (target > 0) "${target - (count % target)}" else "-",
                        color = KararColors.TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    Modifier
                        .background(Color(0x22EF5350), RoundedCornerShape(50))
                        .clickable { state.update { it.copy(tasbihCount = 0) } }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("تصفير", color = KararColors.Danger, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
