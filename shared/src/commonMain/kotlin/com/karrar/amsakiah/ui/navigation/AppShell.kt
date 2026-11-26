package com.karrar.amsakiah.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.navigation3.runtime.NavKey
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.karrar.amsakiah.ui.state.AppState
import com.karrar.amsakiah.ui.state.ToastKind
import com.karrar.amsakiah.ui.state.ToastMessage
import com.karrar.amsakiah.core.weather.WeatherInfo
import com.karrar.amsakiah.ui.adaptive.AppHeightSizeClass
import com.karrar.amsakiah.ui.adaptive.AppWidthSizeClass
import com.karrar.amsakiah.ui.adaptive.NavigationLayout
import com.karrar.amsakiah.ui.adaptive.rememberAppAdaptiveLayout
import com.karrar.amsakiah.ui.adaptive.supportsListDetail
import com.karrar.amsakiah.ui.adaptive.topBarHeight
import com.karrar.amsakiah.ui.components.CityPickerSheet
import com.karrar.amsakiah.ui.components.LocationPermissionDialog
import com.karrar.amsakiah.ui.components.DuaPlayerOverlay
import com.karrar.amsakiah.ui.components.GlassSurface
import com.karrar.amsakiah.ui.components.LiquidFloatingBottomBar
import com.karrar.amsakiah.ui.components.LiquidVerticalRail
import com.karrar.amsakiah.ui.motion.ambientMotionEnabled
import com.karrar.amsakiah.ui.motion.effectsSpec
import com.karrar.amsakiah.ui.motion.motionEffects
import com.karrar.amsakiah.ui.motion.pressScale
import com.karrar.amsakiah.ui.motion.motionSpatial
import com.karrar.amsakiah.ui.motion.motionSpatialFluid
import com.karrar.amsakiah.ui.motion.spatialSpec
import com.karrar.amsakiah.ui.motion.spatialFluidSpec
import com.karrar.amsakiah.ui.platform.PlatformBackHandler
import com.karrar.amsakiah.ui.platform.rememberReduceMotion
import com.karrar.amsakiah.ui.permissions.rememberPermissionController
import com.karrar.amsakiah.ui.player.DuaPlayerController
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.ui.screens.CalendarScreen
import com.karrar.amsakiah.ui.screens.ArcCalendarScreen
import com.karrar.amsakiah.ui.screens.DuasScreen
import com.karrar.amsakiah.ui.screens.FiqhScreen
import com.karrar.amsakiah.ui.screens.KeysScreen
import com.karrar.amsakiah.ui.screens.JinanDetailPlaceholder
import com.karrar.amsakiah.ui.screens.JinanScreen
import com.karrar.amsakiah.ui.screens.DailyDeedsScreen
import com.karrar.amsakiah.ui.screens.QuranScreen
import com.karrar.amsakiah.ui.screens.UnifiedReaderScreen
import com.karrar.amsakiah.ui.screens.SettingsScreen
import com.karrar.amsakiah.ui.screens.ShrinesQiblaScreen
import com.karrar.amsakiah.ui.screens.TasbihScreen
import com.karrar.amsakiah.ui.screens.TasksScreen
import com.karrar.amsakiah.ui.screens.TodayScreen
import com.karrar.amsakiah.ui.screens.WidgetsScreen
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararTokens
import com.karrar.amsakiah.ui.theme.LocalKararPalette
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// آخر وقت معالجة زر الرجوع — على مستوى الملف حتى لا يُعاد تهيئته مع كل Composition؛
// يمنع تنفيذ ضغطة واحدة مرتين (NavDisplay + معالج iOS).
private var lastBackMark: kotlin.time.TimeMark? = null

/**
 * الشل الرئيسي — Material 3 Expressive + Compose Adaptive.
 *
 * - **حالة تبويب صريحة** (`currentTab`) بدل استبدال جذر back stack: الانتقال
 *   إلى «اليوم» يعمل من أي تبويب دائمًا.
 * - **شريط عنوان موحّد واحد**: نفس الشريط يبقى ثابتًا، والعنوان وحده يتغيّر
 *   بحركة عند تبديل التبويب أو فتح دعاء (بلا شريط ثانٍ ولا كتل لونية).
 * - **خلفية واحدة full-bleed** خلف أشرطة النظام، والمحتوى وحده يُزاح.
 * - **مشغّل أدعية واحد** مرفوع إلى الشل: شريط مدمج ≤ 30dp، ويمكن فصله ليصبح
 *   كرة عائمة تُسحب إلى أي مكان.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AppShell(
    state: AppState,
    weather: WeatherInfo?,
    modifier: Modifier = Modifier
) {
    val layout = rememberAppAdaptiveLayout()
    val permissions = rememberPermissionController()
    val scope = rememberCoroutineScope()
    var gpsAfterGrant by remember { mutableStateOf(false) }

    LaunchedEffect(permissions.status.location) {
        if (permissions.status.location && gpsAfterGrant) {
            gpsAfterGrant = false
            state.detectLocation()
        }
    }

    // استشعار ذكي لعودة المستخدم للتطبيق (بعد فتح الإعدادات أو تشغيل GPS من النظام)
    LifecycleResumeEffect(Unit) {
        permissions.refresh()
        state.syncLocationSystemState()
        if (permissions.status.location && (gpsAfterGrant || state.settings.useGps)) {
            gpsAfterGrant = false
            scope.launch { state.detectLocation() }
        }
        onPauseOrDispose {}
    }
    // «تقليل الحركة» من النظام: تُلغى انزلاقات الشاشات والتبويبات فقط.
    val reduceMotion = rememberReduceMotion()

    // ---------- حالة التنقل: Navigation 3 (State-driven) ----------
    // الـback stack نفسه هو الحالة: SnapshotStateList يحفظه عبر rememberNavBackStack
    // (بعد عملية قتل/دوران الشاشة)، وكل تنقل هو تعديل على هذه القائمة فقط.
    val backStack = rememberNavBackStack(appNavConfiguration, AppRoute.Today)
    val currentRoute: AppRoute = backStack.lastOrNull() as? AppRoute ?: AppRoute.Today
    val currentTab = currentRoute.toDestination() ?: AppDestination.TODAY
    val widgetsOpen = currentRoute == AppRoute.Widgets
    // اتجاه الانتقال (1 للأمام، -1 للخلف) لتحديد حركة المنصة الأصلية.
    var navDirection by remember { mutableIntStateOf(1) }

    var duaDetailOpen by remember { mutableStateOf(false) }
    var duaDetailTitle by remember { mutableStateOf<String?>(null) }
    var duaBackSignal by remember { mutableIntStateOf(0) }
    // ورقة اختيار المدينة الموحّدة — تفتحها شريحة المدينة في «اليوم» والضبط.
    var showCityPicker by remember { mutableStateOf(false) }

    // حالة البحث الموحّد — يتحكم بها شريط العنوان ويقرأها شاشات (القرآن/الجنان/الأدعية).
    // حالة البحث — text/mode يصمدان أمام تغيّر الإعدادات (تدوير الشاشة) فتبقى نتائج البحث.
    val searchQuery = rememberSaveable { mutableStateOf("") }
    val searchMode = remember { mutableStateOf(false) }

    // مشغّل واحد لكل التطبيق — يبقى يعمل عبر التنقل ويُحرَّر عند إغلاق الشل.
    val player = remember { DuaPlayerController() }
    DisposableEffect(player) {
        onDispose { player.release() }
    }

    // «اليوم» ولا شيء فوقه؛ أوفتح دعاء/قارئ الجنان يتحولان إلى edge-to-edge
    // بشاشتهما الخاصة (لا شريط التطبيق الرئيسي).
    val isToday = currentRoute == AppRoute.Today && !duaDetailOpen
    // «اليوم» يخفي شريط العنوان فقط — شريط التنقل السفلي يبقى كما في المرجع.
    // وضع القراءة (دعاء/قارئ الجنان/قوس التقويم/قارئ القرآن) يخفي شريطَي الواجهة
    // والتنقل الجانبي في **كل فئات العرض** — لا في المدمج فقط — وإلا بقي شريط
    // الشل مكررًا فوق رأس القارئ الداخلي في landscape (width EXPANDED × height
    // COMPACT) مع زري رجوع (DF-09). الشاشات متعددة الألواح (EXPANDED+) تبقى تعرض
    // القائمة بجانب التفصيل عبر listDetailSceneStrategy، لكن بلا أشرطة شل فوقها.
    // القارئ داخل شاشة القرآن يملك واجهته الخاصة — الشل يخفي أشرطته في وضع القراءة.
    var quranReaderOpen by remember { mutableStateOf(false) }
    val immersive =
        (currentRoute == AppRoute.Duas && duaDetailOpen) ||
            (currentRoute is AppRoute.JinanReader) ||
            (currentRoute == AppRoute.ArcCalendar) ||
            (currentRoute == AppRoute.Quran && quranReaderOpen)
    // شريط العنوان الموحّد يظهر في جميع الشاشات الفرعية والتنقلية ما عدا شاشة اليوم والانغماس.
    val showTopBar = !isToday && !immersive

    // ---------- التكيّف الرسمي (M3 Expressive + Compose Adaptive) ----------
    // هاتف (Compact) → شريط سفلي؛ متوسط (foldables/عريض) → Rail 64dp؛
    // واسع + Large/XL (ChromeOS/DeX/لابتوب) → Rail 72dp. الانتقالات نابضة
    // عبر motionSpatial/motionEffects — بلا أي إعادة تحميل للشاشات.
    // وضع القراءة (immersive) يخفي الـRail والتنقل السفلي في كل فئات العرض (DF-09).
    val showRail = layout.navigationLayout == NavigationLayout.RAIL && !immersive
    val bottomNavVisible = !showRail && !immersive
    val railWidthTarget =
        if (layout.widthSizeClass == AppWidthSizeClass.MEDIUM) KararTokens.Size.railWidthMedium
        else KararTokens.Size.railWidthExpanded

    // ---------- الإنسِتس ----------
    // المعادلة الرسمية: شريط الحالة ∪ شريط العنوان (captionBar — يظهر في DeX/Freeform/الحواسيب)
    // ∪ الثقب. الأسفل: شريط التنقل ∪ الثقب. كلٌّ كامل full-bleed، والتزاحم على المحتوى فقط.
    val topInsets = WindowInsets.statusBars
        .union(WindowInsets.captionBar)
        .union(WindowInsets.displayCutout)
    val bottomInsets = WindowInsets.navigationBars
        .union(WindowInsets.displayCutout)
    val statusTop = topInsets.asPaddingValues().calculateTopPadding()
    val navigationBottom = bottomInsets.asPaddingValues().calculateBottomPadding()

    val layoutDirection = LocalLayoutDirection.current
    // مصدر واحد للمسافات الآمنة الأفقية: safeDrawing يشمل الثقب + أشرطة النظام
    // الجانبية (أفقي/DeX/نافذة حرة). الخلفية تبقى full-bleed، والإزاحة للمحتوى فقط.
    val safeDrawing = WindowInsets.safeDrawing.asPaddingValues()
    val safeStart = safeDrawing.calculateStartPadding(layoutDirection)
    val safeEnd = safeDrawing.calculateEndPadding(layoutDirection)

    val settings = state.settings

    // ارتفاع الشريط العلوي — ديناميكي حسب فئة الارتفاع الرسمية (لا ثابت 56dp).
    val asTopBarHeight = layout.topBarHeight()

    // ارتفاع شريط التنقل السفلي + هوامشه — يُحجز للمحتوى والمشغّل.
    // يُلغى تمامًا على الشاشات الجانبية (Rail) أو في وضع القراءة، فتتمدد المنطقة بالكامل.
    val navBarReserve = if (bottomNavVisible) {
        navigationBottom + 12.dp + KararTokens.Size.navBarHeight
    } else {
        0.dp
    }

    // حتى في وضع القراءة (لا شريط سفلي) لا يُنجرف المحتوى تحت شريط الحركات/
    // الجيبتشر هو المتحكم — فنحجز مسافة النظام ولو كان الشريط مخفيًا.
    val playerDockReserve = if (player.hasTrack) KararTokens.Size.playerDockHeight + 10.dp else 0.dp
    val fullBottom = navBarReserve + 36.dp + playerDockReserve
    val immersiveBottom = navigationBottom + 14.dp
    val bottomReserve = if (fullBottom >= immersiveBottom) fullBottom else immersiveBottom

    // وضع القراءة/القوس (immersive): لا شريط عنوان — لا نحجز فراغه تحت شريط
    // الحالة (كان حجزًا فارغًا في كل فئات العرض الآن).
    val contentTop = when {
        isToday -> statusTop + 6.dp
        immersive -> statusTop + 6.dp
        // ديناميكي حسب فئة الارتفاع الرسمية مع مسافة أمان كافية للشاشات الأفقية
        else -> statusTop + 8.dp + asTopBarHeight + 16.dp
    }
    val contentPadding = PaddingValues(
        start = safeStart,
        top = contentTop,
        end = safeEnd,
        bottom = bottomReserve
    )
    fun select(destination: AppDestination) {
        val route = destination.toRoute()
        if (currentRoute == route && backStack.size == 1) return
        navDirection = if (destination.ordinal >= currentTab.ordinal) 1 else -1
        // كل علامة تبويب قابلة للعودة بـback إلى «اليوم» ثم الخروج:
        // الـback stack أبدًا [اليوم] أو [اليوم، التبويب]. الشاشات الفرعية
        // (المفاتيح ← الأدعية…) تدفع فوق ذلك وتُغلق بالرجوع خطوة واحدة.
        Snapshot.withMutableSnapshot {
            if (destination == AppDestination.TODAY) {
                backStack.clear()
                backStack.add(AppRoute.Today)
            } else {
                backStack.clear()
                backStack.add(AppRoute.Today)
                backStack.add(route)
            }
        }
    }

    fun openSettings() = select(AppDestination.SETTINGS)

    /** فرعية «المفاتيح» تُدفع فوق النافذة. */
    fun openKeysChild(child: AppRoute) {
        if (backStack.lastOrNull() == child) return
        navDirection = 1
        duaDetailOpen = false
        duaDetailTitle = null
        backStack.add(child)
    }

    /** «الودجتات» شاشة فرعية تُدفع فوق الضبط — ليست تبويبًا. */
    fun openWidgets() {
        if (backStack.lastOrNull() == AppRoute.Widgets) return
        navDirection = 1
        duaDetailOpen = false
        duaDetailTitle = null
        backStack.add(AppRoute.Widgets)
    }

    // آخر وقت معالجة زر الرجوع — يمنع تنفيذ ضغطة واحدة مرتين (NavDisplay + معالج iOS).
    fun back() {
        // تفاصيل الدعاء أولًا: يُدار داخل شاشة الأدعية نفسها.
        if (duaDetailOpen) {
            duaBackSignal++
            lastBackMark = kotlin.time.TimeSource.Monotonic.markNow()
            return
        }
        if (backStack.size > 1) {
            // نفس الضغطة قد تصل عبر NavDisplay ومعالج iOS معًا — لا نسمح بقفزة مستويين.
            val now = kotlin.time.TimeSource.Monotonic.markNow()
            if ((lastBackMark?.elapsedNow()?.inWholeMilliseconds ?: Long.MAX_VALUE) < 350) return
            lastBackMark = now
            // لا نلمس navDirection عند الرجوع: popTransitionSpec يقرأه
            // ويحسب المعكوس الحرفي لسلايد الدفع — فلا يتثبّت كدفع جديد (DF-16).
            backStack.removeAt(backStack.lastIndex)
        }
    }

    // NavDisplay يتولى زر الرجوع النظامي؛ نُبقي معالجًا احتياطيًا لتغطية iOS
    // وتفاصيل الدعاء في الوقت نفسه (لا يُستدعى اثنان لنفس الضغطة).
    PlatformBackHandler(enabled = duaDetailOpen || backStack.size > 1) { back() }

    // إعادة ضبط وضع البحث عند تغيير الشاشة — لا يبقى حقل البحث معلقًا على شاشة أخرى.
    LaunchedEffect(currentRoute) {
        searchMode.value = false
        searchQuery.value = ""
    }

    // ---------- الهيكل ----------
    androidx.compose.runtime.CompositionLocalProvider(
        LocalSearchQuery provides searchQuery
    ) {
    Box(modifier = modifier.fillMaxSize()) {
        // خلفية واحدة فقط: سماوية ممتدة في «اليوم» خلف كامل الشل والشريط الجانبي،
        // وتدرج اللوحة الحالية في بقية الشاشات — فلا كتل لونية على الحواف نهائيًا.
        if (isToday) {
            TodaySkyBackdrop(state = state, weather = weather)
        } else {
            AppBackdrop()
        }

        Row(Modifier.fillMaxSize()) {
            // التنقل الجانبي: يظهر/يختفي بانزلاق نابض وتلاشٍ — لا يُعاد تركيب
            // الشاشات ولا يتغيّر موضع المحتوى فجأة (وزن 1f ثابت).
            AnimatedVisibility(
                visible = showRail,
                enter = slideInHorizontally(animationSpec = motionSpatial()) { -it } +
                    fadeIn(animationSpec = motionEffects()),
                exit = slideOutHorizontally(animationSpec = motionSpatial()) { -it } +
                    fadeOut(animationSpec = motionEffects())
            ) {
                SideNavigation(
                    current = currentTab,
                    targetWidth = railWidthTarget,
                    onSelect = ::select,
                    safeStart = safeStart,
                    safeEnd = safeEnd,
                    topInset = statusTop,
                    bottomInset = navigationBottom
                )
            }

            // لا BoxWithConstraints هنا: كل SubcomposeLayout إضافي يؤجّل تركيب
            // الشل بالكامل عند كل قياس. القيد بالأعراض يُعبَّر عنه بمُعدِّلات
            // عادية، فيبقى مسار القياس خفيفًا أثناء الانتقالات وتغيير الحجم.
            // بلا حد أقصى للعرض (M3 Adaptive: المحتوى يملأ العرض المتاح في
            // الهواتف والأفقي؛ النص الطويل وحده يُقيَّد عبر readingMaxWidth).
            Box(Modifier.weight(1f).fillMaxHeight()) {
                val contentModifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()

                // القائمة بجانب التفصيل فقط على الشاشات العريضة/التابلت (EXPANDED+).
                // الهاتف بالعرض (Medium ≈ 600dp — مثل 1284×576) يعرض لوحة واحدة دائمًا،
                // فلا تنفتح «مفاتيح الجنان» و«القارئ» كصفحتين على هاتف عادي (DF-10).
                // الـstrategy تُحفظ دائمًا (قاعدة remember الموحدة) لكن تفعيلها مشروط.
                val listDetailSceneStrategy = rememberListDetailSceneStrategy<NavKey>()
                // الهاتف بالعرض (width EXPANDED لكن height COMPACT — مثل 1280×576) لا يفتح صفحتين؛
                // القائمة بجانب التفصيل تُفعّل فقط على الشاشات العريضة ذات الارتفاع الكافي
                // (تابلت/لابتوب/Foldable مطويّة مفتوحًا) — القارئ دائمًا ملء الشاشة على الهاتف.
                val twoPane = layout.widthSizeClass.supportsListDetail &&
                    layout.heightSizeClass != AppHeightSizeClass.COMPACT

                // Nav3: كل شاشة Scene مبني من الـback stack؛ الانتقالات سلايد
                // موحّد متطابق ثنائي الاتجاه (دخول كامل / خروج ثلث + تلاشٍ) (DF-16).
                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .then(contentModifier),
                    contentAlignment = Alignment.TopCenter,
                    sceneStrategies = if (twoPane) listOf(listDetailSceneStrategy) else emptyList(),
                    onBack = ::back,
                    // الخفة: بلا SizeTransform — لا يُعاد قياس/قصّ المشهد بين الشاشات،
                    // والشاشات هنا كلها بعرض واحد أصلًا، فإلغاؤه يوفّر طبقة قصّ لكل إطار.
                    sizeTransform = null,
                    transitionSpec = { sceneTransition(navDirection, reduceMotion) },
                    popTransitionSpec = { sceneTransition(-navDirection, reduceMotion) },
                    entryProvider = entryProvider {
                        entry(AppRoute.Today) {
                            // شريحة المدينة في «اليوم» تفتح ورقة اختيار المدينة
                            // نفسها (بحث/موقعي)، لا كشف GPS ولا إعدادات.
                            TodayScreen(
                                state = state,
                                weather = weather,
                                onOpenArcCalendar = {
                                    // شاشة فرعية تُدفع للأمام — سلايد بنفس اتجاه بقية الدفعات.
                                    navDirection = 1
                                    backStack.add(AppRoute.ArcCalendar)
                                },
                                onOpenSettings = { showCityPicker = true },
                                contentPadding = contentPadding
                            )
                        }
                        entry(AppRoute.Calendar) {
                            CalendarScreen(state = state, contentPadding = contentPadding)
                        }
                        entry(AppRoute.ArcCalendar) {
                            ArcCalendarScreen(state = state, onBack = ::back, contentPadding = contentPadding)
                        }
                        entry(AppRoute.Keys) {
                            KeysScreen(
                                contentPadding = contentPadding,
                                onOpenDuas = { openKeysChild(AppRoute.Duas) },
                                onOpenQuran = { openKeysChild(AppRoute.Quran) },
                                onOpenQibla = { openKeysChild(AppRoute.Qibla) },
                                onOpenTasbih = { openKeysChild(AppRoute.Tasbih) },
                                onOpenTasks = { openKeysChild(AppRoute.Tasks) },
                                onOpenJinan = { openKeysChild(AppRoute.Jinan) },
                                onOpenDailyDeeds = { openKeysChild(AppRoute.DailyDeeds) }
                            )
                        }
                        entry(AppRoute.Duas) {
                            DuasScreen(
                                contentPadding = contentPadding,
                                player = player,
                                backSignal = duaBackSignal,
                                onDetailVisibilityChanged = { open ->
                                    duaDetailOpen = open
                                    if (!open) duaDetailTitle = null
                                },
                                onDetailTitleChanged = { duaDetailTitle = it }
                            )
                        }
                        entry(AppRoute.Quran) {
                            QuranScreen(
                                state = state,
                                contentPadding = contentPadding,
                                onReaderVisibilityChanged = { quranReaderOpen = it }
                            )
                        }
                        entry(AppRoute.Tasbih) {
                            TasbihScreen(state = state, contentPadding = contentPadding)
                        }
                        entry(AppRoute.Qibla) {
                            ShrinesQiblaScreen(state = state, contentPadding = contentPadding)
                        }
                        entry(AppRoute.Tasks) {
                            val openReader: (com.karrar.amsakiah.core.jinan.JinanItem) -> Unit = { item ->
                                if (item.id in 1001..1114) {
                                    openKeysChild(AppRoute.Quran)
                                } else {
                                    openKeysChild(AppRoute.JinanReader(item.id, item.title))
                                }
                            }
                            TasksScreen(
                                state = state,
                                contentPadding = contentPadding,
                                onOpenJinanReader = openReader
                            )
                        }
                        entry(AppRoute.Fiqh) {
                            FiqhScreen(state = state, contentPadding = contentPadding)
                        }
                        entry(AppRoute.Settings) {
                            SettingsScreen(
                                state = state,
                                contentPadding = contentPadding,
                                onOpenWidgets = ::openWidgets
                            )
                        }
                        entry(AppRoute.Widgets) {
                            WidgetsScreen(state = state, contentPadding = contentPadding)
                        }
                        entry(
                            AppRoute.Jinan,
                            metadata = ListDetailSceneStrategy.listPane(
                                detailPlaceholder = { JinanDetailPlaceholder() }
                            )
                        ) {
                            // تحميل محتوى مفاتيح الجنان (2.9MB) عند الحاجة فقط.
                            LaunchedEffect(Unit) { state.prepareJinanContent() }
                            JinanScreen(
                                contentPadding = contentPadding,
                                onOpenReader = { item ->
                                    openKeysChild(AppRoute.JinanReader(item.id, item.title))
                                }
                            )
                        }
                        entry<AppRoute.JinanReader>(
                            metadata = ListDetailSceneStrategy.detailPane()
                        ) { route ->
                            // كل مقتطف يقرأ نفسه من الملف المُدمج بالكامل (ولا تُحمل من جديد).
                            val favIds = state.settings.favoriteJinanItems
                            val isFav = route.contentId in favIds
                            UnifiedReaderScreen(
                                contentId = route.contentId,
                                title = route.title,
                                subtitle = route.title,
                                onBack = ::back,
                                isFavorite = isFav,
                                onToggleFavorite = {
                                    state.update { s ->
                                        val next = if (isFav) s.favoriteJinanItems - route.contentId else s.favoriteJinanItems + route.contentId
                                        s.copy(favoriteJinanItems = next)
                                    }
                                },
                                onSaveNote = { note ->
                                    state.update { s -> s.copy(notes = s.notes + note) }
                                    state.showToast("حُفظت في «المهام والملاحظات»", ToastKind.SUCCESS)
                                },
                                initialQuery = route.initialQuery
                            )
                        }
                        entry(AppRoute.DailyDeeds) {
                            // أعمال الليلة/اليوم/يوم الأسبوع — من «مفاتيح الجنان» بمطابقة كاملة.
                            LaunchedEffect(Unit) { state.prepareJinanContent() }
                            DailyDeedsScreen(
                                state = state,
                                contentPadding = contentPadding,
                                onOpenReader = { item, query ->
                                    openKeysChild(AppRoute.JinanReader(item.id, item.title, query))
                                }
                            )
                        }
                    }
                )

                if (showTopBar) {
                    val childTitle = when (currentRoute) {
                        AppRoute.Duas -> duaDetailTitle?.takeIf { it.isNotBlank() } ?: "الأدعية والمناجاة"
                        AppRoute.Quran -> "القرآن الكريم"
                        AppRoute.Tasbih -> "المسبحة"
                        AppRoute.Qibla -> "القبلة والمراقد"
                        AppRoute.Tasks -> "المهام والملاحظات"
                        AppRoute.Jinan -> "مفاتيح الجنان"
                        // عنوان واحد ديناميكي للتاريخ — بلا تكرار داخل الشاشة.
                        AppRoute.DailyDeeds -> "أعمال اليوم " + dailyDeedsTitle(settings)
                        AppRoute.Calendar -> "التقويم"
                        is AppRoute.JinanReader -> currentRoute.title
                        else -> null
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        LocalKararPalette.current.backgroundTop.copy(alpha = 0.95f),
                                        LocalKararPalette.current.backgroundTop.copy(alpha = 0.70f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(top = statusTop + 8.dp, bottom = 12.dp, start = 12.dp + safeStart, end = 12.dp + safeEnd),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val supportsSearch = currentRoute in setOf(AppRoute.Quran, AppRoute.Jinan, AppRoute.Duas)
                            UnifiedTopBar(
                                title = childTitle ?: if (widgetsOpen) "الودجتات" else currentTab.title,
                                canGoBack = backStack.size > 1 || duaDetailOpen,
                                onBack = ::back,
                                actionIcon = if (currentTab == AppDestination.SETTINGS) Icons.Filled.Widgets else Icons.Filled.Tune,
                                actionLabel = if (currentTab == AppDestination.SETTINGS) "الودجتات" else "الضبط",
                                onAction = {
                                    if (currentTab == AppDestination.SETTINGS) openWidgets() else openSettings()
                                },
                                height = asTopBarHeight,
                                modifier = Modifier,
                                supportsSearch = supportsSearch,
                                searchMode = searchMode.value,
                                searchQuery = searchQuery.value,
                                onSearchChange = { searchQuery.value = it },
                                onSearchToggle = {
                                    searchMode.value = !searchMode.value
                                    if (!searchMode.value) searchQuery.value = ""
                                }
                            )
                        }
                    }
                }
            }
        }

        // مشغّل واحد: مدمج فوق شريط التنقل، أو كرة عائمة تُسحب.
        // لا يظهر فوق تفاصيل الدعاء — زر التشغيل هناك داخل القارئ نفسه.
        if (player.hasTrack && !duaDetailOpen) {
            DuaPlayerOverlay(
                controller = player,
                bottomReserve = navBarReserve,
                horizontalPadding = 12.dp + safeStart,
                modifier = Modifier.fillMaxSize()
            )
        }

        // شريط التنقل السفلي: يظهر/يختفي بـ«سائل» (لا قفز ولا سيولة زائدة).
        AnimatedVisibility(
            visible = bottomNavVisible,
            enter = slideInVertically(animationSpec = motionSpatialFluid()) { it / 2 } +
                fadeIn(animationSpec = motionEffects()),
            exit = slideOutVertically(animationSpec = motionSpatialFluid()) { it / 2 } +
                fadeOut(animationSpec = motionEffects()),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = 12.dp + safeStart,
                    end = 12.dp + safeEnd,
                    bottom = navigationBottom + 12.dp
                )
        ) {
            FloatingBottomBar(
                current = currentTab,
                onSelect = ::select
            )
        }

        // التنبيهات العائمة: أخطاء الموقع/IP وأي حدث يخص المستخدم تظهر ككبسولة
        // بيضاء أنيقة في الأعلى — لا إغلاق للتطبيق ولا أشرطة أخطاء منبثقة.
        AppToastHost(
            state = state,
            onOpenAppSettings = { permissions.openAppSettings() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(
                    top = statusTop + (if (showTopBar) asTopBarHeight + 16.dp else 14.dp),
                    start = 16.dp + safeStart,
                    end = 16.dp + safeEnd
                )
        )

        // ورقة اختيار المدينة — نفس المكوّن في «اليوم» والضبط (بحث/موقعي).
        CityPickerSheet(
            visible = showCityPicker,
            state = state,
            onDismiss = { showCityPicker = false }
        )

        // نافذة طلب إذن الموقع الجغرافي الموحّدة والأصلية لكافة شاشات التطبيق
        LocationPermissionDialog(
            visible = state.showLocationPrompt,
            onDismissRequest = { state.showLocationPrompt = false },
            onGrant = {
                state.showLocationPrompt = false
                if (permissions.status.location) {
                    scope.launch { state.detectLocation() }
                } else {
                    gpsAfterGrant = true
                    permissions.requestLocation()
                }
            },
            onOpenSettings = {
                state.showLocationPrompt = false
                permissions.openAppSettings()
            }
        )
    }
    }
}

/**
 * انتقالات «سلايد طباقات» ناعمة — مثل انزلاق الصفحات في iOS، بنابض سائل:
 * الداخلة تنزلق كاملة من جهة الاتجاه فوق الخارجة، والخارجة تتراجع ثلث الشاشة
 * بلطف (لا تلاشٍ مفاجئ ولا قطع). الرجوع معكوس حرفيًا للدفع بنفس المنحنى.
 */
private fun sceneTransition(direction: Int, reduceMotion: Boolean): ContentTransform {
    if (reduceMotion) {
        val fade = tween<Float>(durationMillis = KararTokens.Motion.fastMs)
        return fadeIn(animationSpec = fade).togetherWith(fadeOut(animationSpec = fade))
    }
    val sign = if (direction >= 0) 1 else -1
    return (
        slideInHorizontally(animationSpec = spatialFluidSpec(reduceMotion)) { sign * it }
        ).togetherWith(
        slideOutHorizontally(animationSpec = spatialFluidSpec(reduceMotion)) { -sign * it / 3 } +
            fadeOut(animationSpec = effectsSpec(reduceMotion))
    )
}

/** خلفية السماء الحية لشاشة «اليوم»: تمتد عبر كامل جذر AppShell خلف الشريط الجانبي والعلوي بلا أي كتلة سوداء. */
@Composable
private fun TodaySkyBackdrop(state: AppState, weather: WeatherInfo?) {
    val settings = state.settings
    val zone = remember(settings.timeZoneId) { PrayerCalculator.zoneOf(settings) }
    val nowLdt: LocalDateTime = remember(state.now, zone) { state.now.toLocalDateTime(zone) }
    val nowSeconds = PrayerCalculator.secondsOfDay(nowLdt)
    val date: LocalDate = nowLdt.date
    val todayTimes = remember(settings, date) {
        PrayerCalculator.adjustedTimes(PrayerCalculator.timesFor(settings, date), settings)
    }
    val isNight = remember(todayTimes, nowSeconds) {
        val sunrise = com.karrar.amsakiah.core.arc.secondsOf(todayTimes["sunrise"] ?: 6.0)
        val maghrib = com.karrar.amsakiah.core.arc.secondsOf(todayTimes["maghrib"] ?: 18.25)
        !(nowSeconds >= sunrise && nowSeconds < maghrib)
    }
    val (skyTop, skyBottom, skyGlow) = com.karrar.amsakiah.ui.theme.SkyTheme.getDynamicSkyColors(nowSeconds.toFloat(), todayTimes)

    Box(
        Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(Brush.verticalGradient(listOf(skyTop, skyBottom)))
                if (isNight) {
                    com.karrar.amsakiah.ui.theme.SkyTheme.celestialStars.forEach { star ->
                        drawCircle(
                            color = Color.White.copy(alpha = star.maxAlpha),
                            radius = star.radius * 1.1.dp.toPx(),
                            center = Offset(star.xPercent * size.width / 100f, star.yPercent * size.height / 100f)
                        )
                    }
                }
                drawRect(
                    Brush.verticalGradient(
                        listOf(Color(0x3D000000), Color.Transparent),
                        startY = 0f,
                        endY = 78.dp.toPx()
                    )
                )
                drawRect(
                    Brush.radialGradient(
                        colorStops = arrayOf(0f to skyGlow, 0.8f to Color.Transparent),
                        center = Offset(size.width / 2f, size.height * 0.35f),
                        radius = size.maxDimension * 0.75f
                    )
                )
            }
    ) {
        com.karrar.amsakiah.ui.components.WeatherEffects(
            code = weather?.code,
            isNight = isNight,
            modifier = Modifier.matchParentSize()
        )
    }
}

/** خلفية الشاشات غير السماوية: تدرج اللوحة الحالية + توهجان ناعمان. */
@Composable
private fun AppBackdrop() {
    val palette = LocalKararPalette.current
    Box(
        Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(Brush.verticalGradient(listOf(palette.backgroundTop, palette.backgroundBottom)))
                val radius = size.maxDimension * 0.62f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(palette.tealGlow, Color.Transparent),
                        center = Offset(size.width * 0.12f, size.height * 0.10f),
                        radius = radius
                    ),
                    radius = radius,
                    center = Offset(size.width * 0.12f, size.height * 0.10f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(palette.indigoGlow, Color.Transparent),
                        center = Offset(size.width * 0.88f, size.height * 0.82f),
                        radius = radius
                    ),
                    radius = radius,
                    center = Offset(size.width * 0.88f, size.height * 0.82f)
                )
            }
    )
}

/**
 * شريط العنوان الموحّد: زر رجوع دائري + كبسولة عنوان واحدة + زر إجراء.
 * العنوان وحده يتبدّل داخل نفس الكبسولة بحركة انزلاق نابضة؛
 * وإذا كانت الشاشة تدعم البحث تظهر **دائرة بحث** بجانب الاسم تتحول الكبسولة معها لحقل بحث.
 */
@Composable
private fun UnifiedTopBar(
    title: String,
    canGoBack: Boolean,
    onBack: () -> Unit,
    actionIcon: ImageVector,
    actionLabel: String,
    onAction: () -> Unit,
    height: Dp = KararTokens.Size.topBarHeight,
    modifier: Modifier = Modifier,
    supportsSearch: Boolean = false,
    searchMode: Boolean = false,
    searchQuery: String = "",
    onSearchChange: (String) -> Unit = {},
    onSearchToggle: () -> Unit = {}
) {
    // «تقليل الحركة»: عنوان الشريط يظهر/يختفي بتلاشٍ بلا انزلاق.
    val reduceMotion = rememberReduceMotion()
    // تركيز تلقائي على حقل البحث عند تفعيله — تظهر لوحة المفاتيح فورًا بلا نقرة إضافية.
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(searchMode) {
        if (searchMode) focusRequester.requestFocus()
    }
    // عزل تعديلات حقل البحث في modifier مستقل — لا يتعارض مع تخطيط الكبسولة
    // ولا يُسبّب إعادة قياس الشريط عند كل حرف (Compose-first, 1.12.2).
    val searchOverlayModifier = Modifier
        .fillMaxHeight()
        .focusRequester(focusRequester)
    Row(
        modifier = modifier
            .widthIn(max = KararTokens.Size.topBarMaxWidth)
            .fillMaxWidth()
            .height(height),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!searchMode) {
            // مساحة ثابتة لزر الرجوع حتى لا يقفز العنوان أفقياً عند ظهوره واختفائه
            Box(
                modifier = Modifier.size(KararTokens.Size.circleButton),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = canGoBack,
                    enter = scaleIn(initialScale = 0.82f, animationSpec = motionSpatialFluid()) +
                        fadeIn(animationSpec = motionEffects()),
                    exit = scaleOut(targetScale = 0.82f, animationSpec = motionSpatialFluid()) +
                        fadeOut(animationSpec = motionEffects())
                ) {
                    CircleGlassButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        label = "رجوع",
                        onClick = onBack
                    )
                }
            }
        }

        AnimatedContent(
            targetState = searchMode,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            transitionSpec = {
                (scaleIn(initialScale = 0.96f, animationSpec = spatialFluidSpec(reduceMotion)) +
                    fadeIn(animationSpec = effectsSpec(reduceMotion)))
                    .togetherWith(
                        scaleOut(targetScale = 0.96f, animationSpec = spatialFluidSpec(reduceMotion)) +
                            fadeOut(animationSpec = effectsSpec(reduceMotion))
                    )
            },
            label = "topbar-search-mode"
        ) { isSearch ->
            if (isSearch) {
                // وضع البحث: الكبسولة كلها تتحول لحقل بحث أنيق بجانب الاسم.
                GlassSurface(
                    modifier = searchOverlayModifier.fillMaxSize(),
                    shape = CircleShape,
                    elevation = 14.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = KararColors.Gold,
                            modifier = Modifier.size(19.dp)
                        )
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = KararColors.TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                textDirection = androidx.compose.ui.text.style.TextDirection.Rtl
                            ),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(KararColors.Gold),
                            modifier = Modifier.weight(1f),
                            decorationBox = { inner ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        "ابحث…",
                                        color = KararColors.TextMuted,
                                        fontSize = 14.sp
                                    )
                                }
                                inner()
                            }
                        )
                    }
                }
            } else {
                GlassSurface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    elevation = 14.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            AnimatedContent(
                                targetState = title,
                                transitionSpec = {
                                    (
                                        slideInVertically(animationSpec = spatialSpec(reduceMotion)) { -it / 2 } +
                                            fadeIn(animationSpec = effectsSpec(reduceMotion))
                                        ).togetherWith(
                                        slideOutVertically(animationSpec = spatialSpec(reduceMotion)) { it / 2 } +
                                            fadeOut(animationSpec = effectsSpec(reduceMotion))
                                    )
                                },
                                label = "topbar-title",
                                contentAlignment = Alignment.Center
                            ) { current ->
                                val adaptiveSize = when {
                                    current.length <= 12 -> 15.sp
                                    current.length <= 20 -> 13.5.sp
                                    current.length <= 30 -> 11.5.sp
                                    current.length <= 40 -> 9.5.sp
                                    else -> 8.5.sp
                                }
                                Text(
                                    text = current,
                                    color = KararColors.TextPrimary,
                                    fontSize = adaptiveSize,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        if (searchMode) {
            CircleGlassButton(
                icon = Icons.Filled.Close,
                label = "إغلاق البحث",
                onClick = onSearchToggle
            )
        } else if (supportsSearch) {
            CircleGlassButton(
                icon = Icons.Filled.Search,
                label = "بحث",
                onClick = onSearchToggle
            )
        } else {
            CircleGlassButton(
                icon = actionIcon,
                label = actionLabel,
                onClick = onAction
            )
        }
    }
}

/** زر دائري زجاجي منفصل. */
@Composable
private fun CircleGlassButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = androidx.compose.runtime.remember {
        androidx.compose.foundation.interaction.MutableInteractionSource()
    }
    GlassSurface(
        modifier = Modifier.size(KararTokens.Size.circleButton).pressScale(interactionSource, 0.92f),
        shape = CircleShape,
        elevation = 12.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = KararColors.TextPrimary.copy(alpha = 0.92f),
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

/**
 * الشريط السفلي — «Liquid Floating Glass» (تصميم شريط_تنقل_مائي_عائم_مع_دليل_المطور.html):
 *
 * - كبسولة عائمة زجاجية بتدرّج خفيف وحدود لامعة وظل بنفسجي.
 * - مؤشر مائي ينزلق بنابض هيدروليكي (damping 0.62) خلف العنصر النشط.
 * - العنصر النشط: أيقونة مكبّرة (1.14) + اسمها بجانبها؛ غير النشط: أيقونة + تسمية صغيرة.
 * - سحب بالإصبع على الشريط ينتقل بين الوجهات (.PointerInput).
 */
@Composable
private fun FloatingBottomBar(
    current: AppDestination,
    onSelect: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidFloatingBottomBar(
        current = current,
        onSelect = onSelect,
        modifier = modifier
    )
}

/** الشريط الجانبي — «Liquid Floating Glass» رأسيًا (كبسولة عائمة + مؤشر مائي ينزلق). */
@Composable
private fun SideNavigation(
    current: AppDestination,
    targetWidth: Dp,
    onSelect: (AppDestination) -> Unit,
    safeStart: Dp,
    safeEnd: Dp,
    topInset: Dp,
    bottomInset: Dp
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(targetWidth + 24.dp + safeStart + safeEnd)
            .padding(
                start = 10.dp + safeStart,
                end = 10.dp + safeEnd,
                top = 12.dp + topInset,
                bottom = 12.dp + bottomInset
            ),
        contentAlignment = Alignment.Center
    ) {
        LiquidVerticalRail(
            current = current,
            onSelect = onSelect,
            modifier = Modifier.width(targetWidth)
        )
    }
}

// =====================================================================
//  التنبيهات العائمة (كبسولة بيضاء أنيقة) + حركة تحديد الموقع
// =====================================================================

/**
 * مضيف التنبيهات — يقرأ الحالة من النواة (`state.toast` / `state.locating`).
 *
 * النظام:
 * - كبسولة داكنة (#121A2C) `rounded-[20dp]` بحدود teal خفيف وظل عميق —
 *   نفس شخصية شريط التنقل والهيرو، فلا يذوب في أي خلفية.
 * - أيقونة ملوّنة داخل دائرة `tint.copy(alpha = 0.16f)` — لا نعيد استخدام
 *   لون الخطأ كاملًا على الخلفية الداكنة.
 * - دخول/خروج بانزلاق عمودي نابض + تلاشٍ (Motion.spatial).
 * - عند الخطأ يظهر زر فعلي «فتح الإعدادات» بدل مجرد نص.
 * - أثناء تحديد الموقع تظهر كبسولة «رادار» حلقات نابضة.
 */
@Composable
private fun AppToastHost(
    state: AppState,
    onOpenAppSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val toast = state.toast
    val locating = state.locating

    LaunchedEffect(toast?.id) {
        val current = toast ?: return@LaunchedEffect
        delay(5_000L)
        if (state.toast?.id == current.id) state.dismissToast()
    }

    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(
            visible = locating || toast != null,
            enter = expandHorizontally(
                expandFrom = Alignment.CenterHorizontally,
                animationSpec = motionSpatialFluid()
            ) + scaleIn(
                initialScale = 0.74f,
                animationSpec = motionSpatialFluid()
            ) + fadeIn(motionEffects()),
            exit = shrinkHorizontally(
                shrinkTowards = Alignment.CenterHorizontally,
                animationSpec = motionSpatialFluid()
            ) + scaleOut(
                targetScale = 0.74f,
                animationSpec = motionSpatialFluid()
            ) + fadeOut(motionEffects())
        ) {
            Box(
                modifier = Modifier.animateContentSize(animationSpec = motionSpatialFluid()),
                contentAlignment = Alignment.Center
            ) {
                val current = toast
                if (current == null) {
                    LocatingPill()
                } else {
                    ToastPill(
                        message = current,
                        onAction = {
                            if (current.kind == ToastKind.ERROR && current.actionLabel != null) {
                                if (current.text.contains("خدمة الموقع")) {
                                    state.openSystemLocationSettings()
                                } else {
                                    onOpenAppSettings()
                                }
                            }
                            state.dismissToast()
                        },
                        onDismiss = { state.dismissToast() }
                    )
                }
            }
        }
    }
}

/** كبسولة داكنة أنيقة: أيقونة + نص بلون مقروء + إجراء اختياري + إغلاق. */
@Composable
private fun ToastPill(
    message: ToastMessage,
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    val (icon, tint) = when (message.kind) {
        ToastKind.LOCATION -> Icons.Filled.LocationOn to KararColors.Gold
        ToastKind.ERROR -> Icons.Filled.Info to KararColors.Rose
        ToastKind.SUCCESS -> Icons.Filled.CheckCircle to KararColors.Mint
        ToastKind.INFO -> Icons.Filled.Info to KararColors.Teal
    }
    val pillColor = Color(0xF0121A2C)
    val onPill = Color(0xFFF2F5FA)

    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = motionSpatial(),
        label = "toastSwipe"
    )

    Box(
        modifier = Modifier
            .widthIn(max = 340.dp)
            .fillMaxWidth()
            .graphicsLayer { translationX = animatedOffsetX }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (kotlin.math.abs(offsetX) > 180f) {
                            onDismiss()
                        } else {
                            offsetX = 0f
                        }
                    },
                    onDragCancel = { offsetX = 0f },
                    onHorizontalDrag = { _, dragAmount: Float ->
                        offsetX += dragAmount
                    }
                )
            }
            .clip(RoundedCornerShape(20.dp))
            .background(pillColor)
            .border(1.dp, KararColors.Teal.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
            .shadow(10.dp, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(17.dp)
                )
            }
            Text(
                text = message.text,
                color = onPill,
                fontSize = 11.5.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (!message.actionLabel.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(KararColors.Teal.copy(alpha = 0.16f))
                        .clickable(onClick = onAction)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = message.actionLabel,
                        color = KararColors.Teal,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "إغلاق",
                tint = onPill.copy(alpha = 0.55f),
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onDismiss)
                    .padding(3.dp)
            )
        }
    }
}

/**
 * عنوان «أعمال اليوم» الموحّد: اسم اليوم + التاريخ الهجري لمدينتك — يُعرض مرة واحدة
 * في شريط العنوان (لا تكرار داخل الشاشة).
 */
private fun dailyDeedsTitle(settings: com.karrar.amsakiah.core.settings.AppSettings): String {
    val today = PrayerCalculator.todayDate(settings)
    val hijri = PrayerCalculator.hijriFor(settings, today)
    val weekday = PrayerCalculator.weekDays.getOrNull(today.dayOfWeek.ordinal).orEmpty()
    return "$weekday ${hijri.day} ${hijri.monthName} ${hijri.year} هـ"
}

/**
 * كبسولة «جارٍ تحديد الموقع» — حلقات رادار نابضة حول أيقونة الموقع.
 * الحركة دوّارية لانهائية خطية (دورية بطبيعتها)، وتتوقف فور انتهاء الحالة.
 */
@Composable
private fun LocatingPill() {
    val transition = rememberInfiniteTransition(label = "locating")
    // «تقليل الحركة»: الحلقات والنبض يتوقفان — النص وحده يشرح الحالة.
    val ambient = ambientMotionEnabled()
    val progressState: State<Float> = if (ambient) {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "locating-progress"
        )
    } else remember { mutableStateOf(0.5f) }
    val pulseState: State<Float> = if (ambient) {
        transition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "locating-pulse"
        )
    } else remember { mutableStateOf(1f) }
    val progress by progressState
    val pulse by pulseState
    val radarGold = KararColors.Gold
    val pillColor = Color(0xF0121A2C)
    val onPill = Color(0xFFF2F5FA)

    Box(
        modifier = Modifier
            .widthIn(max = 300.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(pillColor)
            .border(1.dp, KararColors.Teal.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .drawBehind {
                        repeat(3) { index ->
                            val phase = (progress + index / 3f) % 1f
                            drawCircle(
                                color = radarGold.copy(alpha = (1f - phase) * 0.35f),
                                radius = size.minDimension * (0.22f + 0.5f * phase),
                                center = center
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = KararColors.Gold,
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer {
                            scaleX = pulse
                            scaleY = pulse
                        }
                )
            }
            Column {
                Text(
                    text = "جارٍ تحديد الموقع…",
                    color = onPill,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "نستخدم أدق مصدر متاح (GPS ← الشبكة ← IP)",
                    color = onPill.copy(alpha = 0.55f),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

