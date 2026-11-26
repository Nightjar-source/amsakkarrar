package com.karrar.amsakiah.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.karrar.amsakiah.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "إمساكية كرار 2026",
        state = rememberWindowState(width = 1120.dp, height = 760.dp)
    ) {
        App()
    }
}
