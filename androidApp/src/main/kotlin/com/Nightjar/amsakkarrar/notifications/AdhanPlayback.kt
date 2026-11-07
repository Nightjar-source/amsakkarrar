package com.Nightjar.amsakkarrar.notifications

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri

/**
 * تشغيل الأذان المخصص من موارد التطبيق بأكثر من مسار احتياطي:
 *
 * 1) `openRawResourceFd` (الأسرع، ويعمل عندما يكون الملف غير مضغوط).
 * 2) `android.resource://` URI (يعمل حتى لو ضغط AAPT الملف).
 * 3) نسخ الملف إلى cache ثم تشغيله كملف عادي (احتياط أخير).
 *
 * الملف نفسه موجود في:
 * - `shared/src/androidMain/res/raw/adhan.mp3` (يُدمج في APK).
 * - `shared/src/commonMain/composeResources/files/adhan.mp3` (لـ iOS).
 */
object AdhanPlayback {

    const val ADHAN = "adhan"
    const val MUNAJAT = "munajat_manzuma"

    fun resourceId(context: Context, name: String): Int =
        context.resources.getIdentifier(name, "raw", context.packageName)

    fun soundUri(context: Context, name: String = ADHAN): Uri? {
        val id = resourceId(context, name)
        if (id == 0) return null
        return Uri.parse("android.resource://${context.packageName}/$id")
    }

    /**
     * ينشئ MediaPlayer جاهزًا للتشغيل، أو null مع سبب الفشل.
     * لا يستدعي start() — الخدمة تتحكم في دورة الحياة.
     */
    fun createPlayer(
        context: Context,
        name: String = ADHAN,
        usage: Int = AudioAttributes.USAGE_MEDIA
    ): MediaPlayer? {
        val resId = resourceId(context, name)
        if (resId == 0) return null

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

        // 1) file descriptor
        var ready = runCatching {
            context.resources.openRawResourceFd(resId)?.use { afd ->
                player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                true
            } ?: false
        }.getOrDefault(false)

        // 2) resource URI
        if (!ready) {
            ready = runCatching {
                player.reset()
                player.setDataSource(
                    context,
                    Uri.parse("android.resource://${context.packageName}/$resId")
                )
                true
            }.getOrDefault(false)
        }

        // 3) نسخ إلى ملف مؤقت ثم تشغيله
        if (!ready) {
            ready = runCatching {
                player.reset()
                val cache = java.io.File(context.cacheDir, "$name.mp3")
                if (!cache.exists() || cache.length() == 0L) {
                    context.resources.openRawResource(resId).use { input ->
                        cache.outputStream().use { output -> input.copyTo(output) }
                    }
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
}
