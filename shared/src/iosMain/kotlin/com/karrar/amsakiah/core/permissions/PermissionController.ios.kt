package com.karrar.amsakiah.core.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.NSObject

/**
 * صلاحيات iOS — نوافذ النظام الأصلية:
 * - الموقع: CLLocationManager.requestWhenInUseAuthorization
 * - الإشعارات: UNUserNotificationCenter.requestAuthorization
 */
@OptIn(ExperimentalForeignApi::class)
private class IosPermissionController : PermissionController {

    private var locationGranted by mutableStateOf(false)
    private var notificationsGranted by mutableStateOf(false)

    override val status: PermissionStatus
        get() = PermissionStatus(
            notifications = notificationsGranted,
            location = locationGranted,
            exactAlarm = true,
            // iOS لا يوفّر استثناء بطارية بنفس مفهوم أندرويد
            batteryOptimized = true
        )

    private val locationManager = CLLocationManager()

    private val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            refreshLocation()
        }
    }

    init {
        locationManager.delegate = delegate
        refresh()
    }

    override fun refresh() {
        refreshLocation()
        UNUserNotificationCenter.currentNotificationCenter()
            .getNotificationSettingsWithCompletionHandler { settings ->
                notificationsGranted = settings?.authorizationStatus == UNAuthorizationStatusAuthorized
            }
    }

    private fun refreshLocation() {
        val s = CLLocationManager.authorizationStatus()
        locationGranted = s == kCLAuthorizationStatusAuthorizedWhenInUse ||
            s == kCLAuthorizationStatusAuthorizedAlways
    }

    override fun requestNotifications() {
        UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { granted, _ ->
            notificationsGranted = granted
        }
    }

    override fun requestLocation() {
        if (CLLocationManager.authorizationStatus() == kCLAuthorizationStatusNotDetermined) {
            locationManager.requestWhenInUseAuthorization()
        } else {
            // ممنوح أو مرفوض مسبقًا — نحدّث الحالة ليظهر زر الإعدادات عند الرفض
            refreshLocation()
        }
    }

    override fun requestExactAlarm() = Unit
    override fun requestBatteryExemption() = Unit

    override fun openAppSettings() {
        // يفتح إعدادات التطبيق؛ يمكن استدعاؤه من Swift عبر الجسر عند الحاجة
    }
}

@Composable
actual fun rememberPermissionController(): PermissionController = remember { IosPermissionController() }
