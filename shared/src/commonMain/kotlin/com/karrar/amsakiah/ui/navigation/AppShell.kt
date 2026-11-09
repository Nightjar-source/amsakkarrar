package com.karrar.amsakiah.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
 * الشريط السفلي: العنصر النشط يتمدّد أفقيًا ويُظهر عنوانه على خلفية
 * متدرجة teal→indigo، وغير النشط أيقونة فقط. كل ذلك بنوابض مكانية.
 */
@Composable
private fun FloatingBottomBar(
    current: AppDestination,
    onSelect: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .widthIn(max = KararTokens.Size.navBarMaxWidth)
            .fillMaxWidth()
    ) {
        val unselected = 40.dp
        val pad = 6.dp
        val selectedWidth = (maxWidth - pad * 2 - unselected * (AppDestination.entries.size - 1))
            .coerceIn(64.dp, 108.dp)

        GlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            elevation = 24.dp,
            contentPadding = PaddingValues(horizontal = pad, vertical = pad)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppDestination.entries.forEach { destination ->
                    BottomNavItem(
                        destination = destination,
                        selected = destination == current,
                        width = if (destination == current) selectedWidth else unselected,
                        onClick = { onSelect(destination) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    destination: AppDestination,
    selected: Boolean,
    width: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val animatedWidth by animateDpAsState(
        targetValue = width,
        animationSpec = KararTokens.Motion.spatial(),
        label = "nav-width"
    )
    val indicator by animateColorAsState(
        targetValue = if (selected) KararColors.Teal else Color.Transparent,
        animationSpec = KararTokens.Motion.effects(),
        label = "nav-indicator"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else KararColors.Teal.copy(alpha = 0.78f),
        animationSpec = KararTokens.Motion.effects(),
        label = "nav-content"
    )
    val gradient = Brush.linearGradient(
        listOf(KararColors.Teal, KararColors.Violet)
    )

    Box(
        modifier = Modifier
            .width(animatedWidth)
            .height(KararTokens.Size.navItem)
            .clip(RoundedCornerShape(18.dp))
            .then(
                if (selected) Modifier.background(gradient) else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = destination.title,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(KararTokens.Motion.effects()) +
                    slideInHorizontally(KararTokens.Motion.spatial()) { it / 2 },
                exit = fadeOut(KararTokens.Motion.effects()) +
                    slideOutHorizontally(KararTokens.Motion.spatial()) { it / 2 }
            ) {
                Text(
                    text = destination.shortTitle,
                    color = contentColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
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
