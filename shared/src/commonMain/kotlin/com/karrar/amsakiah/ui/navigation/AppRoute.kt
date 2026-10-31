package com.karrar.amsakiah.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute {
    @Serializable data object Home : AppRoute
    @Serializable data object Duas : AppRoute
    @Serializable data object Qibla : AppRoute
    @Serializable data object Observatory : AppRoute
    @Serializable data object Calendar : AppRoute
    @Serializable data object Settings : AppRoute
}
