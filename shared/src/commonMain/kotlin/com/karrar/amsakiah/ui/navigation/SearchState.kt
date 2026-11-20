package com.karrar.amsakiah.ui.navigation

import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf

/**
 * نص البحث الموحّد لكل الشاشات الداعمة (القرآن، مفاتيح الجنان، الأدعية).
 *
 * تمنح القيمة الافتراضية الآمنة حتى تصحّ المعاينة والاختبارات بلا موفّر،
 * وفي التطبيق الحقيقي يُحقن الموفّر من «الشل» الذي يملك شريط العنوان.
 */
val LocalSearchQuery = compositionLocalOf<State<String>> { mutableStateOf("") }
