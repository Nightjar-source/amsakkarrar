package com.karrar.amsakiah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.ui.state.AppState
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.core.state.PrayerMeta
import com.karrar.amsakiah.ui.theme.KararIcons
import com.karrar.amsakiah.ui.theme.LocalKararPalette
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.math.roundToInt

/**
 * أوقات الصلاة شهر — **شاشة مستقلة** (مطابقة لـ`amsak3d.tsx`: `arcShowCalendarModal`).
 * تُفتح من أيقونة التقويم في شاشة «اليوم» وتغطي الشاشة كاملة (لا نافذة ولا شبكة):
 *
 * - الرأس: زر رجوع + «أوقات الصلاة شهر X لعام Y هـ» + التاريخ الموحد تحت العنوان
 *   (نفس مصدر «اليوم»: `todayDate` + `zoneOf` ثم `hijriFor` بالإزاحة نفسها — بلا أي مصدر آخر).
 * - قائمة بطاقات الأيام (LazyColumn): كل يوم بطاقة واحدة — رقم اليوم + اسم اليوم
 *   وتاريخه الميلادي وريشة «اليوم» للحالية، وخلايا أوقات الصلاة الثلاثية/الثنائية
 *   حسب `visibleIds` نفسها في شاشة اليوم. الأيام المنقضية باهتة.
 * - التنقل بين الأشهر عائم فوق الخلفية بأسفل الشاشة («الشهر السابق» · «المنقضية N» ·
 *   «المتبقية M» · «الشهر التالي») بلا حاوية صلبة خلفه — ويرتفع فوق شريط التنقل
 *   السفلي متى ظهر الشريط، ويحل في منطقة إينست النظام عندما يكون مخفيًا.
 * - تمرير أولي: عند الفتح تقفز القائمة لبطاقة اليوم الحالي قبل أي رسمة
 *   (`LaunchedEffect(days)` مرة واحدة لكل (السنة، الشهر) — `scrollToItem` بلا حركة).
 */
@Composable
fun ArcCalendarScreen(
    state: AppState,
    onBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    val palette = LocalKararPalette.current
    val settings = state.settings
    // نفس مصدر «اليوم» حرفيًا: التاريخ يُشتق من منطقة المدينة (zoneOf) لا من توقيت النظام
    val today = PrayerCalculator.todayDate(settings)
    // الـhijri مشتق من settings.hijriOffset نفسه — أي تغيير في الإزاحة ينعكس فورًا
    val todayHijri = remember(settings, today) { PrayerCalculator.hijriFor(settings, today) }

    var year by rememberSaveable { mutableIntStateOf(todayHijri.year) }
    var month by rememberSaveable { mutableIntStateOf(todayHijri.monthNum) }

    val days = remember(settings, year, month, today) {
        buildMonth(settings, year, month, today)
    }
    val passed = days.count { it.date < today }
    val remaining = days.size - passed
    val monthTitle = PrayerCalculator.hijriMonths[month - 1]

    // سطر التاريخ الموحد تحت العنوان — اشتقاق واحد من PrayerCalculator (اليوم + zoneOf)
    val weekdayName = PrayerCalculator.weekDays[today.dayOfWeek.ordinal]
    val todayLine = "$weekdayName، ${today.dayOfMonth} " +
        "${PrayerCalculator.gregorianMonths[today.monthNumber - 1]} ${today.year} م · " +
        "${todayHijri.day} ${todayHijri.monthName} ${todayHijri.year} هـ"

    val todayIndex = days.indexOfFirst { it.date == today }
    // تمرير أولي قبل أي رسمة — فهارس القائمة محسوبة من نفس نسخة `days`
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = if (todayIndex >= 0) (todayIndex - 1).coerceAtLeast(0) else 0
    )
    LaunchedEffect(days) {
        if (days.isEmpty()) return@LaunchedEffect
        val idx = days.indexOfFirst { it.date == today }
        // مرة واحدة لكل (السنة، الشهر) — بلا أي حركة عند الفتح
        listState.scrollToItem(if (idx >= 0) (idx - 1).coerceAtLeast(0) else 0)
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // خلفية كاملة الحافة — لا فجوات لونية تحت الشل أو تحت شريط التنقل السفلي
                drawRect(Brush.verticalGradient(listOf(palette.backgroundTop, palette.backgroundBottom)))
                val radius = size.maxDimension * 0.62f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(palette.tealGlow, Color.Transparent),
                        center = Offset(size.width * 0.12f, size.height * 0.10f),
                        radius = radius
                    ),
                    radius = radius,
                    center = Offset(size.width * 0.12f, size.height * 0.10f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(palette.indigoGlow, Color.Transparent),
                        center = Offset(size.width * 0.88f, size.height * 0.82f),
                        radius = radius
                    ),
                    radius = radius,
                    center = Offset(size.width * 0.88f, size.height * 0.82f)
                )
            }
    ) {
        val layoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
        val topInset = contentPadding.calculateTopPadding()
        val bottomInset = contentPadding.calculateBottomPadding()
        val startInset = contentPadding.calculateStartPadding(layoutDirection)
        val endInset = contentPadding.calculateEndPadding(layoutDirection)

        // ---------- بطاقات الأيام: تمتد كامل الشاشة وتمر تحت الشريط العائم ----------
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 14.dp + startInset,
                end = 14.dp + endInset,
                top = topInset + 72.dp,
                bottom = bottomInset + 24.dp
            ),
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

        // ---------- الرأس الموحد العائم: رجوع + العنوان وسنة الهجرة + أزرار التنقل السريعة ----------
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(
                    start = 12.dp + startInset,
                    end = 12.dp + endInset,
                    top = topInset + 6.dp
                )
                .clip(RoundedCornerShape(26.dp))
                .background(palette.surfaceHigh.copy(alpha = 0.94f))
                .border(1.dp, palette.border, RoundedCornerShape(26.dp))
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(palette.surface)
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
                    text = "أوقات الصلاة شهر $monthTitle لعام $year هـ",
                    color = palette.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = todayLine,
                    color = palette.textMuted,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // سهم الشهر السابق
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(palette.surface)
                    .border(1.dp, palette.border, CircleShape)
                    .clickable {
                        if (month == 1) { month = 12; year -= 1 } else month -= 1
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("›", color = palette.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(6.dp))
            // سهم الشهر التالي
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(palette.surface)
                    .border(1.dp, palette.border, CircleShape)
                    .clickable {
                        if (month == 12) { month = 1; year += 1 } else month += 1
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("‹", color = palette.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
private fun MonthNavChip(label: String, next: Boolean, onClick: () -> Unit) {
    val palette = LocalKararPalette.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(palette.surfaceSoft, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            // chevron موجه: السابق = لليمين (RTL)، التالي = لليسار
            text = if (next) "‹" else "›",
            color = palette.gold,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Text(label, color = palette.textPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatChip(label: String, value: Int, highlight: Boolean = false) {
    val palette = LocalKararPalette.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (highlight) Color(0xFF2A2008) else palette.surfaceSoft,
                RoundedCornerShape(16.dp)
            )
            .border(
                1.dp,
                if (highlight) palette.gold.copy(alpha = 0.55f) else palette.border,
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            label,
            color = if (highlight) palette.gold else palette.textMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            value.toString(),
            color = if (highlight) palette.gold else palette.textPrimary,
            fontSize = 13.sp,
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
