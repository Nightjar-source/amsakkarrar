package com.karrar.amsakiah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.jinan.JinanContentData
import com.karrar.amsakiah.core.jinan.JinanItem
import com.karrar.amsakiah.core.state.AppState
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.components.SectionTitle
import com.karrar.amsakiah.ui.theme.KararColors

/**
 * المهام والملاحظات — تبدأ فارغة (بلا أعمال جاهزة):
 *
 * 1) «محفوظاتي من «مفاتيح الجنان»» — كل نص نجّمه المستخدم ⭐ في القارئ يظهر هنا،
 *    مع اسم القسم، ويُفتح بالضغط عليه داخل القارئ نفسه.
 * 2) «ملاحظاتي» — ملاحظات حرة سريعة محلية بحتا.
 *
 * حذف من قائمة المهام المتراكمة سابقًا (اللي كانت 710 بواً مهمل) ليكون للمستخدم
 * الانطلاقة النظيفة: لا شيء مسبق، فقط ما اختاره بنفسه.
 */
@Composable
fun TasksScreen(
    state: AppState,
    contentPadding: PaddingValues,
    onOpenJinanReader: (JinanItem) -> Unit
) {
    val settings = state.settings
    var noteText by rememberSaveable { mutableStateOf("") }

    // محفوظاتي من «مفاتيح الجنان» — نجّم القارئ ⇒ تظهر هنا فورًا.
    val favorites = settings.favoriteJinanItems.mapNotNull { id -> JinanContentData.findItemById(id) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "المهام والملاحظات",
                        color = KararColors.TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "ملاحظاتك ومفضلاتك من «مفاتيح الجنان» — بلا شيء مسبق",
                        color = KararColors.TextMuted,
                        fontSize = 11.sp
                    )
                }
                Icon(
                    Icons.Filled.EventNote,
                    contentDescription = null,
                    tint = KararColors.Gold.copy(alpha = 0.9f),
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // ---------- المجموعة الأولى: محفوظاتي من «مفاتيح الجنان» ----------
        item { SectionTitle("⭐ محفوظاتي من «مفاتيح الجنان» (${favorites.size})") }
        if (favorites.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    corner = 18.dp,
                    color = KararColors.CardSoft,
                    borderColor = KararColors.Border,
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(KararColors.CardSoft)
                                .border(1.dp, KararColors.Border, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = KararColors.Gold.copy(alpha = 0.9f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "لا توجد أعمال محفوظة بعد",
                            color = KararColors.TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "اضبط ⭐ داخل قارئ «مفاتيح الجنان» أو القارئ الموحّد، ثم شاهدها هنا.",
                            color = KararColors.TextMuted,
                            fontSize = 11.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
        items(favorites, key = { "fav-${it.id}" }) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(KararColors.CardSoft)
                    .border(1.dp, KararColors.Border, RoundedCornerShape(16.dp))
                    .clickable { onOpenJinanReader(item) }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(KararColors.Purple.copy(alpha = 0.14f))
                        .border(1.dp, KararColors.Purple.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = KararColors.Purple,
                        modifier = Modifier.size(19.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    item.title,
                    color = KararColors.TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                // نجمة مزالة — إزالة من المفضلة
                Icon(
                    Icons.Filled.Star,
                    contentDescription = "إزالة من المحفوظات",
                    tint = KararColors.Gold,
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .clickable {
                            state.update { s -> s.copy(favoriteJinanItems = s.favoriteJinanItems - item.id) }
                        }
                )
            }
        }

        // ---------- المجموعة الثانية: ملاحظاتي ----------
        item { SectionTitle("ملاحظاتي") }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("اكتب ملاحظة سريعة…", fontSize = 12.sp) },
                    shape = RoundedCornerShape(14.dp),
                    minLines = 1,
                    maxLines = 3,
                    singleLine = false
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(KararColors.Gold)
                        .clickable {
                            val text = noteText.trim()
                            if (text.isNotEmpty()) {
                                state.update { s -> s.copy(notes = listOf(text) + s.notes) }
                                noteText = ""
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = KararColors.OnGold, fontSize = 22.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        items(settings.notes.size) { index ->
            val note = settings.notes[index]
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                corner = 16.dp,
                color = KararColors.CardSoft,
                borderColor = KararColors.Border,
                contentPadding = PaddingValues(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        note,
                        color = KararColors.TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "حذف الملاحظة",
                        tint = KararColors.TextMuted,
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .clickable {
                                state.update { s ->
                                    s.copy(notes = s.notes.toMutableList().apply { removeAt(index) })
                                }
                            }
                            .padding(2.dp)
                    )
                }
            }
        }
    }
}
