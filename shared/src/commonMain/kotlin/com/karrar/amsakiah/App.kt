package com.karrar.amsakiah

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.karrar.amsakiah.core.state.AppState
import com.karrar.amsakiah.core.weather.WeatherInfo
import com.karrar.amsakiah.core.weather.fetchWeather
import com.karrar.amsakiah.ui.navigation.AppShell
import com.karrar.amsakiah.ui.splash.SplashScreen
import com.karrar.amsakiah.ui.theme.KararTheme
import com.karrar.amsakiah.ui.theme.KararTokens
import kotlinx.coroutines.delay

@Composable
fun App() {
    val state = remember { AppState() }
    var weather by remember { mutableStateOf<WeatherInfo?>(null) }
    var showSplash by remember { mutableStateOf(true) }

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

    // مدة السبلاش الدنيا: 1.2 ثانية حتى تكتمل حركة الشعار، ثم يتلاشى بسلاسة.
    // (سبلاش النظام يبقى حتى أول إطار فعلي عبر StartupGate، فلا وميض بينهما.)
    LaunchedEffect(Unit) {
        delay(1200)
        showSplash = false
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
                    exit = fadeOut(animationSpec = KararTokens.Motion.effects())
                ) {
                    SplashScreen()
                }
            }
        }
    }
}
