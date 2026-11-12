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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararTokens
import kotlinx.coroutines.delay

/**
 * ورقة سفلية مدمجة على Spring Motion — تعمل داخل نفس نافذة التطبيق
 * (لا نافذة `android.app.Dialog` منفصلة) ⇒ تمتد خلف أشرطة النظام
 * والمقطوعات (display cutout) بلا فجوات على أي جهاز — بما فيها Samsung.
 *
 * - الدخول: انزلاق من الأسفل بنابض مكاني + تلاشٍ للتعتيمة.
 * - الخروج: نفس المعطيات بالعكس — وتتكرر الحركة مع كل فتح/إغلاق.
 * - التعتيمة full-bleed تغطي الشاشة كاملة؛ تنقر للإغلاق عند السماح.
 * - الخلفية تمتد خلف شريط التنقل (edge-to-edge حقيقي).
 */
@Composable
fun SpringSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissible: Boolean = true,
    /** يتيح إغلاق الورقة بمعاينة التعتيمة (false للتعطيل أثناء انزلاق نشط). */
    dismissibleFromScrim: Boolean = true,
    imePadding: Boolean = false,
    scrimColor: Color = KararTokens.Glass.scrim,
    containerShape: Shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
    containerColor: Color = Color.Unspecified,
    minHeight: Dp = 0.dp,
    maxHeight: Dp = 640.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit
) {
    // مدة التفريغ تتفوق على منحنى الخروج النابض (≈ 470ms slow) كما في SpringDialog.
    var composing by remember { mutableStateOf(visible) }
    LaunchedEffect(visible) {
        if (visible) composing = true else {
            delay(480)
            composing = false
        }
    }

    val scrimAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = KararTokens.Motion.effects(KararTokens.Motion.EffectsStiffnessSlow),
        label = "spring-sheet-scrim"
    )
    val sheetAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = KararTokens.Motion.effects(),
        label = "spring-sheet-alpha"
    )
    val sheetOffset by animateFloatAsState(
        targetValue = if (visible) 0f else 320f,
        animationSpec = KararTokens.Motion.spatial(
            dampingRatio = 0.68f,
            stiffness = KararTokens.Motion.SpatialStiffnessSlow
        ),
        label = "spring-sheet-offset"
    )

    if (!composing) return

    val surface = if (containerColor == Color.Unspecified) KararColors.Card else containerColor

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        // التعتيمة كاملة الشاشة — خلف أشرطة النظام بلا استثناء.
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
                .widthIn(max = 600.dp)
                .heightIn(min = minHeight, max = maxHeight)
                .graphicsLayer { alpha = sheetAlpha; translationY = sheetOffset }
                .shadow(22.dp, containerShape)
                .clip(containerShape)
                .background(surface)
                .padding(contentPadding)
        ) {
            content()
        }
    }
}
