package com.karrar.amsakiah.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.data.ShiaDuaItem
import com.karrar.amsakiah.core.data.ShiaDuasLibrary
import com.karrar.amsakiah.ui.components.SectionTitle
import com.karrar.amsakiah.ui.player.DuaPlayerController
import com.karrar.amsakiah.ui.platform.PlatformBackHandler
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararTokens

/**
 * قسم الأدعية:
 * - فهرس قابل للبحث والتصنيف.
 * - كل دعاء يُفتح في شاشة مستقلة عبر معرّفه (id) لا في نافذة منبثقة.
 * - زر/إيماءة الرجوع تعيد إلى الفهرس (مع دعم Predictive Back).
 */
@Composable
fun DuasScreen(
    contentPadding: PaddingValues,
    player: DuaPlayerController,
    backSignal: Int = 0,
    onDetailVisibilityChanged: (Boolean) -> Unit = {},
    onDetailTitleChanged: (String?) -> Unit = {}
) {
    var selectedId by remember { mutableStateOf<String?>(null) }
    val selected = ShiaDuasLibrary.byId(selectedId)

    // الشل يعرض اسم الدعاء في شريط العنوان الموحّد نفسه (لا شريط ثانٍ).
    LaunchedEffect(selected?.id) {
        onDetailTitleChanged(selected?.title)
        onDetailVisibilityChanged(selected != null)
    }

    // زر الرجوع في الشريط الموحّد يغلق التفاصيل ويعيد الفهرس.
    LaunchedEffect(backSignal) {
        if (backSignal > 0) selectedId = null
    }

    PlatformBackHandler(enabled = selected != null) { selectedId = null }

    Box(Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = selected,
            transitionSpec = {
                if (targetState != null) {
                    // دخول شاشة الدعاء: انزلاق خفيف من جهة البداية + تلاشٍ نابض.
                    (
                        slideInHorizontally(animationSpec = KararTokens.Motion.spatial()) { it / 4 } +
                            fadeIn(animationSpec = KararTokens.Motion.effects())
                        ).togetherWith(
                        slideOutHorizontally(animationSpec = KararTokens.Motion.spatial()) { -it / 6 } +
                            fadeOut(animationSpec = KararTokens.Motion.effects())
                        )
                } else {
                    // العودة إلى الفهرس: عكس الاتجاه.
                    (
                        slideInHorizontally(animationSpec = KararTokens.Motion.spatial()) { -it / 6 } +
                            fadeIn(animationSpec = KararTokens.Motion.effects())
                        ).togetherWith(
                        slideOutHorizontally(animationSpec = KararTokens.Motion.spatial()) { it / 4 } +
                            fadeOut(animationSpec = KararTokens.Motion.effects())
                        )
                }
            },
            label = "dua-navigation"
        ) { item ->
            if (item == null) {
                DuaList(
                    contentPadding = contentPadding,
                    bottomInset = if (player.hasTrack) KararTokens.Size.playerDockHeight else 0.dp,
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

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = KararTokens.Size.duaCardMinWidth),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + bottomInset + 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
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

        item(span = { GridItemSpan(maxLineSpan) }) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    CategoryChip("الكل", category == null) { category = null }
                }
                items(categories) { cat ->
                    CategoryChip(cat, category == cat) { category = if (category == cat) null else cat }
                }
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionTitle("الأدعية والمناجاة (${filtered.size})")
        }

        items(
            count = filtered.size,
            key = { index -> filtered[index].id }
        ) { index ->
            val item = filtered[index]
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
    // Edge-to-edge: لا كتل لونية. الصف شفاف على خلفية الشاشة نفسها،
    // والفاصل خط شعري متلاشٍ الأطراف (كما في مرجع amsak3d).
    val border = KararColors.Border
    val glow = KararColors.Gold.copy(alpha = 0.12f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (isCurrent) Modifier.background(glow, RoundedCornerShape(14.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .drawBehind {
                val y = size.height - 0.5.dp.toPx()
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(Color.Transparent, border, Color.Transparent)
                    ),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(horizontal = 6.dp, vertical = 13.dp)
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
                            if (isCurrent) KararColors.Gold else KararColors.CardSoft,
                            CircleShape
                        )
                        .border(1.dp, KararColors.Border, CircleShape)
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
                        tint = if (isCurrent) KararColors.OnGold else KararColors.Gold,
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
                if (selected) KararColors.Gold else KararColors.CardSoft,
                RoundedCornerShape(50)
            )
            .border(1.dp, KararColors.Border, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text,
            color = if (selected) KararColors.OnGold else KararColors.TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
