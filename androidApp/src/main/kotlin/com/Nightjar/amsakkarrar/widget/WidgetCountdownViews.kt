package com.Nightjar.amsakkarrar.widget

import android.content.Context
import android.os.SystemClock
import android.widget.RemoteViews
import com.Nightjar.amsakkarrar.R
import com.Nightjar.amsakkarrar.notifications.PrayerSnapshot
import com.karrar.amsakiah.core.state.PrayerCalculator
import kotlinx.datetime.Clock
import kotlinx.datetime.toLocalDateTime

/** خلية صلاة داخل هيدر الودجت — وقت منسّق حسب إعدادات المستخدم (12/24 ساعة). */
private data class PrayerCell(
    val label: String,
    val time: String,
    val period: String,
    val active: Boolean
)

/** الستة الأساسية لشبكة الـHero: الإمساك (حسب المذهب/الوضع) + الفجر..العشاء بالترتيب. */
private val HERO_GRID_CORE = listOf("imsak", "fajr", "dhuhr", "asr", "maghrib", "isha")

private val PRAYER_CELL_IDS = listOf(
    R.id.widget_prayer_1, R.id.widget_prayer_2, R.id.widget_prayer_3,
    R.id.widget_prayer_4, R.id.widget_prayer_5, R.id.widget_prayer_6
)
private val PRAYER_LABEL_IDS = listOf(
    R.id.widget_prayer_1_label, R.id.widget_prayer_2_label, R.id.widget_prayer_3_label,
    R.id.widget_prayer_4_label, R.id.widget_prayer_5_label, R.id.widget_prayer_6_label
)
private val PRAYER_TIME_IDS = listOf(
    R.id.widget_prayer_1_time, R.id.widget_prayer_2_time, R.id.widget_prayer_3_time,
    R.id.widget_prayer_4_time, R.id.widget_prayer_5_time, R.id.widget_prayer_6_time
)
private val PRAYER_PERIOD_IDS = listOf(
    R.id.widget_prayer_1_period, R.id.widget_prayer_2_period, R.id.widget_prayer_3_period,
    R.id.widget_prayer_4_period, R.id.widget_prayer_5_period, R.id.widget_prayer_6_period
)

/**
 * يعيد خلايا مربعات الصلوات الست للـHero — نفس ترتيب «الظاهرة» في اللقطة.
 *
 * الاختيار: الستة الأساسية أولًا (الإمساك إن كان ظاهرًا + الفجر..العشاء)؛
 * إن نقصت خانة (مثلًا الإمساك مخفي خارج رمضان) تُملأ من بقية الظاهرة
 * (الشروق/الغروب/منتصف الليل/الإسفار) بنفس ترتيب visibleIds — بحد أقصى 6 خانات.
 *
 * التنسيق: في وضع 12 ساعة تؤخذ الأوقات من اللقطة مباشرة (منسّقة صباحًا/مساءً)؛
 * وفي وضع 24 ساعة تُعاد من المحرك الفلكي لأن اللقطة تحتفظ بنسخة 12 ساعة فقط.
 */
private fun prayerCells(snapshot: PrayerSnapshot): List<PrayerCell> {
    val entries = snapshot.entries
    if (entries.isEmpty()) return emptyList()

    val coreIndices = entries.indices.filter { entries[it].id in HERO_GRID_CORE }
    val extraIndices = entries.indices.filter { entries[it].id !in HERO_GRID_CORE }
    val picked = (coreIndices + extraIndices).take(6).sorted()

    val use24 = snapshot.settings.use24Hour
    // حساب خام مرة واحدة فقط عند وضع 24 ساعة — نفس مصدر اللقطة نفسها.
    val rawTimes: Map<String, Double>? = if (use24) {
        runCatching {
            val settings = snapshot.settings
            val zone = PrayerCalculator.zoneOf(settings)
            val today = Clock.System.now().toLocalDateTime(zone).date
            PrayerCalculator.adjustedTimes(PrayerCalculator.timesFor(settings, today), settings)
        }.getOrNull()
    } else {
        null
    }

    return picked.map { i ->
        val entry = entries[i]
        if (use24) {
            val time = rawTimes?.get(entry.id)
                ?.let { PrayerCalculator.formatHour(it, use24Hour = true).first }
                ?: entry.time.substringBefore(" ")
            PrayerCell(entry.label, time, "", entry.isNext)
        } else {
            PrayerCell(
                label = entry.label,
                time = entry.time.substringBefore(" "),
                period = entry.time.substringAfter(" ", "").trim(),
                active = entry.isNext
            )
        }
    }
}

/**
 * يملأ مربعات الصلوات الست داخل RemoteViews الهيدر — هوية Glance نفسها:
 * القادمة (isNext) = ذهبي شفاف #26FFB432 وليست سوداء، والباقية = زجاج أبيض
 * شفاف. الألوان مطابقة لـ PrayerBoxCell (Gold/GoldSoft/TextPrimary/TextSecondary/TextMuted).
 */
private fun applyPrayerCells(views: RemoteViews, snapshot: PrayerSnapshot, compact: Boolean) {
    val cells = prayerCells(snapshot)
    val normalCell = if (compact) R.drawable.widget_prayer_cell_compact else R.drawable.widget_prayer_cell
    val activeCell = if (compact) R.drawable.widget_prayer_cell_active_compact else R.drawable.widget_prayer_cell_active

    for (i in 0 until 6) {
        val cell = cells.getOrNull(i)
        if (cell == null) {
            views.setViewVisibility(PRAYER_CELL_IDS[i], android.view.View.GONE)
            continue
        }
        views.setViewVisibility(PRAYER_CELL_IDS[i], android.view.View.VISIBLE)
        views.setTextViewText(PRAYER_LABEL_IDS[i], cell.label)
        views.setTextViewText(PRAYER_TIME_IDS[i], cell.time)

        if (compact || cell.period.isEmpty()) {
            // الطراز المكثّف بلا سطر الفترة — نفس PrayerBoxCell compact
            views.setViewVisibility(PRAYER_PERIOD_IDS[i], android.view.View.GONE)
        } else {
            views.setViewVisibility(PRAYER_PERIOD_IDS[i], android.view.View.VISIBLE)
            views.setTextViewText(PRAYER_PERIOD_IDS[i], cell.period)
        }

        views.setInt(
            PRAYER_CELL_IDS[i],
            "setBackgroundResource",
            if (cell.active) activeCell else normalCell
        )
        views.setTextColor(PRAYER_LABEL_IDS[i], if (cell.active) 0xFFFFD700.toInt() else 0xFFB3B9C0D4.toInt())
        views.setTextColor(PRAYER_TIME_IDS[i], if (cell.active) 0xFFFAFAD2.toInt() else 0xFFF5F5F7.toInt())
        if (!compact) {
            views.setTextColor(PRAYER_PERIOD_IDS[i], 0xFF99B9C0D4.toInt())
        }
    }
}

/**
 * عدّاد ودجت أصلي (RemoteViews + Chronometer + ProgressBar).
 *
 * لماذا Chronometer؟
 * - أندرويد يقيّد تحديث الودجت إلى مرة كل 15 دقيقة، لذا لا يمكن تحديث الثواني
 *   عبر Glance/WorkManager كل ثانية.
 * - Chronometer داخليًا يعتمد على SystemClock.elapsedRealtime (ساعة الجهاز
 *   المونوتونية)، فيعدّ بنفسه كل ثانية بدون أي بث أو خدمة، ويستمر حتى لو
 *   تغيّر وقت الجهاز أو دخل في Doze.
 * - عند إعادة التشغيل أو تغيير الوقت/المنطقة يعيد BootReceiver بناء الودجت
 *   ويحسب القاعدة من جديد، فيبقى متزامنًا مع وقت الجهاز.
 */
fun countdownRemoteViews(
    context: Context,
    snapshot: PrayerSnapshot,
    compact: Boolean = false
): RemoteViews {
    val layout = if (compact) R.layout.widget_chrono_compact else R.layout.widget_chrono_large
    val views = RemoteViews(context.packageName, layout)

    views.setTextViewText(R.id.widget_chrono_label, "باقي على ${snapshot.nextLabel}")

    // تصحيح الانحراف: اللقطة قد تكون من كاش عمره ثوانٍ؛ نطرح الفارق حتى يطابق
    // عدّاد الودجت الإشعار الحي وساعة الجهاز بالثانية.
    val driftMs = if (snapshot.generatedAtElapsed > 0L) {
        (SystemClock.elapsedRealtime() - snapshot.generatedAtElapsed).coerceAtLeast(0L)
    } else {
        0L
    }
    val remainingMillis = (snapshot.remainingSeconds * 1000L - driftMs).coerceAtLeast(1_000L)
    views.setChronometer(
        R.id.widget_chrono,
        SystemClock.elapsedRealtime() + remainingMillis,
        null,
        true
    )
    @Suppress("NewApi")
    views.setChronometerCountDown(R.id.widget_chrono, true)

    if (!compact) {
        views.setTextViewText(R.id.widget_chrono_target, "موعد ${snapshot.nextTime} • ${snapshot.city}")
        // شريط تقدم مرسوم كصورة (تدرج المرجع + رأس مستدير) — نفس منطق النسبة
        // لدى الإشعار الحي، لكن بزوايا مضمونة على سامسونج/شاومي.
        val percent = (snapshot.periodProgress * 100f).coerceIn(0f, 100f)
        views.setImageViewBitmap(R.id.widget_chrono_progress, WidgetProgressStrip.render(percent, snapshot.isDaytime))
        // طرفا الشريط (widgetsnotifications.html: منتصف الليل ← الإمساك).
        views.setTextViewText(R.id.widget_progress_start, snapshot.periodStartLabel)
        views.setTextViewText(R.id.widget_progress_end, snapshot.periodEndLabel)
    }

    return views
}

/**
 * هيدر الودجت الرئيسي — RemoteViews لأن خط Reem Kufi لا يعمل في Glance.
 *
 * يعرض: كشيدة الشهر + التاريخ الميلادي + حبة «باقي على…» + Chronometer أصلي
 * بوحدات + شريط تقدم بيضوي سميك (صورة بتدرج المرجع) + طرفا الشريط
 * + شبكة مربعات الصلوات الست — كما في `widgetsnotifications.html`.
 */
fun todayHeroRemoteViews(
    context: Context,
    snapshot: PrayerSnapshot,
    compact: Boolean = false
): RemoteViews {
    val layout = if (compact) R.layout.widget_today_hero_compact else R.layout.widget_today_hero
    val views = RemoteViews(context.packageName, layout)
    val (pre, post) = com.karrar.amsakiah.ui.theme.SkyTheme.getKashidaMonthParts(snapshot.hijriMonth)
    views.setTextViewText(R.id.widget_kashida_pre, pre)
    views.setTextViewText(R.id.widget_kashida_day, snapshot.hijriDay.toString())
    views.setTextViewText(R.id.widget_kashida_post, post)
    views.setTextViewText(R.id.widget_hero_date, "${snapshot.gregorianText} • ${snapshot.city}، ${snapshot.settings.countryName}")
    if (snapshot.weatherText.isNotBlank()) {
        views.setTextViewText(R.id.widget_hero_weather, snapshot.weatherText)
        views.setViewVisibility(R.id.widget_hero_weather, android.view.View.VISIBLE)
    } else {
        views.setViewVisibility(R.id.widget_hero_weather, android.view.View.GONE)
    }
    views.setTextViewText(R.id.widget_hero_label, "باقي على ${snapshot.nextLabel}")

    val driftMs = if (snapshot.generatedAtElapsed > 0L) {
        (SystemClock.elapsedRealtime() - snapshot.generatedAtElapsed).coerceAtLeast(0L)
    } else {
        0L
    }
    val remainingMillis = (snapshot.remainingSeconds * 1000L - driftMs).coerceAtLeast(1_000L)
    views.setChronometer(
        R.id.widget_hero_chrono,
        SystemClock.elapsedRealtime() + remainingMillis,
        null,
        true
    )
    @Suppress("NewApi")
    views.setChronometerCountDown(R.id.widget_hero_chrono, true)

    val percent = (snapshot.periodProgress * 100f).coerceIn(0f, 100f)
    views.setImageViewBitmap(R.id.widget_hero_progress, WidgetProgressStrip.render(percent, snapshot.isDaytime))
    views.setTextViewText(R.id.widget_hero_progress_start, snapshot.periodStartLabel)
    views.setTextViewText(R.id.widget_hero_progress_end, snapshot.periodEndLabel)
    // شبكة الصلوات الست داخل الـHero نفسه — نفس هوية Glance PrayerBoxCell
    // (القادمة ذهبية شفافة، والباقية زجاج أبيض شفاف) مع احترام 12/24 ساعة.
    applyPrayerCells(views, snapshot, compact)
    return views
}

/**
 * ساعة الودجت — TextClock أصلي يتحدّث كل ثانية/دقيقة من نظام الوقت نفسه
 * دون أي بث أو خدمة أو تحديث من التطبيق.
 */
fun clockRemoteViews(
    context: Context,
    snapshot: PrayerSnapshot,
    compact: Boolean = false
): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.widget_clock)
    val parts = snapshot.gregorianText.split(" ")
    val dayName = parts.firstOrNull().orEmpty()
    views.setTextViewText(R.id.widget_clock_day, dayName)
    views.setTextViewText(R.id.widget_clock_date, snapshot.gregorianText.removePrefix("$dayName "))
    views.setTextViewText(R.id.widget_clock_city, snapshot.city)
    views.setViewVisibility(
        R.id.widget_clock_day,
        if (compact) android.view.View.GONE else android.view.View.VISIBLE
    )
    views.setViewVisibility(
        R.id.widget_clock_city,
        if (compact) android.view.View.GONE else android.view.View.VISIBLE
    )
    return views
}
