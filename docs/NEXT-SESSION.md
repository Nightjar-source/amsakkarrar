# ملف السياق — اقرأني أولًا قبل أي عمل

> **الغرض:** إذا انتهى سياق النموذج أو بدأت جلسة جديدة، هذا الملف يعيدك إلى نقطة
> العمل الحالية في دقيقة واحدة. الخطة الكاملة في `docs/MASTER-PLAN-amsak3d.md`.

---

## 1. الحالة الآن

| البند | القيمة |
| --- | --- |
| الفرع | `main` |
| آخر التزام كود | `v1.2.0` — المرحلة 6 + تدقيق الموارد والإشعارات |
| المرحلة | **المرحلة 6** مكتملة برمجيًا، بانتظار Gate جهاز |
| المرحلة التالية | **المرحلة 7 — الشاشة الافتتاحية والأيقونة** |
| الخطة | مكتملة ومحفوظة |
| الجهاز | لا جهاز/محاكي متصل |
| الإصدار الأخير | `v1.2.0` — `versionCode 10200` (بناء GitHub Actions) |
| تعديلات معلّقة | R8 + توحيد الإشعار الحي + إعادة تصميم الودجتات (غير ملتزمة بعد) |

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

---

## 8. قواعد تحديث هذا الملف

- حدّث «الحالة الآن» و«الخطوة التالية» بعد كل مهمة منجزة.
- أضف سطرًا في سجل تقدم الخطة، ولا تحذف التاريخ.
- لا تضع هنا تفاصيل طويلة؛ التفاصيل في الخطة الرئيسية.
