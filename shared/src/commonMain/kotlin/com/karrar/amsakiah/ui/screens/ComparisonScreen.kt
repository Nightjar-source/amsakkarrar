package com.karrar.amsakiah.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookOpen
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FiqhArticle(
    val id: String,
    val title: String,
    val summary: String,
    val content: String
)

@Composable
fun ComparisonScreen(modifier: Modifier = Modifier) {
    val articles = remember {
        listOf(
            FiqhArticle(
                id = "midnight",
                title = "📌 منتصف الليل الشرعي الشيعي ومصفوفة الزوايا",
                summary = "المبدأ الفقهي عند الطائفة الجعفرية وحساباته الدقيقة",
                content = """
• المبدأ الفقهي: منتصف الليل الشرعي عند الطائفة الجعفرية هو منتصف المسافة الزمنية الواقعة تماماً بين [غروب الشمس الفعلي] و[طلوع الفجر الصادق].
• المعادلة الرياضية للبرمجة:
  Midnight = Sunset + ((Fajr - Sunset) / 2)
• تلافي الأخطاء السابقة: تم تصحيح هذه الخوارزمية كلياً بالصيف في لندن وشمال أوروبا لتلافي خطأ العبور الحسابي لخانة الصباح (AM)، لضمان صحة صلاة العشاء وصلاة الليل والوتر.
                """.trimIndent()
            ),
            FiqhArticle(
                id = "kafeel_vs_london",
                title = "1️⃣ الفرق الفقهي بين الكفيل ومؤسسة لندن (السيستاني)",
                summary = "زاوية 18° للكفيل مقابل زاوية 15° لمؤسسة لندن في بلدان شمال أوروبا",
                content = """
• معهد الكفيل (العتبة العباسية المقدسة): يعتمد زاوية الفجر الطبيعية 18° تحت الأفق، وزاوية المغرب 4° (ذهاب الحمرة المشرقية). صيفاً عند تداخل الشفق، يسقط الإحداثيات على خط عرض 48° (باريس) أو يتيح تقويم الصائغ.
• مؤسسة لندن (مؤسسة الإمام علي بلندن): تعتمد زاوية فلكية ناعمة وهي 15° تحت الأفق لبلدان شمال أوروبا لتسهيل العبادات وتقليل المشقة على الجالية المؤمنة في ليالي الصيف القصيرة.
                """.trimIndent()
            ),
            FiqhArticle(
                id = "leufa_tehran",
                title = "2️⃣ مركز لوفا (قم) وجامعة طهران مقابل الكفيل",
                summary = "زاوية 16° لقم وزاوية 17.7° لطهران مع إسقاط خط عرض 45°",
                content = """
• مركز لوفا (قم المقدسة - ID:0): يعتمد زاوية الفجر 16.0° والمغرب زوال الحمرة 4.0° والعشاء 14.0°. صيفاً في الزوايا العليا يسقط الأوقات على خط عرض 45° تماماً.
• جامعة طهران (IGUT - ID:6): تعتمد زاوية الفجر 17.7° والمغرب زوال الحمرة 4.5° والعشاء 14.0°. صيفاً تسقط الحسابات أيضاً على خط عرض 45° تجنباً لانعدام الشفق.
                """.trimIndent()
            ),
            FiqhArticle(
                id = "sayegh",
                title = "3️⃣ مسلك تقويم الصائغ الشيعي الاحترازي صيفاً",
                summary = "الإمساك عند منتصف الليل الفلكي وتأخير الصبح حتى الإسفار",
                content = """
• الإمساك صيفاً: يوجب تقويم الصائغ الإمساك عند منتصف الليل الفلكي صيانةً للصوم ويقين الإمساك قبل أي زيادة للنور غير المحسوس.
• صلاة الصبح: يؤخرها وجوباً حتى حدوث (الإسفار) وهو انتشار الضوء الحسي الملاحظ بالأفق قبل الشروق بحوالي 75 دقيقة (1.25 ساعة) تيقناً لدخول الفجر الصادق الحسي.
                """.trimIndent()
            ),
            FiqhArticle(
                id = "sunni",
                title = "4️⃣ تقويم أم القرى والمذاهب السنية العامة",
                summary = "الفاصل الثابت (90 دقيقة)، قاعدة سُبع الليل، والكسر النسبي",
                content = """
• الفاصل الثابت: فرض وقت الفجر 90 دقيقة ثابتة قبل شروق الشمس، والعشاء 90 دقيقة بعد غروبها (وتزداد تلقائياً في رمضان بتقويم أم القرى إلى 120 دقيقة).
• قاعدة سُبع الليل (1/7): تقسيم الليل التكويني الفعلي من الغروب للشروق إلى سبعة أقسام متساوية، ويحسب الفجر قبل الشروق بسُبع والعشاء بعد الغروب بسُبع.
• الكسر النسبي العشري (Angle-Based Rule): حساب نسبة الزاوية الشرعية للفجر أو العشاء من الـ 60 درجة الكلية.
                """.trimIndent()
            )
        )
    }

    var expandedId by remember { mutableStateOf<String?>("midnight") }

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
                Icon(Icons.Default.BookOpen, contentDescription = null, tint = Color(0xFFFFD54F))
                Text(
                    text = "فقه المواقيت والضوابط الفلكية",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "دراسات فقهية وفلكية مقارنة معتمدة لدى المرجعيات الدينية والمراصد",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.65f)
            )
        }

        items(articles) { article ->
            val isExpanded = (expandedId == article.id)
            val rotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF1A1628),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isExpanded) Color(0xFFFFD54F).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.08f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedId = if (isExpanded) null else article.id },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = article.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isExpanded) Color(0xFFFFD54F) else Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = article.summary,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.rotate(rotation)
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(modifier = Modifier.padding(top = 14.dp)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF100C1C),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = article.content,
                                    fontSize = 12.sp,
                                    lineHeight = 20.sp,
                                    color = Color(0xFFE0E0E0),
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
