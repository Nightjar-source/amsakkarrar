package com.karrar.amsakiah.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararTokens

/** نغمة سطح الزجاج. */
enum class GlassTone { Dark, DarkSoft, Light }

/**
 * سطح زجاجي موحّد (Liquid Glass 2026) مبني على الشفافية والحد المضيء والظل.
 *
 * لا نستخدم `Modifier.blur` لأنه يضبّب المحتوى نفسه لا الخلفية؛ بدلًا من ذلك
 * نحاكي الزجاج بطبقتين متدرجتين + حد علوي مضيء + ظل خارجي، وهو أسلوب M3
 * Expressive الرسمي للأسطح العائمة.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(KararTokens.Radius.xxl),
    tone: GlassTone = GlassTone.Dark,
    borderWidth: Dp = 1.dp,
    elevation: Dp = 18.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit
) {
    // الألوان من اللوحة الحالية (فاتح/داكن) حتى يبقى الزجاج مقروءًا في الوضعين.
    val palette = com.karrar.amsakiah.ui.theme.LocalKararPalette.current
    val base = when (tone) {
        GlassTone.Dark -> palette.glass
        GlassTone.DarkSoft -> palette.glassSoft
        GlassTone.Light -> palette.glass
    }
    val topTint = when (tone) {
        GlassTone.Light -> palette.highlightStrong
        else -> palette.highlightStrong
    }
    val bottomTint = when (tone) {
        GlassTone.Light -> palette.highlight
        else -> palette.highlight.copy(alpha = palette.highlight.alpha * 0.4f)
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = palette.shadow,
                spotColor = palette.shadow
            )
            .clip(shape)
            .background(base)
            .background(Brush.verticalGradient(listOf(topTint, bottomTint)))
            .border(
                width = borderWidth,
                brush = Brush.verticalGradient(
                    listOf(palette.borderStrong, palette.border)
                ),
                shape = shape
            )
            .padding(contentPadding),
        content = content
    )
}

/** خلفية زجاجية بسيطة دون ظل — للعناصر الصغيرة المتكررة. */
@Composable
fun glassFill(
    shape: Shape = RoundedCornerShape(KararTokens.Radius.pill),
    tone: GlassTone = GlassTone.DarkSoft
): Modifier {
    val palette = com.karrar.amsakiah.ui.theme.LocalKararPalette.current
    val base = when (tone) {
        GlassTone.Dark -> palette.glass
        GlassTone.DarkSoft -> palette.glassSoft
        GlassTone.Light -> palette.glass
    }
    return Modifier
        .clip(shape)
        .background(base)
        .border(1.dp, palette.border, shape)
}
