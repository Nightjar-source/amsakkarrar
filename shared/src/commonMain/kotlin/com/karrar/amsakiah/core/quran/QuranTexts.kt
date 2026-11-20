package com.karrar.amsakiah.core.quran

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * نصوص القرآن الكريم أوفلاين — مضمّنة في الحزمة (`files/quran_texts.json`).
 *
 * المفتاح: 1000 + رقم السورة — تمامًا كما في مرجع مفاتيح الجنان/الكفيل.
 * التحليل مرة واحدة لكل عملية؛ قراءة القرآن لا تطلب شبكة أبدًا.
 */
object QuranTexts {
    private var data: Map<Int, String>? = null

    /** حمّل الفهرس من البايتات مرة واحدة؛ التكرار no-op. */
    suspend fun load(bytes: ByteArray) {
        if (data != null) return
        runCatching {
            var jsonStr = bytes.decodeToString()
            if (jsonStr.startsWith("\uFEFF")) {
                jsonStr = jsonStr.substring(1)
            }
            val parsed = Json.parseToJsonElement(jsonStr).jsonObject
                .mapKeys { it.key.toInt() }
                .mapValues { it.value.jsonPrimitive.content }
            data = parsed
        }
    }

    /** هل الفهرس جاهز؟ */
    val isLoaded: Boolean get() = data != null

    /** النص الكامل لسورة برقمها (1..114) أو null عند غياب المحتوى. */
    fun bySurah(number: Int): String? = data?.get(1000 + number)
}
