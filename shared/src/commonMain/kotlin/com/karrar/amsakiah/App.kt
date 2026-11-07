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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
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

    // المؤقّت العام (ثانية واحدة)
    LaunchedEffect(Unit) {
        while (true) {
            state.tick()
            delay(1000)
        }
    }

    // إخفاء شاشة البداية بعد 3 ثوانٍ (ليظهر التوقيع بعد اكتمال دخول العنوان)
    LaunchedEffect(Unit) {
        delay(3000)
        showSplash = false
    }

    // الطقس الاختياري (إنترنت)
    LaunchedEffect(
        state.settings.latitude,
        state.settings.longitude,
        state.settings.showWeather
    ) {
        weather = null
        if (!state.settings.showWeather) return@LaunchedEffect
        while (true) {
            weather = runCatching {
                fetchWeather(state.settings.latitude, state.settings.longitude)
            }.getOrNull()
            delay(20 * 60 * 1000L)
        }
    }

    KararTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
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
