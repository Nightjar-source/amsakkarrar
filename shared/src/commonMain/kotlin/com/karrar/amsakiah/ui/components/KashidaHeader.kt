package com.karrar.amsakiah.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.ui.theme.KararFonts
import com.karrar.amsakiah.ui.theme.LocalKararPalette
import com.karrar.amsakiah.ui.theme.SkyTheme

/**
 * كشيدة الشهر — نسخة حرفية من V7:
 * `رمضـ` + كشيدة بيضاء متصلة يتوسّطها رقم اليوم بالذهبي (`#FFD700`) + `ـان`.
 * خط `Reem Kufi` كما في التطبيق المرجعي (font-reem).
 */
@Composable
fun KashidaHeader(
    hijriDay: Int,
    monthName: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 44.sp
) {
    val reem = KararFonts.ReemKufi
    val palette = LocalKararPalette.current
    val ink = palette.skyText
    val (pre, post) = SkyTheme.getKashidaMonthParts(monthName)
    val unit = fontSize.value
    val daySize = (unit * (38.4f / 44.8f)).sp

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text = pre,
            color = ink,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = reem
        )
        Box(
            modifier = Modifier.widthIn(min = (unit * (45f / 44.8f)).dp),
            contentAlignment = Alignment.Center
        ) {
            // الكشيدة المتصلة في الخلفية (بيضاء)
            Text(
                text = "ـــــ",
                color = ink.copy(alpha = 0.92f),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = reem
            )
            // رقم اليوم فوقها بالذهبي مع ظل خفيف
            Text(
                text = hijriDay.toString(),
                color = palette.gold,
                fontSize = daySize,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = reem,
                modifier = Modifier.offset(y = (-(unit * (6f / 44.8f))).dp)
            )
        }
        Text(
            text = post,
            color = ink,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = reem
        )
    }
}
