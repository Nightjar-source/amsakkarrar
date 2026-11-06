package com.karrar.amsakiah.core.sensors

actual class CompassProvider {
    actual fun isAvailable(): Boolean = false
    actual fun start(onHeading: (Float) -> Unit) = Unit
    actual fun stop() = Unit
}

actual fun createCompassProvider(): CompassProvider = CompassProvider()
