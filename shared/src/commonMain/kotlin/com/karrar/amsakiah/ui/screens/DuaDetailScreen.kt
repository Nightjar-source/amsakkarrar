package com.karrar.amsakiah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.data.ShiaDuaItem
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.player.DuaPlayerController
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararTokens

/**
 * شاشة دعاء مستقلة — تُفتح لكل دعاء عبر معرّفه (id).
 *
 * تتضمن تنسيقًا مريحًا للقراءة، ومشغّل صوت أوفلاين للأدعية التي لها تسجيل
 * (المناجاة المنظومة أولًا)، مع دعم الرجوع النظامي والإيماءات التنبؤية.
 */
@Composable
fun DuaDetailScreen(
    item: ShiaDuaItem,
    contentPadding: PaddingValues,
    player: DuaPlayerController,
    onBack: () -> Unit
) {
    // عمود قراءة مُوسّط بعرض مريح على التابلت/الحاسوب بدل تمدّد النص لعرض النافذة.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding()
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .widthIn(max = KararTokens.Size.readingMaxWidth)
                .fillMaxHeight(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 4.dp,
                bottom = 28.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                DuaHeaderCard(item = item, player = player)
            }

            item {
                DuaTextCard(item)
            }
        }
    }
}

@Composable
private fun DuaHeaderCard(item: ShiaDuaItem, player: DuaPlayerController) {
    val isCurrent = player.currentId == item.id
    val isPlaying = isCurrent && player.playing

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        corner = 22.dp,
        contentPadding = PaddingValues(16.dp)
    ) {
        Text(item.title, color = KararColors.Gold, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        if (item.subtitle.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(item.subtitle, color = KararColors.TextSecondary, fontSize = 12.sp, lineHeight = 20.sp)
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DuaPill(item.category, KararColors.Purple)
            DuaPill(item.recommendedTime, KararColors.TextMuted)
        }

        if (item.hasAudio) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            Brush.linearGradient(listOf(KararColors.Gold, KararColors.Pink)),
                            CircleShape
                        )
                        .clickable { player.play(item) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                        tint = KararColors.OnGold,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = if (isPlaying) {
                            "يعمل الآن — المشغّل أسفل الشاشة"
                        } else if (isCurrent) {
                            "متوقف مؤقتًا — اضغط للمتابعة"
                        } else {
                            item.audioTitle ?: "تشغيل التسجيل الصوتي"
                        },
                        color = if (isCurrent) KararColors.Gold else KararColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                    Text(
                        "تسجيل صوتي أوفلاين — بدون إنترنت",
                        color = KararColors.TextMuted,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DuaPill(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontSize = 10.sp,
        modifier = Modifier
            .background(Color(0x14FFFFFF), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
private fun DuaTextCard(item: ShiaDuaItem) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        corner = 24.dp,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp)
    ) {
        val paragraphs = remember(item.id) {
            item.arabicText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        }
        paragraphs.forEachIndexed { index, line ->
            val isPoetry = line.contains("***")
            Text(
                text = if (isPoetry) line.replace("***", " ۞ ") else line,
                color = if (index == 0) KararColors.Gold else KararColors.TextPrimary,
                fontSize = if (isPoetry) 16.sp else 17.sp,
                lineHeight = if (isPoetry) 30.sp else 32.sp,
                fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                textAlign = if (isPoetry) TextAlign.Center else TextAlign.Justify,
                modifier = Modifier.fillMaxWidth()
            )
            if (index != paragraphs.lastIndex) Spacer(Modifier.height(if (isPoetry) 10.dp else 12.dp))
        }
    }
}
