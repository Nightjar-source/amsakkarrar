package com.karrar.amsakiah.core.jinan

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object JinanContentTexts {
    private var data: Map<Int, String>? = null
    private val processed = mutableMapOf<Int, String>()

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    suspend fun load(bytes: ByteArray) {
        if (data != null) return
        try {
            var jsonStr = bytes.decodeToString()
            // Strip BOM if exists
            if (jsonStr.startsWith("\uFEFF")) {
                jsonStr = jsonStr.substring(1)
            }
            
            val json = Json.parseToJsonElement(jsonStr).jsonObject
            val mapData = json.mapKeys { it.key.toInt() }.mapValues { it.value.jsonPrimitive.content }.toMutableMap()
            
            data = mapData
            _isLoaded.value = true
            kotlinx.coroutines.CoroutineScope(Dispatchers.Default).launch {
                data?.forEach { (id, text) ->
                    if (text.isNotBlank()) processed[id] = JinanNormalizer.normalize(text)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            data = emptyMap()
            _isLoaded.value = true
        }
    }

    fun getContent(id: Int): String = data?.get(id) ?: ""

    fun getProcessed(id: Int): String = processed[id] ?: getContent(id)

    fun getAllEntries(): Map<Int, String> = processed.toMap()
}
