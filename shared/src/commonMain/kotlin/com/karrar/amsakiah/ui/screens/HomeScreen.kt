package com.karrar.amsakiah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.astronomy.*
import com.karrar.amsakiah.ui.components.KashidaMonthHeader
import com.karrar.amsakiah.ui.components.PrayerCardItem
import com.karrar.amsakiah.ui.components.SkyArcView
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    selectedCity: City,
    onSelectCity: (City) -> Unit,
    calculationMethod: CalculationMethod,
    highLatRule: HighLatitudeRule,
    onNavigateToCalendar: () -> Unit,
    onNavigateToObservatory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSeconds by remember { mutableStateOf(12f * 3600f + 30f * 60f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentSeconds = (currentSeconds + 1f) % 86400f
        }
    }

    val year = 2026
    val month = 10
    val day = 31

    val hijri = remember(year, month, day, calculationMethod.isShia) {
        AstronomicalEngine.getHijriDateSafe(year, month, day, calculationMethod.isShia)
    }

    val prayerTimes = remember(year, month, day, selectedCity, calculationMethod, highLatRule) {
        AstronomicalEngine.calculatePrayerTimes(
            year, month, day,
            selectedCity.lat, selectedCity.lon, selectedCity.tzOffset,
            calculationMethod, highLatRule
        )
    }

    val prayersList = remember(prayerTimes, currentSeconds) {
        val currHour = (currentSeconds / 3600f) % 24f
        val rawList = listOf(
            Triple("imsak", "الإمساك", prayerTimes.imsak),
            Triple("fajr", "الفجر", prayerTimes.fajr),
            Triple("sunrise", "الشروق", prayerTimes.sunrise),
            Triple("dhuhr", "الظهر", prayerTimes.dhuhr),
            Triple("asr", "العصر", prayerTimes.asr),
            Triple("sunset", "الغروب", prayerTimes.sunset),
            Triple("maghrib", "المغرب", prayerTimes.maghrib),
            Triple("isha", "العشاء", prayerTimes.isha),
            Triple("midnight", "منتصف الليل", prayerTimes.midnight)
        )

        val nextIndex = rawList.indexOfFirst { it.third > currHour }.let { if (it == -1) 0 else it }

        rawList.mapIndexed { idx, (id, label, rawH) ->
            val (h12, ap, _) = AstronomicalEngine.formatArcHour(rawH)
            PrayerDisplayItem(
                id = id,
                label = label,
                rawHour = rawH,
                formattedTime = h12,
                amPm = ap,
                isNext = (idx == nextIndex)
            )
        }
    }

    val nextPrayer = prayersList.firstOrNull { it.isNext } ?: prayersList[0]

    val currHour = (currentSeconds / 3600f) % 24f
    val diffHours = ((nextPrayer.rawHour - currHour + 24.0) % 24.0)
    val remH = diffHours.toInt()
    val remM = ((diffHours - remH) * 60).toInt()
    val timeRemainingText = "${remH.toString().padStart(2, '0')}:${remM.toString().padStart(2, '0')}"

    var showCitySheet by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B14)),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            SkyArcView(
                currentSeconds = currentSeconds,
                cityName = "${selectedCity.country} - ${selectedCity.city}",
                nextPrayerName = nextPrayer.label,
                timeRemainingText = timeRemainingText
            )
        }

        item {
            KashidaMonthHeader(
                monthName = hijri.monthName,
                hijriDay = hijri.day,
                hijriYear = hijri.year,
                gregorianFormatted = "السبت، 31 تشرين الأول 2026"
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showCitySheet = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF231F36),
                        contentColor = Color(0xFFFFD54F)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.LocationCity, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(selectedCity.city, fontSize = 12.sp, maxLines = 1)
                }

                Button(
                    onClick = onNavigateToCalendar,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF231F36),
                        contentColor = Color(0xFFFFE082)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("جدول الإمساكية", fontSize = 12.sp, maxLines = 1)
                }

                Button(
                    onClick = onNavigateToObservatory,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF231F36),
                        contentColor = Color(0xFF81C784)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("المرصد 3D", fontSize = 12.sp, maxLines = 1)
                }
            }
        }

        items(prayersList) { prayerItem ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                PrayerCardItem(item = prayerItem)
            }
        }
    }

    if (showCitySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCitySheet = false },
            containerColor = Color(0xFF1E1B2E),
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "اختر المدينة",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD54F),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(modifier = Modifier.height(350.dp)) {
                    items(OfflineCities.cities) { city ->
                        Surface(
                            onClick = {
                                onSelectCity(city)
                                showCitySheet = false
                            },
                            color = if (city == selectedCity) Color(0xFFFFD54F).copy(alpha = 0.2f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${city.country} - ${city.city}",
                                    fontSize = 15.sp,
                                    fontWeight = if (city == selectedCity) FontWeight.Bold else FontWeight.Normal,
                                    color = if (city == selectedCity) Color(0xFFFFD54F) else Color.White
                                )
                                Text(
                                    text = "GMT ${if (city.tzOffset >= 0) "+${city.tzOffset}" else city.tzOffset}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
