package com.karrar.amsakiah.core.jinan

import kotlinx.serialization.Serializable

@Serializable
data class JinanPart(
    val id: Int,
    val title: String,
    val chapters: List<JinanChapter>
)

@Serializable
data class JinanChapter(
    val id: Int,
    val title: String,
    val sections: List<JinanSection>
)

@Serializable
data class JinanSection(
    val id: Int,
    val title: String,
    val items: List<JinanItem>
)

@Serializable
data class JinanItem(
    val id: Int,
    val title: String,
    val arabicText: String = "",
    val translation: String? = null,
    val sourceFile: String? = null
)
