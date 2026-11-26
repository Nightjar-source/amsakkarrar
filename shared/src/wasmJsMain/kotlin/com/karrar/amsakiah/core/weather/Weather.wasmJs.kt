package com.karrar.amsakiah.core.weather

import com.karrar.amsakiah.core.network.httpGetText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.roundToInt

private val weatherJson = Json { ignoreUnknownKeys = true }

actual suspend fun fetchWeather(lat: Double, lon: Double): WeatherInfo? {
    val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code"
    val body = httpGetText(url, timeoutMillis = 6_000) ?: return null
    return runCatching {
        val root = weatherJson.parseToJsonElement(body).jsonObject
        val current = root["current"]?.jsonObject ?: return null
        val temp = current["temperature_2m"]?.jsonPrimitive?.doubleOrNull?.roundToInt() ?: return null
        val code = current["weather_code"]?.jsonPrimitive?.intOrNull ?: 0
        WeatherInfo(temperatureC = temp, code = code, labelAr = wmoLabelAr(code))
    }.getOrNull()
}
