package com.karrar.amsakiah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.TextButton
import com.karrar.amsakiah.core.jinan.JinanContentTexts
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararFonts
import com.karrar.amsakiah.ui.theme.LocalKararPalette

/**
 * القارئ الموحّد — لأي محتوى قابل للقراءة: دعاء/زيارة/مفاتيح الجنان/قرآن.
 *
 * التصميم:
 * - يحترم مساحة الحالة (لا تداخل مع الساعة/الإشعارات) — edge-to-edge بلا شريط داخلي محشو.
 * - شريط علوي عائم [رجوع | العنوان | المفضلة] + **شريط أدوات أفقي واحد** تحته:
 *   [A−] [A+] | حجوم الخط | [أشكال خلفية القراءة الستة + «تلقائي»] | [تشكيل/بلا تشكيل] | [ملء الشاشة] —
 *   لا قوائم منسدلة، وكل تغيير يُطبَّق فورًا داخل نفس النافذة.
 * - الخلفية: «تلقائي» يتبع وضع التطبيق (داكن/فاتح)، أو خلفية مختارة يدويًا تحفظ حتى يغيّرها المستخدم.
 * - النص بخط «أميري» وبضبط `TextAlign.Center` الأصلي لكل النصوص.
 * - «قراءة بلا واجهة» تخفي كل شيء ما عدا نقطة استرجاع في الأسفل.
 */
@Composable
fun UnifiedReaderScreen(
    contentId: Int,
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onTitleChange: (String?) -> Unit = {},
    bodyText: String? = null,
    playerSlot: (@Composable () -> Unit)? = null,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null
) {
    LaunchedEffect(title) { onTitleChange(title) }

    var fontSize by remember(contentId) { mutableFloatStateOf(21f) }
    var chromeVisible by remember { mutableStateOf(true) }
    var themeId by remember { mutableStateOf("auto") }
    var withDiacritics by remember { mutableStateOf(true) }

    val body = remember(contentId, bodyText, subtitle) {
        if (bodyText != null) {
            bodyText
        } else {
            buildString {
                append("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\n\n")
                if (subtitle.isNotBlank()) append(subtitle).append("\n\n")
                append(JinanContentTexts.getProcessed(contentId).ifBlank { "" })
            }
        }
    }
    // التشكيل اختياري: أزله عند الحاجة بنقرة واحدة.
    val diacritics = remember { Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED\\u0640]") }
    val renderedBody = remember(body, withDiacritics) {
        if (withDiacritics) body else body.replace(diacritics, "")
    }

    val theme = readerTheme(themeId)

    Box(modifier = Modifier
        .background(theme.back)
        .fillMaxSize()) {

        val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

        // محتوى القراءة — يمرّر بكامل الحواف مع حشوات داخلية متوازنة.
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(
                    start = 18.dp,
                    end = 18.dp,
                    top = statusTop + 62.dp,
                    bottom = 110.dp
                )
        ) {
            Text(
                text = renderedBody,
                color = theme.text,
                fontFamily = KararFonts.Amiri,
                fontSize = fontSize.sp,
                lineHeight = (fontSize * 1.75f).sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(40.dp))
        }

        // الشريحة العلوية: [رجوع] [العنوان] [المفضلة] + شريط الأدوات — تختفي بالكامل في وضع الملء.
        if (chromeVisible) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(top = statusTop + 6.dp, start = 8.dp, end = 8.dp)
            ) {
                // الشريط العلوي الرئيسي — حجم ثابت، لا يظهر على شريط الحالة.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(theme.bar)
                        .border(1.dp, theme.barBorder, RoundedCornerShape(24.dp))
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RoundIcon(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        description = "رجوع",
                        tint = theme.icon,
                        onClick = onBack
                    )
                    Text(
                        text = title,
                        color = theme.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp)
                    )
                    if (onToggleFavorite != null) {
                        RoundIcon(
                            icon = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                            description = if (isFavorite) "إزالة من المفضلة" else "أضف إلى المفضلة",
                            tint = if (isFavorite) KararColors.Gold else theme.icon,
                            onClick = onToggleFavorite
                        )
                    }
                }

                // شريط الأدوات — سطر واحد أفقي يمرَّر بإصبع الإبهام، بلا قوائم منسدلة.
                Row(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(theme.bar)
                        .border(1.dp, theme.barBorder, RoundedCornerShape(22.dp))
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextToolButton(label = "أ−", description = "تصغير الخط", tint = theme.icon) {
                        fontSize = (fontSize - 2f).coerceAtLeast(13f)
                    }
                    TextToolButton(label = "أ+", description = "تكبير الخط", tint = theme.icon) {
                        fontSize = (fontSize + 2f).coerceAtMost(36f)
                    }
                    Text(
                        text = "${fontSize.toInt()}",
                        color = theme.icon.copy(alpha = 0.65f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(26.dp)
                    )
                    ToolDivider(theme.barBorder)

                    // أشكال خلفية القراءة — التبديل فوري داخل نفس النافذة.
                    ReaderTheme.entries.forEach { candidate ->
                        ThemeSwatch(
                            theme = readerTheme(candidate.id),
                            selected = candidate.id == themeId,
                            onSelect = { themeId = candidate.id }
                        )
                    }
                    ToolDivider(theme.barBorder)

                    // التشكيل: إظهاره أو إزالته بنقرة — يسهّل قراءة طويلة بلا نبر/فتحات.
                    DiacriticChip(
                        enabled = withDiacritics,
                        tint = theme.icon,
                        onToggle = { withDiacritics = !withDiacritics }
                    )
                    ToolDivider(theme.barBorder)

                    RoundIcon(
                        icon = Icons.Filled.Fullscreen,
                        description = "قراءة بلا واجهة — ملء الشاشة",
                        tint = theme.icon,
                        onClick = { chromeVisible = false }
                    )
                }
            }
        }

        // مشغّل الصوتيات — يطفو أسفل الشاشة حتى في وضع الملء (للتحكم بالصوت).
        playerSlot?.let { slot ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 14.dp, bottom = 20.dp)
            ) {
                slot()
            }
        }

        // نقطة استرجاع الواجهة في وضع الملء — تطفو أسفل المنتصف وتستعيد كل شيء بلمسة.
        if (!chromeVisible) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 22.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(theme.bar.copy(alpha = 0.92f))
                    .border(1.dp, theme.barBorder, CircleShape)
                    .clickable { chromeVisible = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.FullscreenExit,
                    contentDescription = "إظهار الواجهة",
                    tint = theme.icon,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/** أيقونة دائرية موحّدة لشرائح القارئ. */
@Composable
private fun RoundIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    tint: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.10f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(19.dp)
        )
    }
}

/** زر نصي صغير «أ− / أ+». */
@Composable
private fun TextToolButton(label: String, description: String, tint: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(tint.copy(alpha = 0.10f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = tint,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

/** فاصل رأسي نحيف بين مجموعات شريط الأدوات. */
@Composable
private fun ToolDivider(color: Color) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(22.dp)
            .background(color)
    )
}

/** زر «تشكيل/بلا تشكيل» — يوضح حالته أيقونةً ونصًا صغيرًا. */
@Composable
private fun DiacriticChip(enabled: Boolean, tint: Color, onToggle: () -> Unit) {
    TextButton(
        onClick = onToggle,
        modifier = Modifier.height(36.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.TextFields,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = if (enabled) "تشكيل" else "بلا تشكيل",
            color = tint,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/** شكل خلفية القراءة المختار — دائرة مصمتة بحد يُضيء عند التحديد. */
@Composable
private fun ThemeSwatch(
    theme: ReaderThemeResolved,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(theme.back, theme.swatchEdge)))
            .border(
                if (selected) 2.5.dp else 1.dp,
                if (selected) KararColors.Gold else theme.barBorder,
                CircleShape
            )
            .clickable(onClick = onSelect)
    )
}

// ---------- تعريف خلفيات القراءة ----------

/** خلفيات القراءة المتاحة — (auto = يتبع التطبيق). */
private enum class ReaderTheme { AUTO, PAPER, DARK, CREAM, FOREST, SAND }
// الخاصية id تُحترمها readerTheme للتبديل.
private val ReaderTheme.id: String
    get() = when (this) {
        ReaderTheme.AUTO -> "auto"
        ReaderTheme.DARK -> "dark"
        ReaderTheme.PAPER -> "paper"
        ReaderTheme.CREAM -> "cream"
        ReaderTheme.FOREST -> "forest"
        ReaderTheme.SAND -> "sand"
    }

private class ReaderThemeResolved(
    val back: Color,
    val text: Color,
    val title: Color,
    val bar: Color,
    val barBorder: Color,
    val icon: Color,
    val swatchEdge: Color
)

/** يبني المظهر الكامل للقارئ من معرّف الخلفية؛ «auto» يقرأ وضع التطبيق لحظيًا. */
@Composable
@androidx.compose.runtime.ReadOnlyComposable
private fun readerTheme(id: String): ReaderThemeResolved {
    val palette = LocalKararPalette.current
    return when (id) {
    "paper" -> ReaderThemeResolved(
        back = Color(0xFFFAF9F4),
        text = Color(0xFF2E2010),
        title = KararColors.GoldDeep,
        bar = Color(0xFFFAF9F4).copy(alpha = 0.96f),
        barBorder = Color(0x1A000000),
        icon = Color(0xFF5A4A1A),
        swatchEdge = Color(0xFFE8E2D2)
    )
    "dark" -> ReaderThemeResolved(
        back = Color(0xFF121212),
        text = Color(0xFFF2F2F7),
        title = KararColors.Gold,
        bar = Color(0xFF1F1F1F),
        barBorder = Color(0x33FFFFFF),
        icon = Color(0xFFD6D6DE),
        swatchEdge = Color(0xFF262626)
    )
    "cream" -> ReaderThemeResolved(
        back = Color(0xFFF6EEDD),
        text = Color(0xFF4A2C14),
        title = Color(0xFF8A5A1E),
        bar = Color(0xFFEDE0CC),
        barBorder = Color(0x26000000),
        icon = Color(0xFF5A3A1A),
        swatchEdge = Color(0xFFDED0B8)
    )
    "forest" -> ReaderThemeResolved(
        back = Color(0xFF0F1F17),
        text = Color(0xFFD8E9DE),
        title = Color(0xFF6FD3A0),
        bar = Color(0xFF16301F),
        barBorder = Color(0x333FD08A),
        icon = Color(0xFF9BD8B4),
        swatchEdge = Color(0xFF1C3B28)
    )
    "sand" -> ReaderThemeResolved(
        back = Color(0xFF2B2118),
        text = Color(0xFFE8DCC9),
        title = Color(0xFFE0B878),
        bar = Color(0xFF3A2E23),
        barBorder = Color(0x40FFFFFF),
        icon = Color(0xFFCDB894),
        swatchEdge = Color(0xFF443429)
    )
    // auto — يتبع وضع التطبيق: داكن عميق أو فاتح دافئ لحظيًا بلا إعادة تركيب.
    else -> ReaderThemeResolved(
        back = palette.surface,
        text = palette.textPrimary,
        title = palette.gold,
        bar = palette.glassDeep,
        barBorder = palette.borderStrong,
        icon = palette.textSecondary,
        swatchEdge = palette.surfaceHigh
    )
    }
}
