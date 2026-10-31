package com.Nightjar.amsakkarrar.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.Nightjar.amsakkarrar.MainActivity

class AmsakiahWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AmsakiahWidget()
}

class AmsakiahWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent()
            }
        }
    }

    @Composable
    private fun WidgetContent() {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFF141122))
                .cornerRadius(18.dp)
                .padding(14.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                Text(
                    text = "🌙 إمساكية كرار 2026",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFFFD54F)),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                Column(
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                    modifier = GlanceModifier.padding(horizontal = 6.dp)
                ) {
                    Text(
                        text = "الإمساك",
                        style = TextStyle(color = ColorProvider(Color.White.copy(alpha = 0.7f)), fontSize = 11.sp)
                    )
                    Text(
                        text = "05:11 ص",
                        style = TextStyle(color = ColorProvider(Color.White), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                    modifier = GlanceModifier.padding(horizontal = 6.dp)
                ) {
                    Text(
                        text = "الفجر",
                        style = TextStyle(color = ColorProvider(Color.White.copy(alpha = 0.7f)), fontSize = 11.sp)
                    )
                    Text(
                        text = "05:21 ص",
                        style = TextStyle(color = ColorProvider(Color(0xFFFFD54F)), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                    modifier = GlanceModifier.padding(horizontal = 6.dp)
                ) {
                    Text(
                        text = "المغرب",
                        style = TextStyle(color = ColorProvider(Color.White.copy(alpha = 0.7f)), fontSize = 11.sp)
                    )
                    Text(
                        text = "06:09 م",
                        style = TextStyle(color = ColorProvider(Color(0xFF81C784)), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
