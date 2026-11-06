package com.Nightjar.amsakkarrar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.karrar.amsakiah.App
import com.karrar.amsakiah.core.AndroidAppContext
import com.Nightjar.amsakkarrar.notifications.LiveNotificationManager
import com.Nightjar.amsakkarrar.notifications.LiveUpdateScheduler
import com.Nightjar.amsakkarrar.notifications.NotificationChannels
import com.Nightjar.amsakkarrar.notifications.PrayerAlarmScheduler
import com.Nightjar.amsakkarrar.widget.WidgetUpdater

class MainActivity : ComponentActivity() {

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                LiveNotificationManager.update(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AndroidAppContext.init(this)
        NotificationChannels.createAll(this)
        PrayerAlarmScheduler(this).rescheduleAll()
        LiveUpdateScheduler(this).schedule()
        WidgetUpdater.updateAll(this)

        requestNotificationPermissionIfNeeded()

        setContent {
            App()
        }
    }

    override fun onResume() {
        super.onResume()
        WidgetUpdater.updateAll(this)
        LiveNotificationManager.update(this)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            runCatching { notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS) }
        }
    }
}
