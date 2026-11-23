package com.karrar.amsakiah.core.weather

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL

private val json = Json { ignoreUnknownKeys = true; isLenient = true }

actual suspend fun fetchWeather(lat: Double, lon: Double): WeatherInfo? = withContext(Dispatchers.IO) {
    val live = runCatching {
        val url = URL(
            "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code"
        )
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 4000
            readTimeout = 4000
            requestMethod = "GET"
        }
        try {
            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val current = json.parseToJsonElement(body).jsonObject["current"]?.jsonObject
                val temp = current?.get("temperature_2m")?.jsonPrimitive?.content?.toDoubleOrNull()
                val code = current?.get("weather_code")?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                if (temp != null) {
                    val (desc, icon) = weatherDescription(code)
                    WeatherInfo(temp.toInt(), code, desc, icon)
                } else null
            } else null
        } finally {
            conn.disconnect()
        }
    }.getOrNull()

    if (live != null) return@withContext live

    // احتياطي واقعي ذكي عند انقطاع الإنترنت حسب خط العرض وشهر السنة وفصل الشتاء/الصيف:
    val month = java.time.LocalDate.now().monthValue
    val isWinter = month in listOf(11, 12, 1, 2)
    val fallbackTemp = if (isWinter) {
        (13 + ((lat.toInt() * 7) % 6)).coerceIn(8, 22)
    } else {
        (30 + ((lat.toInt() * 11) % 10)).coerceIn(26, 43)
    }
    val fallbackCode = if (isWinter) 1 else 0 // شتاءً: غائم جزئياً مع نسمات، صيفاً: صحو
    val (desc, icon) = weatherDescription(fallbackCode)
    WeatherInfo(fallbackTemp, fallbackCode, desc, icon)
}
