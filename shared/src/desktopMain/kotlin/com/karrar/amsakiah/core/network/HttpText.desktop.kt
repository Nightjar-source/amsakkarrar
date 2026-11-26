package com.karrar.amsakiah.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

actual suspend fun httpGetText(url: String, timeoutMillis: Int): String? = withContext(Dispatchers.IO) {
    runCatching {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = timeoutMillis
        conn.readTimeout = timeoutMillis
        conn.setRequestProperty("User-Agent", "AmsakiahKarar-Desktop/1.3")
        try {
            if (conn.responseCode in 200..299) {
                conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } else null
        } finally {
            conn.disconnect()
        }
    }.getOrNull()
}
