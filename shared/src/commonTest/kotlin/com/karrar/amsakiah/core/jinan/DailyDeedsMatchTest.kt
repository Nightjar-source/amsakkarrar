package com.karrar.amsakiah.core.jinan

import com.karrar.amsakiah.core.astronomy.HijriDate
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * اختبار عبور «أعمال اليوم ↔ مفاتيح الجنان»: كل عمل موقوتٍ في الجنان يظهر في يومه
 * الهجري فقط ولا يتسرب إلى غيره. جميع العناوين المستخدمة هنا عناوين حقيقية من
 * ملف «مفاتيح الجنان» المدمج في التطبيق.
 */
class DailyDeedsMatchTest {

    private fun hijri(month: Int, day: Int) =
        HijriDate(day = day, monthName = "", year = 1448, monthNum = month)

    // أي يوم أربعاء عشوائي — اليوم غير معتمد في المطابقة الهجرية، ولا المطابقة يومية الأسبوع.
    private val anyDate = LocalDate(2026, 11, 11)

    private fun titlesOn(month: Int, day: Int) =
        dailyDeeds(anyDate, hijri(month, day)).map { it.item.title }

    @Test
    fun `الليلة الأولى واليوم الأول من رجب يظهران فقط في 1 رجب`() {
        val first = titlesOn(7, 1)
        assertTrue(first.any { it.contains("اليوم الأول من رجب") }, "غاب «اليوم الأول من رجب» في يومه")
        assertTrue(first.any { it.contains("الليلة الأولى من رجب") }, "غاب «الليلة الأولى من رجب» في يومه")

        val thirteenth = titlesOn(7, 13)
        assertFalse(thirteenth.contains("اليوم الأول من رجب"), "«اليوم الأول من رجب» تسرب إلى 13 رجب")
        assertFalse(thirteenth.contains("الليلة الأولى من رجب"), "«الليلة الأولى من رجب» تسرب إلى 13 رجب")
    }

    @Test
    fun `الثالث عشر والنصف من رجب بالضبط في أيامهما`() {
        val thirteenth = titlesOn(7, 13)
        assertTrue(thirteenth.any { it.contains("الليلة الثالثة عشرة من رجب") })
        assertTrue(thirteenth.any { it.contains("اليوم الثالث عشر من رجب") })

        val half = titlesOn(7, 15)
        assertTrue(half.any { it.contains("ليلة النصف من رجب") })
        assertTrue(half.any { it.contains("يوم النصف من رجب") })
    }

    @Test
    fun `السّابع والعشرون من رجب — المبعث — يظهر فقط في 27 رجب`() {
        val twentySeventh = titlesOn(7, 27)
        assertTrue(twentySeventh.any { it.contains("الليلة السابعة والعشرون من رجب") })
        assertTrue(twentySeventh.any { it.contains("اليوم السابع والعشرون من رجب") })

        val twentySixth = titlesOn(7, 26)
        assertFalse(twentySixth.any { it.contains("المبعث") }, "«المبعث» تسرب إلى 26 رجب")
    }

    @Test
    fun `الليلة الخامسة عشرة بلا اسم شهر تظهر في كل الأشهر`() {
        // العنوان حقيقي من مفاتيح الجنان بلا اسم شهر — يجب أن يظهر في كل 15 من كل شهر.
        (1..12).forEach { month ->
            assertTrue(
                titlesOn(month, 15).any { it.contains("الليلة الخامسة عشرة") },
                "غاب «الليلة الخامسة عشرة» في $month/15"
            )
        }
    }

    @Test
    fun `أعمال رمضان لا تظهر في غير رمضان ولا موقعًا آخر`() {
        (1..12).filter { it != 9 }.forEach { month ->
            assertFalse(
                titlesOn(month, 15).any { it.contains("رمضان") },
                "ظهر «شهر رمضان» خارج رمضان في $month/15"
            )
        }
        val ramadanThirteenth = titlesOn(9, 13)
        assertTrue(
            ramadanThirteenth.any { it.contains("الثالثة عشرة من شهر رمضان") },
            "غاب عمل رمضان الموقوت في 13 رمضان"
        )
    }

    @Test
    fun `يوم عيد الفطر يظهر في 1 شوال فقط`() {
        val eid = titlesOn(10, 1)
        assertTrue(eid.any { it.contains("عيد الفطر") }, "غاب «اليوم الأول (يوم عيد الفطر)» في 1 شوال")
        assertFalse(titlesOn(10, 2).any { it.contains("عيد الفطر") }, "«عيد الفطر» تسرب إلى 2 شوال")
    }

    @Test
    fun `الغدير يظهر في 18 ذو الحجة فقط`() {
        assertTrue(titlesOn(12, 18).any { it.contains("الغدير") }, "غاب الغدير في 18 ذو الحجة")
        assertFalse(titlesOn(12, 17).any { it.contains("الغدير") }, "الغدير تسرب إلى 17 ذو الحجة")
        assertFalse(titlesOn(11, 18).any { it.contains("الغدير") }, "الغدير تسرب إلى 18 ذي القعدة")
    }

    @Test
    fun `اليوم الأخير من الشهر يظهر في نهاية الشهر لا في بدايته`() {
        // العنوان حقيقي: «اليوم الأخير من شهر رجب» — يطابق نهائي الشهر سواء كان 29 أو 30 يومًا.
        assertTrue(titlesOn(7, 30).any { it.contains("اليوم الأخير من شهر رجب") })
        assertTrue(titlesOn(7, 29).any { it.contains("اليوم الأخير من شهر رجب") })
        assertFalse(titlesOn(7, 5).any { it.contains("اليوم الأخير من شهر رجب") })
    }

    @Test
    fun `اليوم بلا شهر لا يمكن أن يطابق أعمالًا باسم شهر آخر`() {
        // «اليوم الأول من شهر ذي الحجة» — 12/1 فقط، ولو كنا في 1 من أي شهر آخر.
        assertTrue(titlesOn(12, 1).any { it.contains("ذي الحجة") })
        (1..11).forEach { month ->
            assertFalse(titlesOn(month, 1).any { it.contains("ذي الحجة") }, "ذي الحجة تسرب إلى $month/1")
        }
    }
}
