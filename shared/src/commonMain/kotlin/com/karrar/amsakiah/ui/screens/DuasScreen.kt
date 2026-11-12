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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.asPaddingValues
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
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
                // القارئ الموحّد — نفس تصميم القرآن/الجنان: شريط عائم + إخفاء + ⚙،
                // واختفاء أشرطة التطبيق عبر الشل (immersive).
                val duaPlaying = player.currentId == item.id && player.playing
                UnifiedReaderScreen(
                    contentId = 0,
                    title = item.title,
                    subtitle = item.category,
                    onBack = { selectedId = null },
                    onTitleChange = { /* يُمرَّر للشل تلقائيًا عبر LaunchedEffect */ },
                    bodyText = item.arabicText.ifBlank { item.subtitle },
                    playerSlot = if (item.hasAudio) {
                        {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(KararColors.Gold)
                                    .clickable { player.play(item) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (duaPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (duaPlaying) "إيقاف مؤقت" else "تشغيل التسجيل",
                                    tint = KararColors.OnGold,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    } else {
                        null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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

    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = KararTokens.Size.duaCardMinWidth),
        modifier = Modifier
            .fillMaxWidth()
            .imeNestedScroll(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + bottomInset + 8.dp + imeBottom
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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

/**
 * بطاقة دعاء — عمود أيقونة دائري بلا مستطيلات:
 * دائرة زجاجية ٧٢dp تحيطها حدود ذهبية خفيفة، الأيقونة ٣٢dp،
 * والعنوان (سطران كحد أقصى، موسّط) تحتها، ثم الفئة بخط صغير.
 * زر التشغيل يجلس كشارة صغيرة أسفل يمين الدائرة عند وجود صوت.
 */
@Composable
private fun DuaCard(
    item: ShiaDuaItem,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isPreparing: Boolean,
    onClick: () -> Unit,
    onPlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            // الدائرة الزجاجية
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCurrent) KararColors.Gold.copy(alpha = 0.28f)
                        else KararColors.CardSoft
                    )
                    .then(
                        if (isCurrent) Modifier.border(2.dp, KararColors.Gold, CircleShape)
                        else Modifier.border(1.dp, KararColors.Border, CircleShape)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = if (isCurrent) KararColors.Gold else KararColors.TextPrimary.copy(alpha = 0.9f),
                    modifier = Modifier.size(32.dp)
                )
            }
            // زر التشغيل الصوتي كشارة على الدائرة
            if (item.hasAudio) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCurrent) KararColors.Gold else KararColors.CardSoft
                        )
                        .border(1.dp, KararColors.Border, CircleShape)
                        .clickable(onClick = onPlay),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                        tint = if (isCurrent) KararColors.OnGold else KararColors.Gold,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = item.title,
            color = if (isCurrent) KararColors.Gold else KararColors.TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 15.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = item.category,
            color = KararColors.TextMuted,
            fontSize = 8.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
