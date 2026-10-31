package com.karrar.amsakiah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.astronomy.CalculationMethod
import com.karrar.amsakiah.core.astronomy.OfflineCities

@Composable
fun AnglesMatrixScreen(
    currentMethod: CalculationMethod,
    onSelectMethod: (CalculationMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B14))
            .padding(horizontal = 16.dp, vertical = 20.dp),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Layers, contentDescription = null, tint = Color(0xFFFFD54F))
                Text(
                    text = "مصفوفة طرق الحساب والزوايا الفلكية (ID 0..12)",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "جميع الزوايا والمعاملات الفقهية مع إمكانية التفعيل المباشر في المحرك الموحد",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.65f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFD54F).copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("المعيار النشط حالياً:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    Text(currentMethod.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                }
            }
        }

        items(OfflineCities.methodsMatrix) { m ->
            val isCurrent = (m.id == currentMethod.id)

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isCurrent) Color(0xFF2E2442) else Color(0xFF191626),
                border = androidx.compose.foundation.BorderStroke(
                    if (isCurrent) 1.5.dp else 1.dp,
                    if (isCurrent) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.06f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "#${m.id}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD54F)
                            )
                            Text(
                                text = m.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (m.isShia) Color(0xFF81C784).copy(alpha = 0.2f) else Color(0xFF64B5F6).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (m.isShia) "شيعي جعفري" else "سني عام",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (m.isShia) Color(0xFF81C784) else Color(0xFF64B5F6),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("الفجر", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                            Text("${m.fajr}°", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Column {
                            Text("المغرب", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                            Text(
                                if (m.maghribType == 0) "${m.maghribVal}° (زاوية)" else "0.833° (غروب)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFE082)
                            )
                        }

                        Column {
                            Text("العشاء", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                            Text(
                                if (m.ishaType == 0) "${m.ishaVal}° (زاوية)" else "${m.ishaVal.toInt()} دقيقة",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF80DEEA)
                            )
                        }

                        Button(
                            onClick = { onSelectMethod(m) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCurrent) Color(0xFFFFD54F) else Color(0xFF261D38),
                                contentColor = if (isCurrent) Color(0xFF1E1B2E) else Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(if (isCurrent) "مُفعّل ✓" else "اختيار", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
