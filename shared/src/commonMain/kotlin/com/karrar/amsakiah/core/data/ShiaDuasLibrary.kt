package com.karrar.amsakiah.core.data

/**
 * عنصر دعاء في الفهرس.
 *
 * @property id معرّف ثابت يُستخدم للربط بين الفهرس وشاشة الدعاء
 * @property audioResource اسم ملف الصوت في `composeResources/files` (بدون امتداد) أو null
 */
data class ShiaDuaItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val recommendedTime: String,
    val arabicText: String,
    val audioResource: String? = null,
    val audioTitle: String? = null
) {
    val hasAudio: Boolean get() = audioResource != null
}

/**
 * فهرس الأدعية والزيارات — النصوص نفسها في [DuaTexts] مرتبطة بالمعرّف.
 *
 * المناجاة المنظومة أولًا (بصوتها الأوفلاين)، ثم دعاء كميل والتوسل
 * وبقية الأدعية المأثورة عن أهل البيت (عليهم السلام).
 */
object ShiaDuasLibrary {

    const val ID_MUNAJAT_MANZUMA = "munajat_manzuma"

    val duasList = listOf(
        ShiaDuaItem(
            id = ID_MUNAJAT_MANZUMA,
            title = "المناجاة المنظومة",
            subtitle = "لأمير المؤمنين علي بن أبي طالب (ع) — من الصحيفة العلوية",
            category = "مناجاة وأدعية الليالي",
            recommendedTime = "في الأسحار وليالي الجمعة",
            arabicText = DuaTexts.MUNAJAT_MANZUMA,
            audioResource = "munajat_manzuma",
            audioTitle = "المناجاة المنظومة — تلاوة كاملة"
        ),
        ShiaDuaItem(
            id = "kumayl",
            title = "دعاء كميل بن زياد",
            subtitle = "رواية كميل عن أمير المؤمنين (ع) — دعاء الخضر (ع)",
            category = "مناجاة وأدعية الليالي",
            recommendedTime = "ليلة الجمعة وليلة النصف من شعبان",
            arabicText = DuaTexts.KUMAYL
        ),
        ShiaDuaItem(
            id = "tawassul",
            title = "دعاء التوسل",
            subtitle = "التوسل بالنبي وآله (ص) — عن الإمام الصادق (ع)",
            category = "أدعية التوسل والشفاعة",
            recommendedTime = "عند الحاجة وفي كل وقت",
            arabicText = DuaTexts.TAWASSUL
        ),
        ShiaDuaItem(
            id = "faraj",
            title = "دعاء الفرج (إلهي عظم البلاء)",
            subtitle = "لإمامنا صاحب العصر والزمان الحجة بن الحسن (عجل الله فرجه)",
            category = "أدعية الإمام المهدي (عج)",
            recommendedTime = "في كل وقت وبعد الصلوات المفروضة",
            arabicText = DuaTexts.FARAJ
        ),
        ShiaDuaItem(
            id = "hujjah",
            title = "دعاء الحجة (اللهم كن لوليك)",
            subtitle = "دعاء سلامة الإمام المهدي المنتظر (عجل الله تعالى فرجه الشريف)",
            category = "أدعية الإمام المهدي (عج)",
            recommendedTime = "يعقب به دبر كل صلاة وفي ليالي القدر والجمعة",
            arabicText = DuaTexts.HUJJAH
        ),
        ShiaDuaItem(
            id = "sabah",
            title = "دعاء الصباح لأمير المؤمنين (ع)",
            subtitle = "مختارات من أدعية الإمام علي (عليه السلام) بعد صلاة الفجر",
            category = "تعقيبات الصباح والمساء",
            recommendedTime = "بعد صلاة الصبح وعند طلوع الفجر",
            arabicText = DuaTexts.SABAH
        ),
        ShiaDuaItem(
            id = "taqibat_common",
            title = "تعقيبات الصلوات اليومية المشتركة",
            subtitle = "ما يُقرأ عقيب كل فريضة كما ورد عن أئمة أهل البيت (ع)",
            category = "تعقيبات الصباح والمساء",
            recommendedTime = "عقيب كل صلاة مفروضة مع تسبيح الزهراء (ع)",
            arabicText = DuaTexts.TAQIBAT
        ),
        ShiaDuaItem(
            id = "ahd",
            title = "دعاء العهد الصباحي",
            subtitle = "روي عن الإمام الصادق (ع): من دعا أربعين صباحاً بهذا العهد كان من أنصار قائمنا",
            category = "أدعية الإمام المهدي (عج)",
            recommendedTime = "كل صباح بعد صلاة الفجر",
            arabicText = DuaTexts.AHD
        ),
        ShiaDuaItem(
            id = "ziyarat_aminallah",
            title = "زيارة أمين الله الجامعة",
            subtitle = "من أرقى الزيارات المأثورة عن الإمام زين العابدين (ع)",
            category = "الزيارات المشهورة",
            recommendedTime = "في جميع المشاهد المشرفة وفي كل وقت",
            arabicText = DuaTexts.ZIYARAT_AMIN_ALLAH
        )
    )

    fun byId(id: String?): ShiaDuaItem? = duasList.firstOrNull { it.id == id }
}
