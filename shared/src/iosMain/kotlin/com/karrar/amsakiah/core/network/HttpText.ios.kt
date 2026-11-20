package com.karrar.amsakiah.core.network

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionConfiguration
import platform.Foundation.dataTaskWithRequest
import platform.posix.memcpy
import kotlin.coroutines.resume

/**
 * تنفيذ iOS عبر NSURLSession — يعمل مع ATS على https فقط (get.geojs.io https).
 * ملاحظة: لا يوجد مضيف iOS في المستودع، لذلك لا يمكن اختبار هذا المسار على Windows.
 */
@OptIn(ExperimentalForeignApi::class)
actual suspend fun httpGetText(url: String, timeoutMillis: Int): String? =
    suspendCancellableCoroutine { cont ->
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl == null) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }
        val configuration = NSURLSessionConfiguration.defaultSessionConfiguration().apply {
            timeoutIntervalForRequest = timeoutMillis / 1000.0
        }
        val session = NSURLSession.sessionWithConfiguration(configuration)
        val request = NSURLRequest.requestWithURL(nsUrl)
        val task = session.dataTaskWithRequest(request) { data, _, error ->
            val text = if (error == null && data != null) data.toByteArray().decodeToString() else null
            cont.resume(text)
            session.finishTasksAndInvalidate()
        }
        cont.invokeOnCancellation {
            task.cancel()
            session.invalidateAndCancel()
        }
        task.resume()
    }

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)
    return ByteArray(size).also { bytes ->
        bytes.usePinned { pinned -> memcpy(pinned.addressOf(0), this.bytes, this.length) }
    }
}
