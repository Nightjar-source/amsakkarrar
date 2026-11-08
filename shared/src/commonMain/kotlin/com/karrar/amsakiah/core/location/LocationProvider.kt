package com.karrar.amsakiah.core.location

data class GeoPoint(val lat: Double, val lon: Double, val accuracyMeters: Float = 0f)

/**
 * مزوّد الموقع الأوفلاين — يقرأ إحداثيات الجهاز فقط، ولا يرسلها لأي خادم.
 */
expect class LocationProvider() {
    fun hasPermission(): Boolean
    suspend fun currentLocation(): GeoPoint?
    fun isAvailable(): Boolean

    /** هل خدمة الموقع (GPS) مفعّلة في إعدادات النظام؟ */
    fun isLocationEnabled(): Boolean

    /** يفتح شاشة إعدادات الموقع في النظام — لتشغيل الخدمة المعطّلة. */
    fun openLocationSettings()
}

expect fun createLocationProvider(): LocationProvider
