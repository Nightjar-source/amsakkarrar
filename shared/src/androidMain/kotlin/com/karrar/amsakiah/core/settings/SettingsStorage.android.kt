package com.karrar.amsakiah.core.settings

import android.content.Context
import com.karrar.amsakiah.core.AndroidAppContext

actual object SettingsStorage {
    private const val PREFS = "karar_settings"
    private const val KEY = "app_settings_json"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    actual fun read(): String? = prefs(AndroidAppContext.require()).getString(KEY, null)

    actual fun write(value: String) {
        prefs(AndroidAppContext.require()).edit().putString(KEY, value).apply()
    }
}
