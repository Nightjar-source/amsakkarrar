package com.karrar.amsakiah.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import java.io.File

/**
 * مصدر الصوت الموحّد على أندرويد — يقرأ من موارد Compose المشتركة.
 *
 * ترتيب المحاولات في [createPlayer]:
 * 1) ملف `composeResources/files/<name>.mp3` منسوخًا إلى `cacheDir/karar_audio`
 *    (هذه هي النسخة المشتركة مع iOS، وهي المرجع).
 * 2) `res/raw/<name>` عبر file descriptor.
 * 3) `android.resource://` URI.
 * 4) نسخ `res/raw` إلى cache ثم تشغيله.
 *
 * لماذا cache وليس assets مباشرة؟
 * - `MediaPlayer.setDataSource` لا يقبل AssetFileDescriptor المضمّوم مع APK
 *   بشكل موثوق، و`android.resource://` لا يصل إلى assets إطلاقًا.
 * - الملف المحلي يدعم `seekTo` بسلاسة ويعمل بلا فك ضغط إضافي.
 *
 * قناة الإشعار الاحتياطية تحتاج URI يفهمه نظام الإشعارات
 * ([soundUri])، لذلك يبقى `res/raw/adhan.mp3` نسخة واحدة للاحتياط فقط.
 */
object AndroidAudioSource {

    private const val ASSET_DIR = "composeResources/com.karrar.amsakiah.resources/files"
    private const val CACHE_DIR = "karar_audio"

    /** يقبل "adhan" أو "adhan.mp3" ويعيد اسم الملف كاملًا. */
    fun fileName(name: String): String =
        if (name.endsWith(".mp3", ignoreCase = true)) name else "$name.mp3"

    fun resourceId(context: Context, name: String): Int =
        context.resources.getIdentifier(
            name.substringBeforeLast('.'),
            "raw",
            context.packageName
        )

    /**
     * URI نظامي لصوت من `res/raw` — يُستخدم لقناة الإشعار الاحتياطية فقط.
     * يعيد null إذا لم تكن النسخة الاحتياطية موجودة.
     */
    fun soundUri(context: Context, name: String): Uri? {
        val id = resourceId(context, name)
        return if (id == 0) null
        else Uri.parse("android.resource://${context.packageName}/$id")
    }

    /**
     * يضمن وجود نسخة محلية حديثة من ملف Compose Resources.
     * تُعاد الملفات القديمة للنسخ إذا حدّث المستخدم التطبيق.
     */
    fun cachedFile(context: Context, name: String): File? {
        val dir = File(context.cacheDir, CACHE_DIR)
        val target = File(dir, fileName(name))
        val packageUpdate = packageUpdateTime(context)

        if (target.length() > 0L && (packageUpdate <= 0L || target.lastModified() >= packageUpdate)) {
            return target
        }
        if (target.exists()) target.delete()

        return runCatching {
            context.assets.open("$ASSET_DIR/${fileName(name)}").use { input ->
                dir.mkdirs()
                // كتابة ذرّية: لا نشغّل ملفًا نصف منسوخ إذا قُتل التطبيق.
                val tmp = File(dir, target.name + ".part")
                tmp.outputStream().use { output -> input.copyTo(output) }
                if (!tmp.renameTo(target)) {
                    tmp.copyTo(target, overwrite = true)
                    tmp.delete()
                }
            }
            target.takeIf { it.length() > 0L }
        }.getOrNull()
    }

    /**
     * ينشئ MediaPlayer مجهّزًا للبدء، أو null مع فشل كل المسارات.
     * لا يستدعي start() — الخدمة أو الواجهة تتحكم في دورة الحياة.
     */
    fun createPlayer(
        context: Context,
        name: String,
        usage: Int = AudioAttributes.USAGE_MEDIA
    ): MediaPlayer? {
        val player = MediaPlayer()

        runCatching {
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(usage)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            player.isLooping = false
            player.setVolume(1f, 1f)
        }

        // 1) النسخة المشتركة من Compose Resources
        var ready = runCatching {
            val file = cachedFile(context, name)
            if (file == null) false
            else {
                player.setDataSource(file.absolutePath)
                true
            }
        }.getOrDefault(false)

        val resId = resourceId(context, name)

        // 2) file descriptor من res/raw
        if (!ready && resId != 0) {
            ready = runCatching {
                val afd = context.resources.openRawResourceFd(resId) ?: return@runCatching false
                afd.use {
                    player.reset()
                    player.setDataSource(it.fileDescriptor, it.startOffset, it.length)
                }
                true
            }.getOrDefault(false)
        }

        // 3) android.resource://
        if (!ready && resId != 0) {
            ready = runCatching {
                player.reset()
                player.setDataSource(
                    context,
                    Uri.parse("android.resource://${context.packageName}/$resId")
                )
                true
            }.getOrDefault(false)
        }

        // 4) نسخ res/raw إلى cache
        if (!ready && resId != 0) {
            ready = runCatching {
                player.reset()
                val cache = File(File(context.cacheDir, CACHE_DIR), fileName(name))
                cache.parentFile?.mkdirs()
                context.resources.openRawResource(resId).use { input ->
                    cache.outputStream().use { output -> input.copyTo(output) }
                }
                player.setDataSource(cache.absolutePath)
                true
            }.getOrDefault(false)
        }

        if (!ready) {
            runCatching { player.release() }
            return null
        }

        return runCatching {
            player.prepare()
            player
        }.getOrElse {
            runCatching { player.release() }
            null
        }
    }

    private fun packageUpdateTime(context: Context): Long {
        // التحديث لا يتغيّر إلا بتثبيت نسخة جديدة؛ تخزينه يوفّر استدعاء
        // PackageManager (Binder) في كل تشغيل للصوت.
        val cached = cachedUpdateTime
        if (cached >= 0L) return cached
        val value = runCatching {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
        }.getOrDefault(0L)
        if (value > 0L) cachedUpdateTime = value
        return value
    }

    @Volatile
    private var cachedUpdateTime = -1L
}
