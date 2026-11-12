package com.karrar.amsakiah.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.data.TasbihDhikr
import com.karrar.amsakiah.core.data.TasbihPresets
import com.karrar.amsakiah.core.feedback.playTasbihClick
import com.karrar.amsakiah.core.feedback.playTasbihComplete
import com.karrar.amsakiah.core.state.AppState
import com.karrar.amsakiah.ui.adaptive.ScreenHorizontalMargin
import com.karrar.amsakiah.ui.adaptive.contentEnd
import com.karrar.amsakiah.ui.adaptive.contentStart
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.components.MergedAccordion
import com.karrar.amsakiah.ui.components.SectionTitle
import com.karrar.amsakiah.ui.components.SpringDialog
import com.karrar.amsakiah.ui.motion.ambientMotionEnabled
import com.karrar.amsakiah.ui.motion.motionSpatial
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararTokens
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// الألوان تقرأ من اللوحة الحالية حتى يتبدّل الوضع الداكن/الفاتح فورًا —
// لا قيم داكنة ثابتة تُظهر الوضع الفاتح معطوبًا.
private val Sky: Color @Composable @ReadOnlyComposable get() = KararColors.Sky
private val Violet: Color @Composable @ReadOnlyComposable get() = KararColors.Violet

/** نص فوق سطح سماوي فاتح: داكن في الوضعين ليبقى التباين عاليًا. */
private val OnAccent: Color @Composable @ReadOnlyComposable get() =
    if (KararColors.IsDark) Color.White else Color(0xFF082F49)

private const val TAB_COUNTER = 0
private const val TAB_LIST = 1
private const val TAB_STATS = 2
private const val TAB_SETTINGS = 3

@Composable
fun TasbihScreen(state: AppState, contentPadding: PaddingValues) {
    val settings = state.settings
    val presets = remember(settings.customDhikrs) {
        TasbihPresets.combined(settings.customDhikrs)
    }
    val haptic = LocalHapticFeedback.current
    val dhikrIndex = TasbihPresets.safeIndex(settings.tasbihDhikrIndex, presets.size)
    val dhikr = presets[dhikrIndex]
    val target = settings.tasbihTarget.coerceAtLeast(1)
    val count = settings.tasbihCount.coerceAtLeast(0)

    var tab by remember { mutableStateOf(TAB_COUNTER) }
    var showCompletion by remember { mutableStateOf(false) }
    var completedTarget by remember { mutableStateOf(0) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var showResetAllConfirm by remember { mutableStateOf(false) }
    var showAddDhikr by remember { mutableStateOf(false) }

    val todayKey = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    }

    // تصفير عدّاد اليوم تلقائيًا عند تغيّر التاريخ (كما في مسبحة.html)
    LaunchedEffect(todayKey) {
        if (state.settings.tasbihDayDate != todayKey) {
            state.update { it.copy(tasbihDayDate = todayKey, tasbihDayCount = 0) }
        }
    }

    fun selectDhikr(index: Int) {
        val safe = TasbihPresets.safeIndex(index, presets.size)
        state.update {
            it.copy(
                tasbihDhikrIndex = safe,
                tasbihTarget = presets[safe].target,
                tasbihCount = 0
            )
        }
        tab = TAB_COUNTER
    }

    fun onTick() {
        val current = state.settings
        val currentTarget = current.tasbihTarget.coerceAtLeast(1)
        val nextCount = current.tasbihCount + 1
        if (nextCount >= currentTarget) {
            // إتمام الهدف: احفظ الجلسة، وانتقل للذكر التالي إن كان ضمن مجموعة
            if (current.tasbihSound) playTasbihComplete()
            if (current.tasbihHaptics) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            val (nextIndex, nextTarget) = TasbihPresets.next(
                TasbihPresets.combined(current.customDhikrs),
                current.tasbihDhikrIndex,
                current.tasbihAutoAdvance
            )
            completedTarget = currentTarget
            state.update {
                it.copy(
                    tasbihCount = 0,
                    tasbihRound = it.tasbihRound + 1,
                    tasbihTotal = it.tasbihTotal + 1,
                    tasbihDayCount = it.tasbihDayCount + 1,
                    tasbihDayDate = todayKey,
                    tasbihDhikrIndex = nextIndex,
                    tasbihTarget = nextTarget
                )
            }
            if (current.tasbihCompletionOverlay) showCompletion = true
        } else {
            if (current.tasbihSound) playTasbihClick()
            if (current.tasbihHaptics) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            state.update {
                it.copy(
                    tasbihCount = nextCount,
                    tasbihTotal = it.tasbihTotal + 1,
                    tasbihDayCount = it.tasbihDayCount + 1,
                    tasbihDayDate = todayKey
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = contentPadding.contentStart(ScreenHorizontalMargin),
                end = contentPadding.contentEnd(ScreenHorizontalMargin),
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 16.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ---------- الهيدر: اسم المجموعة + تصفير ----------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(KararColors.Danger.copy(alpha = 0.12f))
                    .clickable { showResetConfirm = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.RestartAlt,
                    contentDescription = "تصفير العداد",
                    tint = KararColors.Danger,
                    modifier = Modifier.size(21.dp)
                )
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    dhikr.group,
                    color = Sky,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    "مسبحة كرار — ${presets.size} أذكار",
                    color = KararColors.TextMuted,
                    fontSize = 10.sp
                )
            }
            Box(Modifier.size(42.dp))
        }

        Spacer(Modifier.height(10.dp))
        TasbihTabs(selected = tab, onSelect = { tab = it })
        Spacer(Modifier.height(16.dp))

        when (tab) {
            TAB_LIST -> TasbihListTab(
                items = presets,
                presetCount = TasbihPresets.all.size,
                selectedIndex = dhikrIndex,
                onSelect = ::selectDhikr,
                onAdd = { showAddDhikr = true },
                onDelete = { index ->
                    val item = presets.getOrNull(index)
                    if (item != null && item.id.isNotBlank()) {
                        state.update { s ->
                            val remaining = s.customDhikrs.filterNot { it.id == item.id }
                            val newList = TasbihPresets.combined(remaining)
                            val currentId = presets.getOrNull(s.tasbihDhikrIndex)?.id.orEmpty()
                            val match = newList.indexOfFirst { it.id == currentId && currentId.isNotBlank() }
                            val newIndex = if (match >= 0) {
                                match
                            } else {
                                TasbihPresets.safeIndex(s.tasbihDhikrIndex, newList.size)
                            }
                            s.copy(
                                customDhikrs = remaining,
                                tasbihDhikrIndex = newIndex,
                                tasbihTarget = newList[newIndex].target,
                                tasbihCount = 0
                            )
                        }
                    }
                }
            )
            TAB_STATS -> TasbihStatsTab(
                dayCount = settings.tasbihDayCount,
                total = settings.tasbihTotal,
                rounds = settings.tasbihRound,
                onResetDay = { state.update { it.copy(tasbihDayCount = 0) } },
                onResetAll = { showResetAllConfirm = true }
            )
            TAB_SETTINGS -> TasbihSettingsTab(
                haptics = settings.tasbihHaptics,
                sound = settings.tasbihSound,
                autoAdvance = settings.tasbihAutoAdvance,
                overlay = settings.tasbihCompletionOverlay,
                onHaptics = { v -> state.update { it.copy(tasbihHaptics = v) } },
                onSound = { v -> state.update { it.copy(tasbihSound = v) } },
                onAutoAdvance = { v -> state.update { it.copy(tasbihAutoAdvance = v) } },
                onOverlay = { v -> state.update { it.copy(tasbihCompletionOverlay = v) } },
                onResetAll = { showResetAllConfirm = true }
            )
            else -> TasbihCounterTab(
                dhikr = dhikr,
                count = count,
                target = target,
                rounds = settings.tasbihRound,
                total = settings.tasbihTotal,
                dayCount = settings.tasbihDayCount,
                onTick = ::onTick,
                onOpenList = { tab = TAB_LIST }
            )
        }
    }

    CompletionDialog(
        visible = showCompletion,
        target = completedTarget,
        nextDhikr = presets[
            TasbihPresets.safeIndex(state.settings.tasbihDhikrIndex, presets.size)
        ].text,
        onContinue = { showCompletion = false },
        onChange = {
            showCompletion = false
            tab = TAB_LIST
        }
    )

    TasbihConfirmDialog(
        visible = showResetConfirm,
        title = "إعادة العد",
        message = "هل تريد إعادة العداد الحالي إلى الصفر؟",
        confirmLabel = "تصفير",
        onConfirm = {
            state.update { it.copy(tasbihCount = 0) }
            showResetConfirm = false
        },
        onDismiss = { showResetConfirm = false }
    )

    TasbihConfirmDialog(
        visible = showResetAllConfirm,
        title = "حذف كل الإحصاءات",
        message = "سيتم تصفير اليوم والإجمالي والدورات نهائيًا. هل أنت متأكد؟",
        confirmLabel = "حذف الكل",
        onConfirm = {
            state.update {
                it.copy(
                    tasbihCount = 0,
                    tasbihRound = 0,
                    tasbihTotal = 0,
                    tasbihDayCount = 0,
                    tasbihDayDate = todayKey
                )
            }
            showResetAllConfirm = false
        },
        onDismiss = { showResetAllConfirm = false }
    )

    AddDhikrDialog(
        visible = showAddDhikr,
        onDismiss = { showAddDhikr = false },
            onAdd = { text, target, group ->
                val trimmed = text.trim()
                if (trimmed.isNotEmpty()) {
                    val item = TasbihDhikr(
                        text = trimmed,
                        target = target.coerceIn(1, 9999),
                        group = group.trim().ifBlank { TasbihPresets.CUSTOM_GROUP },
                        hint = "${target.coerceIn(1, 9999)} مرة",
                        id = "custom-${Clock.System.now().toEpochMilliseconds()}"
                    )
                    state.update { s ->
                        val list = s.customDhikrs + item
                        s.copy(
                            customDhikrs = list,
                            tasbihDhikrIndex = TasbihPresets.all.size + list.lastIndex,
                            tasbihTarget = item.target,
                            tasbihCount = 0
                        )
                    }
                }
                showAddDhikr = false
            }
    )
}

// ============================================================================
//  التبويبات
// ============================================================================
@Composable
private fun TasbihTabs(selected: Int, onSelect: (Int) -> Unit) {
    val labels = listOf("المسبحة", "التسبيحات", "الإحصاءات", "الإعدادات")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(KararColors.CardSoft)
            .border(1.dp, KararColors.Border, RoundedCornerShape(16.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val isSelected = index == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Sky else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (isSelected) OnAccent else KararColors.TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

// ============================================================================
//  تبويب المسبحة — العداد الكبير
// ============================================================================
@Composable
private fun TasbihCounterTab(
    dhikr: TasbihDhikr,
    count: Int,
    target: Int,
    rounds: Int,
    total: Int,
    dayCount: Int,
    onTick: () -> Unit,
    onOpenList: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        // ضغط سريع (M3 Expressive): نابض مكاني حاد بلا ارتداد مرئي.
        animationSpec = motionSpatial(stiffness = KararTokens.Motion.SpatialStiffnessFast, dampingRatio = KararTokens.Motion.SpatialDampingFast),
        label = "tasbihScale"
    )
    val progress = (count.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    // تُقرأ ألوان الثيم قبل Canvas (القراءة داخل draw lambda غير مسموحة).
    val progressGold = KararColors.Gold
    val sky = Sky
    val violet = Violet

    Text(
        dhikr.text,
        color = KararColors.TextPrimary,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        lineHeight = 36.sp
    )
    Spacer(Modifier.height(4.dp))
    Text(dhikr.group, color = KararColors.TextMuted, fontSize = 11.sp)
    Spacer(Modifier.height(18.dp))

    Box(
        modifier = Modifier
            .size(296.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(interactionSource = interaction, indication = null) { onTick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val strokeWidth = 15.dp.toPx()
            val radius = size.minDimension / 2f - strokeWidth
            val topLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2, radius * 2)

            // هالة خارجية
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(sky.copy(alpha = 0.16f), Color.Transparent),
                    center = center,
                    radius = size.minDimension / 2f
                ),
                radius = size.minDimension / 2f
            )
            // المسار الخلفي
            drawArc(
                color = sky.copy(alpha = 0.14f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            // التقدم
            if (progress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(sky, violet, progressGold, sky),
                        center = center
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // القرص الداخلي — مثل الدائرة ذات الحد الأزرق في HTML
        Box(
            modifier = Modifier
                .size(238.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(KararColors.SurfaceHigh, KararColors.Card)
                    )
                )
                .border(8.dp, Sky, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = count.toString(),
                    // الرقم على سطح من اللوحة ⇒ نص اللوحة لا أبيض ثابت.
                    color = KararColors.TextPrimary,
                    fontSize = 78.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "من $target",
                    color = KararColors.TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp))
    Text("اضغط في أي مكان داخل الدائرة للتسبيح", color = KararColors.TextMuted, fontSize = 10.sp)
    Spacer(Modifier.height(16.dp))

    // شريط التقدم والهدف
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("الهدف:", color = KararColors.TextMuted, fontSize = 12.sp)
        Text("$target", color = Sky, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        if (rounds > 0) InfoPill("الدورات: $rounds", Violet)
        if (dayCount > 0) InfoPill("اليوم: $dayCount", KararColors.Gold)
    }
    Spacer(Modifier.height(8.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50))
            .background(Sky.copy(alpha = 0.12f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(Brush.horizontalGradient(listOf(Sky, Violet)))
        )
    }
    Spacer(Modifier.height(14.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SmallPill("تغيير الذكر") { onOpenList() }
        InfoPill("الإجمالي: $total", Sky)
    }
}

// ============================================================================
//  تبويب قائمة التسبيحات
// ============================================================================
@Composable
private fun TasbihListTab(
    items: List<TasbihDhikr>,
    presetCount: Int,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onAdd: () -> Unit,
    onDelete: (Int) -> Unit
) {
    // تجميع ديناميكي حسب `group` مع الحفاظ على ترتيب أول ظهور للمجموعة.
    // الفائدة: أي ذكر جديد يظهر داخل مجموعته التي أُنشئ فيها (لا في «أذكاري»
    // دائمًا)، والأذكار الجاهزة تبقى في مجموعاتها كما هي.
    val grouped = items.withIndex().groupBy(
        keySelector = { it.value.group.ifBlank { TasbihPresets.CUSTOM_GROUP } },
        valueTransform = { it.index }
    )
    // حالة الطي/الفرد يحفظها كل MergedAccordion باسم مجموعته.
    grouped.forEach { (group, indices) ->
        // كل مجموعة «نافذة منسدلة» واحدة: العنوان والعدد مدمجان في إطار
        // المجموعة، والأذكار تُفرد داخله وتُطوى بنابض (لا عنوان منفصل).
        MergedAccordion(
            title = group,
            titleColor = KararColors.Gold,
            trailingText = "${indices.size}",
            icon = null,
            container = KararColors.CardSoft,
            borderColor = KararColors.Border,
            corner = 16.dp,
            initiallyOpen = true,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 10.dp,
                end = 10.dp,
                top = 6.dp,
                bottom = 10.dp
            )
        ) {
            indices.forEach { index ->
                DhikrRow(
                    index = index,
                    dhikr = items[index],
                    selected = index == selectedIndex,
                    onClick = { onSelect(index) },
                    onDelete = if (index >= presetCount) ({ onDelete(index) }) else null
                )
                Spacer(Modifier.height(8.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
    }
    Spacer(Modifier.height(4.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Sky.copy(alpha = 0.12f))
            .border(1.dp, Sky.copy(alpha = 0.34f), RoundedCornerShape(18.dp))
            .clickable(onClick = onAdd)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("＋ إضافة تسبيح جديد", color = Sky, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DhikrRow(
    index: Int,
    dhikr: TasbihDhikr,
    selected: Boolean,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) Sky.copy(alpha = 0.13f) else KararColors.CardSoft)
            .border(
                1.dp,
                if (selected) Sky.copy(alpha = 0.7f) else KararColors.Border,
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (selected) Sky else KararColors.CardSoft),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "${index + 1}",
                color = if (selected) OnAccent else KararColors.TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                dhikr.text,
                color = if (selected) Sky else KararColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(dhikr.group, color = KararColors.TextMuted, fontSize = 10.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("${dhikr.target}", color = Sky, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("مرة", color = KararColors.TextMuted, fontSize = 9.sp)
        }
        if (onDelete != null) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(KararColors.Danger.copy(alpha = 0.12f))
                    .clickable(onClick = onDelete),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.DeleteOutline,
                    contentDescription = "حذف الذكر",
                    tint = KararColors.Danger,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ============================================================================
//  تبويب الإحصاءات
// ============================================================================
@Composable
private fun TasbihStatsTab(
    dayCount: Int,
    total: Int,
    rounds: Int,
    onResetDay: () -> Unit,
    onResetAll: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), corner = 22.dp) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatColumn("اليوم", dayCount, Sky)
            StatColumn("الإجمالي", total, KararColors.TextPrimary)
            StatColumn("الدورات", rounds, Violet)
        }
    }
    Spacer(Modifier.height(10.dp))
    GlassCard(modifier = Modifier.fillMaxWidth(), corner = 22.dp) {
        SectionTitle("ملاحظة")
        Spacer(Modifier.height(6.dp))
        Text(
            "عداد «اليوم» يتصفّر تلقائيًا كل 24 ساعة، بينما «الإجمالي» يبقى محفوظًا على جهازك. " +
                "كل تسبيحة تُحفظ فورًا، ولا تحتاج إنترنت.",
            color = KararColors.TextMuted,
            fontSize = 11.sp,
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SmallPill("تصفير اليوم", onClick = onResetDay)
            SmallPill("حذف كل الإحصاءات", danger = true, onClick = onResetAll)
        }
    }
}

@Composable
private fun StatColumn(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value.toString(),
            color = color,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Text(label, color = KararColors.TextMuted, fontSize = 11.sp)
    }
}

// ============================================================================
//  تبويب إعدادات المسبحة
// ============================================================================
@Composable
private fun TasbihSettingsTab(
    haptics: Boolean,
    sound: Boolean,
    autoAdvance: Boolean,
    overlay: Boolean,
    onHaptics: (Boolean) -> Unit,
    onSound: (Boolean) -> Unit,
    onAutoAdvance: (Boolean) -> Unit,
    onOverlay: (Boolean) -> Unit,
    onResetAll: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), corner = 22.dp) {
        TasbihToggleRow(
            title = "الاهتزاز مع كل تسبيحة",
            subtitle = "استجابة لمسية خفيفة",
            checked = haptics,
            onChange = onHaptics
        )
        Spacer(Modifier.height(6.dp))
        TasbihToggleRow(
            title = "الصوت",
            subtitle = "نقرة قصيرة عند التسبيح ونغمة عند الإتمام",
            checked = sound,
            onChange = onSound
        )
        Spacer(Modifier.height(6.dp))
        TasbihToggleRow(
            title = "الانتقال التلقائي",
            subtitle = "بعد إتمام الهدف انتقل للذكر التالي في تسبيح الزهراء",
            checked = autoAdvance,
            onChange = onAutoAdvance
        )
        Spacer(Modifier.height(6.dp))
        TasbihToggleRow(
            title = "شاشة التهنئة",
            subtitle = "إظهار «أحسنت» عند إتمام الهدف",
            checked = overlay,
            onChange = onOverlay
        )
    }
    Spacer(Modifier.height(10.dp))
    GlassCard(modifier = Modifier.fillMaxWidth(), corner = 22.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("حذف كل بيانات المسبحة", color = KararColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("اليوم والإجمالي والدورات", color = KararColors.TextMuted, fontSize = 10.sp)
            }
            SmallPill("حذف", danger = true, onClick = onResetAll)
        }
    }
}

@Composable
private fun TasbihToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = KararColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = KararColors.TextMuted, fontSize = 10.sp, lineHeight = 15.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = OnAccent,
                checkedTrackColor = Sky,
                uncheckedThumbColor = KararColors.TextMuted,
                uncheckedTrackColor = KararColors.SurfaceHigh
            )
        )
    }
}

// ============================================================================
//  عناصر صغيرة
// ============================================================================
@Composable
private fun InfoPill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SmallPill(text: String, danger: Boolean = false, onClick: () -> Unit) {
    val color = if (danger) KararColors.Danger else Sky
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

// ============================================================================
//  نوافذ التأكيد والتهنئة
// ============================================================================
@Composable
private fun AddDhikrDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onAdd: (text: String, target: Int, group: String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("33") }
    var group by remember { mutableStateOf("") }

    SpringDialog(visible = visible, onDismissRequest = onDismiss, imePadding = true) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            corner = 24.dp,
            color = KararColors.Card,
            borderColor = Sky.copy(alpha = 0.35f),
            contentPadding = PaddingValues(20.dp)
        ) {
            Text("إضافة تسبيح جديد", color = KararColors.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(
                "اكتب الذكر وحدّد عدده، وسيُضاف إلى قائمتك الخاصة.",
                color = KararColors.TextMuted,
                fontSize = 10.sp
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("نص الذكر", fontSize = 11.sp) },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(14.dp)
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { value -> targetText = value.filter { it.isDigit() }.take(4) },
                    modifier = Modifier.weight(1f),
                    label = { Text("الهدف", fontSize = 11.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp)
                )
                OutlinedTextField(
                    value = group,
                    onValueChange = { group = it },
                    modifier = Modifier.weight(1.4f),
                    label = { Text("المجموعة (اختياري)", fontSize = 11.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallPill("إلغاء", onClick = onDismiss)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(Sky)
                        .clickable { onAdd(text, targetText.toIntOrNull() ?: 33, group) }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "إضافة التسبيح",
                        color = OnAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletionDialog(
    visible: Boolean,
    target: Int,
    nextDhikr: String,
    onContinue: () -> Unit,
    onChange: () -> Unit
) {
    val pulse = rememberInfiniteTransition(label = "completePulse")
    // «تقليل الحركة»: بطاقة النجاح ثابتة بلا نبض.
    val ambient = ambientMotionEnabled()
    val scaleState: State<Float> = if (ambient) {
        pulse.animateFloat(
            initialValue = 1f,
            targetValue = 1.12f,
            animationSpec = infiniteRepeatable(tween(850, easing = KararTokens.Motion.Fluid), RepeatMode.Reverse),
            label = "completeScale"
        )
    } else remember { mutableStateOf(1f) }
    val scale by scaleState
    SpringDialog(visible = visible, onDismissRequest = onContinue) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            corner = 28.dp,
            color = KararColors.Card,
            borderColor = Sky.copy(alpha = 0.4f),
            contentPadding = PaddingValues(24.dp)
        ) {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Sky,
                    modifier = Modifier
                        .size(72.dp)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                )
                Spacer(Modifier.height(12.dp))
                Text("أحسنت!", color = KararColors.TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "لقد أتممت التسبيح $target مرة",
                    color = KararColors.TextPrimary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "التالي: $nextDhikr",
                    color = KararColors.TextMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(18.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Sky)
                        .clickable(onClick = onContinue)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("متابعة", color = OnAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(KararColors.SurfaceHigh)
                        .clickable(onClick = onChange)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("تغيير التسبيح", color = KararColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TasbihConfirmDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    SpringDialog(visible = visible, onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            corner = 24.dp,
            color = KararColors.Card,
            borderColor = KararColors.Border,
            contentPadding = PaddingValues(20.dp)
        ) {
            Text(title, color = KararColors.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(message, color = KararColors.TextMuted, fontSize = 12.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(KararColors.SurfaceHigh)
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("إلغاء", color = KararColors.TextPrimary, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(KararColors.Danger)
                        .clickable(onClick = onConfirm)
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(confirmLabel, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
