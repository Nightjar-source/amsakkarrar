package com.karrar.amsakiah.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.BlurCircular
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * أيقونات التطبيق الدينية/الوظيفية.
 *
 * اخترنا أيقونات Material Symbols واضحة الدلالة بدل الأشكال العامة السابقة:
 * - اليوم: هلال ونجوم (NightsStay).
 * - الضبط: مقابض ضبط (Tune).
 * - التقويم: تقويم صريح (CalendarMonth).
 * - القبلة والمراقد: مسجد وقبة (Mosque).
 * - الأدعية: مصحف مفتوح (MenuBook).
 * - المسبحة: حلقة خرزات (BlurCircular).
 * - فقه المواقيت: ميزان العدل (Balance).
 *
 * ملاحظة: تُستبدل لاحقًا بأيقونات مرسومة مخصّصة إن لزم، مع الحفاظ على نفس
 * الأسماء حتى لا تتغير بقية الشاشات.
 */
object KararIcons {
    val Today: ImageVector = Icons.Filled.NightsStay
    val Settings: ImageVector = Icons.Filled.Tune
    val Calendar: ImageVector = Icons.Filled.CalendarMonth
    val Qibla: ImageVector = Icons.Filled.Mosque
    val Duas: ImageVector = Icons.Filled.MenuBook
    val Tasbih: ImageVector = Icons.Filled.BlurCircular
    val Fiqh: ImageVector = Icons.Filled.Balance

    /**
     * أيقونة وقت الصلاة الموحّدة (تُستخدم في شاشة اليوم والتقويم والودجتات).
     * مصدر واحد يمنع اختلاف الأيقونة بين الشاشات.
     */
    fun prayer(id: String): ImageVector = when (id) {
        "imsak", "isha", "midnight" -> Icons.Filled.NightsStay
        "fajr", "isfar", "sunrise" -> Icons.Filled.WbTwilight
        "dhuhr", "asr" -> Icons.Filled.WbSunny
        "sunset", "maghrib" -> Icons.Filled.WbTwilight
        else -> Icons.Filled.WbSunny
    }
}
