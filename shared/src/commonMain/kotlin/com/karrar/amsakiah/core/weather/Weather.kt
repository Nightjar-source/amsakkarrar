package com.karrar.amsakiah.core.weather

data class WeatherInfo(
    val temperatureC: Int,
    val code: Int,
    val description: String,
    val icon: String
)

/**
 * طقس اختياري عبر الإنترنت — لا يعمل إلا عند توفر الشبكة، ولا يؤثر على المواقيت.
 */
expect suspend fun fetchWeather(lat: Double, lon: Double): WeatherInfo?

fun weatherDescription(code: Int): Pair<String, String> = when (code) {
    0 -> "صحو" to "☀️"
    1, 2 -> "غائم جزئيًا" to "🌤️"
    3 -> "غائم" to "☁️"
    45, 48 -> "ضباب" to "🌫️"
    51, 53, 55, 56, 57 -> "رذاذ" to "🌦️"
    61, 63, 65, 66, 67 -> "مطر" to "🌧️"
    71, 73, 75, 77 -> "ثلج" to "🌨️"
    80, 81, 82 -> "زخات مطر" to "🌧️"
    85, 86 -> "زخات ثلج" to "🌨️"
    95, 96, 99 -> "عاصفة رعدية" to "⛈️"
    else -> "غير معروف" to "🌡️"
}
