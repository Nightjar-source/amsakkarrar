package com.karrar.amsakiah.core.location

private fun jsTimeZone(): String = js("Intl.DateTimeFormat().resolvedOptions().timeZone || ''")
private fun jsLang(): String = js("(navigator.language || '').split('-')[1] || ''")

actual fun readOfflineLocationHints(): OfflineLocationHints {
    val tz = runCatching { jsTimeZone() }.getOrDefault("")
    val iso = runCatching { jsLang().lowercase() }.getOrDefault("")
    return OfflineLocationHints(countryIso = iso, timeZoneId = tz)
}

actual class LocationProvider {
    actual fun isAvailable(): Boolean = false
    actual fun isLocationEnabled(): Boolean = false
    actual fun openLocationSettings() = Unit
    actual fun hasPermission(): Boolean = false
    actual suspend fun currentLocation(): GeoPoint? = null
}

actual fun createLocationProvider(): LocationProvider = LocationProvider()
