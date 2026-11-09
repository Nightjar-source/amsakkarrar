package com.karrar.amsakiah.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.state.AppState
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararTokens
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.math.roundToInt

/**
 * تقويم القوس — نافذة شهرية قصيرة تُفتح من شاشة «اليوم» (مرجع `amsak3d.tsx`:
 * `calendar`). تختلف عن تقويم السنوات في تبويب «التقويم»:
 *
 * - هنا: شبكة 7 أعمدة بأرقام هجرية + وقت الفجر والمغرب لكل يوم، واليوم الحالي
 *   ذهبي، وأسهم الشهر السابق/التالي. اختيار يوم يغلق النافذة.
 * - هناك: منتقي سنة/شهر، تفاصيل كل يوم، والجدول الميلادي.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArcCalendarSheet(
    state: AppState,
    onDismiss: () -> Unit,
    onSelectDate: (LocalDate) -> Unit = {},
    onOpenFullCalendar: (() -> Unit)? = null
) {
    val settings = state.settings
    val today = PrayerCalculator.todayDate(settings)
    val todayHijri = remember(settings, today) { PrayerCalculator.hijriFor(settings, today) }

    var year by remember { mutableIntStateOf(todayHijri.year) }
    var month by remember { mutableIntStateOf(todayHijri.monthNum) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val days = remember(settings, year, month, today) {
        buildMonth(settings, year, month, today)
    }
    val monthStart = days.firstOrNull()?.date
    val monthTitle = "${PrayerCalculator.hijriMonths[month - 1]} $year هـ"
    val gregorianRange = monthStart?.let { start ->
        val end = days.lastOrNull()?.date ?: start
        "${start.dayOfMonth} ${PrayerCalculator.gregorianMonths[start.monthNumber - 1]} – " +
            "${end.dayOfMonth} ${PrayerCalculator.gregorianMonths[end.monthNumber - 1]} ${end.year} م"
    }.orEmpty()

    // RTL مفروض داخل الورقة: الشبكة تبدأ من اليمين (الأحد) مهما كان اتجاه الشل.
    androidx.compose.runtime.CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = KararColors.Card,
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(KararColors.GoldSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("☾", color = KararColors.Gold, fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "تقويم شهر $monthTitle",
                            color = KararColors.TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            gregorianRange,
                            color = KararColors.TextMuted,
                            fontSize = 9.sp
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MonthStepChip("الشهر السابق") {
                        if (month == 1) {
                            month = 12
                            year -= 1
                        } else {
                            month -= 1
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${days.size} يومًا",
                        color = KararColors.TextMuted,
                        fontSize = 9.sp
                    )
                    Spacer(Modifier.weight(1f))
                    MonthStepChip("الشهر التالي") {
                        if (month == 12) {
                            month = 1
                            year += 1
                        } else {
                            month += 1
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(Modifier.fillMaxWidth()) {
                    WEEKDAYS.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            color = KararColors.TextMuted,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))

                // مقدار الإزاحة قبل أول يوم: الأسبوع يبدأ بالأحد (ISO Sunday = 7).
                val leading = monthStart?.let { it.dayOfWeek.ordinal.let { d -> (d + 1) % 7 } } ?: 0
                val cells = buildList<MonthCell> {
                    repeat(leading) { add(MonthCell.Empty) }
                    days.forEach { add(MonthCell.Day(it)) }
                    while (size % 7 != 0) add(MonthCell.Empty)
                }
                cells.chunked(7).forEach { week ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        week.forEach { cell ->
                            Box(Modifier.weight(1f)) {
                                when (cell) {
                                    MonthCell.Empty -> Spacer(Modifier.height(46.dp))
                                    is MonthCell.Day -> ArcDayCell(
                                        cell = cell,
                                        isToday = cell.day.date == today,
                                        onClick = {
                                            onSelectDate(cell.day.date)
                                            onDismiss()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                if (onOpenFullCalendar != null) {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .background(KararColors.GoldSoft)
                            .clickable { onOpenFullCalendar() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "التقويم الكامل — السنوات والأوقات",
                            color = KararColors.Gold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthStepChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(KararColors.CardSoft)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(label, color = KararColors.TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ArcDayCell(cell: MonthCell.Day, isToday: Boolean, onClick: () -> Unit) {
    val background by animateColorAsState(
        targetValue = if (isToday) KararColors.Gold.copy(alpha = 0.18f) else Color.Transparent,
        animationSpec = KararTokens.Motion.effects(),
        label = "arc-cal-bg"
    )
    val border by animateColorAsState(
        targetValue = if (isToday) KararColors.Gold else Color.Transparent,
        animationSpec = KararTokens.Motion.effects(),
        label = "arc-cal-border"
    )
    val scale by animateFloatAsState(
        targetValue = if (isToday) 1f else 0.98f,
        animationSpec = KararTokens.Motion.spatial(),
        label = "arc-cal-scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(background)
            .border(1.dp, border, RoundedCornerShape(11.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            cell.day.hijri.day.toString(),
            color = if (isToday) KararColors.Gold else KararColors.TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "ف ${PrayerCalculator.formatHour(cell.day.times["fajr"] ?: 0.0).first}",
            color = KararColors.TextMuted,
            fontSize = 6.sp,
            maxLines = 1
        )
        Text(
            "م ${PrayerCalculator.formatHour(cell.day.times["maghrib"] ?: 0.0).first}",
            color = KararColors.TextMuted,
            fontSize = 6.sp,
            maxLines = 1
        )
    }
}

private sealed interface MonthCell {
    data object Empty : MonthCell
    data class Day(val day: ArcCalendarDay) : MonthCell
}

private data class ArcCalendarDay(
    val date: LocalDate,
    val times: Map<String, Double>,
    val hijri: com.karrar.amsakiah.core.astronomy.HijriDate
)

private val WEEKDAYS = listOf("أحد", "اثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت")

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
