package com.karrar.amsakiah.core.data

/** ذكر من أذكار المسبحة مع هدفه واسم مجموعته. */
data class TasbihDhikr(
    val text: String,
    val target: Int,
    val group: String,
    val hint: String = ""
)

/**
 * أذكار المسبحة — مستوحاة من «مسبحة.html»:
 * تسبيح فاطمة الزهراء (ع) بثلاث خطوات متتابعة، ثم مأثورات مفردة.
 */
object TasbihPresets {

    const val ZAHRA_GROUP = "تسبيح فاطمة الزهراء (ع)"

    val all: List<TasbihDhikr> = listOf(
        TasbihDhikr("سبحان الله", 33, ZAHRA_GROUP, "33 مرة"),
        TasbihDhikr("الحمد لله", 33, ZAHRA_GROUP, "33 مرة"),
        TasbihDhikr("الله أكبر", 34, ZAHRA_GROUP, "34 مرة"),
        TasbihDhikr("لا إله إلا الله", 100, "التوحيد", "100 مرة"),
        TasbihDhikr("اللهم صل على محمد وآل محمد", 100, "الصلاة على النبي وآله", "100 مرة"),
        TasbihDhikr("أستغفر الله", 100, "الاستغفار", "100 مرة"),
        TasbihDhikr("لا حول ولا قوة إلا بالله", 100, "الحوقلة", "100 مرة")
    )

    fun safeIndex(index: Int): Int =
        if (index in all.indices) index else 0

    /**
     * الذكر التالي بعد إتمام الهدف:
     * - داخل مجموعة تسبيح الزهراء ينتقل بالترتيب ثم يعود للبداية.
     * - الأذكار المفردة تبقى كما هي.
     */
    fun next(index: Int, autoAdvance: Boolean): Pair<Int, Int> {
        val currentIndex = safeIndex(index)
        val current = all[currentIndex]
        if (!autoAdvance) return currentIndex to current.target
        val groupIndices = all.indices.filter { all[it].group == current.group }
        if (groupIndices.size <= 1) return currentIndex to current.target
        val position = groupIndices.indexOf(currentIndex).coerceAtLeast(0)
        val nextIndex = groupIndices[(position + 1) % groupIndices.size]
        return nextIndex to all[nextIndex].target
    }
}
