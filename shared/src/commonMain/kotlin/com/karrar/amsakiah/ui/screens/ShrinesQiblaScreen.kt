package com.karrar.amsakiah.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.data.HolySite
import com.karrar.amsakiah.core.data.HolySites
import com.karrar.amsakiah.core.data.SiteKind
import com.karrar.amsakiah.core.permissions.rememberPermissionController
import com.karrar.amsakiah.core.sensors.createCompassProvider
import com.karrar.amsakiah.core.state.AppState
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.components.InfoChip
import com.karrar.amsakiah.ui.components.SectionTitle
import com.karrar.amsakiah.ui.theme.KararColors
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ShrinesQiblaScreen(state: AppState, contentPadding: PaddingValues) {
    val settings = state.settings
    val scope = rememberCoroutineScope()
    val permissions = rememberPermissionController()
    var heading by remember { mutableStateOf<Float?>(null) }
    val compass = remember { createCompassProvider() }
    val available = remember { compass.isAvailable() }

    // المرقد/المكان المختار — الافتراضي الكعبة المشرفة
    var selected by remember { mutableStateOf(HolySites.all.first()) }

    // طلب صلاحية الموقع من النظام أولًا، ثم المتابعة تلقائيًا بعد الموافقة
    var gpsAfterGrant by remember { mutableStateOf(false) }
    LaunchedEffect(gpsAfterGrant, permissions.status.location) {
        if (gpsAfterGrant && permissions.status.location) {
            gpsAfterGrant = false
            state.detectLocation()
        }
    }

    DisposableEffect(Unit) {
        if (available) compass.start { heading = it }
        onDispose { compass.stop() }
    }

    val kaabaBearing = remember(settings.latitude, settings.longitude) {
        HolySites.qiblaBearing(settings.latitude, settings.longitude)
    }
    val kaabaDistance = remember(settings.latitude, settings.longitude) {
        HolySites.qiblaDistance(settings.latitude, settings.longitude)
    }
    val sites = remember(settings.latitude, settings.longitude) {
        HolySites.sortedByDistance(settings.latitude, settings.longitude)
    }
    val selectedDistance = remember(sites, selected) {
        sites.firstOrNull { it.first.name == selected.name }?.second ?: 0.0
    }
    val selectedBearing = remember(settings.latitude, settings.longitude, selected) {
        HolySites.bearingFrom(settings.latitude, settings.longitude, selected)
    }
    val accent = Color(HolySites.accentOf(selected))

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoChip("${settings.cityName}، ${settings.countryName}", icon = Icons.Filled.MyLocation)
                when (settings.locationSource) {
                    "gps" -> InfoChip("GPS", container = Color(0x334CAF50), textColor = Color(0xFF81C784))
                    "ip" -> InfoChip("IP", container = Color(0x3338BDF8), textColor = Color(0xFF7DD3FC))
                }
            }
        }

        // ---------- البوصلة الموحّدة: القبلة + كل المراقد بألوانها ----------
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), corner = 30.dp) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "بوصلة المراقد والقبلة",
                        color = KararColors.Gold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "اضغط أي مكان من القائمة ليتحول السهم إليه بلونه",
                        color = KararColors.TextMuted,
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))

                    // شارة المكان المختار
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(accent.copy(alpha = 0.2f))
                                .border(1.dp, accent.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(selected.emoji, fontSize = 16.sp)
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                selected.name,
                                color = accent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${selected.city}، ${selected.country}",
                                color = KararColors.TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    CompassRose(
                        heading = heading ?: 0f,
                        targetBearing = selectedBearing,
                        accent = accent,
                        sites = HolySites.all,
                        userLat = settings.latitude,
                        userLon = settings.longitude,
                        selected = selected,
                        modifier = Modifier.size(292.dp)
                    )
                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "الاتجاه إلى ${selected.name}",
                        color = KararColors.TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${selectedBearing.toInt()}°  •  ${HolySites.compassLabel(selectedBearing)}",
                        color = accent,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (selectedDistance < 1.0) "المسافة: أقل من كم" else "المسافة: ${selectedDistance.toInt()} كم",
                        color = KararColors.TextSecondary,
                        fontSize = 12.sp
                    )
                    if (selected.kind != SiteKind.KAABA) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "القبلة (الكعبة): ${kaabaBearing.toInt()}° • ${kaabaDistance} كم",
                            color = KararColors.Gold.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                    }
                    if (heading != null) {
                        Text(
                            text = "اتجاه جهازك الآن: ${heading!!.toInt()}°",
                            color = KararColors.TextMuted,
                            fontSize = 9.sp
                        )
                    }
                    if (!available) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "حساس البوصلة غير متوفر — الأرقام أعلاه كافية للاتجاه",
                            color = KararColors.Danger,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    if (permissions.status.location) {
                        scope.launch { state.detectLocation() }
                    } else {
                        // نافذة نظام أصلية لطلب صلاحية الموقع
                        gpsAfterGrant = true
                        permissions.requestLocation()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KararColors.Gold.copy(alpha = 0.15f),
                    contentColor = KararColors.Gold
                )
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (state.locating) "جارٍ تحديد الموقع..." else "تحديد موقعي عبر GPS (أوفلاين)",
                    fontSize = 13.sp
                )
            }
        }

        if (state.settings.useGps && state.nearby != null) {
            item {
                Text(
                    text = com.karrar.amsakiah.core.astronomy.CityDatabase.proximityLabel(state.nearby),
                    color = KararColors.TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        item { SectionTitle("المراقد والأماكن المقدسة (${sites.size})", icon = Icons.Filled.MyLocation) }

        items(sites, key = { it.first.name }) { (site, distance) ->
            val bearing = HolySites.bearingFrom(settings.latitude, settings.longitude, site)
            val siteColor = Color(HolySites.accentOf(site))
            val isSelected = site.name == selected.name
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isSelected) siteColor.copy(alpha = 0.13f) else Color(0x0DFFFFFF))
                    .border(
                        1.dp,
                        if (isSelected) siteColor.copy(alpha = 0.65f) else Color(0x14FFFFFF),
                        RoundedCornerShape(18.dp)
                    )
                    .clickable { selected = site }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(siteColor.copy(alpha = 0.18f))
                        .border(1.dp, siteColor.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(site.emoji, fontSize = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        site.name,
                        color = if (isSelected) siteColor else KararColors.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${site.city}، ${site.country}", color = KararColors.TextMuted, fontSize = 11.sp)
                        if (isSelected) {
                            Spacer(Modifier.width(6.dp))
                            Text("• على البوصلة", color = siteColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        if (distance < 1.0) "أقل من كم" else "${distance.toInt()} كم",
                        color = siteColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${bearing.toInt()}° ${HolySites.compassLabel(bearing)}",
                        color = KararColors.TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

/**
 * بوصلة تفاعلية:
 * - تدور مع حساس الجهاز (heading).
 * - تعرض كل المراقد كنقاط ملوّنة على محيطها.
 * - السهم الكبير يتجه إلى المكان المختار بلونه الخاص.
 */
@Composable
private fun CompassRose(
    heading: Float,
    targetBearing: Double,
    accent: Color,
    sites: List<HolySite>,
    userLat: Double,
    userLon: Double,
    selected: HolySite,
    modifier: Modifier = Modifier
) {
    Canvas(modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f - 4.dp.toPx()

        // خلفية متوهجة بلون المكان المختار
        drawCircle(
            brush = Brush.radialGradient(
                listOf(accent.copy(alpha = 0.16f), Color(0x0AFFFFFF)),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )
        drawCircle(Color(0x33FFFFFF), radius = radius, center = center, style = Stroke(2.dp.toPx()))

        rotate(-heading, pivot = center) {
            // علامات الدرجات
            for (i in 0 until 72) {
                val angle = (i * 5.0) * PI / 180.0
                val long = i % 9 == 0
                val inner = radius - if (long) 18.dp.toPx() else 8.dp.toPx()
                val start = Offset(
                    (center.x + inner * cos(angle)).toFloat(),
                    (center.y + inner * sin(angle)).toFloat()
                )
                val end = Offset(
                    (center.x + radius * cos(angle)).toFloat(),
                    (center.y + radius * sin(angle)).toFloat()
                )
                drawLine(
                    color = if (long) Color(0x66FFD700) else Color(0x33FFFFFF),
                    start = start,
                    end = end,
                    strokeWidth = if (long) 2.dp.toPx() else 1.dp.toPx()
                )
            }

            // مؤشر الشمال
            drawCircle(Color(0xFFFFD700), radius = 4.dp.toPx(), center = Offset(center.x, center.y - radius + 4.dp.toPx()))

            // نقاط المراقد على المحيط — كلٌّ بلونه
            sites.forEachIndexed { index, site ->
                val bearing = HolySites.bearingFrom(userLat, userLon, site)
                val rad = bearing * PI / 180.0
                val ringRadius = radius - (14 + (index % 3) * 9).dp.toPx()
                val pos = Offset(
                    (center.x + ringRadius * sin(rad)).toFloat(),
                    (center.y - ringRadius * cos(rad)).toFloat()
                )
                val siteColor = Color(HolySites.accentOf(site))
                val isSelected = site.name == selected.name
                if (isSelected) {
                    drawCircle(siteColor.copy(alpha = 0.28f), radius = 11.dp.toPx(), center = pos)
                    drawCircle(Color.White, radius = 5.5.dp.toPx(), center = pos, style = Stroke(1.5.dp.toPx()))
                }
                drawCircle(siteColor, radius = if (isSelected) 4.5.dp.toPx() else 3.dp.toPx(), center = pos)
            }

            // سهم المكان المختار
            rotate(targetBearing.toFloat(), pivot = center) {
                val tip = Offset(center.x, center.y - radius + 30.dp.toPx())
                val left = Offset(center.x - 13.dp.toPx(), center.y + 22.dp.toPx())
                val right = Offset(center.x + 13.dp.toPx(), center.y + 22.dp.toPx())
                val path = Path().apply {
                    moveTo(tip.x, tip.y)
                    lineTo(left.x, left.y)
                    lineTo(center.x, center.y + 4.dp.toPx())
                    lineTo(right.x, right.y)
                    close()
                }
                drawPath(
                    path,
                    brush = Brush.linearGradient(
                        listOf(accent, accent.copy(alpha = 0.55f)),
                        start = tip,
                        end = Offset(center.x, center.y)
                    )
                )
                drawCircle(accent.copy(alpha = 0.28f), radius = 20.dp.toPx(), center = tip)
                drawCircle(accent, radius = 5.dp.toPx(), center = tip)
            }
        }

        // مركز البوصلة
        drawCircle(Color(0xFF0B1220), radius = 26.dp.toPx(), center = center)
        drawCircle(accent, radius = 6.dp.toPx(), center = center)
    }
}
