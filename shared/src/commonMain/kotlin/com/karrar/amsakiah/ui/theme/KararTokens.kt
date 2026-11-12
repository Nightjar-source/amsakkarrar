package com.karrar.amsakiah.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * رموز التصميم المركزية (Design Tokens) المستخرجة من `appkarv/amsak3d.tsx`.
 *
 * القاعدة: لا تكتب قيمة لون/زاوية/مسافة/مدة حركة داخل أي شاشة مباشرة؛ استخدم
 * هذه الرموز حتى يبقى التصميم موحّدًا وقابلًا للتعديل من مكان واحد.
 */
object KararTokens {

    /** مسافات الشبكة الأساسية (4dp grid). */
    object Spacing {
        val xxs = 2.dp
        val xs = 4.dp
        val sm = 8.dp
        val md = 12.dp
        val lg = 16.dp
        val xl = 20.dp
        val xxl = 24.dp
        val xxxl = 32.dp
        val huge = 48.dp
    }

    /** زوايا M3 Expressive: كبسولات كاملة وبطاقات كبيرة ناعمة. */
    object Radius {
        val xs = 8.dp
        val sm = 12.dp
        val md = 16.dp
        val lg = 20.dp
        val xl = 24.dp
        val xxl = 28.dp
        val xxxl = 32.dp
        val pill = 999.dp
    }

    /** مقاسات عناصر الشل العائم. */
    object Size {
        val topBarHeight = 56.dp
        val topBarMaxWidth = 896.dp
        /** أقصى عرض لمحتوى الشاشات غير السماوية (مطابق لـ max-w-5xl في amsak3d.tsx). */
        val contentMaxWidth = 1024.dp
        val navBarHeight = 64.dp
        val navBarMaxWidth = 560.dp
        val navItem = 52.dp
        /** ارتفاع شريط مشغّل الأدعية المدمج (≤ 30dp كما في المتطلبات). */
        val playerDockHeight = 64.dp
        val touchTarget = 48.dp
        val circleButton = 52.dp
        /** M3 Expressive: مطويات/شاشات متوسطة (Expressive: Rail 64dp). */
        val railWidthMedium = 64.dp

        /** M3 Expressive: واسع + Large/XL وأجهزة ChromeOS وDex (Expressive: 72dp). */
        val railWidthExpanded = 72.dp
        val contentHorizontal = 16.dp
        val contentBottomGap = 104.dp
        val contentTopGap = 74.dp
        /**
         * أقصى عرض لمسار «اليوم» على الشاشات العريضة: الخلفية السماوية تبقى
         * full-bleed، بينما يبقى القوس والعدّاد في مسرح بحجم هاتف مريح (Flex /
         * تابلت / حاسوب) بدل أن يتمدد الأفقي فقط.
         */
        val todayStageMaxWidth = 720.dp
        /** أقل عرض لبطاقة يوم في شبكة التقويم التكيّفية. */
        val calendarCardMinWidth = 340.dp
        /** أقل عرض لبطاقة دعاء في شبكة الأدعية التكيّفية. */
        val duaCardMinWidth = 320.dp
        /** أقصى عرض لنصوص القراءة الطويلة (دعاء/فقه) على الشاشات العريضة. */
        val readingMaxWidth = 840.dp
    }

    /** حركة Fluid — نفس منحنى التصميم `cubic-bezier(0.22, 1, 0.36, 1)`. */
    object Motion {
        val Fluid: Easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
        const val fastMs = 180
        const val normalMs = 300
        const val mediumMs = 420
        const val slowMs = 650

        // ------------------------------------------------------------------
        // Material 3 Expressive (2026): حركة نابضية (Spring) بنوعين:
        //  - Spatial: للحركة والانتقالات والأحجام (مرونة 0.8).
        //  - Effects: للتلاشي والتلوين (بدون ارتداد، استجابة أسرع).
        // القيم مطابقة لرموز ExpressiveMotionTokens الرسمية في material3 1.4.
        // ------------------------------------------------------------------
        const val SpatialDamping = 0.8f
        /** Expressive: النابض المكاني السريع يرتد أكثر (0.6)، الافتراضي/البطيء 0.8. */
        const val SpatialDampingFast = 0.6f
        const val SpatialStiffnessDefault = 380f
        const val SpatialStiffnessFast = 800f
        const val SpatialStiffnessSlow = 200f

        const val EffectsDamping = 1f
        const val EffectsStiffnessDefault = 1600f
        const val EffectsStiffnessFast = 3800f
        const val EffectsStiffnessSlow = 800f

        /** نابض مكاني: للانتقالات، تغيّر الحجم، حركة العناصر. */
        fun <T> spatial(
            stiffness: Float = SpatialStiffnessDefault,
            dampingRatio: Float = SpatialDamping
        ): SpringSpec<T> = spring(dampingRatio = dampingRatio, stiffness = stiffness)

        /** نابض تأثيرات: للتلاشي وتغيّر اللون والشفافية. */
        fun <T> effects(
            stiffness: Float = EffectsStiffnessDefault,
            dampingRatio: Float = EffectsDamping
        ): SpringSpec<T> = spring(dampingRatio = dampingRatio, stiffness = stiffness)
    }

    /** أسطح الزجاج الليلي والنهاري. */
    object Glass {
        val dark = Color(0xE60B1120)
        val darkSoft = Color(0xCC0B1120)
        val darkDeep = Color(0xF2070C18)
        val light = Color(0xE6FFFFFF)
        val lightSoft = Color(0xCCFFFFFF)
        val border = Color(0x1AFFFFFF)
        val borderStrong = Color(0x33FFFFFF)
        val highlight = Color(0x14FFFFFF)
        val highlightStrong = Color(0x24FFFFFF)
        val scrim = Color(0x99000000)
    }

    /** ألوان التوهج/الخلفية للشاشات غير السماوية. */
    object Backdrop {
        val top = Color(0xFF070B13)
        val bottom = Color(0xFF0B1020)
        val tealGlow = Color(0x0D2DD4BF)
        val indigoGlow = Color(0x0D6366F1)
    }

    /** ألوان الحالة والتمييز. */
    object Accent {
        val gold = Color(0xFFFFD700)
        val goldDeep = Color(0xFFFFA000)
        val goldSoft = Color(0x33FFD700)
        val teal = Color(0xFF2DD4BF)
        val sky = Color(0xFF38BDF8)
        val rose = Color(0xFFF43F5E)
        val violet = Color(0xFFA78BFA)
        val mint = Color(0xFF34D399)
        val amber = Color(0xFFFBBF24)
    }
}
