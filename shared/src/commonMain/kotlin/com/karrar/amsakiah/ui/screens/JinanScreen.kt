package com.karrar.amsakiah.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.jinan.JinanContentData
import com.karrar.amsakiah.core.jinan.JinanItem
import com.karrar.amsakiah.ui.navigation.LocalSearchQuery
import com.karrar.amsakiah.ui.theme.KararColors

/**
 * شاشة «مفاتيح الجنان» — أبواب وفصول كأنها «أبواب» تُطوى وتُفتح:
 * - العنوان في الشريط العلوي الموحّد فقط (لا تكرار داخل الشاشة).
 * - موضع «البحث» في شريط العنوان (دائرة) — تقرأه هذه الشاشة عبر [LocalSearchQuery].
 * - الباب (باب/فصل/قسم) = صف عنوان + شارة عدد + سهم يتدوّر عند الطيّ.
 * - حالة الطيّ تُحفظ عبر [rememberSaveable] — تبقى بعد التنقل وإعادة التشغيل.
 */
@Composable
fun JinanScreen(
    contentPadding: PaddingValues,
    onOpenReader: (jinanItem: JinanItem) -> Unit
) {
    val query = LocalSearchQuery.current.value

    // فهرس كامل — يُسترجع من الذاكرة (by lazy) فلا تقلق Composition عن عمد.
    val parts = remember { JinanContentData.bookParts }

    // حالة الطيّ محفوظة: كل «باب» مطوي يُسجَّل بمعرّفه — تبقى بعد التنقل والإرثاء.
    val collapsedIds = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { mutableStateListOf<String>().apply { addAll(it) } }
        )
    ) { mutableStateListOf<String>() }

    fun toggle(id: String) {
        if (id in collapsedIds) collapsedIds.remove(id) else collapsedIds.add(id)
    }

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

    // عند البحث: كل «الأبواب» تُفتح تلقائيًا لتظهر النتائج.
    val isSearching = query.isNotBlank()

    Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 10.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filteredParts.forEach { part ->
                val partId = "part:${part.id}"
                val partCount = part.chapters.sumOf { c -> c.sections.sumOf { s -> s.items.size } }
                val partCollapsed = !isSearching && partId in collapsedIds

                item(key = partId) {
                    ExpandableHeader(
                        title = part.title,
                        count = partCount,
                        level = HeaderLevel.PART,
                        collapsed = partCollapsed,
                        onToggle = { toggle(partId) }
                    )
                }
                item(key = "body:$partId") {
                    AnimatedVisibility(
                        visible = !partCollapsed,
                        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                        label = "part-$partId"
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            part.chapters.forEach { chapter ->
                                val chapterId = "chapter:${chapter.id}"
                                val chapterCount = chapter.sections.sumOf { s -> s.items.size }
                                val chapterCollapsed = !isSearching && chapterId in collapsedIds

                                ExpandableHeader(
                                    title = chapter.title,
                                    count = chapterCount,
                                    level = HeaderLevel.CHAPTER,
                                    collapsed = chapterCollapsed,
                                    onToggle = { toggle(chapterId) }
                                )
                                AnimatedVisibility(
                                    visible = !chapterCollapsed,
                                    enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                                    exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                                    label = "chapter-$chapterId"
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        chapter.sections.forEach { section ->
                                            val sectionId = "section:${section.id}"
                                            val sectionCollapsed = !isSearching && sectionId in collapsedIds

                                            ExpandableHeader(
                                                title = section.title,
                                                count = section.items.size,
                                                level = HeaderLevel.SECTION,
                                                collapsed = sectionCollapsed,
                                                onToggle = { toggle(sectionId) }
                                            )
                                            AnimatedVisibility(
                                                visible = !sectionCollapsed,
                                                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                                                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                                                label = "section-$sectionId"
                                            ) {
                                                Column {
                                                    section.items.forEach { item ->
                                                        JinanItemRow(item) { onOpenReader(item) }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (filteredParts.isEmpty()) {
                item {
                    Text(
                        "لا توجد نتائج لـ«$query» — جرّب البحث بكلمات أخرى.",
                        color = KararColors.TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 32.dp, horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

/** مستوى العنوان: باب / فصل / قسم. */
private enum class HeaderLevel { PART, CHAPTER, SECTION }

/** رأس «باب» — يُطوى/يُفتح بالنقر، ويحمل شارة عدد العناصر والسهم يتدوّر. */
@Composable
private fun ExpandableHeader(
    title: String,
    count: Int,
    level: HeaderLevel,
    collapsed: Boolean,
    onToggle: () -> Unit
) {
    val (titleSize, titleWeight, titleColor) = when (level) {
        HeaderLevel.PART -> Triple(12.sp, FontWeight.Black, KararColors.Gold)
        HeaderLevel.CHAPTER -> Triple(11.sp, FontWeight.Bold, KararColors.TextPrimary)
        HeaderLevel.SECTION -> Triple(10.sp, FontWeight.Bold, KararColors.TextSecondary)
    }
    val arrowRotation by animateFloatAsState(
        targetValue = if (collapsed) 180f else 0f,
        label = "jinan-arrow"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (level == HeaderLevel.PART) KararColors.CardSoft else androidx.compose.ui.graphics.Color.Transparent
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = if (level == HeaderLevel.PART) 10.dp else 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (level == HeaderLevel.PART) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(KararColors.Gold)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = title,
            color = titleColor,
            fontSize = titleSize,
            fontWeight = titleWeight,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$count",
            color = KararColors.TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = if (collapsed) "فتح" else "طيّ",
            tint = KararColors.TextMuted,
            modifier = Modifier
                .size(18.dp)
                .graphicsLayer { rotationZ = arrowRotation }
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

/**
 * تُعرض حين لا يوجد نص مفتوح بعد (الشاشات اللوحية/المتوسطة) — اللوحة اليمنى
 * من مشهد «القائمة-التفصيل» تبقى فارغة حتى يختار المستخدم نصًا.
 */
@Composable
internal fun JinanDetailPlaceholder() {
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
