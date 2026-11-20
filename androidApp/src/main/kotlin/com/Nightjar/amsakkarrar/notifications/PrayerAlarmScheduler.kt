package com.Nightjar.amsakkarrar.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.core.state.PrayerMeta
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlin.math.roundToInt

/**
 * جدولة الأذان فقط (اختياري من الضبط) بالمنبهات الدقيقة لسبعة أيام قادمة،
 * وإعادة الجدولة عند الإقلاع أو تغيير الوقت/المنطقة الزمنية.
 *
 * لا يوجد إشعار تذكير منفصل: إشعارات التطبيق اثنان فقط — العدّاد الحي
 * وبطاقة الأذان — وكلاهما اختياري.
 */
class PrayerAlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun rescheduleAll() {
        val settings = SettingsLoader.load(context)
        val zone = PrayerCalculator.zoneOf(settings)
        val today = PrayerCalculator.todayDate(settings)

        // إلغاء كل ما سبق جدولته (بما فيه منبهات التذكير الملغاة من الإصدارات القديمة)
        for (day in 0..DAYS_AHEAD) {
            for (index in PrayerMeta.order.indices) {
                cancelAdhan(day, index)
                cancelLegacyReminder(day, index)
            }
        }

        val adhanIds = settings.adhanPrayers.toSet()

        for (day in 0..DAYS_AHEAD) {
            val date = today.plus(DatePeriod(days = day))
            val times = PrayerCalculator.adjustedTimes(PrayerCalculator.timesFor(settings, date), settings)

            PrayerMeta.order.forEachIndexed { index, id ->
                val raw = times[id] ?: return@forEachIndexed
                val hour = raw.toInt()
                val minute = ((raw - hour) * 60).roundToInt().coerceIn(0, 59)
                val dateTime = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, hour, minute)
                val triggerAt = dateTime.toInstant(zone).toEpochMilliseconds()
                val now = System.currentTimeMillis()
                if (triggerAt <= now + 2000) return@forEachIndexed

                if (settings.adhanEnabled && id in adhanIds) {
                    scheduleAdhan(day, index, id, triggerAt)
                }
            }
        }
    }

    private fun scheduleAdhan(day: Int, index: Int, prayerId: String, triggerAt: Long) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = PrayerAlarmReceiver.ACTION_ADHAN
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER, PrayerMeta.label(prayerId))
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_ID, prayerId)
            putExtra(PrayerAlarmReceiver.EXTRA_SOUND, "adhan")
        }
        val pending = PendingIntent.getBroadcast(
            context,
            adhanRequestCode(day, index),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExact(pending, triggerAt)
    }

    private fun setExact(pending: PendingIntent, triggerAt: Long) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            }
        }
    }

    private fun cancelAdhan(day: Int, index: Int) =
        cancel(PrayerAlarmReceiver.ACTION_ADHAN, adhanRequestCode(day, index))

    /**
     * إلغاء منبهات التذكير التي جدولتها الإصدارات السابقة (requestCode 5000+).
     * ضروري عند التحديث حتى لا يبقى إشعار ثالث قديم يعمل في الخلفية.
     */
    private fun cancelLegacyReminder(day: Int, index: Int) =
        cancel(LEGACY_REMINDER_ACTION, reminderRequestCode(day, index))

    /**
     * يجب أن يطابق الـIntent المُجدول في **العمل** (action) وليس فقط
     * requestCode؛ فـPendingIntent يقارن action ضمن هويته. غياب action هنا
     * كان يجعل الإلغاء يفشل، فتبقى منبهات الصلوات الملغاة تعمل في الخلفية.
     */
    private fun cancel(action: String, requestCode: Int) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            this.action = action
        }
        val pending = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pending != null) {
            alarmManager.cancel(pending)
            pending.cancel()
        }
    }

    companion object {
        const val DAYS_AHEAD = 6
        private const val LEGACY_REMINDER_ACTION = "com.Nightjar.amsakkarrar.REMINDER"
        private fun adhanRequestCode(day: Int, index: Int) = day * 100 + index
        private fun reminderRequestCode(day: Int, index: Int) = 5000 + day * 100 + index
    }
}
