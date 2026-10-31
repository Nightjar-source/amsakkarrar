package com.karrar.amsakiah.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TasbihScreen(modifier: Modifier = Modifier) {
    var count by remember { mutableStateOf(0) }
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = tween(120)
    )

    // مراحل تسبيح الزهراء (ع)
    val (phrase, stageCurrent, stageMax, stageColor) = remember(count) {
        when {
            count < 34 -> Quadruple("اللهُ أَكْبَر", count + 1, 34, Color(0xFFFFD54F))
            count < 67 -> Quadruple("الحَمْدُ لِلَّه", count - 33, 33, Color(0xFF81C784))
            count < 100 -> Quadruple("سُبْحَانَ اللَّه", count - 66, 33, Color(0xFF4FC3F7))
            else -> Quadruple("تَمَّ التَّسْبِيحُ بِحَمْدِ اللَّه", 100, 100, Color(0xFFFFE082))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B14))
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ترويسة الشاشة
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFFD54F))
                Text(
                    text = "تسبيح السيدة فاطمة الزهراء (عليها السلام)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ما عُبِدَ الله بشيءٍ من التحميد أفضل من تسبيح فاطمة (ع)",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.65f)
            )
        }

        // الزر الدائري الكبير التفاعلي للتسبيح
        Box(
            modifier = Modifier
                .scale(scale)
                .size(240.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF261D38), Color(0xFF130F1F))
                    )
                )
                .border(4.dp, stageColor.copy(alpha = 0.8f), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (count < 100) count++
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = phrase,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = stageColor
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (count >= 100) "100" else "$stageCurrent",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "/$stageMax",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.45f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "انقر في أي مكان للتسبيح",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }

        // بطاقات المراحل الثلاث
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (count < 34) Color(0xFFFFD54F).copy(alpha = 0.2f) else Color(0xFF1E1B2E),
                    border = if (count < 34) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD54F)) else null,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("الله أكبر", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                        Text("34 مرة", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (count in 34..66) Color(0xFF81C784).copy(alpha = 0.2f) else Color(0xFF1E1B2E),
                    border = if (count in 34..66) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF81C784)) else null,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("الحمد لله", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
                        Text("33 مرة", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (count in 67..99) Color(0xFF4FC3F7).copy(alpha = 0.2f) else Color(0xFF1E1B2E),
                    border = if (count in 67..99) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF4FC3F7)) else null,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("سبحان الله", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4FC3F7))
                        Text("33 مرة", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }

            // زر التسبيح وزر التصفير
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { if (count < 100) count++ },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD54F),
                        contentColor = Color(0xFF1A1326)
                    )
                ) {
                    Text("تسبيح (+1)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { count = 0 },
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.RotateLeft, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("تصفير العداد", fontSize = 13.sp)
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
