package com.karrar.amsakiah.ui.adaptive

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * هوامش المحتوى الأفقية الموحّدة.
 *
 * الشل (`AppShell`) يحسب مسافات النظام الآمنة مرة واحدة عبر
 * `WindowInsets.safeDrawing` ويضعها في `contentPadding` (ثقب الشاشة + أشرطة
 * النظام الجانبية في الأفقي/DeX/النوافذ الحرة). الشاشات التي تحتاج هامش
 * تصميم إضافيًا (16dp مثلًا) يجب أن تضيفه **فوق** هذه القيمة لا بدلًا منها،
 * وإلا يزحف المحتوى تحت شريط النظام على الشاشات العريضة.
 *
 * الخلفية تبقى full-bleed دائمًا (تُرسم قبل `padding`)، والإزاحة للمحتوى فقط.
 */
@Composable
fun PaddingValues.contentStart(extra: Dp = 0.dp): Dp =
    calculateStartPadding(LocalLayoutDirection.current) + extra

@Composable
fun PaddingValues.contentEnd(extra: Dp = 0.dp): Dp =
    calculateEndPadding(LocalLayoutDirection.current) + extra

/** هامش تصميم أفقي موحّد لكل الشاشات (يُضاف بعد مسافات النظام). */
val ScreenHorizontalMargin: Dp = 16.dp
