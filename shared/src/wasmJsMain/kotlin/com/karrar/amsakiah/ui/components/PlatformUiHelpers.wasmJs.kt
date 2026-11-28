package com.karrar.amsakiah.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties

actual fun platformDialogProperties(
    dismissOnBackPress: Boolean,
    dismissOnClickOutside: Boolean
): DialogProperties = DialogProperties(
    usePlatformDefaultWidth = false,
    dismissOnBackPress = dismissOnBackPress,
    dismissOnClickOutside = dismissOnClickOutside
)

actual fun Modifier.platformImeNestedScroll(): Modifier = this

actual inline fun <R> platformSync(lock: Any, block: () -> R): R = block()
