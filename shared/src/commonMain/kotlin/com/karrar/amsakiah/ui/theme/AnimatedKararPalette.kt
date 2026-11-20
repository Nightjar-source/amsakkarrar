package com.karrar.amsakiah.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

/**
 * نسخة «سائلة» من اللوحة — كل لون يتلمّس بحركة تلاشي ناعمة عند تبديل الوضع
 * (مظلم ↔ فاتح ↔ تلقائي)، فتختفي الطفرة المفاجئة من كل نقطة قراءة في التطبيق.
 *
 * الفكرة: بدل تبديل `LocalKararPalette` لحظيًا (فتقفز كل الألوان دفعة واحدة)،
 * تُقرأ الألوان الـ50 على حالاتها المُحررة — وكل `@Composable` يقرأ اللوحة
 * يرى الانتقال على مدى مدة الـeffects نفسها دون أي إعادة تركيب إضافية.
 * هذا هو الفرق بين «API تبديل مراوغ» وبارتقاء Fluid حقيقي.
 */
@Composable
fun animateKararPalette(target: KararPalette): KararPalette {
    val spec = KararTokens.Motion.effects<Color>()
    @Composable
    fun c(color: Color, label: String): Color = animateColorAsState(color, spec, label = label).value

    return target.copy(
        backgroundTop = c(target.backgroundTop, "pk-bg-top"),
        backgroundBottom = c(target.backgroundBottom, "pk-bg-bottom"),
        tealGlow = c(target.tealGlow, "pk-teal-glow"),
        indigoGlow = c(target.indigoGlow, "pk-indigo-glow"),
        surface = c(target.surface, "pk-surface"),
        surfaceSoft = c(target.surfaceSoft, "pk-surface-soft"),
        surfaceHigh = c(target.surfaceHigh, "pk-surface-high"),
        glass = c(target.glass, "pk-glass"),
        glassSoft = c(target.glassSoft, "pk-glass-soft"),
        glassDeep = c(target.glassDeep, "pk-glass-deep"),
        border = c(target.border, "pk-border"),
        borderStrong = c(target.borderStrong, "pk-border-strong"),
        highlight = c(target.highlight, "pk-highlight"),
        highlightStrong = c(target.highlightStrong, "pk-highlight-strong"),
        scrim = c(target.scrim, "pk-scrim"),
        shadow = c(target.shadow, "pk-shadow"),
        textPrimary = c(target.textPrimary, "pk-text-primary"),
        textSecondary = c(target.textSecondary, "pk-text-secondary"),
        textMuted = c(target.textMuted, "pk-text-muted"),
        gold = c(target.gold, "pk-gold"),
        goldDeep = c(target.goldDeep, "pk-gold-deep"),
        goldSoft = c(target.goldSoft, "pk-gold-soft"),
        onGold = c(target.onGold, "pk-on-gold"),
        purple = c(target.purple, "pk-purple"),
        cyan = c(target.cyan, "pk-cyan"),
        pink = c(target.pink, "pk-pink"),
        teal = c(target.teal, "pk-teal"),
        sky = c(target.sky, "pk-sky"),
        rose = c(target.rose, "pk-rose"),
        violet = c(target.violet, "pk-violet"),
        mint = c(target.mint, "pk-mint"),
        amber = c(target.amber, "pk-amber"),
        success = c(target.success, "pk-success"),
        danger = c(target.danger, "pk-danger"),
        skyTop = c(target.skyTop, "pk-sky-top"),
        skyBottom = c(target.skyBottom, "pk-sky-bottom"),
        skyText = c(target.skyText, "pk-sky-text")
    )
}
