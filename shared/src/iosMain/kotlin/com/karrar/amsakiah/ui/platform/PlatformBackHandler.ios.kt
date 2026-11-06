package com.karrar.amsakiah.ui.platform

import androidx.compose.runtime.Composable

/** iOS: لا يوجد زر رجوع نظامي — الشاشة تُغلق بزر الرجوع داخل الواجهة. */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // لا شيء
}
