package com.karrar.amsakiah.core.settings

import platform.Foundation.NSUserDefaults

actual object SettingsStorage {
    private const val KEY = "karar_app_settings_json"

    actual fun read(): String? = NSUserDefaults.standardUserDefaults.stringForKey(KEY)

    actual fun write(value: String) {
        NSUserDefaults.standardUserDefaults.setObject(value, KEY)
    }
}
