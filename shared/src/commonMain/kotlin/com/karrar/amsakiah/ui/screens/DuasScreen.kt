package com.karrar.amsakiah.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.data.ShiaDuaItem
import com.karrar.amsakiah.core.data.ShiaDuasLibrary
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.components.SectionTitle
import com.karrar.amsakiah.ui.platform.PlatformBackHandler
import com.karrar.amsakiah.ui.theme.KararColors

/**
 * قسم الأدعية:
 * - فهرس قابل للبحث والتصنيف.
 * - كل دعاء يُفتح في شاشة مستقلة عبر معرّفه (id) لا في نافذة منبثقة.
 * - زر/إيماءة الرجوع تعيد إلى الفهرس (مع دعم Predictive Back).
 */
@Composable
fun DuasScreen(contentPadding: PaddingValues) {
    var selectedId by remember { mutableStateOf<String?>(null) }
    val selected = ShiaDuasLibrary.byId(selectedId)

    PlatformBackHandler(enabled = selected != null) { selectedId = null }

    Crossfade(
        targetState = selected,
        animationSpec = tween(220),
        label = "dua-navigation"
    ) { item ->
        if (item == null) {
            DuaList(contentPadding = contentPadding, onSelect = { selectedId = it.id })
        } else {
            DuaDetailScreen(
                item = item,
                contentPadding = contentPadding,
                onBack = { selectedId = null }
            )
        }
    }
}

@Composable
private fun DuaList(contentPadding: PaddingValues, onSelect: (ShiaDuaItem) -> Unit) {
    val all = remember { ShiaDuasLibrary.duasList }
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf<String?>(null) }

    val categories = remember { all.map { it.category }.distinct() }
    val filtered = remember(query, category) {
        all.filter { item ->
            (category == null || item.category == category) &&
                (query.isBlank() || item.title.contains(query) || item.arabicText.contains(query))
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("ابحث في الأدعية والزيارات...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = KararColors.Gold) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    CategoryChip("الكل", category == null) { category = null }
                }
                items(categories) { cat ->
                    CategoryChip(cat, category == cat) { category = if (category == cat) null else cat }
                }
            }
        }

        item { SectionTitle("الأدعية والمناجاة (${filtered.size})") }

        items(filtered, key = { it.id }) { item ->
            DuaCard(item = item, onClick = { onSelect(item) })
        }
    }
}

@Composable
private fun DuaCard(item: ShiaDuaItem, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        corner = 20.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.title, color = KararColors.Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                if (item.subtitle.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(item.subtitle, color = KararColors.TextSecondary, fontSize = 11.sp, maxLines = 2)
                }
            }
            if (item.hasAudio) {
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier
                        .size(34.dp)
                        .background(Color(0x33FFD700), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "يحتوي تسجيل صوتي",
                        tint = KararColors.Gold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(item.category, color = KararColors.Purple, fontSize = 10.sp)
            Text("•", color = KararColors.TextMuted, fontSize = 10.sp)
            Text(item.recommendedTime, color = KararColors.TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun CategoryChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .background(
                if (selected) KararColors.Gold else Color(0x14FFFFFF),
                RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text,
            color = if (selected) Color(0xFF241A00) else KararColors.TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
