package com.Nightjar.amsakkarrar

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.karrar.amsakiah.App
import com.karrar.amsakiah.core.AndroidAppContext
import com.karrar.amsakiah.core.audio.SoundTestBridge
import com.karrar.amsakiah.core.state.SettingsChangeBus
import com.karrar.amsakiah.ui.startup.StartupGate
import com.Nightjar.amsakkarrar.notifications.AdhanService
import com.Nightjar.amsakkarrar.notifications.LiveNotificationManager
import com.Nightjar.amsakkarrar.notifications.NotificationChannels
import com.Nightjar.amsakkarrar.notifications.PrayerAlarmScheduler
import com.Nightjar.amsakkarrar.widget.WidgetPinHelper
import com.Nightjar.amsakkarrar.widget.WidgetRefreshWorker
import com.Nightjar.amsakkarrar.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val settingsSyncScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * مزامنة فورية: أي تغيير في الإعدادات داخل التطبيق (المدينة، الطريقة،
     * التعديلات، الودجت، الإشعار الحي...) يحدّث الودجت الأربعة والإشعار
     * والمنبهات مباشرة دون انتظار إعادة فتح التطبيق.
     */
    private val settingsChangeListener: () -> Unit = {
        val appContext = applicationContext
        settingsSyncScope.launch {
            runCatching { WidgetUpdater.updateAll(appContext) }
            runCatching { LiveNotificationManager.update(appContext) }
            runCatching { PrayerAlarmScheduler(appContext).rescheduleAll() }
        }
    }

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                LiveNotificationManager.update(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        // نُبقي سبلاش النظام حتى ترسم شاشة البداية الثانية أول إطار لها،
        // فيحدث الوصل بينهما على نفس الخلفية بلا وميض ولا قفزة أيقونة.
        installSplashScreen().setKeepOnScreenCondition { !StartupGate.contentReady }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AndroidAppContext.init(this)
        NotificationChannels.createAll(this)
        WidgetPinHelper.install(this)
        installSoundTestBridge()
        PrayerAlarmScheduler(this).rescheduleAll()
        WidgetUpdater.updateAll(this)
        WidgetRefreshWorker.syncPeriodic(this)
        SettingsChangeBus.addListener(settingsChangeListener)

        requestNotificationPermissionIfNeeded()

        setContent {
            App()
        }
    }

    override fun onDestroy() {
        SettingsChangeBus.removeListener(settingsChangeListener)
        settingsSyncScope.cancel()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        WidgetUpdater.updateAll(this)
        LiveNotificationManager.update(this)
    }

    /** زر «تجربة صوت الأذان» في الإعدادات يشغّل نفس خدمة الأذان الحقيقية. */
    private fun installSoundTestBridge() {
        SoundTestBridge.install(
            play = {
                runCatching {
                    val intent = Intent(this, AdhanService::class.java).apply {
                        action = AdhanService.ACTION_PREVIEW
                        putExtra(AdhanService.EXTRA_PRAYER, "تجربة")
                        putExtra(AdhanService.EXTRA_VIBRATE, false)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent)
                    } else {
                        startService(intent)
                    }
                }.isSuccess
            },
            showLive = {
                val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }
                if (!granted) {
                    // لا يكفي «لا يحدث شيء» — اطلب الإذن أولًا، وأخبر المستخدم.
                    Toast.makeText(this, "فعّل إذن الإشعارات أولًا لتجربة الإشعار", Toast.LENGTH_LONG).show()
                    runCatching { notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS) }
                    false
                } else {
                    LiveNotificationManager.update(this, force = true)
                    true
                }
            },
            isSupported = true
        )
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
