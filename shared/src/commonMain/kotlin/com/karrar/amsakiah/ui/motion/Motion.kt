package com.karrar.amsakiah.ui.motion

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.runtime.Composable
import com.karrar.amsakiah.ui.platform.rememberReduceMotion
import com.karrar.amsakiah.ui.theme.KararTokens

/**
 * مواصفات الحركة مع احترام «تقليل الحركة».
 *
 * كل حركة مكانية/تزيينية في التطبيق تمر من هنا بدل `KararTokens.Motion` مباشرة،
 * فتصبح النتيجة واحدة: نابض M3 Expressive في الوضع العادي، وقطع فوري (`snap`)
 * حين يطلب المستخدم تقليل الحركة — بلا أي إعادة تركيب إضافية ولا تغيير في الشجرة.
 */
@Composable
fun <T> motionSpatial(
    stiffness: Float = KararTokens.Motion.SpatialStiffnessDefault,
    dampingRatio: Float = KararTokens.Motion.SpatialDamping
): FiniteAnimationSpec<T> =
    if (rememberReduceMotion()) snap()
    else KararTokens.Motion.spatial(stiffness = stiffness, dampingRatio = dampingRatio)

/** نابض التأثيرات (تلاشٍ/شفافية) — يُحترم فيه تقليل الحركة أيضًا. */
@Composable
fun <T> motionEffects(
    stiffness: Float = KararTokens.Motion.EffectsStiffnessDefault,
    dampingRatio: Float = KararTokens.Motion.EffectsDamping
): FiniteAnimationSpec<T> =
    if (rememberReduceMotion()) snap()
    else KararTokens.Motion.effects(stiffness = stiffness, dampingRatio = dampingRatio)

/**
 * هل نسمح بالحركات البيئية اللانهائية (نجوم/سحاب/مطر/نبض)؟
 * تُطفأ كليًا في وضع «تقليل الحركة».
 */
@Composable
fun ambientMotionEnabled(): Boolean = !rememberReduceMotion()

/**
 * نسخة غير تركيبية — للاستخدام داخل lambdas غير تركيبية مثل
 * `transitionSpec` في `AnimatedContent`. مرِّر قيمة «تقليل الحركة» من
 * النطاق التركيبي المحيط (`rememberReduceMotion()`).
 */
fun <T> spatialSpec(
    reduceMotion: Boolean,
    stiffness: Float = KararTokens.Motion.SpatialStiffnessDefault,
    dampingRatio: Float = KararTokens.Motion.SpatialDamping
): FiniteAnimationSpec<T> =
    if (reduceMotion) snap() else KararTokens.Motion.spatial(stiffness = stiffness, dampingRatio = dampingRatio)

fun <T> effectsSpec(
    reduceMotion: Boolean,
    stiffness: Float = KararTokens.Motion.EffectsStiffnessDefault,
    dampingRatio: Float = KararTokens.Motion.EffectsDamping
): FiniteAnimationSpec<T> =
    if (reduceMotion) snap() else KararTokens.Motion.effects(stiffness = stiffness, dampingRatio = dampingRatio)
