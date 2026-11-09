package com.karrar.amsakiah.ui.platform

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * أندرويد: نضبط أيقونات شريط الحالة وشريط التنقل حسب لون الخلفية الفعلي
 * (لا وضع النظام فقط)، حتى لا تظهر أيقونات بيضاء على خلفية فاتحة.
 */
@Composable
actual fun ApplySystemBarAppearance(lightBackground: Boolean) {
    val view = LocalView.current
    SideEffect {
        val window = view.context.findActivity()?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = lightBackground
            isAppearanceLightNavigationBars = lightBackground
        }
    }
}

private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
