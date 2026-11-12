package com.karrar.amsakiah.ui.platform

import android.content.ContentResolver
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * أندرويد: «إزالة الحركات» يضبط `animator_duration_scale` على صفر.
 * نراقب القيمة بـ [ContentObserver] حتى يتغيّر السلوك بلا إعادة تشغيل.
 */
@Composable
actual fun rememberReduceMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    var reduced by remember(resolver) { mutableStateOf(isAnimationOff(resolver)) }

    DisposableEffect(resolver) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                reduced = isAnimationOff(resolver)
            }
        }
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer
        )
        // القيمة قد تتغيّر بين `remember` والاشتراك في المراقب.
        reduced = isAnimationOff(resolver)
        onDispose { resolver.unregisterContentObserver(observer) }
    }
    return reduced
}

private fun isAnimationOff(resolver: ContentResolver): Boolean =
    Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
