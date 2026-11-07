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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.data.ShiaDuaItem
import com.karrar.amsakiah.core.data.ShiaDuasLibrary
import com.karrar.amsakiah.ui.components.DuaFloatingPlayer
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.components.SectionTitle
import com.karrar.amsakiah.ui.player.DuaPlayerController
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
    val player = remember { DuaPlayerController() }

    // إيقاف الصوت وتحرير المشغّل الأصلي عند مغادرة قسم الأدعية
    DisposableEffect(Unit) {
        onDispose { player.release() }
    }

    PlatformBackHandler(enabled = selected != null) { selectedId = null }

    Box(Modifier.fillMaxSize()) {
        Crossfade(
            targetState = selected,
            animationSpec = tween(220),
            label = "dua-navigation"
        ) { item ->
            if (item == null) {
                DuaList(
                    contentPadding = contentPadding,
                    bottomInset = if (player.hasTrack) 118.dp else 0.dp,
                    onSelect = { selectedId = it.id },
                    onPlay = { player.play(it) },
                    player = player
                )
            } else {
                DuaDetailScreen(
                    item = item,
                    contentPadding = contentPadding,
                    player = player,
                    onBack = { selectedId = null }
                )
            }
        }

        // المشغل العائم البيضوي — يبقى فوق المحتوى
        DuaFloatingPlayer(
            controller = player,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp)
                .padding(bottom = contentPadding.calculateBottomPadding() + 10.dp)
        )
    }
}

@Composable
private fun DuaList(
    contentPadding: PaddingValues,
    bottomInset: Dp,
    onSelect: (ShiaDuaItem) -> Unit,
    onPlay: (ShiaDuaItem) -> Unit,
    player: DuaPlayerController
) {
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
            bottom = contentPadding.calculateBottomPadding() + bottomInset + 8.dp
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
            DuaCard(
                item = item,
                isCurrent = player.currentId == item.id,
                isPlaying = player.currentId == item.id && player.playing,
                isPreparing = player.currentId == item.id && player.durationMs <= 0L,
                onClick = { onSelect(item) },
                onPlay = { onPlay(item) }
            )
        }
    }
}

@Composable
private fun DuaCard(
    item: ShiaDuaItem,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isPreparing: Boolean,
    onClick: () -> Unit,
    onPlay: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        corner = 20.dp,
        contentPadding = PaddingValues(14.dp),
        color = if (isCurrent) Color(0x1AFFD700) else KararColors.CardSoft,
        borderColor = if (isCurrent) Color(0x4DFFD700) else KararColors.Border
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
                        .size(38.dp)
                        .background(
                            if (isCurrent) KararColors.Gold else Color(0x33FFD700),
                            CircleShape
                        )
                        .clickable(onClick = onPlay),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            isPreparing -> Icons.Filled.PlayArrow
                            isPlaying -> Icons.Filled.Pause
                            else -> Icons.Filled.PlayArrow
                        },
                        contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل التسجيل",
                        tint = if (isCurrent) Color(0xFF241A00) else KararColors.Gold,
                        modifier = Modifier.size(21.dp)
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
