package com.karrar.amsakiah.core.permissions

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

private class AndroidPermissionController(private val context: Context) : PermissionController {

    override var status by mutableStateOf(readStatus())
        private set

    private fun readStatus(): PermissionStatus {
        val notifications = if (Build.VERSION.SDK_INT >= 33) {
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).areNotificationsEnabled()
        }
        val location = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val exact = if (Build.VERSION.SDK_INT >= 31) {
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
        } else true
        val battery = (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .isIgnoringBatteryOptimizations(context.packageName)
        return PermissionStatus(notifications, location, exact, battery)
    }

    override fun refresh() {
        status = readStatus()
    }

    override fun requestNotifications() {
        if (Build.VERSION.SDK_INT >= 33) {
            notificationLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
                ?: run {
                    openAppSettings()
                }
        } else {
            openAppSettings()
        }
    }

    override fun requestLocation() {
        locationLauncher?.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) ?: openAppSettings()
    }

    override fun requestExactAlarm() {
        if (Build.VERSION.SDK_INT >= 31) {
            runCatching {
                context.startActivity(
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }

    override fun requestBatteryExemption() {
        runCatching {
            context.startActivity(
                Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:${context.packageName}")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    override fun openAppSettings() {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    var notificationLauncher: androidx.activity.result.ActivityResultLauncher<String>? = null
    var locationLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>? = null
}

@Composable
actual fun rememberPermissionController(): PermissionController {
    val context = LocalContext.current
    val controller = remember { AndroidPermissionController(context) }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { controller.refresh() }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { controller.refresh() }

    controller.notificationLauncher = notificationLauncher
    controller.locationLauncher = locationLauncher

    androidx.compose.runtime.LaunchedEffect(Unit) { controller.refresh() }

    return controller
}
