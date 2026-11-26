package com.karrar.amsakiah.ui.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberPermissionController(): PermissionController = remember {
    object : PermissionController {
        override val status = PermissionStatus(notifications = true, location = false, exactAlarm = true, batteryOptimized = true)
        override fun refresh() = Unit
        override fun requestNotifications() = Unit
        override fun requestLocation() = Unit
        override fun requestExactAlarm() = Unit
        override fun requestBatteryExemption() = Unit
        override fun openAppSettings() = Unit
    }
}
