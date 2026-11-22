package com.karrar.amsakiah.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import com.karrar.amsakiah.core.astronomy.moonGeometry
import kotlin.math.cos
import kotlin.math.sin

/**
 * طور القمر الفلكي الواقعي فائق الدقة (Hyper-Realistic Celestial Moon):
 * - هالة قمرية متعددة الطبقات (Lunar Corona & Atmospheric Aura).
 * - ضوء دافنشي التراكمي (Earthshine) على الجانب المظلم مع تضاريس خافتة تحاكي الرؤية التلسكوبية.
 * - التدرج الكروي ثلاثي الأبعاد مع حد فاصل فيزيائي دقيق (Terminator Line).
 * - معالم سطح القمر الحقيقية: البحار القمرية (Lunar Maria مثل بحر السكون ومحيط العواصف)،
 *   فوهة تيخو (Tycho) مع خطوط المقذوفات الشعاعية (Ray System)، وفوهة كوبرنيكوس.
 */
@Composable
fun MoonPhaseIcon(
    hijriDay: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(78.dp)) {
        val s = size.minDimension / 100f
        val center = Offset(size.width / 2f, size.height / 2f)
        val r = 38f * s
        val geom = moonGeometry(hijriDay, 1f)
        val k = geom.illuminationRatio

        // 1. الهالات الضوئية الخارجية (Lunar Corona & Glow)
        // هالة ذهبية/فضية شاسعة منتشرة
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x38FDE047),
                    Color(0x1CFDE047),
                    Color(0x06CBD5E1),
                    Color.Transparent
                ),
                center = center,
                radius = r * 2.1f
            ),
            radius = r * 2.1f,
            center = center
        )

        // هالة لؤلؤية وسطى ناعمة
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x40E2E8F0),
                    Color(0x1894A3B8),
                    Color.Transparent
                ),
                center = center,
                radius = r * 1.45f
            ),
            radius = r * 1.45f,
            center = center
        )

        // 2. مسار القرص القمري الكروي الكامل
        val fullSpherePath = Path().apply {
            addOval(Rect(center.x - r, center.y - r, center.x + r, center.y + r))
        }

        // 3. الجانب المظلم (Earthshine - ضوء الأرض المنعكس على القمر)
        // تدرج كروي واقعي يعطي عمقاً ثلاثي الأبعاد 3D Sphere Shading
        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to Color(0xF21E293B),
                    0.65f to Color(0xF80F172A),
                    0.95f to Color(0xFD080C16),
                    1.0f to Color(0xFF020617)
                ),
                center = Offset(center.x - r * 0.25f, center.y - r * 0.25f),
                radius = r * 1.15f
            ),
            radius = r,
            center = center
        )

        // تفاصيل خافتة جداً لبحار القمر على الجانب المظلم (Earthshine Maria)
        clipPath(fullSpherePath) {
            // بحار قمرية خافتة في الظلام
            drawCircle(
                color = Color(0x240F172A),
                radius = r * 0.38f,
                center = Offset(center.x + r * 0.15f, center.y - r * 0.15f)
            )
            drawCircle(
                color = Color(0x1F0F172A),
                radius = r * 0.30f,
                center = Offset(center.x - r * 0.22f, center.y + r * 0.18f)
            )
            drawCircle(
                color = Color(0x28020617),
                radius = r * 0.24f,
                center = Offset(center.x + r * 0.20f, center.y + r * 0.32f)
            )
        }

        // إطار حافة القمر (Limb Edge) خافت وأنيق
        drawCircle(
            color = Color.White.copy(alpha = 0.22f),
            radius = r,
            center = center,
            style = Stroke(width = 0.8f * s)
        )

        // 4. السطح المضاء للقمر (Lit Hemisphere)
        if (!geom.isNewMoon) {
            val rx = geom.rx * r
            val litPath = Path().apply {
                moveTo(center.x, center.y - r)
                if (geom.isFullMoon) {
                    addOval(Rect(center.x - r, center.y - r, center.x + r, center.y + r))
                } else {
                    // القوس الخارجي لنصف الدائرة (حافة القمر الخارجية)
                    addArc(
                        Rect(center.x - r, center.y - r, center.x + r, center.y + r),
                        270f,
                        if (geom.isWaxing) 180f else -180f
                    )
                    // القوس الداخلي لحد الإضاءة (Terminator Ellipse)
                    val crescent = geom.isWaxing == (k < 0.5f)
                    addArc(
                        Rect(center.x - rx, center.y - r, center.x + rx, center.y + r),
                        90f,
                        if (crescent) -180f else 180f
                    )
                    close()
                }
            }

            // رسم الكتلة المضاءة بتدرج لوني فلكي غني
            drawPath(
                path = litPath,
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to Color(0xFFFFFFFF),       // توهج أبيض ساطع
                        0.25f to Color(0xFFFFFDF5),      // ثرى قمري لؤلؤي
                        0.55f to Color(0xFFFDE68A),      // وهج ذهبي دافئ خافت
                        0.80f to Color(0xFFE2E8F0),      // صخور الحافة
                        0.94f to Color(0xFFCBD5E1),      // تدرج رمادي
                        1.0f to Color(0xFF94A3B8)        // تلاشي ناعم عند خط الظل
                    ),
                    center = Offset(
                        if (geom.isWaxing) center.x + r * 0.22f else center.x - r * 0.22f,
                        center.y - r * 0.20f
                    ),
                    radius = r * 1.55f
                )
            )

            // رسم الفوهات والبحار القمرية الحقيقية داخل السطح المضاء فقط
            clipPath(litPath) {
                // بحر السكون (Mare Tranquillitatis) وبحر الصفاء
                drawOval(
                    color = Color(0x3D475569),
                    topLeft = Offset(center.x - r * 0.12f, center.y - r * 0.45f),
                    size = androidx.compose.ui.geometry.Size(r * 0.58f, r * 0.46f)
                )

                // محيط العواصف (Oceanus Procellarum)
                drawOval(
                    color = Color(0x3864748B),
                    topLeft = Offset(center.x - r * 0.55f, center.y - r * 0.22f),
                    size = androidx.compose.ui.geometry.Size(r * 0.65f, r * 0.55f)
                )

                // بحر الأمطار (Mare Imbrium)
                drawCircle(
                    color = Color(0x36475569),
                    radius = r * 0.26f,
                    center = Offset(center.x - r * 0.18f, center.y - r * 0.12f)
                )

                // بحر الشدائد (Mare Crisium)
                drawOval(
                    color = Color(0x42334155),
                    topLeft = Offset(center.x + r * 0.38f, center.y - r * 0.18f),
                    size = androidx.compose.ui.geometry.Size(r * 0.28f, r * 0.22f)
                )

                // فوهة كوبرنيكوس (Copernicus Crater) مع هالة مضاءة
                val copernicus = Offset(center.x - r * 0.16f, center.y + r * 0.08f)
                drawCircle(Color(0x30334155), radius = r * 0.09f, center = copernicus)
                drawCircle(Color(0x60FFFFFF), radius = r * 0.035f, center = copernicus)

                // فوهة تيخو الكبرى (Tycho Crater) في الجنوب مع أشعتها الامتدادية (Ray System)
                val tycho = Offset(center.x - r * 0.06f, center.y + r * 0.54f)
                // خطوط أشعة تيخو الشعاعية
                for (rayDeg in listOf(25, 60, 110, 150, 205, 270, 315)) {
                    val rad = (rayDeg * kotlin.math.PI / 180.0)
                    val rayLen = r * 0.55f
                    val endRay = Offset(
                        (tycho.x + rayLen * cos(rad)).toFloat(),
                        (tycho.y + rayLen * sin(rad)).toFloat()
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.25f),
                        start = tycho,
                        end = endRay,
                        strokeWidth = 0.7f * s
                    )
                }
                // مركز فوهة تيخو الساطع
                drawCircle(Color(0x40334155), radius = r * 0.09f, center = tycho)
                drawCircle(Color(0x99FFFFFF), radius = r * 0.045f, center = tycho)

                // فوهة كبلر (Kepler)
                val kepler = Offset(center.x - r * 0.36f, center.y + r * 0.14f)
                drawCircle(Color(0x30334155), radius = r * 0.06f, center = kepler)
                drawCircle(Color(0x55FFFFFF), radius = r * 0.025f, center = kepler)

                // وهج الحافة المضاءة (Limb Brightening)
                drawCircle(
                    color = Color.White.copy(alpha = 0.28f),
                    radius = r,
                    center = center,
                    style = Stroke(width = 1.2f * s)
                )
            }

            // خط حد الإضاءة الناعم (Terminator Soft Diffusion)
            if (!geom.isFullMoon && rx > 1.5f * s) {
                // نعومة إضافية للحد بين النور والظلام
                drawPath(
                    path = litPath,
                    color = Color(0x18000000),
                    style = Stroke(width = 1.0f * s)
                )
            }
        }
    }
}
