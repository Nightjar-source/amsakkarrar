package com.karrar.amsakiah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.astronomy.*

@Composable
fun SettingsScreen(
    currentMethod: CalculationMethod,
    onSelectMethod: (CalculationMethod) -> Unit,
    currentHighLatRule: HighLatitudeRule,
    onSelectHighLatRule: (HighLatitudeRule) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpandedMethods by remember { mutableStateOf(false) }
    var isExpandedRules by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0B14))
            .padding(horizontal = 20.dp, vertical = 20.dp),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "الإعدادات الفلكية والشرعية",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD54F)
            )
            Text(
                text = "ضبط معايير الحساب وفق فتاوى المرجعية الدينية والخرائط الفلكية",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.65f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF1E1B2E),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = Color(0xFFFFD54F))
                        Text(
                            text = "طريقة حساب المواقيت",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { isExpandedMethods = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD54F))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(currentMethod.name, fontSize = 13.sp, maxLines = 1)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }

                    DropdownMenu(
                        expanded = isExpandedMethods,
                        onDismissRequest = { isExpandedMethods = false },
                        modifier = Modifier.background(Color(0xFF261D38))
                    ) {
                        OfflineCities.methodsMatrix.forEach { m ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        m.name,
                                        color = if (m.id == currentMethod.id) Color(0xFFFFD54F) else Color.White,
                                        fontSize = 13.sp
                                    )
                                },
                                onClick = {
                                    onSelectMethod(m)
                                    isExpandedMethods = false
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF1E1B2E),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = Color(0xFFFFD54F))
                        Text(
                            text = "معالجة خطوط العرض العليا (الدول الاسكندنافية وأوروبا)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { isExpandedRules = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD54F))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(currentHighLatRule.titleAr, fontSize = 13.sp, maxLines = 1)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }

                    DropdownMenu(
                        expanded = isExpandedRules,
                        onDismissRequest = { isExpandedRules = false },
                        modifier = Modifier.background(Color(0xFF261D38))
                    ) {
                        HighLatitudeRule.values().forEach { r ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        r.titleAr,
                                        color = if (r == currentHighLatRule) Color(0xFFFFD54F) else Color.White,
                                        fontSize = 13.sp
                                    )
                                },
                                onClick = {
                                    onSelectHighLatRule(r)
                                    isExpandedRules = false
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF1E1B2E),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF81C784))
                        Text("حول إمساكية كرار 2026", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "تطبيق فلكي إسلامي شامل مبني بأحدث معايير كوتلن كامبوس (Compose Multiplatform 2026) ونظام التنقل المتقدم Navigation 3 مع ودجات الشاشة الحية (Glance Widgets)، متوافق 100% مع تقويم العتبة العباسية المقدسة وخوارزميات معهد الكفيل.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
