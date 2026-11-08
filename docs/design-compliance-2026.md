# مطابقة معايير التصميم 2026/2027 — المصادر والتنفيذ

> تاريخ التحقق: **2026-11-08**. كل بند أدناه مأخوذ من صفحة رسمية (Google /
> Android Developers / Material 3)، وليس من اجتهاد شخصي. أي بند لم يُطبَّق بعد
> مذكور صراحة في «المتبقي».

---

## 1. المصادر الرسمية المستخدمة

| المصدر | ما أُخذ منه |
| --- | --- |
| `developer.android.com/about/versions/16/behavior-changes-16` | إلغاء opt-out من Edge-to-Edge، Predictive Back الافتراضي، إلغاء قيود الاتجاه على `sw>=600dp` |
| `developer.android.com/about/versions/17/behavior-changes-17` | إلزام Edge-to-Edge والاتجاه بلا opt-out، حد ذاكرة RemoteViews للودجت، تقوية الصوت في الخلفية، منع تعديل static final |
| `developer.android.com/develop/ui/compose/layouts/insets` | `WindowInsets.safeDrawing` وإزاحة المحتوى فقط |
| `developer.android.com/develop/ui/views/notifications/live-update` | شروط الإشعار الحي المروَّج، `deleteIntent`، `setUsesChronometer`، `setShortCriticalText` |
| `developer.android.com/topic/performance/app-optimization/enable-app-optimization` | R8: `isMinifyEnabled` + `isShrinkResources`، وتقليص الموارد المحسّن تلقائيًا في AGP 9 |
| `developer.android.com/jetpack/androidx/releases/compose-material3` | Material3 **1.4.0** هو المستقر (تحديث 4 نوفمبر 2026) ويضم Expressive |
| Material 3 Expressive Motion | نابض Spatial (مرونة 0.8) ونابض Effects (مرونة 1.0) |

---

## 2. ما ينطبق على `targetSdk 37` (Android 17) — مُطبَّق

### 2.1 Edge-to-Edge إلزامي
- Android 15/16: التطبيقات التي تستهدف 35+ ترسم خلف أشرطة النظام، و`windowOptOutEdgeToEdgeEnforcement` **معطّل** عند استهداف 36+.
- Android 17: لا يوجد opt-out إطلاقًا.
- التنفيذ الحالي: `enableEdgeToEdge()` في `MainActivity` و`WidgetConfigActivity`، وخلفية جذر واحدة تمتد خلف كل الأشرطة، والإزاحة على المحتوى فقط (`AppShell`)، بلا `windowInsetsPadding` على الجذر.

### 2.2 Predictive Back
- عند استهداف 36+ يعمل الرجوع التنبؤي افتراضيًا، و`onBackPressed` لا يُستدعى.
- التنفيذ: `android:enableOnBackInvokedCallback="true"`، والرجوع عبر Navigation 3 (`NavDisplay.onBack`) بلا اعتراض قديم.

### 2.3 الشاشات الكبيرة
- Android 17 يلغي `screenOrientation` و`resizeableActivity` و`min/maxAspectRatio` على `sw>=600dp` **بلا opt-out**.
- التنفيذ: `rememberAppAdaptiveLayout()` يحوّل الشريط السفلي إلى Rail عند ≥600dp وإلى Drawer عند ≥840dp، والمحتوى يستخدم `fillMaxHeight` بلا افتراض اتجاه.

### 2.4 الصوت في الخلفية (Android 17)
- التطبيق المستهدف 37 يجب أن يشغّل الصوت من الخلفية عبر **Foreground Service**.
- التنفيذ: `AdhanService` بنوع `mediaPlayback` وبإذن `FOREGROUND_SERVICE_MEDIA_PLAYBACK`، ويُستخدم `USAGE_ALARM` مع منبهات دقيقة.

### 2.5 لا انعكاس على أجزاء النظام
- Android 17 يمنع تعديل `static final` بالانعكاس ويعطّل الوصول إلى حقول `MessageQueue`.
- التنفيذ: لا يوجد أي انعكاس على `MessageQueue` أو حقول `static final` في المشروع.

---

## 3. الإشعارات — معايير 2026

شروط الإشعار الحي المروَّج (Promoted Ongoing):
1. نمط قياسي أو `BigTextStyle` أو `ProgressStyle` أو `CallStyle` أو `MetricStyle`.
2. إذن `POST_PROMOTED_NOTIFICATIONS` (مُعلَن في المانيفست).
3. `setRequestPromotedOngoing(true)`.
4. `setOngoing(true)`.
5. `contentTitle` موجود.
6. **بلا** `customContentView` / RemoteViews مخصص.
7. ليس ملخص مجموعة، وليس `setColorized(true)`.
8. قناة الإشعار ليست `IMPORTANCE_MIN`.

سلوك UX إلزامي:
- `setDeleteIntent` لكشف سحب المستخدم، و**عدم** إعادة نشر الإشعار بعد السحب حتى الحدث التالي.
- استخدام `setUsesChronometer(true)` و`setChronometerCountDown(true)` للعدّ التنازلي داخل الشريحة.
- استخدام `setShortCriticalText` لاسم الصلاة في شريحة الحالة.

> حالة التنفيذ: **مُنفَّذ**. الإشعار الحي في `NotificationDesign.buildLive` يستخدم
> `NotificationCompat.ProgressStyle` واحدًا لكل الإصدارات، بلا `customContentView`
> (شرط الترقية)، مع `setShortCriticalText` و`setRequestPromotedOngoing` (يُهملان
> قبل Android 16) و`setProgress` قياسي للإصدارات الأقدم.

---

## 4. الودجتات — معايير Android 17

- حد ذاكرة `RemoteViews` = `6 × العرض × الارتفاع` بكسل لكل تهيئة شاشة، وإذا تجاوزه التطبيق المستهدف 37 يرمي `IllegalArgumentException` ويرفض التحديث.
- الحلول الإلزامية:
  1. حساب الميزانية من `WindowManager.currentWindowMetrics.bounds` لا من `displayMetrics`.
  2. تصغير الصور إلى أبعاد الودجت بـ`inSampleSize` واستخدام `RGB_565`.
  3. فك ترميز الصورة مرة واحدة في `provideGlance` قبل `provideContent` عند `SizeMode.Responsive/Exact`.
  4. عدم استخدام `partiallyUpdateAppWidget` لتبديل الصور؛ استخدم `updateAppWidget` كاملًا.
  5. تنفيذ `onCompositionError` لإرجاع `RemoteViews` بلا صور.
  6. القياس: `adb shell dumpsys appwidget`.

> حالة التنفيذ: الودجتات السبعة تستخدم أيقونات مرسومة (لا صور نقطية كبيرة)،
> لكن يجب إضافة `onCompositionError` وتوثيق الميزانية في Gate الودجتات.

---

## 5. R8 — النتيجة المقاسة

| البند | قبل | بعد |
| --- | --- | --- |
| `isMinifyEnabled` | `false` | `true` |
| `isShrinkResources` | `false` | `true` |
| حجم APK | 24,745,147 بايت (v1.2.0) | **10,729,769 بايت** |
| نسبة التخفيض | — | **−56.6%** |
| `classes.dex` | — | 3.84 MB (منها 1.88 MB مضغوطًا) |
| مدة البناء المحلي | — | 27 م 44 ث (Windows، 2 workers) |

تحقق السلامة بعد R8 (من `mapping.txt`):
- `AppRoute` وكل المسارات الفرعية الثمانية بقيت بأسمائها (ضروري لـ`subclassesOfSealed`).
- كل فئات `com.Nightjar.amsakkarrar.widget.*` و`notifications.*` بقيت.
- أصول Compose سليمة داخل APK: `cities.json` (9.1MB)، `adhan.mp3`، `munajat_manzuma.mp3`، `app_icon.png`.

ملاحظات رسمية مطبَّقة:
- AGP 9.0+ يفعّل **optimized resource shrinking** تلقائيًا مع `isShrinkResources`، فلا حاجة لـ`android.r8.optimizedResourceShrinking`.
- R8 Full Mode مفعّل افتراضيًا؛ لا يوجد `android.enableR8.fullMode=false` في المشروع.
- AGP 9.3+ ينتقل إلى `optimization { enable = true }`؛ مشروعنا على 9.2.1所以在 يستخدم الـDSL القديم الموصى به لتلك النسخة.

---

## 6. Material 3 Expressive — ما طُبّق فعليًا

- حزمة `androidx.compose.material3:material3:1.4.0` (المستقرة) تحتوي فعليًا:
  `MaterialExpressiveTheme` و`MotionScheme.expressive()` و`ShortNavigationBar` و`WideNavigationRail`.
- **قيد مهم تم التحقق منه بالبناء:** في Compose Multiplatform 1.12.2 (حزمة JetBrains) هذه الواجهات ما زالت `internal` في `commonMain`، لذا لا يمكن استدعاؤها من الكود المشترك.
- البديل المطبَّق: رموز Expressive نفسها في `KararTokens.Motion`:
  - `spatial()` = `spring(dampingRatio = 0.8f, stiffness = 380f)` — للانتقالات والأحجام.
  - `effects()` = `spring(dampingRatio = 1.0f, stiffness = 3800f)` — للتلاشي والألوان.
  - نسخ Fast/Slow مطابقة لـ`ExpressiveMotionTokens`.
- استُبدلت انتقالات `tween` في `AppShell` بنوابض Spatial/Effects.

### 6.1 الشريط السفلي
- النمط المطبَّق: `ShortNavigationBar` (مؤشّر كبسولي خلف الأيقونة + اسم أسفلها).
- **عرض ثابت لكل وجهة** (`Modifier.weight(1f)`) بدل `LazyRow` والتوسّع بالاسم؛ هذا يزيل:
  - تغيّر حجم الشريط عند ظهور العنوان.
  - تحرّك بقية الأيقونات.
  - خروج «اليوم» من الشاشة أو عدم استجابتها.
- السبعة وجهات تتجاوز توصية M3 (3–5) لكنها مطلوبة وظيفيًا؛ ولهذا استُخدمت أيقونات صغيرة مع اسم 9sp داخل كبسولة عائمة.

---

## 7. المتبقي (يُغلق في بوابات المراحل)

- [x] تحويل الإشعار الحي إلى `ProgressStyle` بلا RemoteViews مخصص + شريحة زمنية
      (`NotificationCompat.ProgressStyle` + `setUsesChronometer` + `setShortCriticalText`).
- [x] `onCompositionError` لكل ودجت + بديل نصي خفيف `widget_error.xml`
      (التفاصيل في `docs/widgets-android-7-to-18.md`).
- [ ] اختبار Edge-to-Edge على جهاز/محاكي (لا جهاز متصل الآن).
- [ ] اختبار التنبؤي وRail/Drawer على 320/600/840dp.
- [ ] مراجعة أنيميشن Splash ليطابق `Startscreen .txt` مع خلفية `#080514`.
- [ ] تفعيل `MaterialExpressiveTheme` عند توفرها في CMP مستقبلًا (تعليق موثّق في `KararTheme.kt`).
- [ ] **Android 18**: لم يُعلن بعد (صفحة `/about/versions/18` غير موجودة في
      2026-11-08)؛ التحصين الاستباقي موثّق في `docs/widgets-android-7-to-18.md`.

---

## 8. مطابقة الودجتات والإشعارات بالمرجعين البصريين

المرجعان: `appkarv/widgetsnotifications.html` (الودجت الرئيسي + الإشعار الحي +
إشعار الأذان) و`appkarv/WIDGETSPRO.txt` (12 ودجت: WhiteMaster/MiniRow/DynamicPill…).

### 8.1 الودجت الرئيسي (TodayWidget)

| عنصر المرجع | التنفيذ |
| --- | --- |
| حبة «باقي على …» (bg-white/10 + نص ذهبي) | `TextView` بخلفية `widget_label_pill.xml` داخل `widget_chrono_large.xml` |
| عدّاد كبير 4xl مع توهّج | `Chronometer` 32sp ذهبي + `shadowColor/shadowRadius` (يعدّ من ساعة الجهاز) |
| وحدات ساعة / دقيقة / ثانية تحت الأرقام | صف `LinearLayout` بثلاثة أوزان متساوية داخل نفس التخطيط |
| شريط تقدم بيضوي سميك (36px) | `ProgressBar` بارتفاع 10dp وخلفية `widget_progress.xml` (تعبئة ذهبية متدرجة) |
| طرفا الشريط: منتصف الليل ← الإمساك | `widget_progress_start` / `widget_progress_end` تُملأ من `periodStartLabel` / `periodEndLabel` في `countdownRemoteViews()` |
| شبكة 3×2 للصلوات الست + تمييز الصلاة القادمة | `PrayerGridRow` + `PrayerBoxCell` (خلفية `WidgetActive` وحدود ذهبية للصفة النشطة) |
| كشيدة «رمضـ 16 ـان» + التاريخ الميلادي | `WidgetKashida` + `gregorianText` |

### 8.2 الودجت المصغّر

- نمط `MiniRowWidget` / `DynamicPillWidget`: سطر واحد = عنوان «باقي على …» +
  حبة ذهبية شفافة (`widget_chrono_pill.xml`) تحوي `Chronometer` 15sp.
- في درجة COMPACT لا يُكرَّر عنوان العدّاد في Glance، لأن الـRemoteViews يحمله
  بالفعل؛ يُعرض بدله شريط تقدم Glance رفيع + 3 مربعات صلوات.

### 8.3 الإشعارات

- **الإشعار الحي:** `NotificationCompat.ProgressStyle` (شريحة + نقطة + أيقونة قمر)
  \+ `setUsesChronometer` + `setChronometerCountDown` = نفس «العدّاد + الشريط»
  في المرجع، بلا RemoteViews مخصص. `setShortCriticalText` و
  `setRequestPromotedOngoing` على Android 16+.
- **إشعار الأذان:** `notification_prayer.xml` يطابق بطاقة المرجع: جرس + «حان الآن
  موعد …» + «تقبل الله طاعتكم» + صندوقان متساويان (الوقت الحالي / الصلاة القادمة
  باسمها ووقتها الذهبي).

### 8.4 بقية الودجتات

| الودجت | النمط المرجعي | ملاحظة |
| --- | --- | --- |
| TimesWidget | شبكة الصلوات + عدّاد + شريط | نفس محرّك العدّاد الموحّد |
| CountdownWidget | DynamicPill (جزيرة) + عمودي | يتحول تلقائيًا لوضع الجزيرة عند العرض > 230dp والارتفاع < 135dp |
| HijriWidget | YellowNumber / MoonDate | كشيدة + تاريخ هجري/ميلادي + تقدم النهار |
| ClockWidget | MinimalGlass | `TextClock` أصلي + اليوم/التاريخ/المدينة |
| JourneyWidget | VerticalJourney | خط زمني بنقاط، والصفة القادمة بلون وردي |
| TasbihWidget | بطاقة دائرية | عدّاد دائري + شريط + تصفير/إعدادات/فتح التطبيق |

> العدّاد في كل الودجتات `Chronometer`/`TextClock` أصلي، فلا يتوقف بين دورات
> WorkManager ولا يستهلك بطارية.
