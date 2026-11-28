package com.karrar.amsakiah.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.ui.state.AppState
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.navigation.LocalSearchQuery
import com.karrar.amsakiah.ui.motion.effectsSpec
import com.karrar.amsakiah.ui.motion.spatialSpec
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import com.karrar.amsakiah.ui.platform.PlatformBackHandler
import com.karrar.amsakiah.ui.platform.rememberReduceMotion
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararFonts
import com.karrar.amsakiah.ui.theme.LocalKararPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * القرآن الكريم — المصحف الشريف:
 * - فهرس 114 سورة محلي (يعمل أوفلاين بالكامل — الأسماء/الأعداد/النوع).
 * - عند فتح سورة يُجلب نصها من مصدر عام (alquran.cloud) **عند الحاجة فقط**؛
 *   فشل الجلب لا يُعطّل التطبيق: تُعرض رسالة محترمة مع زر «إعادة المحاولة».
 */
@Composable
fun QuranScreen(
    state: AppState,
    contentPadding: PaddingValues,
    onReaderVisibilityChanged: (Boolean) -> Unit = {}
) {
    var opened by remember { mutableStateOf<SurahInfo?>(null) }

    // معالج الرجوع المحلي: عند فتح سورة، الرجوع يعود لفهرس السور أولاً ولا يخرج لليوم
    PlatformBackHandler(enabled = opened != null) {
        opened = null
    }

    LaunchedEffect(opened != null) { onReaderVisibilityChanged(opened != null) }

    val reduceMotion = rememberReduceMotion()

    // في وضع القراءة: يُلغى padding الشل (الذي كان يترك شريطَي الحالة/التنقل
    // بلون مختلف خلف القارئ ويكسر full-bleed) — القارئ يرسم خلفيته بنفسه إلى كل
    // الحواف ويحترم Inset الحالة داخليًا. فهرس السور يبقى بإزاحة الشل كما هي.
    val readerOpen = opened != null

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Container-Transform-like للقارئ: الداخلة تنزلق من تحت + تلاشٍ بنابض
        // المكاني الشبيه، والخارجة تنزلق وتعيد نفسها؛ بلا شفافية صفرية للداخلة.
        androidx.compose.animation.AnimatedContent(
            targetState = opened,
            transitionSpec = {
                val spatial = spatialSpec<androidx.compose.ui.unit.IntOffset>(reduceMotion)
                val effects = effectsSpec<Float>(reduceMotion)
                (
                    slideInVertically(animationSpec = spatial) { full -> full / 5 } +
                        fadeIn(animationSpec = effects)
                    ).togetherWith(
                        slideOutVertically(animationSpec = spatial) { full -> full / 5 } +
                            fadeOut(animationSpec = effects)
                    )
            },
            label = "quran-reader-transform"
        ) { surah ->
            when (surah) {
                null -> QuranIndex(contentPadding = contentPadding, onOpen = { opened = it })
                else -> QuranReader(
                    state = state,
                    surah = surah,
                    onClose = { opened = null }
                )
            }
        }
    }
}

/** تطبيع الأرقام العربية/الفارسية إلى لاتينية حتى يعمل البحث بالرقم من أي لوحة مفاتيح. */
private fun normalizeDigits(input: String): String = buildString(input.length) {
    input.forEach { ch ->
        when (ch) {
            in '٠'..'٩' -> append('0' + (ch - '٠'))
            in '۰'..'۹' -> append('0' + (ch - '۰'))
            else -> append(ch)
        }
    }
}

@Composable
private fun QuranIndex(contentPadding: PaddingValues, onOpen: (SurahInfo) -> Unit) {
    val query = LocalSearchQuery.current.value
    val layoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
    val filtered = remember(query) {
        val q = normalizeDigits(query.trim())
        if (q.isBlank()) SURAHS
        else SURAHS.filter { surah ->
            surah.name.contains(q, ignoreCase = true) ||
                surah.number.toString() == q
        }
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 280.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp + contentPadding.calculateStartPadding(layoutDirection),
            end = 16.dp + contentPadding.calculateEndPadding(layoutDirection),
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (filtered.isEmpty()) {
            item {
                Text(
                    "لا توجد سورة بهذا الاسم — جرّب البحث بالاسم أو الرقم.",
                    color = KararColors.TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 32.dp, horizontal = 8.dp)
                )
            }
        }
        items(filtered, key = { it.number }) { surah ->
            RowSurah(surah, onOpen)
        }
    }
}

@Composable
private fun RowSurah(surah: SurahInfo, onOpen: (SurahInfo) -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth().clickable { onOpen(surah) },
        corner = 18.dp,
        color = KararColors.CardSoft,
        borderColor = KararColors.Border,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // رقم السورة داخل دائرة ذهبية — لا تظهر أعداد غريبة عند تغيير الثيم.
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(KararColors.Gold.copy(alpha = 0.16f))
                    .border(1.dp, KararColors.Gold.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = surah.number.toString(),
                    color = KararColors.Gold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = surah.name,
                    color = KararColors.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${surah.revelation} · ${surah.verses} آية",
                    color = KararColors.TextMuted,
                    fontSize = 10.sp
                )
            }
            Text(
                "‹",
                color = KararColors.TextMuted,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp
            )
        }
    }
}

/**
 * قارئ السورة — أوفلاين بالكامل: نص السورة محلي (`files/quran_texts.json`)،
 * ولا يُجلب أي شيء من الشبكة. يُعرض عبر القارئ الموحّد نفسه (خيارات + إخفاء).
 */
@Composable
private fun QuranReader(state: AppState, surah: SurahInfo, onClose: () -> Unit) {
    var body by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(surah.number) {
        // تحميل مرة واحدة لكل عملية (≈750KB) — على خيط خلفي.
        withContext(Dispatchers.Default) {
            if (!com.karrar.amsakiah.core.quran.QuranTexts.isLoaded) {
                runCatching {
                    com.karrar.amsakiah.resources.Res.readBytes("files/quran_texts.json")
                }.getOrNull()?.let { com.karrar.amsakiah.core.quran.QuranTexts.load(it) }
            }
        }
        val loaded = com.karrar.amsakiah.core.quran.QuranTexts.bySurah(surah.number)
        body = loaded
    }

    val text = body ?: run {
        // خلفية مصمتة من اللوحة نفسها — حتى لا تلمع خلفية الشل خلف رسالة التحميل
        Box(
            Modifier
                .fillMaxSize()
                .background(LocalKararPalette.current.surface),
            contentAlignment = Alignment.Center
        ) {
            Text("جارٍ فتح المصحف…", color = KararColors.TextMuted, fontSize = 12.sp)
        }
        return
    }

    UnifiedReaderScreen(
        contentId = 1000 + surah.number,
        title = "سورة ${surah.name}",
        subtitle = "${surah.revelation} · ${surah.verses} آية",
        onBack = onClose,
        bodyText = remember(text) { formatSurahBody(surah.number, text) },
        isFavorite = 1000 + surah.number in state.settings.favoriteJinanItems,
        onToggleFavorite = {
            state.update { s ->
                val id = 1000 + surah.number
                val next = if (id in s.favoriteJinanItems) s.favoriteJinanItems - id else s.favoriteJinanItems + id
                s.copy(favoriteJinanItems = next)
            }
        },
        onSaveNote = { note ->
            state.update { s -> s.copy(notes = s.notes + note) }
            state.showToast("حُفظت في «المهام والملاحظات»", com.karrar.amsakiah.ui.state.ToastKind.SUCCESS)
        }
    )
}

/**
 * ينسق نص السورة المباركة كنص مصحفي مستمر متدفق:
 * الآيات متصلة مع رموز الآيات ۝ ورقمها مع ضبط النص، وبسملة مستقلة في الصدر.
 */
private fun formatSurahBody(number: Int, raw: String): String {
    val marked = Regex("""﴿(\d+)﴾""").replace(raw) { m ->
        val ayahNum = m.groupValues[1]
        " ﴿$ayahNum﴾ "
    }
    val clean = marked.replace(Regex("""\s+"""), " ").trim()
    return if (number != 1 && number != 9) {
        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\n\n$clean"
    } else {
        clean
    }
}

