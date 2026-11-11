package com.karrar.amsakiah.core.jinan

object JinanNormalizer {

    private val sectionHeaderPattern = Regex(
        """(الفصل\s+(الأول|الثاني|الثالث|الرابع|الخامس|السادس|السابع|الثامن|التاسع|العاشر|الحادي|الثاني\s+عشر|الثالث\s+عشر|الرابع\s+عشر|الخامس\s+عشر|\d+))"""
            + """|(القسم\s+(الأول|الثاني|الثالث|الرابع|الخامس|\d+))"""
            + """|(المطلب\s+(الأول|الثاني|الثالث|\d+))"""
            + """|(الليلة\s+(الأولى|الثانية|الثالثة|الرابعة|الخامسة|السادسة|السابعة|الثامنة|التاسعة|العاشرة|\d+))"""
            + """|(اليوم\s+(الأول|الثاني|الثالث|الرابع|الخامس|\d+))"""
            + """|(الباب\s+(الأول|الثاني|الثالث|الرابع|الخامس|\d+))"""
            + """|^(أقول\s*:?)"""
            + """|^(تنبيه\s*:?)"""
            + """|^(واعلم\s*)"""
            + """|^(اعلم\s*)"""
            + """|^(فائدة\s*:?)"""
            + """|^(تتمة\s*:?)""",
        RegexOption.MULTILINE
    )

    private val arabicFootnotePattern = Regex(
        """[\(（]\d+[\)）]\s*""",
    )

    private val footnoteDividerPattern = Regex(
        """_{3,}""",
    )

    private val numberingPrefixPattern = Regex(
        """^[\d\u06F0-\u06F9]+[-\s.ـ—]+""",
    )

    fun normalize(text: String): String {
        if (text.isBlank()) return text

        var result = text

        result = ensureNewlineBeforeSectionHeaders(result)

        result = ensureNewlineBeforeArabicNumberedSections(result)

        result = ensureNewlineBeforeFootnoteDividers(result)

        result = ensureNewlineBeforePrefixNumberedItems(result)

        result = collapseExcessiveNewlines(result)

        return result.trim()
    }

    private fun ensureNewlineBeforeSectionHeaders(text: String): String {
        return sectionHeaderPattern.replace(text) { match ->
            val prefix = if (match.value.startsWith("أقول") ||
                match.value.startsWith("تنبيه") ||
                match.value.startsWith("واعلم") ||
                match.value.startsWith("اعلم") ||
                match.value.startsWith("فائدة") ||
                match.value.startsWith("تتمة")
            ) "\n\n" else "\n\n"
            "$prefix${match.value}"
        }
    }

    private fun ensureNewlineBeforeArabicNumberedSections(text: String): String {
        return text.replace(Regex("""([.،!؟])\s*(\d+\s*[-\s.ـ—]+\s*)""")) { match ->
            "${match.groupValues[1]}\n${match.groupValues[2]}"
        }
    }

    private fun ensureNewlineBeforeFootnoteDividers(text: String): String {
        return text.replace(footnoteDividerPattern) { "\n${it.value}" }
    }

    private fun ensureNewlineBeforePrefixNumberedItems(text: String): String {
        return text.replace(Regex("""([.،!؟])\s*(الأول\s*:|الثاني\s*:|الثالث\s*:|الرابع\s*:|الخامس\s*:|السادس\s*:|السابع\s*:|الثامن\s*:|التاسع\s*:|العاشر\s*:)""")) { match ->
            "${match.groupValues[1]}\n${match.groupValues[2]}"
        }
    }

    private fun collapseExcessiveNewlines(text: String): String {
        return text.replace(Regex("""\n{3,}"""), "\n\n")
    }

    private val persianDigits = mapOf(
        '\u06F0' to '0', '\u06F1' to '1', '\u06F2' to '2', '\u06F3' to '3',
        '\u06F4' to '4', '\u06F5' to '5', '\u06F6' to '6', '\u06F7' to '7',
        '\u06F8' to '8', '\u06F9' to '9'
    )

    fun toAsciiDigits(text: String): String {
        return text.map { persianDigits[it] ?: it }.joinToString("")
    }

    fun splitIntoParagraphs(text: String): List<String> {
        val normalized = normalize(text)
        return normalized.split(Regex("""\n\n+""")).map { it.trim() }.filter { it.isNotBlank() }
    }
}
