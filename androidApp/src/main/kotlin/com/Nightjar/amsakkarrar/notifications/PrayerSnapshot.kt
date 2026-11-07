package com.Nightjar.amsakkarrar.notifications

import android.content.Context
import com.karrar.amsakiah.core.AndroidAppContext
import com.karrar.amsakiah.core.settings.AppSettings
import com.karrar.amsakiah.core.settings.SettingsStorage
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.core.state.PrayerMeta
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

private val json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }

object SettingsLoader {

    fun load(context: Context): AppSettings {
        AndroidAppContext.init(context)
        return runCatching {
            SettingsStorage.read()?.let { json.decodeFromString<AppSettings>(it) }
        }.getOrNull() ?: AppSettings()
    }
}

data class PrayerEntry(val id: String, val label: String, val time: String, val isNext: Boolean)

data class PrayerSnapshot(
    val settings: AppSettings,
    val city: String,
    val hijriDay: Int,
    val hijriMonth: String,
    val hijriYear: Int,
    val hijriText: String,
    val gregorianText: String,
    val clock: String,
    val entries: List<PrayerEntry>,
    val nextLabel: String,
    val nextTime: String,
    val remainingSeconds: Int,
    val remainingHours: Int,
    val remainingMinutes: Int,
    val remainingSecondsPart: Int,
    val dayProgress: Float,
    val periodProgress: Float,
    val isDaytime: Boolean,
    /** بداية ونهاية الفترة الحالية (لشريط التقدم) */
    val periodStartLabel: String,
    val periodEndLabel: String,
    val fajrText: String,
    val maghribText: String,
    /** الصلاة التي تأتي بعد الصلاة القادمة (تستخدم في إشعار الأذان) */
    val afterLabel: String,
    val afterTime: String
)

object PrayerSnapshotLoader {

    fun load(context: Context): PrayerSnapshot {
        val settings = SettingsLoader.load(context)
        val zone = PrayerCalculator.zoneOf(settings)
        val now = Clock.System.now().toLocalDateTime(zone)
        val today = now.date
        val hijri = PrayerCalculator.hijriFor(settings, today)
        val times = PrayerCalculator.adjustedTimes(PrayerCalculator.timesFor(settings, today), settings)
        val tomorrow = today.plus(DatePeriod(days = 1))
        val tomorrowTimes = PrayerCalculator.adjustedTimes(PrayerCalculator.timesFor(settings, tomorrow), settings)

        val nowSeconds = PrayerCalculator.secondsOfDay(now)
        val ids = PrayerCalculator.visibleIds(settings, today)
        // العدّاد والإشعار الحي يدوران حول الأوقات التي اختارها المستخدم فقط
        val liveIds = PrayerCalculator.liveIds(settings, today)
        val prayerIds = liveIds.ifEmpty {
            PrayerCalculator.visibleIds(settings, today)
        }.ifEmpty {
            listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
        }

        val nextIndex = prayerIds.indexOfFirst { ((times[it] ?: 0.0) * 3600).roundToInt() > nowSeconds }
            .let { if (it < 0) 0 else it }
        val nextId = prayerIds.getOrElse(nextIndex) { "fajr" }
        val afterId = prayerIds.getOrElse((nextIndex + 1) % prayerIds.size.coerceAtLeast(1)) { nextId }

        val nextRaw = times[nextId] ?: 0.0
        val nextSeconds = (nextRaw * 3600).roundToInt()
        val remaining = if (nextSeconds >= nowSeconds) {
            nextSeconds - nowSeconds
        } else {
            (86400 - nowSeconds) + ((tomorrowTimes[nextId] ?: nextRaw) * 3600).roundToInt()
        }

        // طول الفترة الحالية (من الصلاة السابقة إلى القادمة) لحساب شريط التقدم
        val previousId = prayerIds.getOrElse((nextIndex - 1 + prayerIds.size) % prayerIds.size.coerceAtLeast(1)) { nextId }
        val previousRaw = times[previousId] ?: 0.0
        val periodSeconds = if (nextSeconds > nowSeconds) {
            (nextSeconds - (previousRaw * 3600).roundToInt()).coerceAtLeast(1)
        } else {
            ((86400 - (previousRaw * 3600).roundToInt()) + nextSeconds).coerceAtLeast(1)
        }
        val elapsed = (periodSeconds - remaining).coerceIn(0, periodSeconds)

        val fajr = times["fajr"] ?: 5.0
        val maghrib = times["maghrib"] ?: 18.0
        val dayLength = (maghrib - fajr).coerceAtLeast(0.01)
        val progress = (((nowSeconds / 3600.0) - fajr) / dayLength).toFloat().coerceIn(0f, 1f)

        val entries = ids.map { id ->
            val raw = times[id] ?: 0.0
            val (time, amPm, _) = PrayerCalculator.formatHour(raw)
            PrayerEntry(id, PrayerMeta.label(id), "$time $amPm", id == nextId)
        }

        val (afterH, afterM) = format12(times[afterId] ?: 0.0)
        val (nextH, nextAmPm) = format12(nextRaw)

        return PrayerSnapshot(
            settings = settings,
            city = settings.cityName,
            hijriDay = hijri.day,
            hijriMonth = hijri.monthName,
            hijriYear = hijri.year,
            hijriText = PrayerCalculator.hijriText(hijri),
            gregorianText = PrayerCalculator.formattedGregorian(today),
            clock = "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}",
            entries = entries,
            nextLabel = PrayerMeta.label(nextId),
            nextTime = "$nextH $nextAmPm",
            remainingSeconds = remaining,
            remainingHours = remaining / 3600,
            remainingMinutes = (remaining % 3600) / 60,
            remainingSecondsPart = remaining % 60,
            dayProgress = progress,
            periodProgress = (elapsed.toFloat() / periodSeconds.toFloat()).coerceIn(0f, 1f),
            isDaytime = nowSeconds >= (fajr * 3600).roundToInt() &&
                nowSeconds < (maghrib * 3600).roundToInt(),
            periodStartLabel = PrayerMeta.label(previousId),
            periodEndLabel = PrayerMeta.label(nextId),
            fajrText = PrayerCalculator.formatHour(fajr).first,
            maghribText = PrayerCalculator.formatHour(maghrib).first,
            afterLabel = PrayerMeta.label(afterId),
            afterTime = "$afterH $afterM"
        )
    }

    fun todayDate(settings: AppSettings): LocalDate = PrayerCalculator.todayDate(settings)

    /** يعيد (الوقت بصيغة 12 ساعة، اللاحقة ص/م) */
    fun format12(rawHour: Double): Pair<String, String> {
        val (time, amPm, _) = PrayerCalculator.formatHour(rawHour)
        return time to amPm
    }
}
