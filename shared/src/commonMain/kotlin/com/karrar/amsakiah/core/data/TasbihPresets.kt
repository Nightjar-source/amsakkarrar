package com.karrar.amsakiah.core.data

import kotlinx.serialization.Serializable

/** ذكر من أذكار المسبحة مع هدفه واسم مجموعته. */
@Serializable
data class TasbihDhikr(
    val text: String,
    val target: Int,
    val group: String,
    val hint: String = "",
    /** معرّف الأذكار المضافة من المستخدم؛ فارغ للأذكار الجاهزة. */
    val id: String = ""
)

/**
 * أذكار المسبحة — مستوحاة من «مسبحة.html»:
 * تسبيح فاطمة الزهراء (ع) بثلاث خطوات متتابعة، ثم مأثورات مفردة،
 * ثم الأذكار التي يضيفها المستخدم بنفسه.
 */
object TasbihPresets {

    const val ZAHRA_GROUP = "تسبيح فاطمة الزهراء (ع)"
    const val CUSTOM_GROUP = "أذكاري"

    val all: List<TasbihDhikr> = listOf(
        TasbihDhikr("سبحان الله", 33, ZAHRA_GROUP, "33 مرة"),
        TasbihDhikr("الحمد لله", 33, ZAHRA_GROUP, "33 مرة"),
        TasbihDhikr("الله أكبر", 34, ZAHRA_GROUP, "34 مرة"),
        TasbihDhikr("لا إله إلا الله", 100, "التوحيد", "100 مرة"),
        TasbihDhikr("اللهم صل على محمد وآل محمد", 100, "الصلاة على النبي وآله", "100 مرة"),
        TasbihDhikr("أستغفر الله", 100, "الاستغفار", "100 مرة"),
        TasbihDhikr("لا حول ولا قوة إلا بالله", 100, "الحوقلة", "100 مرة")
    )

    /** كل الأذكار: الجاهزة ثم المضافة من المستخدم. */
    fun combined(custom: List<TasbihDhikr>): List<TasbihDhikr> = all + custom

    fun safeIndex(index: Int, size: Int = all.size): Int =
        if (index in 0 until size) index else 0

    /**
     * الذكر التالي بعد إتمام الهدف:
     * - داخل مجموعة تسبيح الزهراء ينتقل بالترتيب ثم يعود للبداية.
     * - الأذكار المفردة تبقى كما هي.
     */
    fun next(
        list: List<TasbihDhikr>,
        index: Int,
        autoAdvance: Boolean
    ): Pair<Int, Int> {
        val currentIndex = safeIndex(index, list.size)
        val current = list[currentIndex]
        if (!autoAdvance) return currentIndex to current.target
        val groupIndices = list.indices.filter { list[it].group == current.group }
        if (groupIndices.size <= 1) return currentIndex to current.target
        val position = groupIndices.indexOf(currentIndex).coerceAtLeast(0)
        val nextIndex = groupIndices[(position + 1) % groupIndices.size]
        return nextIndex to list[nextIndex].target
    }
}
