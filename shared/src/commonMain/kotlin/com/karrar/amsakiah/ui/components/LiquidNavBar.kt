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
import com.karrar.amsakiah.ui.motion.motionEffects
import com.karrar.amsakiah.ui.motion.motionSpatial

/**
 * شريط التنقل العائم — إعادة تصميم «كرار» (DF-11): **عائم شفاف** فوق أي خلفية.
 *
 * المشكلة السابقة: سطح صلب كحلي/أسود (~95%) لا يذوب على السماء الملوّنة
 * في «اليوم» — يقرأ ككتلة سوداء عائمة. التصميم الحالي:
 *
 * - السطح: زجاج شفاف (داكن 10% أبيض / فاتح 65% أبيض مصنفر) — السماء
 *   والتدرّج يمران من خلاله، والظل خفيف لا يثقّل الكبسولة.
 * - العنصر النشط: كبسولة `palette.gold` مع أيقونة ونص `palette.onGold` — تباين ≥ 4.5:1 في الوضعين.
 * - غير النشط: أيقونة ونص `palette.textPrimary` بشفافية تُقرأ فوق أي لون سماء.
 * - أهداف لمس ≥ 56×48dp، والأيقونات 22dp مع تسمية ≥10sp دائمًا ظاهرة.
 * - مؤشر واحد ينزلق بحتف نابض خلف العنصر النشط (بلا «تمويه مائي» وسوائل بيضاء).
 * - سحب بالإصبع لاختيار الوجهة في الاتجاهين + دعم RTL.
 */

/**
 * ألوان الكبسولة العائمة الشفافة (DF-11): السماء/التدرّج يمران من خلالها —
 * لا سطح كحلي/أسود معتّم يطفو فوق الخلفية. تُقرأ فوق أي تدرّج سماوي (ليل/نهار)
 * وفوق تدرّجات الوضع الفاتح الدافئة.
 *
 * @return السطح، الحد
 */
private fun floatingNavColors(isDark: Boolean): Pair<Color, Color> =
    if (isDark) {
        // داكن: 10% أبيض (نمط المرجع bg-white/10) + حد أبيض 12% — لا كتلة لونية.
        Color(0x1AFFFFFF) to Color(0x1FFFFFFF)
    } else {
        // فاتح: 65% أبيض مصنفر فوق الخلفية الدافئة + حد أسود 13% — تباين مقروء.
        Color(0xA6FFFFFF) to Color(0x21000000)
    }

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
    val (containerColor, containerBorder) = floatingNavColors(palette.isDark)

    Box(
        modifier = modifier
            .widthIn(max = KararTokens.Size.navBarMaxWidth)
            .fillMaxWidth()
            .height(62.dp)
            .shadow(14.dp, shape, ambientColor = Color(0x40000000), spotColor = Color(0x40000000))
            .clip(shape)
            .background(containerColor)
            .border(BorderStroke(1.dp, containerBorder), shape)
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
            // موضع الطفل رقم k: من اليسار = k·w (LTR)، ومن اليمين = W−(k+1)·w (RTL، لأن
            // الـRow يُحلّ من اليمين). مؤشر الـBox يبدأ عند TopStart (0 في LTR، عند W−w في
            // RTL)؛ لذلك الإزاحة المطلوبة = k·w في LTR و = −k·w في RTL.
            val direction = if (rtl) -1f else 1f
            val offsetX by animateFloatAsState(
                targetValue = direction * current.ordinal * slotWidth.value,
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
    // غير النشط: نص/أيقونة شبه كاملة التباين (لا رمادية باهتة) — أكبر وضوحًا فوق أي خلفية.
    val inactiveTint = palette.textPrimary.copy(alpha = if (palette.isDark) 0.78f else 0.72f)
    val iconTint by animateColorAsState(
        targetValue = if (selected) palette.onGold else inactiveTint,
        animationSpec = motionEffects(),
        label = "nav-icon-tint"
    )
    val labelTint by animateColorAsState(
        targetValue = if (selected) palette.onGold else inactiveTint,
        animationSpec = motionEffects(),
        label = "nav-label-tint"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = motionSpatial(dampingRatio = 0.62f),
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
 * الشريط الجانبي — اللغة نفسها رأسيًا (DF-11): سطح عائم شفاف يذوب على السماء
 * (نفس ألوان `floatingNavColors`) + كبسولة `gold` للنشط.
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
    val (containerColor, containerBorder) = floatingNavColors(palette.isDark)

    Box(
        modifier = modifier
            .height((entries.size * 64).dp + 16.dp)
            .widthIn(max = 88.dp)
            .fillMaxWidth()
            .shadow(14.dp, RoundedCornerShape(26.dp), ambientColor = Color(0x40000000), spotColor = Color(0x40000000))
            .clip(RoundedCornerShape(26.dp))
            .background(containerColor)
            .border(BorderStroke(1.dp, containerBorder), RoundedCornerShape(26.dp))
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
    val inactiveTint = palette.textPrimary.copy(alpha = if (palette.isDark) 0.78f else 0.72f)
    val iconTint by animateColorAsState(
        targetValue = if (selected) palette.onGold else inactiveTint,
        animationSpec = motionEffects(),
        label = "rail-icon-tint"
    )
    val labelTint by animateColorAsState(
        targetValue = if (selected) palette.onGold else inactiveTint,
        animationSpec = motionEffects(),
        label = "rail-label-tint"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = motionSpatial(dampingRatio = 0.62f),
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
