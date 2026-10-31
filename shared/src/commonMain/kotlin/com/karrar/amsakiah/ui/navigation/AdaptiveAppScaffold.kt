package com.karrar.amsakiah.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.astronomy.City
import com.karrar.amsakiah.core.astronomy.CalculationMethod
import com.karrar.amsakiah.core.astronomy.HighLatitudeRule
import com.karrar.amsakiah.core.astronomy.OfflineCities
import com.karrar.amsakiah.ui.screens.*

/**
 * علامات التبويب السبعة الكاملة لتطبيق إمساكية كرار 2026 المنقولة حرفياً من تطبيق React
 */
enum class BottomBarDestination(val route: AppRoute, val title: String, val icon: ImageVector) {
    SKY_ARC(AppRoute.SkyArc, "اليوم", Icons.Default.WbTwilight),
    OBSERVATORY(AppRoute.Observatory, "القبلة والأهلة", Icons.Default.Explore),
    MONTHLY(AppRoute.Monthly, "الإمساكية", Icons.Default.CalendarMonth),
    DUAS(AppRoute.Duas, "الأدعية", Icons.Default.Favorite),
    TASBIH(AppRoute.Tasbih, "المسبحة", Icons.Default.AutoAwesome),
    ANGLES_MATRIX(AppRoute.AnglesMatrix, "مصفوفة الزوايا", Icons.Default.Layers),
    COMPARISON(AppRoute.Comparison, "فقه المواقيت", Icons.Default.MenuBook)
}

@Composable
fun AdaptiveAppScaffold(modifier: Modifier = Modifier) {
    // Navigation 3: مكدس التنقل المملوك للمستخدم (User-Owned Back Stack)
    val backStack = remember { mutableStateListOf<AppRoute>(AppRoute.SkyArc) }
    val currentRoute = backStack.lastOrNull() ?: AppRoute.SkyArc

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
            // الهواتف الذكية (Bottom Navigation Bar مع ScrollableTabRow للأقسام السبعة)
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    Surface(
                        color = Color(0xFF141122).copy(alpha = 0.96f),
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                    ) {
                        val selectedIndex = BottomBarDestination.values().indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
                        ScrollableTabRow(
                            selectedTabIndex = selectedIndex,
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFFFFD54F),
                            edgePadding = 8.dp,
                            indicator = { tabPositions ->
                                if (selectedIndex < tabPositions.size) {
                                    TabRowDefaults.SecondaryIndicator(
                                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                                        color = Color(0xFFFFD54F),
                                        height = 3.dp
                                    )
                                }
                            },
                            divider = {}
                        ) {
                            BottomBarDestination.values().forEach { destination ->
                                val isSelected = currentRoute == destination.route
                                Tab(
                                    selected = isSelected,
                                    onClick = { navigateTo(destination.route) },
                                    icon = {
                                        Icon(
                                            destination.icon,
                                            contentDescription = destination.title,
                                            tint = if (isSelected) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    text = {
                                        Text(
                                            destination.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.6f)
                                        )
                                    }
                                )
                            }
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
            is AppRoute.SkyArc -> HomeScreen(
                selectedCity = selectedCity,
                onSelectCity = onSelectCity,
                calculationMethod = calculationMethod,
                highLatRule = highLatRule,
                onNavigateToCalendar = { onNavigate(AppRoute.Monthly) },
                onNavigateToObservatory = { onNavigate(AppRoute.Observatory) }
            )
            is AppRoute.Observatory -> ObservatoryScreen(
                currentCity = selectedCity,
                calculationMethod = calculationMethod,
                highLatRule = highLatRule,
                onBack = onBack
            )
            is AppRoute.Monthly -> MonthlyScreen(
                currentCity = selectedCity,
                calculationMethod = calculationMethod,
                highLatRule = highLatRule
            )
            is AppRoute.Duas -> DuasScreen()
            is AppRoute.Tasbih -> TasbihScreen()
            is AppRoute.AnglesMatrix -> AnglesMatrixScreen(
                currentMethod = calculationMethod,
                onSelectMethod = onSelectMethod
            )
            is AppRoute.Comparison -> ComparisonScreen()
        }
    }
}
