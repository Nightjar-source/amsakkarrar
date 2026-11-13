package com.karrar.amsakiah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.state.AppState
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.core.state.PrayerMeta
import com.karrar.amsakiah.ui.theme.KararIcons
import com.karrar.amsakiah.ui.theme.LocalKararPalette
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.math.roundToInt

/**
 * تقويم القوس — **شاشة مستقلة** (مطابقة لـ`amsak3d.tsx`: `arcShowCalendarModal`).
 * تُفتح من زر التقويم في شاشة «اليوم» وتغطي الشاشة كاملة (لا نافذة ولا شبكة):
 *
 * - الرأس: زر رجوع + «تقويم شهر X لعام Y هـ» + الرقمان (المنقضية / المتبقية).
 * - قائمة بطاقات الأيام (LazyColumn): كل يوم بطاقة واحدة — رقم اليوم + اسم اليوم
 *   وتاريخه الميلادي وريشة «اليوم» للحالية، وخلايا أوقات الصلاة الثلاثية/الثنائية
 *   حسب `visibleIds` نفسها في شاشة اليوم. الأيام المنقضية باهتة.
 * - التنقل بين الأشهر بأزرار «الشهر السابق/التالي» — والأوقات محسوبة لكل يوم على حدة.
 */
@Composable
fun ArcCalendarScreen(
    state: AppState,
    onBack: () -> Unit
) {
    val palette = LocalKararPalette.current
    val settings = state.settings
    val today = PrayerCalculator.todayDate(settings)
    val todayHijri = remember(settings, today) { PrayerCalculator.hijriFor(settings, today) }

    var year by rememberSaveable { mutableIntStateOf(todayHijri.year) }
    var month by rememberSaveable { mutableIntStateOf(todayHijri.monthNum) }

    val days = remember(settings, year, month, today) {
        buildMonth(settings, year, month, today)
    }
    val passed = days.count { it.date < today }
    val remaining = days.size - passed
    val monthTitle = PrayerCalculator.hijriMonths[month - 1]
    val monthStart = days.firstOrNull()?.date
    val gregorianRange = monthStart?.let { start ->
        val end = days.lastOrNull()?.date ?: start
        "${start.dayOfMonth} ${PrayerCalculator.gregorianMonths[start.monthNumber - 1]} – " +
            "${end.dayOfMonth} ${PrayerCalculator.gregorianMonths[end.monthNumber - 1]} ${end.year} م"
    }.orEmpty()

    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 12.dp, top = statusTop + 8.dp)
        ) {
            // ---------- الرأس: رجوع + العنوان + رقما المنقضية/المتبقية ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(palette.surfaceHigh)
                        .border(1.dp, palette.border, CircleShape)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "تقويم شهر $monthTitle لعام $year هـ",
                        color = palette.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        gregorianRange,
                        color = palette.textMuted,
                        fontSize = 9.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MonthNavChip("الشهر السابق") {
                    if (month == 1) { month = 12; year -= 1 } else month -= 1
                }
                Spacer(Modifier.weight(1f))
                StatChip("المنقضية", passed)
                Spacer(Modifier.width(8.dp))
                StatChip("المتبقية", remaining, highlight = true)
                Spacer(Modifier.weight(1f))
                MonthNavChip("الشهر التالي") {
                    if (month == 12) { month = 1; year += 1 } else month += 1
                }
            }

            Spacer(Modifier.height(10.dp))

            // ---------- بطاقات الأيام ----------
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(days, key = { it.date.toString() }) { day ->
                    DayCard(
                        day = day,
                        isToday = day.date == today,
                        isPassed = day.date < today,
                        weekday = PrayerCalculator.weekDays.getOrNull(day.date.dayOfWeek.ordinal).orEmpty(),
                        visibleIds = PrayerCalculator.visibleIds(settings, day.date),
                        use24Hour = settings.use24Hour,
                        onSelect = null
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCard(
    day: ArcCalendarDay,
    isToday: Boolean,
    isPassed: Boolean,
    weekday: String,
    visibleIds: List<String>,
    use24Hour: Boolean,
    onSelect: (() -> Unit)?
) {
    val palette = LocalKararPalette.current
    val cardShape = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isPassed) 0.45f else 1f)
            .clip(cardShape)
            .background(
                if (isToday) {
                    Brush.verticalGradient(
                        listOf(palette.gold.copy(alpha = 0.22f), palette.gold.copy(alpha = 0.07f))
                    )
                } else {
                    Brush.verticalGradient(listOf(palette.surfaceSoft, palette.surfaceSoft))
                }
            )
            .border(
                if (isToday) 1.5.dp else 1.dp,
                if (isToday) palette.gold else palette.border,
                cardShape
            )
            .then(if (onSelect != null) Modifier.clickable(onClick = onSelect) else Modifier)
            .padding(12.dp)
    ) {
        // ---- رأس البطاقة: رقم اليوم + اليوم + التاريخ الميلادي ----
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (isToday) palette.gold else palette.surfaceHigh),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    day.hijri.day.toString(),
                    color = if (isToday) palette.onGold else palette.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                weekday,
                color = palette.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            if (isToday) {
                Spacer(Modifier.width(6.dp))
                Text(
                    "اليوم",
                    color = palette.gold,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "${day.date.dayOfMonth} - ${day.date.monthNumber} - ${day.date.year} م",
                color = palette.textMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                style = androidx.compose.ui.text.TextStyle(textDirection = TextDirection.Ltr)
            )
        }

        Spacer(Modifier.height(10.dp))

        // ---- خلايا الأوقات — ثلاثة أعمدة كما في المرجع ----
        val cells = visibleIds.chunked(3)
        cells.forEach { rowIds ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowIds.forEach { id ->
                    val raw = day.times[id] ?: 0.0
                    val formatted = PrayerCalculator.formatHour(raw, use24Hour)
                    PrayerCell(
                        id = id,
                        label = PrayerMeta.label(id),
                        time = formatted.first,
                        amPm = formatted.second,
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - rowIds.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun PrayerCell(
    id: String,
    label: String,
    time: String,
    amPm: String,
    modifier: Modifier = Modifier
) {
    val palette = LocalKararPalette.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(palette.highlight)
            .border(1.dp, palette.border, RoundedCornerShape(12.dp))
            .padding(vertical = 6.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = KararIcons.prayer(id),
                contentDescription = null,
                tint = palette.gold.copy(alpha = 0.85f),
                modifier = Modifier.size(9.dp)
            )
            Text(
                label,
                color = palette.textMuted,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                time,
                color = palette.textPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                style = androidx.compose.ui.text.TextStyle(textDirection = TextDirection.Ltr)
            )
            if (amPm.isNotBlank()) {
                Text(
                    " $amPm",
                    color = palette.textMuted,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun MonthNavChip(label: String, onClick: () -> Unit) {
    val palette = LocalKararPalette.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(palette.surfaceSoft)
            .border(1.dp, palette.border, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(label, color = palette.textSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatChip(label: String, value: Int, highlight: Boolean = false) {
    val palette = LocalKararPalette.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (highlight) palette.gold.copy(alpha = 0.16f) else palette.surfaceSoft
            )
            .border(1.dp, if (highlight) palette.gold.copy(alpha = 0.45f) else palette.border, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label, color = if (highlight) palette.gold else palette.textMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(
            value.toString(),
            color = if (highlight) palette.gold else palette.textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
        )
    }
}

private data class ArcCalendarDay(
    val date: LocalDate,
    val times: Map<String, Double>,
    val hijri: com.karrar.amsakiah.core.astronomy.HijriDate
)

/** أول أيام الشهر الهجري — تقدير ثم مسح نافذة صغيرة حوله. */
private fun buildMonth(
    settings: com.karrar.amsakiah.core.settings.AppSettings,
    year: Int,
    month: Int,
    today: LocalDate
): List<ArcCalendarDay> {
    val current = PrayerCalculator.hijriFor(settings, today)
    val estimate = (
        (year - current.year) * 354.367 +
            (month - current.monthNum) * 29.53
        ).roundToInt()
    var probe = today.plus(DatePeriod(days = estimate - 40))
    var start = today.plus(DatePeriod(days = -(current.day - 1)))
    repeat(90) {
        val h = PrayerCalculator.hijriFor(settings, probe)
        if (h.year == year && h.monthNum == month && h.day == 1) {
            start = probe
            return@repeat
        }
        probe = probe.plus(DatePeriod(days = 1))
    }

    return buildList {
        var date = start
        repeat(31) {
            val h = PrayerCalculator.hijriFor(settings, date)
            if (h.year != year || h.monthNum != month) return@buildList
            val times = PrayerCalculator.adjustedTimes(
                PrayerCalculator.timesFor(settings, date),
                settings
            )
            add(ArcCalendarDay(date, times, h))
            date = date.plus(DatePeriod(days = 1))
        }
    }
}
