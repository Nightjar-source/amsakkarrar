package com.karrar.amsakiah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.astronomy.*

@Composable
fun MonthlyScreen(
    currentCity: City,
    calculationMethod: CalculationMethod,
    highLatRule: HighLatitudeRule,
    modifier: Modifier = Modifier
) {
    var isHijriMode by remember { mutableStateOf(true) }
    var browsedMonth by remember { mutableStateOf(9) } // 9 = رمضان
    var browsedYear by remember { mutableStateOf(1448) }

    val hijriMonthNames = remember {
        listOf(
            "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
            "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
        )
    }

    val daysList = remember(browsedMonth, browsedYear, currentCity, calculationMethod, highLatRule) {
        (1..30).map { dayNum ->
            val pt = AstronomicalEngine.calculatePrayerTimes(
                2026, 2, dayNum,
                currentCity.lat, currentCity.lon, currentCity.tzOffset,
                calculationMethod, highLatRule
            )
            val (imsakStr, _, _) = AstronomicalEngine.formatArcHour(pt.imsak)
            val (fajrStr, _, _) = AstronomicalEngine.formatArcHour(pt.fajr)
            val (sunriseStr, _, _) = AstronomicalEngine.formatArcHour(pt.sunrise)
            val (dhuhrStr, _, _) = AstronomicalEngine.formatArcHour(pt.dhuhr)
            val (maghribStr, _, _) = AstronomicalEngine.formatArcHour(pt.maghrib)

            // حساب مدة الصيام (من الإمساك إلى المغرب)
            val fastDurationHours = ((pt.maghrib - pt.imsak + 24.0) % 24.0)
            val fHours = fastDurationHours.toInt()
            val fMins = ((fastDurationHours - fHours) * 60).toInt()
            val fastDurationFormatted = "${fHours}س ${fMins}د"

            DayRowData(
                dayNum = dayNum,
                hijriStr = "$dayNum ${hijriMonthNames.getOrElse(browsedMonth - 1) { "رمضان" }}",
                imsak = imsakStr,
                fajr = fajrStr,
                sunrise = sunriseStr,
                dhuhr = dhuhrStr,
                maghrib = maghribStr,
                fastDuration = fastDurationFormatted
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B14))
            .padding(horizontal = 14.dp, vertical = 20.dp)
    ) {
        // شريط التنقل بين الشهور الهجرية والميلادية
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1A1628),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFFFFD54F))
                        Text("التقويم والإمساكية الشاملة", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // تبديل هجري / ميلادي
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF100C1C))
                            .padding(2.dp)
                    ) {
                        Surface(
                            onClick = { isHijriMode = true },
                            color = if (isHijriMode) Color(0xFFFFD54F) else Color.Transparent,
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text(
                                "هجري",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isHijriMode) Color(0xFF1A1226) else Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            onClick = { isHijriMode = false },
                            color = if (!isHijriMode) Color(0xFFFFD54F) else Color.Transparent,
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text(
                                "ميلادي",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isHijriMode) Color(0xFF1A1226) else Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // محدد الشهر مع أزرار التنقل
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (browsedMonth > 1) browsedMonth--
                            else { browsedMonth = 12; browsedYear-- }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF261D38))
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "السابق", tint = Color(0xFFFFD54F))
                    }

                    Text(
                        text = "شهر ${hijriMonthNames.getOrElse(browsedMonth - 1) { "رمضان" }} $browsedYear هـ",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD54F)
                    )

                    IconButton(
                        onClick = {
                            if (browsedMonth < 12) browsedMonth++
                            else { browsedMonth = 1; browsedYear++ }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF261D38))
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "التالي", tint = Color(0xFFFFD54F))
                    }
                }
            }
        }

        // ترويسة الجدول
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF201B33),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("اليوم", fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F), fontSize = 12.sp, modifier = Modifier.weight(1.2f))
                Text("الإمساك", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text("الفجر", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text("المغرب", fontWeight = FontWeight.Bold, color = Color(0xFF81C784), fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text("الصيام", fontWeight = FontWeight.Bold, color = Color(0xFFFFE082), fontSize = 12.sp, modifier = Modifier.weight(1.1f))
            }
        }

        // أسطر الأيام
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(daysList) { row ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF151224),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(row.hijriStr, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFFD54F), modifier = Modifier.weight(1.2f))
                        Text(row.imsak, fontSize = 12.sp, color = Color.White, modifier = Modifier.weight(1f))
                        Text(row.fajr, fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f), modifier = Modifier.weight(1f))
                        Text(row.maghrib, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81C784), modifier = Modifier.weight(1f))
                        Text(row.fastDuration, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFFFFE082), modifier = Modifier.weight(1.1f))
                    }
                }
            }
        }
    }
}

private data class DayRowData(
    val dayNum: Int,
    val hijriStr: String,
    val imsak: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val maghrib: String,
    val fastDuration: String
)
