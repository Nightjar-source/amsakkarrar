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
import androidx.compose.foundation.lazy.grid.GridCells

import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Key
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
    onOpenTasks: () -> Unit,
    onOpenJinan: () -> Unit,
    onOpenDailyDeeds: () -> Unit
) {
    // ترتيب الظهور: القرآن ← مفاتيح الجنان ← أعمال اليوم ← الأدعية ← البوصلة ← المسبحة ← أعمال اليوم (المهام).
    // كلها منسقة بنفس البناء (كشيدة شفافة + أيقونة دائرية + عنوان) لتوحيد الشكل.
    val tiles = listOf(
        KeyTile(
            id = "quran",
            title = "القرآن الكريم",
            subtitle = "المصحف الشريف — فهرس سورة آية آية",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            accent = KararColors.Mint,
            onClick = onOpenQuran
        ),
        KeyTile(
            id = "jinan",
            title = "مفاتيح الجنان",
            subtitle = "مصباح المتهجّد — أدعية وأعمال أيام وأشهر وزيارات أوفلاين",
            icon = Icons.Filled.Key,
            accent = KararColors.Gold,
            onClick = onOpenJinan
        ),
        KeyTile(
            id = "dailydeeds",
            title = "أعمال اليوم",
            subtitle = "الليلة واليوم ويوم الأسبوع من «مفاتيح الجنان» — تُحدَّث تلقائيًا",
            icon = Icons.Filled.CalendarToday,
            accent = KararColors.Amber,
            onClick = onOpenDailyDeeds
        ),
        KeyTile(
            id = "duas",
            title = "الأدعية والمناجاة",
            subtitle = "أدعية الحجة وكربلاء وقم والبقيع · تعقيبات الصباح والمساء",
            icon = Icons.AutoMirrored.Filled.MenuBook,
            accent = KararColors.Purple,
            onClick = onOpenDuas
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
            title = "المهام والملاحظات",
            subtitle = "⭐ محفوظاتي من «مفاتيح الجنان» + ملاحظاتي بين إيديك",
            icon = Icons.AutoMirrored.Filled.EventNote,
            accent = KararColors.Amber,
            onClick = onOpenTasks
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        // شبكة تكيّفية رسمية (GridCells.Adaptive): عمود واحد في COMPACT،
        // عمودان فأكثر في MEDIUM+ — البطاقات تتوسع ولا تتقلص أبدًا.
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 340.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 10.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // العنوان في الشريط العلوي الموحّد فقط — بلا تكرار داخل الشاشة.
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
