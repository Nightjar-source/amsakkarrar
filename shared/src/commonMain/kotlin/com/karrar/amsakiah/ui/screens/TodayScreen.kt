package com.karrar.amsakiah.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.state.AppState
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.core.state.PrayerMeta
import com.karrar.amsakiah.core.state.PrayerSlot
import com.karrar.amsakiah.core.weather.WeatherInfo
import com.karrar.amsakiah.ui.components.KashidaHeader
import com.karrar.amsakiah.ui.components.SkyArc
import com.karrar.amsakiah.ui.theme.SkyTheme
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt

/**
 * الشاشة الرئيسية «اليوم» — نقل حرفي لتصميم V7:
 * هيدر (المدينة/الطقس + الكشيدة والتاريخ) ← القوس 3D ← العدّاد التنازلي
 * ← أزرار الإعدادات/الإمساكية ← مربعات الصلوات.
 */
@Composable
fun TodayScreen(
    state: AppState,
    weather: WeatherInfo?,
    onOpenCalendar: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenQibla: () -> Unit,
    contentPadding: PaddingValues
) {
    val settings = state.settings
    val nowInstant = state.now
    val zone = remember(settings.timeZoneId) { PrayerCalculator.zoneOf(settings) }
    val nowLdt: LocalDateTime = remember(nowInstant, zone) { nowInstant.toLocalDateTime(zone) }
    val nowSeconds = PrayerCalculator.secondsOfDay(nowLdt)
    val date: LocalDate = nowLdt.date

    val hijri = remember(settings, date) { PrayerCalculator.hijriFor(settings, date) }
    val tomorrowDate = remember(date) { date.plus(DatePeriod(days = 1)) }
    val tomorrowHijri = remember(settings, tomorrowDate) { PrayerCalculator.hijriFor(settings, tomorrowDate) }

    val todayTimes = remember(settings, date) {
        PrayerCalculator.adjustedTimes(PrayerCalculator.timesFor(settings, date), settings)
    }
    val tomorrowTimes = remember(settings, tomorrowDate) {
        PrayerCalculator.adjustedTimes(PrayerCalculator.timesFor(settings, tomorrowDate), settings)
    }

    val fajrHour = todayTimes["fajr"] ?: 5.0
    val maghribHour = todayTimes["maghrib"] ?: 18.0
    val nowHours = nowSeconds / 3600.0
    val isNight = nowHours < fajrHour || nowHours >= maghribHour

    // نسبة تقدّم القوس (نهارًا من الفجر إلى المغرب، وليلًا من المغرب إلى الفجر)
    val arcProgress: Float = run {
        val value = if (!isNight) {
            (nowHours - fajrHour) / (maghribHour - fajrHour).coerceAtLeast(0.001)
        } else {
            val elapsed = if (nowHours >= maghribHour) {
                nowHours - maghribHour
            } else {
                nowHours + (24.0 - maghribHour)
            }
            elapsed / ((24.0 - maghribHour) + fajrHour).coerceAtLeast(0.001)
        }
        value.toFloat().coerceIn(0f, 1f)
    }

    // ---------- الصلوات الظاهرة ----------
    val ids = remember(settings.visiblePrayers) {
        PrayerMeta.order.filter { it in settings.visiblePrayers }
    }
    val slots = ids.map { id ->
        val raw = todayTimes[id] ?: 0.0
        val (t12, ap, t24) = PrayerCalculator.formatHour(raw)
        PrayerSlot(id, PrayerMeta.label(id), raw, t12, ap, t24)
    }

    // الفهرس التلقائي: أول صلاة لم يحن وقتها اليوم، وإلا الأولى غدًا
    val autoIndex = ids.indexOfFirst { id ->
        (todayTimes[id] ?: 0.0).let { it * 3600.0 } > nowSeconds
    }.let { if (it < 0) 0 else it }

    val pagerState = rememberPagerState(initialPage = autoIndex) { ids.size.coerceAtLeast(1) }
    LaunchedEffect(autoIndex) {
        if (!pagerState.isScrollInProgress) pagerState.animateScrollToPage(autoIndex)
    }

    val page = pagerState.currentPage.coerceIn(0, (ids.size - 1).coerceAtLeast(0))
    val targetId = ids.getOrNull(page) ?: ids.firstOrNull() ?: "fajr"
    val targetLabel = PrayerMeta.label(targetId)
    val targetTodaySec = ((todayTimes[targetId] ?: 0.0) * 3600.0).roundToInt()
    val targetTomorrowSec = ((tomorrowTimes[targetId] ?: 0.0) * 3600.0).roundToInt()
    val isTomorrow = nowSeconds >= targetTodaySec
    val targetSeconds = if (isTomorrow) targetTomorrowSec else targetTodaySec
    val diffSeconds = if (isTomorrow) {
        (86400 - nowSeconds) + targetSeconds
    } else {
        targetSeconds - nowSeconds
    }.coerceAtLeast(0)
    val (hoursLeft, minutesLeft, secondsLeft) = PrayerCalculator.countdownText(diffSeconds)

    val (skyTop, skyBottom, skyGlow) = SkyTheme.getSkyColorsForSecond(nowSeconds.toFloat())

    val hour12 = if (nowLdt.hour % 12 == 0) 12 else nowLdt.hour % 12
    val timeText = "$hour12:${nowLdt.minute.toString().padStart(2, '0')}"
    val meridiem = if (nowLdt.hour >= 12) "مساءً" else "صباحاً"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(Brush.verticalGradient(listOf(skyTop, skyBottom)))
                drawRect(
                    Brush.radialGradient(
                        colorStops = arrayOf(0f to skyGlow, 0.8f to Color.Transparent),
                        center = Offset(size.width / 2f, size.height * 0.35f),
                        radius = size.maxDimension * 0.75f
                    )
                )
            }
            .padding(bottom = contentPadding.calculateBottomPadding())
    ) {
        // ---------- الهيدر ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // يمين: المدينة + الطقس
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HeaderChip(onClick = onOpenSettings) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        settings.cityName,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (settings.showWeather) {
                    HeaderChip {
                        if (weather != null) {
                            WeatherGlyph(weather.code)
                            Text(
                                "${weather.temperatureC}°C",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                style = androidx.compose.ui.text.TextStyle(textDirection = TextDirection.Ltr)
                            )
                        } else {
                            Text("...", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
                        }
                    }
                }
            }

            // يسار: الكشيدة + التاريخ
            Column(horizontalAlignment = Alignment.End) {
                KashidaHeader(
                    hijriDay = hijri.day,
                    monthName = hijri.monthName,
                    modifier = Modifier.padding(top = 8.dp),
                    fontSize = 44.sp
                )
                Spacer(Modifier.height(8.dp))
                Row {
                    Text(
                        text = PrayerCalculator.formattedGregorian(date),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " (${hijri.year} هجرية)",
                        color = Color(0xFFFFFDE7).copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ---------- القوس 3D ----------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            SkyArc(
                progress = arcProgress,
                isNight = isNight,
                moonPhase = hijri.day / 30.0,
                timeText = timeText,
                meridiem = meridiem,
                modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp)
            )
        }

        // ---------- العدّاد التنازلي ----------
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(126.dp),
                pageSpacing = 12.dp
            ) { p ->
                val slotId = ids.getOrNull(p) ?: return@HorizontalPager
                val isTmrw = nowSeconds >= ((todayTimes[slotId] ?: 0.0) * 3600.0).roundToInt()
                val secs = if (isTmrw) {
                    ((tomorrowTimes[slotId] ?: 0.0) * 3600.0).roundToInt()
                } else {
                    ((todayTimes[slotId] ?: 0.0) * 3600.0).roundToInt()
                }
                val remaining = (if (isTmrw) (86400 - nowSeconds) + secs else secs - nowSeconds)
                    .coerceAtLeast(0)
                val (h, m, s) = PrayerCalculator.countdownText(remaining)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // شريحة العنوان
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0x0DFFFFFF))
                            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(50))
                            .padding(horizontal = 24.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "باقي على ${PrayerMeta.label(slotId)}" + if (isTmrw) {
                                " غداً ${PrayerCalculator.weekDays[tomorrowDate.dayOfWeek.ordinal]} ${tomorrowHijri.day} ${tomorrowHijri.monthName}"
                            } else "",
                            color = Color(0xFFFFD700),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            maxLines = 1
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        DigitBox(h)
                        Colon()
                        DigitBox(m)
                        Colon()
                        DigitBox(s)
                    }
                }
            }

            // نقاط الصفحات
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(ids.size) { index ->
                    val active = pagerState.currentPage == index
                    Box(
                        Modifier
                            .height(6.dp)
                            .width(if (active) 32.dp else 8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (active) Color(0xFFFFD700) else Color(0x33FFFFFF))
                    )
                }
            }
        }

        // ---------- المربعات السفلية ----------
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SquareAction(Icons.Filled.Settings, "الإعدادات", onOpenSettings)
                SquareAction(Icons.Filled.CalendarMonth, "الإمساكية", onOpenCalendar)
            }

            Spacer(Modifier.height(16.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(slots, key = { it.id }) { slot ->
                    PrayerBox(
                        slot = slot,
                        active = slot.id == targetId,
                        tomorrow = slot.id == targetId && isTomorrow,
                        tomorrowSlot = if (slot.id == targetId && isTomorrow) {
                            val raw = tomorrowTimes[slot.id] ?: slot.rawHour
                            val (t12, ap, t24) = PrayerCalculator.formatHour(raw)
                            PrayerSlot(slot.id, slot.label, raw, t12, ap, t24, isTomorrow = true)
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }
}

// =====================================================================
// مكوّنات مساعدة
// =====================================================================

@Composable
private fun HeaderChip(
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0x0DFFFFFF))
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(50))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        content = content
    )
}

@Composable
private fun WeatherGlyph(code: Int?) {
    val c = code ?: 0
    val (icon, tint) = when {
        c <= 1 -> Icons.Filled.WbSunny to Color(0xFFFFD700)
        c <= 3 -> Icons.Filled.Cloud to Color.White
        c <= 48 -> Icons.Filled.Cloud to Color(0xFFD0D0D0)
        c <= 67 -> Icons.Filled.WaterDrop to Color(0xFF90CAF9)
        c <= 79 -> Icons.Filled.AcUnit to Color.White
        else -> Icons.Filled.Bolt to Color(0xFFFFF59D)
    }
    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(13.dp))
}

@Composable
private fun DigitBox(value: String) {
    Box(
        modifier = Modifier.size(width = 70.dp, height = 60.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                (slideInVertically(tween(260)) { full -> full } + fadeIn(tween(260))) togetherWith
                    (slideOutVertically(tween(260)) { full -> -full } + fadeOut(tween(260)))
            },
            label = "digit"
        ) { text ->
            Text(
                text = text,
                color = Color.White,
                fontSize = 57.sp,
                fontWeight = FontWeight.Light,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun Colon() {
    Text(
        text = ":",
        color = Color(0x4DFFFFFF),
        fontSize = 30.sp,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun SquareAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x0DFFFFFF))
            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun PrayerBox(
    slot: PrayerSlot,
    active: Boolean,
    tomorrow: Boolean,
    tomorrowSlot: PrayerSlot?
) {
    val shown = tomorrowSlot ?: slot
    val scale = androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(if (active) 1.1f else 1f) }
    LaunchedEffect(active) { scale.animateTo(if (active) 1.1f else 1f, tween(400)) }

    Column(
        modifier = Modifier
            .widthIn(min = 95.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                translationY = if (active) -16.dp.toPx() else 0f
            }
            .clip(RoundedCornerShape(26.dp))
            .background(if (active) Color(0x26FFFFFF) else Color(0x0DFFFFFF))
            .border(
                1.dp,
                if (active) Color(0x4DFFFFFF) else Color(0x0DFFFFFF),
                RoundedCornerShape(26.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                prayerIcon(slot.id),
                contentDescription = null,
                tint = if (active) Color(0xFFFFD700) else Color(0x80FFFFFF),
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = slot.label,
                color = if (active) Color(0xFFFFD700) else Color(0x80FFFFFF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = shown.time12,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = shown.amPm,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 8.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        if (tomorrow) {
            Text(
                text = "(غداً)",
                color = Color(0xFFFFD700),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun prayerIcon(id: String): ImageVector = when (id) {
    "imsak", "isha", "midnight" -> Icons.Filled.NightsStay
    "fajr", "sunrise" -> Icons.Filled.WbTwilight
    "dhuhr", "asr" -> Icons.Filled.WbSunny
    "sunset", "maghrib" -> Icons.Filled.WbTwilight
    else -> Icons.Filled.WbSunny
}
