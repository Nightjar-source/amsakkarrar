package com.karrar.amsakiah.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.astronomy.City
import com.karrar.amsakiah.core.astronomy.CalculationMethod
import com.karrar.amsakiah.core.astronomy.HighLatitudeRule
import com.karrar.amsakiah.core.astronomy.OfflineCities
import com.karrar.amsakiah.ui.screens.*

enum class BottomBarDestination(val route: AppRoute, val title: String, val icon: ImageVector) {
    HOME(AppRoute.Home, "المواقيت", Icons.Default.AccessTime),
    DUAS(AppRoute.Duas, "الأدعية", Icons.Default.MenuBook),
    QIBLA(AppRoute.Qibla, "القبلة", Icons.Default.Explore),
    SETTINGS(AppRoute.Settings, "الإعدادات", Icons.Default.Settings)
}

@Composable
fun AdaptiveAppScaffold(modifier: Modifier = Modifier) {
    // Navigation 3: مكدس التنقل المملوك للمستخدم (User-Owned Back Stack)
    val backStack = remember { mutableStateListOf<AppRoute>(AppRoute.Home) }
    val currentRoute = backStack.lastOrNull() ?: AppRoute.Home

    var selectedCity by remember { mutableStateOf(OfflineCities.getDefaultCity()) }
    var calculationMethod by remember { mutableStateOf(OfflineCities.getDefaultMethod()) }
    var highLatRule by remember { mutableStateOf(HighLatitudeRule.AQRAB_45) }

    fun navigateTo(route: AppRoute) {
        if (backStack.lastOrNull() != route) {
            backStack.add(route)
        }
    }

    fun goBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.size - 1)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B14))
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
    ) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen) {
            // شاشات التابلت والشاشات العريضة (NavigationRail)
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = Color(0xFF141122),
                    contentColor = Color.White,
                    header = {
                        Icon(
                            imageVector = Icons.Default.Brightness3,
                            contentDescription = "الهلال",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.padding(vertical = 16.dp).size(28.dp)
                        )
                    }
                ) {
                    BottomBarDestination.values().forEach { destination ->
                        NavigationRailItem(
                            selected = (currentRoute == destination.route),
                            onClick = { navigateTo(destination.route) },
                            icon = { Icon(destination.icon, contentDescription = destination.title) },
                            label = { Text(destination.title, fontSize = 11.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = Color(0xFF1E1B2E),
                                selectedTextColor = Color(0xFFFFD54F),
                                indicatorColor = Color(0xFFFFD54F),
                                unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                unselectedTextColor = Color.White.copy(alpha = 0.6f)
                            )
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ScreenDisplay(
                        currentRoute = currentRoute,
                        selectedCity = selectedCity,
                        onSelectCity = { selectedCity = it },
                        calculationMethod = calculationMethod,
                        onSelectMethod = { calculationMethod = it },
                        highLatRule = highLatRule,
                        onSelectHighLatRule = { highLatRule = it },
                        onNavigate = { navigateTo(it) },
                        onBack = { goBack() }
                    )
                }
            }
        } else {
            // الهواتف الذكية (Bottom Navigation Bar)
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    NavigationBar(
                        containerColor = Color(0xFF141122).copy(alpha = 0.95f),
                        contentColor = Color.White,
                        tonalElevation = 8.dp
                    ) {
                        BottomBarDestination.values().forEach { destination ->
                            NavigationBarItem(
                                selected = (currentRoute == destination.route),
                                onClick = { navigateTo(destination.route) },
                                icon = { Icon(destination.icon, contentDescription = destination.title) },
                                label = { Text(destination.title, fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF1E1B2E),
                                    selectedTextColor = Color(0xFFFFD54F),
                                    indicatorColor = Color(0xFFFFD54F),
                                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    ScreenDisplay(
                        currentRoute = currentRoute,
                        selectedCity = selectedCity,
                        onSelectCity = { selectedCity = it },
                        calculationMethod = calculationMethod,
                        onSelectMethod = { calculationMethod = it },
                        highLatRule = highLatRule,
                        onSelectHighLatRule = { highLatRule = it },
                        onNavigate = { navigateTo(it) },
                        onBack = { goBack() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenDisplay(
    currentRoute: AppRoute,
    selectedCity: City,
    onSelectCity: (City) -> Unit,
    calculationMethod: CalculationMethod,
    onSelectMethod: (CalculationMethod) -> Unit,
    highLatRule: HighLatitudeRule,
    onSelectHighLatRule: (HighLatitudeRule) -> Unit,
    onNavigate: (AppRoute) -> Unit,
    onBack: () -> Unit
) {
    Crossfade(targetState = currentRoute) { screen ->
        when (screen) {
            is AppRoute.Home -> HomeScreen(
                selectedCity = selectedCity,
                onSelectCity = onSelectCity,
                calculationMethod = calculationMethod,
                highLatRule = highLatRule,
                onNavigateToCalendar = { onNavigate(AppRoute.Calendar) },
                onNavigateToObservatory = { onNavigate(AppRoute.Observatory) }
            )
            is AppRoute.Calendar -> CalendarScreen(
                currentCity = selectedCity,
                calculationMethod = calculationMethod,
                onBack = onBack
            )
            is AppRoute.Observatory -> ObservatoryScreen(
                currentCity = selectedCity,
                calculationMethod = calculationMethod,
                highLatRule = highLatRule,
                onBack = onBack
            )
            is AppRoute.Duas -> DuasScreen()
            is AppRoute.Qibla -> QiblaScreen(currentCity = selectedCity)
            is AppRoute.Settings -> SettingsScreen(
                currentMethod = calculationMethod,
                onSelectMethod = onSelectMethod,
                currentHighLatRule = highLatRule,
                onSelectHighLatRule = onSelectHighLatRule
            )
        }
    }
}
