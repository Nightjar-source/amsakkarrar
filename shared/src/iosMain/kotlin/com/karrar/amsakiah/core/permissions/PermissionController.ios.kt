package com.karrar.amsakiah.core.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private class IosPermissionController : PermissionController {
    override val status: PermissionStatus = PermissionStatus()
    override fun refresh() = Unit
    override fun requestNotifications() = Unit
    override fun requestLocation() = Unit
    override fun requestExactAlarm() = Unit
    override fun requestBatteryExemption() = Unit
    override fun openAppSettings() = Unit
}

@Composable
actual fun rememberPermissionController(): PermissionController = remember { IosPermissionController() }
