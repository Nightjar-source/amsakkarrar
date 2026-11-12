package com.karrar.amsakiah.ui.platform

import androidx.compose.runtime.Composable

/**
 * هل طلب المستخدم «تقليل الحركة» من إعدادات النظام؟
 *
 * - أندرويد: `ANIMATOR_DURATION_SCALE == 0` (خيار «إزالة الحركات» في
 *   إعدادات إمكانية الوصول / خيارات المطور) — ويُراقَب تغيّره لحظيًا.
 * - iOS: `UIAccessibilityIsReduceMotionEnabled` («تقليل الحركة»).
 *
 * القاعدة المطبَّقة: الحركات التزيينية والانتقالات المكانية الكبيرة تُلغى
 * (تظهر فورًا)، أما التلاشي/تغيّر اللون — وهو غير مثير للدوار — فيبقى.
 */
@Composable
expect fun rememberReduceMotion(): Boolean
