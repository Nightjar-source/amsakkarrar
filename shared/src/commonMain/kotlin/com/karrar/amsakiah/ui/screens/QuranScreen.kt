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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.ui.state.AppState
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.navigation.LocalSearchQuery
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararFonts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * القرآن الكريم — المصحف الشريف:
 * - فهرس 114 سورة محلي (يعمل أوفلاين بالكامل — الأسماء/الأعداد/النوع).
 * - عند فتح سورة يُجلب نصها من مصدر عام (alquran.cloud) **عند الحاجة فقط**؛
 *   فشل الجلب لا يُعطّل التطبيق: تُعرض رسالة محترمة مع زر «إعادة المحاولة».
 */
@Composable
fun QuranScreen(state: AppState, contentPadding: PaddingValues) {
    var opened by remember { mutableStateOf<SurahInfo?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        when (val surah = opened) {
            null -> QuranIndex(onOpen = { opened = it })
            else -> QuranReader(
                surah = surah,
                onClose = { opened = null }
            )
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
private fun QuranIndex(onOpen: (SurahInfo) -> Unit) {
    val query = LocalSearchQuery.current.value
    val filtered = remember(query) {
        val q = normalizeDigits(query.trim())
        if (q.isBlank()) SURAHS
        else SURAHS.filter { surah ->
            surah.name.contains(q, ignoreCase = true) ||
                surah.number.toString() == q
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
        itemsIndexed(filtered, key = { _, s -> s.number }) { _, surah ->
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
                    fontWeight = FontWeight.Black
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
            Text("‹", color = KararColors.TextMuted, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * قارئ السورة — أوفلاين بالكامل: نص السورة محلي (`files/quran_texts.json`)،
 * ولا يُجلب أي شيء من الشبكة. يُعرض عبر القارئ الموحّد نفسه (خيارات + إخفاء).
 */
@Composable
private fun QuranReader(surah: SurahInfo, onClose: () -> Unit) {
    var body by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(surah.number) {
        // تحميل مرة واحدة لكل عملية (≈750KB) — على خيط خلفي.
        withContext(Dispatchers.IO) {
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
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("جارٍ فتح المصحف…", color = KararColors.TextMuted, fontSize = 12.sp)
        }
        return
    }

    UnifiedReaderScreen(
        contentId = 1000 + surah.number,
        title = "سورة ${surah.name}",
        subtitle = "${surah.revelation} · ${surah.verses} آية",
        onBack = onClose,
        bodyText = remember(text) { formatSurahBody(surah.number, text) }
    )
}

/**
 * يحول علامات «﴾N﴾» إلى سطور: كل آية سطر مستقل،
 * ونضيف بسملة مستقلة لغير الفاتحة والتوبة كما في المصحف.
 */
private fun formatSurahBody(number: Int, raw: String): String {
    val marked = Regex("""﴿(\d+)﴾""").replace(raw) { m -> "\n﴿${m.groupValues[1]}﴾ " }
    return if (number != 1 && number != 9) {
        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\n\n$marked"
    } else {
        marked.trimStart('\n')
    }
}

