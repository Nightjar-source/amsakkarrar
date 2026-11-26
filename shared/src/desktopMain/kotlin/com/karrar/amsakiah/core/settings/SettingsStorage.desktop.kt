package com.karrar.amsakiah.core.settings

import java.io.File

actual object SettingsStorage {
    private val file: File by lazy {
        val dir = File(System.getProperty("user.home"), ".amsakkarrar")
        if (!dir.exists()) dir.mkdirs()
        File(dir, "settings.json")
    }

    actual fun read(): String? = runCatching {
        if (file.exists()) file.readText(Charsets.UTF_8) else null
    }.getOrNull()

    actual fun write(value: String) {
        runCatching { file.writeText(value, Charsets.UTF_8) }
    }
}
