package com.karrar.amsakiah.core.settings

/**
 * تخزين نصي بسيط (JSON) — SharedPreferences على أندرويد و NSUserDefaults على iOS.
 */
expect object SettingsStorage {
    fun read(): String?
    fun write(value: String)
}
