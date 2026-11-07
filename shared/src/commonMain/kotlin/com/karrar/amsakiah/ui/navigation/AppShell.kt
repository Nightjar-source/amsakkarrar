package com.karrar.amsakiah.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.karrar.amsakiah.core.state.AppState
import com.karrar.amsakiah.core.state.PrayerCalculator
import com.karrar.amsakiah.core.weather.WeatherInfo
import com.karrar.amsakiah.ui.adaptive.NavigationLayout
import com.karrar.amsakiah.ui.adaptive.rememberAppAdaptiveLayout
import com.karrar.amsakiah.ui.components.GlassSurface
import com.karrar.amsakiah.ui.screens.CalendarScreen
import com.karrar.amsakiah.ui.screens.DuasScreen
import com.karrar.amsakiah.ui.screens.FiqhScreen
import com.karrar.amsakiah.ui.screens.SettingsScreen
import com.karrar.amsakiah.ui.screens.ShrinesQiblaScreen
import com.karrar.amsakiah.ui.screens.TasbihScreen
import com.karrar.amsakiah.ui.screens.TodayScreen
import com.karrar.amsakiah.ui.screens.WidgetsScreen
import com.karrar.amsakiah.ui.theme.KararTokens
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclassesOfSealed

/**
 * الشل الرئيسي — Material 3 Expressive + Compose Adaptive + Navigation 3.
 *
 * مبادئ التنفيذ:
 * - لا يوجد `windowInsetsPadding` على الجذر؛ الخلفية تمتد خلف أشرطة النظام
 *   والنوافذ، والإزاحة تُطبّق على المحتوى فقط.
 * - شريط سفلي عائم على الهواتف، وشريط جانبي عائم على العروض المتوسطة/الواسعة.
 * - شريط عنوان عائم بأزرار دائرية منفصلة، ويختفي على شاشة «اليوم» كما في V7.
 * - الـback stack مملوك لنا عبر Navigation 3، مع حفظ الحالة.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
@Composable
fun AppShell(
    state: AppState,
    weather: WeatherInfo?,
    modifier: Modifier = Modifier
) {
    val layout = rememberAppAdaptiveLayout()
    val backStack = rememberAppBackStack()
    val currentRoute = backStack.lastOrNull() as? AppRoute ?: AppRoute.Today
    val currentDestination = currentRoute.toDestination()

    // تفاصيل الدعاء تُدار داخل شاشة الأدعية؛ نخفي شريط الشل ونقلّص الإزاحة.
    var duaDetailOpen by remember { mutableStateOf(false) }
    LaunchedEffect(currentRoute) {
        if (currentRoute != AppRoute.Duas) duaDetailOpen = false
    }

    // الاتحاد المطلوب رسميًا: الحالة + شريط النافذة + فتحة الشاشة.
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
    val hijri = remember(settings.hijriOffset, state.now) {
        PrayerCalculator.hijriFor(settings, PrayerCalculator.todayDate(settings))
    }

    val isToday = currentRoute == AppRoute.Today
    val showTopBar = !isToday && !(currentRoute == AppRoute.Duas && duaDetailOpen)

    val contentTop = when {
        isToday -> statusTop + 6.dp
        currentRoute == AppRoute.Duas && duaDetailOpen -> statusTop + 6.dp
        else -> statusTop + KararTokens.Size.contentTopGap
    }
    val contentPadding = PaddingValues(
        start = cutoutStart,
        top = contentTop,
        end = cutoutEnd,
        bottom = navigationBottom + KararTokens.Size.contentBottomGap
    )

    fun selectDestination(destination: AppDestination) {
        val target = destination.toRoute()
        if (target == currentRoute) return
        while (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
        if (backStack.isEmpty()) backStack.add(target) else backStack[0] = target
    }

    fun push(route: AppRoute) {
        if (backStack.lastOrNull() != route) backStack.add(route)
    }

    fun pop() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    fun openWidgets() = push(AppRoute.Widgets)

    Box(modifier = modifier.fillMaxSize()) {
        // خلفية أساسية تمنع أي وميض أبيض أثناء الانتقالات.
        Box(
            Modifier
                .fillMaxSize()
                .background(KararTokens.Backdrop.top)
        )

        if (!isToday) {
            AppBackdrop()
        }

        Row(Modifier.fillMaxSize()) {
            if (layout.navigationLayout != NavigationLayout.BOTTOM_BAR) {
                SideNavigation(
                    current = currentDestination,
                    expanded = layout.navigationLayout == NavigationLayout.DRAWER,
                    onSelect = ::selectDestination
                )
            }

            Box(Modifier.weight(1f).fillMaxHeight()) {
                NavDisplay(
                    backStack = backStack,
                    modifier = Modifier.fillMaxSize(),
                    onBack = ::pop,
                    transitionSpec = {
                        (
                            slideInHorizontally(
                                animationSpec = tween(
                                    durationMillis = KararTokens.Motion.mediumMs,
                                    easing = KararTokens.Motion.Fluid
                                )
                            ) { it / 3 } + fadeIn(tween(220))
                            ).togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(
                                    durationMillis = KararTokens.Motion.mediumMs,
                                    easing = KararTokens.Motion.Fluid
                                )
                            ) { -it / 4 } + fadeOut(tween(180))
                        )
                    },
                    popTransitionSpec = {
                        (
                            slideInHorizontally(
                                animationSpec = tween(
                                    durationMillis = KararTokens.Motion.mediumMs,
                                    easing = KararTokens.Motion.Fluid
                                )
                            ) { -it / 3 } + fadeIn(tween(220))
                            ).togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(
                                    durationMillis = KararTokens.Motion.mediumMs,
                                    easing = KararTokens.Motion.Fluid
                                )
                            ) { it / 4 } + fadeOut(tween(180))
                        )
                    },
                    entryProvider = entryProvider {
                        entry<AppRoute.Today> {
                            TodayScreen(
                                state = state,
                                weather = weather,
                                onOpenCalendar = { selectDestination(AppDestination.CALENDAR) },
                                onOpenSettings = { selectDestination(AppDestination.SETTINGS) },
                                onOpenQibla = { selectDestination(AppDestination.QIBLA) },
                                contentPadding = contentPadding
                            )
                        }
                        entry<AppRoute.Settings> {
                            SettingsScreen(
                                state = state,
                                contentPadding = contentPadding,
                                onOpenWidgets = ::openWidgets
                            )
                        }
                        entry<AppRoute.Calendar> {
                            CalendarScreen(state = state, contentPadding = contentPadding)
                        }
                        entry<AppRoute.Qibla> {
                            ShrinesQiblaScreen(state = state, contentPadding = contentPadding)
                        }
                        entry<AppRoute.Duas> {
                            DuasScreen(
                                contentPadding = contentPadding,
                                onDetailVisibilityChanged = { duaDetailOpen = it }
                            )
                        }
                        entry<AppRoute.Tasbih> {
                            TasbihScreen(state = state, contentPadding = contentPadding)
                        }
                        entry<AppRoute.Fiqh> {
                            FiqhScreen(state = state, contentPadding = contentPadding)
                        }
                        entry<AppRoute.Widgets> {
                            WidgetsScreen(state = state, contentPadding = contentPadding)
                        }
                    }
                )

                if (showTopBar) {
                    FloatingTopBar(
                        title = when (currentRoute) {
                            AppRoute.Widgets -> "الودجتات"
                            else -> currentDestination?.title ?: "التطبيق"
                        },
                        subtitle = when (currentRoute) {
                            AppRoute.Widgets -> "أشكال الودجت وخلفياتها"
                            AppRoute.Today -> ""
                            else -> "${hijri.day} ${hijri.monthName} ${hijri.year} هـ"
                        },
                        canGoBack = backStack.size > 1,
                        onBack = ::pop,
                        actionIcon = if (currentRoute == AppRoute.Settings) Icons.Filled.Widgets else Icons.Filled.Tune,
                        actionLabel = if (currentRoute == AppRoute.Settings) "الودجتات" else "الضبط",
                        onAction = {
                            if (currentRoute == AppRoute.Settings) openWidgets()
                            else selectDestination(AppDestination.SETTINGS)
                        },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = statusTop + 8.dp, start = 12.dp, end = 12.dp)
                    )
                }
            }
        }

        if (layout.navigationLayout == NavigationLayout.BOTTOM_BAR) {
            FloatingBottomBar(
                current = currentDestination,
                onSelect = ::selectDestination,
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

/** بناء الـback stack الرسمي مع إعداد التسلسل متعدد المنصات. */
@Composable
private fun rememberAppBackStack() = rememberNavBackStack(
    remember {
        SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclassesOfSealed<AppRoute>()
                }
            }
        }
    },
    AppRoute.Today
)

/** خلفية الشاشات غير السماوية: تدرج داكن + توهجان ناعمان بلا كتل لونية. */
@Composable
private fun AppBackdrop() {
    Box(
        Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    Brush.verticalGradient(
                        listOf(KararTokens.Backdrop.top, KararTokens.Backdrop.bottom)
                    )
                )
                val radius = size.maxDimension * 0.62f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(KararTokens.Backdrop.tealGlow, Color.Transparent),
                        center = Offset(size.width * 0.12f, size.height * 0.10f),
                        radius = radius
                    ),
                    radius = radius,
                    center = Offset(size.width * 0.12f, size.height * 0.10f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(KararTokens.Backdrop.indigoGlow, Color.Transparent),
                        center = Offset(size.width * 0.88f, size.height * 0.82f),
                        radius = radius
                    ),
                    radius = radius,
                    center = Offset(size.width * 0.88f, size.height * 0.82f)
                )
            }
    )
}

/** شريط العنوان العائم: زر رجوع دائري + كبسولة العنوان + زر إجراء دائري. */
@Composable
private fun FloatingTopBar(
    title: String,
    subtitle: String,
    canGoBack: Boolean,
    onBack: () -> Unit,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector,
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
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            color = Color(0xFFB9C0D4),
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Text(
                    text = "☾",
                    color = KararTokens.Accent.gold.copy(alpha = 0.9f),
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                tint = Color.White.copy(alpha = 0.92f),
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

/** الشريط السفلي العائم — 7 وجهات، المحددة تتوسّع بالاسم. */
@Composable
private fun FloatingBottomBar(
    current: AppDestination?,
    onSelect: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val selectedIndex = current?.let { AppDestination.entries.indexOf(it) } ?: -1

    LaunchedEffect(selectedIndex) {
        if (selectedIndex >= 0 && listState.layoutInfo.totalItemsCount > 0) {
            runCatching { listState.animateScrollToItem(selectedIndex) }
        }
    }

    GlassSurface(
        modifier = modifier
            .widthIn(max = KararTokens.Size.navBarMaxWidth)
            .fillMaxWidth(),
        shape = CircleShape,
        elevation = 24.dp,
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(AppDestination.entries, key = { it.name }) { destination ->
                BottomNavPill(
                    destination = destination,
                    selected = destination == current,
                    onClick = { onSelect(destination) }
                )
            }
        }
    }
}

/** عنصر الشريط السفلي: أيقونة دائمًا، والاسم عند التحديد فقط. */
@Composable
private fun BottomNavPill(
    destination: AppDestination,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background by animateColorAsState(
        targetValue = if (selected) KararTokens.Accent.gold else Color.Transparent,
        animationSpec = tween(KararTokens.Motion.normalMs, easing = KararTokens.Motion.Fluid),
        label = "nav-bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF241A00) else Color.White.copy(alpha = 0.72f),
        animationSpec = tween(KararTokens.Motion.normalMs, easing = KararTokens.Motion.Fluid),
        label = "nav-fg"
    )

    Row(
        modifier = Modifier
            .height(KararTokens.Size.navItem)
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = destination.title,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        AnimatedVisibility(
            visible = selected,
            enter = expandHorizontally(animationSpec = tween(220, easing = KararTokens.Motion.Fluid)) + fadeIn(),
            exit = shrinkHorizontally(animationSpec = tween(180, easing = KararTokens.Motion.Fluid)) + fadeOut()
        ) {
            Text(
                text = destination.shortTitle,
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}

/** الشريط الجانبي العائم — Rail على العروض المتوسطة، وDrawer موسّع على الواسعة. */
@Composable
private fun SideNavigation(
    current: AppDestination?,
    expanded: Boolean,
    onSelect: (AppDestination) -> Unit
) {
    val width = if (expanded) KararTokens.Size.drawerWidth else KararTokens.Size.railWidth
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
        targetValue = if (selected) KararTokens.Accent.gold else Color.Transparent,
        animationSpec = tween(KararTokens.Motion.normalMs, easing = KararTokens.Motion.Fluid),
        label = "rail-bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF241A00) else Color.White.copy(alpha = 0.72f),
        animationSpec = tween(KararTokens.Motion.normalMs, easing = KararTokens.Motion.Fluid),
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
        if (expanded) {
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
