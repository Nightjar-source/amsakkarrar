package com.Nightjar.amsakkarrar.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
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
import com.karrar.amsakiah.ui.theme.SkyTheme

// ---------- ألوان الويدجت (مطابقة لـ widgetsnotifications.html) ----------
private val WidgetBg = Color(0xF2050A15)
private val WidgetBgSoft = Color(0xE60F1524)
private val WidgetCard = Color(0x33000000)
private val WidgetActive = Color(0x26FFB432)
private val Gold = Color(0xFFFFD700)
private val GoldSoft = Color(0xFFFAFAD2)
private val TextPrimary = Color(0xFFF5F5F7)
private val TextSecondary = Color(0xB3B9C0D4)
private val TextMuted = Color(0x99B9C0D4)
private val DayGradient = Color(0xFFFF9F1C)
private val NightGradient = Color(0xFF80A1D4)

private fun color(value: Color): ColorProvider = ColorProvider(value)

private fun launchIntent(context: Context): Intent =
    Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_MAIN
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

private fun two(value: Int): String = value.toString().padStart(2, '0')

/** الصلوات الست الظاهرة في الودجت (نفس مربعات HTML). */
private val WIDGET_PRAYERS = listOf("imsak", "fajr", "dhuhr", "asr", "maghrib", "isha")

private fun PrayerSnapshot.widgetEntries(): List<PrayerEntry> {
    val byId = entries.associateBy { it.id }
    return WIDGET_PRAYERS.mapNotNull { byId[it] }
}

// ============================================================================
//  مكوّنات مشتركة
// ============================================================================

@Composable
private fun WidgetFrame(
    context: Context,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetBg)
            .cornerRadius(32.dp)
            .padding(18.dp)
            .clickable(actionStartActivity(launchIntent(context))),
        verticalAlignment = Alignment.Vertical.Top,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        content = content
    )
}

/** كشيدة الشهر: «رمضـ 16 ـان» — الحروف ذهبية والرقم أبيض. */
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
            style = TextStyle(color = color(Gold), fontSize = (fontSize + 2).sp, fontWeight = FontWeight.Bold)
        )
        Text(
            text = " ${snapshot.hijriDay} ",
            style = TextStyle(color = color(TextPrimary), fontSize = fontSize.sp, fontWeight = FontWeight.Bold)
        )
        Text(
            text = post,
            style = TextStyle(color = color(Gold), fontSize = (fontSize + 2).sp, fontWeight = FontWeight.Bold)
        )
    }
}

/** شريط تقدّم بشكل حبة دواء (بديل Pill في HTML). */
@Composable
private fun PillProgress(fraction: Float, isDaytime: Boolean) {
    val f = fraction.coerceIn(0f, 1f)
    // Glance لا يدعم defaultWeight(fraction) ولا fillMaxWidth(fraction)؛
    // نحسب العرض الفعلي من LocalSize (حجم الودجت) مطروحًا منه حشوة الإطار 18dp لكل جهة.
    val available = (LocalSize.current.width - 36.dp).coerceAtLeast(1.dp)
    val fillWidth = (available * f).coerceAtLeast(0.dp)
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(10.dp)
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

/** عمود رقم مع تسميته (ساعة / دقيقة / ثانية). */
@Composable
private fun DigitColumn(value: String, label: String) {
    Column(
        modifier = GlanceModifier.width(52.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = value,
            style = TextStyle(color = color(TextPrimary), fontSize = 26.sp, fontWeight = FontWeight.Normal)
        )
        Text(
            text = label,
            style = TextStyle(color = color(TextMuted), fontSize = 6.sp, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun Colon() {
    Text(
        text = ":",
        modifier = GlanceModifier.padding(start = 2.dp, end = 2.dp),
        style = TextStyle(color = color(TextSecondary), fontSize = 18.sp)
    )
}

@Composable
private fun PrayerBoxCell(entry: PrayerEntry, active: Boolean, modifier: GlanceModifier) {
    Column(
        modifier = modifier
            .background(if (active) WidgetActive else WidgetCard)
            .cornerRadius(14.dp)
            .padding(vertical = 6.dp, horizontal = 3.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        Text(
            text = entry.label,
            style = TextStyle(
                color = color(if (active) Gold else TextSecondary),
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = entry.time.substringBefore(" "),
            style = TextStyle(
                color = color(if (active) GoldSoft else TextPrimary),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = entry.time.substringAfter(" ", ""),
            style = TextStyle(color = color(TextMuted), fontSize = 6.sp)
        )
    }
}

/** شبكة 3×2 لمربعات الصلوات الست. */
@Composable
private fun PrayerGridRow(entries: List<PrayerEntry>, activeLabel: String) {
    if (entries.isEmpty()) return
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        entries.forEachIndexed { index, entry ->
            if (index > 0) Spacer(modifier = GlanceModifier.width(6.dp))
            PrayerBoxCell(
                entry = entry,
                active = entry.label == activeLabel,
                modifier = GlanceModifier.defaultWeight()
            )
        }
        // موازنة الصف إذا كان أقل من 3 مربعات
        repeat((3 - entries.size).coerceAtLeast(0)) {
            Spacer(modifier = GlanceModifier.defaultWeight())
        }
    }
}

// ============================================================================
//  1) ويدجت «اليوم» — التصميم الكامل
// ============================================================================
class TodayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayWidget()
}

class TodayWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = PrayerSnapshotLoader.load(context)
        provideContent {
            GlanceTheme {
                TodayWidgetContent(context, snapshot)
            }
        }
    }
}

@Composable
private fun TodayWidgetContent(context: Context, snapshot: PrayerSnapshot) {
    val gridEntries = snapshot.widgetEntries()
    WidgetFrame(context) {
        WidgetKashida(snapshot, fontSize = 26)
        Spacer(GlanceModifier.height(2.dp))
        Text(
            text = snapshot.gregorianText,
            style = TextStyle(color = color(TextMuted), fontSize = 8.sp, textAlign = TextAlign.Center)
        )

        Spacer(GlanceModifier.height(8.dp))

        // شريحة «باقي على …»
        Box(
            modifier = GlanceModifier
                .background(Color(0x14FFFFFF))
                .cornerRadius(50.dp)
                .padding(horizontal = 14.dp, vertical = 4.dp)
        ) {
            Text(
                text = "باقي على ${snapshot.nextLabel}",
                style = TextStyle(color = color(Gold), fontSize = 9.sp, fontWeight = FontWeight.Bold)
            )
        }

        Spacer(GlanceModifier.height(6.dp))

        // العدّاد
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            DigitColumn(two(snapshot.remainingHours), "ساعة")
            Colon()
            DigitColumn(two(snapshot.remainingMinutes), "دقيقة")
            Colon()
            DigitColumn(two(snapshot.remainingSecondsPart), "ثانية")
        }

        Spacer(GlanceModifier.height(6.dp))

        // شريط التقدم
        PillProgress(snapshot.periodProgress, snapshot.isDaytime)
        Row(
            modifier = GlanceModifier.fillMaxWidth().padding(top = 2.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = snapshot.periodStartLabel,
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(color = color(TextMuted), fontSize = 7.sp)
            )
            Text(
                text = snapshot.periodEndLabel,
                style = TextStyle(color = color(TextMuted), fontSize = 7.sp)
            )
        }

        Spacer(GlanceModifier.height(8.dp))

        // مربعات الصلوات
        PrayerGridRow(gridEntries.take(3), snapshot.nextLabel)
        Spacer(GlanceModifier.height(6.dp))
        PrayerGridRow(gridEntries.drop(3).take(3), snapshot.nextLabel)
    }
}

// ============================================================================
//  2) ويدجت «المواقيت» — مربعات الصلوات
// ============================================================================
class TimesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TimesWidget()
}

class TimesWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = PrayerSnapshotLoader.load(context)
        provideContent {
            GlanceTheme {
                TimesWidgetContent(context, snapshot)
            }
        }
    }
}

@Composable
private fun TimesWidgetContent(context: Context, snapshot: PrayerSnapshot) {
    val gridEntries = snapshot.widgetEntries()
    WidgetFrame(context) {
        WidgetKashida(snapshot, fontSize = 20)
        Spacer(GlanceModifier.height(2.dp))
        Text(
            text = "${snapshot.city} • ${snapshot.gregorianText}",
            style = TextStyle(color = color(TextMuted), fontSize = 8.sp, textAlign = TextAlign.Center)
        )
        Spacer(GlanceModifier.height(8.dp))
        PrayerGridRow(gridEntries.take(3), snapshot.nextLabel)
        Spacer(GlanceModifier.height(6.dp))
        PrayerGridRow(gridEntries.drop(3).take(3), snapshot.nextLabel)
        Spacer(GlanceModifier.height(6.dp))
        Text(
            text = "باقي على ${snapshot.nextLabel} • ${two(snapshot.remainingHours)}:${two(snapshot.remainingMinutes)}:${two(snapshot.remainingSecondsPart)}",
            style = TextStyle(color = color(Gold), fontSize = 8.sp, fontWeight = FontWeight.Bold)
        )
    }
}

// ============================================================================
//  3) ويدجت «العدّاد» — الوقت المتبقي بخط كبير
// ============================================================================
class CountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownWidget()
}

class CountdownWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = PrayerSnapshotLoader.load(context)
        provideContent {
            GlanceTheme {
                CountdownWidgetContent(context, snapshot)
            }
        }
    }
}

@Composable
private fun CountdownWidgetContent(context: Context, snapshot: PrayerSnapshot) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetBgSoft)
            .cornerRadius(28.dp)
            .padding(14.dp)
            .clickable(actionStartActivity(launchIntent(context))),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        WidgetKashida(snapshot, fontSize = 18)
        Spacer(GlanceModifier.height(6.dp))
        Text(
            text = "باقي على ${snapshot.nextLabel}",
            style = TextStyle(color = color(TextSecondary), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        )
        Spacer(GlanceModifier.height(2.dp))
        Text(
            text = "${two(snapshot.remainingHours)}:${two(snapshot.remainingMinutes)}:${two(snapshot.remainingSecondsPart)}",
            style = TextStyle(color = color(Gold), fontSize = 28.sp, fontWeight = FontWeight.Bold)
        )
        Text(
            text = "موعد ${snapshot.nextTime}",
            style = TextStyle(color = color(TextPrimary), fontSize = 9.sp)
        )
        Spacer(GlanceModifier.height(6.dp))
        PillProgress(snapshot.periodProgress, snapshot.isDaytime)
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = snapshot.city,
            style = TextStyle(color = color(TextMuted), fontSize = 7.sp)
        )
    }
}

// ============================================================================
//  4) ويدجت «الهجري»
// ============================================================================
class HijriWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HijriWidget()
}

class HijriWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = PrayerSnapshotLoader.load(context)
        provideContent {
            GlanceTheme {
                HijriWidgetContent(context, snapshot)
            }
        }
    }
}

@Composable
private fun HijriWidgetContent(context: Context, snapshot: PrayerSnapshot) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetBgSoft)
            .cornerRadius(28.dp)
            .padding(14.dp)
            .clickable(actionStartActivity(launchIntent(context))),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
    ) {
        WidgetKashida(snapshot, fontSize = 20)
        Spacer(GlanceModifier.height(2.dp))
        Text(
            text = "${snapshot.hijriMonth} ${snapshot.hijriYear} هـ",
            style = TextStyle(color = color(TextPrimary), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        )
        Spacer(GlanceModifier.height(2.dp))
        Text(
            text = snapshot.gregorianText,
            style = TextStyle(color = color(TextMuted), fontSize = 8.sp, textAlign = TextAlign.Center)
        )
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = "${snapshot.city} • ${snapshot.dayProgressPercent()}٪ من النهار",
            style = TextStyle(color = color(TextSecondary), fontSize = 8.sp)
        )
    }
}

private fun PrayerSnapshot.dayProgressPercent(): Int = (dayProgress * 100).toInt().coerceIn(0, 100)

// ============================================================================
//  محدّث كل الويدجت
// ============================================================================
object WidgetUpdater {

    private val widgets = listOf(
        TodayWidget(),
        TimesWidget(),
        CountdownWidget(),
        HijriWidget()
    )

    fun updateAll(context: Context) {
        val appContext = context.applicationContext
        // تهيئة سياق الويدجت (تحميل الإعدادات يحتاجه)
        SettingsLoader.load(appContext)
        runCatching {
            kotlinx.coroutines.runBlocking {
                widgets.forEach { runCatching { it.updateAll(appContext) } }
            }
        }
    }
}
