package com.Nightjar.amsakkarrar.notifications

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import com.karrar.amsakiah.core.audio.AndroidAudioSource

/**
 * واجهة أندرويد لتشغيل الأذان من المصدر المشترك نفسه الذي تستخدمه المناجاة.
 *
 * الأولوية في [createPlayer] (عبر [AndroidAudioSource]):
 * `shared/src/commonMain/composeResources/files/adhan.mp3` → `res/raw` → URI.
 *
 * لماذا يبقى `shared/src/androidMain/res/raw/adhan.mp3`؟
 * - `NotificationChannel` يحتاج `android.resource://` أو `content://` ولا يقرأ
 *   `assets`؛ فتبقى نسخة raw واحدة لقناة الاحتياط ([soundUri]) فقط.
 *   الصوت الأساسي يُشغَّل دائمًا من النسخة المشتركة في assets.
 */
object AdhanPlayback {

    const val ADHAN = "adhan"
    const val MUNAJAT = "munajat_manzuma"

    fun resourceId(context: Context, name: String): Int =
        AndroidAudioSource.resourceId(context, name)

    /** URI لقناة الإشعار الاحتياطية — من res/raw فقط. */
    fun soundUri(context: Context, name: String = ADHAN): Uri? =
        AndroidAudioSource.soundUri(context, name)

    /**
     * ينشئ MediaPlayer جاهزًا للتشغيل، أو null مع فشل كل المسارات.
     * لا يستدعي start() — الخدمة تتحكم في دورة الحياة.
     */
    fun createPlayer(
        context: Context,
        name: String = ADHAN,
        usage: Int = AudioAttributes.USAGE_MEDIA
    ): MediaPlayer? = AndroidAudioSource.createPlayer(context, name, usage)
}
