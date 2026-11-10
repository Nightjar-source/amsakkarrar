package com.karrar.amsakiah.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import com.karrar.amsakiah.resources.Res
import com.karrar.amsakiah.resources.amiri_bold
import com.karrar.amsakiah.resources.amiri_regular
import com.karrar.amsakiah.resources.reem_kufi_variable

/**
 * خطوط الهوية (الخطوط المستخدمة في التطبيق المرجعي amsak3d.tsx):
 * - **Amiri** : نص القرآن والأدعية (font-amiri في المرجع).
 * - **Reem Kufi** : الكشيدة والعناوين الكبرى (font-reem في المرجع).
 *
 * الخطوط SIL OFL 1.1 (Google Fonts)؛ التفاصيل بمسار المصدر:
 * `composeResources/font/NOTICE.txt`.
 */
object KararFonts {

    val Amiri: FontFamily
        @Composable
        get() = FontFamily(
            Font(Res.font.amiri_regular, FontWeight.Normal),
            Font(Res.font.amiri_bold, FontWeight.Bold)
        )

    // Reem Kufi (Variable weight) — الكشيدة والعناوين
    val ReemKufi: FontFamily
        @Composable
        get() = FontFamily(
            Font(Res.font.reem_kufi_variable, FontWeight.Bold)
        )
}
