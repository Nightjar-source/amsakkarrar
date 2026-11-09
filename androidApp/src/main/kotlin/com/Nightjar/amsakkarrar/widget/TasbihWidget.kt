package com.Nightjar.amsakkarrar.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.Nightjar.amsakkarrar.MainActivity
import com.Nightjar.amsakkarrar.notifications.SettingsLoader
import com.Nightjar.amsakkarrar.notifications.SettingsSaver
import com.karrar.amsakiah.core.data.TasbihPresets
import com.karrar.amsakiah.core.settings.AppSettings
import com.karrar.amsakiah.core.state.SettingsChangeBus
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val Sky = Color(0xFF38BDF8)
private val White = Color(0xFFF8FAFC)
private val Muted = Color(0xFF94A3B8)
private val Danger = Color(0xFFEF5350)

private fun color(value: Color): ColorProvider = ColorProvider(value)

/**
 * ودجت المسبحة — 2×2:
 * - الضغط على الودجت يزيد العداد ويحفظه في الإعدادات فورًا (تزامن فوري).
 * - زر تصفير سريع.
 * - زر «التطبيق» لفتح التطبيق.
 * - لا يوجد زر ⚙ داخل الودجت: الخلفية تُضبط من شاشة «الودجتات» في التطبيق
 *   أو بالضغط المطوّل على الودجت ← «تعديل».
 */
class TasbihWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TasbihWidget()
}

class TasbihWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode get() = SizeMode.Exact

    // بديل آمن عند فشل التركيب — سياسة Android 17 للودجتات.
    override fun onCompositionError(
        context: Context,
        glanceId: GlanceId,
        appWidgetId: Int,
        throwable: Throwable
    ) = WidgetErrorViews.show(context, appWidgetId, throwable)

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settings = SettingsLoader.load(context)
        val widgetId = runCatching {
            androidx.glance.appwidget.GlanceAppWidgetManager(context).getAppWidgetId(id)
        }.getOrDefault(android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID)
        provideContent {
            GlanceTheme {
                TasbihWidgetContent(context, settings, widgetId)
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun TasbihWidgetContent(context: Context, settings: AppSettings, appWidgetId: Int) {
    val presetList = TasbihPresets.combined(settings.customDhikrs)
    val preset = presetList[TasbihPresets.safeIndex(settings.tasbihDhikrIndex, presetList.size)]
    val target = settings.tasbihTarget.coerceAtLeast(1)
    val progress = (settings.tasbihCount.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    val background = Color(WidgetStyleStore.backgroundArgb(context, appWidgetId))
    val border = Color(WidgetStyleStore.borderArgb(context, appWidgetId))
    val card = Color(WidgetStyleStore.softBackgroundArgb(context, appWidgetId))
    val size = LocalSize.current
    val compact = size.width < 185.dp || size.height < 150.dp
    val pad = if (compact) 8.dp else 12.dp
    val available = (size.width - (pad + 1.dp) * 2).coerceAtLeast(1.dp)
    val fillWidth = (available * progress).coerceAtLeast(0.dp)
    val circle = if (compact) 66.dp else if (size.height < 235.dp) 84.dp else 98.dp

    val openApp = Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_MAIN
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(border)
            .cornerRadius(if (compact) 22.dp else 28.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(1.dp)
                .background(background)
                .cornerRadius(if (compact) 21.dp else 27.dp)
                .padding(if (compact) 8.dp else 12.dp)
                .clickable(actionRunCallback<TasbihIncrementAction>()),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text(
                text = preset.text,
                style = TextStyle(
                    color = color(Sky),
                    fontSize = if (compact) 8.sp else 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(GlanceModifier.height(if (compact) 2.dp else 4.dp))

            Box(
                modifier = GlanceModifier
                    .size(circle)
                    .background(Sky)
                    .cornerRadius(50.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(circle - 12.dp)
                        .background(Color(0xFF0F172A))
                        .cornerRadius(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = settings.tasbihCount.toString(),
                        style = TextStyle(
                            color = color(White),
                            fontSize = if (compact) 26.sp else 34.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(GlanceModifier.height(if (compact) 1.dp else 3.dp))
            Text(
                text = "من $target • ${settings.tasbihRound} دورة",
                style = TextStyle(color = color(Muted), fontSize = if (compact) 6.sp else 8.sp)
            )
            if (!compact) {
                Spacer(GlanceModifier.height(5.dp))
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .background(Color(0x66000000))
                        .cornerRadius(50.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (fillWidth > 0.dp) {
                        Box(
                            modifier = GlanceModifier
                                .width(fillWidth)
                                .height(7.dp)
                                .background(Sky)
                                .cornerRadius(50.dp)
                        ) {}
                    }
                }
            }

            Spacer(GlanceModifier.height(if (compact) 2.dp else 6.dp))
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                if (!compact) {
                    Text(
                        text = "الإجمالي ${settings.tasbihTotal}",
                        modifier = GlanceModifier.defaultWeight(),
                        style = TextStyle(color = color(Muted), fontSize = 7.sp)
                    )
                } else {
                    Spacer(GlanceModifier.defaultWeight())
                }
                Text(
                    text = "تصفير",
                    modifier = GlanceModifier
                        .background(card)
                        .cornerRadius(50.dp)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                        .clickable(actionRunCallback<TasbihResetAction>()),
                    style = TextStyle(color = color(Danger), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(GlanceModifier.width(5.dp))
                Text(
                    text = "التطبيق",
                    modifier = GlanceModifier
                        .background(card)
                        .cornerRadius(50.dp)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                        .clickable(actionStartActivity(openApp)),
                    style = TextStyle(color = color(Sky), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

/** زيادة التسبيح من الودجت — تُحفظ فورًا في إعدادات التطبيق. */
class TasbihIncrementAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val settings = SettingsLoader.load(context)
        val target = settings.tasbihTarget.coerceAtLeast(1)
        val next = settings.tasbihCount + 1
        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .toString()
        val dayCount = if (settings.tasbihDayDate == today) settings.tasbihDayCount else 0
        val completed = next >= target
        val (nextIndex, nextTarget) = TasbihPresets.next(
            TasbihPresets.combined(settings.customDhikrs),
            settings.tasbihDhikrIndex,
            settings.tasbihAutoAdvance
        )

        val updated = if (completed) {
            settings.copy(
                tasbihCount = 0,
                tasbihRound = settings.tasbihRound + 1,
                tasbihTotal = settings.tasbihTotal + 1,
                tasbihDayCount = dayCount + 1,
                tasbihDayDate = today,
                tasbihDhikrIndex = nextIndex,
                tasbihTarget = nextTarget
            )
        } else {
            settings.copy(
                tasbihCount = next,
                tasbihTotal = settings.tasbihTotal + 1,
                tasbihDayCount = dayCount + 1,
                tasbihDayDate = today
            )
        }
        SettingsSaver.save(context, updated)
        // مزامنة فورية مع التطبيق إن كان مفتوحًا، ثم تحديث كل الودجتات
        runCatching { SettingsChangeBus.notifyChanged() }
        WidgetUpdater.updateAll(context)
    }
}

/** تصفير عدّاد المسبحة من الودجت. */
class TasbihResetAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val settings = SettingsLoader.load(context)
        SettingsSaver.save(context, settings.copy(tasbihCount = 0))
        runCatching { SettingsChangeBus.notifyChanged() }
        WidgetUpdater.updateAll(context)
    }
}
