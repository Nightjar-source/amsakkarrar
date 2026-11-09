package com.karrar.amsakiah.ui.platform

import androidx.compose.runtime.Composable

/**
 * يضبط مظهر أشرطة النظام ليطابق خلفية التطبيق:
 * أيقونات داكنة على خلفية فاتحة، وفاتحة على خلفية داكنة.
 *
 * @param lightBackground `true` عندما تكون الخلفية فاتحة (الوضع النهاري).
 */
@Composable
expect fun ApplySystemBarAppearance(lightBackground: Boolean)
