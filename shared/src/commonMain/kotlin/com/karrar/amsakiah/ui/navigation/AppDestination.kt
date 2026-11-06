package com.karrar.amsakiah.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

/** شاشات التطبيق السبع — شريط تنقل قابل للتمرير */
enum class AppDestination(
    val route: String,
    val title: String,
    val shortTitle: String,
    val icon: ImageVector
) {
    TODAY("today", "اليوم", "اليوم", Icons.Filled.WbSunny),
    QIBLA("qibla", "المراقد والقبلة", "القبلة", Icons.Filled.Explore),
    CALENDAR("calendar", "الإمساكية", "الإمساكية", Icons.Filled.CalendarMonth),
    DUAS("duas", "الأدعية والمناجاة", "الأدعية", Icons.Filled.MenuBook),
    TASBIH("tasbih", "المسبحة", "المسبحة", Icons.Filled.Spa),
    FIQH("fiqh", "الفقه والمعايير", "الفقه", Icons.Filled.Gavel),
    SETTINGS("settings", "الضبط والإعدادات", "الضبط", Icons.Filled.Settings)
}
