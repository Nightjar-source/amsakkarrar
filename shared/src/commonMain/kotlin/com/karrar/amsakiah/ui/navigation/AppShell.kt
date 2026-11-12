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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.karrar.amsakiah.core.state.AppState
import com.karrar.amsakiah.core.state.ToastKind
import com.karrar.amsakiah.core.state.ToastMessage
import com.karrar.amsakiah.core.weather.WeatherInfo
import com.karrar.amsakiah.ui.adaptive.NavigationLayout
import com.karrar.amsakiah.ui.adaptive.rememberAppAdaptiveLayout
import com.karrar.amsakiah.ui.adaptive.topBarHeight
import com.karrar.amsakiah.ui.components.DuaPlayerOverlay
import com.karrar.amsakiah.ui.components.GlassSurface
import com.karrar.amsakiah.ui.motion.ambientMotionEnabled
import com.karrar.amsakiah.ui.motion.effectsSpec
import com.karrar.amsakiah.ui.motion.motionEffects
import com.karrar.amsakiah.ui.motion.motionSpatial
import com.karrar.amsakiah.ui.motion.spatialSpec
import com.karrar.amsakiah.ui.platform.PlatformBackHandler
import com.karrar.amsakiah.ui.platform.isIosUi
import com.karrar.amsakiah.ui.platform.rememberReduceMotion
import com.karrar.amsakiah.ui.player.DuaPlayerController
import com.karrar.amsakiah.ui.screens.CalendarScreen
import com.karrar.amsakiah.ui.screens.DuasScreen
import com.karrar.amsakiah.ui.screens.FiqhScreen
import com.karrar.amsakiah.ui.screens.KeysScreen
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
import kotlinx.coroutines.delay

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
@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
@Composable
fun AppShell(
    state: AppState,
    weather: WeatherInfo?,
    modifier: Modifier = Modifier
) {
    val layout = rememberAppAdaptiveLayout()
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

    // مشغّل واحد لكل التطبيق — يبقى يعمل عبر التنقل ويُحرَّر عند إغلاق الشل.
    val player = remember { DuaPlayerController() }
    DisposableEffect(player) {
        onDispose { player.release() }
    }

    // «اليوم» ولا شيء فوقه؛ أوفتح دعاء/قارئ الجنان يتحولان إلى edge-to-edge
    // بشاشتهما الخاصة (لا شريط التطبيق الرئيسي).
    val isToday = currentRoute == AppRoute.Today && !duaDetailOpen
    // «اليوم» يخفي شريط العنوان فقط — شريط التنقل السفلي يبقى كما في المرجع.
    // يخفي شريطَي الواجهة بالكامل فقط في وضع القراءة (دعاء/قارئ الجنان).
    val immersive = (currentRoute == AppRoute.Duas && duaDetailOpen) || currentRoute is AppRoute.JinanReader
    val showTopBar = !isToday && !immersive

    // ---------- الإنسِتس ----------
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
    val navBarReserve = navigationBottom + 12.dp + KararTokens.Size.navBarHeight

    val contentTop = when {
        isToday -> statusTop + 6.dp
        // ديناميكي حسب فئة الارتفاع الرسمية بدلًا من فراغ ثابت 74dp
        else -> statusTop + 8.dp + asTopBarHeight + 10.dp
    }
    val contentPadding = PaddingValues(
        start = safeStart,
        top = contentTop,
        end = safeEnd,
        bottom = navBarReserve + 36.dp +
            if (player.hasTrack) KararTokens.Size.playerDockHeight + 10.dp else 0.dp
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
            navDirection = -1
            backStack.removeAt(backStack.lastIndex)
        }
    }

    // NavDisplay يتولى زر الرجوع النظامي؛ نُبقي معالجًا احتياطيًا لتغطية iOS
    // وتفاصيل الدعاء في الوقت نفسه (لا يُستدعى اثنان لنفس الضغطة).
    PlatformBackHandler(enabled = duaDetailOpen || backStack.size > 1) { back() }

    // ---------- الهيكل ----------
    Box(modifier = modifier.fillMaxSize()) {
        // خلفية واحدة فقط: سماوية في «اليوم» (ترسمها الشاشة full-bleed)،
        // وتدرج اللوحة الحالية في بقية الشاشات — فلا كتل لونية على الحواف.
        if (!isToday) {
            AppBackdrop()
        }

        Row(Modifier.fillMaxSize()) {
            if (layout.navigationLayout != NavigationLayout.BOTTOM_BAR) {
                SideNavigation(
                    current = currentTab,
                    expanded = layout.navigationLayout == NavigationLayout.DRAWER,
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

                // Nav3: كل شاشة Scene مبني من الـback stack؛ الانتقالات نابضية
                // وتتبع اتجاه المنصة (أندرويد shared-axis / iOS Cupertino).
                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .then(contentModifier),
                    contentAlignment = Alignment.TopCenter,
                    onBack = ::back,
                    // الخفة: بلا SizeTransform — لا يُعاد قياس/قصّ المشهد بين الشاشات،
                    // والشاشات هنا كلها بعرض واحد أصلًا، فإلغاؤه يوفّر طبقة قصّ لكل إطار.
                    sizeTransform = null,
                    transitionSpec = { sceneTransition(navDirection, reduceMotion) },
                    popTransitionSpec = { sceneTransition(-navDirection, reduceMotion) },
                    entryProvider = entryProvider {
                        entry(AppRoute.Today) {
                            TodayScreen(
                                state = state,
                                weather = weather,
                                onOpenFullCalendar = { select(AppDestination.CALENDAR) },
                                onOpenSettings = ::openSettings,
                                contentPadding = contentPadding
                            )
                        }
                        entry(AppRoute.Calendar) {
                            CalendarScreen(state = state, contentPadding = contentPadding)
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
                            QuranScreen(state = state, contentPadding = contentPadding)
                        }
                        entry(AppRoute.Tasbih) {
                            TasbihScreen(state = state, contentPadding = contentPadding)
                        }
                        entry(AppRoute.Qibla) {
                            ShrinesQiblaScreen(state = state, contentPadding = contentPadding)
                        }
                        entry(AppRoute.Tasks) {
                            val openReader: (com.karrar.amsakiah.core.jinan.JinanItem) -> Unit = { item ->
                                openKeysChild(AppRoute.JinanReader(item.id, item.title))
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
                        entry(AppRoute.Jinan) {
                            // تحميل محتوى مفاتيح الجنان (2.9MB) عند الحاجة فقط.
                            LaunchedEffect(Unit) { state.prepareJinanContent() }
                            JinanScreen(
                                contentPadding = contentPadding,
                                onOpenReader = { item ->
                                    openKeysChild(AppRoute.JinanReader(item.id, item.title))
                                }
                            )
                        }
                        entry<AppRoute.JinanReader> { route ->
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
                                }
                            )
                        }
                        entry(AppRoute.DailyDeeds) {
                            // أعمال الليلة/اليوم/يوم الأسبوع — من «مفاتيح الجنان» بمطابقة كاملة.
                            LaunchedEffect(Unit) { state.prepareJinanContent() }
                            DailyDeedsScreen(
                                state = state,
                                contentPadding = contentPadding,
                                onOpenReader = { item ->
                                    openKeysChild(AppRoute.JinanReader(item.id, item.title))
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
                        AppRoute.DailyDeeds -> "أعمال اليوم"
                        else -> null
                    }
                    UnifiedTopBar(
                        title = childTitle ?: if (widgetsOpen) "الودجتات" else currentTab.title,
                        // «اليوم» والشاشات الرئيسية: عنوان فقط — التاريخ الهجري يظهر داخل «اليوم» نفسها.
                        subtitle = when {
                            widgetsOpen -> "أشكال الودجت وخلفياتها"
                            childTitle != null -> "المفاتيح"
                            else -> ""
                        },
                        canGoBack = backStack.size > 1 || duaDetailOpen,
                        onBack = ::back,
                        actionIcon = if (currentTab == AppDestination.SETTINGS) Icons.Filled.Widgets else Icons.Filled.Tune,
                        actionLabel = if (currentTab == AppDestination.SETTINGS) "الودجتات" else "الضبط",
                        onAction = {
                            if (currentTab == AppDestination.SETTINGS) openWidgets() else openSettings()
                        },
                        height = asTopBarHeight,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = statusTop + 8.dp, start = 12.dp + safeStart, end = 12.dp + safeEnd)
                    )
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

        if (layout.navigationLayout == NavigationLayout.BOTTOM_BAR && !immersive) {
            FloatingBottomBar(
                current = currentTab,
                onSelect = ::select,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = 12.dp + safeStart,
                        end = 12.dp + safeEnd,
                        bottom = navigationBottom + 12.dp
                    )
            )
        }

        // التنبيهات العائمة: أخطاء الموقع/IP وأي حدث يخص المستخدم تظهر ككبسولة
        // بيضاء أنيقة في الأعلى — لا إغلاق للتطبيق ولا أشرطة أخطاء منبثقة.
        AppToastHost(
            state = state,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(
                    top = statusTop + (if (showTopBar) asTopBarHeight + 16.dp else 14.dp),
                    start = 16.dp + safeStart,
                    end = 16.dp + safeEnd
                )
        )
    }
}

/**
 * انتقالات المنصة لشاشات Nav3:
 * - أندرويد: M3 Expressive shared-axis خفيف (انزلاق 1/8 عرض + تلاشٍ نابض).
 * - iOS: انزلاق Cupertino كامل العرض للشاشات الفرعية، وتلاشٍ للتبويبات.
 *
 * الخفة: لا تكبير/تصغير (scale) في الانتقال — كل scale يفرض طبقة رسم
 * إضافية على كل شاشة أثناء الحركة. الانزلاق القصير + التلاشي يحققان الإحساس
 * نفسه بكتابة بكسل أقل، فيبقى التنقل ثابت الإطارات على الأجهزة المتوسطة.
 *
 * [direction] = 1 للأمام (دفع/تبويب أعلى)، -1 للخلف (سحب/تبويب أدنى).
 * [reduceMotion] = «تقليل الحركة» في النظام: تلاشٍ قصير بلا أي انزلاق مكاني.
 */
private fun sceneTransition(direction: Int, reduceMotion: Boolean): ContentTransform {
    if (reduceMotion) {
        val fade = tween<Float>(durationMillis = KararTokens.Motion.fastMs)
        return fadeIn(animationSpec = fade).togetherWith(fadeOut(animationSpec = fade))
    }
    val travel: (Int) -> Int = { full -> if (direction >= 0) full else -full }
    return when {
        isIosUi -> (
            slideInHorizontally(animationSpec = KararTokens.Motion.spatial()) { travel(it) } +
                fadeIn(animationSpec = KararTokens.Motion.effects())
            ).togetherWith(
            slideOutHorizontally(animationSpec = KararTokens.Motion.spatial()) { -travel(it) / 3 } +
                fadeOut(animationSpec = KararTokens.Motion.effects())
        )
        else -> (
            slideInHorizontally(animationSpec = KararTokens.Motion.spatial()) { travel(it) / 8 } +
                fadeIn(animationSpec = KararTokens.Motion.effects())
            ).togetherWith(
            slideOutHorizontally(animationSpec = KararTokens.Motion.spatial()) { -travel(it) / 12 } +
                fadeOut(animationSpec = KararTokens.Motion.effects())
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
 * العنوان وحده يتبدّل داخل نفس الكبسولة بحركة انزلاق نابضة.
 */
@Composable
private fun UnifiedTopBar(
    title: String,
    subtitle: String,
    canGoBack: Boolean,
    onBack: () -> Unit,
    actionIcon: ImageVector,
    actionLabel: String,
    onAction: () -> Unit,
    height: Dp = KararTokens.Size.topBarHeight,
    modifier: Modifier = Modifier
) {
    // «تقليل الحركة»: عنوان الشريط يظهر/يختفي بتلاشٍ بلا انزلاق.
    val reduceMotion = rememberReduceMotion()
    Row(
        modifier = modifier
            .widthIn(max = KararTokens.Size.topBarMaxWidth)
            .fillMaxWidth()
            .height(height),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (canGoBack) {
            CircleGlassButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                label = "رجوع",
                onClick = onBack
            )
        }


        GlassSurface(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
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
                            // lambda غير تركيبية: تمرير حالة التركيب المحيط.
                            (
                                slideInVertically(animationSpec = spatialSpec(reduceMotion)) { -it / 2 } +
                                    fadeIn(animationSpec = effectsSpec(reduceMotion))
                                ).togetherWith(
                                slideOutVertically(animationSpec = spatialSpec(reduceMotion)) { it / 2 } +
                                    fadeOut(animationSpec = effectsSpec(reduceMotion))
                            )
                        },
                        label = "topbar-title",
                        contentAlignment = Alignment.CenterStart
                    ) { current ->
                        Text(
                            text = current,
                            color = KararColors.TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            color = KararColors.TextSecondary,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Text(
                    text = "☾",
                    color = KararColors.Gold.copy(alpha = 0.9f),
                    fontSize = 22.sp
                )
            }
        }

        CircleGlassButton(
            icon = actionIcon,
            label = actionLabel,
            onClick = onAction
        )
    }
}

/** زر دائري زجاجي منفصل. */
@Composable
private fun CircleGlassButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    GlassSurface(
        modifier = Modifier.size(KararTokens.Size.circleButton),
        shape = CircleShape,
        elevation = 12.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .clickable(onClick = onClick),
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
 * الشريط السفلي — M3 Expressive «Flexible Navigation Bar» (v0_11_0)، بحدود
 * هوية التطبيق الداكنة:
 *
 * - الحاوية: `surfaceContainer` بنقطة عالية المحتوى، زوايا xxxl، حدّ رقيق
 *   (`outlineVariant`) — نغمة سطحية لا درجة تخصصية فيها، وجهات متساوية.
 * - العناصر عمودية (compact): أيقونة حيوي ٢٤dp فوق تسمية (١١sp).
 * - المؤشر النشط: كبسولة **٣٢×٥٦dp** (CornerFull) بتعبئة `secondaryContainer`،
 *   تتوسع من مركز الأيقونة على محور واحد وتنزلق بنابض مكاني — لا قفزات.
 * - الألوان تتبع MaterialTheme.colorScheme: الأيقونة والمؤشر = onSecondaryContainer،
 *   والتسمية النشطة = secondary (سميكة)، والتسمية غير النشطة = onSurfaceVariant.
 * - الانتقالات: الأيقونة تملأ/تفرغ، المؤشر ينزلق — الحركة كلها `graphicsLayer`،
 *   لا يعاد قياس أي شيء أثناء الانزلاق.
 */
@Composable
private fun FloatingBottomBar(
    current: AppDestination,
    onSelect: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val entries = AppDestination.entries
    Surface(
        modifier = modifier
            .widthIn(max = KararTokens.Size.navBarMaxWidth)
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(KararTokens.Radius.xxxl),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(68.dp)) {
            val slotWidth = maxWidth / entries.size
            val indicatorWidth = 56.dp
            val indicatorHeight = 32.dp
            val targetOffset = slotWidth * current.ordinal + (slotWidth - indicatorWidth) / 2
            val offsetX by animateFloatAsState(
                targetValue = targetOffset.value,
                animationSpec = motionSpatial(),
                label = "nav-m3-offset"
            )
            val density = LocalDensity.current.density

            // مؤشر M3 Expressive: كبسولة secondaryContainer تنزلق خلف الأيقونة فقط.
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 6.dp)
                    .graphicsLayer { translationX = offsetX * density }
                    .width(indicatorWidth)
                    .height(indicatorHeight)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            )

            Row(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                entries.forEach { destination ->
                    FlexibleNavItem(
                        destination = destination,
                        selected = destination == current,
                        onClick = { onSelect(destination) },
                        modifier = Modifier.width(slotWidth)
                    )
                }
            }
        }
    }
}

/**
 * عنصر داخل الشريط السفلي — الأيقونة في خانة المؤشر (٣٢dp) ثم التسمية تحتها،
 * تمامًا كتخطيط «Vertical» في M3 Expressive.
 */
@Composable
private fun FlexibleNavItem(
    destination: AppDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val iconTint by animateColorAsState(
        targetValue = if (selected) scheme.onSecondaryContainer else KararColors.TextMuted,
        animationSpec = KararTokens.Motion.effects(),
        label = "nav-icon-tint"
    )
    val labelTint by animateColorAsState(
        targetValue = if (selected) scheme.secondary else KararColors.TextMuted,
        animationSpec = KararTokens.Motion.effects(),
        label = "nav-label-tint"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = destination.title,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = destination.shortTitle,
            color = labelTint,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** الشريط الجانبي — Rail على المتوسط، وDrawer موسّع على الواسع، بتوسّع نابض. */
@Composable
private fun SideNavigation(
    current: AppDestination,
    expanded: Boolean,
    onSelect: (AppDestination) -> Unit,
    safeStart: Dp,
    safeEnd: Dp,
    topInset: Dp,
    bottomInset: Dp
) {
    val targetWidth = if (expanded) KararTokens.Size.drawerWidth else KararTokens.Size.railWidth
    val width by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = motionSpatial(),
        label = "side-width"
    )
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(width + 20.dp + safeStart + safeEnd)
            .padding(
                start = 10.dp + safeStart,
                end = 10.dp + safeEnd,
                top = 12.dp + topInset,
                bottom = 12.dp + bottomInset
            ),
        contentAlignment = Alignment.Center
    ) {
        GlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(KararTokens.Radius.xxxl),
            elevation = 22.dp,
            contentPadding = PaddingValues(6.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(AppDestination.entries, key = { it.name }) { destination ->
                    SideNavItem(
                        destination = destination,
                        selected = destination == current,
                        expanded = expanded,
                        onClick = { onSelect(destination) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SideNavItem(
    destination: AppDestination,
    selected: Boolean,
    expanded: Boolean,
    onClick: () -> Unit
) {
    val background by animateColorAsState(
        targetValue = if (selected) KararColors.Teal.copy(alpha = 0.22f) else Color.Transparent,
        animationSpec = KararTokens.Motion.effects(),
        label = "rail-bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) KararColors.Teal else KararColors.TextSecondary,
        animationSpec = KararTokens.Motion.effects(),
        label = "rail-fg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(KararTokens.Size.navItem)
            .clip(RoundedCornerShape(KararTokens.Radius.md))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = if (expanded) 14.dp else 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (expanded) Arrangement.spacedBy(10.dp) else Arrangement.Center
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = destination.title,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        AnimatedVisibility(visible = expanded) {
            Text(
                text = destination.shortTitle,
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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
private fun AppToastHost(state: AppState, modifier: Modifier = Modifier) {
    val toast = state.toast
    val locating = state.locating

    LaunchedEffect(toast?.id) {
        val current = toast ?: return@LaunchedEffect
        delay(
            when (current.kind) {
                ToastKind.LOCATION -> 5_000L
                ToastKind.ERROR -> 5_000L
                else -> 3_500L
            }
        )
        if (state.toast?.id == current.id) state.dismissToast()
    }

    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(
            visible = locating || toast != null,
            enter = slideInVertically(motionSpatial()) { -it / 2 } +
                fadeIn(motionEffects()),
            exit = slideOutVertically(
                motionSpatial(stiffness = KararTokens.Motion.SpatialStiffnessFast)
            ) { -it / 2 } + fadeOut(motionEffects())
        ) {
            val current = toast
            if (current == null) {
                LocatingPill()
            } else {
                ToastPill(
                    message = current,
                    onAction = {
                        if (current.kind == ToastKind.ERROR && current.actionLabel != null) {
                            state.openSystemLocationSettings()
                        }
                        state.dismissToast()
                    },
                    onDismiss = { state.dismissToast() }
                )
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

    Box(
        modifier = Modifier
            .widthIn(max = 340.dp)
            .fillMaxWidth()
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
