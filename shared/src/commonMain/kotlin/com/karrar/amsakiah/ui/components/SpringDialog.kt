package com.karrar.amsakiah.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.karrar.amsakiah.ui.motion.motionEffects
import com.karrar.amsakiah.ui.motion.motionSpatial
import com.karrar.amsakiah.ui.platform.rememberReduceMotion
import com.karrar.amsakiah.ui.theme.KararTokens
import kotlinx.coroutines.delay

/**
 * نافذة تأكيد/حوار على Spring Motion — full-bleed على كل عائلة أندرويد.
 *
 * لماذا ليست الحوارات الافتراضية `Dialog { … }`؟
 * - الوضع الافتراضي للنافذة (`usePlatformDefaultWidth = true`) يجعل نافذة
 *   الحوار بحجم البطاقة فقط، فيبقى خطّان داكنان/فارغان أعلى وأسفل منها على
 *   أجهزة Samsung One UI ومثيلاتها — وهو ما رصده المستخدم.
 * - هنا: نافذة `Dialog(decorFitsSystemWindows = false, usePlatformDefaultWidth = false)`
 *   تغطي الشاشة كاملة (خلف أشرطة النظام والمقطوعات)، ونرسم التعتيمة
 *   (scrim) بأنفسنا = full-bleed حقيقية على أي جهاز.
 *
 * الحركة كلها springs وتُعاد مع كل فتح/إغلاق:
 * - الدخول: تلاشٍ بنابض التأثيرات + Scale/Offset مكاني للبطاقة.
 * - الخروج: عكس ذلك — وشرط `composing` يبقي النافذة حتى تنتهي حركة الخروج
 *   كاملة (لا تقاطع أي شيء ويُعاد تشغيلها مع كل فتح).
 *
 * ملاحظة: النقر خارج البطاقة يُدار يدويًا عبر التعتيمة (وليس عبر
 * `dismissOnClickOutside`) حتى لا تتداخلان الدعوَتان.
 */
@Composable
fun SpringDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissible: Boolean = true,
    /** للألواح التي تحتوي حقول إدخال — ترفع البطاقة فوق لوحة المفاتيح. */
    imePadding: Boolean = false,
    scrimColor: Color = KararTokens.Glass.scrim,
    contentPosition: Alignment = Alignment.Center,
    contentPadding: PaddingValues = PaddingValues(24.dp),
    content: @Composable () -> Unit
) {
    // مدة التفريغ أطول من أبطأ منحنى خروج (spatial slow ≈ 470ms).
    // «تقليل الحركة»: لا حركة خروج ننتظرها، والبطاقة تظهر/تختفي فورًا بلا زووم.
    val reduceMotion = rememberReduceMotion()
    var composing by remember { mutableStateOf(visible) }
    LaunchedEffect(visible, reduceMotion) {
        if (visible) composing = true else {
            delay(if (reduceMotion) 0L else 450L)
            composing = false
        }
    }

    // ---- التعتيمة: تلاشٍ بنابض التأثيرات (لا ترتد) ----
    val scrimAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = motionEffects<Float>(KararTokens.Motion.EffectsStiffnessSlow),
        label = "spring-dialog-scrim"
    )
    // ---- البطاقة: تلاشٍ + زووم بنابض مكاني + انزلاق طفيف ----
    val cardAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = motionEffects(),
        label = "spring-dialog-alpha"
    )
    val cardScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.84f,
        animationSpec = motionSpatial(
            dampingRatio = 0.62f,
            stiffness = KararTokens.Motion.SpatialStiffnessSlow
        ),
        label = "spring-dialog-scale"
    )
    val cardOffsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 44f,
        animationSpec = motionSpatial(dampingRatio = 0.68f),
        label = "spring-dialog-offset"
    )

    if (!composing) return

    Dialog(
        onDismissRequest = { if (dismissible) onDismissRequest() },
        properties = DialogProperties(
            decorFitsSystemWindows = false,
            usePlatformDefaultWidth = false,
            dismissOnBackPress = dismissible,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = contentPosition
        ) {
            // ── التعتيمة full-bleed: تمتد خلف كل أشرطة النظام والمقطوعات ──
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = scrimAlpha }
                    .background(scrimColor)
                    .clickable(
                        enabled = dismissible && visible,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismissRequest
                    )
            )

            // ── البطاقة المرفوعة ──
            val base = Modifier.widthIn(max = 420.dp)
            val imeAware = if (imePadding) base.imePadding() else base
            Box(
                modifier = imeAware
                    .padding(contentPadding)
                    .graphicsLayer {
                        alpha = cardAlpha
                        scaleX = cardScale
                        scaleY = cardScale
                        translationY = cardOffsetY
                    }
            ) {
                content()
            }
        }
    }
}
