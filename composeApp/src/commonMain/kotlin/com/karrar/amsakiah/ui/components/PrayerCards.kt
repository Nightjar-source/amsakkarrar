package com.karrar.amsakiah.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.astronomy.PrayerDisplayItem

@Composable
fun PrayerCardItem(
    item: PrayerDisplayItem,
    modifier: Modifier = Modifier
) {
    val isNext = item.isNext

    val cardBg = if (isNext) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF3E2D54), Color(0xFF261938))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFF1E1B2E).copy(alpha = 0.9f), Color(0xFF151322).copy(alpha = 0.9f))
        )
    }

    val borderColor = if (isNext) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.08f)
    val borderWidth = if (isNext) 1.5.dp else 1.dp

    val icon: ImageVector = when (item.id) {
        "imsak", "midnight" -> Icons.Default.NightsStay
        "fajr", "sunrise" -> Icons.Default.WbTwilight
        "dhuhr", "asr" -> Icons.Default.WbSunny
        "sunset", "maghrib" -> Icons.Default.Brightness4
        "isha" -> Icons.Default.Bedtime
        else -> Icons.Default.Schedule
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(18.dp)),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .background(cardBg)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // أيقونة واسم الصلاة
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isNext) Color(0xFFFFD54F).copy(alpha = 0.2f)
                            else Color.White.copy(alpha = 0.06f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isNext) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = item.label,
                        fontSize = 16.sp,
                        fontWeight = if (isNext) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isNext) Color(0xFFFFD54F) else Color.White
                    )
                    if (isNext) {
                        Text(
                            text = "الصلاة القادمة",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF81C784)
                        )
                    }
                }
            }

            // وقت الصلاة بصيغة 12 ساعة
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.formattedTime,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isNext) Color(0xFFFFD54F) else Color.White,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = item.amPm,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}
