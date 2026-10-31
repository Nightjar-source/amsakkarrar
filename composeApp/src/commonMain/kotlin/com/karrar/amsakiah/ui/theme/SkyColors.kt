package com.karrar.amsakiah.ui.theme

import androidx.compose.ui.graphics.Color

data class SkyGradientPoint(
    val tSeconds: Float,
    val top: Color,
    val bottom: Color,
    val glow: Color
)

data class CelestialStar(
    val xPercent: Float,
    val yPercent: Float,
    val radius: Float,
    val delaySec: Float,
    val maxAlpha: Float
)

object SkyTheme {

    val skyGradients = listOf(
        SkyGradientPoint(0f, Color(8, 10, 25), Color(15, 15, 40), Color(70, 100, 255, 76)),
        SkyGradientPoint(4.5f * 3600f, Color(15, 12, 35), Color(40, 25, 60), Color(100, 80, 200, 102)),
        SkyGradientPoint(5.5f * 3600f, Color(50, 25, 70), Color(240, 90, 40), Color(255, 120, 40, 178)),
        SkyGradientPoint(6.5f * 3600f, Color(60, 120, 180), Color(250, 190, 120), Color(255, 220, 120, 153)),
        SkyGradientPoint(12f * 3600f, Color(25, 130, 220), Color(110, 200, 255), Color(255, 255, 255, 102)),
        SkyGradientPoint(17f * 3600f, Color(45, 90, 160), Color(220, 150, 110), Color(255, 190, 90, 128)),
        SkyGradientPoint(18.5f * 3600f, Color(40, 25, 70), Color(220, 50, 30), Color(255, 70, 20, 204)),
        SkyGradientPoint(19.5f * 3600f, Color(18, 15, 40), Color(35, 20, 55), Color(90, 50, 150, 128)),
        SkyGradientPoint(24f * 3600f, Color(8, 10, 25), Color(15, 15, 40), Color(70, 100, 255, 76))
    )

    val celestialStars = listOf(
        CelestialStar(7f, 12f, 1.3f, 0.2f, 0.9f),
        CelestialStar(16f, 24f, 1.8f, 1.1f, 1.0f),
        CelestialStar(25f, 9f, 1.1f, 2.3f, 0.7f),
        CelestialStar(34f, 18f, 1.5f, 0.7f, 0.85f),
        CelestialStar(44f, 7f, 2.0f, 1.7f, 0.95f),
        CelestialStar(56f, 11f, 1.2f, 0.4f, 0.8f),
        CelestialStar(67f, 22f, 1.7f, 1.9f, 0.9f),
        CelestialStar(77f, 10f, 1.4f, 2.6f, 0.75f),
        CelestialStar(88f, 16f, 1.9f, 0.9f, 0.95f),
        CelestialStar(93f, 29f, 1.2f, 1.4f, 0.8f),
        CelestialStar(12f, 38f, 1.4f, 2.1f, 0.7f),
        CelestialStar(29f, 33f, 1.1f, 0.5f, 0.65f),
        CelestialStar(73f, 36f, 1.5f, 1.3f, 0.8f),
        CelestialStar(84f, 41f, 1.1f, 2.0f, 0.7f)
    )

    fun lerpColor(c1: Color, c2: Color, fraction: Float): Color {
        val f = fraction.coerceIn(0f, 1f)
        return Color(
            red = c1.red + f * (c2.red - c1.red),
            green = c1.green + f * (c2.green - c1.green),
            blue = c1.blue + f * (c2.blue - c1.blue),
            alpha = c1.alpha + f * (c2.alpha - c1.alpha)
        )
    }

    fun getSkyColorsForSecond(currentSeconds: Float): Triple<Color, Color, Color> {
        val s = (currentSeconds % 86400f + 86400f) % 86400f
        for (i in 0 until skyGradients.size - 1) {
            val curr = skyGradients[i]
            val next = skyGradients[i + 1]
            if (s >= curr.tSeconds && s <= next.tSeconds) {
                val span = next.tSeconds - curr.tSeconds
                val fraction = if (span == 0f) 0f else (s - curr.tSeconds) / span
                return Triple(
                    lerpColor(curr.top, next.top, fraction),
                    lerpColor(curr.bottom, next.bottom, fraction),
                    lerpColor(curr.glow, next.glow, fraction)
                )
            }
        }
        return Triple(skyGradients[0].top, skyGradients[0].bottom, skyGradients[0].glow)
    }

    fun getKashidaMonthParts(monthName: String?): Pair<String, String> {
        val clean = (monthName ?: "رمضان").replace(Regex("[\u064B-\u065F\u0651\u0654]"), "")
        return when {
            clean.contains("محرم") -> Pair("محـ", "ـرم")
            clean.contains("صفر") -> Pair("صـ", "ـفر")
            clean.contains("ربيع الأول") -> Pair("ربيـ", "ـع الأول")
            clean.contains("ربيع الآخر") || clean.contains("ربيع الثاني") -> Pair("ربيـ", "ـع الآخر")
            clean.contains("جمادى الأولى") -> Pair("جمـ", "ـادى الأولى")
            clean.contains("جمادى الآخرة") || clean.contains("جمادى الثانية") -> Pair("جمـ", "ـادى الآخرة")
            clean.contains("رجب") -> Pair("رجـ", "ـب")
            clean.contains("شعبان") -> Pair("شعبـ", "ـان")
            clean.contains("رمضان") -> Pair("رمضـ", "ـان")
            clean.contains("شوال") -> Pair("شـ", "ـوال")
            clean.contains("القعدة") -> Pair(clean.replace("القعدة", "القعـ"), "ـدة")
            clean.contains("الحجة") -> Pair(clean.replace("الحجة", "الحجـ"), "ـة")
            clean.length > 2 -> Pair(clean.dropLast(1) + "ـ", "ـ" + clean.takeLast(1))
            else -> Pair(clean, "")
        }
    }
}
