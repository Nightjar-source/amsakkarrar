package com.karrar.amsakiah

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.delay

@Composable
fun App() {
    val state = remember { AppState() }
    var weather by remember { mutableStateOf<WeatherInfo?>(null) }
    var showSplash by remember { mutableStateOf(true) }

    DisposableEffect(state) {
        onDispose { state.dispose() }
    }

    // تهيئة قاعدة المدن الأوفلاين
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

    // إخفاء شاشة البداية بعد 3 ثوانٍ (ليظهر التوقيع بعد اكتمال دخول العنوان)
    LaunchedEffect(Unit) {
        delay(3000)
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

    KararTheme {
        // حجم الخط المختار من الضبط يُطبَّق عالميًا على كل النصوص.
        val baseDensity = LocalDensity.current
        val scaledFont = baseDensity.fontScale * state.settings.fontScale
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl,
            LocalDensity provides Density(baseDensity.density, scaledFont)
        ) {
            Box(Modifier.fillMaxSize().background(Color(0xFF050A15))) {
                AppShell(state = state, weather = weather)

                AnimatedVisibility(
                    visible = showSplash,
                    exit = fadeOut(animationSpec = tween(500))
                ) {
                    SplashScreen()
                }
            }
        }
    }
}
