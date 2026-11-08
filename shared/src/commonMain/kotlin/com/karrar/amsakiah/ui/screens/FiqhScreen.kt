package com.karrar.amsakiah.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.astronomy.HighLatitudeRule
import com.karrar.amsakiah.core.astronomy.OfflineCities
import com.karrar.amsakiah.core.state.AppState
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.components.SectionTitle
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararTokens

private data class FiqhArticle(val title: String, val body: String)

private val articles = listOf(
    FiqhArticle(
        "📐 المرجع الشامل لجميع معايير الأوقات والزوايا الفلكية المعتمدة (أوفلاين)",
        """فيما يلي تفصيل كافة معايير وزوايا المواقيت المعتمدة في المحرك الفلكي:

• 1. معيار الكفيل (المرجعية العليا في النجف الأشرف - العتبة العباسية):
  - الفجر: 18.0° تحت الأفق الشرقي | الشروق والغروب: 0.833° | المغرب: 4.0° (ذهاب الحمرة المشرقية) | العشاء: 14.0°.
  - معالجة العروض العليا صيفاً: إسقاط أقرب بلد معتدل (خط عرض 48°) أو تقويم الصائغ الاحترازي.

• 2. معهد بحوث ليفا - مدينة قم المقدسة (شيعة اثنا عشري):
  - الفجر: 16.0° تحت الأفق | المغرب: 4.0° (ذهاب الحمرة المشرقية) | العشاء: 14.0°.
  - معالجة العروض العليا صيفاً: إسقاط الإحداثيات على خط عرض 45°.

• 3. معهد الجيوفيزياء - جامعة طهران (IGUT):
  - الفجر: 17.7° تحت الأفق | المغرب: 4.5° (ذهاب الحمرة المشرقية) | العشاء: 14.0°.
  - معالجة العروض العليا صيفاً: إسقاط الإحداثيات على خط عرض 45°.

• 4. مؤسسة الإمام علي عليه السلام - لندن (شيعي لأوروبا):
  - الفجر: 15.0° تحت الأفق | المغرب: 4.0° | العشاء: 15.0° (مع إسقاط خط عرض 48° عند تداخل الشفق).

• 5. تقويم أم القرى - مكة المكرمة:
  - الفجر: 18.5° تحت الأفق | المغرب: عند الغروب الفلكي (0.833°) | العشاء: 90 دقيقة ثابتة بعد المغرب (وتصبح 120 دقيقة تلقائياً في شهر رمضان).

• 6. رابطة العالم الإسلامي (MWL):
  - الفجر: 18.0° تحت الأفق | المغرب: 0.833° | العشاء: 17.0° تحت الأفق الغربي.

• 7. الهيئة المصرية العامة للمساحة:
  - الفجر: 19.5° تحت الأفق | المغرب: 0.833° | العشاء: 17.5° تحت الأفق.

• 8. الجمعية الإسلامية لأمريكا الشمالية (ISNA):
  - الفجر: 15.0° تحت الأفق | المغرب: 0.833° | العشاء: 15.0° تحت الأفق.

• 9. جامعة العلوم الإسلامية بكراتشي:
  - الفجر: 18.0° تحت الأفق | المغرب: 0.833° | العشاء: 18.0° تحت الأفق.

• 10. اتحاد المنظمات الإسلامية في فرنسا (UOIF 12°):
  - الفجر: 12.0° تحت الأفق | المغرب: 0.833° | العشاء: 12.0° تحت الأفق.

• 11. المجلس الأوروبي للإفتاء والبحوث (ECFR):
  - الفجر: 15.0° تحت الأفق | المغرب: 0.833° | العشاء: 15.0° (مع قاعدة الكسر النسبي العشري أو الفاصل الثابت صيفاً)."""
    ),
    FiqhArticle(
        "📌 منتصف الليل الشرعي الشيعي (تحديث فقهي هام)",
        """• المبدأ الفقهي: منتصف الليل الشرعي عند الطائفة الجعفرية هو منتصف المسافة الزمنية الواقعة تماماً بين [غروب الشمس] و[طلوع الفجر الصادق].

• المعادلة الرياضية للبرمجة:
Midnight = Sunset + ((Fajr - Sunset) / 2)

• تلافي الأخطاء السابقة: تم تصحيح هذه الخوارزمية كلياً بالصيف في لندن لتلافي خطأ العبور الحسابي لخانة الصباح (AM). يظهر التوقيت الآن دقيقاً بالمساء (11:20 م) بدلاً من (11:20 ص) بمطابقة تامة للتقاويم المعتمدة عسكرياً وشرعياً."""
    ),
    FiqhArticle(
        "1️⃣ الفرق الفقهي بين الكفيل ومؤسسة لندن (السيستاني) صيفاً",
        """على الرغم من كونهما يتبعان فتاوى المرجع الأعلى السيد السيستاني (دام ظله)، إلا أن هناك اختلافاً بـ 48 دقيقة صيفاً بلندن للسبب التالي:

• معهد الكفيل (العتبة العباسية): يعتمد زاوية الفجر الطبيعية 18° تحت الأفق. صيفاً عند تداخل الشفق، يسقط الإحداثيات على خط عرض 48° (باريس) لمطابقة فتوى لندن لتسهيل العبادات، أو يتيح تقويم الصائغ كمعيار بديل.

• مؤسسة لندن (مؤسسة الإمام علي بلندن): تعتمد زاوية فلكية ناعمة وهي 15° تحت الأفق (وهو المعتمد فقهياً لديهم لبلدان شمال أوروبا لتسهيل العبادات وتقليل المشقة على الجالية).

• لغز الـ 48 دقيقة صيفاً: صيفاً في لندن (خط عرض 51.5°)، تنحدر الشمس بشكل مائل وجانبي جداً موازٍ للأفق، وتتحرك ببطء شديد صعوداً وهبوطاً. هذا التباطؤ يجعل الفارق البسيط بين زاوية 15° وزاوية 18° (3 درجات فقط) يتمدد زمنياً ليصبح 48 دقيقة كاملة! بينما في البلاد القريبة من خط الاستواء (مثل كربلاء ومكة)، يكون انحدار الشمس عمودياً وسريعاً فلا يتجاوز الفارق بينهما 12 إلى 15 دقيقة."""
    ),
    FiqhArticle(
        "2️⃣ مركز لوفا (قم) وجامعة طهران مقابل الكفيل في الزوايا العليا",
        """• مركز لوفا (قم المقدسة): يعتمد زاوية الفجر 16.0° والمغرب زوال الحمرة 4.0°. صيفاً في الزوايا العليا يسقط الأوقات على خط عرض 45° تماماً.

• جامعة طهران: تعتمد زاوية الفجر 17.7° والمغرب زوال الحمرة 4.5°. صيفاً تسقط الحسابات أيضاً على خط عرض 45° تماماً.

• لماذا يختلفون صيفاً؟ إسقاط خط عرض 45° (لوفا وطهران) يختلف تماماً عن إسقاط خط عرض 48° (الكفيل ومؤسسة لندن)، لأن الشمس في خط عرض 45° تنزل لعمق أكبر تحت الأفق مقارنة بخط عرض 48°، مما يجعل الفجر متأخراً والمغرب مبكراً، وهما متطابقان أوفلاين لأنهما يسقطان على نفس دائرة العرض الافتراضية صيفاً في حال تفعيل المحرك."""
    ),
    FiqhArticle(
        "3️⃣ مسلك تقويم الصائغ الشيعي الاحترازي بالتفصيل صيفاً",
        """• الفكرة والفتوى: يعتبر المهندس الصائغ فقهياً وعلمياً أنه بمجرد وصول الشمس لأدنى نقطة انخفاض لها تحت الأفق (منتصف الليل الفلكي) وبداية صعودها التدريجي، يبدأ تزايد النور تكوينياً في الأفق.

• الإمساك صيفاً: يوجب الصائغ الإمساك عند منتصف الليل الفلكي صيانةً للصوم ويقين الإمساك قبل أي زيادة للنور.

• صلاة الصبح: يؤخرها وجوباً حتى حدوث (الإسفار) وهو انتشار الضوء الحسي الملاحظ بالأفق قبل الشروق بحوالي 75 دقيقة، وذلك لعدم تمايز الفجر الصادق الطبيعي في فترة تداخل الشفق بالصيف."""
    ),
    FiqhArticle(
        "4️⃣ تقويم أم القرى والمذاهب السنية (الفاصل الثابت والسبع والكسر النسبي)",
        """تطرح المدارس السنية صيفاً في مناطق الشفق المتداخل الخيارات التالية:

• الفاصل الثابت: وهو فرض وقت الفجر 90 دقيقة ثابتة قبل شروق الشمس، والعشاء 90 دقيقة بعد غروبها، وفي رمضان تزداد تلقائياً في أم القرى إلى 120 دقيقة تيسيراً وتطابقاً مع تقاويم الحرمين الشريفين.

• قاعدة سُبع الليل: تقسيم الليل التكويني الفعلي إلى سبعة أقسام متساوية، واعتبار وقت الفجر الشرعي الاستثنائي في السبع الأخير قبل الشروق.

• الكسر النسبي العشري (Angle-Based Rule): وهو حساب نسبة الزاوية الشرعية للفجر أو العشاء (مثلاً 18 درجة) من الـ 60 درجة الكلية لزاوية دوران الأفق ليلاً.

المعادلة للفجر: Fajr = Sunrise - (Night * (Fajr Angle / 60)).
المعادلة للعشاء: Isha = Sunset + (Night * (Isha Angle / 60)).
وهذا الكسر النسبي يضمن عدم تداخل الأوقات صيفاً وتدرج الأوقات بشكل طبيعي."""
    ),
    FiqhArticle(
        "5️⃣ معاهد أوروبا السنية والشيعية الأخرى (فرنسا 12° وغيرها)",
        """• اتحاد فرنسا (UOIF): يعتمد زاوية 12° دائماً للفجر والعشاء صيفاً وشتاءً لتسهيل العبادات ومنع تعطل الحسابات أو المشقة على المسلمين.

• المجلس الأوروبي للإفتاء والبحوث: يعتمد زاوية 15° للفجر والعشاء، وصيفاً يطبق الكسر النسبي العشري في المناطق المرتفعة.

• الهيئة المصرية العامة للمساحة: تعتمد زاوية الفجر الكبرى 19.5° وهي تسبب تعطلاً فلكياً صيفاً في أوروبا، فيلجأ التطبيق تلقائياً لخفض الزاوية تدريجياً وبشكل آمن لمنع تعطل الحسابات."""
    ),
    FiqhArticle(
        "6️⃣ هل هذه الاستثناءات تُطبق صيفاً فقط؟ (الموسمية الفلكية)",
        """نعم، تُطبق هذه القواعد الاستثنائية للزوايا العليا فقط في أواخر الربيع وفصل الصيف (عندما يكون ميل الشمس موجباً ومرتفعاً). أما في الخريف والشتاء، فإن الشمس تهبط ليلاً تحت الأفق بأعماق كافية تزيد عن 20 درجة، وبالتالي تعود علامات الفجر والعشاء الصادقة للظهور بوضوح فلكي دامس، وعليه تُلغى الاستثناءات تلقائياً في الشتاء وتُطبق الزوايا الطبيعية لتوفر العلامات التكوينية الفلكية."""
    ),
    FiqhArticle(
        "مذهب العصر: الشافعي والحنفي",
        "وقت العصر عند المذهب الشافعي (والجعفري في المشهور) يكون عندما يصبح ظل الشيء مثله بعد ظل الزوال، أما عند الحنفي فيكون عندما يصبح ظل الشيء مثليه، فيتأخر العصر نحو ساعة تقريبًا."
    )
)

@Composable
fun FiqhScreen(state: AppState, contentPadding: PaddingValues) {
    val settings = state.settings
    val times = remember(settings) {
        PrayerCalculator.timesFor(settings, PrayerCalculator.todayDate(settings))
    }
    var expandedArticle by remember { mutableStateOf<Int?>(null) }

    // عمود قراءة مُوسّط على الشاشات العريضة (تابلت/حاسوب) بعرض مريح للنص الطويل.
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = KararTokens.Size.readingMaxWidth)
                .fillMaxHeight(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        if (times.isHighLatAnomaly) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    corner = 20.dp,
                    color = Color(0x33FF9800),
                    borderColor = Color(0x66FFB74D)
                ) {
                    Text("⚠️ تنبيه فلكي", color = Color(0xFFFFB74D), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        "خط عرض منطقتك أعلى من 47.5° وشمس الصيف لا تغيب بما يكفي لظهور الفجر أو العشاء فلكيًا. " +
                            "يُطبَّق حاليًا: ${PrayerCalculator.highLatFor(settings.highLatRule).titleAr}",
                        color = KararColors.TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        item {
            SectionTitle("طرق الحساب وزواياها", icon = null)
        }

        items(OfflineCities.methodsMatrix.size) { index ->
            val method = OfflineCities.methodsMatrix[index]
            val selected = method.code == settings.methodCode
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { state.update { it.copy(methodCode = method.code) } },
                corner = 18.dp,
                color = if (selected) Color(0x1AFFD700) else KararColors.CardSoft,
                borderColor = if (selected) KararColors.Gold else KararColors.Border,
                contentPadding = PaddingValues(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            method.name,
                            color = if (selected) KararColors.Gold else KararColors.TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "فجر ${method.fajr}° • مغرب ${if (method.maghribType == 0) "${method.maghribVal}°" else "${method.maghribVal.toInt()} د"} • " +
                                "عشاء ${if (method.ishaType == 0) "${method.ishaVal}°" else "${method.ishaVal.toInt()} د"}",
                            color = KararColors.TextMuted,
                            fontSize = 10.sp
                        )
                    }
                    if (method.isShia) {
                        Box(
                            Modifier
                                .background(Color(0x334CAF50), RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("جعفري", color = KararColors.Success, fontSize = 9.sp)
                        }
                    }
                }
            }
        }

        item { SectionTitle("مذهب العصر") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("shafii" to "الشافعي / الجعفري", "hanafi" to "الحنفي").forEach { (code, label) ->
                    val selected = settings.asrMethod == code
                    Box(
                        Modifier
                            .background(if (selected) KararColors.Gold else Color(0x14FFFFFF), RoundedCornerShape(50))
                            .clickable { state.update { it.copy(asrMethod = code) } }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            label,
                            color = if (selected) Color(0xFF241A00) else KararColors.TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item { SectionTitle("قواعد خطوط العرض العليا") }
        items(HighLatitudeRule.entries.size) { index ->
            val rule = HighLatitudeRule.entries[index]
            val selected = rule.code == settings.highLatRule
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(
                        if (selected) Color(0x1AFFD700) else Color(0x0DFFFFFF),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { state.update { it.copy(highLatRule = rule.code) } }
                    .padding(12.dp)
            ) {
                Text(
                    rule.titleAr,
                    color = if (selected) KararColors.Gold else KararColors.TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        item { SectionTitle("الفقه والمعايير") }
        items(articles.size) { index ->
            val article = articles[index]
            val expanded = expandedArticle == index
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedArticle = if (expanded) null else index },
                corner = 18.dp
            ) {
                // رأس المقال: العنوان + سهم يدور عند الفتح
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        article.title,
                        color = KararColors.Gold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Filled.ExpandMore,
                        contentDescription = if (expanded) "إخفاء المقال" else "عرض المقال",
                        tint = KararColors.Gold,
                        modifier = Modifier
                            .size(18.dp)
                            .rotate(if (expanded) 180f else 0f)
                    )
                }
                AnimatedVisibility(visible = expanded) {
                    Column {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            article.body,
                            color = KararColors.TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
    }
}
