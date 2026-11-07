package com.karrar.amsakiah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.state.AppState
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.core.state.PrayerMeta
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.components.KashidaHeader
import com.karrar.amsakiah.ui.theme.KararColors
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

@Composable
fun CalendarScreen(state: AppState, contentPadding: PaddingValues) {
    val settings = state.settings
    val today = PrayerCalculator.todayDate(settings)
    var monthShift by remember { mutableIntStateOf(0) }

    val monthStart: LocalDate = remember(settings.hijriOffset, settings.methodCode, today, monthShift) {
        var start = today
        val hijri = PrayerCalculator.hijriFor(settings, today)
        start = today.plus(DatePeriod(days = -(hijri.day - 1)))
        var shift = monthShift
        while (shift > 0) {
            start = start.plus(DatePeriod(days = 30))
            val h = PrayerCalculator.hijriFor(settings, start)
            start = start.plus(DatePeriod(days = -(h.day - 1)))
            shift--
        }
        while (shift < 0) {
            start = start.plus(DatePeriod(days = -29))
            val h = PrayerCalculator.hijriFor(settings, start)
            start = start.plus(DatePeriod(days = -(h.day - 1)))
            shift++
        }
        start
    }

    val monthHijri = remember(monthStart) { PrayerCalculator.hijriFor(settings, monthStart) }

    val days = remember(monthStart, settings) {
        buildList {
            var date = monthStart
            repeat(31) { index ->
                val h = PrayerCalculator.hijriFor(settings, date)
                if (h.monthNum != monthHijri.monthNum || h.year != monthHijri.year) return@buildList
                val times = PrayerCalculator.adjustedTimes(PrayerCalculator.timesFor(settings, date), settings)
                add(CalendarDay(h.day, date, times, h.monthName))
                date = date.plus(DatePeriod(days = 1))
            }
        }
    }

    val todayHijriDay = PrayerCalculator.hijriFor(settings, today).day

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), corner = 24.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    MonthArrow(Icons.AutoMirrored.Filled.KeyboardArrowRight) { monthShift-- }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        KashidaHeader(hijriDay = monthHijri.day, monthName = monthHijri.monthName, fontSize = 26.sp)
                        Text(
                            "${monthHijri.year} هـ — ${days.size} يومًا",
                            color = KararColors.TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    MonthArrow(Icons.AutoMirrored.Filled.KeyboardArrowLeft) { monthShift++ }
                }
                if (monthShift != 0) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "العودة إلى الشهر الحالي",
                        color = KararColors.Gold,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable { monthShift = 0 }
                    )
                }
            }
        }

        items(days, key = { it.date.toString() }) { day ->
            val isToday = monthShift == 0 && day.hijriDay == todayHijriDay
            // نفس فلترة الشاشة الرئيسية: الغروب شيعي فقط، والإمساك حسب وضعه.
            val dayVisible = PrayerCalculator.visibleIds(settings, day.date)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                corner = 18.dp,
                color = when {
                    isToday -> Color(0x1AFFD700)
                    day.hijriDay < todayHijriDay && monthShift == 0 -> Color(0x08FFFFFF)
                    else -> KararColors.CardSoft
                },
                borderColor = if (isToday) KararColors.Gold else KararColors.Border,
                contentPadding = PaddingValues(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .background(
                                if (isToday) KararColors.Gold else Color(0x1AFFFFFF),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            day.hijriDay.toString(),
                            color = if (isToday) Color(0xFF241A00) else KararColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(Modifier.size(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            PrayerCalculator.formattedGregorian(day.date),
                            color = KararColors.TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            day.times.entries
                                .filter { it.key in dayVisible && PrayerMeta.fixed(it.key) }
                                .joinToString("  ") { (id, value) ->
                                    "${PrayerCalculator.formatHour(value).first} ${shortLabel(id)}"
                                },
                            color = KararColors.TextMuted,
                            fontSize = 9.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthArrow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .size(36.dp)
            .background(Color(0x14FFFFFF), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = KararColors.Gold, modifier = Modifier.size(20.dp))
    }
}

private fun shortLabel(id: String): String = when (id) {
    "imsak" -> "إمساك"
    "fajr" -> "فجر"
    "sunrise" -> "شروق"
    "dhuhr" -> "ظهر"
    "asr" -> "عصر"
    "sunset" -> "غروب"
    "maghrib" -> "مغرب"
    "isha" -> "عشاء"
    "midnight" -> "منتصف"
    else -> id
}

private data class CalendarDay(
    val hijriDay: Int,
    val date: LocalDate,
    val times: Map<String, Double>,
    val monthName: String
)
