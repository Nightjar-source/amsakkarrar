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
    runCatching {
        val url = URL(
            "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code"
        )
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 6000
            readTimeout = 6000
            requestMethod = "GET"
        }
        try {
            if (conn.responseCode != 200) return@runCatching null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val current = json.parseToJsonElement(body).jsonObject["current"]?.jsonObject ?: return@runCatching null
            val temp = current["temperature_2m"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: return@runCatching null
            val code = current["weather_code"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            val (desc, icon) = weatherDescription(code)
            WeatherInfo(temp.toInt(), code, desc, icon)
        } finally {
            conn.disconnect()
        }
    }.getOrNull()
}
