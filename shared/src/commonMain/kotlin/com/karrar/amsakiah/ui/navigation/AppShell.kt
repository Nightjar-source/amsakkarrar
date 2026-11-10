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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.karrar.amsakiah.core.state.AppState
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.core.state.ToastKind
import com.karrar.amsakiah.core.state.ToastMessage
import com.karrar.amsakiah.core.weather.WeatherInfo
import com.karrar.amsakiah.ui.adaptive.NavigationLayout
import com.karrar.amsakiah.ui.adaptive.rememberAppAdaptiveLayout
import com.karrar.amsakiah.ui.components.DuaPlayerOverlay
import com.karrar.amsakiah.ui.components.GlassSurface
import com.karrar.amsakiah.ui.platform.PlatformBackHandler
import com.karrar.amsakiah.ui.platform.isIosUi
import com.karrar.amsakiah.ui.player.DuaPlayerController
import com.karrar.amsakiah.ui.screens.CalendarScreen
import com.karrar.amsakiah.ui.screens.DuasScreen
import com.karrar.amsakiah.ui.screens.FiqhScreen
import com.karrar.amsakiah.ui.screens.SettingsScreen
import com.karrar.amsakiah.ui.screens.ShrinesQiblaScreen
import com.karrar.amsakiah.ui.screens.TasbihScreen
import com.karrar.amsakiah.ui.screens.TodayScreen
import com.karrar.amsakiah.ui.screens.WidgetsScreen
import com.karrar.amsakiah.ui.theme.KararColors
import com.karrar.amsakiah.ui.theme.KararTokens
import com.karrar.amsakiah.ui.theme.LocalKararPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    val isToday = currentRoute == AppRoute.Today && !duaDetailOpen
    val showTopBar = !isToday

    // ---------- الإنسِتس ----------
    val topInsets = WindowInsets.statusBars
        .union(WindowInsets.captionBar)
        .union(WindowInsets.displayCutout)
    val bottomInsets = WindowInsets.navigationBars
        .union(WindowInsets.displayCutout)
    val statusTop = topInsets.asPaddingValues().calculateTopPadding()
    val navigationBottom = bottomInsets.asPaddingValues().calculateBottomPadding()

    val layoutDirection = LocalLayoutDirection.current
    val cutout = WindowInsets.displayCutout.asPaddingValues()
    val cutoutStart = cutout.calculateStartPadding(layoutDirection)
    val cutoutEnd = cutout.calculateEndPadding(layoutDirection)

    val settings = state.settings
    // الخفة: الشل يقرأ المؤقّت (كل ثانية) داخل قيمة مشتقّة لا تُبلّغ إلا عند
    // تغيّر اليوم. قبل ذلك كان كل نبض ثانية يعيد تركيب الشل وNavDisplay معه.
    val today by remember {
        derivedStateOf {
            state.now // اعتماد صريح على المؤقّت لإعادة الحساب
            PrayerCalculator.todayDate(state.settings)
        }
    }
    val hijri = remember(today, settings.hijriOffset) {
        PrayerCalculator.hijriFor(settings, today)
    }

    // ارتفاع شريط التنقل السفلي + هوامشه — يُحجز للمحتوى والمشغّل.
    val navBarReserve = navigationBottom + 12.dp + KararTokens.Size.navBarHeight

    val contentTop = when {
        isToday -> statusTop + 6.dp
        else -> statusTop + KararTokens.Size.contentTopGap
    }
    val contentPadding = PaddingValues(
        start = cutoutStart,
        top = contentTop,
        end = cutoutEnd,
        bottom = navBarReserve + 36.dp +
            if (player.hasTrack) KararTokens.Size.playerDockHeight + 10.dp else 0.dp
    )

    fun select(destination: AppDestination) {
        val route = destination.toRoute()
        if (backStack.size == 1 && backStack[0] == route) return
        navDirection = if (destination.ordinal >= currentTab.ordinal) 1 else -1
        // التبويبات تُستبدل في جذر الـback stack (لا تتكدّس)، وأي شاشة فرعية تُغلق.
        //
        // الخفة: كل تعديل على SnapshotStateList يوقظ NavDisplay؛ مسح الشاشات
        // الفرعية ثم تبديل الجذر داخل لقطة واحدة يعني إعادة تركيب واحدة وحركة
        // واحدة، بدل سلسلة حركات متتالية عند العودة من شاشة فرعية.
        Snapshot.withMutableSnapshot {
            if (backStack.size > 1) backStack.subList(1, backStack.size).clear()
            if (backStack.isEmpty()) backStack.add(route) else backStack[0] = route
        }
    }

    fun openSettings() = select(AppDestination.SETTINGS)

    /** «الودجتات» شاشة فرعية تُدفع فوق الضبط — ليست تبويبًا ثامنًا. */
    fun openWidgets() {
        if (backStack.lastOrNull() == AppRoute.Widgets) return
        navDirection = 1
        duaDetailOpen = false
        duaDetailTitle = null
        backStack.add(AppRoute.Widgets)
    }

    fun back() {
        when {
            // تفاصيل الدعاء أولًا: تُدار داخل شاشة الأدعية نفسها.
            duaDetailOpen -> duaBackSignal++
            backStack.size > 1 -> {
                navDirection = -1
                backStack.removeAt(backStack.lastIndex)
            }
            else -> Unit
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
                    onSelect = ::select
                )
            }

            // لا BoxWithConstraints هنا: كل SubcomposeLayout إضافي يؤجّل تركيب
            // الشل بالكامل عند كل قياس. القيد بالأعراض يُعبَّر عنه بمُعدِّلات
            // عادية، فيبقى مسار القياس خفيفًا أثناء الانتقالات وتغيير الحجم.
            Box(Modifier.weight(1f).fillMaxHeight()) {
                val contentModifier = if (isToday) {
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                } else {
                    Modifier
                        .fillMaxHeight()
                        .widthIn(max = KararTokens.Size.contentMaxWidth)
                        .fillMaxWidth()
                }

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
                    transitionSpec = { sceneTransition(navDirection) },
                    popTransitionSpec = { sceneTransition(-navDirection) },
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
                        entry(AppRoute.Settings) {
                            SettingsScreen(
                                state = state,
                                contentPadding = contentPadding,
                                onOpenWidgets = ::openWidgets
                            )
                        }
                        entry(AppRoute.Calendar) {
                            CalendarScreen(state = state, contentPadding = contentPadding)
                        }
                        entry(AppRoute.Qibla) {
                            ShrinesQiblaScreen(state = state, contentPadding = contentPadding)
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
                        entry(AppRoute.Tasbih) {
                            TasbihScreen(state = state, contentPadding = contentPadding)
                        }
                        entry(AppRoute.Fiqh) {
                            FiqhScreen(state = state, contentPadding = contentPadding)
                        }
                        entry(AppRoute.Widgets) {
                            WidgetsScreen(state = state, contentPadding = contentPadding)
                        }
                    }
                )

                if (showTopBar) {
                    UnifiedTopBar(
                        title = when {
                            widgetsOpen -> "الودجتات"
                            currentTab == AppDestination.DUAS && duaDetailTitle != null -> duaDetailTitle!!
                            else -> currentTab.title
                        },
                        subtitle = when {
                            widgetsOpen -> "أشكال الودجت وخلفياتها"
                            currentTab == AppDestination.DUAS && duaDetailTitle != null -> "الأدعية والمناجاة"
                            else -> "${hijri.day} ${hijri.monthName} ${hijri.year} هـ"
                        },
                        canGoBack = backStack.size > 1 || duaDetailOpen,
                        onBack = ::back,
                        actionIcon = if (currentTab == AppDestination.SETTINGS) Icons.Filled.Widgets else Icons.Filled.Tune,
                        actionLabel = if (currentTab == AppDestination.SETTINGS) "الودجتات" else "الضبط",
                        onAction = {
                            if (currentTab == AppDestination.SETTINGS) openWidgets() else openSettings()
                        },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = statusTop + 8.dp, start = 12.dp + cutoutStart, end = 12.dp + cutoutEnd)
                    )
                }
            }
        }

        // مشغّل واحد: مدمج فوق شريط التنقل، أو كرة عائمة تُسحب.
        if (player.hasTrack) {
            DuaPlayerOverlay(
                controller = player,
                bottomReserve = navBarReserve,
                horizontalPadding = 12.dp + cutoutStart,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (layout.navigationLayout == NavigationLayout.BOTTOM_BAR) {
            FloatingBottomBar(
                current = currentTab,
                onSelect = ::select,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = 12.dp + cutoutStart,
                        end = 12.dp + cutoutEnd,
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
                    top = statusTop + (if (showTopBar) KararTokens.Size.topBarHeight + 16.dp else 14.dp),
                    start = 16.dp + cutoutStart,
                    end = 16.dp + cutoutEnd
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
 */
private fun sceneTransition(direction: Int): ContentTransform {
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
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .widthIn(max = KararTokens.Size.topBarMaxWidth)
            .fillMaxWidth()
            .height(KararTokens.Size.topBarHeight),
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
                            (
                                slideInVertically(animationSpec = KararTokens.Motion.spatial()) { -it / 2 } +
                                    fadeIn(animationSpec = KararTokens.Motion.effects())
                                ).togetherWith(
                                slideOutVertically(animationSpec = KararTokens.Motion.spatial()) { it / 2 } +
                                    fadeOut(animationSpec = KararTokens.Motion.effects())
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
 * الشريط السفلي البيضوي العائم — نسخة حرفية من `amsak3d.tsx` (M3 Expressive
 * Floating Pill Bottom Bar):
 *
 * - كبسولة واحدة `rounded-full` داكنة شبه شفافة بحدود بيضاء 10% وظل عميق.
 * - **كل العناوين ظاهرة دائمًا** بأيقونة + اسم قصير، بلا توسّع/طي للعنصر
 *   النشط — لذلك لا يُدفع أي تبويب خارج الشاشة (كان «اليوم» يُقصّ على
 *   الشاشات الضيقة لأنه كان أول عنصر في صف بعرض ثابت).
 * - النشط: تدرج teal→indigo + نص أبيض + `scale(1.03)` + ظل ملوّن.
 * - غير النشط: أيقونة teal بشفافية 70% ونص أبيض 55%.
 * - الصف قابل للتمرير أفقيًا عند الحاجة، ويُمرَّر العنصر النشط إلى الظهور
 *   تلقائيًا بحركة نابضة.
 */
@Composable
private fun FloatingBottomBar(
    current: AppDestination,
    onSelect: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val activeIndex = AppDestination.entries.indexOf(current).coerceAtLeast(0)

    // إبقاء التبويب النشط ظاهرًا داخل الكبسولة عند تبديله من الشريط الجانبي.
    LaunchedEffect(activeIndex) {
        runCatching { listState.animateScrollToItem(activeIndex) }
    }

    Box(
        modifier = modifier
            .widthIn(max = KararTokens.Size.navBarMaxWidth)
            .fillMaxWidth()
    ) {
        GlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            elevation = 26.dp,
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
        ) {
            LazyRow(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                userScrollEnabled = true
            ) {
                items(
                    items = AppDestination.entries,
                    key = { it.name },
                    contentType = { "nav_item" }
                ) { destination ->
                    PillNavItem(
                        destination = destination,
                        selected = destination == current,
                        onClick = {
                            // تمرير فوري نابض ثم التبديل — لا انتظار لإعادة القياس.
                            scope.launch { runCatching { listState.animateScrollToItem(activeIndex) } }
                            onSelect(destination)
                        }
                    )
                }
            }
        }
    }
}

/**
 * عنصر كبسولة داخل شريط التنقل — عنوان ظاهر دائمًا، والنشط يتدرّج ويكبر
 * قليلًا (1.03) بنابض مكاني، وكل الحركة في طبقة الرسم فقط.
 */
@Composable
private fun PillNavItem(
    destination: AppDestination,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.03f else 1f,
        animationSpec = KararTokens.Motion.spatial(),
        label = "nav-scale"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.55f,
        animationSpec = KararTokens.Motion.effects(),
        label = "nav-alpha"
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) Color.White else KararColors.Teal.copy(alpha = 0.7f),
        animationSpec = KararTokens.Motion.effects(),
        label = "nav-icon"
    )
    val gradient = Brush.linearGradient(listOf(KararColors.Teal, KararColors.Violet))

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .then(if (selected) Modifier.background(gradient) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = destination.title,
                tint = iconTint,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = destination.shortTitle,
                color = Color.White.copy(alpha = contentAlpha),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}

/** الشريط الجانبي — Rail على المتوسط، وDrawer موسّع على الواسع، بتوسّع نابض. */
@Composable
private fun SideNavigation(
    current: AppDestination,
    expanded: Boolean,
    onSelect: (AppDestination) -> Unit
) {
    val targetWidth = if (expanded) KararTokens.Size.drawerWidth else KararTokens.Size.railWidth
    val width by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = KararTokens.Motion.spatial(),
        label = "side-width"
    )
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(width + 20.dp)
            .padding(start = 10.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
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
 * مضيف التنبيهات — يقرأ الحالة من النواة (`state.toast` / `state.locating`)
 * ويختار الشكل والحركة. لا يملك أي منطق: التعديل كله في [com.karrar.amsakiah.core.state.AppState].
 *
 * - كبسولة بيضاء `rounded-full` بظل عميق ونص داكن (تباين عالٍ في الوضعين).
 * - دخول/خروج بانزلاق عمودي نابض + تلاشٍ.
 * - إخفاء تلقائي بعد مدة تختلف حسب النوع (الموقع أطول قليلًا).
 * - أثناء تحديد الموقع تظهر كبسولة «رادار» حلقات نابضة — حتى لو كان GPS سريعًا،
 *   فالحركة هي المطلوبة من المستخدم.
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
                ToastKind.ERROR -> 4_500L
                else -> 3_500L
            }
        )
        if (state.toast?.id == current.id) state.dismissToast()
    }

    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(
            visible = locating || toast != null,
            enter = slideInVertically(KararTokens.Motion.spatial()) { -it / 2 } +
                fadeIn(KararTokens.Motion.effects()),
            exit = slideOutVertically(
                KararTokens.Motion.spatial(stiffness = KararTokens.Motion.SpatialStiffnessFast)
            ) { -it / 2 } + fadeOut(KararTokens.Motion.effects())
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

/** كبسولة بيضاء أنيقة: أيقونة ملوّنة + نص + زر إجراء اختياري + زر إغلاق. */
@Composable
private fun ToastPill(
    message: ToastMessage,
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    val (icon, tint) = when (message.kind) {
        ToastKind.LOCATION -> Icons.Filled.LocationOn to KararColors.Gold
        ToastKind.ERROR -> Icons.Filled.Info to KararColors.Danger
        ToastKind.SUCCESS -> Icons.Filled.CheckCircle to KararColors.Success
        ToastKind.INFO -> Icons.Filled.Info to KararColors.Teal
    }
    // الكبسولة بيضاء في الوضعين (تباين AA) مع حدود خفيفة تمنع ذوبانها في الخلفية.
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color(0x14000000),
                shape = CircleShape
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = message.text,
                color = Color(0xFF1B1B1F),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 240.dp)
            )
            if (!message.actionLabel.isNullOrBlank()) {
                Text(
                    text = message.actionLabel,
                    color = KararColors.Teal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onAction)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "إغلاق",
                tint = Color(0x88000000),
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onDismiss)
                    .padding(2.dp)
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
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "locating-progress"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "locating-pulse"
    )
    // تُقرأ الألوان قبل drawBehind (القراءة داخل draw lambda غير مسموحة).
    val radarGold = KararColors.Gold

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White)
            .border(1.dp, Color(0x14000000), CircleShape)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .drawBehind {
                        // حلقات رادار: ثلاث دوائر تتمدّد وتخفت بشكل متتابع.
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
            Text(
                text = "جارٍ تحديد الموقع…",
                color = Color(0xFF1B1B1F),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
