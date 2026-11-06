package com.karrar.amsakiah.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

private data class FiqhArticle(val title: String, val body: String)

private val articles = listOf(
    FiqhArticle(
        "منتصف الليل الشرعي عند الشيعة",
        "منتصف الليل الشرعي هو منتصف ما بين غروب الشمس (الغروب الفيزيائي) وطلوع الفجر الصادق، وليس منتصف الساعة 12:00. " +
            "ولهذا يختلف وقت منتصف الليل صيفًا وشتاءً، ويُحسب في هذا التطبيق أوفلاين من فرق الغروب عن الفجر."
    ),
    FiqhArticle(
        "معيار الكفيل (18° / 4°)",
        "يعتمد معيار الكفيل على زاوية 18 درجة للفجر وزاوية 4 درجات للمغرب، وهو المعيار المعتمد في العتبة الحسينية المقدسة " +
            "ومرجعية النجف الأشرف، ويُستخدم افتراضيًا في هذا التطبيق لمدينة كربلاء المقدسة."
    ),
    FiqhArticle(
        "ليه/لندن والزوايا الشيعية الأخرى",
        "يستخدم معهد ليفا في قم زاوية 16° للفجر و4° للمغرب و14° للعشاء، بينما تستخدم مؤسسة الإمام علي في لندن 15° للفجر و4° للمغرب. " +
            "الفروق مقصودة بحسب خط العرض والمناخ المحلي لكل مؤسسة."
    ),
    FiqhArticle(
        "قواعد خطوط العرض العليا",
        "في البلدان التي يتجاوز عرضها 47.5° قد لا تنخفض الشمس تحت الزاوية المطلوبة صيفًا، فلا يوجد فجر أو عشاء فلكي حقيقي. " +
            "الحلول الفقهية: قاعدة الصائغ (المنتصف الفلكي)، أو أقرب بلد 45°/48°، أو سُبع الليل، أو زمن ثابت 90 دقيقة."
    ),
    FiqhArticle(
        "مذهب العصر: الشافعي والحنفي",
        "وقت العصر عند المذهب الشافعي (والجعفري في المشهور) يكون عندما يصبح ظل الشيء مثله بعد ظل الزوال، " +
            "أما عند الحنفي فيكون عندما يصبح ظل الشيء مثليه، فيتأخر العصر نحو ساعة تقريبًا."
    )
)

@Composable
fun FiqhScreen(state: AppState, contentPadding: PaddingValues) {
    val settings = state.settings
    val times = remember(settings) {
        PrayerCalculator.timesFor(settings, PrayerCalculator.todayDate(settings))
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
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
            GlassCard(modifier = Modifier.fillMaxWidth(), corner = 18.dp) {
                Text(article.title, color = KararColors.Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(article.body, color = KararColors.TextSecondary, fontSize = 12.sp, lineHeight = 22.sp)
            }
        }
    }
}
