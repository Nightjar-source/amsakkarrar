package com.karrar.amsakiah.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.karrar.amsakiah.ui.theme.KararIcons

/**
 * وجهات التطبيق السبع — بترتيب `amsak3d.tsx`:
 * اليوم، الضبط والأوقات، التقويم، القبلة والمراقد، الأدعية، المسبحة، فقه المواقيت.
 */
enum class AppDestination(
    val route: String,
    val title: String,
    val shortTitle: String,
    val icon: ImageVector
) {
    TODAY("today", "اليوم", "اليوم", KararIcons.Today),
    SETTINGS("settings", "الضبط والأوقات", "الضبط", KararIcons.Settings),
    CALENDAR("calendar", "التقويم", "التقويم", KararIcons.Calendar),
    QIBLA("qibla", "القبلة والمراقد", "القبلة", KararIcons.Qibla),
    DUAS("duas", "الأدعية والمناجاة", "الأدعية", KararIcons.Duas),
    TASBIH("tasbih", "المسبحة", "المسبحة", KararIcons.Tasbih),
    FIQH("fiqh", "فقه المواقيت", "الفقه", KararIcons.Fiqh)
}
