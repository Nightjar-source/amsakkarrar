package com.karrar.amsakiah.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.karrar.amsakiah.core.astronomy.moonGeometry

/**
 * طور القمر الواقعي — نفس `RealisticMoonPhaseSVG` في `amsak3d.tsx`:
 * هالة مزدوجة + قرص داكن + السطح المضاء بقوسين + ثلاث فوهات.
 * يُستخدم في بطاقة القمر في شاشة «المراقد والقبلة».
 */
@Composable
fun MoonPhaseIcon(
    hijriDay: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(56.dp)) {
        val unit = this.size.width / 56f
        val c = Offset(28f * unit, 28f * unit)
        val r = 15f * unit
        val geom = moonGeometry(hijriDay, 1f)
        val k = geom.illuminationRatio

        // هالة مزدوجة
        drawCircle(Color(0x24FDE68A), radius = r * 1.9f, center = c)
        drawCircle(Color(0x2EE2E8F0), radius = r * 1.35f, center = c)

        // القرص المظلم + محيط
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(0f to Color(0xEB1E293B), 1f to Color(0xF6090D16)),
                center = c,
                radius = r
            ),
            radius = r,
            center = c
        )
        drawCircle(Color.White.copy(alpha = 0.18f), radius = r, center = c, style = Stroke(width = 0.6f))

        // السطح المضاء
        if (!geom.isNewMoon) {
            val rx = geom.rx * r
            val lit = Path().apply {
                moveTo(c.x, c.y - r)
                if (geom.isFullMoon) {
                    addOval(Rect(c.x - r, c.y - r, c.x + r, c.y + r))
                } else {
                    addArc(Rect(c.x - r, c.y - r, c.x + r, c.y + r), 270f, if (geom.isWaxing) 180f else -180f)
                    val crescent = geom.isWaxing == (k < 0.5f)
                    addArc(Rect(c.x - rx, c.y - r, c.x + rx, c.y + r), 90f, if (crescent) -180f else 180f)
                    close()
                }
            }
            drawPath(
                path = lit,
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0f to Color(0xFFFFFDF7),
                        0.45f to Color(0xFFFDE68A),
                        0.80f to Color(0xFFE2E8F0),
                        1f to Color(0xFF94A3B8)
                    ),
                    center = Offset(c.x - r * 0.24f, c.y - r * 0.3f),
                    radius = r * 1.6f
                )
            )
            drawCircle(Color(0xFF64748B).copy(alpha = if (k > 0.4f) 0.22f else 0.08f), r * 0.25f, Offset(c.x - r * 0.28f, c.y - r * 0.2f))
            drawCircle(Color(0xFF64748B).copy(alpha = 0.20f), r * 0.21f, Offset(c.x + r * 0.22f, c.y - r * 0.12f))
            drawCircle(Color(0xFF475569).copy(alpha = 0.18f), r * 0.3f, Offset(c.x + r * 0.15f, c.y + r * 0.3f))
        }
    }
}
