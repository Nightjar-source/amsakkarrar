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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.state.AppState
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.components.SectionTitle
import com.karrar.amsakiah.ui.theme.KararColors
import kotlinx.datetime.LocalDate

/**
 * أعمال اليوم — تعهد المفتي المحبب: مهام يومية بأرقام مسلمة + ملاحظات.
 *
 * البيانات تُحفظ في `AppSettings` نفسه (`todayTasksDone`/`todayTasksDate`/`notes`)
 * فيتزامنها كل التبويبات والودجتات بدون إعدادات إضافية، وتُصفَّر المهام تلقائيًا
 * عند تغيّر التاريخ.
 */
@Composable
fun TasksScreen(state: AppState, contentPadding: PaddingValues) {
    val settings = state.settings
    val today = PrayerCalculator.todayDate(settings)
    val todayKey = "${today.year}-${two(today.monthNumber)}-${two(today.dayOfMonth)}"
    val done = remember(settings.todayTasksDone, settings.todayTasksDate) {
        if (settings.todayTasksDate == todayKey) settings.todayTasksDone.toSet() else emptySet()
    }
    var noteText by rememberSaveable { mutableStateOf("") }

    // المهام تُصفَّر تلقائيًا مع تغيّر اليوم — داخل LaunchedEffect حتى لا
    // يُكتب على القرص في كل فحص تغيّر للإعدادات.
    androidx.compose.runtime.LaunchedEffect(todayKey) {
        if (settings.todayTasksDate != todayKey) {
            state.update { it.copy(todayTasksDate = todayKey, todayTasksDone = emptyList()) }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "أعمال اليوم",
                        color = KararColors.TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "تعهدنا قبل النوم — مهام وعدّ سريع والملاحظات بين إيديك",
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

        item { SectionTitle("تعهد اليوم") }
        items(DEFAULT_TASKS.size) { index ->
            val task = DEFAULT_TASKS[index]
            val checked = task.id in done
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(KararColors.CardSoft)
                    .border(1.dp, KararColors.Border, RoundedCornerShape(16.dp))
                    .clickable {
                        state.update { s ->
                            val day = if (s.todayTasksDate == todayKey) s.todayTasksDone else emptyList()
                            val next = if (checked) day - task.id else (day + task.id).distinct()
                            s.copy(todayTasksDate = todayKey, todayTasksDone = next)
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (checked) KararColors.Mint.copy(alpha = 0.18f) else KararColors.SurfaceHigh)
                        .border(1.dp, if (checked) KararColors.Mint else KararColors.Border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (checked) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            tint = KararColors.Mint,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Text(
                    task.label,
                    color = if (checked) KararColors.TextMuted else KararColors.TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

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

private fun two(v: Int): String = v.toString().padStart(2, '0')

private data class DefaultTask(val id: String, val label: String)

private val DEFAULT_TASKS = listOf(
    DefaultTask("salah", "الصلاة في وقتها مع الجماعة"),
    DefaultTask("quran", "قراءة القرآن الكريم (ولو صفحة)"),
    DefaultTask("dhikr_am", "أذكار الصباح"),
    DefaultTask("dhikr_pm", "أذكار المساء"),
    DefaultTask("salawat", "الصلاة على محمد وآل محمد ١٠٠ مرة"),
    DefaultTask("duas", "دعاء مفضل أو تعقيب الصلاة")
)
