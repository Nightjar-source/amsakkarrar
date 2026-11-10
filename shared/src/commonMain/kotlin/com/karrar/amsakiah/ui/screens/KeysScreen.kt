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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararTokens

/**
 * نافذة «المفاتيح» — بوابة كل المحتوى الإيماني:
 * الأدعية والمناجاة، القرآن الكريم، البوصلة والمراقد، المسبحة، أعمال اليوم.
 *
 * كل بطاقة تفتح شاشة فرعية في الـback stack (اليوم ← المفاتيح ← الشاشة)،
 * واستجابة اللمس تلائم شكل البطاقة (ripple مقصوص بنفس نصف القطر).
 */
@Composable
fun KeysScreen(
    contentPadding: PaddingValues,
    onOpenDuas: () -> Unit,
    onOpenQuran: () -> Unit,
    onOpenQibla: () -> Unit,
    onOpenTasbih: () -> Unit,
    onOpenTasks: () -> Unit
) {
    val tiles = listOf(
        KeyTile(
            id = "duas",
            title = "الأدعية والمناجاة",
            subtitle = "أدعية الحجة وكربلاء وقم والبقيع · تعقيبات الصباح والمساء",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            accent = KararColors.Gold,
            onClick = onOpenDuas
        ),
        KeyTile(
            id = "quran",
            title = "القرآن الكريم",
            subtitle = "المصحف الشريف · قراءة ومتابعة كل سورة آية آية",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            accent = KararColors.Mint,
            onClick = onOpenQuran
        ),
        KeyTile(
            id = "qibla",
            title = "البوصلة والمراقد",
            subtitle = "القبلة الحية · المراقد من مكة إلى سامراء",
            icon = Icons.Filled.Mosque,
            accent = KararColors.Sky,
            onClick = onOpenQibla
        ),
        KeyTile(
            id = "tasbih",
            title = "المسبحة",
            subtitle = "مسبحة الزهراء (ع) · أذكارك المفضلة · تتبّع الجولات",
            icon = Icons.Filled.CheckCircle,
            accent = KararColors.Violet,
            onClick = onOpenTasbih
        ),
        KeyTile(
            id = "tasks",
            title = "أعمال اليوم",
            subtitle = "مهام وملاحظات · أعمالنا قبل النوم · تعهدنا اليومي",
            icon = Icons.Filled.EventNote,
            accent = KararColors.Amber,
            onClick = onOpenTasks
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 10.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "المفاتيح",
                            color = KararColors.TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "اختر ما تريد — كل باب يُفتح بشاشة كاملة",
                            color = KararColors.TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Icon(
                        Icons.Filled.Mosque,
                        contentDescription = null,
                        tint = KararColors.Gold.copy(alpha = 0.9f),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            items(tiles, key = { it.id }) { tile ->
                KeyTileCard(tile)
            }
        }
    }
}

private data class KeyTile(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
    val onClick: () -> Unit
)

@Composable
private fun KeyTileCard(tile: KeyTile) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                tile.onClick()
            },
        corner = 22.dp,
        color = KararColors.CardSoft,
        borderColor = KararColors.Border,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // الأيقونة داخل دائرة بلون الوحدة — نفس لغة بطاقات المفاتيح في البرنامج.
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(tile.accent.copy(alpha = 0.16f))
                    .border(1.dp, tile.accent.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tile.icon,
                    contentDescription = null,
                    tint = tile.accent,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = tile.title,
                    color = KararColors.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = tile.subtitle,
                    color = KararColors.TextMuted,
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                )
            }
            Text("‹", color = KararColors.TextMuted, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}
