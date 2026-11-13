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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.karrar.amsakiah.core.state.AppState
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.components.SectionTitle
import com.karrar.amsakiah.ui.theme.KararColors

/**
 * «أعمال اليوم» — نافذة ديناميكية ذكية من «مفاتيح الجنان».
 *
 * لا اختيار يدوي: كل شيء يُحسم تلقائيًا من التقويم المدني ومدينتك ووعد القوس القمري
 * (الهجري المقابل)، ثم يُطابَق مع تمام النصوص الموجودة «مفاتيح الجنان»:
 * الليلة (منذ مساء أمس) ← اليوم الهجري ← يوم الأسبوع ← أعمال عامة الشهر.
 *
 * الثلاثاء … "أعمال اليوم الخميس 12 جمادى الأولى 1447" يضيء بخط مصغّر فوق الكارتات،
 * والكروت كلها تفتح القارئ الموحّد على النص/الفصل المطلوب.
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

    // المطابقة الصارمة — تاريخ اليوم الهجري فقط (لا رمضان ولا أيام من أشهر أخرى).
    val deeds = remember(today, hijri) { dailyDeeds(today, hijri) }
    val night = deeds.filter { it.source == DailyDeedSource.MONTH_NIGHT }
    val day = deeds.filter { it.source == DailyDeedSource.MONTH_DAY }
    val week = deeds.filter { it.source == DailyDeedSource.WEEKDAY }
    val groups = listOf(
        "أعمال هذه الليلة (${night.size})" to night,
        "أعمال اليوم (${day.size})" to day,
        "أعمال يوم $weekdayName (${week.size})" to week
    ).filter { it.second.isNotEmpty() }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 165.dp),
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 10.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ---------- الكروت (العنوان في الشريط العلوي فقط — بلا تكرار) ----------
        if (groups.isEmpty()) {
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
                            text = "جرّب يوم الخميس/الجمعة/السبت، أو الأشهر ذات الليالي المحفوظة (رمضان، شعبان، رجب…).",
                            color = KararColors.TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        } else {
            groups.forEach { (title, groupDeeds) ->
                item(span = { GridItemSpan(maxLineSpan) }) { SectionTitle(title) }
                items(groupDeeds, key = { deed -> "${deed.source}-${deed.item.id}" }) { deed ->
                    DeedCard(
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

/**
 * كارت العمل — يحاكي بطاقات «المفاتيح»: أيقونة + عنوان العمل فقط (بلا تفاصيل).
 * بالضغط يفتح «القارئ الموحّد» على الفصل/العمل المطلوب.
 */
@Composable
private fun DeedCard(
    deed: DailyDeed,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onOpen: () -> Unit
) {
    GlassCard(
        corner = 20.dp,
        color = KararColors.CardSoft,
        borderColor = KararColors.Border,
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = if (isFavorite) "إزالة من المفضل" else "أضف إلى المفضل",
                    tint = if (isFavorite) KararColors.Gold else KararColors.TextMuted,
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onToggleFavorite)
                )
            }
            Text(
                text = deed.item.title,
                color = KararColors.TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp,
                minLines = 3,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
