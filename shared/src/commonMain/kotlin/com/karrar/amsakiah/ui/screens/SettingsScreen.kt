package com.karrar.amsakiah.ui.screens

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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Widgets
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.karrar.amsakiah.core.astronomy.CityDatabase
import com.karrar.amsakiah.core.astronomy.OfflineCity
import com.karrar.amsakiah.core.audio.SoundTestBridge
import com.karrar.amsakiah.core.astronomy.OfflineCities
import com.karrar.amsakiah.core.permissions.PermissionController
import com.karrar.amsakiah.core.permissions.rememberPermissionController
import com.karrar.amsakiah.core.state.AppState
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.core.state.PrayerMeta
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.components.SectionTitle
import com.karrar.amsakiah.ui.theme.KararColors
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    state: AppState,
    contentPadding: PaddingValues,
    onOpenWidgets: () -> Unit = {}
) {
    val settings = state.settings
    val scope = rememberCoroutineScope()
    val permissions = rememberPermissionController()
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
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ---------- الموقع ----------
        item { SectionTitle("الموقع والمدينة") }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), corner = 20.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                    }
                    SmallAction("تغيير") { showCityPicker = true }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallAction(if (state.locating) "جارٍ..." else "GPS أوفلاين", icon = true) {
                        if (permissions.status.location) {
                            scope.launch { state.detectLocation() }
                        } else {
                            // نافذة نظام أصلية لطلب صلاحية الموقع (تعمل مع كل إصدارات أندرويد)
                            gpsAfterGrant = true
                            permissions.requestLocation()
                        }
                    }
                    SmallAction("عبر IP", icon = false) {
                        // استشعار الموقع عبر الشبكة كما في تطبيق الكفيل
                        scope.launch { state.detectLocationByIp() }
                    }
                    SmallAction("بحث المدن") { showCityPicker = true }
                }
            }
        }

        // ---------- الودجتات (زر V7) ----------
        item { SectionTitle("الودجتات") }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), corner = 20.dp) {
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
                            "خمسة أشكال: اليوم، المواقيت، العدّاد، الهجري، والمسبحة",
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
                        tint = Color(0xFF7DD3FC),
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "إضافة ودجت للشاشة",
                        color = Color(0xFF7DD3FC),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ---------- الحساب ----------
        item { SectionTitle("طريقة الحساب") }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), corner = 20.dp) {
                Text("الطريقة الحالية", color = KararColors.TextMuted, fontSize = 10.sp)
                Text(
                    OfflineCities.methodsMatrix.firstOrNull { it.code == settings.methodCode }?.name ?: "",
                    color = KararColors.Gold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "alkafeel" to "الكفيل",
                        "leufa" to "ليفا",
                        "tehran" to "طهران",
                        "london_shia" to "لندن",
                        "mwl" to "MWL",
                        "egypt" to "مصر"
                    ).forEach { (code, label) ->
                        val selected = settings.methodCode == code
                        Box(
                            Modifier
                                .background(if (selected) KararColors.Gold else Color(0x14FFFFFF), RoundedCornerShape(50))
                                .clickable { state.update { it.copy(methodCode = code) } }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                label,
                                color = if (selected) Color(0xFF241A00) else KararColors.TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "لبقية الطرق (13 طريقة) انتقل إلى شاشة «الفقه والمعايير»",
                    color = KararColors.TextMuted,
                    fontSize = 10.sp
                )
            }
        }

        // ---------- الهجري والتعديلات ----------
        item { SectionTitle("التاريخ الهجري") }
        item {
            StepperRow(
                title = "تقديم/تأخير الشهر",
                value = settings.hijriOffset,
                onChange = { state.update { s -> s.copy(hijriOffset = it.coerceIn(-3, 3)) } }
            )
        }

        item { SectionTitle("تعديل الأوقات (دقائق)") }
        items(PrayerMeta.order) { id ->
            val current = settings.offsets[id] ?: 0
            StepperRow(
                title = PrayerMeta.label(id),
                value = current,
                onChange = { newValue ->
                    state.update { s ->
                        s.copy(offsets = s.offsets + (id to newValue.coerceIn(-30, 30)))
                    }
                }
            )
        }

        item { SectionTitle("الأوقات الظاهرة") }
        items(PrayerMeta.order) { id ->
            val visible = id in settings.visiblePrayers
            ToggleRow(
                title = PrayerMeta.label(id),
                checked = visible,
                onChange = { checked ->
                    state.update { s ->
                        val list = s.visiblePrayers.toMutableList()
                        if (checked) {
                            if (!list.contains(id)) list.add(id)
                        } else {
                            list.remove(id)
                        }
                        s.copy(visiblePrayers = list)
                    }
                }
            )
        }

        // ---------- الإشعارات ----------
        item { SectionTitle("الأذان والإشعارات") }
        item {
            ToggleRow(
                title = "تنبيه الأذان عند دخول الوقت",
                checked = settings.adhanEnabled,
                onChange = { c -> state.update { it.copy(adhanEnabled = c) } }
            )
        }
        item {
            PrayerSelectionCard(
                title = "أوقات الأذان",
                subtitle = "اختر الصلوات التي يُرفع لها الأذان — أو الكل",
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
        }
        item {
            ToggleRow(
                title = "تذكير قبل الوقت",
                subtitle = "قبل ${settings.reminderMinutes} دقيقة",
                checked = settings.reminderEnabled,
                onChange = { c -> state.update { it.copy(reminderEnabled = c) } }
            )
        }
        item {
            StepperRow(
                title = "دقائق التذكير",
                value = settings.reminderMinutes,
                step = 5,
                onChange = { v -> state.update { it.copy(reminderMinutes = v.coerceIn(0, 60)) } }
            )
        }
        item {
            PrayerSelectionCard(
                title = "أوقات التذكير",
                subtitle = "اختر الأوقات التي يصلك قبلها التنبيه — أو الكل",
                selected = settings.reminderPrayers,
                accent = Color(0xFFFFB74D),
                onToggle = { id ->
                    state.update { s ->
                        s.copy(reminderPrayers = s.reminderPrayers.togglePrayer(id, PrayerMeta.order))
                    }
                },
                onAll = { state.update { it.copy(reminderPrayers = PrayerMeta.order) } },
                onNone = { state.update { it.copy(reminderPrayers = emptyList()) } }
            )
        }
        item {
            ToggleRow(
                title = "الإشعار الحي (العدّاد)",
                checked = settings.liveNotification,
                onChange = { c -> state.update { it.copy(liveNotification = c) } }
            )
        }
        item {
            PrayerSelectionCard(
                title = "أوقات العدّاد",
                subtitle = "يدور العدّاد والإشعار الحي حول هذه الأوقات — أو الكل",
                selected = settings.livePrayers,
                accent = Color(0xFF7DD3FC),
                onToggle = { id ->
                    state.update { s ->
                        s.copy(livePrayers = s.livePrayers.togglePrayer(id, PrayerMeta.order))
                    }
                },
                onAll = { state.update { it.copy(livePrayers = PrayerMeta.order) } },
                onNone = { state.update { it.copy(livePrayers = emptyList()) } }
            )
        }
        item {
            ToggleRow(
                title = "الاهتزاز مع الأذان",
                checked = settings.vibration,
                onChange = { c -> state.update { it.copy(vibration = c) } }
            )
        }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), corner = 16.dp, contentPadding = PaddingValues(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("تشخيص الصوت والإشعار", color = KararColors.TextPrimary, fontSize = 13.sp)
                        Text(
                            if (SoundTestBridge.supported) {
                                "يشغّل adhan.mp3 نفسه من الموارد المشتركة، ويعيد نشر الإشعار الحي"
                            } else {
                                "التشخيص متاح على أندرويد فقط"
                            },
                            color = KararColors.TextMuted,
                            fontSize = 9.sp,
                            lineHeight = 14.sp
                        )
                    }
                    if (SoundTestBridge.supported) {
                        SmallAction("الأذان") { SoundTestBridge.playAdhan() }
                        Spacer(Modifier.width(6.dp))
                        SmallAction("الإشعار") { SoundTestBridge.showLiveNotification() }
                    }
                }
            }
        }

        // ---------- الصلاحيات ----------
        item { SectionTitle("الصلاحيات") }
        item { PermissionRow("الإشعارات", permissions.status.notifications) { permissions.requestNotifications() } }
        item { PermissionRow("الموقع (للقبلة والمدن)", permissions.status.location) { permissions.requestLocation() } }
        item { PermissionRow("المنبهات الدقيقة", permissions.status.exactAlarm) { permissions.requestExactAlarm() } }
        item { PermissionRow("استثناء البطارية", permissions.status.batteryOptimized) { permissions.requestBatteryExemption() } }
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clickable { permissions.openAppSettings() }
                    .padding(8.dp)
            ) {
                Text("فتح إعدادات التطبيق في النظام", color = KararColors.Gold, fontSize = 12.sp)
            }
        }

        // ---------- المظهر ----------
        item { SectionTitle("المظهر والطقس") }
        item {
            ToggleRow(
                title = "الطقس (يتطلب إنترنت)",
                checked = settings.showWeather,
                onChange = { c -> state.update { it.copy(showWeather = c) } }
            )
        }
        item {
            StepperRow(
                title = "حجم الخط %",
                value = (settings.fontScale * 100).toInt(),
                step = 5,
                onChange = { v -> state.update { it.copy(fontScale = (v.coerceIn(80, 140)) / 100f) } }
            )
        }

        // ---------- أخرى ----------
        item { SectionTitle("أخرى") }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), corner = 20.dp) {
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
        }
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0x1AEF5350), RoundedCornerShape(14.dp))
                    .clickable { state.reset() }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("إعادة تعيين كل الإعدادات", color = KararColors.Danger, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        item {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("إمساكية كرار 2026", color = KararColors.Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("الإصدار 8.0 — أوفلاين بالكامل", color = KararColors.TextMuted, fontSize = 10.sp)
                Text("تطوير: كرار", color = KararColors.TextMuted, fontSize = 10.sp)
            }
        }
    }

    if (showCityPicker) {
        CityPickerDialog(state = state, onDismiss = { showCityPicker = false })
    }
}

@Composable
private fun CityPickerDialog(state: AppState, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<OfflineCity>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(query) {
        results = if (query.isBlank()) CityDatabase.popular(60) else CityDatabase.search(query, 60)
    }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier.fillMaxWidth().height(560.dp),
            corner = 24.dp,
            color = KararColors.Card,
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = KararColors.Gold, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                androidx.compose.material3.OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("اسم المدينة أو الدولة", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
            LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                items(results, key = { "${it.name}|${it.country}|${it.lat}" }) { city ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                state.selectCity(city)
                                scope.launch { state.prepareCities() }
                                onDismiss()
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📍", fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${city.name}، ${city.country}",
                            color = KararColors.TextPrimary,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
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

/** يضيف/يزيل وقتًا من مجموعة الاختيار ويحافظ على الترتيب الزمني */
private fun List<String>.togglePrayer(id: String, order: List<String>): List<String> {
    val set = toMutableSet()
    if (!set.add(id)) set.remove(id)
    return order.filter { it in set }
}

/** بطاقة اختيار أوقات الصلاة لمجموعة معيّنة (تذكير / عدّاد / أذان) */
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
                    .background(if (allSelected) accent.copy(alpha = 0.16f) else Color(0x0FFFFFFF), RoundedCornerShape(50))
                    .border(
                        1.dp,
                        if (allSelected) accent.copy(alpha = 0.6f) else Color(0x1FFFFFFF),
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
                                if (on) accent.copy(alpha = 0.18f) else Color(0x0AFFFFFF),
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                if (on) accent.copy(alpha = 0.55f) else Color(0x12FFFFFF),
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
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                    checkedThumbColor = Color(0xFF241A00),
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
            StepButton("−") { onChange(value - step) }
            Text(
                text = if (value > 0) "+$value" else value.toString(),
                color = KararColors.Gold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(48.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            StepButton("+") { onChange(value + step) }
        }
    }
}

@Composable
private fun StepButton(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(30.dp)
            .background(Color(0x1AFFFFFF), CircleShape)
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
private fun PermissionRow(title: String, granted: Boolean, onRequest: () -> Unit) {    GlassCard(modifier = Modifier.fillMaxWidth(), corner = 16.dp, contentPadding = PaddingValues(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (granted) "✅" else "⭕",
                fontSize = 14.sp
            )
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

