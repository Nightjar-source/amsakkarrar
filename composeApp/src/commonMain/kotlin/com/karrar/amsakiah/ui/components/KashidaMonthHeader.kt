package com.karrar.amsakiah.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.ui.theme.SkyTheme

@Composable
fun KashidaMonthHeader(
    monthName: String,
    hijriDay: Int,
    hijriYear: Int,
    gregorianFormatted: String,
    modifier: Modifier = Modifier
) {
    val (pre, post) = SkyTheme.getKashidaMonthParts(monthName)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // بطاقة الكشيدة المتصلة
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E1B2E).copy(alpha = 0.85f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // صف الكشيدة المبتكرة: [بادئة الكشيدة] + [دائرة اليوم الهجري الذهبية] + [لاحقة الكشيدة]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = pre,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD54F)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // دائرة اليوم الهجري الذهبية الفاخرة
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFFFD54F), Color(0xFFFFA000))
                                )
                            )
                            .border(2.dp, Color(0xFFFFF8E1), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = hijriDay.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF261200)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = post,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD54F)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // السنة الهجرية والتاريخ الميلادي المقابل
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$hijriYear هـ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFE082)
                    )
                    Text(
                        text = "•",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        text = gregorianFormatted,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}
