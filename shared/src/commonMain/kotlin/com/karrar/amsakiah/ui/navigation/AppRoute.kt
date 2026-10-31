package com.karrar.amsakiah.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute {
    @Serializable data object SkyArc : AppRoute       // اليوم (القوس السماوي والمواقيت)
    @Serializable data object Observatory : AppRoute  // القبلة والأهلة (مرصد الكفيل 3D)
    @Serializable data object Monthly : AppRoute      // التقويم والإمساكية
    @Serializable data object Duas : AppRoute         // الأدعية والزيارات
    @Serializable data object Tasbih : AppRoute       // مسبحة الزهراء
    @Serializable data object AnglesMatrix : AppRoute // مصفوفة الزوايا
    @Serializable data object Comparison : AppRoute   // فقه المواقيت
}
