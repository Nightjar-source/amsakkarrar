package com.karrar.amsakiah.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.ui.theme.KararColors

/** خلفية الشاشة المتدرجة (ليل → فجر) */
@Composable
fun ScreenBackground(
    modifier: Modifier = Modifier,
    top: Color = KararColors.BackgroundTop,
    bottom: Color = KararColors.BackgroundBottom,
    content: BoxScopeContent
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(top, bottom)))
    ) {
        content()
    }
}

typealias BoxScopeContent = @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit

/** بطاقة زجاجية موحّدة */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    corner: Dp = 24.dp,
    color: Color = KararColors.CardSoft,
    borderColor: Color = KararColors.Border,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(corner))
            .background(color, RoundedCornerShape(corner))
            .border(1.dp, borderColor, RoundedCornerShape(corner))
            .padding(contentPadding),
        content = content
    )
}

/** شريحة صغيرة (مدينة / طقس / معلومة) */
@Composable
fun InfoChip(
    text: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    textColor: Color = KararColors.TextPrimary,
    container: Color = KararColors.CardSoft,
    border: Color = KararColors.Border
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(container, RoundedCornerShape(50))
            .border(1.dp, border, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(14.dp))
        }
        Text(text = text, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

/** عنوان قسم داخل شاشة */
@Composable
fun SectionTitle(
    text: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = KararColors.Gold, modifier = Modifier.size(18.dp))
        }
        Text(
            text = text,
            color = KararColors.Gold,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start
        )
        Spacer(Modifier.width(4.dp))
        Box(
            Modifier
                .height(1.dp)
                .weight(1f)
                .background(
                    Brush.horizontalGradient(
                        listOf(KararColors.Gold.copy(alpha = 0.35f), Color.Transparent)
                    )
                )
        )
    }
}

/** بطاقة سطر إعداد/معلومة */
@Composable
fun RowCard(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth(), corner = 20.dp, contentPadding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(KararColors.GoldSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = KararColors.Gold, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, color = KararColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                if (subtitle != null) {
                    Text(subtitle, color = KararColors.TextMuted, fontSize = 11.sp)
                }
            }
            trailing?.invoke()
        }
    }
}
