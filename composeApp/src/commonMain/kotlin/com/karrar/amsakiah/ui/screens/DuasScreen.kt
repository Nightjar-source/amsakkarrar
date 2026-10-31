package com.karrar.amsakiah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.data.ShiaDuaItem
import com.karrar.amsakiah.core.data.ShiaDuasLibrary

@Composable
fun DuasScreen(modifier: Modifier = Modifier) {
    var selectedCategory by remember { mutableStateOf("الكل") }
    var activeDua by remember { mutableStateOf<ShiaDuaItem?>(null) }
    var fontSizeSp by remember { mutableStateOf(18) }
    var isPlaying by remember { mutableStateOf(false) }

    val categories = remember {
        listOf("الكل", "أدعية الإمام المهدي (عج)", "مناجاة وأدعية الليالي", "تعقيبات الصباح والمساء", "الزيارات المشهورة")
    }

    val filteredDuas = remember(selectedCategory) {
        if (selectedCategory == "الكل") ShiaDuasLibrary.duasList
        else ShiaDuasLibrary.duasList.filter { it.category == selectedCategory }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B14))
    ) {
        if (activeDua != null) {
            // قارئ الدعاء المفصل
            val dua = activeDua!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                // شريط علوي مع زر الرجوع وأدوات الخط والصوت
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            activeDua = null
                            isPlaying = false
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF1E1B2E))
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "رجوع", tint = Color.White)
                    }

                    // أزرار تكبير وتصغير الخط
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (fontSizeSp > 14) fontSizeSp -= 2 },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF1E1B2E))
                        ) {
                            Text("A-", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = { if (fontSizeSp < 32) fontSizeSp += 2 },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF1E1B2E))
                        ) {
                            Text("A+", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // عنوان الدعاء
                Text(
                    text = dua.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD54F),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = dua.subtitle,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp)
                )

                // نص الدعاء الشريف
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF181528),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = dua.arabicText,
                            fontSize = fontSizeSp.sp,
                            lineHeight = (fontSizeSp * 1.8).sp,
                            color = Color(0xFFF5F5F5),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // مشغل الصوت السفلي
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF261D38),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 60.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFFFFD54F))
                            Text("صوت قارئ العتبة الحسينية", fontSize = 13.sp, color = Color.White)
                        }

                        IconButton(
                            onClick = { isPlaying = !isPlaying },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFFFD54F))
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFF261D38)
                            )
                        }
                    }
                }
            }
        } else {
            // قائمة الأدعية والزيارات
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "مفاتيح الجنان والأدعية المباركة",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD54F)
                )
                Text(
                    text = "أدعية وتعقيبات مأثورة عن أئمة أهل البيت (عليهم السلام)",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.65f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // شريط التصنيفات
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = (selectedCategory == cat),
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFD54F),
                                selectedLabelColor = Color(0xFF1E1B2E),
                                containerColor = Color(0xFF1E1B2E),
                                labelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                // قائمة البطاقات
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredDuas) { item ->
                        Surface(
                            onClick = { activeDua = item },
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFF1A1728),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFD54F)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.subtitle,
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.7f),
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "الميقات: ${item.recommendedTime}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF81C784)
                                    )
                                }
                                Icon(
                                    Icons.Default.ChevronLeft,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
