package com.karrar.amsakiah.ui.startup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * جسر جهوزية الواجهة بين سبلاش النظام (Android 12+) وسبلاش Compose.
 *
 * - `MainActivity` يوقف سبلاش النظام ما دام [contentReady] = `false`.
 * - شاشة البداية الثانية ترفعه بعد أول إطار مرسوم لها، فيحدث الوصل
 *   بين الشاشتين على نفس الخلفية بلا وميض ولا قفزة.
 */
object StartupGate {
    var contentReady by mutableStateOf(false)
        private set

    fun markContentReady() {
        contentReady = true
    }

    /** يُستخدم عند إعادة إنشاء النشاط (مثل تغيير الوضع) لتفادي شاشة عالقة. */
    fun reset() {
        contentReady = false
    }
}
