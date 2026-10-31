package com.karrar.amsakiah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.astronomy.*

@Composable
fun CalendarScreen(
    currentCity: City,
    calculationMethod: CalculationMethod,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ramadanDays = remember(currentCity, calculationMethod) {
        (1..30).map { ramadanDay ->
            val pt = AstronomicalEngine.calculatePrayerTimes(
                2026, 2, ramadanDay,
                currentCity.lat, currentCity.lon, currentCity.tzOffset,
                calculationMethod
            )
            val (imsakStr, _, _) = AstronomicalEngine.formatArcHour(pt.imsak)
            val (fajrStr, _, _) = AstronomicalEngine.formatArcHour(pt.fajr)
            val (maghribStr, _, _) = AstronomicalEngine.formatArcHour(pt.maghrib)
            val (dhuhrStr, _, _) = AstronomicalEngine.formatArcHour(pt.dhuhr)

            Triple(ramadanDay, Pair(imsakStr, fajrStr), Pair(maghribStr, dhuhrStr))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B14))
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
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
                    text = "جدول إمساكية شهر رمضان المبارك",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD54F)
                )
                Text(
                    text = "مواقيت الإمساك والإفطار لمدينة ${currentCity.city}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF1E1B2E),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("اليوم", fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F), fontSize = 13.sp)
                Text("الإمساك", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                Text("الفجر", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                Text("الظهر", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                Text("المغرب", fontWeight = FontWeight.Bold, color = Color(0xFF81C784), fontSize = 13.sp)
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(ramadanDays) { (d, dawnTimes, eveningTimes) ->
                val (imsak, fajr) = dawnTimes
                val (maghrib, dhuhr) = eveningTimes

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF161324),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("رمضان $d", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                        Text(imsak, fontSize = 13.sp, color = Color.White)
                        Text(fajr, fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
                        Text(dhuhr, fontSize = 13.sp, color = Color.White.copy(alpha = 0.85f))
                        Text(maghrib, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
                    }
                }
            }
        }
    }
}
