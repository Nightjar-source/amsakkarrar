package com.karrar.amsakiah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.jinan.AlmanacDay
import com.karrar.amsakiah.core.jinan.DailyDeed
import com.karrar.amsakiah.core.jinan.JinanItem
import com.karrar.amsakiah.core.jinan.MonthAlmanac
import com.karrar.amsakiah.core.jinan.buildMonthAlmanac
import com.karrar.amsakiah.core.jinan.monthWorkCount
import com.karrar.amsakiah.core.jinan.MonthAlmanacCache
import com.karrar.amsakiah.ui.state.AppState
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.components.SectionTitle
import com.karrar.amsakiah.ui.theme.KararColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.toLocalDateTime

/**
 * «أعمال اليوم» — منقصة هجرية تفاعلية مربوطة بالتقويم الهجري مباشرة:
 *
 * - شريط الأشهر (١..١٢): كل رقاقة باسم الشهر وعدد أعماله (من «مفاتيح الجنان»)،
 *   والشهر الحالي يضيء بلون القوس.
 * - «منقصة الشهر»: أيام ١..٣٠ (أو ٢٩ في الأشهر القصيرة — تُكتشف بأداتنا نفسها)؛
 *   كل مربّع يحمل اليوم الهجري وشارة عدد أعماله، واليوم الحالي دائرة ذهبية.
 * - ما دون المنقصة: أعمال اليوم المحدد — الليلة ← اليوم ← عامة الشهر ← يوم
 *   الأسبوع — ببطاقات الكاتب الواحد، وفوق كل مجموعة «ابحث: N عمل».
 *
 * كل العناوين تُشتق من «مفاتيح الجنان» نفسه — لا تحرير يدوي للنصوص،
 * فما يظهر هو ما ورد في الكتاب فعلًا.
 */
@Composable
fun DailyDeedsScreen(
    state: AppState,
    contentPadding: PaddingValues,
    onOpenReader: (JinanItem, String?) -> Unit
) {
    val settings = state.settings
    val today = remember(settings.hijriOffset) { PrayerCalculator.todayDate(settings) }
    val todayHijri = remember(today, settings.hijriOffset) { PrayerCalculator.hijriFor(settings, today) }

    var year by rememberSaveable { mutableIntStateOf(todayHijri.year) }
    var month by rememberSaveable { mutableIntStateOf(todayHijri.monthNum) }
    var selectedDay by rememberSaveable { mutableIntStateOf(todayHijri.day) }

    // الحالة الأولية تُقرأ من الكاش — الدخول المتكرر لا يُظهر «جارٍ الفحص» ولا يُعيد البناء.
    var almanac by remember(year, month, settings.hijriOffset, today) {
        mutableStateOf(MonthAlmanacCache.get(year, month, settings.hijriOffset))
    }
    LaunchedEffect(year, month, settings.hijriOffset, today) {
        if (almanac != null) return@LaunchedEffect
        // البناء على Default — لا يعيق الإطار أثناء الطيّ/الانتقال/الضغط.
        val built = withContext(Dispatchers.Default) {
            buildMonthAlmanac(settings, year, month, today)
        }
        MonthAlmanacCache.put(built, settings.hijriOffset)
        almanac = built
    }

    val favorites = settings.favoriteJinanItems
    val toggleFavorite: (Int) -> Unit = { id ->
        state.update { s ->
            val next = if (id in s.favoriteJinanItems) s.favoriteJinanItems - id else s.favoriteJinanItems + id
            s.copy(favoriteJinanItems = next)
        }
    }

    // اليوم المختار (مقيّد بـعدد أيام الشهر الفعلي 29/30).
    val selected: AlmanacDay? = almanac?.days?.firstOrNull { it.hijriDay == selectedDay }
        ?: almanac?.days?.lastOrNull()

    // اسم الشهر المعروض — متاح حتى أثناء بناء المنقصة في الخلفية.
    val monthName = PrayerCalculator.hijriMonths.getOrElse(month - 1) { "" }

    val nowHour = remember(state.now) {
        val zone = PrayerCalculator.zoneOf(settings)
        state.now.toLocalDateTime(zone).hour
    }
    // بعد الظهر والمساء (>= 12): تبدأ الاستعدادات لليلة أو تصبح نشطة، فتتصدر أعمال الليلة القمة ديناميكياً
    val isAfternoonOrNight = nowHour >= 12

    val groups: List<Pair<String, List<DailyDeed>>> = selected?.let { d ->
        val weekdayTitle = if (d.weekday == "الخميس") {
            "أعمال يوم الخميس وليلة الجمعة المباركة (${d.weekdayWorks.size})"
        } else {
            "يوم ${d.weekday} (${d.weekdayWorks.size})"
        }
        val rawGroups = if (isAfternoonOrNight) {
            listOf(
                "الليلة — ليلة ${d.hijriDay} من $monthName (${d.night.size})" to d.night,
                "اليوم — ${d.hijriDay} $monthName (${d.day.size})" to d.day,
                weekdayTitle to d.weekdayWorks,
                "عامة $monthName (${d.general.size})" to d.general
            )
        } else {
            listOf(
                "اليوم — ${d.hijriDay} $monthName (${d.day.size})" to d.day,
                weekdayTitle to d.weekdayWorks,
                "الليلة — ليلة ${d.hijriDay} من $monthName (${d.night.size})" to d.night,
                "عامة $monthName (${d.general.size})" to d.general
            )
        }
        rawGroups.filter { it.second.isNotEmpty() }
    }.orEmpty()

    val layoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding() + 24.dp
            )
    ) {
        // ---------- شريط الأشهر ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PrayerCalculator.hijriMonths.forEachIndexed { index, name ->
                val m = index + 1
                val isSelected = m == month
                val isCurrent = m == todayHijri.monthNum && year == todayHijri.year
                val count = monthWorkCount[m] ?: 0
                MonthChip(
                    name = name,
                    count = count,
                    selected = isSelected,
                    isCurrentMonth = isCurrent,
                    onClick = {
                        month = m
                        selectedDay = selectedDay.coerceAtMost(30)
                    }
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // ---------- منقصة الشهر ----------
        // البناء في الخلفية (Dispatchers.Default) — والفهرس المسبق يجعل
        // الظهور شبه فوري؛ لا نعرض أي عبارة «فحص/ربط» بل مؤشرًا هادئًا فقط.
        val almanacDays = almanac?.days
        if (almanacDays == null) {
            GlassCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                corner = 20.dp,
                contentPadding = PaddingValues(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = KararColors.Gold
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "تحضير أعمال اليوم…",
                        color = KararColors.TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        } else {
        // ---------- منقصة الشهر — شريط أفقي ----------
        // الأيام تتكيّف أفقيًا كشريط (مثل شريط الأشهر أعلاه): تمرير سلس،
        // واليوم الحالي يدخل منتصف المنظرة تلقائيًا عند الفتح.
        val daysListState = rememberLazyListState()
        LaunchedEffect(almanacDays) {
            val idx = almanacDays.indexOfFirst { it.hijriDay == selectedDay }
            if (idx >= 0) daysListState.scrollToItem((idx - 2).coerceAtLeast(0))
        }
        LazyRow(
            state = daysListState,
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(almanacDays, key = { it.hijriDay }) { day ->
                DayCell(
                    day = day,
                    selected = day.hijriDay == selectedDay,
                    isToday = day.date == today,
                    onClick = { selectedDay = day.hijriDay },
                    modifier = Modifier.width(54.dp)
                )
            }
        }

            Spacer(Modifier.height(2.dp))

        // ---------- ترويسة اليوم المحدد ----------
        // اليوم نفسه: الشريط العلوي الموحّد يكتفي (بلا تكرار أي عنوان هنا).
        // يوم آخر: تُعرض ترويسته كاملة + زر «اليوم» للعودة — حتى لا يخطئ المستخدم.
        val viewingToday = year == todayHijri.year && month == todayHijri.monthNum &&
            selected?.hijriDay == todayHijri.day
        if (!viewingToday) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                selected?.let { d ->
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        tint = KararColors.Gold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = "${d.weekday} ${d.hijriDay} $monthName $year هـ",
                        color = KararColors.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(KararColors.Teal.copy(alpha = 0.14f))
                        .border(1.dp, KararColors.Teal.copy(alpha = 0.35f), RoundedCornerShape(50))
                        .clickable {
                            year = todayHijri.year
                            month = todayHijri.monthNum
                            selectedDay = todayHijri.day
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        "اليوم",
                        color = KararColors.Teal,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // ---------- أعمال اليوم المحدد ----------
        if (groups.isEmpty()) {
            GlassCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                corner = 20.dp,
                contentPadding = PaddingValues(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "لا توجد أعمال موقوتة في هذا اليوم",
                        color = KararColors.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "كثير من الأشهر بلا أعمال كل يوم — انقر رقّاصة يومٍ موسوم بعدد الأعمال (١٣، ١٥، ١٧، ١٩…).",
                        color = KararColors.TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            groups.forEach { (title, deeds) ->
                SectionTitle(title)
                Spacer(Modifier.height(4.dp))
                deeds.forEach { deed ->
                    val isRamadan = month == 9
                    val customTitle = if (isRamadan) {
                        when (deed.item.id) {
                            5698 -> "صلوات ليالي شهر رمضان (صلاة الليلة ${selectedDay})"
                            5699 -> "دعاء اليوم ${selectedDay} من شهر رمضان"
                            else -> null
                        }
                    } else null
                    val query = if (isRamadan) ramadanItemQuery(deed.item.id, selectedDay) else null
                    DeedCard(
                        deed = deed,
                        customTitle = customTitle,
                        isFavorite = deed.item.id in favorites,
                        onToggleFavorite = { toggleFavorite(deed.item.id) },
                        onOpen = { onOpenReader(deed.item, query) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
            }
        }

        Spacer(Modifier.height(28.dp))
        }
    }
}

/** رقاقة الشهر — اسم الشهر + عدد أعماله، والشهر الحالي نبض لامع. */
@Composable
private fun MonthChip(
    name: String,
    count: Int,
    selected: Boolean,
    isCurrentMonth: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = Modifier
            .clip(shape)
            .background(
                if (selected) {
                    Brush.verticalGradient(listOf(KararColors.Gold.copy(alpha = 0.28f), KararColors.Gold.copy(alpha = 0.10f)))
                } else {
                    Brush.verticalGradient(listOf(KararColors.CardSoft, KararColors.CardSoft))
                },
                shape
            )
            .border(
                1.dp,
                if (selected) KararColors.Gold else if (isCurrentMonth) KararColors.Gold.copy(alpha = 0.45f) else KararColors.Border,
                shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            name,
            color = if (selected) KararColors.Gold else KararColors.TextPrimary,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isCurrentMonth) {
                Box(
                    Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(KararColors.Gold)
                )
                Spacer(Modifier.size(4.dp))
            }
            Text(
                text = if (count > 0) "$count عمل" else "لا أعمال",
                color = if (selected) KararColors.Gold.copy(alpha = 0.85f) else KararColors.TextMuted,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

/** مربّع اليوم في المنقصة — اليوم رقم هجري + شارة عدد أعماله. */
@Composable
private fun DayCell(
    day: AlmanacDay,
    selected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    val hasWorks = day.total > 0
    Column(
        modifier = modifier
            .clip(shape)
            .background(
                when {
                    selected -> Brush.verticalGradient(listOf(KararColors.Gold, KararColors.Gold.copy(alpha = 0.82f)))
                    isToday -> Brush.verticalGradient(listOf(KararColors.Gold.copy(alpha = 0.28f), KararColors.Gold.copy(alpha = 0.10f)))
                    else -> Brush.verticalGradient(listOf(KararColors.CardSoft, KararColors.CardSoft))
                },
                shape
            )
            .border(
                1.dp,
                when {
                    selected -> KararColors.Gold
                    isToday -> KararColors.Gold.copy(alpha = 0.6f)
                    else -> KararColors.Border
                },
                shape
            )
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text(
            day.hijriDay.toString(),
            color = if (selected) Color(0xFF241A03) else if (isToday) KararColors.Gold else KararColors.TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = if (hasWorks) "${day.total}" else "·",
            color = when {
                selected -> Color(0xFF241A03).copy(alpha = 0.85f)
                hasWorks -> KararColors.Gold
                else -> KararColors.TextMuted
            },
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
    }
}

/**
 * كارت العمل — يحاكي بطاقات «المفاتيح»: أيقونة + عنوان العمل فقط (بلا تفاصيل).
 * بالضغط يفتح «القارئ الموحّد» على الفصل/العمل المطلوب.
 */
@Composable
private fun DeedCard(
    deed: DailyDeed,
    customTitle: String? = null,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        corner = 20.dp,
        color = KararColors.CardSoft,
        borderColor = KararColors.Border,
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(KararColors.Purple.copy(alpha = 0.16f))
                    .border(1.dp, KararColors.Purple.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = KararColors.Purple,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(Modifier.size(10.dp))
            Text(
                text = customTitle ?: deed.item.title,
                color = KararColors.TextPrimary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.size(6.dp))
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = if (isFavorite) "إزالة من المفضل" else "أضف إلى المفضل",
                tint = if (isFavorite) KararColors.Gold else KararColors.TextMuted,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onToggleFavorite)
                    .padding(2.dp)
            )
        }
    }
}

/**
 * يحدّد كلمة البحث المباشرة في القارئ لنصوص صلوات ليالي رمضان (5698) وأدعية أيامه (5699)
 * للانتقال مباشرة إلى صفحة اليوم/الليلة المحددة في الكتاب.
 */
private fun ramadanItemQuery(itemId: Int, day: Int): String? {
    if (day !in 1..30) return null
    return when (itemId) {
        5698 -> when (day) {
            1 -> "الاولي"
            2 -> "الثانيه"
            3 -> "الثالثه"
            4 -> "الرابعه"
            5 -> "الخامسه"
            6 -> "السادسه"
            7 -> "السابعه"
            8 -> "الثامنه"
            9 -> "التاسعه"
            10 -> "العاشره"
            11 -> "الحاديه عشره"
            12 -> "الثانيه عشره"
            13 -> "الثالثه عشره"
            14 -> "الرابعه عشره"
            15 -> "الخامسه عشره"
            16 -> "السادسه عشره"
            17 -> "السابعه عشره"
            18 -> "الثامنه عشره"
            19 -> "التاسعه عشره"
            in 20..24 -> "صلوات الليله العشرين"
            25 -> "الخامسه والعشرين"
            26 -> "السادسه والعشرين"
            27 -> "السابعه والعشرين"
            28 -> "الثامنه والعشرين"
            29 -> "التاسعه والعشرين"
            30 -> "الثلاثين"
            else -> null
        }
        5699 -> when (day) {
            1 -> "الاول"
            2 -> "الثاني"
            3 -> "الثالث"
            4 -> "الرابع"
            5 -> "الخامس"
            6 -> "السادس"
            7 -> "السابع"
            8 -> "الثامن"
            9 -> "التاسع"
            10 -> "العاشر"
            11 -> "الحادي عشر"
            12 -> "الثاني عشر"
            13 -> "الثالث عشر"
            14 -> "الرابع عشر"
            15 -> "الخامس عشر"
            16 -> "السادس عشر"
            17 -> "السابع عشر"
            18 -> "الثامن عشر"
            19 -> "التاسع عشر"
            20 -> "العشرين"
            21 -> "الحادي والعشرين"
            22 -> "الثاني والعشرين"
            23 -> "الثالث والعشرين"
            24 -> "الرابع والعشرين"
            25 -> "الخامس والعشرين"
            26 -> "السادس والعشرين"
            27 -> "السابع والعشرين"
            28 -> "الثامن والعشرين"
            29 -> "التاسع والعشرين"
            30 -> "الثلاثين"
            else -> null
        }
        else -> null
    }
}

