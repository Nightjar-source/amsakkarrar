package com.karrar.amsakiah.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.settings.AppSettings
import com.karrar.amsakiah.core.state.AppState
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.core.state.PrayerMeta
import com.karrar.amsakiah.core.weather.WeatherInfo
import com.karrar.amsakiah.ui.components.KashidaHeader
import com.karrar.amsakiah.ui.components.SkyArc
import com.karrar.amsakiah.ui.components.WeatherEffects
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararIcons
import com.karrar.amsakiah.ui.theme.KararTokens
import com.karrar.amsakiah.ui.theme.SkyTheme
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * شاشة «اليوم» — نسخة مطابقة لـ`amsak3d.tsx` (السطور 1638–2068):
 * هيدر (المدينة/الطقس يمينًا + الكشيدة والتاريخ يسارًا) ← القوس السماوي
 * ← العدّاد التنازلي (بالصلوات المختارة فقط) ← زر التقويم + شريط الصلوات الموسّط.
 *
 * لا يوجد شريط عنوان ولا زر إعدادات أسفل القوس؛ فتح الضبط من شريحة المدينة،
 * وفتح التقويم من الزر الوحيد تحت القوس.
 *
 * زر التقويم يفتح **تقويم القوس** (نافذة شهرية قصيرة) كما في `amsak3d.tsx`،
 * لا تبويب التقويم الكامل؛ ومن داخل النافذة يمكن الانتقال إلى تقويم السنوات.
 */
@Composable
fun TodayScreen(
    state: AppState,
    weather: WeatherInfo?,
    onOpenFullCalendar: () -> Unit,
    onOpenSettings: () -> Unit,
    contentPadding: PaddingValues
) {
    // نافذة تقويم القوس — تُفتح من زر التقويم تحت القوس.
    var showArcCalendar by rememberSaveable { mutableStateOf(false) }
    val settings = state.settings
    val zone = remember(settings.timeZoneId) { PrayerCalculator.zoneOf(settings) }
    val nowLdt: LocalDateTime = remember(state.now, zone) { state.now.toLocalDateTime(zone) }
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

    val isShia = remember(settings.methodCode) {
        PrayerCalculator.methodFor(settings.methodCode).isShia
    }
    val visibleIds = remember(settings, date) { PrayerCalculator.visibleIds(settings, date) }
    val countdownIds = remember(settings, date) { PrayerCalculator.liveIds(settings, date) }

    val phase = remember(todayTimes, tomorrowTimes, nowSeconds, isShia) {
        computeArcPhase(nowSeconds, todayTimes, tomorrowTimes, isShia, state.settings.use24Hour)
    }
    val isNight = remember(todayTimes, nowSeconds) {
        val sunrise = secondsOf(todayTimes["sunrise"] ?: 6.0)
        val maghrib = secondsOf(todayTimes["maghrib"] ?: 18.25)
        !(nowSeconds >= sunrise && nowSeconds < maghrib)
    }

    val (skyTop, skyBottom, skyGlow) = SkyTheme.getSkyColorsForSecond(nowSeconds.toFloat())
    val clock = PrayerCalculator.formatHour(nowLdt.hour + nowLdt.minute / 60.0, state.settings.use24Hour)

    // ---- العدّاد: الصلاة التالية تلقائيًا، مع إمكانية اختيار صلاة أخرى باللمس ----
    val autoIndex = countdownIds.indexOfFirst { id ->
        secondsOf(todayTimes[id] ?: 0.0) > nowSeconds
    }.let { if (it < 0) 0 else it }
    val autoTargetId = countdownIds.getOrNull(autoIndex)
    var manualIndex by remember(countdownIds) { mutableIntStateOf(-1) }
    LaunchedEffect(autoTargetId) { manualIndex = -1 }

    val activeIndex = if (manualIndex in countdownIds.indices) manualIndex else autoIndex
    val targetId = countdownIds.getOrNull(activeIndex)
    val targetTodaySec = targetId?.let { secondsOf(todayTimes[it] ?: 0.0) }
    val targetIsTomorrow = targetTodaySec != null && nowSeconds >= targetTodaySec
    val targetSeconds = if (targetId == null) 0 else {
        if (targetIsTomorrow) secondsOf(tomorrowTimes[targetId] ?: 0.0) else targetTodaySec ?: 0
    }
    val remainingSeconds = if (targetId == null) 0 else {
        if (targetIsTomorrow) {
            ((86400 - nowSeconds) + targetSeconds).coerceAtLeast(0)
        } else {
            (targetSeconds - nowSeconds).coerceAtLeast(0)
        }
    }
    val (hoursLeft, minutesLeft, secondsLeft) = PrayerCalculator.countdownText(remainingSeconds)
    val targetLabel = targetId?.let { PrayerMeta.label(it) }.orEmpty()
    val countdownLabel = if (targetId == null) "" else buildString {
        append("باقي على ").append(targetLabel)
        if (targetIsTomorrow) {
            append(" غداً ")
            append(PrayerCalculator.weekDays[tomorrowDate.dayOfWeek.ordinal])
            append(' ').append(tomorrowHijri.day)
            append(' ').append(tomorrowHijri.monthName)
        }
    }

    // هوامش آمنة أفقية (ثقوب الإطار/الأشرطة/الكابشن) — تُطبق على القوس فقط
    // حتى لا تتداخل أطرافه (الشروق/المغرب ونصوصها) مع الحافة الفعلية.
    val safeInsets = WindowInsets.safeDrawing.asPaddingValues()
    val layoutDirection = LocalLayoutDirection.current
    val arcSafeStart = safeInsets.calculateStartPadding(layoutDirection) + 8.dp
    val arcSafeEnd = safeInsets.calculateEndPadding(layoutDirection) + 8.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(Brush.verticalGradient(listOf(skyTop, skyBottom)))
                // النجوم — تظهر فقط ليلاً، مثل المرجع (stars + boxShadow).
                if (isNight) {
                    SkyTheme.celestialStars.forEach { star ->
                        drawCircle(
                            color = Color.White.copy(alpha = star.maxAlpha),
                            radius = star.radius * 1.1.dp.toPx(),
                            center = Offset(star.xPercent * size.width / 100f, star.yPercent * size.height / 100f)
                        )
                    }
                }
                // تدرّج علوي خفيف خلف شريط الحالة فقط — يبقى السماء ظاهرة،
                // والأيقونات البيضاء تُقرأ دائمًا في الوضعين بلا "كتلة لونية".
                drawRect(
                    Brush.verticalGradient(
                        listOf(Color(0x3D000000), Color.Transparent),
                        startY = 0f,
                        endY = 78.dp.toPx()
                    )
                )
                drawRect(
                    Brush.radialGradient(
                        colorStops = arrayOf(0f to skyGlow, 0.8f to Color.Transparent),
                        center = Offset(size.width / 2f, size.height * 0.35f),
                        radius = size.maxDimension * 0.75f
                    )
                )
            }
            .padding(contentPadding)
    ) {
        val compact = maxHeight < 620.dp
        // على الشاشات العريضة (تابلت/حاسوب/مطوية مفتوحة) نُبقي الخلفية السماوية
        // full-bleed لكن نُوسّط مسرح اليوم بعرض هاتف مريح حتى لا يتمدد القوس
        // والعدّاد أفقيًا بلا داعٍ. على الهاتف stageWidth = maxWidth كما هو.
        val stageWidth = minOf(maxWidth, KararTokens.Size.todayStageMaxWidth)

        // تأثيرات الطقس خلف كل المحتوى (z-index 1 في المرجع).
        WeatherEffects(
            code = weather?.code,
            isNight = isNight,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .width(stageWidth)
                .fillMaxHeight()
                // على الشاشات الصغيرة/المنقسمة: المحتوى يتمرّر بدل أن يُقصّ —
                // القوس يبقى كاملًا والأرقام لا تُفقد تحت الحواف.
                .verticalScroll(rememberScrollState())
        ) {
            TodayHeader(
                settings = settings,
                weather = weather,
                hijri = hijri,
                date = date,
                onOpenSettings = onOpenSettings
            )

            // ---- القوس ----
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val ratio = 356f / 298f
                val arcHeight = minOf(maxHeight, maxWidth / ratio)
                val arcWidth = arcHeight * ratio
                SkyArc(
                    progress = phase.progress,
                    isNight = isNight,
                    hijriDay = hijri.day,
                    timeText = clock.first,
                    meridiem = clock.second,
                    leftLabel = phase.leftLabel,
                    leftTime = PrayerCalculator.formatHour(phase.leftHour, state.settings.use24Hour).first,
                    rightLabel = phase.rightLabel,
                    rightTime = PrayerCalculator.formatHour(phase.rightHour, state.settings.use24Hour).first,
                    subPhaseBadge = phase.badge,
                    modifier = Modifier
                        .size(width = arcWidth, height = arcHeight)
                        // هوامش آمنة أفقية تمنع الطرف (الشروق/المغرب) من ملامسة الحافة الفعلية
                        .padding(start = arcSafeStart, end = arcSafeEnd)
                )
            }

            if (countdownIds.isEmpty() || targetId == null) {
                // سلوك V7: عند إلغاء كل الصلوات يبقى فراغ ثابت دون عدّاد.
                Spacer(Modifier.height(100.dp))
            } else {
                CountdownSection(
                    label = countdownLabel,
                    hours = hoursLeft,
                    minutes = minutesLeft,
                    seconds = secondsLeft,
                    dotsCount = countdownIds.size,
                    activeDot = activeIndex,
                    compact = compact,
                    onDotClick = { manualIndex = it },
                    onSwipeNext = {
                        manualIndex = (activeIndex + 1).mod(countdownIds.size)
                    },
                    onSwipePrevious = {
                        manualIndex = (activeIndex - 1 + countdownIds.size).mod(countdownIds.size)
                    }
                )
            }

            BottomArea(
                visibleIds = visibleIds,
                todayTimes = todayTimes,
                tomorrowTimes = tomorrowTimes,
                targetId = targetId,
                targetIsTomorrow = targetIsTomorrow,
                compact = compact,
                use24Hour = state.settings.use24Hour,
                onOpenCalendar = { showArcCalendar = true },
                onPrayerClick = { id ->
                    if (countdownIds.isNotEmpty()) {
                        val index = countdownIds.indexOf(id)
                        if (index >= 0) manualIndex = index
                    }
                }
            )
        }
    }

    // تقويم القوس (V7) — نافذة سفلية فوق شاشة اليوم مباشرة (SpringMotion).
    ArcCalendarSheet(
        visible = showArcCalendar,
        state = state,
        onDismiss = { showArcCalendar = false },
        onOpenFullCalendar = {
            showArcCalendar = false
            onOpenFullCalendar()
        }
    )
}

// =====================================================================
// الرأس: المدينة/الطقس + الكشيدة والتاريخ
// =====================================================================

@Composable
private fun TodayHeader(
    settings: AppSettings,
    weather: WeatherInfo?,
    hijri: com.karrar.amsakiah.core.astronomy.HijriDate,
    date: LocalDate,
    onOpenSettings: () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val tiny = maxWidth < 360.dp
        // الكشيدة عنوان تصميمي ثابت: لا يتضخم مع حجم خط المستخدم حتى يبقى في سطر واحد.
        val userFontScale = LocalDensity.current.fontScale.coerceAtLeast(0.1f)
        val kashidaSize = (maxWidth.value * 0.054f / userFontScale).coerceIn(18.4f, 45.6f).sp
        val dateSize = if (maxWidth < 420.dp) 8.5.sp else 10.5.sp
        val chipTextSize = when {
            maxWidth < 360.dp -> 9.5.sp
            maxWidth < 600.dp -> 11.sp
            else -> 13.sp
        }
        val horizontalPadding = if (tiny) 8.dp else 16.dp
        val showWeatherCondition = maxWidth >= 380.dp

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (maxWidth < 600.dp) 66.dp else 84.dp)
                .clipToBounds()
                .padding(start = horizontalPadding, end = horizontalPadding, top = if (maxWidth < 600.dp) 6.dp else 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // يمين الشاشة (أول عنصر في RTL): المدينة + الطقس
            Column(
                modifier = Modifier.weight(0.44f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.10f), CircleShape)
                        .clickable(onClick = onOpenSettings)
                        .padding(
                            horizontal = if (tiny) 8.dp else 12.dp,
                            vertical = if (tiny) 4.dp else 6.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = KararColors.Gold,
                        modifier = Modifier.size(if (tiny) 10.dp else 12.dp)
                    )
                    Text(
                        text = settings.cityName.substringBefore(" ("),
                        color = Color.White,
                        fontSize = chipTextSize,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (settings.showWeather) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape)
                            .padding(
                                horizontal = if (tiny) 8.dp else 10.dp,
                                vertical = if (tiny) 4.dp else 5.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (weather != null) {
                            val (icon, tint) = weatherIcon(weather.code)
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(if (tiny) 10.dp else 12.dp)
                            )
                            Text(
                                text = "${weather.temperatureC}°C",
                                color = Color.White,
                                fontSize = chipTextSize,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                style = androidx.compose.ui.text.TextStyle(textDirection = TextDirection.Ltr)
                            )
                            if (showWeatherCondition) {
                                Text(
                                    text = "• ${weatherConditionLabel(weather.code)}",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = if (tiny) 8.5.sp else 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Text(
                                text = seasonBadge(date, settings.latitude),
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = if (tiny) 8.5.sp else 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // يسار الشاشة (آخر عنصر في RTL): الكشيدة + التاريخ
            Column(
                modifier = Modifier.weight(0.56f),
                horizontalAlignment = Alignment.End
            ) {
                KashidaHeader(
                    hijriDay = hijri.day,
                    monthName = hijri.monthName,
                    fontSize = kashidaSize
                )
                Spacer(Modifier.height(if (tiny) 5.dp else 7.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = PrayerCalculator.formattedGregorian(date),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = dateSize,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = " (${hijri.year} هـ)",
                        color = Color(0xFFFFFDE7).copy(alpha = 0.65f),
                        fontSize = dateSize,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

private fun weatherIcon(code: Int): Pair<ImageVector, Color> = when {
    code <= 1 -> Icons.Filled.WbSunny to Color(0xFFFFD700)
    code <= 3 -> Icons.Filled.Cloud to Color(0xFFFDE68A)
    code <= 48 -> Icons.Filled.Cloud to Color(0xFFCBD5E1)
    code <= 67 || code in 80..82 -> Icons.Filled.WaterDrop to Color(0xFF7DD3FC)
    code <= 79 -> Icons.Filled.AcUnit to Color.White
    else -> Icons.Filled.Bolt to Color(0xFFFDE047)
}

private fun weatherConditionLabel(code: Int): String = when {
    code <= 0 -> "سماء صافية"
    code == 1 -> "صافي غالباً"
    code == 2 -> "غائم جزئياً"
    code == 3 -> "غائم كلياً"
    code in 45..48 -> "ضباب"
    code in 51..57 -> "رذاذ مطر"
    code in 61..67 -> "ممطر"
    code in 71..77 -> "ثلوج"
    code in 80..82 -> "زخات مطر"
    code >= 95 -> "عاصفة رعدية"
    else -> "معتدل"
}

private fun seasonBadge(date: LocalDate, latitude: Double): String {
    val north = latitude >= 0.0
    return when (date.monthNumber) {
        3, 4, 5 -> if (north) "فصل الربيع المعتدل" else "فصل الخريف"
        6, 7, 8 -> if (north) "فصل الصيف" else "فصل الشتاء"
        9, 10, 11 -> if (north) "فصل الخريف" else "فصل الربيع"
        else -> if (north) "فصل الشتاء" else "فصل الصيف"
    }
}

// =====================================================================
// العدّاد التنازلي
// =====================================================================

@Composable
private fun CountdownSection(
    label: String,
    hours: String,
    minutes: String,
    seconds: String,
    dotsCount: Int,
    activeDot: Int,
    compact: Boolean,
    onDotClick: (Int) -> Unit,
    onSwipeNext: () -> Unit,
    onSwipePrevious: () -> Unit
) {
    val currentActive by rememberUpdatedState(activeDot)
    val currentNext by rememberUpdatedState(onSwipeNext)
    val currentPrevious by rememberUpdatedState(onSwipePrevious)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            // ارتفاع ثابت: العدّاد لا يرفع القوس/يخفضه أبدًا مهما تغيّر المحتوى.
            .height(if (compact) 118.dp else 132.dp)
            .pointerInput(dotsCount) {
                if (dotsCount <= 1) return@pointerInput
                var accumulated = 0f
                detectHorizontalDragGestures(
                    onDragStart = { accumulated = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        accumulated += dragAmount
                        val threshold = 30.dp.toPx()
                        if (abs(accumulated) > threshold) {
                            if (accumulated < 0f) currentNext() else currentPrevious()
                            accumulated = 0f
                        }
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.10f), CircleShape)
                .padding(
                    horizontal = if (compact) 16.dp else 24.dp,
                    vertical = 4.dp
                )
        ) {
            Text(
                text = label,
                color = KararColors.Gold,
                fontSize = if (compact) 10.sp else 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp)
        ) {
            CountdownDigit(hours, compact)
            CountdownColon(compact)
            CountdownDigit(minutes, compact)
            CountdownColon(compact)
            CountdownDigit(seconds, compact)
        }

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(dotsCount) { index ->
                val active = index == currentActive
                val width by animateDpAsState(
                    targetValue = if (active) 28.dp else 8.dp,
                    animationSpec = KararTokens.Motion.spatial(),
                    label = "countdown-dot-width"
                )
                val color by animateColorAsState(
                    targetValue = if (active) KararColors.Gold else Color.White.copy(alpha = 0.20f),
                    animationSpec = KararTokens.Motion.effects(),
                    label = "countdown-dot-color"
                )
                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { onDotClick(index) }
                )
            }
        }
    }
}

@Composable
private fun CountdownDigit(value: String, compact: Boolean) {
    Box(
        modifier = Modifier
            .size(
                width = if (compact) 54.dp else 68.dp,
                height = if (compact) 46.dp else 56.dp
            )
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                (
                    slideInVertically(
                        animationSpec = KararTokens.Motion.spatial()
                    ) { full -> full } + fadeIn(KararTokens.Motion.effects())
                    ).togetherWith(
                    slideOutVertically(
                        animationSpec = KararTokens.Motion.spatial()
                    ) { full -> -full } + fadeOut(KararTokens.Motion.effects())
                )
            },
            label = "countdown-digit"
        ) { text ->
            Text(
                text = text,
                color = Color.White,
                fontSize = if (compact) 40.sp else 54.sp,
                fontWeight = FontWeight.Light,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CountdownColon(compact: Boolean) {
    Text(
        text = ":",
        color = Color.White.copy(alpha = 0.30f),
        fontSize = if (compact) 24.sp else 30.sp,
        fontWeight = FontWeight.Light,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

// =====================================================================
// زر التقويم + شريط الصلوات
// =====================================================================

@Composable
private fun BottomArea(
    visibleIds: List<String>,
    todayTimes: Map<String, Double>,
    tomorrowTimes: Map<String, Double>,
    targetId: String?,
    targetIsTomorrow: Boolean,
    compact: Boolean,
    use24Hour: Boolean,
    onOpenCalendar: () -> Unit,
    onPrayerClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (compact) 12.dp else 24.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 42.dp else 48.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(18.dp))
                    .clickable(onClick = onOpenCalendar),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = "تقويم وإمساكية الشهر",
                    tint = Color.White,
                    modifier = Modifier.size(if (compact) 16.dp else 17.dp)
                )
            }
        }

        Spacer(Modifier.height(if (compact) 2.dp else 4.dp))

        PrayerStrip(
            visibleIds = visibleIds,
            todayTimes = todayTimes,
            tomorrowTimes = tomorrowTimes,
            targetId = targetId,
            targetIsTomorrow = targetIsTomorrow,
            compact = compact,
            use24Hour = use24Hour,
            onPrayerClick = onPrayerClick
        )
    }
}

@Composable
private fun PrayerStrip(
    visibleIds: List<String>,
    todayTimes: Map<String, Double>,
    tomorrowTimes: Map<String, Double>,
    targetId: String?,
    targetIsTomorrow: Boolean,
    compact: Boolean,
    use24Hour: Boolean,
    onPrayerClick: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val targetIndex = visibleIds.indexOf(targetId)

    LaunchedEffect(targetId, visibleIds) {
        if (targetIndex >= 0) listState.centerItem(targetIndex)
    }

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            // ارتفاع ثابت لشريط الصلوات: يبقى القوس بموضعه ولا يرتفع/ينخفض.
            .height(if (compact) 128.dp else 140.dp)
    ) {
        val sidePadding = maxWidth * if (compact) 0.36f else 0.38f

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    // قناع تلاشي الحواف كما في المرجع (mask-image linear-gradient).
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0f to Color.Transparent,
                                0.06f to Color.Black,
                                0.94f to Color.Black,
                                1f to Color.Transparent
                            )
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
        ) {
            LazyRow(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = sidePadding,
                    end = sidePadding,
                    top = 20.dp,
                    bottom = 6.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp)
            ) {
                itemsIndexed(visibleIds, key = { _, id -> id }) { _, id ->
                    val todayHour = todayTimes[id] ?: 0.0
                    val showTomorrow = id == targetId && targetIsTomorrow
                    val rawHour = if (showTomorrow) tomorrowTimes[id] ?: todayHour else todayHour
                    val formatted = PrayerCalculator.formatHour(rawHour, use24Hour)
                    PrayerCard(
                        id = id,
                        label = PrayerMeta.label(id),
                        time12 = formatted.first,
                        amPm = formatted.second,
                        active = id == targetId,
                        tomorrow = showTomorrow,
                        compact = compact,
                        onClick = { onPrayerClick(id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PrayerCard(
    id: String,
    label: String,
    time12: String,
    amPm: String,
    active: Boolean,
    tomorrow: Boolean,
    compact: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(if (compact) 20.dp else 24.dp)
    val scale by animateFloatAsState(
        targetValue = if (active) 1.1f else 1f,
        animationSpec = KararTokens.Motion.spatial(),
        label = "prayer-card-scale"
    )
    val lift by animateDpAsState(
        targetValue = if (active) (-10).dp else 0.dp,
        animationSpec = KararTokens.Motion.spatial(),
        label = "prayer-card-lift"
    )
    val background by animateColorAsState(
        targetValue = if (active) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
        animationSpec = KararTokens.Motion.effects(),
        label = "prayer-card-bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (active) Color.White.copy(alpha = 0.30f) else Color.White.copy(alpha = 0.05f),
        animationSpec = KararTokens.Motion.effects(),
        label = "prayer-card-border"
    )

    Column(
        modifier = Modifier
            .widthIn(min = if (compact) 88.dp else 100.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = lift.toPx()
            }
            .then(
                if (active) Modifier.shadow(18.dp, shape, clip = false) else Modifier
            )
            .clip(shape)
            .background(background)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(if (compact) 10.dp else 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = KararIcons.prayer(id),
                contentDescription = null,
                tint = if (active) KararColors.Gold else Color.White.copy(alpha = 0.50f),
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = label,
                color = if (active) KararColors.Gold else Color.White.copy(alpha = 0.50f),
                fontSize = if (compact) 10.sp else 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = time12,
                color = Color.White,
                fontSize = if (compact) 12.sp else 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                style = androidx.compose.ui.text.TextStyle(textDirection = TextDirection.Ltr)
            )
            Text(
                text = amPm,
                color = Color.White.copy(alpha = 0.60f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        if (tomorrow) {
            Text(
                text = "(غداً)",
                color = Color(0xFFFFD700),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/** توسيط عنصر داخل LazyRow بدقة عبر فرق المركزين (يدعم RTL). */
private suspend fun LazyListState.centerItem(index: Int) {
    val visible = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
    if (visible == null) {
        scrollToItem(index)
    }
    val layout = layoutInfo
    val item = layout.visibleItemsInfo.firstOrNull { it.index == index } ?: return
    val viewportCenter = (layout.viewportStartOffset + layout.viewportEndOffset) / 2f
    val itemCenter = item.offset + item.size / 2f
    animateScrollBy(itemCenter - viewportCenter)
}

// =====================================================================
// حساب مرحلة القوس (مطابق لكتلة الحساب في المرجع)
// =====================================================================

private data class ArcPhase(
    val progress: Float,
    val leftLabel: String,
    val leftHour: Double,
    val rightLabel: String,
    val rightHour: Double,
    val badge: String?
)

private fun secondsOf(hour: Double): Int = (hour * 3600.0).roundToInt()

private fun computeArcPhase(
    nowSeconds: Int,
    times: Map<String, Double>,
    tomorrowTimes: Map<String, Double>,
    isShia: Boolean,
    use24Hour: Boolean = false
): ArcPhase {
    val fajr = times["fajr"] ?: 5.0
    val sunrise = times["sunrise"] ?: 6.5
    val sunset = times["sunset"] ?: 18.0
    val maghrib = times["maghrib"] ?: 18.25
    val fajrSec = secondsOf(fajr)
    val sunriseSec = secondsOf(sunrise)
    val sunsetSec = secondsOf(sunset)
    val maghribSec = secondsOf(maghrib)
    fun timeText(hour: Double): String = PrayerCalculator.formatHour(hour, use24Hour).first

    return when {
        // 1) الليل: من المغرب إلى الفجر
        nowSeconds >= maghribSec || nowSeconds < fajrSec -> {
            val rightHour = if (nowSeconds >= maghribSec) {
                tomorrowTimes["fajr"] ?: fajr
            } else {
                fajr
            }
            val span = ((86400 - maghribSec) + fajrSec).coerceAtLeast(1)
            val elapsed = if (nowSeconds >= maghribSec) {
                nowSeconds - maghribSec
            } else {
                nowSeconds + (86400 - maghribSec)
            }
            ArcPhase(
                progress = (elapsed.toFloat() / span).coerceIn(0f, 1f),
                leftLabel = "المغرب",
                leftHour = maghrib,
                rightLabel = "الفجر",
                rightHour = rightHour,
                badge = null
            )
        }

        // 2) بين الفجر والشروق
        nowSeconds < sunriseSec -> {
            val span = ((86400 - maghribSec) + sunriseSec).coerceAtLeast(1)
            val elapsed = nowSeconds + (86400 - maghribSec)
            ArcPhase(
                progress = (elapsed.toFloat() / span).coerceIn(0f, 1f),
                leftLabel = "المغرب",
                leftHour = maghrib,
                rightLabel = "الشروق",
                rightHour = sunrise,
                badge = "حلّ الفجر (${timeText(fajr)}) • بانتظار الشروق"
            )
        }

        // 3) النهار: من الشروق إلى الغروب (الشيعي) أو المغرب (السني)
        nowSeconds < sunsetSec -> {
            val endHour = if (isShia) sunset else maghrib
            val endSec = if (isShia) sunsetSec else maghribSec
            val span = (endSec - sunriseSec).coerceAtLeast(1)
            val elapsed = nowSeconds - sunriseSec
            ArcPhase(
                progress = (elapsed.toFloat() / span).coerceIn(0f, 1f),
                leftLabel = "الشروق",
                leftHour = sunrise,
                rightLabel = if (isShia) "الغروب" else "المغرب",
                rightHour = endHour,
                badge = null
            )
        }

        // 4) بين الغروب الفلكي والمغرب الشرعي (ذهاب الحمرة المشرقية)
        else -> {
            val span = (maghribSec - sunriseSec).coerceAtLeast(1)
            val elapsed = nowSeconds - sunriseSec
            ArcPhase(
                progress = (elapsed.toFloat() / span).coerceIn(0f, 1f),
                leftLabel = "الشروق",
                leftHour = sunrise,
                rightLabel = "المغرب",
                rightHour = maghrib,
                badge = "تم الغروب (${timeText(sunset)}) • ذهاب الحمرة المشرقية للمغرب"
            )
        }
    }
}
