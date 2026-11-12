package com.karrar.amsakiah.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.karrar.amsakiah.ui.theme.KararTokens
import kotlinx.coroutines.delay

/**
 * نافذة تأكيد/حوار مدمجة على Spring Motion — تعمل *داخل نفس نافذة التطبيق*.
 *
 * لماذا ليست `Dialog`؟
 * - نافذة الحوار على أندرويد قد لا تمتد حتى حواف الشاشة على بعض الأجهزة
 *   (Samsung One UI بالذات)، فتظهر "أشرطة فارغة" داكنة حول النافذة وتخالف
 *   معايير edge-to-edge.
 * - هنا: ترسم التعتيمة (scrim) `fillMaxSize` داخل نفس المحتوى، فتمتد خلف
 *   أشرطة النظام تلقائيًا — فلا أشرطة سوداء/فاتحة فارغة أبدًا على أي جهاز.
 *
 * الحركة كلها springs وتُعاد كل فتح/إغلاق:
 * - الدخول: تلاشٍ بنابض التأثيرات + Scale/Offset مكاني للبطاقة.
 * - الخروج: عكس ذلك — ومدة تفريغ الـ[compose] مضبوطة على منحنى النابض
 *   حتى تظهر النوافذ مع كل دخول وخروج بلا أي حفظ للكاش.
 *
 * توافق One UI Reachability: لا شيء في هذا المكوِّن يعترض إيماءة "سحب
 * الشاشة إلى المنتصف" — فالبطاقة تتمرك تلقائيًا حيث تريد المستخدم أن تُزلَ.
 */
@Composable
fun SpringDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissible: Boolean = true,
    /** عند true (لوحات فيها حقول إدخال) تُرفَع البطاقة فوق لوحة المفاتيح بنابض. */
    imePadding: Boolean = false,
    scrimColor: Color = KararTokens.Glass.scrim,
    contentPosition: Alignment = Alignment.Center,
    contentPadding: PaddingValues = PaddingValues(24.dp),
    content: @Composable () -> Unit
) {
    // مدة التفريغ يجب أن تتفوق على أبطأ نابض استخدام (spatial slow ≈ 470ms).
    var composing by remember { mutableStateOf(visible) }
    LaunchedEffect(visible) {
        if (visible) composing = true else {
            delay(450)
            composing = false
        }
    }

    // ---- التعتيمة: تلاشٍ بنابض التأثيرات (لا ترتد) ----
    val scrimAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = KararTokens.Motion.effects(KararTokens.Motion.EffectsStiffnessSlow),
        label = "spring-dialog-scrim"
    )
    // ---- البطاقة: تلاشٍ + زووم + انزلاق بنابض مكاني ----
    val cardAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = KararTokens.Motion.effects(),
        label = "spring-dialog-alpha"
    )
    val cardScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.84f,
        animationSpec = KararTokens.Motion.spatial(dampingRatio = 0.62f, stiffness = KararTokens.Motion.SpatialStiffnessSlow),
        label = "spring-dialog-scale"
    )
    val cardOffsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 44f,
        animationSpec = KararTokens.Motion.spatial(dampingRatio = 0.68f),
        label = "spring-dialog-offset"
    )

    if (!composing) return

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = contentPosition
    ) {
        // ── التعتيمة (Full-bleed) — تمتد خلف كل أشرطة النظام بلا استثناء ──
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
