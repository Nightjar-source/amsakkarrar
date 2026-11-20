package com.karrar.amsakiah.core.state

/**
 * ناقل تغييرات الإعدادات داخل وحدة shared.
 *
 * يسمح لوحدة androidApp (الودجت + الإشعارات) بالتسجيل كمستمع دون أن تعتمد
 * shared على androidApp، فيتم تحديث الودجت الأربعة والإشعار الحي فور أي تغيير
 * في الإعدادات أو الموقع داخل التطبيق.
 */
object SettingsChangeBus {

    private val listeners = mutableListOf<() -> Unit>()

    fun addListener(listener: () -> Unit) {
        if (!listeners.contains(listener)) listeners += listener
    }

    fun removeListener(listener: () -> Unit) {
        listeners -= listener
    }

    fun notifyChanged() {
        listeners.toList().forEach { listener -> runCatching { listener() } }
    }
}
