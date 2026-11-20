package com.karrar.amsakiah.core.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.karrar.amsakiah.core.AndroidAppContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

/**
 * **AppDataStore** — النظام الرسمي الموحّد للتخزين (Jetpack DataStore 1.3.0 المستقر،
 *创始人836-11-17).
 *
 * - ملف واحد `app_settings.preferences_pb` لكل مفاتيح التطبيق.
 * - **كاش ذاكرة فوري** عبر [ConcurrentHashMap] — القراءة `O(1)` بلا أي تعليق للواجهة.
 * - **ترحيل تلقائي** لكل بيانات SharedPreferences القديمة عند أول تشغيل (مرة واحدة).
 * - الكتابة تُطبَّق في الكاش فورًا ثم تُرسل إلى DataStore على [Dispatchers.IO].
 *
 * يخدم: الإعدادات · نمط الودجت · حالة إشعار العدّاد · علامة صلاحية الموقع.
 */
val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

object AppDataStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val cache = ConcurrentHashMap<String, Any>()
    private const val MIGRATED = "__karar_datastore_migrated_v1"

    // مفاتيح DataStore الرسمية
    private val KEY_SETTINGS = stringPreferencesKey("app_settings_json")
    private val KEY_WIDGET_STYLE = stringPreferencesKey("widget_style")
    private val KEY_DISMISSED_UNTIL = longPreferencesKey("live_dismissed_until")
    private val KEY_LOCATION_ASKED = booleanPreferencesKey("location_asked")

    /** تحميل مسبق متزامن + ترحيل لمرة واحدة — يُستدعى من MainActivity قبل setContent. */
    fun initialize(context: Context) {
        runBlocking(Dispatchers.IO) {
            runCatching {
                val prefs = context.appDataStore.data.first()
                prefs[KEY_SETTINGS]?.let { cache[KEY_SETTINGS.name] = it }
                prefs[KEY_WIDGET_STYLE]?.let { cache[KEY_WIDGET_STYLE.name] = it }
                prefs[KEY_DISMISSED_UNTIL]?.let { cache[KEY_DISMISSED_UNTIL.name] = it }
                prefs[KEY_LOCATION_ASKED]?.let { cache[KEY_LOCATION_ASKED.name] = it }
                if (prefs[booleanPreferencesKey(MIGRATED)] != true) {
                    migrateLegacy(context)
                    context.appDataStore.edit { it[booleanPreferencesKey(MIGRATED)] = true }
                }
            }
        }
    }

    /** ترحيل كل SharedPreferences القديمة دون فقد أي بيانات. */
    private suspend fun migrateLegacy(context: Context) {
        runCatching {
            context.getSharedPreferences("karar_settings", Context.MODE_PRIVATE)
                .getString("app_settings_json", null)?.let {
                    cache[KEY_SETTINGS.name] = it
                    context.appDataStore.edit { p -> p[KEY_SETTINGS] = it }
                }
        }
        runCatching {
            context.getSharedPreferences("karar_widget_style", Context.MODE_PRIVATE).all.forEach { (k, v) ->
                if (v is String) {
                    val key = if (k == "style") KEY_WIDGET_STYLE else stringPreferencesKey(k)
                    cache[key.name] = v
                    context.appDataStore.edit { p -> p[key] = v }
                }
            }
        }
        runCatching {
            context.getSharedPreferences("karar_live_state", Context.MODE_PRIVATE)
                .getLong("dismissed_until", 0L).takeIf { it != 0L }?.let {
                    cache[KEY_DISMISSED_UNTIL.name] = it
                    context.appDataStore.edit { p -> p[KEY_DISMISSED_UNTIL] = it }
                }
        }
        runCatching {
            context.getSharedPreferences("karar_permissions", Context.MODE_PRIVATE)
                .getBoolean("location_asked", false).takeIf { it }?.let {
                    cache[KEY_LOCATION_ASKED.name] = it
                    context.appDataStore.edit { p -> p[KEY_LOCATION_ASKED] = true }
                }
        }
    }

    // ---- إعدادات التطبيق ----
    fun readSettings(): String? = cache[KEY_SETTINGS.name] as? String
    fun writeSettings(value: String) = writeString(KEY_SETTINGS, value)

    // ---- نمط الودجت (عام + لكل appWidgetId) ----
    fun readWidgetStyle(key: String): String? = cache[key] as? String ?: cache[KEY_WIDGET_STYLE.name] as? String
    fun writeWidgetStyle(key: String, value: String) {
        val prefKey = if (key == "style") KEY_WIDGET_STYLE else stringPreferencesKey(key)
        writeString(prefKey, value)
    }

    // ---- حالة إخفاء الإشعار ----
    fun readDismissedUntil(): Long = cache[KEY_DISMISSED_UNTIL.name] as? Long ?: 0L
    fun writeDismissedUntil(value: Long) {
        cache[KEY_DISMISSED_UNTIL.name] = value
        scope.launch { runCatching { AndroidAppContext.require().appDataStore.edit { it[KEY_DISMISSED_UNTIL] = value } } }
    }

    // ---- علامة سؤال الموقع ----
    fun locationAsked(): Boolean = cache[KEY_LOCATION_ASKED.name] as? Boolean ?: false
    fun markLocationAsked() {
        cache[KEY_LOCATION_ASKED.name] = true
        scope.launch { runCatching { AndroidAppContext.require().appDataStore.edit { it[KEY_LOCATION_ASKED] = true } } }
    }

    private fun writeString(key: androidx.datastore.preferences.core.Preferences.Key<String>, value: String) {
        cache[key.name] = value
        scope.launch { runCatching { AndroidAppContext.require().appDataStore.edit { it[key] = value } } }
    }
}
