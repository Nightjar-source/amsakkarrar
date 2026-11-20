package com.karrar.amsakiah.ui.motion

import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.karrar.amsakiah.ui.platform.rememberReduceMotion

/**
 * **Motion Design للأزرار** — ضغطة ناعمة بلا تقطيع:
 *
 * - انكماش خفيف (0.94) بنابض «سائل» ثم عودة فورية — بلا ارتداد ولا قفزات.
 * - إضاءة تلقائية: عند اللمس تُظلَّل الكبسولة قليلًا (0.85) وحين تحوم الفأرة
 *   تُضاء قليلًا (1.05) — كل شيء بنفس النابض، بلا تبديل لون مفاجئ.
 * - يُقرأ من `InteractionSource` نفسه الذي يملكه `clickable` — صفر تعارض.
 */
@Composable
fun Modifier.pressScale(
    source: InteractionSource,
    scaleTo: Float = 0.94f
): Modifier {
    if (rememberReduceMotion()) return this
    val pressed by source.collectIsPressedAsState()
    val hovered by source.collectIsHoveredAsState()
    val animatedScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed) scaleTo else 1f,
        animationSpec = motionSpatialFluid(stiffness = 700f, dampingRatio = 0.72f),
        label = "press-scale"
    )
    val animatedAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = when {
            pressed -> 0.86f
            hovered -> 1f
            else -> 0.97f
        },
        animationSpec = motionEffects(),
        label = "press-light"
    )
    return this.graphicsLayer {
        scaleX = animatedScale
        scaleY = animatedScale
        alpha = animatedAlpha
    }
}
