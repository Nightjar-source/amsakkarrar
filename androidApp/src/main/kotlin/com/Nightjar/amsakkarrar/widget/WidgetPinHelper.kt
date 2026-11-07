package com.Nightjar.amsakkarrar.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.karrar.amsakiah.core.widgets.WidgetPinBridge

/**
 * يسجّل جسر الودجت بين الواجهة المشتركة وأندرويد:
 * - طلب تثبيت ودجت معيّن (Android 8+).
 * - فتح شاشة إعدادات الخلفية (Material / Blur / شفاف / ليلي).
 * - قراءة/كتابة نمط الخلفية.
 */
object WidgetPinHelper {

    private fun providerFor(key: String): Class<*>? = when (key) {
        WidgetPinBridge.TODAY -> TodayWidgetReceiver::class.java
        WidgetPinBridge.TIMES -> TimesWidgetReceiver::class.java
        WidgetPinBridge.COUNTDOWN -> CountdownWidgetReceiver::class.java
        WidgetPinBridge.HIJRI -> HijriWidgetReceiver::class.java
        WidgetPinBridge.TASBIH -> TasbihWidgetReceiver::class.java
        WidgetPinBridge.CLOCK -> ClockWidgetReceiver::class.java
        WidgetPinBridge.JOURNEY -> JourneyWidgetReceiver::class.java
        else -> null
    }

    fun install(context: Context) {
        val ctx = context.applicationContext
        val manager = AppWidgetManager.getInstance(ctx)
        val supported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            manager.isRequestPinAppWidgetSupported

        WidgetPinBridge.install(
            handler = { key ->
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    false
                } else {
                    val provider = providerFor(key)
                    if (provider == null || !manager.isRequestPinAppWidgetSupported) {
                        false
                    } else {
                        runCatching {
                            manager.requestPinAppWidget(ComponentName(ctx, provider), null, null)
                        }.isSuccess
                    }
                }
            },
            isSupported = supported
        )

        WidgetPinBridge.installSettings(
            open = {
                runCatching {
                    ctx.startActivity(
                        Intent(ctx, WidgetConfigActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                }.isSuccess
            },
            getStyle = { WidgetStyleStore.current(ctx) },
            setStyle = { style ->
                runCatching {
                    WidgetStyleStore.set(ctx, style)
                    WidgetUpdater.updateAll(ctx)
                }.isSuccess
            }
        )
    }
}
