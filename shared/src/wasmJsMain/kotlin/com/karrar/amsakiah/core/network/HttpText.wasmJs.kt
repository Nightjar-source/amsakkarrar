package com.karrar.amsakiah.core.network

import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.fetch.Response
import kotlin.coroutines.resume
import kotlin.js.Promise

private fun jsFetch(url: String): Promise<Response> = js("fetch(url)")
private fun jsResponseText(resp: Response): Promise<JsString> = js("resp.text()")

actual suspend fun httpGetText(url: String, timeoutMillis: Int): String? = runCatching {
    val resp = awaitPromise<Response>(jsFetch(url))
    if (!resp.ok) return null
    awaitPromise<JsString>(jsResponseText(resp)).toString()
}.getOrNull()

private suspend fun <T : JsAny?> awaitPromise(promise: Promise<T>): T =
    suspendCancellableCoroutine { cont ->
        promise.then(
            onFulfilled = { value ->
                if (cont.isActive) cont.resume(value)
                null
            },
            onRejected = { err ->
                if (cont.isActive) cont.cancel(RuntimeException(err.toString()))
                null
            }
        )
    }
