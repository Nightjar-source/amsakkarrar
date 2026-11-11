# ملف السياق — اقرأني أولًا قبل أي عمل

> **الغرض:** إذا انتهى سياق النموذج أو بدأت جلسة جديدة، هذا الملف يعيدك إلى نقطة
> العمل الحالية في دقيقة واحدة. الخطة الكاملة في `docs/MASTER-PLAN-amsak3d.md`.

---

## 1. الحالة الآن

| البند | القيمة |
| --- | --- |
| الفرع | `main` |
| آخر التزام كود | `564a260` — مدفوع (v1.5.8 — مفاتيح الجنان + القارئ الموحّد) |
| الإصدار الحالي | `versionCode 10507` / `versionName 1.5.8` — في الطريق (tag بعد البناء) |
| التعديلات معلّقة | تحقق بصري + لمس الودجتات + المشغل — معلّقة على الجهاز |
### 1.2 إعادة تصميم الشاشات — 2026-11-08 (مدفوعة في v1.3.0)

- **Splash:** مقاسات نسبية عبر `BoxWithConstraints` (تعمل من 320dp وفي الوضع
  الأفقي) بدل 310dp الثابتة، وأيقونة الـSplash داخل منطقة آمنة
  (`splash_icon_safe.xml`، inset 14%).
- **الثيم:** داكن دائمًا في `values/` و`values-night/` (Parent داكن،
  `windowLightStatusBar=false`, `forceDarkAllowed=false`) لمنع وميض الإطار
  الأول وضبط أيقونات الشرائط.
- **الأيقونة:** طبقة أمامية حقيقية `ic_launcher_foreground` + `<monochrome>`.
- **الشل:** محتوى الشاشات غير السماوية بعرض أقصى 1024dp وموسّط
  (`contentMaxWidth`)، وشريط العنوان 896dp.
- **اليوم:** ضبط المسافات الرقمية للعدّاد والنقاط والهيدر.
- **التقويم:** شريحتا «المنقضية/المتبقية» في الهيدر + بطاقة اليوم بشبكة
  3 أعمدة لخلايا الصلوات (أيقونة مائية 10%، وقت 11sp، اسم 9sp) + تعتيم
  الأيام المنقضية (`alpha 0.5`) + بطاقة اليوم بحدود ذهبية ومقياس 1.02.
- **الضبط:** هيرو «إمساكية كرار» (تاريخ هجري/ميلادي، ساعة حيّة، UTC، بحث
  مدينة، GPS/IP، شبكة طرق الحساب 2×4).
- **الأدعية:** انتقال `AnimatedContent` نابض (Expressive spatial/effects)
  بدل `Crossfade(tween(220))`.
- **الأيقونات:** `KararIcons.prayer(id)` مصدر موحّد لليوم والتقويم.


### 1.3 جولة التكيّف والأدعية — 2026-11-08 (مدفوعة في v1.3.0)

- **الأدعية:** أُضيف «دعاء الغيبة (يا من تحل به عقد المكاره)» في
  `DuaTexts.GHAYBA` وعنصر `ghayba` في `ShiaDuasLibrary` (فئة «تعقيبات
  الصباح والمساء») **دون حذف** أي من الأدعية التسعة السابقة ⇒ الفهرس الآن 10.
- **التقويم:** بطاقة كل يوم تعرض الآن التاريخين معًا: الرقم في الدائرة يتبع
  وضع العرض (هجري/ميلادي)، وتحته سطر هجري كامل (`يوم شهر سنة هـ`) وعلى
  اليسار التاريخ الميلادي الرقمي + اسم الشهر الميلادي.
- **شبكة التقويم:** `LazyVerticalGrid(GridCells.Adaptive(340.dp))` — عمود على
  الهاتف، عمودان على التابلت، ٣ على الحاسوب؛ الهيدر يمتد `maxLineSpan`.
- **شبكة الأدعية:** `GridCells.Adaptive(320.dp)` — نفس السلوك، مع بقاء
  البحث والتصنيفات بعرض كامل.
- **اليوم:** الخلفية السماوية تبقى full-bleed، لكن القوس/العدّاد/الشريط داخل
  مسرح موسّط أقصى عرضه `todayStageMaxWidth = 720dp` (تابلت/حاسوب/مطوية).
- **القراءة الطويلة:** تفاصيل الدعاء وشاشة الفقه داخل عمود موسّط
  `readingMaxWidth = 840dp` على الشاشات العريضة.
- **الرموز:** `calendarCardMinWidth` / `duaCardMinWidth` /
  `todayStageMaxWidth` / `readingMaxWidth` في `KararTokens.Size`.

### 1.4 المراحل A+B+C+D1 — 2026-11-08 (غير مدفوعة بعد)

- **A1 — الوضع الثلاثي:** `AppSettings.themeMode` (`system/light/dark`) +
  `ThemeMode` + ترحيل `darkTheme` القديم في `AppState.loadSettings`، مفتاح
  ثلاثي أنيق في «الطقس والمظهر»، `MainActivity` يطبّق `KararTheme(mode)`،
  و`ApplySystemBarAppearance` (expect/actual) يضبط أيقونات أشرطة النظام.
  `themes.xml` (فاتح) و`values-night` (داكن) و`app_background_light`.
- **A2 — خلفية واحدة:** حُذف `Box(background(#050A15))` من `App.kt`؛
  `AppBackdrop` يقرأ لوحة الوضع الحالي، وشاشة اليوم تبقى sky full-bleed.
- **B1 — «اليوم» يعمل:** استُبدل `NavDisplay` للتبويبات بحالة صريحة
  `currentTab` + `AnimatedContent`، مع شاشة فرعية `widgetsOpen` وحالة تفاصيل
  دعاء ورجوع نظامي عبر `PlatformBackHandler`.
- **B2 — الشريط السفلي:** العنصر النشط يتمدّد أفقيًا (`animateDpAsState` +
  `spatial`) ويظهر عنوانه على تدرّج teal→indigo؛ غير النشط أيقونة فقط.
- **B3 — انتقالات المنصة:** `isIosUi` (expect/actual): أندرويد shared-axis
  خفيف، iOS انزلاق Cupertino كامل للشاشات الفرعية وتلاشٍ للتبويبات.
- **B4 — الشريط الجانبي:** Rail/Drawer بعرض متحرك نابض وأسماء تظهر بتلاشٍ.
- **C — السبلاش:** `StartupGate` (mutableState مشترك) + `setKeepOnScreenCondition`
  في `MainActivity`؛ `SplashScreen` يرفع الجهوزية بعد أول إطار، والمدة الدنيا
  1.2s. `splash_icon_safe` صار inset 17% لمنع قطع الحلقة.
- **D1 — المدينة:** `CityDatabase` محمي بـ`Mutex` وكل فكّ/فرز/بحث على
  `Dispatchers.Default`، فهرس `stableId`، و`CityPickerDialog` أصبح
  `ModalBottomSheet` ببحث مثبّت وdebounce 220ms وحالة تحميل وعدّاد نتائج
  وزر «موقعي».
- **G3.1 — المشغل:** مشغل واحد مرفوع إلى `AppShell`؛ شريط مدمج 28dp مع خط
  تقدم 2dp **تحت النص**، زر فصل يحوّله كرة عائمة تُسحب بحدود آمنة، ولوحة
  موسّعة بشريط تمرير + تثبيت/إغلاق. أُزيل المشغل من `DuaHeaderCard` ومن
  `DuasScreen` مع إبقاء زر تشغيل صغير في التفاصيل.
- **الشريط الموحّد:** شريط عنوان واحد في الشل، العنوان وحده يتغيّر بحركة
  (`AnimatedContent`) عند تبديل التبويب أو فتح دعاء، مع سهم رجوع واحد.
- **التحقق الآلي:** `:shared:compileAndroidMain` و`:androidApp:processDebugResources`
  نجحا. بوابات الشاشة/الإشعارات/الصوت/الودجتات ما زالت معلّقة (لا جهاز).

**الخطوة التالية:** E1–E4 (بطاقة إشعار واحدة + صوت مرة واحدة + تصميم
`widgetsnotifications.html` + عدم التعليق)، ثم F1–F7 (الودجتات)، ثم G4/G5/G6
(التسبيح/التقويمان) وG2/G1 (إعادة بناء الأدعية/اليوم) وتدقيق ألوان الوضع الفاتح.

### 1.5 جولة «السلاسة والخفة» — 2026-11-09 (مدفوعة في v1.4.0)
هدف: تنقّل بثابت إطارات وحركة نابضية بلا عمل زائد في كل إطار أو كل ثانية.

- **Nav3 كحالة واحدة:** `AppShell` يستخدم `rememberNavBackStack` + `NavDisplay`
  (لا `currentTab`). تبديل التبويب من شاشة فرعية يُنفَّذ داخل
  `Snapshot.withMutableSnapshot` (مسح الفروع + تبديل الجذر) ⇒ إعادة تركيب
  واحدة وحركة واحدة بدل سلسلة حركات.
- **انتقالات أخف:** أُزيل `scaleIn/scaleOut` من انتقالات أندرويد (كل scale
  يفرض طبقة رسم إضافية)، و`sizeTransform = null` يمنع قصّ/قياس المشهد بين
  الشاشات. المتبقي: انزلاق قصير (1/8 عرض) + تلاشٍ نابض.
- **الشل لا يُركَّب كل ثانية:** `today` قيمة مشتقّة (`derivedStateOf`) من
  `state.now`؛ لا تُبلّغ إلا عند تغيّر اليوم، فـ`NavDisplay` وكل الشاشات
  الأخرى لا يُعاد تركيبها مع نبض المؤقّت.
- **الطقس:** مؤشّرات المطر/الثلج تُنشأ فقط عند الحاجة (كانت تعمل في يوم
  غائم فقط)، وكل تحويلات dp→px تُحسب مرة لكل إطار بدل مرة لكل شكل.
- **حركة نابضية موحّدة:** استُبدل كل `tween` انتقالي بـ
  `KararTokens.Motion.spatial/effects` في اليوم (النقاط، أرقام العدّاد،
  بطاقات الصلوات) والضبط (الأكورديون) و`SkyArc` والسبلاش. المؤقّتات
  اللانهائية (الطقس/السبلاش/المسبحة) تبقى خطية لأنها دورية بطبيعتها.


### 1.1 آخر تحقق آلي — 2026-11-10 (v1.4.1)

- **v1.4.1:** وسم `v1.4.1` على `d452a3d`، تشغيلان سحابيان ناجحان:
  - `42817054544` (`workflow_dispatch` على `main`): نجاح خلال 4 دقائق.
  - `42817769131` (وسم `v1.4.1`): نجاح وأنشأ GitHub Release.
- APK موقّع: **10,927,488 بايت**، SHA-256
  `7acc37d67b45a0c5e64c32ac62d5f46431f49c56ba3534e11cff2815d6759784`.
- التوقيع: `CN=Nightjar` (شهادة `01e73103…78d`)، v2 scheme عبر `apksigner`.
- `aapt badging`: `versionCode 10401` / `versionName 1.4.1` / `targetSdkVersion 37` / `compileSdk 37`.
- البناء المحلي قبل الدفع: `:shared:compileAndroidMain` + `:androidApp:compileDebugKotlin`
  **BUILD SUCCESSFUL** (6m51s) بعد إصلاح 3 أخطاء ترجمة.

#### v1.4.0 (تاريخي)

- **v1.4.0:** وسم `v1.4.0` على `0577bab`، تشغيلان سحابيان ناجحان:
  - `42633294491` (`workflow_dispatch` على `main`): نجاح خلال 4 دقائق.
  - `42634292297` (وسم `v1.4.0`): نجاح وأنشأ GitHub Release.
- **التوقيع:** الأسرار المشفّرة في GitHub (`KEYSTORE_BASE64`,
  `KEYSTORE_PASSWORD`, `KEY_PASSWORD`) — كانت فارغة فضُبطت عبر sealed box.
- APK موقّع: **10,910,816 بايت**، SHA-256
  `9b58ecb6113a05abdc9d3d2a91c104f853e43224400e633acac7db7e010c8fba`.
- التوقيع: `CN=Nightjar` (شهادة `01e73103…78d`)، `apksigner` — v2 scheme.
- `aapt badging`: `versionCode 10400` / `versionName 1.4.0` / `targetSdkVersion 37` / `minSdk 24`.

#### v1.3.0 (تاريخي)

- **v1.3.0:** وسم `v1.3.0` على `bfe6464`، CI run `42500279791`: **نجاح** كامل
  (بناء R8 + توقيع + GitHub Release).
- APK موقّع: **10,823,037 بايت**، SHA-256
  `9F7F5C43238E763129DC6E7ECC55D6A99E3399AE626EE5D18544A1EB0029B523`.
- التوقيع: `CN=Nightjar` (شهادة `01e73103…78d`) — تم التحقق بـ`apksigner`.
- أُرسل إلى تلغرام «خاص» (`-1001966716959`): `amsakkarrar-v1.3.0.apk` (10.32MB).
- التحقق المحلي قبل الوسم: `:shared:compileAndroidMain` و
  `:androidApp:processReleaseResources` ناجحان.
- CI run `42491087625` السابق (v1.2.0) على `37004d8`: نجاح بناء R8 الكامل.
- APK موقّع: **10,728,661 بايت**، SHA-256
  `ABADB28246B33BEE3F7766ABD5E60EA40A2D1FA25E0C7CA85BD0F5F4FC1C332A`.
- التوقيع: `CN=Nightjar` (شهادة `01e73103…78d`).
- `workflow_dispatch` الآن يبني الـartifact فقط؛ إنشاء GitHub Release محفوظ
  للوسوم `v*` (كان يفشل سابقًا برسالة «GitHub Releases requires a tag»).
- إصلاحات تصميم الودجتات مطبَّقة: حبة العدّاد + وحدات ساعة/دقيقة/ثانية +
  شريط تقدم 10dp + طرفا الشريط، والودجت 4×3 يستخدم عدّادًا مصغّرًا ليتّسع
  للصلوات الست (التفاصيل في `docs/design-compliance-2026.md` §8).

---

### 1.6 النوافذ المنسدلة المدمجة + تدقيق 2026-11-09

- **`MergedAccordion`** مكوّن واحد (في `ui/components/`): إطار واحد فيه الرأس
  ثم المحتوى داخل نفس السطح، فيتقلّص الإطار ويتوسّع ككتلة واحدة. طُبِّق على
  الضبط (كل الأقسام)، والفقه (كل المقالات)، والمسبحة (كل مجموعة أذكار).
  - فاصل رقيق بين الرأس والمحتوى يظهر بتلاشٍ داخل النافذة.
  - الطي/الفرد بنابض `spatial`، والخروج أسرع (`SpatialStiffnessFast`).
  - الطي يُحفظ لكل عنوان عبر `rememberSaveable` (المسبحة تفتح موسّعة).
  - أُزيلت البطاقة الداخلية من «الموقع والمدينة» حتى لا يظهر إطار مزدوج.
- **أندرويد 17+:** `compileSdk`/`targetSdk` = 37، `AdhanService` يعلن النوع
  `mediaPlayback` صراحةً عند `startForeground` من API 29+، ومنبهات دقيقة مع
  تراجع تلقائي عند غياب الصلاحية.
- **قاعدة المدن:** `node scripts/fix-cities-json.js` → 73,757 صفًا،
  `renamed: 0, dropped: 0`، 0 صفوف معطوبة، العراق 131 صفًا / 19 محافظة /
  0 أسماء لاتينية، وكربلاء بإحداثياتها ومنطقتها الزمنية.
- **إرشادات الحركة:** `docs/motion-guidelines.md` (خلاصة مهارات Emil Kowalski
  مطبَّقة على Compose).

### 1.7 جولة إعادة الضبط v2 — 2026-11-10 (غير مدفوعة)

> الخطة المفصّلة لكل نقطة: `docs/PLAN-V2-2026-11-10.md` (قراءة مرجعية ← معمارية
> UI-Free Core/UDF ← أعطال ← الوضع ← تنقل ← شاشات ← إشعارات/ودجتات ← سبلاش ← بناء).

- **معمارية:** فصل المنطق عن الواجهة — `ToastKind`/`ToastMessage` في النواة
  (`core/state/AppState.kt`) بلا أي رسم، و`showToast`/`dismissToast` أحداث.
- **شريط التنقل السفلي (أُعيد تصميمه حرفيًا من المرجع):** كبسولة `rounded-full`،
  **كل العناوين ظاهرة دائمًا** بأيقونة واسم، نشط = تدرج teal→indigo + `scale 1.03`
  بحركة في `graphicsLayer`، غير نشط = أيقونة teal 70% + نص أبيض 55%، والصف
  `LazyRow` قابل للتمرير مع `animateScrollToItem` لإبقاء النشط ظاهرًا.
  - هذا يحل قصّ «اليوم» على الشاشات الضيقة (كانت العنصر الأول يُدفع خارج
    الحاوية بسبب التوسّع/الطي بعرض ثابت) ويحل انتقاد التوسّع.
- **التنبيهات العائمة:** `AppToastHost` + `ToastPill` + `LocatingPill` في `AppShell`
  (كبسولة بيضاء أنيقة، دخول/خروج نابض، إخفاء تلقائي، زر إجراء).
  - أخطاء الموقع/IP تظهر ككبسولة (لا إغلاق للتطبيق) مع زر «تشغيل الموقع».
  - أثناء تحديد الموقع: حلقات رادار نابضة حول أيقونة الموقع (حتى لو كان GPS سريعًا).
- **اختيار المدينة (إصلاح الانهيار):** لا `fillMaxHeight` داخل الورقة السفلية
  (كان يسبب انهيار قياس محتملًا)، `contentType` للعناصر، و`distinctBy { stableId }
  **` بعد البحث أيضًا حتى لا تتكرر مفاتيح `LazyColumn`.
- **الأذان/الصوت:**
  - `USAGE_MEDIA` بدل `USAGE_ALARM` في `AdhanService` ⇒ زر الصوت في الهاتف هو المتحكم
    وكتم النظام يُحترم.
  - احترام `RINGER_MODE_SILENT` (لا صوت إطلاقًا) و`RINGER_MODE_VIBRATE`.
  - نجاح المسار الأساسي يزيل أي بطاقة احتياطية (`clearFallback`)، و`showAdhanFallback`
    تلغي النسخة السابقة قبل النشر ⇒ **بطاقة واحدة لكل حدث**.
- **الإشعار الحي:** لا يُعاد نشره إلا إذا تغيّر المحتوى فعليًا (بصمة
  `nextLabel|nextTime|percent|remaining/30`) بدل إعادة الرسم كل فتحة للتطبيق.
- **الوضع الفاتح:** استبدال الألوان الداكنة الحرفية في `TasbihScreen`
  (`Slate`/`SlateCard`/`SlateMuted`/`082F49`) برموز اللوحة (`KararColors.*` +
  `OnAccent` حسب `IsDark`).

### 1.8 جولة v1.4.2 — إشعارات مصمّمة + صوت مطابق للمعايير + أدعية edge-to-edge

- **الإشعار الحي صار البطاقة المصمّمة نفسها:** كان يستخدم
  `NotificationCompat.ProgressStyle`؛ الآن يبني `notification_live.xml`
  (النسخة الحرفية من `widgetsnotifications.html`) عبر RemoteViews:
  هيدر «رمضان مباشر» + نقطة حمراء + الوقت المستهدف + `Chronometer` تنازلي
  حقيقي داخل النظام + شريط تقدّم بشكل حبة دواء (`notif_progress_day/night`).
  - العدّاد ينبض بلا أي تحديث من التطبيق ⇒ لا استهلاك بطارية إضافي.
  - `setOngoing(false)` ⇒ قابل للسحب، و`LiveDismissReceiver` يمنع عودته.
- **حذف سبب التكرار المرئي:** أُزيل `setRequestPromotedOngoing` و
  `setShortCriticalText` — كانا يُظهران شريحة إضافية فوق البطاقة على
  Android 16+ فيبدو الأمر كإشعارين لنفس العدّاد.
- **شكل أندرويد الأصلي:** `setColorized(false)` في البطاقتين — التصميم
  مرسوم داخل RemoteViews، والإطار يبقى إطار النظام (بلا تلوين كتلي).
- **سحب الإشعار يوقف الصوت فعليًا:** `setDeleteIntent` وزر «إيقاف الأذان»
  صارا `PendingIntent.getBroadcast` إلى `AdhanStopReceiver` جديد، يستدعي
  `stopService` (مسموح دائمًا من الخلفية) بدل `startService` الذي قد يرمي
  `BackgroundServiceStartNotAllowedException` على 12+ ويترك الصوت عالقًا.
- **اتساق مسار الصوت:** طلب تركيز الصوت صار `USAGE_MEDIA`/`STREAM_MUSIC`
  (كان `ALARM` بينما التشغيل `MEDIA`)، وقناة الاحتياط صارت `USAGE_MEDIA`
  أيضًا ⇒ كتم النظام وزر الصوت يُحترمان في كل المسارات.
- **وضع الاهتزاز:** `RINGER_MODE_VIBRATE` لا يُصدر صوتًا ويُشغّل نمط
  الاهتزاز فقط (كان يتجاهله فيشغّل صوتًا كاملًا).
- **الأدعية edge-to-edge:** بطاقات القائمة صارت صفوفًا شفافة على خلفية
  الشاشة مع فاصل خطّي متلاشٍ بدل الكتل الممتلئة، وصفحة الدعاء بلا بطاقة
  خلف النص.
- **الوضع الفاتح:** استبدال بقية الألوان الحرفية في `TasbihScreen`
  (`#E2E8F0`)، و`SettingsScreen` (الهيرو + حقل البحث + شرائح)،
  و`WidgetsScreen` (رسالة الحالة)، و`FiqhScreen` (تنبيه الفلك).
- **توحيد شريط التقدم:** `widget_progress*.xml` صار بنفس تدرّجات
  `notif_progress*.xml` (نهار `#FFE066→#FF9F1C→#E71D36`،
  ليل `#80A1D4→#C0D6DF→#F2F5EA`) ونفس نصف القطر والحد.

### 1.9 جولة v1.5.0 — «المفاتيح» + حركات M3 Expressive + طباعة الأذكار الشرطية

- **شريط التنقل:** مؤشر سائل واحد (offset بنابض `SpatialDefault 380/0.8`) يوضع خلف
  خانات متساوية العرض؛ كل العناصر ظاهرة بلا طي؛ 52dp لمس لكل عنصر؛ بلا
  LazyRow/توسّع.
- **بوب الرجوع:** `back()` الآن دائمًا: تفاصيل دعاء ← رجوع خطوة واحدة إلى
  «المفاتيح» ← رجوع إلى «اليوم» ← خروج النظام. انقر زر الرجوع في أي تبويب
  أصبح لا يخرج إلا من اليوم.
- **الأدعية = المفاتيح:** التبويب الرابع باسم «المفاتيح»؛ نافذة أيقونات
  (الأدعية والمناجاة / القرآن الكريم / البوصلة والمراقد / المسبحة / أعمال اليوم).
  - الشاشات الخمس تُفتح كمفاتيح فرعية في الـback stack (اليوم ← المفاتيح ← الشاشة).
  - **القرآن الكريم:** فهرس 114 سورة أوفلاين + نص السورة من مصدر عام
    عند الفتح فقط (expect/actual `fetchRemoteText`؛ iOS يعيد null ويعرض
    رسالة «تحقق من الاتصال» مع زر إعادة المحاولة).
  - **أعمال اليوم:** تعهّد يومي (6 أعمال) + ملاحظات حرّة (تحفظ في
    `AppSettings.notes`، صفر في اليوم التالي، تُبقى الملاحظات) + `todayTasksDone/todayTasksDate`.
  - **ضُبط الترتيب:** اليوم ← التقويم ← المفاتيح ← فقه المواقيت ← الضبط (الضبط في الأخير).
- **الإشعارات:** حُذف «رمضان مباشر» (نص منقول)؛ العنوان صار «إمساكية كرار».
  أُزيل `DecoratedCustomViewStyle` من الإشعارين — تُعرض الكبسولة المصمّمة
  كما هي (أندرويد لا يعطي حدودًا سوداء حول أيقونتي البطاقتين).
  تُطلب معاينة الإشعار لحظة التشغيل الأول عبر نافذة نظام فوق التطبيق،
  وإن رُفضت نهائيًا يتم فتح إعدادات التطبيق (الحالة الثانية فقط).
- **تشخيص الصوت والإشعار:** عبارات مهنية («التشخيص والمعاينة»)، وأزالنا
  ذكر الملف الصوتي؛ أزرار «معاينة الأذان / معاينة الإشعار».
- **حل مشكلة «لا يصل أي إشعار»:** التطبيق لم يكن يطلب `POST_NOTIFICATIONS`
  على أندرويد 13+ — الآن يُطلب بعد 1.6 ثانية من الإقلاع (نافذة النظام فوق
  التطبيق، لا صفحة إعدادات الهاتف) + `LifecycleResumeEffect` في الضبط
  لتحديث حالة الصلاحيات عند العودة من الإعدادات.
- **المشغل:** شريط سفلي واحد `rounded-full` داكن بحدود teal وظل عميق؛
  غلاف ألبوم متدرج ذهبي→وردي، عنوان الدعاء، شريط تقدّم رفيع داخل المحتوى،
  وتوسيع بضغط على المحتوى (Slider حقيقي + وقت الحال/الكلي). **لا كرة عائمة**.
  الارتفاع 64dp. متزامن مع التنقل، ويبقى يعمل أثناء العودة من الأدعية.
- **التنبيهات (Toast):** كبسولة داكنة `#121A2C` بحدود teal، أيقونة ملونة
  داخل دائرة `tint.alpha .18`، زر إغلاق وإجراء بخلفية `Teal.alpha .16`؛
  دخول/خروج بانزلاق نابض. موقع التحديد يصاحبه سطر فرعي.
- **TodayScreen:** ارتفاعات ثابتة لشريط العنوان (66/84dp) ومنطقة العدّاد
  (118/132dp) ومنطقة الصلوات (128/140dp) — القوس لم يعد يرتفع/ينخفض مع
  تغيّر العدّاد، والرقمان ثابتان بعرض 54/68dp.
  - شريط حالة تدرج أسود 24% → شفاف فوق السماء بتغطية 78dp — لا كتلة لونية
    والأيقونات مقروءة في الوضعين مع إزاحة المحتوى فقط.
  - `KararTokens.Motion` الآن: spatial 380/0.8 default، fast 800 (كان 1400)،
    effects الافتراضي 1600 (مطابق MDC/Expressive 2026).
- **غلاف الصفحة:** `contentPadding` يعبّر وضعية اليوم عبر `isToday/immersive`
  (لا شريط حالة فوق «اليوم» ولا شريط علوي، تمامًا كالنسخة المرجعية).
- **صفحة الدعاء:** شريط إغلاق مدمج خاص بها (X دائري + فئة الدعاء/العنوان)
  فوق جسم الدعاء — لا شريط التطبيق الرئيسي ولا شريط سفلي أثناء القراءة
  (edge-to-edge تمامًا).
- **الودجتات:** الودجت «اليوم» 4×3 بدل 4×4 (يقلل الفراغ السفلي)؛
  `WidgetConfigActivity` يدعم edge-to-edge أصلًا مع `safeDrawing`؛
  معاينة الإعدادات تتجاهل خبز «رمضان» (تعني «الإمساكية»).
- **المهارات المرجعية:** ثُبّتت `emilkowalski/skills` (animate، review-animations،
  improve-animations، animation-performance، apple-design، break-ui،
  animation-vocabulary، emil-design-eng)، و`android/skills`
  (adaptive، jetpack-compose-performance، navigation-3/event، …)،
  و`liquid-glass`، و`m3-expressive` (صبغ بـgeo-min 1.0 مع visuals مستثناة)،
  و`kmp-agent-skills` (kmp-compose-animation، وغيرها).
  - ملاحظة: `abhixv/google-m3-expressive-design-skill` تثبّت يدويًا عبر
    sparse clone (حجم 495MB وإلا)؛ لا تُلتزم في المستودع.
- **الرخصة:** `.agents/` و`content-factory/` مستثناة من الالتزامات عبر
  `.gitignore`.

### 1.12 إصلاح CI v1.5.1 — استيراد ناقص في TodayScreen

- **الفشل:** `Unresolved reference 'KararColors'` في `TodayScreen.kt`
  (استدعاءات `KararColors.Gold` الجديدة لانقلتها الاستيرادات).
- **الإصلاح:** إضافة `import com.karrar.amsakiah.ui.theme.KararColors`
  (`ed6547f`)؛ دفع إلى main؛ CI #`42930059246` قيد التشغيل لاستخراجه.
- **الدرس:** التبديل إلى الرموز الداعمة (`KararColors.*`) ثم التشغيل **ليس**
  كافيًا قبل وسم — يجب التحقق من الترجمة (قبل v1.6.0) — جزء 1.13.

### 1.13 جولة v1.5.1 — إصلاحات الترجمة (لماذا تعطلت إصدارات CI)

| المحاولة | SHA | النتيجة | سبب الفشل |
| --- | --- | --- | --- |
| #42925817567 | `ab850eb` | فشل | `KararColors` بدون استيراد في `TodayScreen.kt` |
| #42930059246 | `ed6547f` | فشل | `shortCriticalText(Int/Long)` + Glance `FontWeight` مزدوج + `FontFamily(String)` لا يُقبل بـresource ID (لا يدعم Glance خطوطًا مخصصة) |
| #42932443820 | `d49435b` | فشل | نفس السبب الأول — نجح `androidApp:compileReleaseKotlin` من جديد بعد رفع ترتيب الاستيراد المكرر، و`ReemKufi = FontFamily.Serif` |
| #42934847673 | `d7c90cf` | **نجاح** | نفسه — الإشعار الحي + اللهب + الخطوط |

- **ملاحظة صادقة:** خط Reem Kufi الحقيقي لا يعمل داخل Glance لأنه يقبل فقط
  أسماء خطوط النظام (وليست ملفات `@font/*.ttf`)؛ تطلب الحل نقل kشيدة إلى
  RemoteViews — تم بطلب صريح أو الحصول على نتيجة مع أفضل ما لدى Glance
  (`FontFamily.Serif`).

### 1.14 إصدار v1.5.1 — التحقق والنشر (2026-11-11)

- **الفشل #1 (42925817567 على `ab850eb`):** استيراد `KararColors` مفقود في
  `TodayScreen.kt`. **الإصلاح ed6547f:**
  `import com.karrar.amsakiah.ui.theme.KararColors`.
- **الفشل #2 (42930059246 على `ed6547f`):** نوع `shortCriticalText`
  (كان `Long`، صار `Int`) + Glance font references غير متوافقة.
  **الإصلاح d49435b.**
- **الفشل #3 (42932443820 على `d49435b`):** استيراد مكرر + تركيب
  `FontFamily("reem_kufi")` غير مسموح (Glance لا يدعم `@font/*.ttf`).
  **الإصلاح d7c90cf:** `FontFamily.Serif` (أقرب خط نظام حرفي بقدر Glance).
- **النجاح:** CI #42934847673 (dispatch) و#42935966711 (وسم على `d7c90cf`)
  كلاهما **ناجح**.
- **الحجم النهائي:** `amsakkarrar-release.apk` = 11,439,235 بايت؛
  SHA-256 = `BB205F0E0005778BE39F17D9AFA51BDBC08E78AE9987C27108243CA6017067AE`.
- **فحص APK:** `apksigner verify --print-certs` ناجح (v2 scheme، CN=Nightjar،
  RSA 2048)؛ `aapt badging` ⇒ `versionCode 10501` / `versionName 1.5.1` /
  `targetSdkVersion 37` / `package com.Nightjar.amsakkarrar` /
  `application-label='إمساكية كرار 2026'`.
- الوسم `v1.5.1` مدفوع → GitHub Release موقّع:
  https://github.com/Nightjar-source/amsakkarrar/releases/tag/v1.5.1
- **Honesty gates:** الإشعاران (الصوت/السحب) + لمس الودجت + kشيدة عربية
  على جهاز مقحوم، كلها معلّقة للجهاز — لا أخضر فيها هنا.

### 1.10 إصدار v1.5.0 — التحقق والنشر (2026-11-11)

- **الوسم والإصدار:** `v1.5.0` على `e15a6e4`؛ تشغيلان للوسم والتشغيل:
  `#42912234034` (dispatch على `aaa3b9c`) و`#42913339500` (push على `e15a6e4`)
  كلاهما **ناجح**.
- **الحجم النهائي:** `amsakkarrar-release.apk` = 10,950,585 بايت؛
  SHA-256 = `A3019EF64E9D18DB4DC5452C7EE73F7D3A1F3CE0599996B89459FB90D7215908`.
- **فحص APK:** `apksigner verify --print-certs` ناجح (v2 scheme، CN=Nightjar)؛
  `aapt badging` ⇒ `versionCode 10500` / `versionName 1.5.0` /
  `targetSdkVersion 37` / `package com.Nightjar.amsakkarrar` /
  `application-label='إمساكية كرار 2026'`.
- الوسم مدفوع → GitHub Release موقّع متوفّر للتحميل:
  https://github.com/Nightjar-source/amsakkarrar/releases/tag/v1.5.0
- **ملاحظة إصدار (honesty):** بوابات التحقق البصري والسلوك (الإشعارات على
  الجهاز، الصوت، زر الرجوع، لمس الودجت وفيتباه، القوس عند تغيّر العدّ)
  لم تُتحقق على جهاز — هذه بوابات تحتاج معاينة حية.

### 1.11 جولة v1.5.1 — اللهب + Live Update على أندرويد 16+

- **اللهب:** أيقونة اللهب البنيّة عروضيًا تُرسم `ic_notif_flame.xml` فوق
  `notif_flame_circle.xml` (تدرج `#FDE047→#F97316`)؛ هيدر «رمضان مباشر» بمسافة
  0.02em، «متبقي» أحرف كبيرة، Chronometer 24sp، شريط تقدّم **28dp** (حبة دواء)
  — حرفيًا من `widgetsnotifications.html`.
- **الإشعار الحي مساران:** بوابة `Build.VERSION.SDK_INT >= 36`:
  - أندرويد 16+ → **Live Update** عبر `NotificationCompat.ProgressStyle`
    (androidx-core 1.18 سيلك Importance ثابت) — `setStyle()` +
    `setOngoing(true)` + `setShortCriticalText("HH:mm أو N دقيقة")` +
    `setRequestPromotedOngoing(true)`. يترقّى للشريحة في شريط الحالة
    (Now Bar على سامسونج One UI 8) دون التلاعب بالـRemoteViews/colorized.
  - أندرويد 7–15 → بطاقة `notification_live.xml` كما كانت.
- **المانيفست:** `POST_PROMOTED_NOTIFICATIONS` (إذن Live Update لا runtime).
- **الخطوط:** Amiri + Reem Kufi (SIL OFL 1.1، Google Fonts) مثبتة
  (`composeResources/font/`) وتمر عبر `KararFonts`.
- **الودجت:** kشيدة تُرسم بـReem Kufi (`R.font.reem_kufi`).
- **الإشعار الصلاة:** «حان الآن موعد {الاسم}» بالذهبي، نفس الزوايا (35dp).
- **التقويم — الشكل الثاني (السنوات):** الضغط على قيمة «السنة» في شريحة
  التقويم يفتح شبكة 12 سنة (هجرية أو ميلادية حسب الوضع) مع سهمَي تنقّل
  للنطاق التالي/السابق وزر «رجوع»؛ اختيار سنة يقفز إلى تقويمها الشهري.
  - الشكل الأول يبقى كما هو: بطاقة الشهر القصيرة من شاشة القوس
    (`ArcCalendarSheet`) + تقويم الشهر الكامل من شريط التنقل.
  - بلا زر إضافي في الصف ⇒ لا يزاحم «اليوم» على الشاشات الضيقة.
- **التقويم — الوضع الفاتح:** استبدال كل الألوان البيضاء الشفافة
  (`0x1AFFFFFF`, `Color.White.copy(...)`) برموز اللوحة في `ModePill`,
  `CalendarStepper`, `StepChip`, `MonthArrow`, `CalendarStatChip`,
  `DayCard`, `PrayerTimeCell`.

---

## 2. اقرأ بهذا الترتيب

1. `AGENTS.md` — قواعد المشروع.
2. `docs/MASTER-PLAN-amsak3d.md` — الخطة الكاملة وبنك الأهداف.
3. هذا الملف — حالة اللحظة.
4. `appkarv/amsak3d.tsx` — المرجع البصري (لا تعدّله).

---

## 3. الخطوة التالية فورًا — المرحلة 7

1. **Splash:** شاشة Android 12 مطابقة للشاشة المتحركة الثانية وفق
   `Startscreen .txt`، بلا وميض، وأيقونة غير مربعة.
2. **Gate 7:** تشغيل فعلي على جهاز للتأكد من الشاشة والأيقونة.

**المرحلة 6 (منجزة برمجيًا — تحتاج Gate جهاز):**
- `cities.json`: 73,757 مدينة، العراق 131 صفًا و0 أسماء لاتينية، 19 محافظة.
- `fix-cities-json.js` آمن للتكرار (`renamed: 0, dropped: 0` عند إعادة التشغيل).
- DST لكل تاريخ عبر `dstMode` (auto/summer/winter) و`offsetHours(settings, date)`.
- الموقع: نافذة صلاحية نظام، كشف تعطيل الخدمة + زر تشغيل، بحث GPS/IP/مدن أوفلاين.
- الطقس: تراجع آمن بلا مسح القيمة القديمة عند فشل التحديث، ويتوقف في الخلفية.
- **تدقيق الموارد/الإشعارات:** المؤقّت والطقس والبوصلة والمشغّل مرتبطة بدورة
  الحياة، إشعار حي قابل للسحب مع `deleteIntent` ومنع إعادة النشر، أذان قابل
  للسحب يوقف الخدمة، إصلاح إلغاء المنبهات بـ`action`، وWorkManager فقط مع
  `syncPeriodic/KEEP`، وكاش اللقطة 10s وكاش `packageUpdateTime`.

**المرحلة 5 (منجزة برمجيًا — تحتاج Gate جهاز):**
- الإشعار الحي: `Notification.ProgressStyle` + `requestPromotedOngoing` على
  Android 16+، وRemoteViews (Chronometer/ProgressBar) لما قبله، وقنوات ثابتة.
- إشعار الأذان بزر إيقاف، وإزالة إشعار التشغيل عند التوقف، وبطاقة احتياط واحدة
  على قناة تحمل `raw/adhan.mp3` فقط عند فشل الخدمة.
- `AndroidAudioSource`: مصدر موحّد يفضّل `composeResources/files` (cache)،
  ثم `res/raw` / `android.resource://`، ويجدّد النسخة عند تحديث التطبيق.
- `IosAudioPlayer`: `Res.readBytes` + `AVAudioPlayerDelegate` لإكمال التشغيل.
- `PlaybackCoordinator`: أي تشغيل جديد يوقف الأذان/المناجاة الآخر.
- حذف النسخ المكررة: `assets/sounds/*.mp3` و`res/raw/munajat_manzuma.mp3`.
- بناء ناجح: `:shared:compileAndroidMain` و`:androidApp:assembleDebug` (4m 58s).

**المرحلة 4 (منجزة برمجيًا — تحتاج Gate جهاز):**
- 7 ودجتات Glance مسجّلة: اليوم، المواقيت، العدّاد، الهجري، المسبحة، الساعة،
  المسار — مع درجات COMPACT/MEDIUM/LARGE ووضع الجزيرة للعدّاد.
- `minResizeWidth/Height = 110dp` لكل الودجتات، فتعمل على 2×2 و4×2 و4×4.
- عدّاد Chronometer تنازلي + ProgressBar وTextClock أصلية في RemoteViews.
- `WidgetUpdater`: قناة CONFLATED + Mutex (غير حاجز)، والمستقبلات بـ`goAsync`
  ومهلة 8 ثوانٍ، والعامل الدوري ينتظر `updateAllSuspending`.
- شاشة الودجتات: 7 أشكال + 5 خلفيات (مادة/زجاجي/ضبابي/شفاف/ليلي).
- بناء ناجح: `:androidApp:assembleDebug` (1m 39s).

**المرحلة 3 (منجزة برمجيًا — تحتاج Gate جهاز):**
- `ShrinesQiblaScreen`: بوصلة حية + 17 موقعًا مع المسافة والاتجاه وأسماء
  الأئمة، واختيار الموقع يغيّر لون السهم.
- الأدعية: 9 أدعية/زيارات بمعرّفات ونصوص كاملة، تفاصيل بلا حد مصطنع، بحث
  وتصنيفات، ومشغل عائم واحد بحلقة تقدم حقيقية.
- المسبحة: `customDhikrs` مع إضافة/حذف الأذكار، وقائمة مدمجة آمنة في الشاشة
  وودجت المسبحة.
- الفقه: 8 مقالات كاملة داخل أكورديون + 13 طريقة + قواعد خطوط العرض العليا.
- بناء ناجح: `:shared:compileAndroidMain` و`:androidApp:assembleDebug` (6m 15s).

**المرحلة 2 (منجزة برمجيًا — تحتاج Gate جهاز):**
- `SettingsScreen.kt` أُعيد بناؤه بأقسام منسدلة: الموقع والمدينة (GPS/IP/بحث
  أوفلاين)، مواقيت اليوم، طريقة الحساب، التاريخ الهجري، تعديل ±60، الأوقات
  الظاهرة + `imsakMode`، العدّاد والإشعار الحي، الأذان والتذكير (محدد/الكل)،
  الودجتات، الطقس والمظهر، الصلاحيات، أخرى.
- `CalendarScreen.kt`: تبديل هجري/ميلادي مع مزامنة الشهر عند التبديل، سنة وشهر
  مستقلان، أسهم تنقل، زر «اليوم»، بطاقات أيام بشرائح أوقات `FlowRow` وفق
  `visibleIds`، وحفظ الحالة عبر `rememberSaveable`.
- `App.kt`: تطبيق `fontScale` عالميًا عبر `LocalDensity`.
- `TodayScreen`: الكشيدة تحافظ على سطر واحد بمعزل عن `fontScale`.
- بناء ناجح: `:shared:compileAndroidMain` و`:androidApp:assembleDebug` (6m 14s).

**بوابات معلّقة:** المراحل 1–6 تحتاج اختبارًا بصريًا على جهاز/محاكي
(320dp / 600dp / 840dp).

---

## 4. ثوابت لا تُخالف

- `amsak3d.tsx` هو المرجع البصري الحرفي.
- 7 وجهات فقط؛ الودجتات شاشة فرعية.
- Edge-to-edge حقيقي: خلفية واحدة + إزاحة محتوى.
- Navigation 3 (androidx 1.2.1 / JetBrains 1.2.0).
- Adaptive 1.3.0 الرسمي.
- Kotlin 2.4.21 / CMP 1.12.2 / AGP 9.2.1 / Gradle 9.4.1 — لا ترقية.
- `kotlinx-datetime` 0.6.2.
- لا alpha/beta.
- الصوت في الموارد المشتركة، وبلا تكرار.
- العدّاد في الودجت/الإشعار عبر Chronometer/ProgressBar الأصلية.
- لا أسرار في المستودع.

---

## 5. أماكن الملفات المهمة

| الملف | الدور |
| --- | --- |
| `docs/MASTER-PLAN-amsak3d.md` | الخطة الكاملة |
| `shared/.../ui/navigation/AppShell.kt` | الشل الحالي (Navigation 3 + Adaptive + Edge-to-edge) |
| `shared/.../ui/navigation/AppRoute.kt` | مسارات Navigation 3 |
| `shared/.../ui/theme/KararTheme.kt` | الثيم الحالي |
| `shared/.../ui/theme/KararTokens.kt` | رموز التصميم المركزية |
| `shared/.../ui/theme/SkyColors.kt` | تدرجات السماء الحالية |
| `shared/.../ui/components/SkyArc.kt` | قوس اليوم |
| `shared/.../ui/components/WeatherEffects.kt` | تأثيرات الطقس |
| `shared/.../ui/screens/TodayScreen.kt` | شاشة اليوم الحالية |
| `shared/.../ui/screens/SettingsScreen.kt` | الضبط الحالي (المرحلة 2) |
| `shared/.../ui/screens/CalendarScreen.kt` | التقويم الحالي (المرحلة 2) |
| `shared/.../core/settings/AppSettings.kt` | نموذج الإعدادات |
| `androidApp/.../widget/KararWidgets.kt` | محرك الودجتات |
| `androidApp/.../notifications/NotificationDesign.kt` | الإشعارات |
| `androidApp/.../notifications/AdhanService.kt` | تشغيل الأذان في المقدمة |
| `shared/.../core/audio/AndroidAudioSource.kt` | مصدر الصوت الموحّد (composeResources → cache → raw) |
| `shared/.../core/audio/PlaybackCoordinator.kt` | قفل تشغيل صوتي واحد |
| `appkarv/` | كل مراجع التصميم والبيانات |

---

## 6. ما أُنجز سابقًا (لا تعده)

- إصدار `v1.1.0` موقّع ومرفوع إلى GitHub وتلغرام.
- مجموعات اختيار الصلوات (`adhanPrayers` / `reminderPrayers` / `livePrayers`).
- إصلاحات ترجمة الإصدار (WidgetConfigActivity، NotificationDesign، Glance).
- سكربت نسخة تلغرام المشفّرة + أول نسخة احتياطية.
- تحليل تفصيلي لـ`amsak3d.tsx` وفحص معماري شامل.
- **المرحلة 0**: `KararTokens` + `KararIcons` + `KararTheme` + `GlassSurface` +
  `AppShell` (Navigation 3 + Adaptive + Edge-to-edge) + انتقالات انزلاقية.
- **المرحلة 1**: `SkyArc` + `WeatherEffects` + `TodayScreen` + فلترة
  `visibleIds`/`liveIds` الممركزة.
- **المرحلة 2**: `SettingsScreen` بأقسام منسدلة + `CalendarScreen` M3 Expressive
  + `fontScale` عالمي.
- **المرحلة 3**: بوصلة ومراقد بنصوص الأئمة + أدعية كاملة ومشغل واحد + مسبحة
  بأذكار مخصصة + 8 مقالات فقه في أكورديون.
- **المرحلة 4**: 7 ودجتات متكيفة (2×2–4×4) + عدّاد أصلي + خلفيات + تحديث
  غير حاجز ومزامنة فورية.
- **المرحلة 5**: صوت موحّد من `composeResources/files` على Android وiOS +
  `PlaybackCoordinator` يمنع التزامن + إشعار حي/أذان منظّف + حذف النسخ المكررة.
- **المرحلة 6**: مدن أوفلاين كاملة وأسماء عربية صحيحة + DST لكل تاريخ +
  خيار ذكي/صيفي/شتوي + كشف تعطيل الموقع + طقس بلا وميض.
- **تدقيق الإشعارات/الذاكرة**: توقف العمل في الخلفية، إشعار حي/أذان قابلان
  للسحب، منع إعادة النشر، إصلاح إلغاء المنبهات، وتقليل عمل WorkManager.

---

## 7. القيود الحالية

- لا جهاز/محاكي: لا يمكن التحقق الفعلي من الودجت/الإشعار/الصوت/Edge-to-edge
  ولا بوابات 320/600/840dp.
- iOS غير قابل للبناء على Windows (كود `AVAudioPlayerDelegate` غير مترجم بعد).
- الصوت: مسار المصدر موحّد ومُتحقَّق من وجود الملفات في APK، لكن السماع الفعلي
  على جهاز حقيقي لم يُختبر بعد.
- Android 16: الإشعار الحي `ongoing` إلزامي لترقية Live Update؛ النظام يسمح
  بإخفائه مؤقتًا فقط، بخلاف ما قبل Android 16 حيث يُسحب نهائيًا.
- أسرار GitHub: المستودع لا يحتوي أسرار Actions حاليًا (`total_count=0`)؛
  التوقيع يعمل عبر `keystore_base64.txt` المتتبَّع وكلمة السر الافتراضية.
  يُفضَّل إضافة `KEYSTORE_BASE64` و`KEYSTORE_PASSWORD` و`KEY_PASSWORD` لاحقًا.

### 1.15 إصدار v1.5.2 — الإسفار + النشر (2026-11-11)

- **CI:** dispatch #42950956480 على `2e9b4d0` — **ناجح**؛ وسم `v1.5.2` على نفس الالتزام؛ tag push run #42952071963 — **ناجح**.
- **APK موقّع:** 11,439,235 بايت؛ SHA-256 = `1301E9F4A877B80A7AEB8C799FA241BC3A33838ED9461CBC6B4C86804F2D1CF1`.
- **فحص APK:** apksigner ناجح (v2 scheme, CN=Nightjar). aapt badging ⇒ `versionCode 10502` / `versionName 1.5.2` / `targetSdkVersion 37`.
- **الإصدارات المتحركة:** كل الانتقالات الآن على Typography بنابض، لا tween؛ الجرد الكامل: 0 tween لناقل حالة، 10 tween للحركات اللامنتهائية (مؤقّت/طقس/نبض).
- الوسم المدفوع → GitHub Release موقّع:
  https://github.com/Nightjar-source/amsakkarrar/releases/tag/v1.5.2

### 1.16 إصدار v1.5.4 / v1.5.5 — formal astronomy naming (2026-11-11)

- **CI #43033274020** و**CI #43036577918** فشلا مع عشرات الأخطاء من نفس النوع:
  `Unresolved reference 'state'` في ملفات الشاشات — لأن استدعاءًا واحدًا
  لـ`use24Hour` وضعناه في نطاق لا يتوفر فيه `state`. **الإصلاح الكبير:**
  - في `TodayScreen`، تُمرر قِيمة `use24Hour: Boolean` صراحةً لكل الدوال
    الفرعية: `BottomArea` ← `PrayerStrip`، `computeArcPhase` ←
    `timeText`، `ArcDayCell` (مثلث)، DayCard → PrayerTimeCell.
  - في `WidgetsScreen`، معاينة الويدجت ليست بحاجة لـ`state`، فقيمة
    `use24Hour` ثابتة فيها (البطاقات للمعاينة فقط).
  - أيقونة «12/24» أُبدلت للـ`AccessTime` (لا يوجد `Icons.Filled.Schedule`).
- **التسمية الرسمية للمحرك الفلكي:** 0 أحرف مفردة؛ إعادة تسمية كاملة:
  `dsin → sineOfDegrees`، `dcos → cosineOfDegrees`، `darcsin → arcSineOfDegrees`،
  `darccos → arcCosineOfDegrees`، `darctan2 → arctangentOfDegrees`،
  `DTR → DEGREES_TO_RADIANS`، `RTD → RADIANS_TO_DEGREES`،
  `fixAngle → normalizeAngleInDegrees`، `fixHour → normalizeHourOfDay`،
  `getJulianDay → calculateJulianDay`،
  `getSolarPosition → calculateSolarPosition`،
  `getHourAngle → calculateHourAngle`،
  `getHijriDateSafe → calculateHijriDate`،
  `formatArcHour → formatHourOfDay`،
  `calculateDistance → greatCircleDistanceKm`،
  `haSunrise → hourAngleSunrise`،
  `jd / eqt / g / q / l / e / d → julianDay / equationOfTime / meanAnomaly /
  meanLongitude / trueLongitude / obliquityOfEcliptic / daysSinceJ2000`.
- **نتيجة تجريبية:** CI **نجاح** (#429/43036577918 dispatch على `2f09bc8`،
  والوسم `v1.5.5` على `e65162b`).
- **لا شيء اليوم على جهازك** — كل ما سبق نجاح/ناجح عبر CI.

### 1.17 إصدار v1.5.7 — Aligns versionCode/versionName (2026-11-11)

- **v1.5.6** كان موقّعًا بنية R8، لكن `versionName` كان `1.5.5` و`versionCode 10505`
  — تم ربطه خطيًا بإصدار v1.5.5 (statusurry: من وسم v1.5.6 لم يكن متطابقًا).
- **الإصلاح:** `versionCode = 10506` / `versionName = "1.5.7"` كي ينطق الإصدار
  تجاهل ngram راضي: وقت `aapt dump badging` يظهره على الجهاز شكل صحيح.
- **v1.5.7 (وسم على `7bbd040`):** CI #43043277379 **ناجح**؛
  APK موقّع 11,439,235 بايت، SHA‑256 (`17BCD6B9F442E24AB6DAC01717CD574A1E5AEFDAFB327933F93F9E06736BDF69`).
  التحقق: v2 scheme successful، CN=Nightjar. الحجم في الجدول `aapt badging` ⇒ `1.5.7` / `10506` / targetSdk37.
- **الوسم:** https://github.com/Nightjar-source/amsakkarrar/releases/tag/v1.5.7

### 1.18 إصدار v1.5.8 — مفاتيح الجنان + قارئ موحّد (2026-11-12)

- **مصدر:** ملف `C:\Users\kar\StudioProjects\MafateehAlJinan\shared\src\commonMain\composeResources\files\content_texts.json` (2.9MB — الفهرس الكامل للأدعية وأعمال الأيام والأشهر والأحداث المعروفة).
- **قاعدة المفاتيح الجنان أرقامها من:** `content_texts.json` (مفاتيح 5550 → 19168 مع حرف المضي — مزروعة في منبع المحتوى وتعمل offline بدون شبكة).
- **Models** مُعاد تسميتها: `JinanPart` / `JinanChapter` / `JinanSection` / `JinanItem` / `JinanContentTexts` / `JinanNormalizer` — مع إزالتها من `Mafateeh_folders` مباشرة (= حذف نفس الذاكرة المكتسمة من اسم الحزمة) لتفادي تصادم مع كامل الكود.
- **الوسيلة:** `LaunchedEffect(Unit) { state.prepareJinanContent() }` تُستدعى عند فتح «المفاتيح»؛ التشغيل مطلوب مرة واحدة فقط وثقته كل مرة سوء نادر.
- **المفاتيح:** 6 أيقونات حقيقية: القرآن `AutoMirrored.Filled.MenuBook`، المفاتيح `Icons.Filled.Key`، الأدعية `AutoMirrored.Filled.MenuBook`، المسبحة `Icons.Filled.CheckCircle`، أعمال اليوم `Icons.Filled.EventNote`، البوصلة `Icons.Filled.Mosque`.
- **القارئ الموحّد:** صفحة edge-to-edge بالكامل — الزر «X» + عنوان صغير + زر ⚙ صغير لقائمة تكبير/تصغير الخط وخلفية ورقية/داكنة. زر «رجوع» واحد.
- **القرآن والسماء والأدعية وقارئ الجنان** كلها يمكن أن تمر بملف قارئ واحد فقط — القرار هنا للوظائف المحلية؛ الآن يستطيع الجنان استدعاء نفسه مباشرة (مؤشر onReadJinanItem من KeyScreen).
- **التحقق أمان CSS للصوت:** في مرحلة (متعمدة) لم يُعرف اسم الجزء أولًا؛ ثم قدمته على أنه AsyncLazy معطى عند الطلب (فتح القارئ = طلب مرة واحدة).
- **نقاط مكملة غير منجزة في هذه الجولة:** محرك Rammomentي المنشأ لأرقام الوضع العربي (أرقام عربية الهندية ٠-٩)، أفضل تنسيق القراءة بصريًا (Adaptive FontSize), مزامنة bookmark! إنها لم تُكتب بعد ولذلك ببساطة.

---

## 8. قواعد تحديث هذا الملف

- حدّث «الحالة الآن» و«الخطوة التالية» بعد كل مهمة منجزة.
- أضف سطرًا في سجل تقدم الخطة، ولا تحذف التاريخ.
- لا تضع هنا تفاصيل طويلة؛ التفاصيل في الخطة الرئيسية.
