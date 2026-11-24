package com.karrar.amsakiah.ui.screens

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import com.karrar.amsakiah.ui.motion.pressScale
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.karrar.amsakiah.ui.platform.PlatformBackHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.safeDrawing
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
 * - النص بخط «أميري» بمحاذاة **Justify** (توزيع الكلمات بالتساوي من اليمين إلى اليسار) — مظهر كتب وصحف منتظم.
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
    onToggleFavorite: (() -> Unit)? = null,
    /** تُستدعى بملاحظة المستخدم من القارئ مباشرة — «المهام والملاحظات» تبغيها هنا أيضًا. */
    onSaveNote: ((String) -> Unit)? = null,
    /** عبارة أو كلمة للانتقال مباشرة إليها (مثل يوم معين في أعمال الشهر أو صلاة ليلة معينة). */
    initialQuery: String? = null
) {
    PlatformBackHandler(enabled = true) { onBack() }

    LaunchedEffect(title) { onTitleChange(title) }

    var fontSize by remember(contentId) { mutableFloatStateOf(21f) }
    var chromeVisible by remember { mutableStateOf(true) }
    var toolsVisible by remember { mutableStateOf(false) }
    var noteDialogVisible by remember { mutableStateOf(false) }
    var themeId by remember { mutableStateOf("auto") }
    var withDiacritics by remember { mutableStateOf(true) }
    var fontIndex by remember { mutableIntStateOf(0) }
    var isTwoPageMode by remember(contentId) { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    val body = remember(contentId, bodyText, subtitle) {
        if (bodyText != null) {
            bodyText
        } else {
            val content = JinanContentTexts.getProcessed(contentId).ifBlank { "" }
            buildString {
                if (!content.startsWith("بِسْمِ اللَّهِ") && !content.contains("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ")) {
                    append("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\n\n")
                }
                if (subtitle.isNotBlank() && !content.startsWith(subtitle)) {
                    append(subtitle).append("\n\n")
                }
                append(content)
            }
        }
    }
    // التشكيل اختياري: أزله عند الحاجة بنقرة واحدة.
    val diacritics = remember { Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED\\u0640]") }
    val renderedBody = remember(body, withDiacritics) {
        if (withDiacritics) body else body.replace(diacritics, "")
    }

    val theme = readerTheme(themeId)
    val readerFonts = listOf(
        KararFonts.Amiri to "أميري",
        KararFonts.ReemKufi to "كوفي",
        androidx.compose.ui.text.font.FontFamily.Serif to "Serif"
    )
    val activeFont = readerFonts[fontIndex.coerceIn(0, readerFonts.lastIndex)]

    Box(modifier = Modifier
        .background(theme.back)
        .fillMaxSize()) {

        // مسافة الحالة/التنقل تُقرأ مباشرة — الشل لا يمرر contentPadding للقارئ؛
        // الخلفية (theme.back) تمتد كامل الحواف بما فيها خلف شريط الحالة.
        val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val navigationBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        // الحواف الجانبية (الثقب/DeX في الأفقي) تحجزها Inset الآمن فقط — بلا مضاعفة
        // لأن الشل لا يمرر contentPadding لهذه الشاشة أصلًا.
        val safeDrawing = WindowInsets.safeDrawing.asPaddingValues()
        val layoutDirection = LocalLayoutDirection.current
        val safeStart = safeDrawing.calculateStartPadding(layoutDirection)
        val safeEnd = safeDrawing.calculateEndPadding(layoutDirection)

        // محتوى القراءة — نظام كتاب حقيقي:
        // في وضع الهاتف الأفقي (Landscape) أو الشاشات العريضة (≥ 500dp)، ينقسم القارئ
        // إلى ورقتين كالكتاب الحقيقي (يمين ويسار) مع تقليب أفقي ناعم للأوراق.
        // يمكن التبديل في أي لحظة بين «ورقتين كالكتاب» و«ملء كامل بصفحة واحدة».
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = safeStart + 14.dp,
                    end = safeEnd + 14.dp,
                    top = statusTop + (if (chromeVisible) 60.dp else 18.dp),
                    bottom = if (chromeVisible) 64.dp else 24.dp
                )
        ) {
            // التكيّف الرسمي (WindowSizeClass & Material 3 Adaptive):
            // Compact (< 600dp): ورقة واحدة لملء شاشة الهاتف العمودية.
            // Medium/Expanded (>= 600dp) أو أفقي (Landscape): ورقتان متقابلتان كالكتاب الحقيقي.
            val isLandscapeOrWide = maxWidth >= 600.dp || (maxHeight < 500.dp && maxWidth >= 540.dp)
            val showTwoPages = isLandscapeOrWide && isTwoPageMode

            val pages = remember(renderedBody, showTwoPages) {
                splitIntoBookPages(renderedBody, targetPageChars = if (showTwoPages) 760 else 920)
            }
            val spreadCount = if (showTwoPages) (pages.size + 1) / 2 else pages.size
            val initialSpread = remember(pages, initialQuery, showTwoPages) {
                if (initialQuery.isNullOrBlank()) 0
                else {
                    val qNorm = normSearch(initialQuery)
                    val targetIdx = pages.indexOfFirst { page ->
                        normSearch(page).contains(qNorm)
                    }.takeIf { it >= 0 } ?: 0
                    if (showTwoPages) targetIdx / 2 else targetIdx
                }
            }
            val pagerState = rememberPagerState(initialPage = initialSpread) { spreadCount }

            LaunchedEffect(initialSpread) {
                if (initialSpread > 0 && initialSpread < spreadCount) {
                    pagerState.scrollToPage(initialSpread)
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (showTwoPages) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { spreadIndex ->
                        val rightIdx = spreadIndex * 2
                        val leftIdx = spreadIndex * 2 + 1
                        val pageOffset = ((pagerState.currentPage - spreadIndex) + pagerState.currentPageOffsetFraction)
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    cameraDistance = 16f * density
                                    transformOrigin = TransformOrigin(if (pageOffset > 0) 0f else 1f, 0.5f)
                                    rotationY = -18f * pageOffset.coerceIn(-1f, 1f)
                                    val absOffset = kotlin.math.abs(pageOffset).coerceIn(0f, 1f)
                                    alpha = 1f - (absOffset * 0.20f)
                                },
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // الصفحة الأولى (اليمين في القراءة العربية RTL)
                            ReaderBookPage(
                                pageNumber = rightIdx + 1,
                                totalPages = pages.size,
                                content = pages[rightIdx],
                                theme = theme,
                                activeFont = activeFont.first,
                                fontSize = fontSize,
                                modifier = Modifier.weight(1f)
                            )
                            // طية منتصف الكتاب (Spine Divider)
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .fillMaxHeight()
                                    .background(theme.barBorder.copy(alpha = 0.55f))
                            )
                            // الصفحة الثانية (اليسار في القراءة العربية RTL)
                            if (leftIdx < pages.size) {
                                ReaderBookPage(
                                    pageNumber = leftIdx + 1,
                                    totalPages = pages.size,
                                    content = pages[leftIdx],
                                    theme = theme,
                                    activeFont = activeFont.first,
                                    fontSize = fontSize,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Box(Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { pageIdx ->
                        val pageOffset = ((pagerState.currentPage - pageIdx) + pagerState.currentPageOffsetFraction)
                        ReaderBookPage(
                            pageNumber = pageIdx + 1,
                            totalPages = pages.size,
                            content = pages[pageIdx],
                            theme = theme,
                            activeFont = activeFont.first,
                            fontSize = fontSize,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    cameraDistance = 14f * density
                                    transformOrigin = TransformOrigin(if (pageOffset > 0) 0f else 1f, 0.5f)
                                    rotationY = -24f * pageOffset.coerceIn(-1f, 1f)
                                    val absOffset = kotlin.math.abs(pageOffset).coerceIn(0f, 1f)
                                    alpha = 1f - (absOffset * 0.25f)
                                }
                        )
                    }
                }

                // شريط تقليب الصفحات العائم — كبسولة زجاجية أنيقة واحدة فقط تطفو فوق مسافة النظام
                if (spreadCount > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = navigationBottom + 12.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(theme.bar.copy(alpha = 0.94f))
                            .border(1.dp, theme.barBorder, RoundedCornerShape(22.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RoundIcon(
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            description = "الصفحة السابقة",
                            tint = if (pagerState.currentPage > 0) theme.icon else theme.icon.copy(alpha = 0.25f),
                            onClick = {
                                if (pagerState.currentPage > 0) {
                                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                                }
                            }
                        )

                        val pageIndicator = if (showTwoPages) {
                            val p1 = pagerState.currentPage * 2 + 1
                            val p2 = minOf(pagerState.currentPage * 2 + 2, pages.size)
                            if (p1 == p2) "ورقة $p1 من ${pages.size}" else "ورقة $p1 - $p2 من ${pages.size}"
                        } else {
                            "ورقة ${pagerState.currentPage + 1} من ${pages.size}"
                        }

                        Text(
                            text = pageIndicator,
                            color = theme.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = activeFont.first
                        )

                        RoundIcon(
                            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            description = "الصفحة التالية",
                            tint = if (pagerState.currentPage < spreadCount - 1) theme.icon else theme.icon.copy(alpha = 0.25f),
                            onClick = {
                                if (pagerState.currentPage < spreadCount - 1) {
                                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                                }
                            }
                        )
                    }
                }
            }
        }

        // الشريحة العلوية: [رجوع] [العنوان] [المفضلة] + شريط الأدوات — تختفي بالكامل في وضع الملء.
        if (chromeVisible) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(top = statusTop + 6.dp, start = safeStart + 8.dp, end = safeEnd + 8.dp)
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
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
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
                    // زر التبديل بين وضع الورقتين والصفحة الواحدة
                    RoundIcon(
                        icon = if (isTwoPageMode) Icons.AutoMirrored.Filled.MenuBook else Icons.Filled.CropSquare,
                        description = if (isTwoPageMode) "عرض ورقتين (كتاب) — انقر لملء الشاشة بصفحة واحدة" else "صفحة واحدة — انقر لعرض ورقتين كالكتاب",
                        tint = if (isTwoPageMode) KararColors.Gold else theme.icon,
                        onClick = { isTwoPageMode = !isTwoPageMode }
                    )
                    // زر الأدوات المخصص — يظهر/يخفي شريط الأدوات فقط (زر مستقل بدل ما يكون ظاهرا دائما).
                    RoundIcon(
                        icon = Icons.Filled.Tune,
                        description = if (toolsVisible) "إخفاء شريط الأدوات" else "إظهار شريط الأدوات",
                        tint = if (toolsVisible) KararColors.Gold else theme.icon,
                        onClick = { toolsVisible = !toolsVisible }
                    )
                    if (onSaveNote != null) {
                        RoundIcon(
                            icon = Icons.AutoMirrored.Filled.Notes,
                            description = "حفظ ملاحظة في «المهام والملاحظات»",
                            tint = theme.icon,
                            onClick = { noteDialogVisible = true }
                        )
                    }
                }

                // شريط الأدوات — يظهر فقط بزر «الأدوات» من الأعلى (زر مخصص قابل للتبديل)،
                // سطر أفقي واحد يمرَّر بالإصبع بلا قوائم منسدلة.
                if (toolsVisible) {
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
                            textAlign = TextAlign.Center,
                            lineHeight = 11.sp,
                            modifier = Modifier.width(26.dp)
                        )
                        ToolDivider(theme.barBorder)

                        // نوع الخط — النقر يتبدل بالترتيب «أميري ← كوفي ← لاتيني» فوراً.
                        RoundIcon(
                            icon = Icons.Filled.TextFields,
                            description = "نوع الخط: ${activeFont.second} (انقر للتبديل)",
                            tint = theme.icon,
                            onClick = { fontIndex = (fontIndex + 1) % readerFonts.size }
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
        }

        // نافذة «حفظ ملاحظة» — تُخزَّن في «المهام والملاحظات» بمحتواها والعنوان.
        if (noteDialogVisible && onSaveNote != null) {
            NoteSaveDialog(
                title = title,
                onDismiss = { noteDialogVisible = false },
                onSave = { text ->
                    onSaveNote(text)
                    noteDialogVisible = false
                }
            )
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

        // نقطة استرجاع الواجهة في وضع الملء — تطفو على الجانب الأيسر السفلي لتفادي أي تداخل
        if (!chromeVisible) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = safeStart + 16.dp, bottom = navigationBottom + 16.dp)
                    .size(44.dp)
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

/** نافذة حفظ ملاحظة من القارئ — تخزنها في «المهام والملاحظات» بعنوان القارئ. */
@Composable
private fun NoteSaveDialog(
    title: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    com.karrar.amsakiah.ui.components.SpringDialog(
        visible = true,
        onDismissRequest = onDismiss,
        dismissible = true,
        contentPosition = Alignment.BottomCenter,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        imePadding = true
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(com.karrar.amsakiah.ui.theme.LocalKararPalette.current.surfaceHigh)
                .border(1.dp, com.karrar.amsakiah.ui.theme.LocalKararPalette.current.borderStrong, RoundedCornerShape(24.dp))
                .padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Text(
                text = "حفظ ملاحظة في «المهام والملاحظات»",
                color = com.karrar.amsakiah.ui.theme.LocalKararPalette.current.gold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                color = com.karrar.amsakiah.ui.theme.LocalKararPalette.current.textMuted,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("اكتب ملاحظتك…", fontSize = 13.sp) },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDismiss) { Text("إلغاء") }
                TextButton(
                    onClick = { if (text.isNotBlank()) onSave("${text.trim()}\n— $title") }
                ) { Text("حفظ", fontWeight = FontWeight.Black) }
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
    val roundSource = androidx.compose.runtime.remember {
        androidx.compose.foundation.interaction.MutableInteractionSource()
    }
    Box(
        modifier = Modifier
            .size(36.dp)
            .pressScale(roundSource, 0.9f)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.10f))
            .clickable(
                interactionSource = roundSource,
                indication = null,
                onClick = onClick
            ),
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
    val toolSource = androidx.compose.runtime.remember {
        androidx.compose.foundation.interaction.MutableInteractionSource()
    }
    Box(
        modifier = Modifier
            .size(36.dp)
            .pressScale(toolSource, 0.9f)
            .clip(RoundedCornerShape(18.dp))
            .background(tint.copy(alpha = 0.10f))
            .clickable(
                interactionSource = toolSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = tint,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            lineHeight = 15.sp
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

/** زر «تشكيل/بلا تشكيل» — يوضح حالته أيقونةً ونصًا صغيرًا؛ تُتوسّط الكبسولة كل العناصر. */
@Composable
private fun DiacriticChip(enabled: Boolean, tint: Color, onToggle: () -> Unit) {
    val diaSource = androidx.compose.runtime.remember {
        androidx.compose.foundation.interaction.MutableInteractionSource()
    }
    Box(
        modifier = Modifier
            .height(36.dp)
            .pressScale(diaSource, 0.92f)
            .clip(RoundedCornerShape(18.dp))
            .background(tint.copy(alpha = 0.10f))
            .clickable(
                interactionSource = diaSource,
                indication = null,
                onClick = onToggle
            )
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.TextFields,
                contentDescription = if (enabled) "إخفاء التشكيل" else "إظهار التشكيل",
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (enabled) "تشكيل" else "بلا تشكيل",
                color = tint,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 11.sp
            )
        }
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
private enum class ReaderTheme { AUTO, PAPER, DARK, AMOLED, SEPIA, CREAM, FOREST, SAND }
// الخاصية id تُحترمها readerTheme للتبديل.
private val ReaderTheme.id: String
    get() = when (this) {
        ReaderTheme.AUTO -> "auto"
        ReaderTheme.DARK -> "dark"
        ReaderTheme.PAPER -> "paper"
        ReaderTheme.CREAM -> "cream"
        ReaderTheme.FOREST -> "forest"
        ReaderTheme.SAND -> "sand"
        ReaderTheme.AMOLED -> "amoled"
        ReaderTheme.SEPIA -> "sepia"
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
    "amoled" -> ReaderThemeResolved(
        back = Color(0xFF000000),
        text = Color(0xFFE9E9EF),
        title = KararColors.Gold,
        bar = Color(0xFF0F0F0F),
        barBorder = Color(0x33FFFFFF),
        icon = Color(0xFFD6D6DE),
        swatchEdge = Color(0xFF000000)
    )
    "sepia" -> ReaderThemeResolved(
        back = Color(0xFFF2E7D0),
        text = Color(0xFF40301F),
        title = Color(0xFF7A4A16),
        bar = Color(0xFFE9DCC4),
        barBorder = Color(0x24100000),
        icon = Color(0xFF5A4020),
        swatchEdge = Color(0xFFCDBFA0)
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

/**
 * يقسم النص إلى صفحات متوازنة بحجم كتاب حقيقي (≈ 750 إلى 950 حرفًا لكل صفحة)،
 * مع احترام نهايات الآيات ﴿...﴾ وعلامات الترقيم ونهايات الفقرات.
 */
private fun splitIntoBookPages(text: String, targetPageChars: Int = 850): List<String> {
    if (text.length <= targetPageChars) return listOf(text)
    val tokens = mutableListOf<String>()
    val currentToken = StringBuilder()
    var i = 0
    while (i < text.length) {
        val c = text[i]
        currentToken.append(c)
        if (c == '\n' || c == '.' || c == '،' || c == '؛' || c == '؟' || c == '!' || c == '۝' || c == '﴾') {
            tokens.add(currentToken.toString())
            currentToken.clear()
        }
        i++
    }
    if (currentToken.isNotEmpty()) {
        tokens.add(currentToken.toString())
    }

    val pages = mutableListOf<String>()
    val current = StringBuilder()
    for (token in tokens) {
        // إذا كان الرمز سطرًا جديدًا متعددًا أو فاصل سورة/عنوان رئيسي كبير، ننهي الصفحة إذا كانت ممتلئة بنسبة معقولة
        val isMajorBreak = token.startsWith("بِسْمِ اللَّهِ") || token.contains("\n\n\n")
        if ((current.length + token.length > targetPageChars && current.isNotBlank()) ||
            (isMajorBreak && current.length > targetPageChars / 2)
        ) {
            pages.add(current.toString().trim())
            current.clear()
        }
        current.append(token)
    }
    if (current.isNotBlank()) {
        pages.add(current.toString().trim())
    }
    return if (pages.isEmpty()) listOf(text) else pages
}

private val ayahPattern = Regex("﴿(\\d+)﴾")
private val easternDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
private fun toEasternDigits(s: String): String = buildString(s.length) {
    for (ch in s) {
        if (ch in '0'..'9') append(easternDigits[ch - '0']) else append(ch)
    }
}

private fun normSearch(s: String): String =
    s.replace(Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED\\u0640]"), "")
        .replace(Regex("[أإآٱ]"), "ا")
        .replace('ة', 'ه')
        .replace('ى', 'ي')

/**
 * ينسّق نصوص القراءة بجمالية إسلامية وقارئية رفيعة:
 * - تحويل أرقام الآيات ﴿1﴾ إلى أرقام مشرقية ذهبية ﴿١﴾.
 * - إبراز الشروح والبدايات (الأول:، الثاني:، روي:، أقول:) بلون مميز وخط عريض.
 * - تمييز الصلوات والألقاب القدسية (ص، ع، عج) بلطف لعدم تشتيت تدفق الدعاء.
 */
@Composable
private fun formatReaderAnnotatedString(
    content: String,
    theme: ReaderThemeResolved,
    fontSize: Float
): androidx.compose.ui.text.AnnotatedString {
    val normalized = remember(content) {
        ayahPattern.replace(content) { match ->
            "﴿" + toEasternDigits(match.groupValues[1]) + "﴾"
        }
    }
    return remember(normalized, theme, fontSize) {
        androidx.compose.ui.text.buildAnnotatedString {
            append(normalized)
            // 1) ترقيم الآيات المشرقية ﴿...﴾ بلون الذهب والخط العريض
            val goldColor = Color(0xFFD4AF37)
            Regex("﴿[٠-٩]+﴾").findAll(normalized).forEach { m ->
                addStyle(
                    androidx.compose.ui.text.SpanStyle(
                        color = goldColor,
                        fontWeight = FontWeight.Bold
                    ),
                    m.range.first,
                    m.range.last + 1
                )
            }
            // 2) علامات الفقرات والشروح (الأول:، الثاني:، روي:، أقول:، فصل:، دعاء:، صلاة:)
            val headerRegex = Regex("(?:^|\\n)(الأول|الثاني|الثالث|الرابع|الخامس|السادس|السابع|الثامن|التاسع|العاشر|الحادي عشر|الثاني عشر|الثالث عشر|الرابع عشر|الخامس عشر|أقول|روي|وعنه|فصل|دعاء|صلاة|اليوم|الليلة)[:\\s]")
            headerRegex.findAll(normalized).forEach { m ->
                addStyle(
                    androidx.compose.ui.text.SpanStyle(
                        color = theme.title,
                        fontWeight = FontWeight.Bold
                    ),
                    m.range.first,
                    m.range.last + 1
                )
            }
            // 3) الصلوات والتحيات المباركة
            val blessingsRegex = Regex("\\(صلى الله عليه وآله\\)|\\(عليه السلام\\)|\\(عليها السلام\\)|\\(عليهم السلام\\)|\\(عجل الله تعالى فرجه\\)|\\(عج\\)|\\(س\\)")
            blessingsRegex.findAll(normalized).forEach { m ->
                addStyle(
                    androidx.compose.ui.text.SpanStyle(
                        color = theme.icon.copy(alpha = 0.85f),
                        fontSize = (fontSize * 0.92f).sp
                    ),
                    m.range.first,
                    m.range.last + 1
                )
            }
        }
    }
}

/** ورقة كتاب واحدة بمحاذاة تامة وترقيم أنيق. */
@Composable
private fun ReaderBookPage(
    pageNumber: Int,
    totalPages: Int,
    content: String,
    theme: ReaderThemeResolved,
    activeFont: androidx.compose.ui.text.font.FontFamily,
    fontSize: Float,
    modifier: Modifier = Modifier
) {
    val annotatedContent = formatReaderAnnotatedString(content, theme, fontSize)
    Column(
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = annotatedContent,
            color = theme.text,
            fontFamily = activeFont,
            fontSize = fontSize.sp,
            lineHeight = (fontSize * 1.75f).sp,
            textAlign = TextAlign.Justify,
            modifier = Modifier.fillMaxWidth()
        )
        if (totalPages > 1) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "— $pageNumber —",
                    color = theme.icon.copy(alpha = 0.40f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = activeFont
                )
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}
