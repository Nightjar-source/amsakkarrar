package com.karrar.amsakiah.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.karrar.amsakiah.ui.theme.KararIcons

/**
 * وجهات التطبيق الأساسية (شريط التنقل السفلي والجانبي) — بالترتيب:
 *
 * اليوم ← التقويم ← **المفاتيح** ← فقه المواقيت ← **الضبط (في الأخير)**.
 *
 * «المفاتيح» نافذة أيقونات تجمع: الأدعية والمناجاة، القرآن الكريم،
 * البوصلة والمراقد، المسبحة، وأعمال اليوم (ملاحظات/مهام). الشاشات
 * بداخلها فرعية في الـback stack — لا تُكدَّس أبدًا أكثر من
 * `اليوم ← المفاتيح ← الشاشة`، والرجوع يكون دائمًا خطوة واحدة.
 */
enum class AppDestination(
    val route: String,
    val title: String,
    val shortTitle: String,
    val icon: ImageVector
) {
    TODAY("today", "اليوم", "اليوم", KararIcons.Today),
    CALENDAR("calendar", "التقويم", "التقويم", KararIcons.Calendar),
    KEYS("keys", "المفاتيح", "المفاتيح", KararIcons.Duas),
    FIQH("fiqh", "فقه المواقيت", "الفقه", KararIcons.Fiqh),
    SETTINGS("settings", "الضبط والأوقات", "الضبط", KararIcons.Settings)
}
