package com.karrar.amsakiah.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.karrar.amsakiah.core.AndroidAppContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.Executor
import kotlin.coroutines.resume

/**
 * مزوّد الموقع الأوفلاين على أندرويد.
 *
 * - يطلب تحديثًا واحدًا فقط (لا يستهلك البطارية في الخلفية).
 * - يعمل على كل الإصدارات: getCurrentLocation على Android 11+،
 *   و requestSingleUpdate على Android 10 وما قبله.
 * - مهلة 15 ثانية ثم يعود لآخر موقع معروف إن وُجد.
 */
actual fun createLocationProvider(): LocationProvider = LocationProvider()

actual class LocationProvider {

    private fun contextOrNull(): Context? = AndroidAppContext.optional()

    actual fun isAvailable(): Boolean = contextOrNull()?.let { ctx ->
        (ctx.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)
            ?.allProviders?.isNotEmpty() == true
    } ?: false

    actual fun isLocationEnabled(): Boolean {
        val ctx = contextOrNull() ?: return false
        val manager = ctx.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            manager.isLocationEnabled
        } else {
            @Suppress("DEPRECATION")
            runCatching {
                manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            }.getOrDefault(false)
        }
    }

    actual fun openLocationSettings() {
        val ctx = contextOrNull() ?: return
        runCatching {
            ctx.startActivity(
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

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
        bestLastKnown(manager)?.let { return it }

        // 2) طلب تحديث واحد مع مهلة قصيرة — يعمل على كل إصدارات أندرويد
        return withTimeoutOrNull(15_000L) { awaitSingleUpdate(manager) }
    }

    @SuppressLint("MissingPermission")
    private fun bestLastKnown(manager: LocationManager): GeoPoint? {
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
        var best: Location? = null
        for (provider in providers) {
            val loc = runCatching {
                if (manager.isProviderEnabled(provider)) manager.getLastKnownLocation(provider) else null
            }.getOrNull() ?: continue
            if (best == null || loc.time > best.time) best = loc
        }
        return best?.let { GeoPoint(it.latitude, it.longitude, it.accuracy) }
    }

    @SuppressLint("MissingPermission")
    private suspend fun awaitSingleUpdate(manager: LocationManager): GeoPoint? =
        suspendCancellableCoroutine { cont ->
            val provider = when {
                manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                else -> return@suspendCancellableCoroutine
            }

            var finished = false
            var listener: LocationListener? = null

            fun finish(location: Location?) {
                if (finished) return
                finished = true
                listener?.let { l -> runCatching { manager.removeUpdates(l) } }
                if (cont.isActive) {
                    cont.resume(location?.let { GeoPoint(it.latitude, it.longitude, it.accuracy) })
                }
            }

            listener = object : LocationListener {
                override fun onLocationChanged(location: Location) = finish(location)

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

                override fun onProviderEnabled(provider: String) = Unit
                override fun onProviderDisabled(provider: String) = finish(null)
            }

            cont.invokeOnCancellation { listener?.let { l -> runCatching { manager.removeUpdates(l) } } }

            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val mainExecutor = Executor { r -> Handler(Looper.getMainLooper()).post(r) }
                    manager.getCurrentLocation(provider, null, mainExecutor) { loc -> finish(loc) }
                } else {
                    @Suppress("DEPRECATION")
                    manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                }
            }.onFailure { finish(null) }
        }
}
