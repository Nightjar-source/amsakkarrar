package com.karrar.amsakiah.core.settings

/**
 * واجهة التخزين المشتركة — على أندرويد تُدار عبر [AppDataStore]
 * (Jetpack DataStore + كاش ذاكرة فوري + ترحيل تلقائي من SharedPreferences).
 */
actual object SettingsStorage {
    actual fun read(): String? = AppDataStore.readSettings()
    actual fun write(value: String) = AppDataStore.writeSettings(value)
}
