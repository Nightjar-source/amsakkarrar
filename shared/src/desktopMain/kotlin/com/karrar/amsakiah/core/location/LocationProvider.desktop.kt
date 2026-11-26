package com.karrar.amsakiah.core.location

import java.util.Locale
import java.util.TimeZone

actual fun readOfflineLocationHints(): OfflineLocationHints {
    val tzId = runCatching { TimeZone.getDefault().id }.getOrDefault("")
    val iso = runCatching { Locale.getDefault().country.lowercase() }.getOrDefault("")
    return OfflineLocationHints(countryIso = iso, timeZoneId = tzId)
}

actual class LocationProvider {
    actual fun isAvailable(): Boolean = false
    actual fun isLocationEnabled(): Boolean = false
    actual fun openLocationSettings() = Unit
    actual fun hasPermission(): Boolean = false
    actual suspend fun currentLocation(): GeoPoint? = null
}

actual fun createLocationProvider(): LocationProvider = LocationProvider()
