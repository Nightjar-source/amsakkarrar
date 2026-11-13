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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.ui.navigation.AppDestination
import com.karrar.amsakiah.ui.theme.LocalKararPalette
import com.karrar.amsakiah.ui.theme.KararTokens
import com.karrar.amsakiah.ui.motion.motionSpatial

/**
 * شريط التنقل العائم — إعادة تصميم «كرار»: أسطح صلبة قرائية من اللوحة.
 *
 * المشكلة السابقة: تدرّجات بيضاء نصف شفافة + نصوص رمادية باهتة فوق أي خلفية —
 * بلا أي تباين يضمن القراءة. التصميم الحالي مبني على قواعد M3 Expressive:
 *
 * - السطح: `palette.glassDeep` — معتم عمليًا (~91–95%) فوق أي خلفية سماوية/داكنة.
 * - العنصر النشط: كبسولة `palette.gold` مع أيقونة ونص `palette.onGold` — تباين ≥ 4.5:1 في الوضعين.
 * - غير النشط: أيقونة ونص `palette.textMuted` على السطح المعتم — تباين ≥ 4.5:1.
 * - أهداف لمس ≥ 56×48dp، والأيقونات 22dp مع تسمية ≥10sp دائمًا ظاهرة.
 * - مؤشر واحد ينزلق بحتف نابض خلف العنصر النشط (بلا «تمويه مائي» وسوائل بيضاء).
 * - سحب بالإصبع لاختيار الوجهة في الاتجاهين + دعم RTL.
 */

@Composable
fun LiquidFloatingBottomBar(
    current: AppDestination,
    onSelect: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalKararPalette.current
    val entries = AppDestination.entries
    val density = androidx.compose.ui.platform.LocalDensity.current.density
    // اتجاه الواجهة: في RTL يكون أول عنصر من اليمين.
    val rtl = androidx.compose.ui.platform.LocalLayoutDirection.current ==
        androidx.compose.ui.unit.LayoutDirection.Rtl
    val shape = RoundedCornerShape(30.dp)

    Box(
        modifier = modifier
            .widthIn(max = KararTokens.Size.navBarMaxWidth)
            .fillMaxWidth()
            .height(62.dp)
            .shadow(20.dp, shape, ambientColor = Color(0x59000000), spotColor = Color(0x59000000))
            .clip(shape)
            .background(palette.glassDeep)
            .border(BorderStroke(1.dp, palette.borderStrong), shape)
            .padding(6.dp)
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
            val indexFromLeft =
                if (rtl) entries.size - 1 - current.ordinal else current.ordinal
            val offsetX by animateFloatAsState(
                targetValue = (slotWidth * indexFromLeft).value,
                animationSpec = motionSpatial(),
                label = "bar-offset"
            )
            // المؤشر النشط — كبسولة ذهبية صلبة (تباين مضمون فوق أي خلفية).
            Box(
                modifier = Modifier
                    .graphicsLayer { translationX = offsetX * density }
                    .fillMaxHeight()
                    .width(slotWidth)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(palette.gold)
            )
            Row(Modifier.fillMaxSize()) {
                entries.forEach { destination ->
                    NavItem(
                        destination = destination,
                        selected = destination == current,
                        onClick = { onSelect(destination) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    destination: AppDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalKararPalette.current
    val iconTint by animateColorAsState(
        targetValue = if (selected) palette.onGold else palette.textMuted,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 380f),
        label = "nav-icon-tint"
    )
    val labelTint by animateColorAsState(
        targetValue = if (selected) palette.onGold else palette.textMuted,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 380f),
        label = "nav-label-tint"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 380f),
        label = "nav-icon-scale"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
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
                    .size(21.dp)
                    .graphicsLayer { scaleX = iconScale; scaleY = iconScale }
            )
            Text(
                text = destination.shortTitle,
                color = labelTint,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
    }
}

/**
 * الشريط الجانبي — اللغة نفسها رأسيًا: سطح `glassDeep` صلب + كبسولة `gold` للنشط.
 * الارتفاع يُحسب من عدد العناصر الفعلي وبأهداف لمس ≥ 56dp لكل وجهة.
 */
@Composable
fun LiquidVerticalRail(
    current: AppDestination,
    onSelect: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalKararPalette.current
    val entries = AppDestination.entries
    val density = androidx.compose.ui.platform.LocalDensity.current.density

    Box(
        modifier = modifier
            .height((entries.size * 64).dp + 16.dp)
            .widthIn(max = 88.dp)
            .fillMaxWidth()
            .shadow(20.dp, RoundedCornerShape(26.dp), ambientColor = Color(0x59000000), spotColor = Color(0x59000000))
            .clip(RoundedCornerShape(26.dp))
            .background(palette.glassDeep)
            .border(BorderStroke(1.dp, palette.borderStrong), RoundedCornerShape(26.dp))
            .padding(8.dp)
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
                animationSpec = motionSpatial(),
                label = "rail-offset"
            )
            Box(
                modifier = Modifier
                    .graphicsLayer { translationY = offsetY * density }
                    .fillMaxWidth()
                    .height(slotHeight)
                    .padding(horizontal = 2.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(palette.gold)
            )
            Column(Modifier.fillMaxSize()) {
                entries.forEach { destination ->
                    RailItem(
                        destination = destination,
                        selected = destination == current,
                        onClick = { onSelect(destination) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(slotHeight)
                    )
                }
            }
        }
    }
}

@Composable
private fun RailItem(
    destination: AppDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalKararPalette.current
    val iconTint by animateColorAsState(
        targetValue = if (selected) palette.onGold else palette.textMuted,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 380f),
        label = "rail-icon-tint"
    )
    val labelTint by animateColorAsState(
        targetValue = if (selected) palette.onGold else palette.textMuted,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = 380f),
        label = "rail-label-tint"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = 380f),
        label = "rail-icon-scale"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
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
                    .size(21.dp)
                    .graphicsLayer { scaleX = iconScale; scaleY = iconScale }
            )
            Text(
                text = destination.shortTitle,
                color = labelTint,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
