package com.karrar.amsakiah.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.data.HolySites
import com.karrar.amsakiah.core.data.SiteKind
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
    var heading by remember { mutableStateOf<Float?>(null) }
    val compass = remember { createCompassProvider() }
    val available = remember { compass.isAvailable() }

    DisposableEffect(Unit) {
        if (available) compass.start { heading = it }
        onDispose { compass.stop() }
    }

    val qibla = remember(settings.latitude, settings.longitude) {
        HolySites.qiblaBearing(settings.latitude, settings.longitude)
    }
    val kaabaDistance = remember(settings.latitude, settings.longitude) {
        HolySites.qiblaDistance(settings.latitude, settings.longitude)
    }
    val sites = remember(settings.latitude, settings.longitude) {
        HolySites.sortedByDistance(settings.latitude, settings.longitude)
    }

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
                if (state.settings.useGps) InfoChip("GPS", container = Color(0x334CAF50))
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), corner = 30.dp) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "اتجاه القبلة",
                        color = KararColors.Gold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    CompassRose(
                        heading = heading ?: 0f,
                        qiblaBearing = qibla,
                        modifier = Modifier.size(260.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "القبلة ${qibla.toInt()}°  •  ${HolySites.compassLabel(qibla)}",
                        color = KararColors.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "المسافة إلى الكعبة: $kaabaDistance كم",
                        color = KararColors.TextSecondary,
                        fontSize = 12.sp
                    )
                    if (!available) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "حساس البوصلة غير متوفر — استخدم الزاوية الرقمية أعلاه",
                            color = KararColors.Danger,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = { scope.launch { state.detectLocation() } },
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
            GlassCard(modifier = Modifier.fillMaxWidth(), corner = 18.dp, contentPadding = PaddingValues(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(38.dp)
                            .background(
                                when (site.kind) {
                                    SiteKind.KAABA -> Color(0x33FFD700)
                                    SiteKind.PROPHET -> Color(0x334CAF50)
                                    SiteKind.SHRINE -> Color(0x33A18CD1)
                                    SiteKind.MOSQUE -> Color(0x3300F2FE)
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(site.emoji, fontSize = 18.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(site.name, color = KararColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${site.city}، ${site.country}",
                            color = KararColors.TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            if (distance < 1.0) "أقل من كم" else "${distance.toInt()} كم",
                            color = KararColors.Gold,
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
}

@Composable
private fun CompassRose(heading: Float, qiblaBearing: Double, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f - 4.dp.toPx()

        // خلفية دائرية
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0x22A18CD1), Color(0x0AFFFFFF)),
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
                val start = Offset(
                    (center.x + (radius - if (long) 18.dp.toPx() else 8.dp.toPx()) * cos(angle)).toFloat(),
                    (center.y + (radius - if (long) 18.dp.toPx() else 8.dp.toPx()) * sin(angle)).toFloat()
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

            // سهم القبلة
            rotate(qiblaBearing.toFloat(), pivot = center) {
                val tip = Offset(center.x, center.y - radius + 20.dp.toPx())
                val left = Offset(center.x - 14.dp.toPx(), center.y + 24.dp.toPx())
                val right = Offset(center.x + 14.dp.toPx(), center.y + 24.dp.toPx())
                val path = Path().apply {
                    moveTo(tip.x, tip.y)
                    lineTo(left.x, left.y)
                    lineTo(center.x, center.y + 6.dp.toPx())
                    lineTo(right.x, right.y)
                    close()
                }
                drawPath(path, brush = Brush.linearGradient(listOf(KararColors.Gold, KararColors.GoldDeep)))
                drawCircle(Color(0x33FFD700), radius = 22.dp.toPx(), center = tip)
            }
        }

        drawCircle(KararColors.Gold, radius = 5.dp.toPx(), center = center)
    }
}
