package com.karrar.amsakiah.ui.platform

import androidx.compose.runtime.Composable

/**
 * معالج زر/إيماءة الرجوع النظامية.
 *
 * على أندرويد يستخدم PredictiveBackHandler لدعم الإيماءات التنبؤية (Android 13+)
 * مع رجوع تقليدي على الإصدارات الأقدم. على iOS لا يفعل شيئًا.
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean = true, onBack: () -> Unit)
