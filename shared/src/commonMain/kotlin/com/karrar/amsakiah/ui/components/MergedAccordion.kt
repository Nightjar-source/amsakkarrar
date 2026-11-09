package com.karrar.amsakiah.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararTokens

/**
 * «نافذة منسدلة» واحدة بعنوان مدمج.
 *
 * التصميم المطلوب (وليس بطاقة عنوان منفصلة عن بطاقة محتوى): إطار واحد يحتوي
 * رأسًا ثابتًا ثم محتوى يُطوى/يُفرد داخله، فيتقلّص الإطار ويتوسّع ككتلة
 * واحدة، والعنوان يبقى داخل النافذة نفسها لا فوقها.
 *
 * الخفة:
 * - الشفافية/التدوير يُقرآن داخل `graphicsLayer`/`rotate` (لا إعادة تركيب).
 * - الطي/الفرد بنابض مكاني، والتلاشي بنابض تأثيرات.
 * - لا خلفية/حدود إضافية للعنوان: سطح واحد فقط.
 */
@Composable
fun MergedAccordion(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    leadingTint: Color = KararColors.Gold,
    leadingBackground: Color = KararColors.GoldSoft,
    container: Color = KararColors.CardSoft,
    borderColor: Color = KararColors.Border,
    titleColor: Color = KararColors.TextPrimary,
    corner: Dp = 18.dp,
    subtitle: String? = null,
    /** نص صغير على يمين العنوان (مثل عدد الأذكار في المجموعة). */
    trailingText: String? = null,
    initiallyOpen: Boolean = false,
    /** يُعرض المحتوى دائمًا داخل النافذة بلا طيّ (للمجموعات المفتوحة افتراضيًا). */
    alwaysOpen: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(
        start = 14.dp,
        end = 14.dp,
        top = 4.dp,
        bottom = 14.dp
    ),
    content: @Composable ColumnScope.() -> Unit
) {
    var open by rememberSaveable(title) { mutableStateOf(initiallyOpen || alwaysOpen) }
    val expanded = alwaysOpen || open

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = KararTokens.Motion.spatial(),
        label = "accordion-chevron"
    )
    val dividerAlpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = KararTokens.Motion.effects(),
        label = "accordion-divider"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(corner))
            .background(container)
            .border(1.dp, borderColor, RoundedCornerShape(corner))
    ) {
        // ---- الرأس: داخل النافذة نفسها، بلا سطح منفصل ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (alwaysOpen) Modifier else Modifier.clickable { open = !open }
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(leadingBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = leadingTint,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = titleColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        color = KararColors.TextMuted,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    color = KararColors.TextMuted,
                    fontSize = 10.sp
                )
                Spacer(Modifier.width(6.dp))
            }
            if (!alwaysOpen) {
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "إغلاق" else "فتح",
                    tint = if (expanded) KararColors.Gold else KararColors.TextMuted,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotation)
                )
            }
        }

        // خط فاصل رقيق داخل النافذة نفسها — يُظهر الحدّ بين العنوان والمحتوى
        // بلا فصل بصري: الشفافية تُقرأ في طبقة الرسم فلا إعادة تركيب.
        HorizontalDivider(
            modifier = Modifier.graphicsLayer { alpha = dividerAlpha },
            thickness = 1.dp,
            color = borderColor
        )

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = KararTokens.Motion.spatial()) +
                fadeIn(animationSpec = KararTokens.Motion.effects()),
            exit = shrinkVertically(
                animationSpec = KararTokens.Motion.spatial(KararTokens.Motion.SpatialStiffnessFast)
            ) + fadeOut(animationSpec = KararTokens.Motion.effects())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = content
            )
        }
    }
}
