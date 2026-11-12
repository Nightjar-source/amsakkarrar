package com.karrar.amsakiah.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIAccessibilityIsReduceMotionEnabled

/**
 * iOS: نقرأ «تقليل الحركة» من إعدادات إمكانية الوصول مرة واحدة عند التركيب
 * (لا واجهة مراقبة بسيطة في UIKit لـ Compose، والتغيير يُطبَّق عند الفتح التالي).
 */
@Composable
actual fun rememberReduceMotion(): Boolean =
    remember { UIAccessibilityIsReduceMotionEnabled() }
