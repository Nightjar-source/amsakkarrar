package com.karrar.amsakiah

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.karrar.amsakiah.core.permissions.rememberPermissionController
import com.karrar.amsakiah.core.state.AppState
import com.karrar.amsakiah.core.weather.WeatherInfo
import com.karrar.amsakiah.core.weather.fetchWeather
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.components.SpringDialog
import com.karrar.amsakiah.ui.navigation.AppShell
import com.karrar.amsakiah.ui.splash.SplashScreen
import com.karrar.amsakiah.ui.theme.KararTheme
import com.karrar.amsakiah.ui.motion.motionEffects
import kotlinx.coroutines.delay

@Composable
fun App() {
    val state = remember { AppState() }
    var weather by remember { mutableStateOf<WeatherInfo?>(null) }
    var showSplash by remember { mutableStateOf(true) }
    val permissions = rememberPermissionController()
    var showNotificationAsk by remember { mutableStateOf(false) }

    DisposableEffect(state) {
        onDispose { state.dispose() }
    }

    // تهيئة قاعدة المدن الأوفلاين (على خيط خلفي داخل CityDatabase)
    LaunchedEffect(Unit) {
        state.prepareCities()
    }

    // المؤقّت العام (ثانية واحدة) — يتوقف تمامًا عند خروج التطبيق من المقدمة
    // حتى لا يوقظ المعالج كل ثانية في الخلفية.
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            state.tick()
            while (true) {
                delay(1000)
                state.tick()
            }
        }
    }

    // مدة السبلاش الدنيا + طلب إذن الإشعارات بلطف بعد أن تستقر الواجهة:
    // لا نطلب الصلاحية في أول ومضة — ننتظر حتى يرى المستخدم التطبيق أولًا.
    LaunchedEffect(Unit) {
        delay(1600)
        showSplash = false
        if (!permissions.status.notifications) {
            showNotificationAsk = true
        }
    }

    // الطقس الاختياري (إنترنت) — يتوقف في الخلفية ولا يستهلك شبكة/بطارية.
    LaunchedEffect(
        lifecycleOwner,
        state.settings.latitude,
        state.settings.longitude,
        state.settings.showWeather
    ) {
        if (!state.settings.showWeather) {
            weather = null
            return@LaunchedEffect
        }
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                val fresh = runCatching {
                    fetchWeather(state.settings.latitude, state.settings.longitude)
                }.getOrNull()
                // لا نمسح القيمة القديمة عند فشل التحديث: يمنع وميض اختفاء الطقس.
                if (fresh != null) weather = fresh
                delay(20 * 60 * 1000L)
            }
        }
    }

    // الوضع (تلقائي/فاتح/مظلم) يُقرأ من الإعدادات فيتبدّل فورًا بلا إعادة تشغيل.
    KararTheme(mode = state.settings.themeMode) {
        // حجم الخط المختار من الضبط يُطبَّق عالميًا على كل النصوص.
        val baseDensity = LocalDensity.current
        val scaledFont = baseDensity.fontScale * state.settings.fontScale
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl,
            LocalDensity provides Density(baseDensity.density, scaledFont)
        ) {
            // لا طبقة خلفية صلبة هنا: الشل يرسم خلفية واحدة full-bleed خلف
            // أشرطة النظام، والمحتوى وحده يُزاح — فلا كتل لونية على الحواف.
            Box(Modifier.fillMaxSize()) {
                AppShell(state = state, weather = weather)

                AnimatedVisibility(
                    visible = showSplash,
                    exit = fadeOut(animationSpec = motionEffects())
                ) {
                    SplashScreen()
                }

                // طلب إذن الإشعارات — نافذة تأكيد على Spring Motion (دواخل نفس النافذة،
                // full-bleed بدون أشرطة فارغة) بدلًا من حوار النظام.
                SpringDialog(
                    visible = showNotificationAsk,
                    onDismissRequest = { showNotificationAsk = false },
                    dismissible = true
                ) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        corner = 24.dp,
                        contentPadding = PaddingValues(20.dp)
                    ) {
                        Text(
                            text = "إذن الإشعارات",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "لنصلك بطاقة الأذان عند دخول وقت الصلاة، والعدّاد التنازلي للصلاة القادمة " +
                                "في شريط الإشعارات. لا نُصدر أي صوت ما لم تُفعّل ذلك من الإعدادات، " +
                                "ونحترم كتم النظام وزر الصوت.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { showNotificationAsk = false }) {
                                Text("ليس الآن")
                            }
                            TextButton(onClick = {
                                showNotificationAsk = false
                                permissions.requestNotifications()
                            }) {
                                Text("تفعيل الإشعارات")
                            }
                        }
                    }
                }
            }
        }
    }
}
