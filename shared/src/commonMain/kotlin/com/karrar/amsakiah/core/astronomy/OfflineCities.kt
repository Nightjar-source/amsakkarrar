package com.karrar.amsakiah.core.astronomy

object OfflineCities {

    val cities = listOf(
        City("العراق", "كربلاء", 32.6160, 44.0249, 3.0),
        City("العراق", "النجف", 32.0286, 44.3436, 3.0),
        City("العراق", "بغداد", 33.3152, 44.3661, 3.0),
        City("العراق", "البصرة", 30.5081, 47.7835, 3.0),
        City("العراق", "الموصل", 36.3350, 43.1189, 3.0),
        City("السعودية", "مكة المكرمة", 21.4225, 39.8262, 3.0),
        City("السعودية", "المدينة المنورة", 24.4672, 39.6112, 3.0),
        City("السعودية", "الرياض", 24.7136, 46.6753, 3.0),
        City("إيران", "طهران", 35.6892, 51.3890, 3.5),
        City("إيران", "قم", 34.6399, 50.8759, 3.5),
        City("إيران", "مشهد", 36.2605, 59.6168, 3.5),
        City("فرنسا", "باريس", 48.8566, 2.3522, 2.0),
        City("بريطانيا", "لندن", 51.5074, -0.1278, 1.0),
        City("السويد", "ستوكهولم", 59.3293, 18.0686, 2.0),
        City("كوريا الجنوبية", "سيول", 37.5665, 126.9780, 9.0),
        City("كندا", "تورونتو", 43.6510, -79.3470, -4.0),
        City("أمريكا", "نيويورك", 40.7128, -74.0060, -4.0)
    )

    val methodsMatrix = listOf(
        CalculationMethod(0, "leufa", "معهد ليفا - قم المقدسة (ID:0)", 16.0, 0, 4.0, 0, 14.0, true),
        CalculationMethod(1, "karachi", "جامعة العلوم الإسلامية - كراتشي (ID:1)", 18.0, 1, 0.0, 0, 18.0, false),
        CalculationMethod(2, "isna", "الجمعية الإسلامية لأمريكا الشمالية ISNA (ID:2)", 15.0, 1, 0.0, 0, 15.0, false),
        CalculationMethod(3, "mwl", "رابطة العالم الإسلامي MWL (ID:3)", 18.0, 1, 0.0, 0, 17.0, false),
        CalculationMethod(4, "ummalqura", "تقويم أم القرى - مكة المكرمة (ID:4)", 18.5, 1, 0.0, 1, 90.0, false),
        CalculationMethod(5, "egypt", "الهيئة المصرية العامة للمساحة (ID:5)", 19.5, 1, 0.0, 0, 17.5, false),
        CalculationMethod(6, "tehran", "معهد الجيوفيزياء - جامعة طهران (ID:6)", 17.7, 0, 4.5, 0, 14.0, true),
        CalculationMethod(7, "mwl_custom", "المعيار القياسي الموحد (ID:7)", 18.0, 1, 0.0, 0, 17.0, false),
        CalculationMethod(8, "gulf_shia", "معيار زاوية 4.1° وعشاء 75 د (ID:8)", 18.0, 0, 4.1, 1, 75.0, true),
        CalculationMethod(9, "alkafeel", "معيار الكفيل - المرجعية العليا بالنجف (18° / 4°)", 18.0, 0, 4.0, 0, 14.0, true),
        CalculationMethod(10, "london_shia", "مؤسسة الإمام علي (ع) - لندن (15° / 4°)", 15.0, 0, 4.0, 0, 15.0, true),
        CalculationMethod(11, "france_12", "اتحاد المنظمات الإسلامية في فرنسا UOIF (12°)", 12.0, 1, 0.0, 0, 12.0, false),
        CalculationMethod(12, "european_council", "المجلس الأوروبي للإفتاء والبحوث (15°)", 15.0, 1, 0.0, 0, 15.0, false)
    )

    fun getDefaultCity(): City = cities[0]
    fun getDefaultMethod(): CalculationMethod = methodsMatrix[9]
}
