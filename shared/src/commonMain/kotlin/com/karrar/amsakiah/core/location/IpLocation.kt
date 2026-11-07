package com.karrar.amsakiah.core.location

import com.karrar.amsakiah.core.network.httpGetText
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject

/**
 * نتيجة استشعار الموقع عبر عنوان IP — نفس خدمة تطبيق الكفيل (get.geojs.io).
 * لا تُحفظ إلا الإحداثيات واسم المدينة؛ لا يُرسل أي شيء من الجهاز للسيرفر.
 */
data class IpLocation(
    val latitude: Double,
    val longitude: Double,
    val city: String,
    val country: String,
    val timeZoneId: String?
)

private val ipJson = Json { ignoreUnknownKeys = true; isLenient = true }

private const val GEO_JS_URL = "https://get.geojs.io/v1/ip/geo.json"

/**
 * استشعار الموقع عبر IP (يتطلب إنترنت). يعيد null عند فشل الشبكة أو الخدمة،
 * ولا يمسّ إعدادات المستخدم في هذه الحالة.
 */
suspend fun fetchIpLocation(): IpLocation? = withTimeoutOrNull(12_000L) {
    val body = httpGetText(GEO_JS_URL) ?: return@withTimeoutOrNull null
    runCatching {
        val obj = ipJson.parseToJsonElement(body).jsonObject
        fun field(name: String): String? =
            (obj[name] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }

        val lat = field("latitude")?.toDoubleOrNull() ?: return@runCatching null
        val lon = field("longitude")?.toDoubleOrNull() ?: return@runCatching null
        if (lat !in -90.0..90.0 || lon !in -180.0..180.0) return@runCatching null

        val zone = field("timezone")?.takeIf { tz ->
            runCatching { TimeZone.of(tz) }.isSuccess
        }

        IpLocation(
            latitude = lat,
            longitude = lon,
            city = field("city").orEmpty(),
            country = field("country").orEmpty(),
            timeZoneId = zone
        )
    }.getOrNull()
}
