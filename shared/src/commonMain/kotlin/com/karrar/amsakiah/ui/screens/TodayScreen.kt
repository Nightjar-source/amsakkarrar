package com.karrar.amsakiah.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.platform.LocalLayoutDirection
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.settings.AppSettings
import com.karrar.amsakiah.ui.state.AppState
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.core.state.PrayerMeta
import com.karrar.amsakiah.core.arc.computeArcPhase
import com.karrar.amsakiah.core.arc.secondsOf
import com.karrar.amsakiah.core.weather.WeatherInfo
import com.karrar.amsakiah.ui.components.KashidaHeader
import com.karrar.amsakiah.ui.components.SKY_ARC_STAGE_ASPECT
import com.karrar.amsakiah.ui.components.SkyArc
import com.karrar.amsakiah.ui.components.WeatherEffects
import com.karrar.amsakiah.ui.motion.effectsSpec
import com.karrar.amsakiah.ui.motion.motionEffects
import com.karrar.amsakiah.ui.motion.motionSpatial
import com.karrar.amsakiah.ui.motion.spatialSpec
import com.karrar.amsakiah.ui.motion.spatialFluidSpec
import com.karrar.amsakiah.ui.platform.rememberReduceMotion
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararFonts
import com.karrar.amsakiah.ui.theme.KararIcons
import com.karrar.amsakiah.ui.theme.KararTokens
import com.karrar.amsakiah.ui.theme.OnSky
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
 * زر التقويم يفتح **شاشة «تقويم القوس» المستقلة** كما في `amsak3d.tsx`
 * (`arcShowCalendarModal`) — بطاقات الأيام بأوقاتها الحقيقية، لا نافذة سفلية.
 */
@Composable
fun TodayScreen(
    state: AppState,
    weather: WeatherInfo?,
    onOpenArcCalendar: () -> Unit,
    onOpenSettings: () -> Unit,
    contentPadding: PaddingValues
) {
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

    val (skyTop, skyBottom, skyGlow) = SkyTheme.getDynamicSkyColors(nowSeconds.toFloat(), todayTimes)
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

    // الفجر والإمساك في الليل (بعد المغرب وقبل الفجر):
    // فلكياً وإسلامياً، الليلة تسبق النهار وتبدأ بمغرب اليوم، لذا فإن الفجر والإمساك القادمين هما خاتمة
    // هذه الليلة الحالية (ليلة الغد الشرعية)، فلا يُقال لهما «غداً» في العد التنازلي.
    // أما أوقات الشروق والظهر والعصر والمغرب فتكون في نهار الغد فعلاً وتوسم بـ «غداً [اليوم] [التاريخ]».
    // وعند منتصف الليل (12 ص) ينقلب التاريخ المدني وتختفي «غداً» لأن الفجر صار في نفس اليوم المدني.
    val maghribSec = secondsOf(todayTimes["maghrib"] ?: 18.0)
    val fajrSec = secondsOf(todayTimes["fajr"] ?: 5.0)
    val isNightTime = nowSeconds >= maghribSec || nowSeconds < fajrSec
    val isTonightFajrOrImsak = isNightTime && (targetId == "imsak" || targetId == "fajr")
    val showTomorrowLabel = targetIsTomorrow && !isTonightFajrOrImsak

    val countdownLabel = if (targetId == null) "" else buildString {
        append("باقي على ").append(targetLabel)
        if (showTomorrowLabel) {
            append(" غداً ")
            append(PrayerCalculator.weekDays[tomorrowDate.dayOfWeek.ordinal])
            append(' ').append(tomorrowHijri.day)
            append(' ').append(tomorrowHijri.monthName)
        }
    }

    val layoutDirection = LocalLayoutDirection.current
    val topInset = contentPadding.calculateTopPadding()
    val bottomInset = contentPadding.calculateBottomPadding()
    val startInset = contentPadding.calculateStartPadding(layoutDirection)
    val endInset = contentPadding.calculateEndPadding(layoutDirection)

    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val windowPosture = adaptiveInfo.windowPosture
    val isTabletop = windowPosture.isTabletop
    val separatingHinge = remember(windowPosture) {
        windowPosture.hingeList.firstOrNull { it.isSeparating } ?: windowPosture.hingeList.firstOrNull()
    }
    var localHingeTopPx by remember { mutableStateOf<Float?>(null) }
    var localHingeBottomPx by remember { mutableStateOf<Float?>(null) }
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                if (isTabletop && separatingHinge != null) {
                    val b = separatingHinge.bounds
                    localHingeTopPx = coordinates.windowToLocal(Offset(0f, b.top.toFloat())).y
                    localHingeBottomPx = coordinates.windowToLocal(Offset(0f, b.bottom.toFloat())).y
                } else {
                    localHingeTopPx = null
                    localHingeBottomPx = null
                }
            }
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val stageCompact = screenHeight < 620.dp
        val compact = stageCompact
        val scrollState = rememberScrollState()

        if (isTabletop) {
            // ---------- وضع الطاولة / Flex Mode في الأجهزة المطوية (Tabletop Posture) ----------
            // النصف العلوي (فوق المفصل): الهيدر + القوس الشمسي + العدّاد التنازلي
            // النصف السفلي (تحت المفصل): بطاقات الصلوات الست + زر التقويم بعيداً عن خط الطيّ
            val fallbackTop = screenHeight * 0.5f
            val hingeTopDp = with(density) {
                localHingeTopPx?.coerceIn(120f, constraints.maxHeight.toFloat())?.toDp() ?: fallbackTop
            }
            val hingeBottomDp = with(density) {
                localHingeBottomPx?.coerceIn(0f, constraints.maxHeight.toFloat())?.toDp() ?: fallbackTop
            }
            val hingeGapDp = (hingeBottomDp - hingeTopDp).coerceAtLeast(16.dp)
            val topPaneHeight = (hingeTopDp - topInset).coerceAtLeast(180.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = startInset, end = endInset),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(topInset))

                // الجزء العلوي فوق المفصل
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = KararTokens.Size.todayStageMaxWidth)
                        .height(topPaneHeight)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    TodayHeader(
                        settings = settings,
                        weather = weather,
                        hijri = hijri,
                        date = date,
                        onOpenSettings = onOpenSettings
                    )
                    val availableW = minOf(screenWidth - 32.dp, KararTokens.Size.todayStageMaxWidth - 32.dp).coerceAtLeast(1.dp)
                    val targetArcHeight = minOf(
                        availableW / SKY_ARC_STAGE_ASPECT,
                        (topPaneHeight * 0.46f).coerceAtLeast(120.dp)
                    )
                    val animatedArcHeight by animateDpAsState(
                        targetValue = targetArcHeight,
                        animationSpec = motionSpatialFluid(),
                        label = "tabletop-arc-height"
                    )
                    val arcWidth = animatedArcHeight * SKY_ARC_STAGE_ASPECT
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
                        modifier = Modifier.size(width = arcWidth, height = animatedArcHeight)
                    )
                    if (countdownIds.isNotEmpty() && targetId != null) {
                        CountdownSection(
                            label = countdownLabel,
                            hours = hoursLeft,
                            minutes = minutesLeft,
                            seconds = secondsLeft,
                            dotsCount = countdownIds.size,
                            activeDot = activeIndex,
                            compact = true,
                            onDotClick = { manualIndex = it },
                            onSwipeNext = { manualIndex = (activeIndex + 1).mod(countdownIds.size) },
                            onSwipePrevious = { manualIndex = (activeIndex - 1 + countdownIds.size).mod(countdownIds.size) }
                        )
                    }
                }

                // فجوة المفصل (لا نضع أي محتوى أو أزرار فوق خط الطيّ)
                Spacer(Modifier.height(hingeGapDp))

                // الجزء السفلي تحت المفصل — سهل اللمس على السطح الأفقي
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .widthIn(max = KararTokens.Size.todayStageMaxWidth)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    BottomArea(
                        visibleIds = visibleIds,
                        todayTimes = todayTimes,
                        tomorrowTimes = tomorrowTimes,
                        targetId = targetId,
                        targetIsTomorrow = targetIsTomorrow,
                        isNightTime = isNightTime,
                        compact = true,
                        use24Hour = state.settings.use24Hour,
                        onOpenCalendar = onOpenArcCalendar,
                        onPrayerClick = { id ->
                            if (countdownIds.isNotEmpty()) {
                                val index = countdownIds.indexOf(id)
                                if (index >= 0) manualIndex = index
                            }
                        }
                    )
                    Spacer(Modifier.height(bottomInset + 12.dp))
                }
            }
            return@BoxWithConstraints
        }

        // التمرير يمتد من الحافة للحافة كاملة الشاشة (Edge-to-Edge) وخلف أشرطة النظام
        // والشريط العائم بلا أي "حد تمرير" أو قص مستطيل
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // مسافة الأمان العلوية تحت شريط الحالة
            Spacer(Modifier.height(topInset))

            // المسرح الداخلي: موسّط في الشاشات العريضة/المستطيلة ويمتد انسيابياً
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = KararTokens.Size.todayStageMaxWidth)
                    .padding(start = startInset, end = endInset),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TodayHeader(
                    settings = settings,
                    weather = weather,
                    hijri = hijri,
                    date = date,
                    onOpenSettings = onOpenSettings
                )

                // ---- القوس السماوي التفاعلي (توسع وتقلص مائي سلس عند تغير الحجم والدوران والطيّ) ----
                val availableW = minOf(screenWidth - 32.dp, KararTokens.Size.todayStageMaxWidth - 32.dp).coerceAtLeast(1.dp)
                val targetArcHeight = (availableW / SKY_ARC_STAGE_ASPECT).coerceAtMost(if (compact) 230.dp else 295.dp)
                val animatedArcHeight by animateDpAsState(
                    targetValue = targetArcHeight,
                    animationSpec = motionSpatialFluid(),
                    label = "sky-arc-height"
                )
                val arcWidth = animatedArcHeight * SKY_ARC_STAGE_ASPECT

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(animatedArcHeight + 8.dp),
                    contentAlignment = Alignment.Center
                ) {
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
                        modifier = Modifier.size(width = arcWidth, height = animatedArcHeight)
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
                    isNightTime = isNightTime,
                    compact = compact,
                    use24Hour = state.settings.use24Hour,
                    onOpenCalendar = onOpenArcCalendar,
                    onPrayerClick = { id ->
                        if (countdownIds.isNotEmpty()) {
                            val index = countdownIds.indexOf(id)
                            if (index >= 0) manualIndex = index
                        }
                    }
                )
            }

            // مسافة الأمان السفلية لضمان تدفق ومرور المحتوى كاملاً تحت شريط التنقل واستقراره فوقه بأريحية
            Spacer(Modifier.height(bottomInset + 20.dp))
        }
    }
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
                // ارتفاع أدنى فقط، بلا قصّ: مربع الطقس/المدينة ينمو مع حجم الخط
                // وعرض النافذة بدل أن تُقطع حروفه (كان height + clipToBounds).
                .heightIn(min = if (maxWidth < 600.dp) 66.dp else 84.dp)
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
                        .background(OnSky.glassDeep)
                        .border(1.dp, OnSky.borderStrong, CircleShape)
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
                        tint = OnSky.gold,
                        modifier = Modifier.size(if (tiny) 10.dp else 12.dp)
                    )
                    Text(
                        text = "${settings.cityName.substringBefore(" (")}، ${settings.countryName}",
                        color = OnSky.text,
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
                            .background(OnSky.glassDeep)
                            .border(1.dp, OnSky.borderStrong, CircleShape)
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
                                color = OnSky.text,
                                fontSize = chipTextSize,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                style = androidx.compose.ui.text.TextStyle(textDirection = TextDirection.Ltr)
                            )
                            if (showWeatherCondition) {
                                Text(
                                    text = "• ${weatherConditionLabel(weather.code)}",
                                    color = OnSky.textSecondary,
                                    fontSize = if (tiny) 8.5.sp else 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Text(
                                text = seasonBadge(date, settings.latitude),
                                color = OnSky.textSecondary,
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
                        color = OnSky.text,
                        fontSize = dateSize,
                        fontWeight = FontWeight.Bold,
                        fontFamily = KararFonts.ReemKufi,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = " (${hijri.year} هـ)",
                        color = OnSky.text.copy(alpha = 0.65f),
                        fontSize = dateSize,
                        fontWeight = FontWeight.Bold,
                        fontFamily = KararFonts.ReemKufi,
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
        val reduceMotion = rememberReduceMotion()
        AnimatedContent(
            targetState = currentActive,
            transitionSpec = {
                val isForward = targetState > initialState
                // انسيابية تقليب الصلوات أفقياً (يمين / يسار) بفيزياء الزنبرك السائلة وبدون أي ارتداد أو وميض:
                val dir = if (isForward) -1 else 1
                (slideInHorizontally(
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                    )
                ) { width -> dir * width / 3 } +
                    fadeIn(animationSpec = effectsSpec(reduceMotion)))
                    .togetherWith(
                        slideOutHorizontally(
                            animationSpec = androidx.compose.animation.core.spring(
                                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                                stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                            )
                        ) { width -> -dir * width / 3 } +
                        fadeOut(animationSpec = effectsSpec(reduceMotion))
                    )
            },
            label = "prayer-countdown-flip",
            modifier = Modifier.fillMaxWidth()
        ) { _ ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
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

                // الترتيب الرسمي للعدّاد: الساعات يسارًا ← الدقائق وسطًا ← الثواني يمينًا.
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.ui.platform.LocalLayoutDirection provides
                        androidx.compose.ui.unit.LayoutDirection.Ltr
                ) {
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
                }
            }
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
                    animationSpec = motionSpatial(),
                    label = "countdown-dot-width"
                )
                val color by animateColorAsState(
                    targetValue = if (active) KararColors.Gold else Color.White.copy(alpha = 0.20f),
                    animationSpec = motionEffects(),
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
    val reduceMotion = rememberReduceMotion()
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
                // سلايد طباقات ناعم: القادم يطلع من ربع الارتفاع بتلاشٍ تدريجي،
                // والخارج يتراجع ربعًا فقط — بلا انزلاق كامل ولا «وميض» أرقام صلب.
                val incoming = slideInVertically(animationSpec = spatialFluidSpec(reduceMotion)) { it / 4 } +
                    fadeIn(animationSpec = effectsSpec(reduceMotion))
                val outgoing = slideOutVertically(animationSpec = spatialFluidSpec(reduceMotion)) { -it / 4 } +
                    fadeOut(animationSpec = effectsSpec(reduceMotion))
                incoming.togetherWith(outgoing) using SizeTransform(clip = false)
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
    isNightTime: Boolean,
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
                    contentDescription = "أوقات الصلاة شهر — قائمة أوقات الصلاة طوال الشهر الهجري",
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
            isNightTime = isNightTime,
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
    isNightTime: Boolean,
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
                    val isTonightFajrOrImsak = isNightTime && (id == "imsak" || id == "fajr")
                    // المنطق الأصلي: عند انتهاء وقت أي صلاة اليوم ينتقل عرضها لوقت اليوم التالي
                    val isPast = nowSeconds >= (todayHour * 3600).roundToInt()
                    val showTomorrow = isPast || (id == targetId && targetIsTomorrow && !isTonightFajrOrImsak)
                    val rawHour = if (showTomorrow) tomorrowTimes[id] ?: todayHour else todayHour
                    val formatted = PrayerCalculator.formatHour(rawHour, use24Hour)
                    // في الليل بعد المغرب: لا نكتب (غداً) على الفجر/الإمساك لأن الليلة صارت ليلتهما؛
                    // وبعد الساعة 12 ليلاً يتغير التاريخ المدني فتسقط (غداً) عن جميع الصلوات تلقائياً.
                    val displayTomorrowLabel = showTomorrow && !isTonightFajrOrImsak
                    PrayerCard(
                        id = id,
                        label = PrayerMeta.label(id),
                        time12 = formatted.first,
                        amPm = formatted.second,
                        active = id == targetId,
                        tomorrow = displayTomorrowLabel,
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
        animationSpec = motionSpatial(),
        label = "prayer-card-scale"
    )
    val lift by animateDpAsState(
        targetValue = if (active) (-10).dp else 0.dp,
        animationSpec = motionSpatial(),
        label = "prayer-card-lift"
    )
    // خلفية البطاقة النشطة: تدرّج كهرماني داكن مصمت — لا مستطيل فاتح شفاف
    // فوق الزجاج (كان يبدو كمربع باهت بداخله دائرة).
    val activeFill = Brush.verticalGradient(
        listOf(Color(0xFF4A3410), Color(0xFF241A08))
    )
    val borderColor by animateColorAsState(
        targetValue = if (active) Color(0xFFFFC832) else OnSky.borderStrong,
        animationSpec = motionEffects(),
        label = "prayer-card-border"
    )
    val captionColor by animateColorAsState(
        targetValue = if (active) OnSky.gold else OnSky.textMuted,
        animationSpec = motionEffects(),
        label = "prayer-card-caption"
    )

    Column(
        modifier = Modifier
            .widthIn(min = if (compact) 88.dp else 100.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = lift.toPx()
            }
            .clip(shape)
            // نشط: تعبئة كهرمانية مصمتة + حد ذهبي واضح. غير نشط: زجاج السماء.
            .background(if (active) activeFill else SolidColor(OnSky.glass))
            .border(if (active) 1.5.dp else 1.dp, borderColor, shape)
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
                tint = captionColor,
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = label,
                color = captionColor,
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
                color = OnSky.text,
                fontSize = if (compact) 12.sp else 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                style = androidx.compose.ui.text.TextStyle(textDirection = TextDirection.Ltr)
            )
            Text(
                text = amPm,
                color = OnSky.textSecondary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        if (tomorrow) {
            Text(
                text = "(غداً)",
                color = OnSky.gold,
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

