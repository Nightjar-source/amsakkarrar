package com.karrar.amsakiah.core

import android.content.Context

/**
 * حامل سياق التطبيق — يُهيأ من MainActivity قبل عرض الواجهة.
 * يُستخدم للوصول إلى SharedPreferences والموارد الصوتية.
 */
object AndroidAppContext {
    private var appContext: Context? = null

    fun init(context: Context) {
        if (appContext == null) appContext = context.applicationContext
    }

    fun require(): Context = appContext
        ?: error("AndroidAppContext لم يُهيأ — استدعِ AndroidAppContext.init(context) في MainActivity")

    fun optional(): Context? = appContext
}
