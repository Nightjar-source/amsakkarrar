package com.karrar.amsakiah.ui.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karrar.amsakiah.core.astronomy.CityDatabase
import com.karrar.amsakiah.core.astronomy.OfflineCity
import com.karrar.amsakiah.ui.permissions.rememberPermissionController
import com.karrar.amsakiah.ui.state.AppState
import com.karrar.amsakiah.ui.theme.KararColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * ورقة اختيار المدينة — مصدر واحد لكل الشاشات (الضبط وشريحة المدينة في «اليوم»).
 *
 * - بحث عربي/إنجليزي عبر [CityDatabase.search] (الأسماء الأصلية محفوظة كأسماء بحث
 *   بديلة حتى لو عُرِضت المدينة بالعربية).
 * - «موقعي» يعمل هنا: إن كانت الصلاحية ممنوحة يكتشف الموقع مباشرةً ولا يفتح أي
 *   إعدادات نظام؛ وإن لم تكن ممنوحة يطلبها ثم يتابع تلقائيًا بعد الموافقة.
 * - تُعرض المنطقة فقط إن كانت عربية (فلا يظهر «England» بجانب «لندن»).
 */
@Composable
fun CityPickerSheet(
    visible: Boolean,
    state: AppState,
    onDismiss: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var results by remember { mutableStateOf<List<OfflineCity>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val permissions = rememberPermissionController()

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
        // لا صفّين متطابقين بالاسم والدولة في القائمة نفسها.
        results = loaded.distinctBy { "${it.name}|${it.country}" }
        loading = false
    }

    // إن مُنحت الصلاحية للتوّ نتابع الكشف فورًا — بلا فتح إعدادات.
    var gpsAfterGrant by remember { mutableStateOf(false) }
    LaunchedEffect(gpsAfterGrant, permissions.status.location) {
        if (gpsAfterGrant && permissions.status.location) {
            gpsAfterGrant = false
            scope.launch {
                runCatching { state.detectLocation() }
                onDismiss()
            }
        }
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
                            if (permissions.status.location) {
                                // الصلاحية ممنوحة: كشف مباشر — لا إعدادات ولا نوافذ.
                                scope.launch {
                                    runCatching { state.detectLocation() }
                                    onDismiss()
                                }
                            } else {
                                state.showLocationPrompt = true
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

            OutlinedTextField(
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
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                items(
                    items = results,
                    key = { it.stableId },
                    contentType = { "city" }
                ) { city ->
                    CityRow(
                        city = city,
                        onClick = {
                            state.selectCity(city)
                            scope.launch { runCatching { state.prepareCities() } }
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CityRow(city: OfflineCity, onClick: () -> Unit) {
    // المنطقة تُعرض فقط إن كانت عربية؛ «England» تُستخدم للبحث ولا تظهر.
    val region = city.region.takeIf {
        it.isNotBlank() && it != city.name && it.any { ch -> ch in '\u0600'..'\u06FF' }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
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
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                buildString {
                    append(city.country)
                    region?.let { append(" • ").append(it) }
                    if (city.isProvince) append(" • محافظة/ولاية")
                },
                color = KararColors.TextMuted,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            utcOffsetLabel(city.offsetHoursNow()),
            color = KararColors.TextMuted,
            fontSize = 9.sp
        )
    }
}

/** UTC+3 / UTC-4.5 — بلا أسماء مناطق لاتينية في الواجهة العربية. */
private fun utcOffsetLabel(offsetHours: Double): String {
    val rounded = (offsetHours * 10).roundToInt() / 10.0
    val sign = if (rounded >= 0) "+" else "-"
    val absolute = abs(rounded)
    val whole = absolute.toInt()
    val fraction = ((absolute - whole) * 10).roundToInt()
    return if (fraction == 0) "UTC$sign$whole" else "UTC$sign$whole.$fraction"
}
