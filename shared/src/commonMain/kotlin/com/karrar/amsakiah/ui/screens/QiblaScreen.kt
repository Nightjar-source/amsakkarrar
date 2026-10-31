package com.karrar.amsakiah.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.astronomy.AstronomicalEngine
import com.karrar.amsakiah.core.astronomy.City

@Composable
fun QiblaScreen(
    currentCity: City,
    modifier: Modifier = Modifier
) {
    val jd = remember { AstronomicalEngine.getJulianDay(2026, 10, 31) }
    val qiblaInfo = remember(currentCity, jd) {
        AstronomicalEngine.calculateQiblaAndMoon(currentCity.lat, currentCity.lon, jd)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B14))
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "بوصلة القبلة وطور القمر",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD54F)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "الاتجاه الدقيق نحو الكعبة المشرفة من ${currentCity.city}",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        Box(
            modifier = Modifier
                .size(270.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF251E3D), Color(0xFF141024))
                    )
                )
                .border(2.dp, Color(0xFFFFD54F).copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                for (deg in 0 until 360 step 30) {
                    val angleRad = (deg * AstronomicalEngine.DTR).toFloat()
                    val rOuter = size.width / 2f
                    val rInner = rOuter - (if (deg % 90 == 0) 14.dp.toPx() else 8.dp.toPx())
                    val p1 = Offset(center.x + rOuter * kotlin.math.sin(angleRad), center.y - rOuter * kotlin.math.cos(angleRad))
                    val p2 = Offset(center.x + rInner * kotlin.math.sin(angleRad), center.y - rInner * kotlin.math.cos(angleRad))
                    drawLine(
                        color = if (deg % 90 == 0) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.3f),
                        start = p1,
                        end = p2,
                        strokeWidth = if (deg % 90 == 0) 2.5.dp.toPx() else 1.dp.toPx()
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(qiblaInfo.qiblaBearing.toFloat()),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "سهم القبلة",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = Color(0xFF161226),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD54F)),
                modifier = Modifier.size(90.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${qiblaInfo.qiblaBearing.toInt()}°",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD54F)
                    )
                    Text(
                        text = "زاوية القبلة",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E1B2E),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Mosque, contentDescription = null, tint = Color(0xFFFFD54F))
                        Column {
                            Text("المسافة المباشرة إلى الكعبة المشرفة", fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("${qiblaInfo.distanceKm} كم", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E1B2E),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Brightness2, contentDescription = null, tint = Color(0xFFFFE082))
                        Column {
                            Text("طور القمر الفلكي (Moon Phase)", fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
                            Text(qiblaInfo.phaseName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFE082))
                        }
                    }
                    Text(
                        text = "${qiblaInfo.illuminationPercent}%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD54F)
                    )
                }
            }
        }
    }
}
