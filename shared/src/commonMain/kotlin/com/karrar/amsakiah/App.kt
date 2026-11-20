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
import com.karrar.amsakiah.ui.permissions.rememberPermissionController
import com.karrar.amsakiah.ui.state.AppState
import com.karrar.amsakiah.core.weather.WeatherInfo
import com.karrar.amsakiah.core.weather.fetchWeather
import com.karrar.amsakiah.core.jinan.MonthAlmanacCache
import com.karrar.amsakiah.core.jinan.buildMonthAlmanac
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.components.SpringDialog
import com.karrar.amsakiah.ui.navigation.AppShell
import com.karrar.amsakiah.ui.splash.SplashScreen
import com.karrar.amsakiah.ui.theme.KararTheme
import com.karrar.amsakiah.ui.motion.motionEffects
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    // المعالجة المسبقة لمفاتيح الجنان (تُقل بعد الإقلاع، على خيط خلفي):
    // 1) فهرس الأعمال المسبق (DeedIndex) — تطبيع كل عنوان مرة واحدة فقط.
    // 2) تحليل JSON كامل للكتاب (~2.9MB) مرة واحدة لكل جلسة.
    // 3) بناء منقصة الأشهر الاثني عشر وتخزينها — فتح «أعمال اليوم» أو تبديل
    //    الشهر لا يظهر أي «جارٍ الفحص» ولا يتجمّد التطبيق.
    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            runCatching { com.karrar.amsakiah.core.jinan.DeedIndex.warmUp() }
            runCatching { state.prepareJinanContent() }
        }
        withContext(Dispatchers.Default) {
            val settings = state.settings
            val today = PrayerCalculator.todayDate(settings)
            val hijri = PrayerCalculator.hijriFor(settings, today)
            // الشهر الحالي أولًا (أهم شاشة)، ثم بقية الأشهر ليكون التبديل فوريًا.
            runCatching {
                val current = buildMonthAlmanac(settings, hijri.year, hijri.monthNum, today)
                MonthAlmanacCache.put(current, settings.hijriOffset)
            }
            for (month in 1..12) {
                if (month == hijri.monthNum) continue
                runCatching {
                    val almanac = buildMonthAlmanac(settings, hijri.year, month, today)
                    MonthAlmanacCache.put(almanac, settings.hijriOffset)
                }
            }
        }
    }

    // المؤقّت العام (ثانية واحدة) — يتوقف تمامًا عند خروج التطبيق من المقدمة
    // حتى لا يوقظ المعالج كل ثانية في الخلفية. وفي كل عودة للمقدمة نزامن
    // حالة الموقع بصمت: إن صار مفعّلًا تتلاشى كبسولة «معطّلة» ويعود التطبيق للعمل.
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            state.tick()
            // إن كان آخر خطأ هو إطفاء خدمة الموقع — أعد الفحص بصمت.
            if (state.locationServiceDisabled) {
                if (state.syncLocationSystemState() && state.settings.useGps) {
                    try {
                        state.detectLocation()
                    } catch (e: kotlinx.coroutines.CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        // صامت — لا نزعج المستخدم إن فشلت إعادة الكشف على العودة
                    }
                }
            }
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
