package com.karrar.amsakiah.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.data.TasbihPresets
import com.karrar.amsakiah.ui.state.AppState
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.core.state.PrayerMeta
import com.karrar.amsakiah.core.widgets.WidgetPinBridge
import com.karrar.amsakiah.ui.adaptive.ScreenHorizontalMargin
import com.karrar.amsakiah.ui.adaptive.contentEnd
import com.karrar.amsakiah.ui.adaptive.contentStart
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.components.SectionTitle
import com.karrar.amsakiah.ui.theme.KararColors
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt

private data class WidgetDef(
    val key: String,
    val title: String,
    val size: String,
    val description: String,
    val accent: Color
)

private val widgetDefs = listOf(
    WidgetDef(
        WidgetPinBridge.TODAY,
        "ودجت اليوم",
        "2×2 ← 4×5",
        "الكشيدة الهجرية، العدّاد الحي، شريط التقدم البيضوي، ومربعات الصلوات الست. يتكيّف من مربع صغير إلى لوحة كاملة.",
        Color(0xFFFFD700)
    ),
    WidgetDef(
        WidgetPinBridge.TIMES,
        "ودجت المواقيت",
        "2×2 ← 4×4",
        "مربعات الصلوات بأزمنتها مع إبراز الصلاة القادمة، وعدّاد حي مضغوط في الشكل العريض.",
        Color(0xFF38BDF8)
    ),
    WidgetDef(
        WidgetPinBridge.COUNTDOWN,
        "ودجت العدّاد",
        "2×2 ← 4×2",
        "الوقت المتبقي بخط كبير، شريط تقدم الفترة، ووضع «الجزيرة» الديناميكي عند الشكل العريض.",
        Color(0xFFA78BFA)
    ),
    WidgetDef(
        WidgetPinBridge.HIJRI,
        "ودجت الهجري",
        "2×2 ← 4×2",
        "اليوم والشهر الهجري مع التاريخ الميلادي، ونسبة تقدّم النهار، والمدينة.",
        Color(0xFF4ADE80)
    ),
    WidgetDef(
        WidgetPinBridge.TASBIH,
        "ودجت المسبحة",
        "2×2 ← 4×2",
        "عداد التسبيح مع الهدف والدورات — اضغط الودجت للتسبيح فورًا، مع زر تصفير وإعدادات.",
        Color(0xFF38BDF8)
    ),
    WidgetDef(
        WidgetPinBridge.CLOCK,
        "ودجت الساعة",
        "2×2 ← 4×2",
        "الساعة المفرغة العمقية: وقت حي عبر TextClock الأصلي، اسم اليوم والتاريخ الميلادي.",
        Color(0xFFE2E8F0)
    ),
    WidgetDef(
        WidgetPinBridge.JOURNEY,
        "ودجت المسار",
        "2×3 ← 4×3",
        "مسار الصلوات العمودي: نقطة مضيئة للصلاة القادمة وخط زمني لكل اليوم.",
        Color(0xFFF472B6)
    ),
    WidgetDef(
        WidgetPinBridge.DAILYDEEDS,
        "ودجت أعمال اليوم",
        "2×2 ← 4×2",
        "أعمال الليلة/اليوم/الأسبوع وأعمال الشهر العامة — تُحدَّث تلقائيًا من «مفاتيح الجنان».",
        Color(0xFFA78BFA)
    )
)

private val styleDefs = listOf(
    Triple(WidgetPinBridge.STYLE_ARC, "ألوان اليوم", Color(0xFF274070)),
    Triple(WidgetPinBridge.STYLE_DEEP, "ليلي", Color(0xF2050A15)),
    Triple(WidgetPinBridge.STYLE_MATERIAL, "مادة", Color(0xFF0E1626)),
    Triple(WidgetPinBridge.STYLE_GLASS, "زجاجي", Color(0xCC1B2438)),
    Triple(WidgetPinBridge.STYLE_BLUR, "ضبابي", Color(0xD91B2438)),
    Triple(WidgetPinBridge.STYLE_TRANSPARENT, "شفاف", Color(0x33000000))
)

@Composable
fun WidgetsScreen(state: AppState, contentPadding: PaddingValues) {
    val settings = state.settings
    var style by remember { mutableStateOf(WidgetPinBridge.currentStyle()) }
    var message by remember { mutableStateOf<String?>(null) }

    val zone = remember(settings.timeZoneId) { PrayerCalculator.zoneOf(settings) }
    val nowLdt = remember(state.now, zone) { state.now.toLocalDateTime(zone) }
    val nowSeconds = PrayerCalculator.secondsOfDay(nowLdt)
    val date = nowLdt.date
    val hijri = remember(settings, date) { PrayerCalculator.hijriFor(settings, date) }
    val times = remember(settings, date) {
        PrayerCalculator.adjustedTimes(PrayerCalculator.timesFor(settings, date), settings)
    }
    val widgetPrayers = listOf("imsak", "fajr", "dhuhr", "asr", "maghrib", "isha")
    val entries = remember(times) {
        widgetPrayers.mapNotNull { id ->
            val raw = times[id] ?: return@mapNotNull null
            val (t12, ap, _) = PrayerCalculator.formatHour(raw, settings.use24Hour)
            Triple(id, PrayerMeta.label(id), "$t12 $ap")
        }
    }
    val nextId = entries.firstOrNull { (id, _, _) ->
        ((times[id] ?: 0.0) * 3600.0) > nowSeconds
    }?.first ?: entries.firstOrNull()?.first ?: "fajr"
    val nextLabel = PrayerMeta.label(nextId)
    val nextTime = times[nextId] ?: 0.0
    val remaining = (((nextTime * 3600.0) - nowSeconds).toInt()).coerceAtLeast(0)
    val countdown = PrayerCalculator.countdownText(remaining)
    val progress = run {
        val previous = entries.mapNotNull { (id, _, _) -> times[id] }.lastOrNull { it * 3600.0 <= nowSeconds }
            ?: 0.0
        val start = previous * 3600.0
        val end = if (nextTime * 3600.0 <= nowSeconds) nextTime * 3600.0 + 86400.0 else nextTime * 3600.0
        ((nowSeconds - start) / (end - start).coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)
    }
    val dhikrList = TasbihPresets.combined(settings.customDhikrs)
    val dhikrIndex = TasbihPresets.safeIndex(settings.tasbihDhikrIndex, dhikrList.size)
    val dhikr = dhikrList[dhikrIndex]
    val tasbihProgress = if (settings.tasbihTarget > 0) {
        (settings.tasbihCount.toFloat() / settings.tasbihTarget).coerceIn(0f, 1f)
    } else 0f

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = contentPadding.contentStart(ScreenHorizontalMargin),
            end = contentPadding.contentEnd(ScreenHorizontalMargin),
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 12.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), corner = 24.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(KararColors.Gold.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Widgets, contentDescription = null, tint = KararColors.Gold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("ودجتات كرار", color = KararColors.Gold, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "سبعة أشكال متكيّفة الحجم — العدّاد وشريط التقدم يعملان بعقل الودجت وساعة الجهاز",
                            color = KararColors.TextMuted,
                            fontSize = 10.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "كل ودجت تتكيّف تلقائيًا مع حجمها على الشاشة: نسخة مصغّرة 2×2، متوسطة 4×3، " +
                        "وكبيرة 4×4/4×5. اضغط مطوّلًا على أي ودجت في الشاشة الرئيسية ← «تعديل» لتغيير خلفيته، " +
                        "أو استخدم زر ⚙ داخل الودجت.",
                    color = KararColors.TextSecondary,
                    fontSize = 10.sp,
                    lineHeight = 16.sp
                )
            }
        }

        item { SectionTitle("خلفية الودجتات", icon = Icons.Filled.Tune) }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), corner = 20.dp, contentPadding = PaddingValues(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    styleDefs.forEach { (key, label, _) ->
                        StyleChip(
                            label = label,
                            key = key,
                            current = style,
                            modifier = Modifier.weight(1f)
                        ) { selected ->
                            style = selected
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = KararColors.Gold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "المادة صلبة، الزجاجي شبه شفاف بحدود لامعة، الضبابي يحاكي Blur (RemoteViews لا يدعم ضبابًا حقيقيًا)، " +
                            "والشفاف يُظهر خلفية الشاشة. الليلي هو هوية كرار الأصلية.",
                        color = KararColors.TextMuted,
                        fontSize = 9.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        item { SectionTitle("أشكال الودجت (${widgetDefs.size})", icon = Icons.Filled.Widgets) }
        items(widgetDefs, key = { it.key }) { def ->
            GlassCard(modifier = Modifier.fillMaxWidth(), corner = 22.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(def.accent)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        def.title,
                        color = KararColors.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(def.accent.copy(alpha = 0.14f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(def.size, color = def.accent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(def.description, color = KararColors.TextMuted, fontSize = 10.sp, lineHeight = 15.sp)
                Spacer(Modifier.height(10.dp))

                WidgetPreviewFrame(style = style, accent = def.accent) {
                    when (def.key) {
                        WidgetPinBridge.TODAY -> TodayPreview(
                            hijriDay = hijri.day,
                            hijriMonth = hijri.monthName,
                            gregorian = PrayerCalculator.formattedGregorian(date),
                            nextLabel = nextLabel,
                            countdown = countdown,
                            entries = entries,
                            progress = progress
                        )
                        WidgetPinBridge.TIMES -> TimesPreview(
                            city = settings.cityName,
                            date = PrayerCalculator.formattedGregorian(date),
                            entries = entries,
                            nextLabel = nextLabel,
                            countdown = countdown
                        )
                        WidgetPinBridge.COUNTDOWN -> CountdownPreview(
                            hijriDay = hijri.day,
                            hijriMonth = hijri.monthName,
                            nextLabel = nextLabel,
                            nextTime = nextTime,
                            countdown = countdown,
                            progress = progress
                        )
                        WidgetPinBridge.HIJRI -> HijriPreview(
                            hijriDay = hijri.day,
                            hijriMonth = hijri.monthName,
                            hijriYear = hijri.year,
                            gregorian = PrayerCalculator.formattedGregorian(date),
                            city = settings.cityName
                        )
                        WidgetPinBridge.TASBIH -> TasbihPreview(
                            dhikr = dhikr.text,
                            count = settings.tasbihCount,
                            target = settings.tasbihTarget,
                            rounds = settings.tasbihRound,
                            progress = tasbihProgress
                        )
                        WidgetPinBridge.CLOCK -> ClockPreview(
                            clock = "${nowLdt.hour.toString().padStart(2, '0')}:${nowLdt.minute.toString().padStart(2, '0')}",
                            gregorian = PrayerCalculator.formattedGregorian(date)
                        )
                        WidgetPinBridge.DAILYDEEDS -> DailyDeedsPreview(
                            hijriDay = hijri.day,
                            hijriMonth = hijri.monthName
                        )
                        else -> JourneyPreview(
                            entries = entries,
                            nextLabel = nextLabel,
                            countdown = countdown
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(def.accent)
                            .clickable {
                                val ok = WidgetPinBridge.requestPin(def.key)
                                message = if (ok) {
                                    "تم فتح نافذة إضافة «${def.title}» — أكّد الإضافة من الشاشة الرئيسية."
                                } else {
                                    "افتح الشاشة الرئيسية مطوّلًا على مكان فارغ ← «ودجتات» ← «${def.title}»."
                                }
                            }
                            .padding(vertical = 9.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Color(0xFF0B1220), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("إضافة للشاشة", color = Color(0xFF0B1220), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(KararColors.CardSoft)
                            .clickable {
                                val ok = WidgetPinBridge.openWidgetSettings()
                                message = if (ok) {
                                    "اختر الخلفية من شاشة إعدادات الودجت."
                                } else {
                                    "إعدادات الودجت غير متاحة على هذا النظام."
                                }
                            }
                            .padding(vertical = 9.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Tune, contentDescription = null, tint = KararColors.TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("الخلفية", color = KararColors.TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        if (message != null) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    corner = 16.dp,
                    color = KararColors.Teal.copy(alpha = 0.10f),
                    borderColor = KararColors.Teal.copy(alpha = 0.28f),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Text(message ?: "", color = KararColors.Teal, fontSize = 11.sp, lineHeight = 16.sp)
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), corner = 20.dp) {
                Text(
                    "كيف يعمل العدّاد وشريط التقدم؟",
                    color = KararColors.Gold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "العدّاد داخل الودجت Chronometer أصلي محسوب من SystemClock.elapsedRealtime — " +
                        "يعدّ بنفسه كل ثانية دون أي خدمة أو بث، ويصحّح نفسه عند تغيير الوقت أو إعادة التشغيل. " +
                        "شريط التقدم ProgressBar أصلي يتحدّث عند تغيّر الصلاة وكل 15 دقيقة عبر WorkManager. " +
                        "أي تغيير في المدينة أو الطريقة أو الأذكار يُحدّث الودجتات والإشعار الحي فورًا، " +
                        "وودجت المسبحة يزيد العداد مباشرة ويحفظه في الإعدادات.",
                    color = KararColors.TextMuted,
                    fontSize = 10.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun StyleChip(
    label: String,
    key: String,
    current: String,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit
) {
    val selected = key == current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) KararColors.Gold else KararColors.CardSoft)
            .border(1.dp, if (selected) Color.Transparent else KararColors.Border, RoundedCornerShape(12.dp))
            .clickable {
                if (WidgetPinBridge.setStyle(key)) onSelect(key)
            }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) Color(0xFF241A00) else KararColors.TextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

// ============================================================================
//  إطار المعاينة + الأشكال السبعة
// ============================================================================
@Composable
private fun WidgetPreviewFrame(
    style: String,
    accent: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    val background = when (style) {
        WidgetPinBridge.STYLE_MATERIAL -> Color(0xFF0E1626)
        WidgetPinBridge.STYLE_GLASS -> Color(0xCC1B2438)
        WidgetPinBridge.STYLE_BLUR -> Color(0xD91B2438)
        WidgetPinBridge.STYLE_TRANSPARENT -> Color(0x33000000)
        WidgetPinBridge.STYLE_ARC -> Color(0xFF1A2A44)
        else -> Color(0xF2050A15)
    }
    val borderColor = when (style) {
        WidgetPinBridge.STYLE_GLASS -> Color(0x66FFFFFF)
        WidgetPinBridge.STYLE_TRANSPARENT -> Color(0x1FFFFFFF)
        else -> accent.copy(alpha = 0.22f)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(background, background.copy(alpha = (background.alpha * 0.88f).coerceAtLeast(0.12f)))
                )
            )
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
private fun PreviewKashida(day: Int, month: String, fontSize: Int = 20) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(month, color = KararColors.Gold, fontSize = (fontSize + 1).sp, fontWeight = FontWeight.Bold)
        Text(" $day ", color = Color.White, fontSize = fontSize.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TodayPreview(
    hijriDay: Int,
    hijriMonth: String,
    gregorian: String,
    nextLabel: String,
    countdown: Triple<String, String, String>,
    entries: List<Triple<String, String, String>>,
    progress: Float
) {
    PreviewKashida(hijriDay, hijriMonth, 24)
    Text(gregorian, color = KararColors.TextMuted, fontSize = 8.sp)
    Spacer(Modifier.height(4.dp))
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(KararColors.CardSoft)
            .padding(horizontal = 12.dp, vertical = 3.dp)
    ) {
        Text("باقي على $nextLabel", color = KararColors.Gold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
    Text(
        "${countdown.first}:${countdown.second}:${countdown.third}",
        color = Color.White,
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold
    )
    PreviewProgress(progress, Color(0xFFFF9F1C))
    Spacer(Modifier.height(6.dp))
    PreviewPrayerGrid(entries, nextLabel)
}

@Composable
private fun TimesPreview(
    city: String,
    date: String,
    entries: List<Triple<String, String, String>>,
    nextLabel: String,
    countdown: Triple<String, String, String>
) {
    Text(city, color = KararColors.Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    Text(date, color = KararColors.TextMuted, fontSize = 8.sp)
    Spacer(Modifier.height(6.dp))
    PreviewPrayerGrid(entries, nextLabel)
    Spacer(Modifier.height(6.dp))
    Text(
        "باقي على $nextLabel • ${countdown.first}:${countdown.second}:${countdown.third}",
        color = Color(0xFF38BDF8),
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun CountdownPreview(
    hijriDay: Int,
    hijriMonth: String,
    nextLabel: String,
    nextTime: Double,
    countdown: Triple<String, String, String>,
    progress: Float
) {
    PreviewKashida(hijriDay, hijriMonth, 18)
    Text("باقي على $nextLabel", color = KararColors.TextSecondary, fontSize = 9.sp)
    Text(
        "${countdown.first}:${countdown.second}:${countdown.third}",
        color = Color(0xFFFFD700),
        fontSize = 38.sp,
        fontWeight = FontWeight.Bold
    )
    val (t12, ap, _) = PrayerCalculator.formatHour(nextTime, false)
    Text("موعد $t12 $ap", color = KararColors.TextMuted, fontSize = 8.sp)
    Spacer(Modifier.height(6.dp))
    PreviewProgress(progress, Color(0xFFA78BFA))
}

@Composable
private fun HijriPreview(
    hijriDay: Int,
    hijriMonth: String,
    hijriYear: Int,
    gregorian: String,
    city: String
) {
    PreviewKashida(hijriDay, hijriMonth, 16)
    Text("$hijriYear هـ", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
    Text(gregorian, color = KararColors.TextMuted, fontSize = 8.sp, textAlign = TextAlign.Center)
    Spacer(Modifier.height(4.dp))
    Text("$city • 38٪ من النهار", color = Color(0xFF4ADE80), fontSize = 8.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun TasbihPreview(dhikr: String, count: Int, target: Int, rounds: Int, progress: Float) {
    Text(dhikr, color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    Spacer(Modifier.height(4.dp))
    Box(
        modifier = Modifier
            .size(86.dp)
            .clip(CircleShape)
            .background(Color(0xFF0F172A))
            .border(4.dp, Color(0xFF38BDF8), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text("$count", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
    }
    Spacer(Modifier.height(4.dp))
    Text("من $target • $rounds دورة", color = Color(0xFF94A3B8), fontSize = 9.sp)
    Spacer(Modifier.height(5.dp))
    PreviewProgress(progress, Color(0xFF38BDF8))
}

@Composable
private fun ClockPreview(clock: String, gregorian: String) {
    Text(clock, color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)
    Text(gregorian, color = Color(0xFFB9C0D4), fontSize = 9.sp, textAlign = TextAlign.Center)
}

@Composable
private fun DailyDeedsPreview(hijriDay: Int, hijriMonth: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("أعمال اليوم", color = Color(0xFFA78BFA), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Text("$hijriDay $hijriMonth", color = Color(0xFF94A3B8), fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
    Spacer(Modifier.height(5.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        listOf("الليلة", "اليوم", "الأسبوع", "الشهر").forEach { label ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x33000000))
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("4", color = KararColors.Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(label, color = Color(0xFF94A3B8), fontSize = 6.sp)
            }
        }
    }
    Spacer(Modifier.height(5.dp))
    listOf("دُعاء يوم الثلاثاء", "أعمال نهار الجُمعة", "أعمال هذه الليلة").forEach { title ->
        Row(Modifier.fillMaxWidth().padding(vertical = 1.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(4.dp).height(11.dp).clip(RoundedCornerShape(2.dp)).background(KararColors.Gold.copy(alpha = 0.85f)))
            Spacer(Modifier.width(5.dp))
            Text(title, color = Color(0xFFE2E8F0), fontSize = 7.sp, maxLines = 1)
        }
    }
}

@Composable
private fun JourneyPreview(
    entries: List<Triple<String, String, String>>,
    nextLabel: String,
    countdown: Triple<String, String, String>
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("مسار اليوم", color = Color(0xFFF472B6), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Text("${countdown.first}:${countdown.second}:${countdown.third}", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
    Spacer(Modifier.height(6.dp))
    entries.take(4).forEach { (_, label, time) ->
        val active = label == nextLabel
        Row(
            Modifier.fillMaxWidth().padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (active) Color(0xFFF472B6) else Color(0x33FFFFFF))
            )
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                color = if (active) Color(0xFFF472B6) else Color(0xFFE2E8F0),
                fontSize = 9.sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            Text(time, color = if (active) Color(0xFFFFD700) else Color(0xFF94A3B8), fontSize = 9.sp)
        }
    }
}

@Composable
private fun PreviewProgress(progress: Float, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(7.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0x66000000))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(7.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

@Composable
private fun PreviewPrayerGrid(entries: List<Triple<String, String, String>>, nextLabel: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        entries.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { (_, label, time) ->
                    val active = label == nextLabel
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (active) Color(0x26FFB432) else Color(0x33000000))
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            label,
                            color = if (active) KararColors.Gold else KararColors.TextSecondary,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            time.substringBefore(" "),
                            color = if (active) Color(0xFFFAFAD2) else Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                repeat((3 - row.size).coerceAtLeast(0)) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
