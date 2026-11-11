package com.karrar.amsakiah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.jinan.JinanContentTexts
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararFonts

/**
 * القارئ الموحّد — لأي محتوى قابل للقراءة: دعاء/زيارة/مفاتيح الجنان/قرآن.
 *
 * التصميم:
 * - الشريط العائم: زر رجوع فقط + عنوان + زر ⚙ خفيفة وقائمة الخيارات.
 * - بمساحة كاملة edge-to-edge بلا حواف لونية ولا تابلت محيط.
 * - الخط Amiri والقراءة حيّة.
 */
@Composable
fun UnifiedReaderScreen(
    contentId: Int,
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onTitleChange: (String?) -> Unit = {}
) {
    LaunchedEffect(title) { onTitleChange(title) }

    var fontSize by remember { mutableFloatStateOf(20f) }
    var showMenu by remember { mutableStateOf(false) }
    var readingBackground by remember { mutableStateOf("paper") }

    val body = listOf(
        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
        subtitle.ifBlank { "" },
        "",
        "────────────────",
        JinanContentTexts.getProcessed(contentId).ifBlank { "" },
        "────────────────",
        ""
    ).joinToString("\n")

    val isPaper = readingBackground == "paper"
    val isDark = readingBackground == "dark"
    val backColor = when {
        isDark -> androidx.compose.ui.graphics.Color(0xFF121212)
        else -> androidx.compose.ui.graphics.Color(0xFFFAF9F4)  // ورقية
    }

    Box(modifier = Modifier.background(backColor)) {
        // محتوى القراءة المتاح للتمرير Spot
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 18.dp, end = 18.dp, top = 56.dp, bottom = 120.dp)
        ) {
            Text(
                text = title,
                color = if (isDark || !isPaper) KararColors.TextPrimary else KararColors.GoldDeep,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))
            val amiri = KararFonts.Amiri
            Text(
                text = body,
                color = if (isDark || !isPaper) KararColors.TextPrimary else androidx.compose.ui.graphics.Color(0xFF2E2010),
                fontFamily = amiri,
                fontSize = fontSize.sp,
                lineHeight = (fontSize * 1.75f).sp,
                textAlign = TextAlign.Right
            )
            Spacer(Modifier.height(80.dp))
        }

        // الشريط العائم: زر رجوع فقط + عنوان + قائمة خيارات — لا شيء آخر
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 6.dp, start = 8.dp, end = 8.dp)
                .height(42.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    if (isDark || !isPaper) androidx.compose.ui.graphics.Color(0xFF1F1F1F)
                    else androidx.compose.ui.graphics.Color(0xFFFAF9F4).copy(alpha = 0.90f)
                )
                .border(
                    1.dp,
                    if (isDark || !isPaper) androidx.compose.ui.graphics.Color(0x33FFFFFF)
                    else androidx.compose.ui.graphics.Color(0x14000000),
                    RoundedCornerShape(50)
                )
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // زر الرجوع داخل دائرة زجاجية ٢٤dp
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDark || !isPaper) androidx.compose.ui.graphics.Color(0xFF2B2B2B)
                        else androidx.compose.ui.graphics.Color(0xFFE8E2D2)
                    )
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "إغلاق",
                    tint = if (isDark || !isPaper) KararColors.TextPrimary else KararColors.GoldDeep,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = title,
                color = if (isDark || !isPaper) KararColors.TextPrimary else KararColors.GoldDeep,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            // زر الخيارات (ثلاث نقاط) — ⚙ أيضاً: إعدادات القراءة
            Box {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(
                        imageVector = if (showMenu) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = "خيارات",
                        tint = if (isDark || !isPaper) KararColors.TextPrimary else KararColors.GoldDeep
                    )
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("تكبير النص") },
                        onClick = { fontSize = (fontSize + 2f).coerceAtMost(36f) },
                        leadingIcon = { Icon(Icons.Filled.Add, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("تصغير النص") },
                        onClick = { fontSize = (fontSize - 2f).coerceAtLeast(14f) },
                        leadingIcon = { Icon(Icons.Filled.Remove, null) }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isPaper) "خلفية داكنة" else "خلفية ورقية") },
                        onClick = { readingBackground = if (isPaper) "dark" else "paper" }
                    )
                }
            }
        }
    }
}
