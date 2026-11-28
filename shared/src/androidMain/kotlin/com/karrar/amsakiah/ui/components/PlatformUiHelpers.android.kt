package com.karrar.amsakiah.ui.components

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties

actual fun platformDialogProperties(
    dismissOnBackPress: Boolean,
    dismissOnClickOutside: Boolean
): DialogProperties = DialogProperties(
    decorFitsSystemWindows = false,
    usePlatformDefaultWidth = false,
    dismissOnBackPress = dismissOnBackPress,
    dismissOnClickOutside = dismissOnClickOutside
)

@OptIn(ExperimentalLayoutApi::class)
actual fun Modifier.platformImeNestedScroll(): Modifier = this.imeNestedScroll()

actual inline fun <R> platformSync(lock: Any, block: () -> R): R = synchronized(lock, block)
