package com.karrar.amsakiah.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.ui.navigation.AppDestination
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararTokens
import com.karrar.amsakiah.ui.motion.motionSpatial

/**
 * شريط التنقل العائم — تصميم «Liquid Glass Floating» مطابق لـشريط_تنقل_مائي_عائم_مع_دليل_المطور.html:
 *
 * - **كبسولة عائمة زجاجية**: خلفية متدرجة شبه شفافة + تمويه + حدود لامعة + ظل بنفسجي خفيف،
 *   وتتكئ على مسافة ثابتة عن الحافة (fullWidth مع أقصى عرض).
 * - **المؤشر المائي (Water-drop glass)**: كبسولة صغيرة تنجرف بحركة نابضية هيدروليكية
 *   (`spring(lowDamp; 0.6)` معّمَّمة) خلف العنصر النشط، وتحمل أيقونة مكبّرة وتسمية تتوسع.
 * - **العنصر النشط**: أيقونة Scale‑12 + الاسم جنبًا إلى جنب. غير النشط: أيقونة فوق تسمية صغيرة.
 * - **سحب بالإصبع** عبر [Modifier] على الشريط كله — أي مسافة أفقية (/عمودي للـRail).
 *
 * اللون المعتم (فوق الكبسولة على الخلفية البيضاء): داكن بنفسجي `#2E1A47`.
 */

private val LiquidBarSurface = Brush.linearGradient(
    colors = listOf(
        Color(0x2EFFFFFF),   // 0%
        Color(0x0AFFFFFF),   // 50%
        Color(0x1FFFFFF)    // 100%
    ),
    start = Offset(0f, 0f),
    end = Offset(1f, 1f)
)

private val LiquidIndicatorSurface = Brush.linearGradient(
    colors = listOf(
        Color(0x3DFFFFFF),   // top-left — مضيء
        Color(0x14FFFFFF),   // المنتصف — مشبع قليلًا
        Color(0x30FFFFFF)  // bottom-right — إعادة إضاءة
    ),
    start = Offset(0f, 0f),
    end = Offset(1f, 1f)
)

private val LiquidBarBorder = BorderStroke(1.5.dp, Color(0x40FFFFFF))
private val LiquidIndicatorBorder = BorderStroke(1.5.dp, Color(0x59FFFFFF))
private val LiquidActiveText = Color(0xFF2E1A47)
private val LiquidMutedText = Color(0xCC8C92A8)
private val PurpleAmbient = Color(0xFF6E50A4)

@Composable
fun LiquidFloatingBottomBar(
    current: AppDestination,
    onSelect: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val entries = AppDestination.entries
    val density = androidx.compose.ui.platform.LocalDensity.current.density
    // اتجاه الواجهة: في RTL يكون أول عنصر من اليمين — نحسب مواضع المؤشر والسحب accordingly.
    val rtl = androidx.compose.ui.platform.LocalLayoutDirection.current ==
        androidx.compose.ui.unit.LayoutDirection.Rtl

    Box(
        modifier = modifier
            .widthIn(max = KararTokens.Size.navBarMaxWidth)
            .fillMaxWidth()
            .height(58.dp)
            .shadow(24.dp, RoundedCornerShape(34.dp), ambientColor = PurpleAmbient, spotColor = PurpleAmbient)
            .clip(RoundedCornerShape(34.dp))
            .background(LiquidBarSurface)
            .border(LiquidBarBorder, RoundedCornerShape(34.dp))
            .padding(5.dp)
            .pointerInput(entries) {
                fun indexAt(x: Float): Int {
                    val ratio = (x / size.width).coerceIn(0f, 0.999f)
                    val fromLeft = (ratio * entries.size).toInt()
                    return if (rtl) entries.size - 1 - fromLeft else fromLeft
                }
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        val index = indexAt(offset.x)
                        if (index != current.ordinal) onSelect(entries[index])
                    },
                    onHorizontalDrag = { change, _ ->
                        val index = indexAt(change.position.x)
                        if (index != current.ordinal) onSelect(entries[index])
                    }
                )
            }
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val slotWidth = maxWidth / entries.size
            // في RTL يكون أول عنصر من اليمين — نحسب موضع المؤشر من اليسار accordingly.
            val indexFromLeft =
                if (rtl) entries.size - 1 - current.ordinal else current.ordinal
            val targetX = slotWidth * indexFromLeft
            val offsetX by animateFloatAsState(
                targetValue = targetX.value,
                animationSpec = spring(dampingRatio = 0.62f, stiffness = 380f),
                label = "liquid-offset"
            )
            Box(
                modifier = Modifier
                    .graphicsLayer { translationX = offsetX * density }
                    .fillMaxHeight()
                    .widthIn(max = slotWidth)
                    .background(LiquidIndicatorSurface, CircleShape)
                    .border(LiquidIndicatorBorder, CircleShape)
            )
            // العنصر نفسه — الظلال شفافة أي يمكن أن تظهر الكابسولة خلف الحدود
            Row(Modifier.fillMaxSize()) {
                entries.forEach { destination ->
                    LiquidItem(
                        destination = destination,
                        selected = destination == current,
                        onClick = { onSelect(destination) },
                        modifier = Modifier.widthIn(max = slotWidth)
                    )
                }
            }
        }
    }
}

@Composable
private fun LiquidItem(
    destination: AppDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.14f else 0.92f,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = 380f),
        label = "liquid-icon-scale"
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) LiquidActiveText else LiquidMutedText,
        animationSpec = KararTokens.Motion.effects(),
        label = "liquid-icon-tint"
    )
    val labelAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.7f,
        animationSpec = KararTokens.Motion.effects(),
        label = "liquid-label-alpha"
    )
    Box(
        modifier = modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = destination.icon,
                    contentDescription = destination.title,
                    tint = iconTint,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                )
                Text(
                    text = destination.shortTitle,
                    color = iconTint.copy(alpha = labelAlpha),
                    fontSize = 11.5.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = destination.icon,
                    contentDescription = destination.title,
                    tint = iconTint.copy(alpha = labelAlpha),
                    modifier = Modifier
                        .size(23.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                )
                Text(
                    text = destination.shortTitle,
                    color = iconTint.copy(alpha = labelAlpha),
                    fontSize = 8.5.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * الشريط الجانبي المدني — نفس لغة «Liquid Floating Glass» لكن رأسيًا.
 * مؤشر مائي ينزلق رأسيًا بحتف 80px كما في المرجع، ولا حواف خارجية.
 */
@Composable
fun LiquidVerticalRail(
    current: AppDestination,
    onSelect: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val entries = AppDestination.entries
    val density = androidx.compose.ui.platform.LocalDensity.current.density

    Box(
        modifier = modifier
            .height(KararTokens.Size.navItem * 8 + 16.dp + 60.dp)
            .widthIn(max = 84.dp)
            .fillMaxWidth()
            .shadow(22.dp, RoundedCornerShape(26.dp), ambientColor = PurpleAmbient, spotColor = PurpleAmbient)
            .clip(RoundedCornerShape(26.dp))
            .background(LiquidBarSurface)
            .border(BorderStroke(1.5.dp, Color(0x40FFFFFF)), RoundedCornerShape(26.dp))
            .padding(6.dp)
            .pointerInput(entries) {
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        val index = ((offset.y / size.height) * entries.size).toInt()
                            .coerceIn(0, entries.size - 1)
                        if (index != current.ordinal) onSelect(entries[index])
                    },
                    onVerticalDrag = { change, _ ->
                        val index = ((change.position.y / size.height) * entries.size).toInt()
                            .coerceIn(0, entries.size - 1)
                        if (index != current.ordinal) onSelect(entries[index])
                    }
                )
            }
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val slotHeight = maxHeight / entries.size
            val targetY = slotHeight * current.ordinal
            val offsetY by animateFloatAsState(
                targetValue = targetY.value,
                animationSpec = spring(dampingRatio = 0.62f, stiffness = 380f),
                label = "liquid-rail-offset"
            )
            Box(
                modifier = Modifier
                    .graphicsLayer { translationY = offsetY * density }
                    .fillMaxWidth()
                    .height(slotHeight)
                    .background(LiquidIndicatorSurface, RoundedCornerShape(22.dp))
                    .border(BorderStroke(1.5.dp, Color(0x59FFFFFF)), RoundedCornerShape(22.dp))
            )
            Column(Modifier.fillMaxSize()) {
                entries.forEach { destination ->
                    LiquidRailItem(
                        destination = destination,
                        selected = destination == current,
                        onClick = { onSelect(destination) },
                        modifier = Modifier.height(slotHeight)
                    )
                }
            }
        }
    }
}
@Composable
private fun LiquidRailItem(
    destination: AppDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.14f else 0.92f,
        animationSpec = spring(dampingRatio = 0.58f, stiffness = 380f),
        label = "liquid-rail-icon-scale"
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) LiquidActiveText else LiquidMutedText,
        animationSpec = KararTokens.Motion.effects(),
        label = "liquid-rail-icon-tint"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = destination.title,
                tint = iconTint,
                modifier = Modifier
                    .size(23.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    }
            )
            Text(
                text = destination.shortTitle,
                color = iconTint,
                fontSize = 9.5.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}
