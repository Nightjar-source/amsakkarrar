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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Search
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
import com.karrar.amsakiah.core.jinan.JinanContentData
import com.karrar.amsakiah.core.jinan.JinanItem
import com.karrar.amsakiah.ui.theme.KararColors

@Composable
internal fun JinanDetailPlaceholder() {
    // تُعرض حين لا يوجد نص مفتوح بعد (الشاشات اللوحية/المتوسطة).
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.Key,
                contentDescription = null,
                tint = KararColors.Gold.copy(alpha = 0.85f),
                modifier = Modifier.size(54.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "اختر نصًا من «مفاتيح الجنان»",
                color = KararColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "تظهر الأدعية والصلوات والزيارات مباشرة في هذه اللوحة.",
                color = KararColors.TextMuted,
                fontSize = 11.sp,
                lineHeight = 17.sp
            )
        }
    }
}

/**
 * شاشة «مفاتيح الجنان» — فهرس الأجزاء والجدران والأدعية من مصباح المتهجّد
 * وكامل مفاتيح الجنان بترقيم مرجعي.
 *
 * - لا يدخل نص JSON الكامل (2.9MB) في الـCompose graph؛ الفهرس يُحمل مرة واحدة (`by lazy`).
 * - في الشاشات المتوسطة والعريضة/المطويات تعمل الشاشة بجانب القارئ بوصفها
 *   «القائمة» في مشهد List-Detail الرسمي التابع لـNavigation 3 + Adaptive.
 */
@Composable
fun JinanScreen(
    contentPadding: PaddingValues,
    onOpenReader: (jinanItem: JinanItem) -> Unit
) {
    var query by remember { mutableStateOf("") }

    // فهرس كامل — يُسترجع من الذاكرة (by lazy) فلا تقلب Composition عن عمد.
    val parts = remember { JinanContentData.bookParts }

    // مفلترة سريعة — لا تنتظر التحميل (الـby lazy يحمل على الخيط الخلفي أولًا).
    val filteredParts = remember(query) {
        if (query.isBlank()) parts
        else {
            val q = query.trim().lowercase()
            parts.mapNotNull { part ->
                val matchingChapters = part.chapters.mapNotNull { chapter ->
                    val matchingSections = chapter.sections.mapNotNull { section ->
                        val matchingItems = section.items.filter { item ->
                            item.title.lowercase().contains(q)
                        }
                        if (matchingItems.isNotEmpty()) section.copy(items = matchingItems) else null
                    }
                    if (matchingSections.isNotEmpty()) chapter.copy(sections = matchingSections) else null
                }
                if (matchingChapters.isNotEmpty()) part.copy(chapters = matchingChapters) else null
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 10.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "مفاتيح الجنان",
                            color = KararColors.TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "مصباح المتهجّد والجُزءان الكاملان — أدعية وشهرية وأيام وزيارات",
                            color = KararColors.TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = KararColors.Gold.copy(alpha = 0.9f),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            // سطر بحث أزق — بسيط، يعمل أوفلاين.
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, KararColors.Border, RoundedCornerShape(20.dp))
                        .background(KararColors.CardSoft)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = KararColors.TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            filteredParts.forEach { part ->
                item(key = "part:${part.id}") {
                    JinanPartHeader(part.title, itemsCount = part.chapters.sumOf { c -> c.sections.sumOf { s -> s.items.size } })
                }
                part.chapters.forEach { chapter ->
                    item(key = "chapter:${chapter.id}") {
                        JinanChapterHeader(chapter.title, itemsCount = chapter.sections.sumOf { s -> s.items.size })
                    }
                    chapter.sections.forEach { section ->
                        item(key = "section:${section.id}") {
                            JinanSectionHeader(section.title, itemsCount = section.items.size)
                        }
                        items(items = section.items, key = { item -> "jinanItem:${item.id}" }) { item ->
                            JinanItemRow(item) { onOpenReader(item) }
                        }
                    }
                }
            }

            // تحذيرات صغيرة ختامية
            item {
                Text(
                    "المحتوى أوفلاين بالكامل من ملف content_texts.json (آيات ومناجاة) — بدون طلبات شبكة.",
                    color = KararColors.TextMuted,
                    fontSize = 9.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
private fun JinanPartHeader(title: String, itemsCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(KararColors.Gold)
                .height(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            color = KararColors.Gold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "$itemsCount",
            color = KararColors.TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun JinanChapterHeader(title: String, itemsCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = KararColors.TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "$itemsCount",
            color = KararColors.TextMuted,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun JinanSectionHeader(title: String, itemsCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = KararColors.TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "$itemsCount",
            color = KararColors.TextMuted,
            fontSize = 9.sp
        )
    }
}

@Composable
private fun JinanItemRow(item: JinanItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(KararColors.Teal.copy(alpha = 0.8f))
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = item.title,
            color = KararColors.TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            lineHeight = 16.sp
        )
    }
}
