# ملف السياق — اقرأني أولًا قبل أي عمل

> **الغرض:** إذا انتهى سياق النموذج أو بدأت جلسة جديدة، هذا الملف يعيدك إلى نقطة
> العمل الحالية في دقيقة واحدة. الخطة الكاملة في `docs/MASTER-PLAN-amsak3d.md`.

---

## 1. الحالة الآن

| البند | القيمة |
| --- | --- |
| الفرع | `main` |
| آخر التزام كود | `47ce44a` — المرحلة 5 (توحيد الصوت + قفل التشغيل + تنظيف الإشعارات) |
| المرحلة | **المرحلة 5** مكتملة برمجيًا، بانتظار Gate جهاز |
| المرحلة التالية | **المرحلة 6 — البيانات والموقع والتوقيت الصيفي والطقس** |
| الخطة | مكتملة ومحفوظة |
| الجهاز | لا جهاز/محاكي متصل |
| الإصدار الأخير | `v1.1.0` — `versionCode 10100` |

---

## 2. اقرأ بهذا الترتيب

1. `AGENTS.md` — قواعد المشروع.
2. `docs/MASTER-PLAN-amsak3d.md` — الخطة الكاملة وبنك الأهداف.
3. هذا الملف — حالة اللحظة.
4. `appkarv/amsak3d.tsx` — المرجع البصري (لا تعدّله).

---

## 3. الخطوة التالية فورًا — المرحلة 6

1. **المدن العربية أوفلاين:** مراجعة `cities.json` وأسماء المدن العراقية
   والمحافظات، والتأكد من عدم وجود أسماء مكررة أو مشوّهة.
2. **DST لكل تاريخ:** إصلاح `offsetHours` في `PrayerCalculator` ليحسب التوقيت
   الصيفي/الشتوي للتاريخ المطلوب لا للحظة الحالية، مع خيار ذكي/صيفي/شتوي.
3. **الموقع:** نافذة صلاحية نظام، كشف تعطيل الموقع مع زر تشغيل، وبحث
   GPS/IP/مدن أوفلاين.
4. **الطقس:** ربط API خفيف وتأثيرات خلف القوس بلا وميض، مع تراجع آمن بلا إنترنت.
5. **Gate 6**: لندن والعراق صحيحتان، والموقع يعمل.

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

**بوابات معلّقة:** المراحل 1–5 تحتاج اختبارًا بصريًا على جهاز/محاكي
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

---

## 7. القيود الحالية

- لا جهاز/محاكي: لا يمكن التحقق الفعلي من الودجت/الإشعار/الصوت/Edge-to-edge
  ولا بوابات 320/600/840dp.
- iOS غير قابل للبناء على Windows (كود `AVAudioPlayerDelegate` غير مترجم بعد).
- الصوت: مسار المصدر موحّد ومُتحقَّق من وجود الملفات في APK، لكن السماع الفعلي
  على جهاز حقيقي لم يُختبر بعد.
- `offsetHours` يستخدم `Clock.System.now()` لكل التواريخ؛ DST لكل تاريخ في
  المرحلة 6.

---

## 8. قواعد تحديث هذا الملف

- حدّث «الحالة الآن» و«الخطوة التالية» بعد كل مهمة منجزة.
- أضف سطرًا في سجل تقدم الخطة، ولا تحذف التاريخ.
- لا تضع هنا تفاصيل طويلة؛ التفاصيل في الخطة الرئيسية.
