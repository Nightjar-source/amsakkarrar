package com.karrar.amsakiah.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.astronomy.CityDatabase
import com.karrar.amsakiah.core.astronomy.OfflineCities
import com.karrar.amsakiah.core.astronomy.OfflineCity
import com.karrar.amsakiah.core.audio.SoundTestBridge
import com.karrar.amsakiah.core.permissions.rememberPermissionController
import com.karrar.amsakiah.core.settings.AppSettings
import com.karrar.amsakiah.core.settings.ThemeMode
import com.karrar.amsakiah.core.state.AppState
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.core.state.PrayerMeta
import com.karrar.amsakiah.ui.adaptive.ScreenHorizontalMargin
import com.karrar.amsakiah.ui.adaptive.contentEnd
import com.karrar.amsakiah.ui.adaptive.contentStart
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.components.MergedAccordion
import com.karrar.amsakiah.ui.components.SpringSheet
import com.karrar.amsakiah.ui.motion.motionEffects
import com.karrar.amsakiah.ui.motion.motionSpatial
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

/**
 * شاشة «الضبط والأوقات» — دمج ضبط الكفيل وV7 في أقسام منسدلة M3 Expressive.
 *
 * كل إعداد يُحفظ فورًا في `AppSettings`، فتنعكس قيمته مباشرة على شاشة اليوم
 * والتقويم والودجت والإشعار (عبر `visibleIds` / `liveIds`).
 */
@Composable
fun SettingsScreen(
    state: AppState,
    contentPadding: PaddingValues,
    onOpenWidgets: () -> Unit = {}
) {
    val settings = state.settings
    val scope = rememberCoroutineScope()
    // مواقيت اليوم — تُستخدم في الشبكة اليومية للمعاينة وتنبيه خطوط العرض العليا.
    val today = remember(settings) { PrayerCalculator.todayDate(settings) }
    val times = remember(settings, today) { PrayerCalculator.timesFor(settings, today) }
    val permissions = rememberPermissionController()
    // عندما يعود المستخدم من إعدادات النظام نحدّث حالة الصلاحيات فورًا —
    // فتعرف الشاشة إن كانت الصلاحيات صارت مكتملة دون النقر على «تحديث».
    LifecycleResumeEffect(Unit) {
        permissions.refresh()
        onPauseOrDispose {}
    }
    var showCityPicker by remember { mutableStateOf(false) }
    // طلب صلاحية الموقع من النظام أولًا، ثم المتابعة تلقائيًا بعد الموافقة
    var gpsAfterGrant by remember { mutableStateOf(false) }
    LaunchedEffect(gpsAfterGrant, permissions.status.location) {
        if (gpsAfterGrant && permissions.status.location) {
            gpsAfterGrant = false
            state.detectLocation()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = contentPadding.contentStart(ScreenHorizontalMargin),
            end = contentPadding.contentEnd(ScreenHorizontalMargin),
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        // ==================== تنبيه خطوط العرض العليا (مرصد الكفيل 3D) ====================
        if (times.isHighLatAnomaly) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    corner = 22.dp,
                    color = KararColors.Amber.copy(alpha = 0.16f),
                    borderColor = KararColors.Amber.copy(alpha = 0.45f),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = KararColors.Amber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "تنبيه فلكي: خطوط العرض العليا (زاوية استثنائية صيفية)",
                                color = KararColors.Amber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "منطقتك (${kotlin.math.abs(settings.latitude).toInt()}°) فوق خط عرض 47.5° في فصل الصيف الفلكي (تداخل الشفق). تم تفعيل قانون «${PrayerCalculator.highLatFor(settings.highLatRule).titleAr}» لضبط الصلوات والحد من اضطراب أوقات الفجر والعشاء.",
                                color = KararColors.TextPrimary,
                                fontSize = 10.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // ==================== ترويسة الضبط ====================
        item {
            SettingsHero(
                state = state,
                onSearchCity = { showCityPicker = true },
                onGps = {
                    if (permissions.status.location) {
                        scope.launch { state.detectLocation() }
                    } else {
                        gpsAfterGrant = true
                        permissions.requestLocation()
                    }
                },
                onIp = { scope.launch { state.detectLocationByIp() } },
                onSelectMethod = { code ->
                    state.update {
                        it.copy(methodCode = code, highLatRule = PrayerCalculator.autoHighLatForMethod(code))
                    }
                }
            )
        }

        // ==================== الموقع والمدينة ====================
        item {
            SettingsAccordion(
                title = "الموقع والمدينة",
                icon = Icons.Filled.LocationOn,
                initiallyOpen = true
            ) {
                // بلا بطاقة داخلية: المحتوى جزء من النافذة نفسها (العنوان مدمج).
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${settings.cityName}، ${settings.countryName}",
                            color = KararColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            when {
                                settings.useGps -> CityDatabase.proximityLabel(state.nearby).ifBlank { "محدّد عبر GPS" }
                                settings.locationSource == "ip" -> "استشعار تلقائي عبر IP — ${settings.latitude.round1()} , ${settings.longitude.round1()}"
                                else -> "اختيار يدوي — ${settings.latitude.round1()} , ${settings.longitude.round1()}"
                            },
                            color = KararColors.TextMuted,
                            fontSize = 10.sp
                        )
                        if (state.lastLocationError != null) {
                            Text(state.lastLocationError ?: "", color = KararColors.Danger, fontSize = 10.sp)
                        }
                        if (state.locationServiceDisabled) {
                            Spacer(Modifier.height(6.dp))
                            SmallAction("تشغيل خدمة الموقع") { state.openSystemLocationSettings() }
                        }
                    }
                    SmallAction("تغيير") { showCityPicker = true }
                }
                DstModeSelector(
                    current = settings.dstMode,
                    timeZoneId = settings.timeZoneId,
                    onChange = { mode -> state.update { it.copy(dstMode = mode) } }
                )
            }
        }

        // ==================== مواقيت اليوم ====================
        item {
            SettingsAccordion(
                title = "مواقيت الصلاة اليومية",
                icon = Icons.Filled.AccessTime,
                initiallyOpen = true
            ) {
                DailyTimesGrid(settings = settings)
            }
        }

        // ==================== الحساب ====================
        item {
            SettingsAccordion(title = "طريقة الحساب", icon = Icons.Filled.Calculate) {
                GlassCard(modifier = Modifier.fillMaxWidth(), corner = 16.dp, contentPadding = PaddingValues(12.dp)) {
                    Text("الطريقة الحالية", color = KararColors.TextMuted, fontSize = 10.sp)
                    Text(
                        OfflineCities.methodsMatrix.firstOrNull { it.code == settings.methodCode }?.name ?: "",
                        color = KararColors.Gold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    MethodSelector(
                        selected = settings.methodCode,
                        onSelect = { code ->
                            state.update {
                                it.copy(methodCode = code, highLatRule = PrayerCalculator.autoHighLatForMethod(code))
                            }
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "قاعدة خطوط العرض الحالية: ${PrayerCalculator.highLatFor(settings.highLatRule).titleAr}",
                        color = KararColors.TextMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        "للتفاصيل الفقهية والزوايا والقواعد مع المرجع راجع شاشة «فقه المواقيت» (يختصرها نفس التقسيم).",
                        color = KararColors.TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // ==================== التاريخ الهجري ====================
        item {
            SettingsAccordion(title = "التاريخ الهجري", icon = Icons.Filled.DateRange) {
                StepperRow(
                    title = "تقديم/تأخير الشهر",
                    value = settings.hijriOffset,
                    onChange = { state.update { s -> s.copy(hijriOffset = it.coerceIn(-3, 3)) } }
                )
            }
        }

        // ==================== تعديل الأوقات ====================
        item {
            SettingsAccordion(title = "تعديل أوقات الصلاة (±60 دقيقة)", icon = Icons.Filled.Tune) {
                OffsetsGrid(
                    settings = settings,
                    onChange = { id, value ->
                        state.update { s ->
                            s.copy(offsets = s.offsets + (id to value.coerceIn(-60, 60)))
                        }
                    }
                )
            }
        }

        // ==================== الأوقات الظاهرة ====================
        item {
            SettingsAccordion(title = "الأوقات الظاهرة ومربعات الصلوات", icon = Icons.Filled.SettingsSuggest) {
                PrayerSelectionCard(
                    title = "مربعات الصلوات في الشاشة السفلية والتقويم",
                    subtitle = "العصر والعشاء اختياريان، والغروب يظهر في المعيار الشيعي فقط.",
                    selected = settings.visiblePrayers,
                    accent = KararColors.Gold,
                    onToggle = { id ->
                        state.update { s ->
                            s.copy(visiblePrayers = s.visiblePrayers.togglePrayer(id, PrayerMeta.order))
                        }
                    },
                    onAll = { state.update { it.copy(visiblePrayers = PrayerMeta.order) } },
                    onNone = { state.update { it.copy(visiblePrayers = emptyList()) } }
                )
                ImsakModeSelector(
                    current = settings.imsakMode,
                    onChange = { mode -> state.update { it.copy(imsakMode = mode) } }
                )
            }
        }

        // ==================== العدّاد ====================
        item {
            SettingsAccordion(title = "الإشعار الحي للعدّاد (اختياري)", icon = Icons.Filled.Notifications) {
                ToggleRow(
                    title = "الإشعار الحي (العدّاد)",
                    subtitle = "اختياري — عدّاد حي بشريط تقدم في شريط الإشعارات، يتحدّث تلقائيًا",
                    checked = settings.liveNotification,
                    onChange = { c -> state.update { it.copy(liveNotification = c) } }
                )
                PrayerSelectionCard(
                    title = "أوقات العدّاد",
                    subtitle = "يدور العدّاد والإشعار الحي حول هذه الأوقات — إلغاء الكل يفرغ العدّاد.",
                    selected = settings.livePrayers,
                    accent = KararColors.Teal,
                    onToggle = { id ->
                        state.update { s ->
                            s.copy(livePrayers = s.livePrayers.togglePrayer(id, PrayerMeta.order))
                        }
                    },
                    onAll = { state.update { it.copy(livePrayers = PrayerMeta.order) } },
                    onNone = { state.update { it.copy(livePrayers = emptyList()) } }
                )
            }
        }

        // ==================== الإشعارات (اثنان فقط، وكلاهما اختياري) ====================
        item {
            SettingsAccordion(title = "إشعار الأذان", icon = Icons.Filled.Notifications) {
                ToggleRow(
                    title = "بطاقة الأذان عند دخول الوقت",
                    subtitle = "اختياري — بطاقة واحدة لكل صلاة، وصوت الأذان مرة واحدة",
                    checked = settings.adhanEnabled,
                    onChange = { c -> state.update { it.copy(adhanEnabled = c) } }
                )
                PrayerSelectionCard(
                    title = "أوقات الأذان",
                    subtitle = "اختر الصلوات التي يُرفع لها الأذان — أو الكل.",
                    selected = settings.adhanPrayers,
                    accent = KararColors.Gold,
                    onToggle = { id ->
                        state.update { s ->
                            s.copy(adhanPrayers = s.adhanPrayers.togglePrayer(id, PrayerMeta.order))
                        }
                    },
                    onAll = { state.update { it.copy(adhanPrayers = PrayerMeta.order) } },
                    onNone = { state.update { it.copy(adhanPrayers = emptyList()) } }
                )
                ToggleRow(
                    title = "الاهتزاز مع الأذان",
                    checked = settings.vibration,
                    onChange = { c -> state.update { it.copy(vibration = c) } }
                )
                GlassCard(modifier = Modifier.fillMaxWidth(), corner = 16.dp, contentPadding = PaddingValues(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("التشخيص والمعاينة", color = KararColors.TextPrimary, fontSize = 13.sp)
                            Text(
                                if (SoundTestBridge.supported) {
                                    "تشغيل معاينة الأذان ومعاينة الإشعار الحي — تُحاكي ما يصلك ساعة التنبيه"
                                } else {
                                    "المعاينة متاحة على أندرويد فقط"
                                },
                                color = KararColors.TextMuted,
                                fontSize = 9.sp,
                                lineHeight = 14.sp
                            )
                        }
                        if (SoundTestBridge.supported) {
                            SmallAction("معاينة الأذان") { SoundTestBridge.playAdhan() }
                            Spacer(Modifier.width(6.dp))
                            SmallAction("معاينة الإشعار") { SoundTestBridge.showLiveNotification() }
                        }
                    }
                }
            }
        }

        // ==================== الودجتات ====================
        item {
            SettingsAccordion(title = "الودجتات", icon = Icons.Filled.Widgets) {
                GlassCard(modifier = Modifier.fillMaxWidth(), corner = 16.dp, contentPadding = PaddingValues(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Widgets,
                            contentDescription = null,
                            tint = KararColors.Gold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("ودجتات الشاشة الرئيسية", color = KararColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "سبعة أشكال متكيّفة: اليوم، المواقيت، العدّاد، الهجري، المسبحة…",
                                color = KararColors.TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x2E38BDF8), RoundedCornerShape(14.dp))
                            .clickable(onClick = onOpenWidgets)
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Widgets,
                            contentDescription = null,
                            tint = KararColors.Teal,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "إضافة ودجت للشاشة",
                            color = KararColors.Teal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // ==================== الطقس والمظهر ====================
        item {
            SettingsAccordion(title = "الطقس والمظهر", icon = Icons.Filled.WbSunny) {
                ThemeModeRow(
                    current = settings.themeMode,
                    onChange = { mode -> state.update { it.copy(themeMode = mode) } }
                )
                ToggleRow(
                    title = "الطقس (يتطلب إنترنت)",
                    subtitle = "تأثيرات خفيفة خلف القوس، ولا تؤثر على المواقيت",
                    checked = settings.showWeather,
                    onChange = { c -> state.update { it.copy(showWeather = c) } }
                )
                StepperRow(
                    title = "حجم الخط %",
                    value = (settings.fontScale * 100).toInt(),
                    step = 5,
                    onChange = { v -> state.update { it.copy(fontScale = (v.coerceIn(80, 140)) / 100f) } }
                )
            }
        }

        // ==================== الصلاحيات ====================
        item {
            SettingsAccordion(title = "الصلاحيات", icon = Icons.Filled.Lock) {
                PermissionRow("الإشعارات", permissions.status.notifications) { permissions.requestNotifications() }
                PermissionRow("الموقع (للقبلة والمدن)", permissions.status.location) { permissions.requestLocation() }
                PermissionRow("المنبهات الدقيقة", permissions.status.exactAlarm) { permissions.requestExactAlarm() }
                PermissionRow("استثناء البطارية", permissions.status.batteryOptimized) { permissions.requestBatteryExemption() }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { permissions.openAppSettings() }
                        .padding(8.dp)
                ) {
                    Text("فتح إعدادات التطبيق في النظام", color = KararColors.Gold, fontSize = 12.sp)
                }
            }
        }

        // ==================== أخرى ====================
        item {
            SettingsAccordion(title = "أخرى", icon = Icons.Filled.Info) {
                GlassCard(modifier = Modifier.fillMaxWidth(), corner = 16.dp, contentPadding = PaddingValues(12.dp)) {
                    Text(
                        "قاعدة المدن الأوفلاين: ${state.cityCount} مدينة حول العالم",
                        color = KararColors.TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "المصدر GeoNames (CC BY 4.0) — تعمل بدون إنترنت، ويتم مطابقة موقعك عبر GPS مع أقرب مدينة تلقائيًا.",
                        color = KararColors.TextMuted,
                        fontSize = 10.sp
                    )
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x1AEF5350))
                        .clickable { state.reset() }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("إعادة تعيين كل الإعدادات", color = KararColors.Danger, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("إمساكية كرار", color = KararColors.Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("الإصدار 1.5.9 — أوفلاين بالكامل", color = KararColors.TextMuted, fontSize = 10.sp)
                    Text("تطوير: كرار", color = KararColors.TextMuted, fontSize = 10.sp)
                }
            }
        }
    }

    CityPickerDialog(visible = showCityPicker, state = state, onDismiss = { showCityPicker = false })
}

// =====================================================================
// ترويسة الضبط (هيرو المدينة والمواقيت)
// =====================================================================

/** الطرق الأربع السريعة في الهيرو — البقية في قسم «طريقة الحساب». */
private val HERO_METHODS = listOf(
    "alkafeel" to "الكفيل",
    "leufa" to "ليفا (قم)",
    "tehran" to "طهران",
    "ummalqura" to "أم القرى"
)

@Composable
private fun SettingsHero(
    state: AppState,
    onSearchCity: () -> Unit,
    onGps: () -> Unit,
    onIp: () -> Unit,
    onSelectMethod: (String) -> Unit
) {
    val settings = state.settings
    val today = PrayerCalculator.todayDate(settings)
    val hijri = remember(settings, today) { PrayerCalculator.hijriFor(settings, today) }

    // ساعة حيّة تتبع إزاحة المنطقة المختارة (بما فيها الوضع الصيفي/الشتوي الإجباري).
    val offsetHours = remember(settings) { PrayerCalculator.offsetHours(settings) }
    val clockText = remember(state.now, offsetHours) {
        val local = state.now
            .plus((offsetHours * 3600).toInt().seconds)
            .toLocalDateTime(TimeZone.UTC)
        fun two(v: Int) = v.toString().padStart(2, '0')
        "${two(local.hour)}:${two(local.minute)}:${two(local.second)}"
    }
    val whole = offsetHours.toInt()
    val utcLabel = if (offsetHours == whole.toDouble()) {
        "UTC${if (whole >= 0) "+" else ""}$whole"
    } else {
        "UTC${if (offsetHours >= 0) "+" else ""}${(offsetHours * 10).roundToInt() / 10.0}"
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        corner = 28.dp,
        color = Color(0x08FFFFFF),
        borderColor = Color(0x0DFFFFFF),
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.AccessTime,
                        contentDescription = null,
                        tint = KararTokens.Accent.teal,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "مرصد الكفيل 3D",
                        style = TextStyle(
                            brush = Brush.linearGradient(
                                listOf(KararColors.Teal, KararColors.Violet)
                            ),
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 30.sp
                        )
                    )
                }
                Text(
                    text = "نظام الحسابات الفلكية الشرعية المتكامل للمناطق العادية وخطوط العرض المرتفعة صيفاً — أوفلاين بالكامل",
                    color = KararColors.TextMuted,
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${hijri.day} ${hijri.monthName} ${hijri.year} هـ",
                    color = KararColors.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.End
                )
                Text(
                    text = "${today.dayOfMonth} - ${today.monthNumber} - ${today.year} م",
                    color = KararColors.TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    style = TextStyle(textDirection = TextDirection.Ltr)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.LocationOn,
                contentDescription = null,
                tint = KararTokens.Accent.teal,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = settings.cityName,
                color = KararColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeroPill(clockText, KararColors.Teal, ltr = true)
            HeroPill(utcLabel, KararColors.Violet, ltr = true)
            if (state.locating) HeroPill("جارٍ التحديد…", KararColors.TextMuted)
        }

        Spacer(Modifier.height(12.dp))

        // صندوق بحث يشبه حقل المرجع، يفتح منتقي المدن الأوفلاين.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(KararColors.SurfaceHigh.copy(alpha = 0.72f))
                .border(1.dp, KararColors.Border, RoundedCornerShape(22.dp))
                .clickable(onClick = onSearchCity)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Search,
                contentDescription = null,
                tint = KararColors.TextMuted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "ابحث عن مدينة (كربلاء، النجف، لندن…)",
                color = KararColors.TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeroAction(
                text = if (state.locating) "جارٍ…" else "GPS ذكي",
                icon = Icons.Filled.MyLocation,
                color = KararColors.Mint,
                container = Color(0x1A34D399),
                border = Color(0x3334D399),
                modifier = Modifier.weight(1f),
                onClick = onGps
            )
            HeroAction(
                text = "مدينتي (IP)",
                icon = Icons.Filled.Public,
                color = KararColors.Violet,
                container = Color(0x1A6366F1),
                border = Color(0x336366F1),
                modifier = Modifier.weight(1f),
                onClick = onIp
            )
            HeroAction(
                text = "المدن",
                icon = Icons.Filled.Search,
                color = KararColors.TextSecondary,
                container = KararColors.CardSoft,
                border = KararColors.Border,
                onClick = onSearchCity
            )
            HeroAction(
                text = if (settings.use24Hour) "24 ساعة" else "12 ساعة",
                icon = Icons.Filled.AccessTime,
                color = KararColors.Gold,
                container = KararColors.Gold.copy(alpha = 0.12f),
                border = KararColors.Gold.copy(alpha = 0.30f),
                onClick = { state.update { it.copy(use24Hour = !it.use24Hour) } }
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "طريقة الحساب",
            color = KararColors.TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val columns = if (maxWidth >= 420.dp) 4 else 2
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                HERO_METHODS.chunked(columns).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        row.forEach { (code, label) ->
                            HeroMethodButton(
                                label = label,
                                selected = settings.methodCode == code,
                                modifier = Modifier.weight(1f),
                                onClick = { onSelectMethod(code) }
                            )
                        }
                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroPill(text: String, color: Color, ltr: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.30f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            style = if (ltr) TextStyle(textDirection = TextDirection.Ltr) else TextStyle.Default
        )
    }
}

@Composable
private fun HeroAction(
    text: String,
    icon: ImageVector,
    color: Color,
    container: Color,
    border: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(container)
            .border(1.dp, border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(5.dp))
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun HeroMethodButton(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = if (selected) {
                    Brush.verticalGradient(listOf(Color(0xFF14B8A6), Color(0xFF0F766E)))
                } else {
                    SolidColor(Color(0x80000000))
                },
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                if (selected) Color(0x8014B8A6) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) KararColors.OnGold else KararColors.TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}

// =====================================================================
// الأقسام المنسدلة
// =====================================================================

/**
 * قسم منسدل في الضبط.
 *
 * الآن هو **نافذة واحدة**: العنوان داخل الإطار نفسه، والمحتوى يُطوى داخلها
 * فتتقلّص النافذة وتتوسّع ككتلة واحدة (بدل بطاقة عنوان منفصلة فوق محتوى
 * عائم). المنطق كله في [MergedAccordion] المشترك مع الفقه والتسبيح.
 */
@Composable
private fun SettingsAccordion(
    title: String,
    icon: ImageVector,
    initiallyOpen: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    MergedAccordion(
        title = title,
        icon = icon,
        initiallyOpen = initiallyOpen,
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 14.dp),
        content = content
    )
}

// =====================================================================
// شبكة مواقيت اليوم
// =====================================================================

@Composable
private fun DailyTimesGrid(settings: AppSettings) {
    val date = PrayerCalculator.todayDate(settings)
    val times = remember(settings, date) {
        PrayerCalculator.adjustedTimes(PrayerCalculator.timesFor(settings, date), settings)
    }
    val ids = remember(settings, date) { PrayerCalculator.visibleIds(settings, date) }

    if (ids.isEmpty()) {
        GlassCard(modifier = Modifier.fillMaxWidth(), corner = 16.dp, contentPadding = PaddingValues(14.dp)) {
            Text(
                "لا توجد أوقات ظاهرة — اختر وقتًا واحدًا على الأقل من قسم «الأوقات الظاهرة».",
                color = KararColors.TextMuted,
                fontSize = 11.sp
            )
        }
        return
    }

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = when {
            maxWidth >= 600.dp -> 4
            maxWidth >= 420.dp -> 3
            else -> 2
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ids.chunked(columns).forEach { rowIds ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowIds.forEach { id ->
                        DailyTimeCell(
                            id = id,
                            time = PrayerCalculator.formatHour(times[id] ?: 0.0, settings.use24Hour),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(columns - rowIds.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun DailyTimeCell(
    id: String,
    time: Triple<String, String, String>,
    modifier: Modifier = Modifier
) {
    val highlight = id == "fajr" || id == "maghrib"
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (highlight) KararColors.Teal.copy(alpha = 0.12f) else KararColors.CardSoft)
            .border(
                1.dp,
                if (highlight) KararColors.Teal.copy(alpha = 0.30f) else KararColors.Border,
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            PrayerMeta.label(id),
            color = if (highlight) KararTokens.Accent.teal else KararColors.TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Spacer(Modifier.height(4.dp))
        Text(
            time.first,
            color = KararColors.TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
        Text(
            time.second,
            color = KararColors.TextMuted,
            fontSize = 8.sp
        )
    }
}

// =====================================================================
// اختيار الطريقة
// =====================================================================

/** الطرق السريعة — تُعرض دائمًا في الضبط (من amsak3d.tsx). */
private val SETTINGS_METHODS_QUICK = listOf(
    "alkafeel" to "الكفيل (السيستاني)",
    "leufa" to "معهد ليفا - قم",
    "tehran" to "طهران",
    "ummalqura" to "أم القرى (مكة)"
)

/** بقية المعاهد العالمية — تُعرض بعد النقر على «متقدمة» (نفس ترتيب المرجع). */
private val SETTINGS_METHODS_ADVANCED = listOf(
    "london_shia" to "مؤسسة لندن (شيعي)",
    "mwl" to "رابطة العالم الإسلامي",
    "egypt" to "الهيئة المصرية العامة للمساحة",
    "isna" to "رابطة أمريكا الشمالية (ISNA)",
    "karachi" to "جامعة كراتشي (18°)",
    "france_12" to "فرنسا 12°",
    "european_council" to "المجلس الأوروبي للإفتاء",
    "mwl_custom" to "المعيار الموحد (18°)",
    "gulf_shia" to "معيار زاوية 4.1°"
)

@Composable
private fun MethodSelector(selected: String, onSelect: (String) -> Unit) {
    var showAdvanced by rememberSaveable { mutableStateOf(false) }
    // السريعة دائمًا ٤ فقط، و«متقدمة» لا توسّع إلا بعد النقر عليها —
    // نفس البنية في المرجع وفيه تقلص تلقائي بلا ازدحام.
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        MethodChipsGrid(SETTINGS_METHODS_QUICK, selected, onSelect, columns = 2)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(KararColors.CardSoft)
                .border(1.dp, KararColors.Border, RoundedCornerShape(12.dp))
                .clickable { showAdvanced = !showAdvanced }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (showAdvanced) "إخفاء الطرق المتقدمة" else "عرض الطرق المتقدمة (9 معايير)",
                color = KararColors.Gold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(if (showAdvanced) "−" else "+", color = KararColors.Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        if (showAdvanced) {
            MethodChipsGrid(SETTINGS_METHODS_ADVANCED, selected, onSelect, columns = 2)
        }
    }
}

@Composable
private fun MethodChipsGrid(
    methods: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    columns: Int
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val cols = if (maxWidth >= 420.dp) 3 else columns
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            methods.chunked(cols).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { (code, label) ->
                        val isSelected = selected == code
                        Box(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) KararColors.Gold else KararColors.CardSoft)
                                .border(
                                    1.dp,
                                    if (isSelected) Color.Transparent else KararColors.Border,
                                    RoundedCornerShape(50)
                                )
                                .clickable { onSelect(code) }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                color = if (isSelected) KararColors.OnGold else KararColors.TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                softWrap = true
                            )
                        }
                    }
                    repeat(cols - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

// =====================================================================
// وضع الإمساك
// =====================================================================

private val IMSAK_MODES = listOf(
    "ramadan_auto" to "تلقائي (رمضان)",
    "always" to "دائمًا",
    "hidden" to "مخفي"
)

@Composable
private fun ImsakModeSelector(current: String, onChange: (String) -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth(), corner = 16.dp, contentPadding = PaddingValues(10.dp)) {
        Text(
            "ظهور مربع وقت الإمساك",
            color = KararColors.TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "الافتراضي في V7: يظهر في شهر رمضان فقط.",
            color = KararColors.TextMuted,
            fontSize = 9.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x1A000000))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            IMSAK_MODES.forEach { (id, label) ->
                val selected = current == id
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) KararColors.Gold else Color.Transparent)
                        .clickable { onChange(id) }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = if (selected) Color(0xFF241A00) else KararColors.TextSecondary,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// =====================================================================
// التوقيت الصيفي
// =====================================================================

private val DST_MODES = listOf(
    "auto" to "تلقائي",
    "summer" to "صيفي",
    "winter" to "شتوي"
)

@Composable
private fun DstModeSelector(current: String, timeZoneId: String, onChange: (String) -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth(), corner = 16.dp, contentPadding = PaddingValues(10.dp)) {
        Text(
            "التوقيت الصيفي",
            color = KararColors.TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "تلقائي يتبع قاعدة $timeZoneId لكل تاريخ على حدة؛ «صيفي/شتوي» يثبّت الإزاحة طوال السنة.",
            color = KararColors.TextMuted,
            fontSize = 9.sp,
            lineHeight = 14.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x1A000000))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            DST_MODES.forEach { (id, label) ->
                val selected = current == id
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) Color(0xFF7DD3FC) else Color.Transparent)
                        .clickable { onChange(id) }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = if (selected) Color(0xFF062033) else KararColors.TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// =====================================================================
// شبكة تعديل الأوقات
// =====================================================================

@Composable
private fun OffsetsGrid(settings: AppSettings, onChange: (String, Int) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val columns = if (maxWidth >= 480.dp) 3 else 2
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PrayerMeta.order.chunked(columns).forEach { rowIds ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowIds.forEach { id ->
                        OffsetCell(
                            id = id,
                            value = settings.offsets[id] ?: 0,
                            onChange = { onChange(id, it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(columns - rowIds.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun OffsetCell(
    id: String,
    value: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(KararColors.CardSoft)
            .border(1.dp, KararColors.Border, RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(
            PrayerMeta.label(id),
            color = KararColors.TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepChip("−") { onChange(value - 1) }
            Text(
                if (value > 0) "+$value" else value.toString(),
                color = KararColors.Gold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            StepChip("+") { onChange(value + 1) }
        }
    }
}

// =====================================================================
// اختيار المدينة
// =====================================================================

@Composable
private fun CityPickerDialog(visible: Boolean, state: AppState, onDismiss: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var results by remember { mutableStateOf<List<OfflineCity>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // انتظار قصير يجعل الكتابة السريعة تُلغي البحث السابق (debounce)؛
    // كل القراءة/الفك/الفرز على Dispatchers.Default داخل CityDatabase.
    LaunchedEffect(query) {
        loading = true
        delay(220)
        val loaded = runCatching {
            if (query.isBlank()) {
                CityDatabase.popular(60, preferredCountry = state.settings.countryName)
            } else {
                CityDatabase.search(query, 60, preferredCountry = state.settings.countryName)
            }
        }.getOrElse { emptyList() }
        results = loaded
        loading = false
    }

    SpringSheet(
        visible = visible,
        onDismissRequest = onDismiss,
        containerColor = KararColors.Card,
        imePadding = true,
        contentPadding = PaddingValues(horizontal = 16.dp),
        maxHeight = 640.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // لا fillMaxHeight داخل الورقة السفلية: القيود هناك قد تكون غير
                // محدودة على بعض الأجهزة ما يسبب انهيار القياس. ارتفاع ثابت
                // معقول مع LazyColumn بوزن كامل يعطي نفس الشكل بأمان.
                .heightIn(min = 320.dp, max = 640.dp)
                .padding(bottom = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "اختيار المدينة",
                        color = KararColors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (loading) "جارٍ البحث في المدن الأوفلاين…"
                        else "النتائج: ${results.size} — ${state.settings.countryName}",
                        color = KararColors.TextMuted,
                        fontSize = 10.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(KararColors.GoldSoft)
                        .clickable {
                            scope.launch {
                                state.detectLocation()
                                onDismiss()
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.MyLocation,
                            contentDescription = null,
                            tint = KararColors.Gold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "موقعي",
                            color = KararColors.Gold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            androidx.compose.material3.OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("اسم المدينة أو الدولة…", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = KararColors.Gold,
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = KararColors.Gold
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(
                    items = results,
                    key = { it.stableId },
                    contentType = { "city" }
                ) { city ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                state.selectCity(city)
                                scope.launch { runCatching { state.prepareCities() } }
                                onDismiss()
                            }
                            .padding(horizontal = 8.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📍", fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                city.name,
                                color = KararColors.TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                buildString {
                                    append(city.country)
                                    if (city.region.isNotBlank() && city.region != city.name) {
                                        append(" • ").append(city.region)
                                    }
                                    if (city.isProvince) append(" • محافظة/ولاية")
                                },
                                color = KararColors.TextMuted,
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            city.timeZoneId.substringAfterLast('/').replace('_', ' '),
                            color = KararColors.TextMuted,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

// =====================================================================
// عناصر مساعدة
// =====================================================================

/** يضيف/يزيل وقتًا من مجموعة الاختيار ويحافظ على الترتيب الزمني */
private fun List<String>.togglePrayer(id: String, order: List<String>): List<String> {
    val set = toMutableSet()
    if (!set.add(id)) set.remove(id)
    return order.filter { it in set }
}

/** بطاقة اختيار أوقات الصلاة لمجموعة معيّنة (تذكير / عدّاد / أذان / ظهور) */
@Composable
private fun PrayerSelectionCard(
    title: String,
    subtitle: String,
    selected: List<String>,
    accent: Color,
    onToggle: (String) -> Unit,
    onAll: () -> Unit,
    onNone: () -> Unit
) {
    val allSelected = selected.containsAll(PrayerMeta.order)
    GlassCard(modifier = Modifier.fillMaxWidth(), corner = 16.dp, contentPadding = PaddingValues(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = KararColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(subtitle, color = KararColors.TextMuted, fontSize = 9.sp, lineHeight = 13.sp)
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .background(if (allSelected) accent.copy(alpha = 0.16f) else KararColors.CardSoft, RoundedCornerShape(50))
                    .border(
                        1.dp,
                        if (allSelected) accent.copy(alpha = 0.6f) else KararColors.Border,
                        RoundedCornerShape(50)
                    )
                    .clickable { if (allSelected) onNone() else onAll() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    if (allSelected) "مسح" else "الكل",
                    color = if (allSelected) accent else KararColors.TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        PrayerMeta.order.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { id ->
                    val on = id in selected
                    Box(
                        Modifier
                            .weight(1f)
                            .background(
                                if (on) accent.copy(alpha = 0.18f) else KararColors.CardSoft,
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                if (on) accent.copy(alpha = 0.55f) else KararColors.Border,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onToggle(id) }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            PrayerMeta.label(id),
                            color = if (on) accent else KararColors.TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun ThemeModeRow(
    current: String,
    onChange: (String) -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), corner = 16.dp, contentPadding = PaddingValues(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("المظهر", color = KararColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(
                    "يتغيّر التطبيق كاملًا فورًا — بلا إعادة تشغيل",
                    color = KararColors.TextMuted,
                    fontSize = 9.sp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ThemeMode.all.forEach { mode ->
                    val selected = current == mode
                    val icon = when (mode) {
                        ThemeMode.LIGHT -> Icons.Filled.WbSunny
                        ThemeMode.DARK -> Icons.Filled.DarkMode
                        else -> Icons.Filled.BrightnessAuto
                    }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) KararColors.Gold else KararColors.CardSoft)
                            .clickable { onChange(mode) }
                            .padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            icon,
                            contentDescription = ThemeMode.label(mode),
                            tint = if (selected) KararColors.OnGold else KararColors.TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        AnimatedVisibility(
                            visible = selected,
                            // كشف أفقي نابض (M3 Expressive) بدل الظهور المفاجئ،
                            // ويتحول إلى قطع فوري في وضع «تقليل الحركة».
                            enter = expandHorizontally(motionSpatial()) +
                                fadeIn(motionEffects()),
                            exit = shrinkHorizontally(motionSpatial()) +
                                fadeOut(motionEffects())
                        ) {
                            Text(
                                ThemeMode.label(mode),
                                color = KararColors.OnGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), corner = 16.dp, contentPadding = PaddingValues(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = KararColors.TextPrimary, fontSize = 13.sp)
                if (subtitle != null) Text(subtitle, color = KararColors.TextMuted, fontSize = 10.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = KararColors.OnGold,
                    checkedTrackColor = KararColors.Gold
                )
            )
        }
    }
}

@Composable
private fun StepperRow(
    title: String,
    value: Int,
    step: Int = 1,
    onChange: (Int) -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth(), corner = 16.dp, contentPadding = PaddingValues(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = KararColors.TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
            StepChip("−") { onChange(value - step) }
            Text(
                text = if (value > 0) "+$value" else value.toString(),
                color = KararColors.Gold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(48.dp),
                textAlign = TextAlign.Center
            )
            StepChip("+") { onChange(value + step) }
        }
    }
}

@Composable
private fun StepChip(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(30.dp)
            .background(KararColors.CardSoft, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = KararColors.Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SmallAction(label: String, icon: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .background(Color(0x1AFFD700), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (icon) Icon(Icons.Filled.MyLocation, contentDescription = null, tint = KararColors.Gold, modifier = Modifier.size(13.dp))
        Text(label, color = KararColors.Gold, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PermissionRow(title: String, granted: Boolean, onRequest: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth(), corner = 16.dp, contentPadding = PaddingValues(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (granted) "✅" else "⭕", fontSize = 14.sp)
            Spacer(Modifier.width(8.dp))
            Text(title, color = KararColors.TextPrimary, fontSize = 12.sp, modifier = Modifier.weight(1f))
            if (!granted) {
                SmallAction("سماح", onClick = onRequest)
            } else {
                Text("ممنوح", color = KararColors.Success, fontSize = 10.sp)
            }
        }
    }
}

private fun Double.round1(): String = (kotlin.math.round(this * 100) / 100.0).toString()
