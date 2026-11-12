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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.jinan.DailyDeed
import com.karrar.amsakiah.core.jinan.DailyDeedSource
import com.karrar.amsakiah.core.jinan.JinanItem
import com.karrar.amsakiah.core.jinan.dailyDeeds
import com.karrar.amsakiah.core.jinan.monthDayDeeds
import com.karrar.amsakiah.core.jinan.weekdayDeeds
import com.karrar.amsakiah.core.state.AppState
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.components.SectionTitle
import com.karrar.amsakiah.ui.theme.KararColors

/**
 * «أعمال اليوم» — شاشة مخصَّصة من «مفاتيح الجنان»: أعمال الليلة/اليوم/يوم الأسبوع.
 *
 * ثلاثة تبويبات (مستطيلات كبسولة واحدة الاختيار — بجميع الشاشات):
 * 1) «اليوم» — ويُملأ تلقائيًا (اليوم الهجري + يوم الأسبوع + الليلة منذ مساء أمس).
 * 2) «هذا الشهر» — كل أيام الشهر الهجري (١…٣٠) بطيئيات تناظرية: تظهر أعمال كل ليلة/يوم هجري.
 * 3) «كل أيام الأسبوع» — الاثنين…الأحد (كاسم العنوان المتعلق به في مفاتيح الجنان).
 *
 * البطاقات مستطيلات تكيفية (Adaptive، لا تتقلص)؛ الفتح يفتح «القارئ الموحّد»،
 * والنجمة تنقل العمل إلى «أعمال اليوم (المهام)».
 */
@Composable
fun DailyDeedsScreen(
    state: AppState,
    contentPadding: PaddingValues,
    onOpenReader: (JinanItem) -> Unit
) {
    val settings = state.settings
    val today = remember(settings.hijriOffset) { PrayerCalculator.todayDate(settings) }
    val hijri = remember(today, settings.hijriOffset) { PrayerCalculator.hijriFor(settings, today) }
    val weekdayName = PrayerCalculator.weekDays.getOrNull(today.dayOfWeek.ordinal).orEmpty()
    val favorites = settings.favoriteJinanItems

    val toggleFavorite: (Int) -> Unit = { id ->
        state.update { s ->
            val next = if (id in s.favoriteJinanItems) s.favoriteJinanItems - id else s.favoriteJinanItems + id
            s.copy(favoriteJinanItems = next)
        }
    }

    val deedsToday = remember(today, hijri) { dailyDeeds(today, hijri) }

    // التبويب النشط: 0 = اليوم، 1 = هذا الشهر، 2 = كل أيام الأسبوع.
    var selectedTab by remember { mutableStateOf(0) }
    var selectedDay by remember(hijri.day) { mutableStateOf(hijri.day) }
    var selectedWeekday by remember(today.dayOfWeek.ordinal) { mutableStateOf(today.dayOfWeek.ordinal) }
    // أسماء أيام الأسبوع بالترتيب الرسمي (الاثنين أولاً — نفس تسلسل kotlinx dayOfWeek).
    val weekDays = listOf("الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت", "الأحد")

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 430.dp),
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 10.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ---------- ترويسة التاريخ ----------
        item(span = { GridItemSpan(maxLineSpan) }) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                corner = 24.dp,
                color = KararColors.CardSoft,
                borderColor = KararColors.Border.copy(alpha = 0.5f),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "أعمال اليوم",
                            color = KararColors.Gold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${hijri.day} ${hijri.monthName} ${hijri.year} هـ",
                            color = KararColors.TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$weekdayName · ${today.dayOfMonth} / ${today.monthNumber} / ${today.year}",
                            color = KararColors.TextMuted,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(KararColors.Gold.copy(alpha = 0.14f))
                            .border(1.dp, KararColors.Gold.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.CalendarToday,
                            contentDescription = null,
                            tint = KararColors.Gold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // ---------- اختيار التبويب ----------
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(KararColors.CardSoft)
                    .border(1.dp, KararColors.Border, RoundedCornerShape(50))
                    .padding(4.dp)
            ) {
                val tabs = listOf("اليوم", "هذا الشهر", "كل أيام الأسبوع")
                tabs.forEachIndexed { index, title ->
                    val selected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) KararColors.Gold else androidx.compose.ui.graphics.Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (selected) KararColors.OnGold else KararColors.TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // ---------- تبويب «هذا الشهر» — اختيار اليوم الهجري ----------
        if (selectedTab == 1) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(30) { index ->
                        val day = index + 1
                        val selected = selectedDay == day
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selected) KararColors.Gold.copy(alpha = 0.18f) else KararColors.CardSoft)
                                .border(
                                    1.dp,
                                    if (selected) KararColors.Gold else KararColors.Border,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedDay = day }
                                .padding(horizontal = 13.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$day",
                                color = if (selected) KararColors.Gold else KararColors.TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // ---------- تبويب «كل أيام الأسبوع» — اختيار يوم الأسبوع ----------
        if (selectedTab == 2) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(weekDays.size) { index ->
                        val name = weekDays[index]
                        val selected = selectedWeekday == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selected) KararColors.Gold.copy(alpha = 0.18f) else KararColors.CardSoft)
                                .border(
                                    1.dp,
                                    if (selected) KararColors.Gold else KararColors.Border,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedWeekday = index }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                color = if (selected) KararColors.Gold else KararColors.TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // ---------- المجموعات حسب التبويب ----------
        val groups: List<Pair<String, List<DailyDeed>>> = when (selectedTab) {
            1 -> monthDayDeeds(selectedDay)
                .let { deeds ->
                    val night = deeds.filter { it.source == DailyDeedSource.MONTH_NIGHT }
                    val day = deeds.filter { it.source == DailyDeedSource.MONTH_DAY }
                    listOf(
                        "أعمال ليلة $selectedDay (كل الأشهر)" to night,
                        "أعمال يوم $selectedDay (كل الأشهر)" to day
                    ).filter { it.second.isNotEmpty() }
                }
            2 -> {
                val name = weekDays.getOrNull(selectedWeekday).orEmpty()
                listOf(
                    "أعمال يوم $name" to weekdayDeeds(name)
                ).filter { it.second.isNotEmpty() }
            }
            else -> {
                val night = deedsToday.filter { it.source == DailyDeedSource.MONTH_NIGHT }
                val day = deedsToday.filter { it.source == DailyDeedSource.MONTH_DAY }
                val week = deedsToday.filter { it.source == DailyDeedSource.WEEKDAY }
                listOf(
                    "أعمال هذه الليلة (${night.size})" to night,
                    "أعمال اليوم (${day.size})" to day,
                    "أعمال يوم $weekdayName (${week.size})" to week
                ).filter { it.second.isNotEmpty() }
            }
        }

        if (groups.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    corner = 20.dp,
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            when (selectedTab) {
                                1 -> "لا توجد أعمال موقوتة لليوم $selectedDay في «مفاتيح الجنان»"
                                2 -> "لا توجد أعمال موقوتة ليوم ${weekDays.getOrNull(selectedWeekday)} في «مفاتيح الجنان»"
                                else -> "لا توجد أعمال موقوتة اليوم في «مفاتيح الجنان»"
                            },
                            color = KararColors.TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "جرّب يوم الخميس/الجمعة/السبت، أو الأشهر ذات الليالي المحفوظة (رمضان، شعبان، رجب…). للمزيد استخدم «مفاتيح الجنان».",
                            color = KararColors.TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        } else {
            groups.forEach { (title, deeds) ->
                item(span = { GridItemSpan(maxLineSpan) }) { SectionTitle(title) }
                items(deeds, key = { deed -> "${selectedTab}-${deed.source}-${deed.item.id}" }) { deed ->
                    DeedRow(
                        deed = deed,
                        isFavorite = deed.item.id in favorites,
                        onToggleFavorite = { toggleFavorite(deed.item.id) },
                        onOpen = { onOpenReader(deed.item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DeedRow(
    deed: DailyDeed,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onOpen: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(KararColors.CardSoft)
            .border(1.dp, KararColors.Border, RoundedCornerShape(16.dp))
            .clickable(onClick = onOpen)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(KararColors.Purple.copy(alpha = 0.13f))
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
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = deed.item.title,
                color = KararColors.TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp
            )
            if (deed.sectionTitle.isNotBlank()) {
                Text(
                    text = deed.sectionTitle,
                    color = KararColors.TextMuted,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
            contentDescription = if (isFavorite) "إزالة من المفضل" else "أضف إلى المفضل",
            tint = if (isFavorite) KararColors.Gold else KararColors.TextMuted,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .clickable(onClick = onToggleFavorite)
        )
    }
}
