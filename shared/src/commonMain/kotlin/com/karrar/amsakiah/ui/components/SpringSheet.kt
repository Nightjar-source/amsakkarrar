package com.karrar.amsakiah.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.karrar.amsakiah.ui.motion.motionEffects
import com.karrar.amsakiah.ui.motion.motionSpatial
import com.karrar.amsakiah.ui.platform.rememberReduceMotion
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararTokens
import kotlinx.coroutines.delay

/**
 * ورقة سفلية على Spring Motion — full-bleed على كل عائلة أندرويد.
 *
 * - الورقة نافذة `Dialog(decorFitsSystemWindows = false)` كاملة الشاشة:
 *   التعتيمة full-bleed خلف أشرطة النظام والمقطوعات — فلا فراغات على أي جهاز،
 *   وصمام خروج يدوي بالضغط على التعتيمة.
 * - الحركة spring: انزلاق من الأسفل بنابض مكاني + تلاشٍ بنابض التأثيرات،
 *   وتتكرر مع كل فتح/إغلاق.
 * - مقضة سحب (Drag Handle) حسب معايير M3 Expressive.
 */
@Composable
fun SpringSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissible: Boolean = true,
    dismissibleFromScrim: Boolean = true,
    imePadding: Boolean = false,
    scrimColor: Color = KararTokens.Glass.scrim,
    containerShape: Shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
    containerColor: Color = Color.Unspecified,
    minHeight: Dp = 0.dp,
    maxHeight: Dp = 640.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    /** إظهار مقضة السحب (توصية M3 Expressive للأوراق السفلية). */
    dragHandle: Boolean = true,
    content: @Composable () -> Unit
) {
    // «تقليل الحركة»: لا انزلاق، والورقة تظهر/تختفي فورًا.
    val reduceMotion = rememberReduceMotion()
    var composing by remember { mutableStateOf(visible) }
    LaunchedEffect(visible, reduceMotion) {
        if (visible) composing = true else {
            delay(if (reduceMotion) 0L else 480L)
            composing = false
        }
    }

    val scrimAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = motionEffects<Float>(KararTokens.Motion.EffectsStiffnessSlow),
        label = "spring-sheet-scrim"
    )
    val sheetAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = motionEffects(),
        label = "spring-sheet-alpha"
    )
    val sheetOffset by animateFloatAsState(
        targetValue = if (visible) 0f else 280f,
        animationSpec = motionSpatial(
            dampingRatio = 0.72f,
            stiffness = KararTokens.Motion.SpatialStiffnessSlow
        ),
        label = "spring-sheet-offset"
    )

    if (!composing) return

    val surface = if (containerColor == Color.Unspecified) KararColors.Card else containerColor

    Dialog(
        onDismissRequest = { if (dismissible) onDismissRequest() },
        properties = DialogProperties(
            decorFitsSystemWindows = false,
            usePlatformDefaultWidth = false,
            dismissOnBackPress = dismissible,
            dismissOnClickOutside = false
        )
    ) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            // التعتيمة full-bleed — تغطي خلف كل أشرطة النظام والمقطوعات.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = scrimAlpha }
                    .background(scrimColor)
                    .clickable(
                        enabled = dismissible && dismissibleFromScrim && visible,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest
                    )
            )

            val padded = if (imePadding) Modifier.imePadding() else Modifier
            Box(
                modifier = padded
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .heightIn(min = minHeight, max = maxHeight)
                    .graphicsLayer { alpha = sheetAlpha; translationY = sheetOffset }
                    .shadow(22.dp, containerShape)
                    .clip(containerShape)
                    .background(surface)
                    .padding(contentPadding)
            ) {
                if (dragHandle) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 32.dp, height = 4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(KararColors.TextMuted.copy(alpha = 0.35f))
                        )
                    }
                }
                content()
            }
        }
    }
}
