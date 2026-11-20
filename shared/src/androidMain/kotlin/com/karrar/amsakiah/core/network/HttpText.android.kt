package com.karrar.amsakiah.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

actual suspend fun httpGetText(url: String, timeoutMillis: Int): String? =
    withContext(Dispatchers.IO) {
        runCatching {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = timeoutMillis
                readTimeout = timeoutMillis
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "KararAmsakiah/8.0 (Android)")
            }
            try {
                if (conn.responseCode !in 200..299) {
                    null
                } else {
                    conn.inputStream.bufferedReader().use { it.readText() }
                }
            } finally {
                runCatching { conn.disconnect() }
            }
        }.getOrNull()
    }
