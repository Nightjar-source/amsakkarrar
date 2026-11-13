package com.Nightjar.amsakkarrar.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.text.FontFamily
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.Nightjar.amsakkarrar.MainActivity
import com.Nightjar.amsakkarrar.notifications.PrayerEntry
import com.Nightjar.amsakkarrar.notifications.PrayerSnapshot
import com.Nightjar.amsakkarrar.notifications.PrayerSnapshotLoader
import com.Nightjar.amsakkarrar.notifications.SettingsLoader
import com.karrar.amsakiah.core.astronomy.HijriDate
import com.karrar.amsakiah.core.jinan.DailyDeed
import com.karrar.amsakiah.core.jinan.DailyDeedSource
import com.karrar.amsakiah.core.jinan.dailyDeeds
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.ui.theme.SkyTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.roundToInt

// ---------- ألوان الويدجت (مطابقة لـ widgetsnotifications.html) ----------
private val WidgetCard = Color(0x33000000)
private val WidgetActive = Color(0x26FFB432)
private val Gold = Color(0xFFFFD700)
private val GoldSoft = Color(0xFFFAFAD2)
private val TextPrimary = Color(0xFFF5F5F7)
private val TextSecondary = Color(0xB3B9C0D4)
private val TextMuted = Color(0x99B9C0D4)
private val DayGradient = Color(0xFFFF9F1C)
private val NightGradient = Color(0xFF80A1D4)

/**
 * ثلاث درجات تكيّف، تُحسب من حجم الودجت الفعلي على الشاشة:
 * - COMPACT: 2×2 (يُخفى غير الأساسي)
 * - MEDIUM : 4×3
 * - LARGE  : 4×4 و4×5 (كل العناصر)
 */
internal enum class WidgetTier { COMPACT, MEDIUM, LARGE }

@Composable
private fun rememberWidgetTier(): WidgetTier {
    val size = LocalSize.current
    // على Android 7–11 لا يوفّر المشغّل أبعادًا دقيقة، وقد تصل القيمة صفرًا
    // عند أول تركيب. نرجع إلى MEDIUM بدل الانهيار إلى COMPACT، لأن COMPACT
    // يُخفي الصلوات ويُظهر الودجت ناقصًا على أجهزة قديمة.
    if (size.width <= 0.dp || size.height <= 0.dp) return WidgetTier.MEDIUM
    // العرض هو الفيصل: 2 أعمدة = مصغّر، 4 أعمدة = كامل. الارتفاع يكمّل الحالة.
    return when {
        size.width < 200.dp || size.height < 120.dp -> WidgetTier.COMPACT
        size.width < 300.dp || size.height < 250.dp -> WidgetTier.MEDIUM
        else -> WidgetTier.LARGE
    }
}

private fun color(value: Color): ColorProvider = ColorProvider(value)

private fun launchIntent(context: Context): Intent =
    Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_MAIN
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

/** الصلوات الست الظاهرة في الودجت (نفس مربعات HTML). */
private val WIDGET_PRAYERS = listOf("imsak", "fajr", "dhuhr", "asr", "maghrib", "isha")

private fun PrayerSnapshot.widgetEntries(): List<PrayerEntry> {
    val byId = entries.associateBy { it.id }
    return WIDGET_PRAYERS.mapNotNull { byId[it] }
}

private fun tierPadding(tier: WidgetTier): Dp = when (tier) {
    WidgetTier.COMPACT -> 9.dp
    WidgetTier.MEDIUM -> 13.dp
    WidgetTier.LARGE -> 17.dp
}

private fun tierCorner(tier: WidgetTier): Dp = when (tier) {
    WidgetTier.COMPACT -> 22.dp
    WidgetTier.MEDIUM -> 28.dp
    WidgetTier.LARGE -> 32.dp
}

// ============================================================================
//  الإطار المتكيّف — خلفية + حدود + زر إعدادات + فتح التطبيق
// ============================================================================

@Composable
private fun WidgetFrame(
    context: Context,
    appWidgetId: Int,
    tier: WidgetTier,
    center: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val background = Color(WidgetStyleStore.backgroundArgb(context, appWidgetId))
    val border = Color(WidgetStyleStore.borderArgb(context, appWidgetId))
    val radius = tierCorner(tier)
    val pad = tierPadding(tier)

    // لا زر إعدادات داخل الودجت (كان ⚙ يزاحم المحتوى ويسرق مساحة).
    // الخلفية تُضبط من شاشة «الودجتات» داخل التطبيق أو بالضغط المطوّل على
    // الودجت ← «تعديل» (widgetFeatures=reconfigurable).
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(border)
            .cornerRadius(radius)
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(1.dp)
                .background(background)
                .cornerRadius(radius - 1.dp)
                .padding(pad)
                .clickable(actionStartActivity(launchIntent(context))),
            // المحتوى يُوزَّع على كامل ارتفاع الودجت بدل الالتصاق بالأعلى،
            // فلا يبقى فراغ أسفل البطاقة.
            verticalAlignment = if (center) Alignment.Vertical.CenterVertically else Alignment.Vertical.Top,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            content = content
        )
    }
}

/**
 * كشيدة الشهر: «رمضـ 16 ـان» — الحروف ذهبية والرقم أبيض.
 *
 * ملاحظة صريحة: Glance يقبل فقط أسماء خطوط النظام عبر `FontFamily(String)`
 * ولا يسمح بخط مخصص داخل Compose Glance. لذلك تُرسم الكشيدة بأقرب خط نظام
 * (`serif`). للمتابعة بخط Reem Kufi حرفيًا يجب تحويل الكشيدة إلى RemoteViews
 * مع `@font/reem_kufi` — قابل للإضافة عند الطلب.
 */
private val ReemKufi = FontFamily.Serif

@Composable
private fun WidgetKashida(snapshot: PrayerSnapshot, fontSize: Int = 26) {
    val (pre, post) = SkyTheme.getKashidaMonthParts(snapshot.hijriMonth)
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = pre,
            style = TextStyle(
                color = color(Gold),
                fontSize = (fontSize + 2).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ReemKufi
            )
        )
        Text(
            text = " ${snapshot.hijriDay} ",
            style = TextStyle(
                color = color(TextPrimary),
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ReemKufi
            )
        )
        Text(
            text = post,
            style = TextStyle(
                color = color(Gold),
                fontSize = (fontSize + 2).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ReemKufi
            )
        )
    }
}

/** شريط تقدّم بشكل حبة دواء (بديل Pill في HTML). */
@Composable
private fun PillProgress(fraction: Float, isDaytime: Boolean, inset: Dp = 18.dp) {
    val f = fraction.coerceIn(0f, 1f)
    // Glance لا يدعم fillMaxWidth(fraction)؛ نحسب العرض من حجم الودجت ناقص الحاشية الفعلية.
    val available = (LocalSize.current.width - inset * 2).coerceAtLeast(1.dp)
    val fillWidth = (available * f).coerceAtLeast(0.dp)
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(8.dp)
            .background(Color(0x66000000))
            .cornerRadius(50.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (fillWidth > 0.dp) {
            Box(
                modifier = GlanceModifier
                    .width(fillWidth)
                    .fillMaxHeight()
                    .background(if (isDaytime) DayGradient else NightGradient)
                    .cornerRadius(50.dp)
            ) {}
        }
    }
}

@Composable
private fun PrayerBoxCell(entry: PrayerEntry, active: Boolean, modifier: GlanceModifier, compact: Boolean) {
    Column(
        modifier = modifier
            .background(if (active) WidgetActive else WidgetCard)
            .cornerRadius(if (compact) 9.dp else 14.dp)
            .padding(vertical = if (compact) 3.dp else 6.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = entry.label,
            style = TextStyle(
                color = color(if (active) Gold else TextSecondary),
                fontSize = if (compact) 6.sp else 7.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = entry.time.substringBefore(" "),
            style = TextStyle(
                color = color(if (active) GoldSoft else TextPrimary),
                fontSize = if (compact) 9.sp else 11.sp,
                fontWeight = FontWeight.Bold
            )
        )
        if (!compact) {
            Text(
                text = entry.time.substringAfter(" ", ""),
                style = TextStyle(color = color(TextMuted), fontSize = 6.sp)
            )
        }
    }
}

/** شبكة 3×2 لمربعات الصلوات الست. */
@Composable
private fun PrayerGridRow(entries: List<PrayerEntry>, activeLabel: String, compact: Boolean = false) {
    if (entries.isEmpty()) return
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        entries.forEachIndexed { index, entry ->
            if (index > 0) Spacer(modifier = GlanceModifier.width(if (compact) 3.dp else 6.dp))
            PrayerBoxCell(
                entry = entry,
                active = entry.label == activeLabel,
                modifier = GlanceModifier.defaultWeight(),
                compact = compact
            )
        }
        repeat((3 - entries.size).coerceAtLeast(0)) {
            Spacer(modifier = GlanceModifier.defaultWeight())
        }
    }
}

private fun appWidgetIdFor(context: Context, id: GlanceId): Int =
    runCatching { GlanceAppWidgetManager(context).getAppWidgetId(id) }
        .getOrDefault(AppWidgetManager.INVALID_APPWIDGET_ID)

// ============================================================================
//  1) ويدجت «اليوم» — التصميم الكامل المتكيّف
// ============================================================================
class TodayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayWidget()
}

class TodayWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode get() = SizeMode.Exact

    // بديل آمن عند فشل التركيب — سياسة Android 17 للودجتات.
    override fun onCompositionError(
        context: Context,
        glanceId: GlanceId,
        appWidgetId: Int,
        throwable: Throwable
    ) = WidgetErrorViews.show(context, appWidgetId, throwable)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = PrayerSnapshotLoader.load(context)
        val widgetId = appWidgetIdFor(context, id)
        provideContent {
            GlanceTheme { TodayWidgetContent(context, snapshot, widgetId) }
        }
    }
}

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
@Composable
private fun TodayWidgetContent(context: Context, snapshot: PrayerSnapshot, appWidgetId: Int) {
    val tier = rememberWidgetTier()
    val gridEntries = snapshot.widgetEntries()
    WidgetFrame(context, appWidgetId, tier) {
        WidgetKashida(
            snapshot,
            fontSize = when (tier) {
                WidgetTier.COMPACT -> 17
                WidgetTier.MEDIUM -> 22
                else -> 27
            }
        )
        Spacer(GlanceModifier.height(if (tier == WidgetTier.COMPACT) 1.dp else 2.dp))
        Text(
            text = snapshot.gregorianText,
            style = TextStyle(
                color = color(TextMuted),
                fontSize = if (tier == WidgetTier.COMPACT) 6.sp else 8.sp,
                textAlign = TextAlign.Center
            )
        )
        Spacer(GlanceModifier.height(if (tier == WidgetTier.LARGE) 7.dp else 4.dp))

        // العدّاد — حبة «باقي على…» + Chronometer أصلي + شريط تقدم سميك + طرفا الشريط
        // كلها داخل RemoteViews واحد مطابق لـ widgetsnotifications.html.
        // MEDIUM يستخدم السطر المصغّر حتى تتّسع شبكة الصلوات الست داخل 4×3.
        AndroidRemoteViews(
            remoteViews = countdownRemoteViews(context, snapshot, compact = tier != WidgetTier.LARGE),
            modifier = GlanceModifier.fillMaxWidth()
        )

        when (tier) {
            WidgetTier.LARGE -> {
                Spacer(GlanceModifier.height(7.dp))
                PrayerGridRow(gridEntries.take(3), snapshot.nextLabel)
                Spacer(GlanceModifier.height(5.dp))
                PrayerGridRow(gridEntries.drop(3).take(3), snapshot.nextLabel)
            }
            WidgetTier.MEDIUM -> {
                Spacer(GlanceModifier.height(5.dp))
                PrayerGridRow(gridEntries.take(3), snapshot.nextLabel, compact = true)
                Spacer(GlanceModifier.height(3.dp))
                PrayerGridRow(gridEntries.drop(3).take(3), snapshot.nextLabel, compact = true)
            }
            WidgetTier.COMPACT -> {
                Spacer(GlanceModifier.height(3.dp))
                PillProgress(snapshot.periodProgress, snapshot.isDaytime, inset = 10.dp)
                Spacer(GlanceModifier.height(6.dp))
                PrayerGridRow(gridEntries.filter { it.id == "fajr" || it.id == "maghrib" || it.id == "isha" }, snapshot.nextLabel, compact = true)
            }
        }
    }
}

// ============================================================================
//  2) ويدجت «المواقيت» — مربعات الصلوات
// ============================================================================
class TimesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TimesWidget()
}

class TimesWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode get() = SizeMode.Exact

    override fun onCompositionError(
        context: Context,
        glanceId: GlanceId,
        appWidgetId: Int,
        throwable: Throwable
    ) = WidgetErrorViews.show(context, appWidgetId, throwable)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = PrayerSnapshotLoader.load(context)
        val widgetId = appWidgetIdFor(context, id)
        provideContent {
            GlanceTheme { TimesWidgetContent(context, snapshot, widgetId) }
        }
    }
}

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
@Composable
private fun TimesWidgetContent(context: Context, snapshot: PrayerSnapshot, appWidgetId: Int) {
    val tier = rememberWidgetTier()
    val gridEntries = snapshot.widgetEntries()
    WidgetFrame(context, appWidgetId, tier) {
        if (tier != WidgetTier.COMPACT) {
            WidgetKashida(snapshot, fontSize = if (tier == WidgetTier.MEDIUM) 18 else 22)
            Spacer(GlanceModifier.height(2.dp))
        }
        Text(
            text = if (tier == WidgetTier.COMPACT) snapshot.city else "${snapshot.city} • ${snapshot.gregorianText}",
            style = TextStyle(
                color = color(if (tier == WidgetTier.COMPACT) Gold else TextMuted),
                fontSize = if (tier == WidgetTier.COMPACT) 9.sp else 8.sp,
                fontWeight = if (tier == WidgetTier.COMPACT) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        )
        Spacer(GlanceModifier.height(if (tier == WidgetTier.COMPACT) 3.dp else 7.dp))
        PrayerGridRow(gridEntries.take(3), snapshot.nextLabel, compact = tier == WidgetTier.COMPACT)
        Spacer(GlanceModifier.height(if (tier == WidgetTier.COMPACT) 3.dp else 6.dp))
        PrayerGridRow(gridEntries.drop(3).take(3), snapshot.nextLabel, compact = tier == WidgetTier.COMPACT)
        Spacer(GlanceModifier.height(if (tier == WidgetTier.COMPACT) 3.dp else 6.dp))
        AndroidRemoteViews(
            remoteViews = countdownRemoteViews(context, snapshot, compact = tier != WidgetTier.LARGE),
            modifier = GlanceModifier.fillMaxWidth()
        )
    }
}

// ============================================================================
//  3) ويدجت «العدّاد» — الوقت المتبقي + وضع الجزيرة العريض
// ============================================================================
class CountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownWidget()
}

class CountdownWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode get() = SizeMode.Exact

    override fun onCompositionError(
        context: Context,
        glanceId: GlanceId,
        appWidgetId: Int,
        throwable: Throwable
    ) = WidgetErrorViews.show(context, appWidgetId, throwable)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = PrayerSnapshotLoader.load(context)
        val widgetId = appWidgetIdFor(context, id)
        provideContent {
            GlanceTheme { CountdownWidgetContent(context, snapshot, widgetId) }
        }
    }
}

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
@Composable
private fun CountdownWidgetContent(context: Context, snapshot: PrayerSnapshot, appWidgetId: Int) {
    val size = LocalSize.current
    val tier = rememberWidgetTier()
    // وضع «الجزيرة»: عريض وقصير — يُعرض أفقياً كشريط حالة
    val island = size.width > 230.dp && size.height < 135.dp
    WidgetFrame(context, appWidgetId, tier, center = !island) {
        if (island) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "باقي على ${snapshot.nextLabel}",
                        style = TextStyle(color = color(Gold), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${snapshot.city} • ${snapshot.nextTime}",
                        style = TextStyle(color = color(TextMuted), fontSize = 8.sp)
                    )
                }
                AndroidRemoteViews(
                    remoteViews = countdownRemoteViews(context, snapshot, compact = true),
                    modifier = GlanceModifier.width(150.dp)
                )
            }
            Spacer(GlanceModifier.height(5.dp))
            PillProgress(snapshot.periodProgress, snapshot.isDaytime, inset = tierPadding(tier) + 1.dp)
        } else {
            if (tier != WidgetTier.COMPACT) {
                WidgetKashida(snapshot, fontSize = if (tier == WidgetTier.MEDIUM) 16 else 19)
                Spacer(GlanceModifier.height(4.dp))
            }
            AndroidRemoteViews(
                remoteViews = countdownRemoteViews(context, snapshot, compact = tier == WidgetTier.COMPACT),
                modifier = GlanceModifier.fillMaxWidth()
            )
            if (tier == WidgetTier.COMPACT) {
                Spacer(GlanceModifier.height(3.dp))
                PillProgress(snapshot.periodProgress, snapshot.isDaytime, inset = 10.dp)
            }
            Spacer(GlanceModifier.height(3.dp))
            Text(
                text = snapshot.city,
                style = TextStyle(
                    color = color(TextMuted),
                    fontSize = if (tier == WidgetTier.COMPACT) 6.sp else 7.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

// ============================================================================
//  4) ويدجت «الهجري»
// ============================================================================
class HijriWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HijriWidget()
}

class HijriWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode get() = SizeMode.Exact

    override fun onCompositionError(
        context: Context,
        glanceId: GlanceId,
        appWidgetId: Int,
        throwable: Throwable
    ) = WidgetErrorViews.show(context, appWidgetId, throwable)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = PrayerSnapshotLoader.load(context)
        val widgetId = appWidgetIdFor(context, id)
        provideContent {
            GlanceTheme { HijriWidgetContent(context, snapshot, widgetId) }
        }
    }
}

@Composable
private fun HijriWidgetContent(context: Context, snapshot: PrayerSnapshot, appWidgetId: Int) {
    val tier = rememberWidgetTier()
    WidgetFrame(context, appWidgetId, tier, center = true) {
        WidgetKashida(snapshot, fontSize = if (tier == WidgetTier.COMPACT) 16 else 20)
        Spacer(GlanceModifier.height(2.dp))
        Text(
            text = "${snapshot.hijriMonth} ${snapshot.hijriYear} هـ",
            style = TextStyle(
                color = color(TextPrimary),
                fontSize = if (tier == WidgetTier.COMPACT) 9.sp else 11.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(GlanceModifier.height(2.dp))
        Text(
            text = snapshot.gregorianText,
            style = TextStyle(
                color = color(TextMuted),
                fontSize = if (tier == WidgetTier.COMPACT) 6.sp else 8.sp,
                textAlign = TextAlign.Center
            )
        )
        if (tier != WidgetTier.COMPACT) {
            Spacer(GlanceModifier.height(5.dp))
            Text(
                text = "${snapshot.city} • ${snapshot.dayProgressPercent()}٪ من النهار",
                style = TextStyle(color = color(TextSecondary), fontSize = 8.sp)
            )
            Spacer(GlanceModifier.height(5.dp))
            PillProgress(snapshot.dayProgress, snapshot.isDaytime, inset = tierPadding(tier) + 1.dp)
        }
    }
}

private fun PrayerSnapshot.dayProgressPercent(): Int = (dayProgress * 100).roundToInt().coerceIn(0, 100)

// ============================================================================
//  5) ويدجت «الساعة» — TextClock أصلي + التاريخ
// ============================================================================
class ClockWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ClockWidget()
}

class ClockWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode get() = SizeMode.Exact

    override fun onCompositionError(
        context: Context,
        glanceId: GlanceId,
        appWidgetId: Int,
        throwable: Throwable
    ) = WidgetErrorViews.show(context, appWidgetId, throwable)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = PrayerSnapshotLoader.load(context)
        val widgetId = appWidgetIdFor(context, id)
        provideContent {
            GlanceTheme { ClockWidgetContent(context, snapshot, widgetId) }
        }
    }
}

@OptIn(ExperimentalGlanceRemoteViewsApi::class)
@Composable
private fun ClockWidgetContent(context: Context, snapshot: PrayerSnapshot, appWidgetId: Int) {
    val tier = rememberWidgetTier()
    WidgetFrame(context, appWidgetId, tier, center = true) {
        AndroidRemoteViews(
            remoteViews = clockRemoteViews(context, snapshot, compact = tier == WidgetTier.COMPACT),
            modifier = GlanceModifier.fillMaxWidth()
        )
        if (tier != WidgetTier.COMPACT) {
            Spacer(GlanceModifier.height(5.dp))
            Text(
                text = "باقي على ${snapshot.nextLabel} • ${snapshot.nextTime}",
                style = TextStyle(
                    color = color(Gold),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

// ============================================================================
//  6) ويدجت «المسار» — خط زمني عمودي للصلوات
// ============================================================================
class JourneyWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = JourneyWidget()
}

class JourneyWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode get() = SizeMode.Exact

    override fun onCompositionError(
        context: Context,
        glanceId: GlanceId,
        appWidgetId: Int,
        throwable: Throwable
    ) = WidgetErrorViews.show(context, appWidgetId, throwable)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = PrayerSnapshotLoader.load(context)
        val widgetId = appWidgetIdFor(context, id)
        provideContent {
            GlanceTheme { JourneyWidgetContent(context, snapshot, widgetId) }
        }
    }
}

@Composable
private fun JourneyWidgetContent(context: Context, snapshot: PrayerSnapshot, appWidgetId: Int) {
    val tier = rememberWidgetTier()
    val entries = snapshot.widgetEntries()
    val visible = when (tier) {
        WidgetTier.COMPACT -> entries.take(4)
        WidgetTier.MEDIUM -> entries.take(5)
        WidgetTier.LARGE -> entries
    }
    WidgetFrame(context, appWidgetId, tier) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = "مسار اليوم",
                style = TextStyle(
                    color = color(Color(0xFFF472B6)),
                    fontSize = if (tier == WidgetTier.COMPACT) 9.sp else 11.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(GlanceModifier.defaultWeight())
            Text(
                text = "باقي على ${snapshot.nextLabel}",
                style = TextStyle(color = color(Gold), fontSize = if (tier == WidgetTier.COMPACT) 8.sp else 10.sp, fontWeight = FontWeight.Bold)
            )
        }
        Spacer(GlanceModifier.height(if (tier == WidgetTier.COMPACT) 3.dp else 6.dp))
        visible.forEach { entry ->
            val active = entry.isNext
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(vertical = if (tier == WidgetTier.COMPACT) 1.dp else 2.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .width(if (tier == WidgetTier.COMPACT) 5.dp else 7.dp)
                        .height(if (tier == WidgetTier.COMPACT) 5.dp else 7.dp)
                        .background(if (active) Color(0xFFF472B6) else Color(0x33FFFFFF))
                        .cornerRadius(50.dp)
                ) {}
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    text = entry.label,
                    modifier = GlanceModifier.defaultWeight(),
                    style = TextStyle(
                        color = color(if (active) Color(0xFFF472B6) else Color(0xFFE2E8F0)),
                        fontSize = if (tier == WidgetTier.COMPACT) 7.sp else 9.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                    )
                )
                Text(
                    text = entry.time,
                    style = TextStyle(
                        color = color(if (active) Gold else TextMuted),
                        fontSize = if (tier == WidgetTier.COMPACT) 7.sp else 9.sp
                    )
                )
            }
        }
        if (tier == WidgetTier.LARGE) {
            Spacer(GlanceModifier.height(4.dp))
            PillProgress(snapshot.periodProgress, snapshot.isDaytime, inset = tierPadding(tier) + 1.dp)
        }
    }
}

// ============================================================================
//  8) ويدجت «أعمال اليوم» — أعمال الليلة/اليوم/يوم الأسبوع/الشهر العامة
// ============================================================================
class DailyDeedsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailyDeedsWidget()
}

class DailyDeedsWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode get() = SizeMode.Exact

    override fun onCompositionError(
        context: Context,
        glanceId: GlanceId,
        appWidgetId: Int,
        throwable: Throwable
    ) = WidgetErrorViews.show(context, appWidgetId, throwable)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settings = SettingsLoader.load(context)
        val today = PrayerSnapshotLoader.todayDate(settings)
        val hijri = PrayerCalculator.hijriFor(settings, today)
        val deeds = dailyDeeds(today, hijri)
        val snapshot = PrayerSnapshotLoader.load(context)
        val widgetId = appWidgetIdFor(context, id)
        provideContent {
            GlanceTheme { DailyDeedsWidgetContent(context, snapshot, widgetId, deeds) }
        }
    }
}

@Composable
private fun DailyDeedsWidgetContent(
    context: Context,
    snapshot: PrayerSnapshot,
    appWidgetId: Int,
    deeds: List<DailyDeed>
) {
    val tier = rememberWidgetTier()
    val night = deeds.count { it.source == DailyDeedSource.MONTH_NIGHT }
    val day = deeds.count { it.source == DailyDeedSource.MONTH_DAY }
    val week = deeds.count { it.source == DailyDeedSource.WEEKDAY }
    val general = deeds.count { it.source == DailyDeedSource.MONTH_GENERAL }

    WidgetFrame(context, appWidgetId, tier) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                "أعمال اليوم",
                style = TextStyle(
                    color = color(Gold),
                    fontSize = if (tier == WidgetTier.COMPACT) 9.sp else 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(GlanceModifier.defaultWeight())
            Text(
                snapshot.hijriText,
                style = TextStyle(
                    color = color(TextMuted),
                    fontSize = if (tier == WidgetTier.COMPACT) 6.sp else 8.sp
                )
            )
        }
        Spacer(GlanceModifier.height(if (tier == WidgetTier.COMPACT) 2.dp else 5.dp))

        // عدّادات ليلة/يوم/أسبوع/عامة — أعمدة متساوية بعنوان صغير وقيمة كبيرة.
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            DeedStatCell("الليلة", night, Color(0xFFF59E0B))
            Spacer(GlanceModifier.width(4.dp))
            DeedStatCell("اليوم", day, Gold)
            Spacer(GlanceModifier.width(4.dp))
            DeedStatCell("الأسبوع", week, Color(0xFF38BDF8))
            Spacer(GlanceModifier.width(4.dp))
            DeedStatCell("الشهر", general, Color(0xFFA78BFA))
        }
        Spacer(GlanceModifier.height(if (tier == WidgetTier.COMPACT) 2.dp else 4.dp))

        // أبرز الأعمال — بحد أقصى حسب الحجم
        val shown = when (tier) {
            WidgetTier.COMPACT -> deeds.take(1)
            WidgetTier.MEDIUM -> deeds.take(3)
            WidgetTier.LARGE -> deeds.take(5)
        }
        shown.forEach { deed ->
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(vertical = if (tier == WidgetTier.COMPACT) 0.dp else 2.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .width(4.dp)
                        .height(if (tier == WidgetTier.COMPACT) 10.dp else 14.dp)
                        .background(Gold.copy(alpha = 0.85f))
                        .cornerRadius(2.dp)
                ) {}
                Spacer(GlanceModifier.width(5.dp))
                Text(
                    deed.item.title,
                    maxLines = if (tier == WidgetTier.COMPACT) 1 else 2,
                    modifier = GlanceModifier.defaultWeight(),
                    style = TextStyle(
                        color = color(TextPrimary),
                        fontSize = if (tier == WidgetTier.COMPACT) 6.5.sp else 7.5.sp,
                        lineHeight = if (tier == WidgetTier.COMPACT) 9.sp else 11.sp
                    )
                )
            }
        }
        if (deeds.isEmpty()) {
            Text(
                "لا توجد أعمال موقوتة اليوم",
                style = TextStyle(color = color(TextMuted), fontSize = 9.sp)
            )
        }
    }
}

@Composable
private fun DeedStatCell(label: String, count: Int, accent: Color) {
    Column(
        modifier = GlanceModifier
            .defaultWeight()
            .background(WidgetCard)
            .cornerRadius(10.dp)
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            count.toString(),
            style = TextStyle(
                color = color(accent),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            label,
            style = TextStyle(color = color(TextMuted), fontSize = 6.sp)
        )
    }
}

// ============================================================================
//  محدّث كل الودجت
// ============================================================================
object WidgetUpdater {

    private val widgets = listOf(
        TodayWidget(),
        TimesWidget(),
        CountdownWidget(),
        HijriWidget(),
        TasbihWidget(),
        ClockWidget(),
        JourneyWidget(),
        DailyDeedsWidget()
    )

    /**
     * تنفيذ التحديث في الخلفية دائمًا.
     *
     * سبب التصميم:
     * - `GlanceAppWidget.updateAll` عملية معلّقة/حاجزة، واستدعاؤها من الخيط
     *   الرئيسي (onCreate/onResume/المستقبلات/لمسات الودجت) يسبب تقطيعًا وقد
     *   يصل إلى ANR مع سبعة ودجتات.
     * - القناة `CONFLATED` تدمج الطلبات المتلاحقة: أثناء تحديث جارٍ لا تتراكم
     *   طوابير، بل يُنفَّذ أحدث طلب مرة واحدة بعد انتهاء الحالي.
     * - `Mutex` يمنع تحديثين متوازيين لنفس الودجتات.
     */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val refreshLock = Mutex()
    private val requests = Channel<Context>(Channel.CONFLATED)

    init {
        scope.launch {
            for (appContext in requests) {
                runCatching { refreshInternal(appContext) }
            }
        }
    }

    /** تحديث غير حاجز — آمن من الخيط الرئيسي. */
    fun updateAll(context: Context) {
        requests.trySend(context.applicationContext)
    }

    /**
     * نسخة للمستقبلات (BroadcastReceiver):
     * تُنفّذ التحديث في الخلفية ثم تستدعي [onDone] — استخدمها مع `goAsync()`
     * لضمان عدم قتل العملية قبل اكتمال التحديث.
     */
    fun updateAllAsync(context: Context, onDone: () -> Unit) {
        val appContext = context.applicationContext
        scope.launch {
            // مهلة قصيرة تناسب نافذة BroadcastReceiver (~10 ثوانٍ) وتمنع التعليق.
            runCatching { withTimeoutOrNull(8_000) { refreshInternal(appContext) } }
            onDone()
        }
    }

    /** تحديث معلّق ينتظر إتمام كل الودجتات — يستخدمه العامل الدوري. */
    suspend fun updateAllSuspending(context: Context) {
        refreshInternal(context.applicationContext)
    }

    private suspend fun refreshInternal(appContext: Context) {
        refreshLock.withLock {
            val snapshot = runCatching { PrayerSnapshotLoader.load(appContext) }.getOrNull()
            // لا تبنِ الودجتات السبعة إن لم يكن أيّ منها مثبّتًا على الشاشة.
            if (WidgetRefreshWorker.hasAnyWidget(appContext)) {
                widgets.forEach { runCatching { it.updateAll(appContext) } }
            }
            // WorkManager: تحديث دوري مضمون في الخلفية + تحديث عند وقت الصلاة القادمة.
            // العدّاد نفسه يعمل عبر Chronometer/TextClock أصلي داخل الودجت فلا يتوقف بين التحديثات.
            WidgetRefreshWorker.syncPeriodic(appContext)
            snapshot?.let {
                WidgetRefreshWorker.scheduleBoundary(appContext, it.remainingSeconds * 1000L + 3_000L)
            }
        }
    }
}
