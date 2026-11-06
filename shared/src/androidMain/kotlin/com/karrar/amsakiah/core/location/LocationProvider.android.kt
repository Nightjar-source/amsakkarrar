package com.karrar.amsakiah.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.karrar.amsakiah.core.AndroidAppContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executor
import kotlin.coroutines.resume

actual class LocationProvider {

    private fun contextOrNull(): Context? = AndroidAppContext.optional()

    actual fun isAvailable(): Boolean = contextOrNull()?.let {
        it.getSystemService(Context.LOCATION_SERVICE) is LocationManager
    } ?: false

    actual fun hasPermission(): Boolean {
        val ctx = contextOrNull() ?: return false
        val fine = ctx.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val coarse = ctx.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    actual suspend fun currentLocation(): GeoPoint? {
        val ctx = contextOrNull() ?: return null
        if (!hasPermission()) return null
        val manager = ctx.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null

        // 1) آخر موقع معروف من أي مزوّد (أسرع وأوفر للبطارية)
        runCatching {
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )
            for (provider in providers) {
                if (!manager.isProviderEnabled(provider)) continue
                val loc = manager.getLastKnownLocation(provider)
                if (loc != null) return GeoPoint(loc.latitude, loc.longitude, loc.accuracy)
            }
        }

        // 2) طلب تحديث واحد مع مهلة قصيرة (أندرويد 11+)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        return suspendCancellableCoroutine { cont ->
            val mainExecutor = Executor { r -> Handler(Looper.getMainLooper()).post(r) }
            runCatching {
                manager.getCurrentLocation(LocationManager.NETWORK_PROVIDER, null, mainExecutor) { loc ->
                    if (cont.isActive) cont.resume(loc?.let { GeoPoint(it.latitude, it.longitude, it.accuracy) })
                }
            }.onFailure { if (cont.isActive) cont.resume(null) }
        }
    }
}

actual fun createLocationProvider(): LocationProvider = LocationProvider()
