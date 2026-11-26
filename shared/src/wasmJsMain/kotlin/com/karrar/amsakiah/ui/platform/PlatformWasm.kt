package com.karrar.amsakiah.ui.platform

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit

actual val isIosUi: Boolean = false

@Composable
actual fun rememberReduceMotion(): Boolean = false

@Composable
actual fun ApplySystemBarAppearance(lightBackground: Boolean) = Unit
