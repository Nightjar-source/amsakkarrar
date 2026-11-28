package com.karrar.amsakiah.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties

/**
 * خصائص الحوار المتعددة المنصات (تُفعّل `decorFitsSystemWindows = false` على أندرويد فقط).
 */
expect fun platformDialogProperties(
    dismissOnBackPress: Boolean,
    dismissOnClickOutside: Boolean
): DialogProperties

/**
 * تمرير متداخل مع لوحة المفاتيح على أندرويد (imeNestedScroll)، ولا يؤثر على بقية المنصات.
 */
expect fun Modifier.platformImeNestedScroll(): Modifier

/**
 * قفل متوافق مع كل المنصات (بما فيها WasmJS) لحماية الخرائط المؤقتة في الذاكرة.
 */
expect inline fun <R> platformSync(lock: Any, block: () -> R): R
