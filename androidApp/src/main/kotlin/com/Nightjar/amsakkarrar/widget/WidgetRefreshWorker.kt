package com.Nightjar.amsakkarrar.widget

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.Nightjar.amsakkarrar.notifications.LiveNotificationManager
import java.util.concurrent.TimeUnit

/**
 * عامل WorkManager أصلي لتحديث الودجت والإشعار الحي في الخلفية.
 *
 * لماذا WorkManager؟
 * - أندرويد يمنع تحديث الودجت أكثر من مرة كل 15 دقيقة عبر updatePeriodMillis،
 *   لذلك لا يمكن الاعتماد عليه للعدّاد التنازلي.
 * - العدّاد نفسه (الثواني) يعمل داخل الودجت عبر Chronometer أصلي مرتبط
 *   بـ SystemClock.elapsedRealtime وجهاز المستخدم، فلا يتوقف بين التحديثات.
 * - WorkManager يضمن تحديث النصوص (الصلاة القادمة، الهجري، شريط التقدم)
 *   بشكل دوري، ويكمل بعد إعادة التشغيل لأن مهامه محفوظة على القرص.
 */
class WidgetRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        runCatching { WidgetUpdater.updateAllSuspending(context) }
        runCatching { LiveNotificationManager.update(context) }
        return Result.success()
    }

    companion object {
        private const val UNIQUE_PERIODIC = "karar_widget_refresh_periodic"
        private const val UNIQUE_NEXT_BOUNDARY = "karar_widget_refresh_boundary"

        /** جدولة دورية كل 15 دقيقة (أقل مدة يسمح بها أندرويد) */
        fun ensurePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .build()
            val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            runCatching {
                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    UNIQUE_PERIODIC,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    request
                )
            }
        }

        /** تحديث فوري عند دخول وقت الصلاة القادمة (يُجدول من WidgetUpdater) */
        fun scheduleBoundary(context: Context, delayMillis: Long) {
            val request = OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
                .setInitialDelay(delayMillis.coerceAtLeast(1_000L), TimeUnit.MILLISECONDS)
                .build()
            runCatching {
                WorkManager.getInstance(context).enqueueUniqueWork(
                    UNIQUE_NEXT_BOUNDARY,
                    ExistingWorkPolicy.REPLACE,
                    request
                )
            }
        }
    }
}
