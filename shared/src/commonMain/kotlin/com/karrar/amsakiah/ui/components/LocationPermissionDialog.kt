package com.karrar.amsakiah.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.LocalKararPalette

/**
 * نافذة طلب إذن الموقع الجغرافي الموحّدة والأصلية لكافة شاشات التطبيق.
 *
 * الميزات:
 * 1. خلفية مصمتة فاخرة عالية التباين (تمنع الشفافية 100% السابقة تماماً).
 * 2. توضيح صريح لخصوصية المستخدم: الحساب الفلكي 100% أوفلاين دون إرسال إحداثيات لأي خادم.
 * 3. خياران واضحان: السماح الفوري بالإذن أو الانتقال لإعدادات النظام الاحتياطية.
 */
@Composable
fun LocationPermissionDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    onGrant: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalKararPalette.current
    val isDark = palette.isDark

    SpringDialog(
        visible = visible,
        onDismissRequest = onDismissRequest,
        dismissible = true,
        modifier = modifier
    ) {
        // بطاقة مصمتة فاخرة مع حماية من الشفافية وخلفية غنية
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(Color(0xFF172033), Color(0xFF0F172A), Color(0xFF0B1120))
                        } else {
                            listOf(Color(0xFFFFFFFF), Color(0xFFF8FAFC), Color(0xFFF1F5F9))
                        }
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(KararColors.Gold.copy(alpha = 0.55f), Color(0x33FFFFFF))
                        } else {
                            listOf(KararColors.Gold.copy(alpha = 0.65f), Color(0x1F000000))
                        }
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(22.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // ترويسة النافذة
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(KararColors.Gold.copy(alpha = 0.18f))
                            .border(1.dp, KararColors.Gold.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MyLocation,
                            contentDescription = null,
                            tint = KararColors.Gold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "الموقع الجغرافي",
                                color = KararColors.TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(KararColors.Success.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "أوفلاين 100%",
                                    color = KararColors.Success,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "لحساب المواقيت والقبلة وإمساكية مدينتك بدقة",
                            color = KararColors.TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // مربع الشرح والضمان الأمني
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) Color(0x2E000000) else Color(0x0A000000))
                        .border(1.dp, KararColors.Border, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = null,
                            tint = KararColors.Gold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "لا نجمع إحداثياتك ولا نرسلها إلى أي خادم خارجي. تُستخدم فقط داخل جهازك لمطابقة أقرب نقطة فلكية من قاعدة المدن المدمجة.",
                            color = KararColors.TextSecondary,
                            fontSize = 11.5.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // زر السماح بالموقع المباشر
                Button(
                    onClick = onGrant,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KararColors.Gold,
                        contentColor = KararColors.OnGold
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "السماح بتحديد موقعي",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(10.dp))

                // الأزرار الثانوية (إعدادات الجهاز + ليس الآن)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, KararColors.Border),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = KararColors.TextPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = null,
                            tint = KararColors.TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "إعدادات الجهاز",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(0.7f).height(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = KararColors.TextMuted
                        )
                    ) {
                        Text(
                            text = "ليس الآن",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
