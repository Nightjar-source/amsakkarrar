package com.karrar.amsakiah.core.sensors

/** بوصلة الجهاز — اتجاه الشمال المغناطيسي بالدرجات */
expect class CompassProvider() {
    fun isAvailable(): Boolean
    fun start(onHeading: (Float) -> Unit)
    fun stop()
}

expect fun createCompassProvider(): CompassProvider
