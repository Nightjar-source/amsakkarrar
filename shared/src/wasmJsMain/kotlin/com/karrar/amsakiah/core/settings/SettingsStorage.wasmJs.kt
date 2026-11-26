package com.karrar.amsakiah.core.settings

private fun lsGet(key: String): String? = js("window.localStorage.getItem(key)")
private fun lsSet(key: String, value: String): Unit = js("window.localStorage.setItem(key, value)")

actual object SettingsStorage {
    private const val KEY = "amsakiah_karar_settings_v1"
    actual fun read(): String? = runCatching { lsGet(KEY) }.getOrNull()
    actual fun write(value: String) {
        runCatching { lsSet(KEY, value) }
    }
}
