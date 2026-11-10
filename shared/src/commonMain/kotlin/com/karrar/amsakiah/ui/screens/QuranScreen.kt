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
import androidx.compose.material.icons.filled.Close
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
import com.karrar.amsakiah.core.net.fetchRemoteText
import com.karrar.amsakiah.core.state.AppState
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararFonts
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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

@Composable
private fun QuranIndex(onOpen: (SurahInfo) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
            ) {
                Text(
                    "القرآن الكريم",
                    color = KararColors.TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "١١٤ سورة · المصحف الشريف · اختر سورة لتقرأ آياتها",
                    color = KararColors.TextMuted,
                    fontSize = 11.sp
                )
            }
        }
        itemsIndexed(SURAHS, key = { _, s -> s.number }) { _, surah ->
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

@Composable
private fun QuranReader(surah: SurahInfo, onClose: () -> Unit) {
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var verses by remember { mutableStateOf<List<String>>(emptyList()) }
    var reloadTick by remember { mutableStateOf(0) }
    val amiriFont = KararFonts.Amiri

    LaunchedEffect(surah.number, reloadTick) {
        loading = true
        failed = false
        // مفتاح ذاكرة مؤقتة على مستوى العملية فقط — لا نخزن آيات المستخدم.
        val cached = surahCache[surah.number]
        if (cached != null) {
            verses = cached
            loading = false
            return@LaunchedEffect
        }
        val raw = withContext(Dispatchers.IO) {
            fetchRemoteText("https://api.alquran.cloud/v1/surah/${surah.number}/quran-uthmani")
        }
        val parsed = raw?.let(::parseSurah)
        if (parsed != null && parsed.isNotEmpty()) {
            surahCache[surah.number] = parsed
            verses = parsed
            loading = false
        } else {
            verses = emptyList()
            loading = false
            failed = true
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 8.dp),
            corner = 20.dp,
            color = KararColors.CardSoft,
            borderColor = KararColors.Border,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "سورة ${surah.name}",
                        color = KararColors.Gold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${surah.revelation} · ${surah.verses} آية",
                        color = KararColors.TextMuted,
                        fontSize = 10.sp
                    )
                }
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "إغلاق المصحف",
                    tint = KararColors.TextSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(KararColors.SurfaceHigh)
                        .clickable(onClick = onClose)
                        .padding(2.dp)
                )
            }
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("جارٍ تحميل الآيات…", color = KararColors.TextMuted, fontSize = 12.sp)
            }
            failed -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "تعذر جلب نص السورة — تحقق من اتصالك بالإنترنت",
                        color = KararColors.TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(KararColors.Teal.copy(alpha = 0.16f))
                            .clickable { reloadTick += 1 }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("إعادة المحاولة", color = KararColors.Teal, fontWeight = FontWeight.Bold)
                    }
                }
            }
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (surah.number != 1 && surah.number != 9) {
                    item {
                        Text(
                            "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            color = KararColors.Gold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontFamily = amiriFont,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                itemsIndexed(verses, key = { index, _ -> index }) { index, verse ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = verse,
                            color = KararColors.TextPrimary,
                            fontSize = 17.sp,
                            lineHeight = 30.sp,
                            textAlign = TextAlign.Right,
                            fontFamily = amiriFont,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "﴿${index + 1}﴾",
                            color = KararColors.Gold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private val surahCache = mutableMapOf<Int, List<String>>()

@Serializable
private data class AyahDto(val numberInSurah: Int = 0, val text: String = "")

@Serializable
private data class SurahDataDto(val ayahs: List<AyahDto> = emptyList())

@Serializable
private data class SurahResponseDto(val data: SurahDataDto = SurahDataDto())

private val ayahJson = Json { ignoreUnknownKeys = true; isLenient = true }

private fun parseSurah(raw: String): List<String>? = runCatching {
    ayahJson.decodeFromString(SurahResponseDto.serializer(), raw).data.ayahs.map { it.text }
}.getOrNull()
