package com.Nightjar.amsakkarrar.notifications

import android.content.Context
import com.karrar.amsakiah.core.AndroidAppContext
import com.karrar.amsakiah.core.settings.AppSettings
import com.karrar.amsakiah.core.settings.SettingsStorage
import kotlinx.serialization.json.Json

/** حفظ الإعدادات من خارج واجهة Compose (ودجت المسبحة / الإجراءات السريعة). */
object SettingsSaver {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    fun save(context: Context, settings: AppSettings) {
        AndroidAppContext.init(context)
        runCatching {
            SettingsStorage.write(json.encodeToString(AppSettings.serializer(), settings))
        }
    }
}
