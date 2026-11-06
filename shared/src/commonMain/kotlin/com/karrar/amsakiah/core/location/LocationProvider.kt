package com.karrar.amsakiah.core.location

data class GeoPoint(val lat: Double, val lon: Double, val accuracyMeters: Float = 0f)

/**
 * مزوّد الموقع الأوفلاين — يقرأ إحداثيات الجهاز فقط، ولا يرسلها لأي خادم.
 */
expect class LocationProvider() {
    fun hasPermission(): Boolean
    suspend fun currentLocation(): GeoPoint?
    fun isAvailable(): Boolean
}

expect fun createLocationProvider(): LocationProvider
