package com.karrar.amsakiah.ui.platform

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.flow.Flow

/**
 * دعم الإيماءات التنبؤية: يُستدعى [onBack] بعد اكتمال إيماءة الرجوع.
 * نستهلك تدفق التقدم (progress) لدعم التنبؤ دون قطع الإيماءة.
 */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    val currentOnBack by rememberUpdatedState(onBack)
    PredictiveBackHandler(enabled = enabled) { progress: Flow<androidx.activity.BackEventCompat> ->
        progress.collect { }
        currentOnBack()
    }
}
