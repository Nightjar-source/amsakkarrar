package com.karrar.amsakiah.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.astronomy.*

@Composable
fun ObservatoryScreen(
    currentCity: City,
    calculationMethod: CalculationMethod,
    highLatRule: HighLatitudeRule,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val jd = remember { AstronomicalEngine.getJulianDay(2026, 10, 31) }
    val qiblaAndMoon = remember(currentCity, jd) {
        AstronomicalEngine.calculateQiblaAndMoon(currentCity.lat, currentCity.lon, jd)
    }

    val prayerTimes = remember(currentCity, calculationMethod, highLatRule) {
        AstronomicalEngine.calculatePrayerTimes(
            2026, 10, 31,
            currentCity.lat, currentCity.lon, currentCity.tzOffset,
            calculationMethod, highLatRule
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B14))
            .padding(horizontal = 16.dp, vertical = 20.dp),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF1E1B2E))
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "رجوع", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "مرصد الكفيل الفلكي 3D",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD54F)
                    )
                    Text(
                        text = "بيانات الميكانيكا الفلكية والانحراف الشمسي",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // القبة السماوية التفاعلية
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF161226),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        val w = size.width
                        val h = size.height
                        val cx = w / 2f
                        val cy = h / 2f

                        // دوائر المدارات السماوية
                        drawCircle(
                            color = Color(0xFFFFD54F).copy(alpha = 0.15f),
                            radius = w * 0.45f,
                            style = Stroke(1.5.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFF81C784).copy(alpha = 0.15f),
                            radius = w * 0.30f,
                            style = Stroke(1.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFF64B5F6).copy(alpha = 0.15f),
                            radius = w * 0.15f,
                            style = Stroke(1.dp.toPx())
                        )

                        // مركز الأرض
                        drawCircle(
                            color = Color(0xFF4FC3F7),
                            radius = 10.dp.toPx(),
                            center = Offset(cx, cy)
                        )

                        // موضع الشمس التقريبي
                        val sunX = cx + (w * 0.35f) * kotlin.math.cos(0.8).toFloat()
                        val sunY = cy - (h * 0.35f) * kotlin.math.sin(0.8).toFloat()
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFD54F), Color.Transparent),
                                center = Offset(sunX, sunY),
                                radius = 25.dp.toPx()
                            ),
                            radius = 25.dp.toPx(),
                            center = Offset(sunX, sunY)
                        )
                        drawCircle(
                            color = Color(0xFFFFD54F),
                            radius = 8.dp.toPx(),
                            center = Offset(sunX, sunY)
                        )

                        // موضع القمر
                        val moonX = cx - (w * 0.28f) * kotlin.math.cos(1.2).toFloat()
                        val moonY = cy + (h * 0.28f) * kotlin.math.sin(1.2).toFloat()
                        drawCircle(
                            color = Color(0xFFECEFF1),
                            radius = 6.dp.toPx(),
                            center = Offset(moonX, moonY)
                        )
                    }

                    Column(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "إحداثيات: خط عرض ${currentCity.lat}° | خط طول ${currentCity.lon}°",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // بطاقات الميكانيكا الفلكية (من معادلات كبلر)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E1B2E),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("ميل الشمس (Declination)", fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${prayerTimes.declination}°", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E1B2E),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("معادلة الوقت (EqT)", fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${prayerTimes.eqtMinutes} دقيقة", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E1B2E),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("طول النهار", fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${prayerTimes.dayLengthHours} ساعة", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFE082))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E1B2E),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("طول الليل", fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${prayerTimes.nightLengthHours} ساعة", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF90CAF9))
                    }
                }
            }
        }

        // بيانات القمر الفلكي
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF1E1B2E),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(qiblaAndMoon.phaseSymbol, fontSize = 32.sp)
                        Column {
                            Text(qiblaAndMoon.phaseName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                            Text("عمر القمر: ${qiblaAndMoon.moonAgeDays} يوماً", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                    Text(
                        "${qiblaAndMoon.illuminationPercent}% إضاءة",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF81C784)
                    )
                }
            }
        }
    }
}
