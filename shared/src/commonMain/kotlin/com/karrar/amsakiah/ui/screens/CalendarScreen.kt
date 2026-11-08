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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.astronomy.HijriDate
import com.karrar.amsakiah.core.settings.AppSettings
import com.karrar.amsakiah.core.state.AppState
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararIcons
import com.karrar.amsakiah.ui.theme.KararTokens
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.math.roundToInt

/**
 * شاشة «التقويم» — M3 Expressive.
 *
 * تحلّ محل «الإمساكية» القديمة، وتتيح:
 * - تبديل التقويم الهجري/الميلادي.
 * - تغيير السنة والشهر لكل تقويم على حدة (أزرار مستقلة).
 * - التنقل بين الأشهر ذهابًا وإيابًا والعودة للشهر الحالي.
 * - عرض أوقات الصلاة اليومية وفق إعدادات الظهور (الغروب شيعي، الإمساك حسب وضعه).
 */
@Composable
fun CalendarScreen(state: AppState, contentPadding: PaddingValues) {
    val settings = state.settings
    val today = PrayerCalculator.todayDate(settings)
    val todayHijri = remember(settings, today) { PrayerCalculator.hijriFor(settings, today) }

    var mode by rememberSaveable { mutableStateOf(CalendarMode.HIJRI) }
    var hijriYear by rememberSaveable { mutableIntStateOf(todayHijri.year) }
    var hijriMonth by rememberSaveable { mutableIntStateOf(todayHijri.monthNum) }
    var gregYear by rememberSaveable { mutableIntStateOf(today.year) }
    var gregMonth by rememberSaveable { mutableIntStateOf(today.monthNumber) }

    fun shiftMonth(delta: Int) {
        if (mode == CalendarMode.HIJRI) {
            var m = hijriMonth + delta
            var y = hijriYear
            if (m > 12) {
                m = 1; y += 1
            } else if (m < 1) {
                m = 12; y -= 1
            }
            hijriMonth = m
            hijriYear = y
        } else {
            var m = gregMonth + delta
            var y = gregYear
            if (m > 12) {
                m = 1; y += 1
            } else if (m < 1) {
                m = 12; y -= 1
            }
            gregMonth = m
            gregYear = y
        }
    }

    fun shiftYear(delta: Int) {
        if (mode == CalendarMode.HIJRI) hijriYear += delta else gregYear += delta
    }

    fun shiftMonthByPicker(delta: Int) {
        if (mode == CalendarMode.HIJRI) {
            hijriMonth = ((hijriMonth - 1 + delta + 12) % 12) + 1
        } else {
            gregMonth = ((gregMonth - 1 + delta + 12) % 12) + 1
        }
    }

    fun resetToToday() {
        hijriYear = todayHijri.year
        hijriMonth = todayHijri.monthNum
        gregYear = today.year
        gregMonth = today.monthNumber
    }

    // أول يوم في الشهر المختار (هجري أو ميلادي)
    val monthStart: LocalDate = remember(
        mode, settings.hijriOffset, settings.methodCode, hijriYear, hijriMonth,
        gregYear, gregMonth, today
    ) {
        when (mode) {
            CalendarMode.HIJRI -> hijriMonthStart(settings, hijriYear, hijriMonth, today)
            CalendarMode.GREGORIAN -> LocalDate(gregYear, gregMonth, 1)
        }
    }

    val firstHijri = remember(settings, monthStart) { PrayerCalculator.hijriFor(settings, monthStart) }

    val days = remember(mode, monthStart, settings, hijriYear, hijriMonth, gregYear, gregMonth) {
        buildList {
            var date = monthStart
            repeat(31) {
                if (mode == CalendarMode.HIJRI) {
                    val h = PrayerCalculator.hijriFor(settings, date)
                    if (h.year != hijriYear || h.monthNum != hijriMonth) return@buildList
                } else {
                    if (date.year != gregYear || date.monthNumber != gregMonth) return@buildList
                }
                val times = PrayerCalculator.adjustedTimes(PrayerCalculator.timesFor(settings, date), settings)
                val h = PrayerCalculator.hijriFor(settings, date)
                add(CalendarDay(h.day, date, times, h))
                date = date.plus(DatePeriod(days = 1))
            }
        }
    }

    val monthTitle = when (mode) {
        CalendarMode.HIJRI -> "${PrayerCalculator.hijriMonths[hijriMonth - 1]} $hijriYear هـ"
        CalendarMode.GREGORIAN -> "${PrayerCalculator.gregorianMonths[gregMonth - 1]} $gregYear م"
    }
    val monthSubtitle = when (mode) {
        CalendarMode.HIJRI ->
            "${PrayerCalculator.gregorianMonths[monthStart.monthNumber - 1]} ${monthStart.year} م — ${days.size} يومًا"
        CalendarMode.GREGORIAN ->
            "${firstHijri.monthName} ${firstHijri.year} هـ — ${days.size} يومًا"
    }

    // شريحتا «المنقضية/المتبقية» — تُحسبان من الأيام المعروضة مقارنةً باليوم الحالي،
    // فتعملان في التقويمين وفي أي شهر معروض لا في الشهر الجاري فقط.
    val passedCount = remember(days, today) { days.count { it.date < today } }
    val remainingCount = days.size - passedCount

    // شبكة تكيّفية: عمود واحد على الهاتف، عمودان على التابلت، وثلاثة على
    // الحاسوب/الشاشات العريضة — دون تغيير تسلسل الأيام (RTL يتبع LayoutDirection).
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = KararTokens.Size.calendarCardMinWidth),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            GlassCard(modifier = Modifier.fillMaxWidth(), corner = 24.dp) {
                // ---- تبديل التقويم ----
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ModePill(
                        label = "هجري",
                        selected = mode == CalendarMode.HIJRI,
                        onClick = {
                            if (mode != CalendarMode.HIJRI) {
                                // مزامنة العرض الهجري مع الشهر الميلادي المعروض حاليًا
                                val h = PrayerCalculator.hijriFor(settings, monthStart)
                                hijriYear = h.year
                                hijriMonth = h.monthNum
                                mode = CalendarMode.HIJRI
                            }
                        }
                    )
                    Spacer(Modifier.width(6.dp))
                    ModePill(
                        label = "ميلادي",
                        selected = mode == CalendarMode.GREGORIAN,
                        onClick = {
                            if (mode != CalendarMode.GREGORIAN) {
                                // مزامنة العرض الميلادي مع الشهر الهجري المعروض حاليًا
                                gregYear = monthStart.year
                                gregMonth = monthStart.monthNumber
                                mode = CalendarMode.GREGORIAN
                            }
                        }
                    )
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .background(Color(0x1A2DD4BF), RoundedCornerShape(50))
                            .clickable { resetToToday() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "اليوم",
                            color = KararTokens.Accent.teal,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ---- عنوان الشهر مع سهمي التنقل ----
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MonthArrow(Icons.AutoMirrored.Filled.KeyboardArrowRight) { shiftMonth(-1) }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            monthTitle,
                            color = KararColors.TextPrimary,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            monthSubtitle,
                            color = KararColors.TextMuted,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    MonthArrow(Icons.AutoMirrored.Filled.KeyboardArrowLeft) { shiftMonth(1) }
                }

                Spacer(Modifier.height(10.dp))

                // ---- شريحتا المنقضية / المتبقية ----
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CalendarStatChip(
                        label = "المنقضية:",
                        value = passedCount,
                        highlighted = false
                    )
                    CalendarStatChip(
                        label = "المتبقية:",
                        value = remainingCount,
                        highlighted = true
                    )
                }

                Spacer(Modifier.height(10.dp))

                // ---- تغيير السنة والشهر كلًّا على حدة ----
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CalendarStepper(
                        label = "السنة",
                        value = if (mode == CalendarMode.HIJRI) "$hijriYear" else "$gregYear",
                        modifier = Modifier.weight(1f),
                        onMinus = { shiftYear(-1) },
                        onPlus = { shiftYear(1) }
                    )
                    CalendarStepper(
                        label = "الشهر",
                        value = if (mode == CalendarMode.HIJRI) {
                            PrayerCalculator.hijriMonths[hijriMonth - 1]
                        } else {
                            PrayerCalculator.gregorianMonths[gregMonth - 1]
                        },
                        modifier = Modifier.weight(1f),
                        onMinus = { shiftMonthByPicker(-1) },
                        onPlus = { shiftMonthByPicker(1) }
                    )
                }
            }
        }

        // ---- بطاقات الأيام ----
        items(
            count = days.size,
            key = { index -> days[index].date.toString() }
        ) { index ->
            val day = days[index]
            DayCard(
                day = day,
                mode = mode,
                isToday = day.date == today,
                isPassed = day.date < today,
                visibleIds = PrayerCalculator.visibleIds(settings, day.date)
            )
        }
    }
}

// =====================================================================
// عناصر الواجهة
// =====================================================================

private enum class CalendarMode { HIJRI, GREGORIAN }

@Composable
private fun ModePill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (selected) KararTokens.Accent.teal else Color(0x0FFFFFFF),
                RoundedCornerShape(50)
            )
            .border(
                1.dp,
                if (selected) Color.Transparent else Color(0x1FFFFFFF),
                RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            label,
            color = if (selected) Color(0xFF06281F) else KararColors.TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CalendarStepper(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onMinus: () -> Unit,
    onPlus: () -> Unit
) {
    Column(
        modifier = modifier
            .background(Color(0x0AFFFFFF), RoundedCornerShape(14.dp))
            .border(1.dp, Color(0x12FFFFFF), RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 7.dp)
    ) {
        Text(label, color = KararColors.TextMuted, fontSize = 9.sp)
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepChip("−", onMinus)
            Text(
                value,
                color = KararColors.TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            StepChip("+", onPlus)
        }
    }
}

@Composable
private fun StepChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .background(Color(0x1AFFFFFF), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = KararColors.Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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

@Composable
private fun CalendarStatChip(label: String, value: Int, highlighted: Boolean) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(
                brush = if (highlighted) {
                    Brush.horizontalGradient(
                        listOf(Color(0x33FFD700), Color(0x1AFF9800))
                    )
                } else {
                    SolidColor(Color(0x1AFFFFFF))
                },
                shape = shape
            )
            .border(
                1.dp,
                if (highlighted) Color(0x66FFD700) else Color(0x33FFFFFF),
                shape
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            label,
            color = if (highlighted) Color(0xE6FFE9A8) else Color.White.copy(alpha = 0.70f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            value.toString(),
            color = if (highlighted) KararColors.Gold else KararColors.TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            style = TextStyle(textDirection = TextDirection.Ltr)
        )
    }
}

@Composable
private fun DayCard(
    day: CalendarDay,
    mode: CalendarMode,
    isToday: Boolean,
    isPassed: Boolean,
    visibleIds: List<String>
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // الأيام المنقضية باهتة كما في المرجع (`opacity-50 grayscale`).
            .alpha(if (isPassed) 0.5f else 1f)
            .graphicsLayer {
                if (isToday) {
                    scaleX = 1.02f
                    scaleY = 1.02f
                }
            }
            .clip(shape)
            .background(
                brush = if (isToday) {
                    Brush.linearGradient(listOf(Color(0x26FFFFFF), Color(0x0DFFFFFF)))
                } else {
                    SolidColor(KararColors.CardSoft)
                },
                shape = shape
            )
            .border(
                1.dp,
                if (isToday) Color(0x99FFD700) else Color(0x0DFFFFFF),
                shape
            )
            .padding(16.dp)
    ) {
        // ---- رأس البطاقة: التقويمان معًا (هجري + ميلادي) ----
        // الرقم في الدائرة يتبع وضع العرض المختار، أما السطران فيُظهران
        // التاريخ بالتقويمين دائمًا حتى لا يفقد المستخدم مرجعًا زمنيًا.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    Modifier
                        .size(32.dp)
                        .background(
                            if (isToday) KararColors.Gold else Color(0x1AFFFFFF),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (if (mode == CalendarMode.HIJRI) day.hijriDay else day.date.dayOfMonth).toString(),
                        color = if (isToday) Color(0xFF241A00) else KararColors.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(textDirection = TextDirection.Ltr)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = PrayerCalculator.weekDays[day.date.dayOfWeek.ordinal],
                        color = KararColors.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${day.hijri.day} ${day.hijri.monthName} ${day.hijri.year} هـ",
                        color = KararColors.Gold.copy(alpha = if (isToday) 0.95f else 0.75f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${day.date.dayOfMonth} - ${day.date.monthNumber} - ${day.date.year} م",
                    color = Color.White.copy(alpha = 0.70f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    style = TextStyle(textDirection = TextDirection.Ltr)
                )
                Text(
                    text = PrayerCalculator.gregorianMonths[day.date.monthNumber - 1],
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 9.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0x1AFFFFFF))
        )
        Spacer(Modifier.height(12.dp))

        // ---- شبكة أوقات الصلوات: ثلاثة أعمدة كما في المرجع ----
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            visibleIds.chunked(3).forEach { rowIds ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowIds.forEach { id ->
                        PrayerTimeCell(
                            id = id,
                            rawHour = day.times[id] ?: 0.0,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // إبقاء عرض الخلايا ثابتًا (الثلث) في الصف الأخير الناقص.
                    repeat(3 - rowIds.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun PrayerTimeCell(id: String, rawHour: Double, modifier: Modifier = Modifier) {
    val (time, amPm, _) = PrayerCalculator.formatHour(rawHour)
    val shortAmPm = if (amPm.startsWith("م")) "م" else "ص"
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x4D000000))
            .padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        // أيقونة مائية كبيرة أسفل الخلية (10% كما في المرجع).
        Icon(
            imageVector = KararIcons.prayer(id),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.10f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 8.dp, y = 8.dp)
                .size(30.dp)
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = KararIcons.prayer(id),
                    contentDescription = null,
                    tint = KararColors.Gold.copy(alpha = 0.60f),
                    modifier = Modifier.size(10.dp)
                )
                Text(
                    text = shortLabel(id),
                    color = Color.White.copy(alpha = 0.80f),
                    fontSize = 9.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
            Spacer(Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = time,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    style = TextStyle(textDirection = TextDirection.Ltr)
                )
                Text(
                    text = shortAmPm,
                    color = Color.White.copy(alpha = 0.50f),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 1.dp)
                )
            }
        }
    }
}

/**
 * يبحث عن أول يوم من الشهر الهجري المطلوب.
 * نقدّر الفرق بالأيام ثم نمسح نافذة صغيرة حول التقدير (الخطأ أقل من يومين عادةً).
 */
private fun hijriMonthStart(
    settings: AppSettings,
    targetYear: Int,
    targetMonth: Int,
    today: LocalDate
): LocalDate {
    val current = PrayerCalculator.hijriFor(settings, today)
    val estimate = (
        (targetYear - current.year) * 354.367 +
            (targetMonth - current.monthNum) * 29.53
        ).roundToInt()
    var probe = today.plus(DatePeriod(days = estimate - 40))
    repeat(90) {
        val h = PrayerCalculator.hijriFor(settings, probe)
        if (h.year == targetYear && h.monthNum == targetMonth && h.day == 1) return probe
        probe = probe.plus(DatePeriod(days = 1))
    }
    // احتياطي: أول الشهر الهجري الحالي
    return today.plus(DatePeriod(days = -(current.day - 1)))
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
    val hijri: HijriDate
)
