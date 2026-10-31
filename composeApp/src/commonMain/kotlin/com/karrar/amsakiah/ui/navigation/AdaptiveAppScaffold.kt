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
import com.karrar.amsakiah.ui.screens.DuasScreen
import com.karrar.amsakiah.ui.screens.HomeScreen
import com.karrar.amsakiah.ui.screens.QiblaScreen
import com.karrar.amsakiah.ui.screens.SettingsScreen

enum class AppDestination(val title: String, val icon: ImageVector) {
    HOME("المواقيت", Icons.Default.AccessTime),
    DUAS("الأدعية", Icons.Default.MenuBook),
    QIBLA("القبلة", Icons.Default.Explore),
    SETTINGS("الإعدادات", Icons.Default.Settings)
}

@Composable
fun AdaptiveAppScaffold(modifier: Modifier = Modifier) {
    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }
    var selectedCity by remember { mutableStateOf(OfflineCities.getDefaultCity()) }
    var calculationMethod by remember { mutableStateOf(OfflineCities.getDefaultMethod()) }
    var highLatRule by remember { mutableStateOf(HighLatitudeRule.AQRAB_45) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B14))
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
    ) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen) {
            // شاشات التابلت والشاشات العريضة (Medium & Expanded Width >= 600dp): شريط ملاحة جانبي NavigationRail
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
                    AppDestination.values().forEach { destination ->
                        NavigationRailItem(
                            selected = (currentDestination == destination),
                            onClick = { currentDestination = destination },
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
                    ScreenContent(
                        destination = currentDestination,
                        selectedCity = selectedCity,
                        onSelectCity = { selectedCity = it },
                        calculationMethod = calculationMethod,
                        onSelectMethod = { calculationMethod = it },
                        highLatRule = highLatRule,
                        onSelectHighLatRule = { highLatRule = it }
                    )
                }
            }
        } else {
            // الهواتف المحمولة في الوضع العمودي (Compact Width < 600dp): شريط سفلي عائم
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    NavigationBar(
                        containerColor = Color(0xFF141122).copy(alpha = 0.95f),
                        contentColor = Color.White,
                        tonalElevation = 8.dp
                    ) {
                        AppDestination.values().forEach { destination ->
                            NavigationBarItem(
                                selected = (currentDestination == destination),
                                onClick = { currentDestination = destination },
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
                    ScreenContent(
                        destination = currentDestination,
                        selectedCity = selectedCity,
                        onSelectCity = { selectedCity = it },
                        calculationMethod = calculationMethod,
                        onSelectMethod = { calculationMethod = it },
                        highLatRule = highLatRule,
                        onSelectHighLatRule = { highLatRule = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenContent(
    destination: AppDestination,
    selectedCity: City,
    onSelectCity: (City) -> Unit,
    calculationMethod: CalculationMethod,
    onSelectMethod: (CalculationMethod) -> Unit,
    highLatRule: HighLatitudeRule,
    onSelectHighLatRule: (HighLatitudeRule) -> Unit
) {
    Crossfade(targetState = destination) { screen ->
        when (screen) {
            AppDestination.HOME -> HomeScreen(
                selectedCity = selectedCity,
                onSelectCity = onSelectCity,
                calculationMethod = calculationMethod,
                highLatRule = highLatRule
            )
            AppDestination.DUAS -> DuasScreen()
            AppDestination.QIBLA -> QiblaScreen(currentCity = selectedCity)
            AppDestination.SETTINGS -> SettingsScreen(
                currentMethod = calculationMethod,
                onSelectMethod = onSelectMethod,
                currentHighLatRule = highLatRule,
                onSelectHighLatRule = onSelectHighLatRule
            )
        }
    }
}
