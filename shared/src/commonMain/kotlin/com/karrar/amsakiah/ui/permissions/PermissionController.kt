package com.karrar.amsakiah.ui.permissions

import androidx.compose.runtime.Composable

data class PermissionStatus(
    val notifications: Boolean = false,
    val location: Boolean = false,
    val exactAlarm: Boolean = false,
    val batteryOptimized: Boolean = false,
    val summary: String = ""
)

interface PermissionController {
    val status: PermissionStatus
    fun refresh()
    fun requestNotifications()
    fun requestLocation()
    fun requestExactAlarm()
    fun requestBatteryExemption()
    fun openAppSettings()
}

@Composable
expect fun rememberPermissionController(): PermissionController
