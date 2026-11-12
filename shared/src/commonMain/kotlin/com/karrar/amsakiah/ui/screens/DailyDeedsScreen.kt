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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.jinan.DailyDeedSource
import com.karrar.amsakiah.core.jinan.JinanItem
import com.karrar.amsakiah.core.jinan.dailyDeeds
import com.karrar.amsakiah.core.state.AppState
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.components.SectionTitle
import com.karrar.amsakiah.ui.theme.KararColors

/**
 * «أعمال اليوم» — معروض حي من «مفاتيح الجنان»: اليوم الهجري + أعمال الليلة/اليوم/يوم الأسبوع.
 *
 * الأقسام الثلاثة تُملأ تلقائيًا من `JinanContentData` بمطابقة (رقم اليوم الهجري + اسم الشهر + اسم اليوم)،
 * والضغط على أي عمل يفتح القارئ الموحّد. نجمة ⭐ تجعل العمل يظهر في شاشة «أعمال اليوم (المهام)».
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
    val deeds = remember(today, hijri) { dailyDeeds(today, hijri) }
    val weekdayName = PrayerCalculator.weekDays.getOrNull(today.dayOfWeek.ordinal).orEmpty()
    val favorites = settings.favoriteJinanItems

    val nightDeeds = deeds.filter { it.source == DailyDeedSource.MONTH_NIGHT }
    val dayDeeds = deeds.filter { it.source == DailyDeedSource.MONTH_DAY }
    val weekdayDeeds = deeds.filter { it.source == DailyDeedSource.WEEKDAY }

    // شبكة تكيّفية رسمية (Adaptive): عمود واحد على COMPACT، عمودان MEDIUM+ —
    // بطاقات الأعمال لا تتقلص؛ تتسع فقط حين تتوفر مساحة كافية.
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

        if (deeds.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    corner = 20.dp,
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "لا توجد أعمال موقوتة اليوم في «مفاتيح الجنان»",
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
        }

        // ---------- أعمال هذه الليلة ----------
        if (nightDeeds.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) { SectionTitle("أعمال هذه الليلة (${nightDeeds.size})") }
            items(nightDeeds, key = { "n-${it.item.id}" }) { deed ->
                DeedRow(
                    deed = deed,
                    isFavorite = deed.item.id in favorites,
                    onToggleFavorite = {
                        state.update { s ->
                            val next = if (deed.item.id in s.favoriteJinanItems) {
                                s.favoriteJinanItems - deed.item.id
                            } else {
                                s.favoriteJinanItems + deed.item.id
                            }
                            s.copy(favoriteJinanItems = next)
                        }
                    },
                    onOpen = { onOpenReader(deed.item) }
                )
            }
        }

        // ---------- أعمال اليوم ----------
        if (dayDeeds.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) { SectionTitle("أعمال اليوم (${dayDeeds.size})") }
            items(dayDeeds, key = { "d-${it.item.id}" }) { deed ->
                DeedRow(
                    deed = deed,
                    isFavorite = deed.item.id in favorites,
                    onToggleFavorite = {
                        state.update { s ->
                            val next = if (deed.item.id in s.favoriteJinanItems) {
                                s.favoriteJinanItems - deed.item.id
                            } else {
                                s.favoriteJinanItems + deed.item.id
                            }
                            s.copy(favoriteJinanItems = next)
                        }
                    },
                    onOpen = { onOpenReader(deed.item) }
                )
            }
        }

        // ---------- أعمال يوم الأسبوع ----------
        if (weekdayDeeds.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) { SectionTitle("أعمال يوم $weekdayName (${weekdayDeeds.size})") }
            items(weekdayDeeds, key = { "w-${it.item.id}" }) { deed ->
                DeedRow(
                    deed = deed,
                    isFavorite = deed.item.id in favorites,
                    onToggleFavorite = {
                        state.update { s ->
                            val next = if (deed.item.id in s.favoriteJinanItems) {
                                s.favoriteJinanItems - deed.item.id
                            } else {
                                s.favoriteJinanItems + deed.item.id
                            }
                            s.copy(favoriteJinanItems = next)
                        }
                    },
                    onOpen = { onOpenReader(deed.item) }
                )
            }
        }
    }
}

@Composable
private fun DeedRow(
    deed: com.karrar.amsakiah.core.jinan.DailyDeed,
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

