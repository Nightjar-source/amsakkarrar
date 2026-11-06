package com.karrar.amsakiah.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.state.AppState
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.core.weather.WeatherInfo
import com.karrar.amsakiah.ui.adaptive.NavigationLayout
import com.karrar.amsakiah.ui.adaptive.rememberAppAdaptiveLayout
import com.karrar.amsakiah.ui.screens.CalendarScreen
import com.karrar.amsakiah.ui.screens.DuasScreen
import com.karrar.amsakiah.ui.screens.FiqhScreen
import com.karrar.amsakiah.ui.screens.SettingsScreen
import com.karrar.amsakiah.ui.screens.ShrinesQiblaScreen
import com.karrar.amsakiah.ui.screens.TasbihScreen
import com.karrar.amsakiah.ui.screens.TodayScreen
import com.karrar.amsakiah.ui.theme.KararColors

/**
 * جذر التطبيق المتكيّف:
 * - Compact  → شريط سفلي قابل للتمرير (٧ عناصر)
 * - Medium   → شريط جانبي
 * - Expanded → درج تنقل دائم
 *
 * كل قرارات التكيّف تأتي من `ui/adaptive/WindowAdaptive.kt` فقط.
 */
@Composable
fun AdaptiveAppScaffold(
    state: AppState,
    weather: WeatherInfo?,
    modifier: Modifier = Modifier
) {
    val layout = rememberAppAdaptiveLayout()
    var destination by remember { mutableStateOf(AppDestination.TODAY) }
    val settings = state.settings
    val hijri = remember(settings, state.now) {
        PrayerCalculator.hijriFor(settings, PrayerCalculator.todayDate(settings))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(KararColors.BackgroundTop, KararColors.BackgroundBottom)
                )
            )
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Row(Modifier.weight(1f)) {
                if (layout.navigationLayout != NavigationLayout.BOTTOM_BAR) {
                    SideNavigation(
                        destination = destination,
                        compact = layout.navigationLayout == NavigationLayout.RAIL,
                        onSelect = { destination = it }
                    )
                }

                Column(Modifier.weight(1f)) {
                    if (destination != AppDestination.TODAY) {
                        AppTitleBar(
                            title = destination.title,
                            subtitle = "${hijri.day} ${hijri.monthName} ${hijri.year} هـ"
                        )
                    }
                    Crossfade(
                        targetState = destination,
                        animationSpec = tween(220),
                        label = "screen"
                    ) { dest ->
                        val padding = PaddingValues(0.dp)
                        when (dest) {
                            AppDestination.TODAY -> TodayScreen(
                                state = state,
                                weather = weather,
                                onOpenCalendar = { destination = AppDestination.CALENDAR },
                                onOpenSettings = { destination = AppDestination.SETTINGS },
                                onOpenQibla = { destination = AppDestination.QIBLA },
                                contentPadding = padding
                            )
                            AppDestination.QIBLA -> ShrinesQiblaScreen(state, padding)
                            AppDestination.CALENDAR -> CalendarScreen(state, padding)
                            AppDestination.DUAS -> DuasScreen(padding)
                            AppDestination.TASBIH -> TasbihScreen(state, padding)
                            AppDestination.FIQH -> FiqhScreen(state, padding)
                            AppDestination.SETTINGS -> SettingsScreen(state, padding)
                        }
                    }
                }
            }

            if (layout.navigationLayout == NavigationLayout.BOTTOM_BAR) {
                BottomNavigation(
                    destination = destination,
                    onSelect = { destination = it }
                )
            }
        }
    }
}

@Composable
private fun AppTitleBar(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = KararColors.Gold, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = KararColors.TextMuted, fontSize = 10.sp)
        }
        Box(
            Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0x1AFFD700)),
            contentAlignment = Alignment.Center
        ) {
            Text("🌙", fontSize = 13.sp)
        }
    }
}

@Composable
private fun BottomNavigation(
    destination: AppDestination,
    onSelect: (AppDestination) -> Unit
) {
    Column {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(KararColors.Border)
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xCC070C18))
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(AppDestination.entries) { dest ->
                NavItem(
                    destination = dest,
                    selected = dest == destination,
                    compact = true,
                    onClick = { onSelect(dest) }
                )
            }
        }
    }
}

@Composable
private fun SideNavigation(
    destination: AppDestination,
    compact: Boolean,
    onSelect: (AppDestination) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(if (compact) 92.dp else 220.dp)
            .background(Color(0x99070C18))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            items(AppDestination.entries) { dest ->
                NavItem(
                    destination = dest,
                    selected = dest == destination,
                    compact = compact,
                    onClick = { onSelect(dest) }
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    destination: AppDestination,
    selected: Boolean,
    compact: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) KararColors.Gold else Color.Transparent
    val fg = if (selected) Color(0xFF241A00) else KararColors.TextSecondary

    Column(
        modifier = Modifier
            .width(if (compact) 76.dp else 200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, if (selected) Color.Transparent else Color(0x14FFFFFF), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(destination.icon, contentDescription = destination.title, tint = fg, modifier = Modifier.size(18.dp))
            if (!compact) {
                Spacer(Modifier.width(8.dp))
                Text(destination.shortTitle, color = fg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        if (compact) {
            Spacer(Modifier.height(2.dp))
            Text(destination.shortTitle, color = fg, fontSize = 9.sp, maxLines = 1)
        }
    }
}
