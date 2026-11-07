package com.karrar.amsakiah.core.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.delay
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse

@OptIn(ExperimentalForeignApi::class)
actual class LocationProvider {

    private val manager = CLLocationManager()

    actual fun isAvailable(): Boolean = true

    actual fun hasPermission(): Boolean {
        val status = CLLocationManager.authorizationStatus()
        return status == kCLAuthorizationStatusAuthorizedWhenInUse ||
            status == kCLAuthorizationStatusAuthorizedAlways
    }

    actual suspend fun currentLocation(): GeoPoint? {
        if (!hasPermission()) return null

        // أولًا: آخر موقع معروف إن وُجد
        manager.location?.let { return it.toGeoPoint() }

        // ثانيًا: تشغيل التحديث مؤقتًا وانتظار أول قراءة (حتى 12 ثانية)
        manager.startUpdatingLocation()
        try {
            repeat(40) {
                manager.location?.let { return it.toGeoPoint() }
                delay(300)
            }
            return null
        } finally {
            manager.stopUpdatingLocation()
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun platform.CoreLocation.CLLocation.toGeoPoint(): GeoPoint {
    val coord = coordinate
    val lat = coord.useContents { latitude }
    val lon = coord.useContents { longitude }
    return GeoPoint(lat, lon, horizontalAccuracy.toFloat())
}

actual fun createLocationProvider(): LocationProvider = LocationProvider()
