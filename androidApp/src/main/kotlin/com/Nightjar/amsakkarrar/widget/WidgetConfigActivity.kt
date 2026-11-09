package com.Nightjar.amsakkarrar.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.karrar.amsakiah.ui.components.GlassCard
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararTheme

/**
 * شاشة إعدادات خلفية الودجتات.
 *
 * تُفتح من:
 * - زر «⚙ الخلفية» داخل كل ودجت.
 * - الضغط المطوّل على الودجت ثم «تعديل/إعادة ضبط» (widgetFeatures=reconfigurable).
 * - زر «الخلفية» في شاشة الودجتات داخل التطبيق.
 */
class WidgetConfigActivity : ComponentActivity() {

    private val appWidgetId: Int by lazy {
        intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            KararTheme {
                WidgetConfigScreen(
                    initialStyle = WidgetStyleStore.current(this, appWidgetId),
                    onApply = { style ->
                        WidgetStyleStore.set(this, style, appWidgetId)
                        WidgetUpdater.updateAll(this)
                        finishWithResult()
                    },
                    onCancel = { finishWithResult(cancelled = true) }
                )
            }
        }
    }

    private fun finishWithResult(cancelled: Boolean = false) {
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            if (cancelled) {
                setResult(RESULT_CANCELED)
            } else {
                setResult(
                    RESULT_OK,
                    Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                )
            }
        }
        finish()
    }
}

private data class StyleOption(
    val key: String,
    val title: String,
    val description: String,
    val background: Color,
    val card: Color
)

@Composable
private fun WidgetConfigScreen(
    initialStyle: String,
    onApply: (String) -> Unit,
    onCancel: () -> Unit
) {
    var selected by remember { mutableStateOf(initialStyle) }
    val options = listOf(
        StyleOption(
            WidgetStyleStore.MATERIAL,
            "مادة صمّاء",
            "خلفية داكنة صلبة — أوضح قراءة وأقل شفافية",
            Color(0xFF101828),
            Color(0xFF1B2438)
        ),
        StyleOption(
            WidgetStyleStore.GLASS,
            "زجاجي",
            "زجاج داكن شبه شفاف بحدود لامعة — أنيق ومقروء",
            Color(0xCC141C2E),
            Color(0x33FFFFFF)
        ),
        StyleOption(
            WidgetStyleStore.BLUR,
            "ضبابي (Blur)",
            "محاكاة زجاج ضبابي شبه شفاف (RemoteViews لا يدعم Blur الحقيقي)",
            Color(0xD91B2438),
            Color(0x66FFFFFF)
        ),
        StyleOption(
            WidgetStyleStore.TRANSPARENT,
            "شفاف",
            "شفافية عالية تُظهر خلفية الشاشة الرئيسية",
            Color(0x33000000),
            Color(0x1FFFFFFF)
        ),
        StyleOption(
            WidgetStyleStore.DEEP,
            "ليلي متدرّج",
            "خلفية كرار الليلية الأصلية (الافتراضي)",
            Color(0xF2050A15),
            Color(0xE60F1524)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(KararColors.BackgroundTop, KararColors.BackgroundBottom)))
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(KararColors.Gold.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚙", color = KararColors.Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.size(12.dp))
                Column {
                    Text("خلفية الودجتات", color = KararColors.Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("تُطبَّق على كل الودجتات فورًا", color = KararColors.TextMuted, fontSize = 10.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "اختر النمط المناسب لشاشتك. يمكنك الرجوع هنا في أي وقت من شاشة «الودجتات» داخل التطبيق أو بالضغط المطوّل على الودجت ثم «تعديل».",
                color = KararColors.TextSecondary,
                fontSize = 11.sp,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(14.dp))

            options.forEach { option ->
                StyleOptionCard(
                    option = option,
                    selected = option.key == selected,
                    onClick = { selected = option.key }
                )
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x14FFFFFF))
                        .clickable(onClick = onCancel)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("إلغاء", color = KararColors.TextSecondary, fontSize = 13.sp)
                }
                Row(
                    modifier = Modifier
                        .weight(1.4f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(KararColors.Gold)
                        .clickable { onApply(selected) }
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✓", color = Color(0xFF241A00), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.size(6.dp))
                    Text("تطبيق", color = Color(0xFF241A00), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "ملاحظة: بعض إصدارات One UI تسمح بالضغط المطوّل ← «تعديل» مباشرةً، وبعض المشغّلات تفتح هذه الشاشة من قائمة إعدادات الودجت.",
                color = KararColors.TextMuted,
                fontSize = 9.sp,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StyleOptionCard(option: StyleOption, selected: Boolean, onClick: () -> Unit) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (selected) KararColors.Gold else Color.Transparent,
                RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick),
        corner = 22.dp,
        color = if (selected) Color(0x1FFFD700) else KararColors.CardSoft,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    option.title,
                    color = if (selected) KararColors.Gold else KararColors.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(option.description, color = KararColors.TextMuted, fontSize = 9.sp, lineHeight = 14.sp)
            }
            if (selected) {
                Box(
                    Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(50))
                        .background(KararColors.Gold),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", color = Color(0xFF241A00), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        // معاينة مصغّرة للودجت بالنمط المختار
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(option.background)
                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("رمضان", color = KararColors.Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(" 16 ", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("12:45", color = KararColors.TextSecondary, fontSize = 8.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text("باقي على الفجر 04:32:11", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x66000000))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(0.62f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Brush.horizontalGradient(listOf(KararColors.Gold, Color(0xFFFF9F1C))))
                )
            }
        }
    }
}
