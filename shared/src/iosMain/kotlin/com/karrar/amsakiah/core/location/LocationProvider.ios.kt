package com.karrar.amsakiah.core.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
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
        val loc = manager.location ?: return null
        val coord = loc.coordinate
        val lat = coord.useContents { latitude }
        val lon = coord.useContents { longitude }
        return GeoPoint(lat, lon, loc.horizontalAccuracy.toFloat())
    }
}

actual fun createLocationProvider(): LocationProvider = LocationProvider()
