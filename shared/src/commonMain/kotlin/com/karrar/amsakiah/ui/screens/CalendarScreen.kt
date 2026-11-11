package com.karrar.amsakiah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

    // ---- الشكل الثاني: تقويم السنوات ----
    // يُفتح بالضغط على قيمة السنة، ويعرض اثنتي عشرة سنة في شبكة، ويقفز
    // مباشرة إلى الشهر الحالي من السنة المختارة. مسار مستقل تمامًا عن
    // تقويم الشهر (الشكل الأول) الذي يأتي من شاشة القوس.
    var yearsMode by rememberSaveable { mutableStateOf(false) }
    var yearsRangeStart by rememberSaveable {
        mutableIntStateOf(((todayHijri.year - 5) / 12) * 12 + 1)
    }
    // الشكل الثالث للعرض: الجدول الكامل لشهر كامل (نموذج المرجع `calendar_hub`)
    var calendarView by rememberSaveable { mutableStateOf("grid") }

    if (yearsMode) {
        YearPicker(
            contentPadding = contentPadding,
            hijri = mode == CalendarMode.HIJRI,
            selectedYear = if (mode == CalendarMode.HIJRI) hijriYear else gregYear,
            rangeStart = yearsRangeStart,
            onRangeShift = { yearsRangeStart += it },
            onPick = { year ->
                if (mode == CalendarMode.HIJRI) hijriYear = year else gregYear = year
                yearsMode = false
            },
            onClose = { yearsMode = false }
        )
        return
    }

    @Composable
    fun HeaderCard() {
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
                            .background(KararColors.Teal.copy(alpha = 0.12f), RoundedCornerShape(50))
                            .clickable { resetToToday() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "اليوم",
                            color = KararColors.Teal,
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

                // ---- تبديل شكل العرض: بطاقات / جدول المرجع ----
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ModePill(label = "بطاقات", selected = calendarView == "grid", onClick = { calendarView = "grid" })
                    ModePill(label = "جدول", selected = calendarView == "table", onClick = { calendarView = "table" })
                }

                Spacer(Modifier.height(10.dp))

                // ---- تغيير السنة والشهر كلًّا على حدة ----
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CalendarStepper(
                        label = "السنة",
                        value = if (mode == CalendarMode.HIJRI) "$hijriYear" else "$gregYear",
                        modifier = Modifier.weight(1f),
                        onMinus = { shiftYear(-1) },
                        onPlus = { shiftYear(1) },
                        onValueClick = {
                            yearsRangeStart =
                                (((if (mode == CalendarMode.HIJRI) hijriYear else gregYear) - 5) / 12) * 12 + 1
                            yearsMode = true
                        }
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

    // شبكة تكيّفية: عمود واحد على الهاتف، عمودان على التابلت، وثلاثة على
    // الحاسوب/الشاشات العريضة — دون تغيير تسلسل الأيام (RTL يتبع LayoutDirection).
    if (calendarView == "grid") {
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
            item(span = { GridItemSpan(maxLineSpan) }) { HeaderCard() }

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
    } else {
        // ---- الجدول (calendar_hub) — عرض 30 يومًا في جدول كامل ----
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
            item(key = "header") { HeaderCard() }
            item(key = "table") {
                MonthTableCard(
                    monthTitle = monthTitle,
                    hijriSub = monthSubtitle,
                    cityName = state.settings.cityName,
                    days = days,
                    mode = mode,
                    today = today,
                    visible = PrayerCalculator.visibleIds(settings, today)
                )
            }
        }
    }
}

// =====================================================================
// عناصر الواجهة
// =====================================================================

private enum class CalendarMode { HIJRI, GREGORIAN }

/**
 * الشكل الثاني للتقويم: شبكة السنوات.
 *
 * لماذا شكل مستقل؟ المطلوب أن يكون للتقويم مسارَان واضحان:
 *  - من شاشة القوس: بطاقة الشهر الحالي القصيرة (`ArcCalendarSheet`).
 *  - من شريط التنقل: تقويم الشهر الكامل، ومنه شبكة السنوات هذه للقفز السريع.
 *
 * الشبكة تعرض 12 سنة (هجرية أو ميلادية حسب الوضع) بأزرار كبيرة، مع سهمَي
 * تنقّل لنطاق الاثنتي عشرة سنة التالي/السابق، وزر رجوع للتقويم الشهري.
 */
@Composable
private fun YearPicker(
    contentPadding: PaddingValues,
    hijri: Boolean,
    selectedYear: Int,
    rangeStart: Int,
    onRangeShift: (Int) -> Unit,
    onPick: (Int) -> Unit,
    onClose: () -> Unit
) {
    val span = 12
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 78.dp),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MonthArrow(Icons.AutoMirrored.Filled.KeyboardArrowRight) { onRangeShift(-span) }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (hijri) "السنوات الهجرية" else "السنوات الميلادية",
                            color = KararColors.TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "$rangeStart — ${rangeStart + span - 1}",
                            color = KararColors.TextMuted,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    MonthArrow(Icons.AutoMirrored.Filled.KeyboardArrowLeft) { onRangeShift(span) }
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "اختر سنة للانتقال إلى تقويمها الشهري",
                        color = KararColors.TextMuted,
                        fontSize = 10.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .background(KararColors.CardSoft, RoundedCornerShape(50))
                            .border(1.dp, KararColors.Border, RoundedCornerShape(50))
                            .clickable(onClick = onClose)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "رجوع",
                            color = KararColors.Teal,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        items(count = span, key = { index -> rangeStart + index }) { index ->
            val year = rangeStart + index
            val isSelected = year == selectedYear
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.4f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isSelected) KararColors.Gold else KararColors.CardSoft)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) KararColors.Gold else KararColors.Border,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable { onPick(year) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$year",
                    color = if (isSelected) KararColors.OnGold else KararColors.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ModePill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(
                if (selected) KararColors.Teal else KararColors.CardSoft,
                RoundedCornerShape(50)
            )
            .border(
                1.dp,
                if (selected) Color.Transparent else KararColors.Border,
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
    onPlus: () -> Unit,
    onValueClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .background(KararColors.CardSoft, RoundedCornerShape(14.dp))
            .border(1.dp, KararColors.Border, RoundedCornerShape(14.dp))
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
                modifier = Modifier
                    .weight(1f)
                    .then(
                        // الضغط على السنة يفتح تقويم السنوات (الشكل الثاني) —
                        // بلا زر إضافي يزاحم الصف على الشاشات الضيقة.
                        if (onValueClick != null) Modifier.clickable(onClick = onValueClick) else Modifier
                    )
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
            .background(KararColors.CardSoft, CircleShape)
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
            .background(KararColors.CardSoft, CircleShape)
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
                    SolidColor(KararColors.CardSoft)
                },
                shape = shape
            )
            .border(
                1.dp,
                if (highlighted) Color(0x66FFD700) else KararColors.Border,
                shape
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            label,
            color = if (highlighted) KararColors.Gold else KararColors.TextMuted,
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
                    Brush.linearGradient(
                        listOf(
                            KararColors.Gold.copy(alpha = 0.14f),
                            KararColors.CardSoft
                        )
                    )
                } else {
                    SolidColor(KararColors.CardSoft)
                },
                shape = shape
            )
            .border(
                1.dp,
                if (isToday) Color(0x99FFD700) else KararColors.Border,
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
                            if (isToday) KararColors.Gold else KararColors.CardSoft,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (if (mode == CalendarMode.HIJRI) day.hijriDay else day.date.dayOfMonth).toString(),
                        color = if (isToday) KararColors.OnGold else KararColors.TextPrimary,
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
                    color = KararColors.TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    style = TextStyle(textDirection = TextDirection.Ltr)
                )
                Text(
                    text = PrayerCalculator.gregorianMonths[day.date.monthNumber - 1],
                    color = KararColors.TextMuted,
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
                .background(KararColors.Border)
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
            .background(KararColors.CardSoft)
            .border(1.dp, KararColors.Border, RoundedCornerShape(12.dp))
            .padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        // أيقونة مائية كبيرة أسفل الخلية (10% كما في المرجع).
        Icon(
            imageVector = KararIcons.prayer(id),
            contentDescription = null,
            tint = KararColors.Gold.copy(alpha = 0.10f),
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
                    color = KararColors.TextSecondary,
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
                    color = KararColors.TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    style = TextStyle(textDirection = TextDirection.Ltr)
                )
                Text(
                    text = shortAmPm,
                    color = KararColors.TextMuted,
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
    "isfar" -> "إسفار"
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

/**
 * الجدول الكامل (نموذج المرجع `calendar_hub`):
 * جدول أفقي قابل للتمرير، بعرض كل يوم في صف واحد — التاريخان معًا،
 * ثم الأوقات المختارة كأعمدة حسب إعدادات المستخدم.
 */
@Composable
private fun MonthTableCard(
    monthTitle: String,
    hijriSub: String,
    cityName: String,
    days: List<CalendarDay>,
    mode: CalendarMode,
    today: LocalDate,
    visible: List<String>
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), corner = 24.dp) {
        // شريط الهيدر السماوي: عنوان الشهر + المدينة — كما في عنصر المرجع.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(KararColors.Teal.copy(alpha = 0.12f))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = "$monthTitle — $cityName",
                color = KararColors.Teal,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(10.dp))

        Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
            Column {
                // رأس الجدول
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KararColors.SurfaceHigh)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableHeadCell("التاريخ الهجري", modifier = Modifier.size(width = 110.dp, height = 40.dp))
                    TableHeadCell("الميلادي", modifier = Modifier.size(width = 100.dp, height = 40.dp))
                    visible.forEach { id ->
                        TableHeadCell(
                            shortLabel(id),
                            modifier = Modifier.size(width = 74.dp, height = 40.dp)
                        )
                    }
                }
                TableDivider()

                days.forEach { day ->
                    val isToday = day.date == today
                    Row(
                        modifier = Modifier.background(
                            if (isToday) KararColors.Teal.copy(alpha = 0.12f) else Color.Transparent
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // التاريخ الهجري + اسم اليوم من أسبوع
                        Column(Modifier.size(width = 110.dp, height = 52.dp).padding(horizontal = 6.dp)) {
                            Text(
                                text = "${day.hijriDay} ${day.hijri.monthName}",
                                color = if (isToday) KararColors.Teal else KararColors.TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = PrayerCalculator.weekDays[day.date.dayOfWeek.ordinal],
                                color = KararColors.TextMuted,
                                fontSize = 9.sp
                            )
                        }
                        // التاريخ الميلادي
                        Text(
                            text = "${day.date.dayOfMonth.toString().padStart(2, '0')}-${day.date.monthNumber.toString().padStart(2, '0')}-${day.date.year}",
                            color = KararColors.TextMuted,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.size(width = 100.dp, height = 52.dp).padding(horizontal = 4.dp)
                        )
                        visible.forEach { id ->
                            val raw = day.times[id] ?: 0.0
                            val (time, amPm, _) = PrayerCalculator.formatHour(raw)
                            val isMag = id == "maghrib"
                            Text(
                                text = "$time ${if (amPm.startsWith("م")) "م" else "ص"}",
                                color = if (isMag) KararColors.Teal else KararColors.TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isMag) FontWeight.Black else FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.size(width = 74.dp, height = 52.dp).padding(horizontal = 4.dp)
                            )
                        }
                    }
                    TableDivider()
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = hijriSub,
            color = KararColors.TextMuted,
            fontSize = 9.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TableHeadCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = KararColors.Teal,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(horizontal = 4.dp),
        maxLines = 2,
        softWrap = true
    )
}

@Composable
private fun TableDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(KararColors.Border)
    )
}
