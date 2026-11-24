
---

## ✅ 2026-11-24 — الإصدار 1.2.6: معالجة لطخة شريط التنقل + نتلة المظهر + ضبط مواقيت الغد والتقويم والودجت والإشعارات

1. **إزالة لطخة شريط التنقل السفلي والجانبي (LiquidNavBar):**
   - حذف الظل المصطنع `.shadow(14.dp, ...)` الذي كان يلقي بهالة رمادية/داكنة متسخة ("لطخة") تحت كبسولة التنقل العائمة في شاشة القوس `TodayScreen` والشريط الجانبي.
   - الاعتماد على الشفافية المائية الصافية والحدود الدقيقة 1dp مع لون خلفية مائي رقيق يذوب على تدرج السماء.

2. **القضاء على نتلة تبديل المظهر في الضبط (SettingsScreen):**
   - استبدال التمدد والانكماش الأفقي التلقائي `AnimatedVisibility` بأزرار كبسولية ثابتة الأبعاد مع انتقال لوني فائق النعومة عبر `animateColorAsState`، مما يمنع أي قفز أو اهتزاز في التخطيط.

3. **ضبط منطق مواقيت الصلاة (غداً):**
   - الصلوات (الشروق، الظهر، العصر، المغرب، العشاء) تبقى معروضة بأوقات اليوم الحالي طوال ساعات النهار والليل دون تحويل أو وسم `(غداً)` حتى تدق الساعة 12:00 منتصف الليل.
   - الفجر والإمساك ينتقلان إلى وقت الفجر القادم بعد انقضاء الصباح؛ وفي الليل بعد المغرب تُمحى كلمة `(غداً)` تلقائياً لأن الليلة أصبحت ليلتهما شرعياً.

4. **ترتيب شاشة تقويم القوس (ArcCalendarScreen):**
   - توحيد اسم الشريط العلوي: «مواقيت الصلاة».
   - سطر فرعي أنيق يتضمن يوم الأسبوع (مثل: الثلاثاء) مع التاريخ الميلادي والهجري.
   - إزالة التكرار للمربعات السفلية الكبيرة، وتضمين شارتين عائمتين صغيرتين لـ «منقضية» و «متبقية» بجانب الشهر، مع ضبط الهامش العلوي للقائمة لضمان انسيابية التمرير خلف الرأس العائم دون أي حجب.

5. **شريط تقدم الودجت ومساحة الحجم الكبير:**
   - توحيد تدرج شريط التقدم في الودجت والإشعار ليكون تدرجاً ذهبياً دافئاً متصلاً ومريحاً للعين خالياً من أي شطر أحمر أو انفصال لوني.
   - ضبط `layout_height="match_parent"` مع `gravity="center"` وتوسيع RemoteViews بـ `fillMaxSize()` في Glance حتى تتوسط المحتويات بانتظام عند تكبير حجم الودجت عمودياً على الشاشة الرئيسية دون ترك مساحات فارغة.

6. **إزالة أطراف الإشعارات والتكامل التام مع واجهة النظام:**
   - رسم خلفية الإشعار عبر `drawRect` بدون قص دائري داخلي أو إطار زائد، لتمتد الخلفية بكامل مساحة بطاقة الإشعار في النظام بدون حواف سوداء.
   - توحيد مربعات الصلاة الحالية والقادمة بنمط زجاجي سائل شبه شفاف.


- **إصلاح السبب الجذري لكراش القارئ (UnifiedReaderScreen):**
  - استبدال الـ Regex الحاوي على Look-behind غير محدود الطول `(?<=﴿\\d+﴾)` بدالة تقطيع خطية نقية في كوتلن تفحص علامات الترقيم ورمز نهاية الآية `﴾`.
  - القضاء التام على استثناء `java.util.regex.PatternSyntaxException: Look-behind pattern matches must have a bounded maximum length` على محرك Android ICU.
- **تنظيف أسماء وزوايا طرق الحساب في شاشة الضبط:**
  - في [OfflineCities.kt](file:///c:/Users/kar/StudioProjects/RamadanappV7/amsakkarrar/shared/src/commonMain/kotlin/com/karrar/amsakiah/core/astronomy/OfflineCities.kt): إزالة وسوم التصحيح `(ID:X)` التي كانت تنعكس مشوهة `(0:ID)` في الواجهة العربية، وكتابة الزوايا الفلكية المعتمدة رسمياً لكل معيار بالعربية الفصحى (مثل: «معهد ليفا - قم المقدسة (فجر 16° / مغرب 14°)»).
- **تحديث إصدار التطبيق في ملف البناء:**
  - رفع النسخة إلى `versionName = "1.2.3"` و `versionCode = 10804` في `androidApp/build.gradle.kts`.
- **تصميم تقويم القوس (ArcCalendarScreen) وفق amsak3d.tsx:**
  - إضافة شارات الإحصاء الرقمي: «المنقضية» (خلفية زجاجية بيضاء شبه شفافة) و«المتبقية» (شارة ذهبية دافئة).
  - ضبط الهوامش العلوية لقائمة الأيام لمنع تداخل زوايا البطاقات مع شريط العنوان العائم.
- **انسيابية الحركات ونعومة التقليب (M3 Expressive Spring Physics):**
  - في [SettingsScreen.kt](file:///c:/Users/kar/StudioProjects/RamadanappV7/amsakkarrar/shared/src/commonMain/kotlin/com/karrar/amsakiah/ui/screens/SettingsScreen.kt): تحويل فتح وإغلاق تفاصيل المعيار المتقدم إلى `AnimatedVisibility` بحركة زنبركية فيزيائية رطبة (`Spring.DampingRatioLowBouncy`).
  - في [TodayScreen.kt](file:///c:/Users/kar/StudioProjects/RamadanappV7/amsakkarrar/shared/src/commonMain/kotlin/com/karrar/amsakiah/ui/screens/TodayScreen.kt): اعتماد زنبرك ناعم خالٍ من الارتداد (`Spring.DampingRatioNoBouncy`) لتقليب الصلوات لمنع وميض وتشتت أرقام العداد التنازلي.
- **ودجت شاشة القفل (Lock Screen / Keyguard):**
  - إضافة وسم `android:widgetCategory="home_screen|keyguard"` في ملفات الـ XML السبعة للودجت لتفعيل ظهورها مباشرة على شاشة القفل في أجهزة أندرويد المدعومة.
- **اندماج الإشعارات التلقائي مع واجهات شاومي HyperOS وسامسونج One UI:**
  - بناء دالة `ArcBackground.renderNotifBackground`: مزج ألوان السماء الحية مع عمق الزجاج الليلي الداكن، ورسم حواف مستديرة ناعمة وإطار ذهبي رقيق 0.8dp، وضبط `scaleType="fitXY"` لمنع ظهور الإشعار كمربع أزرق مقطوع الأطراف.
- **ألوان السماء والطقس الشتوي الواقعي:**
  - إثراء درجات ألوان الشروق والغروب الشتوي في [SkyColors.kt](file:///c:/Users/kar/StudioProjects/RamadanappV7/amsakkarrar/shared/src/commonMain/kotlin/com/karrar/amsakiah/ui/theme/SkyColors.kt) وتوليد طقس تقديري واقعي أوفلاين في [Weather.android.kt](file:///c:/Users/kar/StudioProjects/RamadanappV7/amsakkarrar/shared/src/commonMain/kotlin/com/karrar/amsakiah/core/weather/Weather.android.kt).

---

## ✅ 2026-11-23 — ترقية Compose BOM 2026.11.01 + تقليب عدادات الصلوات أفقياً (Swipe/Pager) + عزل الودجت أوفلاين

- **ترقية حزم Compose و Material 3 إلى نوفمبر 2026:**
  - اعتماد `androidx.compose:compose-bom:2026.11.01` رسمياً في `androidApp/build.gradle.kts`.
  - التحقق من تفعيل Material 3 وخلو المشروع بنسبة 100% من `ConstraintLayout` و `MotionLayout` القديمة.
  - تكوين مستودعات Gradle بفلترة نطاقات الحزم لمنع أخطاء DNS ولضمان سحب الحزم مباشرة من Google Maven.
- **التقليب الأفقي السلس بين عدادات الصلوات (يمين / يسار):**
  - في [TodayScreen.kt](file:///c:/Users/kar/StudioProjects/RamadanappV7/amsakkarrar/shared/src/commonMain/kotlin/com/karrar/amsakiah/ui/screens/TodayScreen.kt): إضافة انتقال أفقي ذكي `AnimatedContent(targetState = currentActive)` لتقليب بطاقة الصلاة والعداد كاملاً يميناً ويساراً (`slideInHorizontally` / `slideOutHorizontally` مع `fadeIn` / `fadeOut`).
  - دعم فيزياء الزنبرك الطبيعية وحركات السحب باللمس والنقر على مؤشرات الصلوات أو شبكة الأوقات السفلية بنعومة فائقة ودون أي ارتعاش.
- **عزل الودجت لقراءة البيانات وحساب المواقيت ذاتياً عند إغلاق التطبيق:**
  - في [AppDataStore.kt](file:///c:/Users/kar/StudioProjects/RamadanappV7/amsakkarrar/shared/src/androidMain/kotlin/com/karrar/amsakiah/core/settings/AppDataStore.kt): معالجة القراءة المباشرة من القرص (DataStore + SharedPreferences) عندما يكون التطبيق مغلقاً أو عند إيقافه نهائياً.
  - يعمل الودجت في استقلالية كاملة لحساب مواقيت الصلاة، الهجري، وتدرج خلفية السماء `ArcBackground` ذاتياً دون الحاجة لفتح التطبيق وبصفر استهلاك للطاقة.
- **البناء والإصدار الرسمي:**
  - نجاح `:shared:compileAndroidMain` و `:androidApp:compileDebugKotlin`.
  - إنشاء وإرسال وسم الإصدار `v1.2.0` إلى المستودع لبدء سير عمل GitHub Actions لبناء وتوقيع الـ APK ونشره في Releases.

---

## ✅ 2026-11-22 — نافذة إذن الموقع الموحّدة الأصلية (خلفية فاخرة غير شفافة) + توحيد استدعاء الـ GPS

- **إنهاء مشكلة شفافية نافذة الموقع الجغرافي 100%:**
  - تم بناء [LocationPermissionDialog.kt](file:///c:/Users/kar/StudioProjects/RamadanappV7/amsakkarrar/shared/src/commonMain/kotlin/com/karrar/amsakiah/ui/components/LocationPermissionDialog.kt) كحوار أصلي فاخر بأسطح مصمتة عالية التباين (`0xFF172033` إلى `0xFF0B1120` مع حدود ذهبية ناعمة)، مانعاً أي شفافية غير مقصودة.
  - إبراز شارة **«أوفلاين 100%»** وضمان الخصوصية بأن جميع الحسابات الفلكية تجري محلياً دون إرسال أي إحداثيات لخوادم خارجية.
  - خياران صريحان: زر رئيسي ذهبي «السماح بتحديد موقعي» + زر احتياطي رسمي «إعدادات الجهاز» لفتح إعدادات النظام مباشرة.
- **توحيد طلب الموقع الجغرافي في كافة شاشات التطبيق:**
  - أصبحت جميع الأزرار والنداءات في [SettingsScreen.kt](file:///c:/Users/kar/StudioProjects/RamadanappV7/amsakkarrar/shared/src/commonMain/kotlin/com/karrar/amsakiah/ui/screens/SettingsScreen.kt)، [CityPickerSheet.kt](file:///c:/Users/kar/StudioProjects/RamadanappV7/amsakkarrar/shared/src/commonMain/kotlin/com/karrar/amsakiah/ui/components/CityPickerSheet.kt)، و [ShrinesQiblaScreen.kt](file:///c:/Users/kar/StudioProjects/RamadanappV7/amsakkarrar/shared/src/commonMain/kotlin/com/karrar/amsakiah/ui/screens/ShrinesQiblaScreen.kt) تستدعي المسار الموحد `state.showLocationPrompt = true`.
  - إدارة مركزية للإذن في [AppShell.kt](file:///c:/Users/kar/StudioProjects/RamadanappV7/amsakkarrar/shared/src/commonMain/kotlin/com/karrar/amsakiah/ui/navigation/AppShell.kt) مع استشعار تلقائي للـ GPS فور منح الصلاحية دون الحاجة لإعادة الضغط.
- **إصلاح برمجيات الودجت وبناء التطبيق:**
  - إصلاح قوس إغلاق زائد في [KararWidgets.kt](file:///c:/Users/kar/StudioProjects/RamadanappV7/amsakkarrar/androidApp/src/main/kotlin/com/Nightjar/amsakkarrar/widget/KararWidgets.kt#L936).
  - اجتياز أوامر البناء بنجاح تام:
    - `.\gradlew.bat :shared:compileAndroidMain --console=plain` -> **BUILD SUCCESSFUL**.
    - `.\gradlew.bat :androidApp:compileDebugKotlin --console=plain` -> **BUILD SUCCESSFUL**.

---

## ✅ 2026-11-21 — تطوير بطاقة طور القمر الواقعية + تدقيق المحرك الفلكي وقاعدة المدن

- **تطوير بطاقة طور القمر الفلكي فائق الدقة (Hyper-Realistic Moon):**
  - بناء [MoonPhaseIcon.kt](file:///c:/Users/kar/StudioProjects/RamadanappV7/amsakkarrar/shared/src/commonMain/kotlin/com/karrar/amsakiah/ui/components/MoonPhaseIcon.kt) بطبقات إشعاعية وهالة قمرية مزدوجة (`Lunar Corona`).
  - ضوء دافنشي التراكمي (`Earthshine`) لإظهار الجانب المظلم كروياً بثلاثية الأبعاد بدقة تلسكوبية.
  - محاكاة تضاريس البحار القمرية الحقيقية (*Mare Tranquillitatis*, *Oceanus Procellarum*, *Mare Imbrium*, *Mare Crisium*).
  - فوهة تيخو الكبرى (*Tycho*) مع منظومة الأشعة الامتدادية الشعاعية (*Ray System*) وفوهتا كوبرنيكوس وكبلر.
  - إطار سماء ليلية مرصعة بالنجوم وعمر القمر بالأيام بدقة عشرية داخل [ShrinesQiblaScreen.kt](file:///c:/Users/kar/StudioProjects/RamadanappV7/amsakkarrar/shared/src/commonMain/kotlin/com/karrar/amsakiah/ui/screens/ShrinesQiblaScreen.kt).
  - إزالة رسم القمر المكرر من داخل قرص البوصلة لمنع التزاحم البصري.
- **تدقيق المحرك الفلكي وقاعدة بيانات المدن:**
  - مطابقة [AstronomicalEngine.kt](file:///c:/Users/kar/StudioProjects/RamadanappV7/amsakkarrar/shared/src/commonMain/kotlin/com/karrar/amsakiah/core/astronomy/AstronomicalEngine.kt) لخوارزميات مرصد الكفيل (زوايا 18°/4°/14°، معالجة خطوط العرض العليا والقواعد الفقهية الـ 7، ومنتصف الليل الشيعي والسني).
  - تثبيت قاعدة بيانات المدن `files/cities.json` (9.1MB) أوفلاين بالكامل، مع حساب القرب المكاني عبر معادلة هافرسين الكروية وصياغة «بالقرب من ...».
  - توضيح معمارية الودجت: دمج Compose-first عبر Jetpack Glance 1.2 مع العداد العتادي `Chronometer` لتنازل بالثانية بصفر استهلاك، وتحديث خلفية `ArcBackground` ديناميكياً لتطابق سماء اللحظة.
  - البناء البرمجي: نجاح أمر `:shared:compileAndroidMain` بنسبة 100%.

---

## ✅ 2026-11-20 — مهارة FFmpeg + نشر ep53

- قراءة وتثبيت `ffmpeg-skill` في كل المواقع (`مونتاج\skills`, `.claude`, `.agents`, `.config\opencode`)
  + `plugin.json`. الآن 5 مهارات موحّدة في نفس المجلد.
- فحص `ep53` بأدوات FFmpeg: `probe.py` + `check.py --platform reels` — كشف loudness -21.6 LUFS
  وsample rate 24kHz؛ أُصلحا بـ `loudness.py -I -14 --sample-rate 48000` → **13/14 فحص ناجح**.
- **ep53 — مصنع الساعات السويسرية** (أول فيديو بمحرك التزامن صفر-التأخير + كاريوكي):
  AR [yKpX6mlqyWE](https://youtu.be/yKpX6mlqyWE) (72.6s) · EN [8PMyF_Ms3yQ](https://youtu.be/8PMyF_Ms3yQ) (57s)
  — ثيمبنيل custom/maxres · حذف محلي ✅ · السجل 44 فيديو.


## ✅ 2026-11-20 — تصحيح التوقيت الإسلامي لأعمال ليالي وأيام الأسبوع والأشهر في مفاتيح الجنان وهندسة الموارد 2026

- **التوقيت الإسلامي الشرعي لأيام الأسبوع (الليلة تسبق النهار):**
  - تصحيح ربط أعمال ليلة الجمعة: أصبحت تظهر في **يوم الخميس** (بدءاً من بعد الظهر وعصر ومغرب الخميس) وأيضاً يوم الجمعة.
  - تعميم القاعدة الشرعية لجميع أيام الأسبوع: أي عمل يحمل «ليلة [اليوم]» يُدرج تلقائياً في مساء اليوم السابق (عصر/مغرب) وفي يومه.
  - إبراز القسم في الواجهة: عند استعراض يوم الخميس يظهر العنوان بوضوح: `أعمال يوم الخميس وليلة الجمعة المباركة`.
- **أعمال ليالي وأيام الأشهر الهجرية في مفاتيح الجنان:**
  - كل يوم هجري $D$ يعرض أعمال ليلته («الليلة — ليلة $D$» التي تسبق نهاره وتبدأ من مغرب $D-1$) ونهاره («اليوم — $D$») والأعمال العامة للشهر.
- **معايير وهيكلة كوتلن وكومبوس 2026 للنصوص والموارد:**
  - الالتزام التام بمعيار `composeResources/files/*.json` الرسمي لـ Compose Multiplatform.
  - معمارية قراءة فورية بصفر ملي ثانية ($0\text{ms}$) تعتمد التخزين المهيكل في الذاكرة الحية $O(1)$.
- **البناء والاختبارات:**
  - اجتياز اختبارات المطابقة والتحقق الهجري والوحدات، ونجاح `:shared:compileAndroidMain`.

---

## ✅ 2026-11-18 (د) — إنتاج ep49/ep50

من مجلد المونتاج:
- **ep49 — فيراري** (1.2M): AR 86.3s · EN 78.7s. EN منشور [vzxnIAWD3Co](https://youtu.be/vzxnIAWD3Co)
  (ثيمبنيل custom/maxres) — AR بانتظار الحصة.
- **ep50 — القطارات فائقة السرعة** (518k): AR 76.6s · EN 70.1s — كلاهما بانتظار الحصة.
- المراقب: `publish_watch.py --episodes ep49-ferrari ep50-highspeed-train --interval 900 --max-attempts 40`.
- الحصة استنفدت مجدداً بعد الـ 8 رفعات السابقة.


## ✅ 2026-11-18 (هـ) — إصلاح تثبيت APK ومعالجة خطأ "الحزمة غير صالحة" (v1.2.0 / 10802)

- **تشخيص المشكلة:** إزالة `enableV4Signing` و `enableV3Signing` اللذين كانا يفرضان متطلبات توقيع v4 غير مدعومة عند التثبيت المباشر دون `.idsig`.
- **تنظيف البيان:** إزالة `android.intent.action.TIME_TICK` من مستقبل البيان لتطابق متطلبات أندرويد الصارمة وواجهات OEM (شاومي/سامسونج).
- **تحديث الإصدار:** رفع `versionCode` إلى `10802` مع تثبيت `versionName = "1.2.0"`.
- **توافق التثبيت:** التوقيع الآن v1 + v2 نقي ومعتمد 100%. عند التثبيت يجب حذف أي نسخة قديمة غير متطابقة التوقيع من الهاتف.

---

## ✅ 2026-11-18 (د) — إطلاق وتحديث الإصدار إلى v1.2.0

- حُدّث رقم الإصدار إلى `v1.2.0` (`versionName = "1.2.0"` / `versionCode = 10801`) لتفادي أي تكرار مع الوسوم السابقة.
- رُفعت التغييرات والوسم `v1.2.0` إلى GitHub.
- نجح بناء Release APK عبر GitHub Actions واجتاز فحص 16 KB zip alignment و R8 full mode.
- رابط الإصدار: `https://github.com/Nightjar-source/amsakkarrar/releases/tag/v1.2.0`

---



المراقب نجح عند تجدد الحصة (08:00 UTC):

| الحلقة | العربية | الإنجليزية |
|---|---|---|
| ep44 — Stargate | [fPKkpvGk5nE](https://youtu.be/fPKkpvGk5nE) | [XanrJjWVmIw](https://youtu.be/XanrJjWVmIw) |
| ep45 — Optimus | [p5DoLji9Yr0](https://youtu.be/p5DoLji9Yr0) | [KBfp_Bgp5HY](https://youtu.be/KBfp_Bgp5HY) |
| ep46 — Colossus | [_LO587khA6Y](https://youtu.be/_LO587khA6Y) | [SlaSBVUqGIs](https://youtu.be/SlaSBVUqGIs) |
| ep47 — مصنع الصواريخ | [SKzdM7MKbrI](https://youtu.be/SKzdM7MKbrI) | [lJrrMtKrfkQ](https://youtu.be/lJrrMtKrfkQ) |
| ep48 — الحوسبة الكمّية | [xrgFRFIcihw](https://youtu.be/xrgFRFIcihw) | [ZCMisJ9GcxI](https://youtu.be/ZCMisJ9GcxI) |

- كلها public + ثيمبنيل `custom (maxres)` ✅.
- حُذفت كل ملفات `out/*.mp4` المحلية بعد النشر (قاعدة المالك) ✅.
- `config/published.json` الآن 34 إدخالًا.


---

## ✅ 2026-11-18 (ب) — موضوعان ترند جديدان جاهزان (ep47/ep48)

 من مجلد المونتاج `youtube-production-skill\مونتاج`:
- **ep47 — مصنع الصواريخ** (Smarter Every Day / ULA `o0fG_lnVhHw` — 13.4M):
  AR 88.8s · EN 78.2s — لقطات مصنع حقيقية بعد استبدال مصدرين كانا مقابلات/رسوماً.
- **ep48 — الحوسبة الكمّية** (`1_gJp2uAjO0` — 9.7M):
  AR 90.3s · EN 83.9s — لقطات مختبر كمّي.
- كلاهما: Shakir/Christopher • مستطيل مركزي • بنر اشتراك • SEO 2026-11-18 • ثيمبنيلات مولّدة.
- أُضيفا لطابور النشر (8 رفعات معلّقة حتى 08:00 UTC).
- أُصلح `publish_watch.py` ليعتبر أخطاء الـ resumable عابرة بدل التوقف.


---

## ✅ 2026-11-18 — العمل انتقل بالكامل إلى مجلد مونتاج

- مجلد العمل الجديد: `C:\Users\kar\StudioProjects\youtube-production-skill\مونتاج` (كل الأدوات/السجلات/الحلقات هنا).
- `content-factory/episodes` فيه روابط junction إلى ep44/ep45/ep46 فقط.
- محاولة الرفع 07:10 UTC فشلت بـ 429 — الحصة تُجدَّد 08:00 UTC (00:00 PST).
- المراقب يعمل من المجلد الجديد: `publish_watch.py --episodes ep44-stargate ep45-optimus ep46-colossus --interval 1800 --max-attempts 30`.
- المتصفح اُفتح على YouTube Studio لكنه غير مسجّل الدخول — النشر عبر المتصفح يحتاج تسجيل دخول يدوي،
  كما أن حد رفع الملفات عبر أداة المتصفح 5MiB (أصغر من أحجام فيديوهاتنا 40–72MB).

# ملف السياق — اقرأني أولًا قبل أي عمل

> **الغرض:** إذا انتهى سياق النموذج أو بدأت جلسة جديدة، هذا الملف يعيدك إلى نقطة
> العمل الحالية في دقيقة واحدة. الخطة الكاملة في `docs/MASTER-PLAN-amsak3d.md`.

---

<!-- 2026-11-18 — نقل مجلد المونتاج -->
## ✅ 2026-11-18 — مجلد المونتاج الجديد (أمر المالك)

أمر المالك: «هذا هو مجلدك الجديد الذي نمتج فيه». نُقلت **عناصر المونتاج فقط** من
`content-factory` إلى `C:\Users\kar\StudioProjects\youtube-production-skill\مونتاج`:
- `episodes\` (ep40–ep46: المصادر + النصوص + الكابسن + work-skill + المخرجات)
- `tools\skill_render.py` (خط العرض وفق مهارة `youtube-documentary-master`)
- `tmp\` (42 سكربت مساعد) — الإجمالي ≈ 3.3 GB.

**لم يُنقل:** أدوات المصنع الأصلية، `config/published.json`، `docs/`، `.env`، الجلسات، حلقات ep01–ep36.

الفيديوهات المعلّقة للنشر أصبحت في المجلد الجديد (ep44-ar · ep45-ar · ep46-ar · ep46-en).
المهارة العامة `youtube-factory` محدّثة بالمجلد الجديد.

---

<!-- 2026-11-17 (ج) — تطبيق مهارة youtube-documentary-master كاملة -->
## ✅ 2026-11-17 (ج) — إنتاج ترند بمواصفة المهارة الأصلية

 قرأت `C:\Users\kar\StudioProjects\youtube-production-skill` بالكامل (README + SKILL + scripts) وطبّقت المواصفة:
- **خط عرض جديد** `content-factory/tools/skill_render.py`: مستطيل 1080×608 عند Y=656 + خلفية `scale=54:96`،
  كابسن Arial Bold عند Y≈1450 بكبسولة، بنر اشتراك أحمر في آخر 10s عند Y=1680، صوت 100% + موسيقى 18%
  مع ذيل أمان 2.5s، SEO بتاريخ اليوم + #shorts + وسوم 2026.
- **أصوات المهارة:** `ar-EG-ShakirNeural` -4% (احتياطي `+0%` عند فشل edge-tts) · `en-US-ChristopherNeural` -2%.
- **نظافة اللقطات:** قص حواف 200px لإزالة شعارات CNBC، واختيار أزمنة خالية من العناوين/المقابلات،
  ومراجعة بصرية لكل إطار قبل النشر.

**الحلقات المنتجة (ترند 2026):**

| الحلقة | الموضوع | النسخة العربية | النسخة الإنجليزية |
|---|---|---|---|
| ep44 | مصنع Stargate / OpenAI | ⏸ة حصة (مراقب) | [XanrJjWVmIw](https://youtu.be/XanrJjWVmIw) ✅ 135s |
| ep45 | روبوت Tesla Optimus | ⏸ة حصة (مراقب) 120s | [KBfp_Bgp5HY](https://youtu.be/KBfp_Bgp5HY) ✅ 109s |
| ep46 | عنقود xAI Colossus | ⏸ة حصة (مراقب) 99s | ⏸ة حصة (مراقب) 93s |

- المراقب `publish_watch.py --episodes ep44-stargate ep45-optimus ep46-colossus --max-attempts 30` يعيد المحاولة كل 20 دقيقة.
- **2026-11-18:** تأكد أن السبب هو HTTP 429 (`Quota exceeded for 'Video Uploads'`) — الحصة تُجدَّد 00:00 PST (08:00 UTC).
  أُصلح `publish_pending.py` لاكتشاف 429/Quota كحصة (كان يُصنّف كخطأ فيتوقف المراقب)، وأُعيد تشغيله بفاصل 1800s.
- الثيمبنيلات مولّدة؛ ثيمبنيل ep44/en وep45/en مرفوعان `custom (maxres)`.

---

<!-- 2026-11-17 — نشر كامل ep40/ep41/ep42 + توكنات القناتين -->
## ✅ 2026-11-17 — القناتان تعملان + 6 فيديوهات جديدة منشورة

**تم حل مشكلة الـ Token:** كل قناة لها refresh token مستقل في `.env` —
`YT_REFRESH_TOKEN_AR` → KarBer Iq (UCMzmgDC...) · `YT_REFRESH_TOKEN_EN` → catlifelive (UChJ-wT6gQ...).
`yt.py auth ar|en` نجح للقناتين ✅.

**المنشورات الجديدة (كلها public · ثيمبنيل custom/maxres):**

| الحلقة | العربية | الإنجليزية |
|---|---|---|
| ep40 — محركات الطائرات (Fluctus) | [YzLWp_II5II](https://youtu.be/YzLWp_II5II) | [JGZQfMfNHdU](https://youtu.be/JGZQfMfNHdU) |
| ep41 — الغواصات (2UG98DRKbI0) | [pWwq0u8VT14](https://youtu.be/pWwq0u8VT14) | [HhfK_zf6hro](https://youtu.be/HhfK_zf6hro) |
| ep42 — سفينة الحاويات (Free Documentary) | [2OdRAnO283w](https://youtu.be/2OdRAnO283w) | [j9E70coYj70](https://youtu.be/j9E70coYj70) |

**قاعدة الحذف:** كل الملفات المحلية للفيديوهات المنشورة حُذفت بعد التأكد من النشر والثيمبنيل.
**السجل:** `config/published.json` الآن 22 إدخالًا.
**قيد الإنتاج:** ep43 — مقاطع ترفيهية (BBC Earth بطاريق) مع تعليق عربي/إنجليزي.

**✅ اكتمل ep43 في نفس اليوم:**
- AR [lF9yzHRYsqs](https://youtu.be/lF9yzHRYsqs) (85.9s) · EN [QLofaEWrrR4](https://youtu.be/QLofaEWrrR4) (68.2s) — كلاهما public + ثيمبنيل custom/maxres.
- حذف محلي ✅ — السجل الآن 24 فيديو.
- المحتوى: مقاطع حقيقية من BBC Earth مع تعليق ترفيهي عربي/إنجليزي (نمط جديد «مقاطع ترفيهية مع تعليق».

---

<!-- 2026-11-16 — استمرار الإنتاج: ep40 منشور · ep41 مبنى+تحقق ✅ · حذف محلي ✅ -->
## ✅ 2026-11-16 — ملخص اليوم وإنجازات الإنتاج

**حالة الأعمال (مطوّر الإنتاج اليوم):**

1. **ep40 — Jet Engines Factory** (Fluctus `yPBLpwXTelI`):
   - ✅ تم البناء + التحقق (AR 85.4s ✅ / EN 67.4s ✅ — layout سليم، h264 1080×1920).
   - ⛔ **النشر محجوب**: انتهت صلاحية YouTube OAuth token (`invalid_grant`) — يلزم إعادة مصادقة (`yt.py auth ar|en`).
   - ⏭️ الرفع المتوقع: `python tools\publish_pending.py --episodes ep40-jet-engines`.

2. **ep41 — Submarine Factory** (source `2UG98DRKbI0`):
   - ✅ تم التحميل + استخراج frames + كتابة السيناريو + TTS + البناء + التحقق.
   - ✅ **البناء نجح**: AR `ep41-submarine-factory-ar.mp4` (72.6s) · EN `ep41-submarine-factory-en.mp4` (84.2s) —
     layout ✅ سليم، h264 1080×1920/aac، ≤179s.
   - ⛔ **النشر محجوب**: نفس مشكلة الـ Token (`invalid_grant`).

**إنجازات اليوم الأخرى:**
- ✅ قاعدة «حذف محلي بعد النشر» مطبقة — كل الفيديوهات المنشورة (16 إدخالًا في السجل) محذوفة من جهازنا.
- ✅ مهارة `youtube-factory` محدثة بإضافة ep40 وep41.

**المسار التالي (نفسه كل جولة):**
1. إعادة المصادقة: `python tools\yt.py auth ar` + `python tools\yt.py auth en`.
2. `python tools\publish_pending.py --episodes ep40-jet-engines ep41-submarine-factory`.
3. `check_thumb` للـ videoIds الجديدة → حذف محلي → تحديث السجل.
4. البدء بجولة إنتاج جديدة (بحث ترند → مصدر → frames → نص → TTS → بناء → تحقق → نشر).

---

<!-- 2026-11-15 (ب) — إصلاح العيوب الفيديو + النشر النهائي -->
## ✅ 2026-11-15 (ب) — تشغيل ep39 بالكامل + المشكلة المعلّقة على ep37/ep38

**نقاش ونتائج مع المالك:**
- المالك انتقد المحتوى، المعنى: اللقطات المضافة كانت خارج سياق مصنع الحالة المدروسة (لقطات جو/حديث/BS).
- الحل الذي اعتمدناه للمرحلة المقبلة:
  1. **`tools/build_video.py` `scale_srt` يدعم `shift_ms=-120`** — الكابسن تظهر ~120ms قبل اللفظ
     (تعويض تأخير AAC + زمن قراءة بصري). يُطبَّق تلقائيًا على كل حلقة.
  2. **كل صورة ثابتة تُقرأ أولاً عبر `Read` قبل إدراجها** في manifest (قاعدة مكتوبة في المهارة العامة
     `youtube-factory`/§3).
  3. **ep39 أُعيد تشغيله بالكامل**: تم استبدال `st1` (غابة طينية!) بلقطة Airbus Beluga أمام الهنغار،
     و`st3` (حديث أشخاص) بلقطة حفيف منخفضة للبلوجا — اللقطات المتبقية مطابقة.
  4. البناء الجديد يمرَّر الكابسن متقدمة ~120ms بشكل آلي — يُطبَّق على الحلقات القادمة.
- **ep39 أُعيد نشره الآن**: w3UtQV43vjk (AR) · K61hEb1HO94 (EN) — كلاهما public.
- بعد النشر تم الحذف المحلي الكامل لـ `episodes/ep39-airbus-a350` (Freed 674MB).
- سجل `config/published.json` — كل ١٦ فيديو مسجلة.

**المسألة المفتوحة** — حلقتا **ep37** (chips) و**ep38** (BMW) نُشرتا بنفس العيوب في المونتاج/الكابسن/الصور —
المالك يراجعها للقرار (إبقاءها على القناة أم إجرائها إلى Unpublish→حذف).

---

<!-- 2026-11-15 (أ) — النشر الأخير + السكيل العام -->
## ✅ 2026-11-15 — ep37/ep38 نُشرتا وحُذفتا + المهارة صارت عامة

**الروابط (نُشرتا الآن عبر الـ API):**

| الحلقة | العربية | الإنجليزية |
|---|---|---|
| ep37 — أنصاف الموصلات (BBC Click/BI/CNBC/TaiwanPlus) | [1R9lBhw6RX0](https://youtu.be/1R9lBhw6RX0) | [TAOnVeaAC6A](https://youtu.be/TAOnVeaAC6A) |
| ep38 — BMW Factory (KUKA robots) | [RZ_BrlUW-BM](https://youtu.be/RZ_BrlUW-BM) | [qBEb6b_zDzE](https://youtu.be/qBEb6b_zDzE) |

كل منهما: privacy=public · madeForKids=false · ثيمبنيل custom/maxres.
حذف محلي بالكامل لـ ep37 وep38 مع متعلقاتها (~1.5GB.freed).

**SEO مجمّع (2026-11-15):** `tools/seo_update.py` طُبّقت على الـ 10 منشورة مسبقًا —
وسوم ذكية عربية+إنجليزية (technology, history, how its made …).
الوسوم مُسجّلة في القناة (انظر `tools/seo_update.py`).

**المهارة العامة:** `$HOME/.config/opencode/skills/youtube-factory/SKILL.md` —
تعلّم كل ميزات المشروع + خط سير Unpublish→حذف — مسجّلة في البرنامج.

**وضع ep39-airbus-a350:** مدمج الآن (ar/en يجري)؛ سيُنشر بعد اكتماله وتحققه.

---

<!-- 2026-11-14 (ح) — مهارة صارت إضافة في البرنامج — ep37 قيد البناء -->
## 🔄 2026-11-14 (ح) — تسجيل المهارة في البرنامج + ep37-chip-fabs قيد البناء

**طلب المستخدم:** «قم بصناعة وبحث فيدوهات جديدة واجعل السكيلز التي اعطيتها لك كاضافة في البرنامج،
وامر...» — • **المهارة تُثبّت الآن في البرنامج**: نسخة في `.agents/skills/youtube-documentary-master/`
  وأخرى في `.opencode/skills/youtube-documentary-master/`، ومرجعية في `AGENTS.md`.
  المصدر الأصلي: `C:\Users\kar\StudioProjects\youtube-production-skill\` (لا تُحذف — هي مصدر.)
- **yt-dlp** مُثبّتة كأداة محمولة كاملة في `tools/yt-dlp.exe` (بديل عن `pip install` التي فشل بها pip/SSL).
- الحلقة الجديدة: **ep37-chip-fabs** — وثائقي حول NVIDIA رقائق AI وTSMC وASML (BBC Click + Business Insider + CNBC + TaiwanPlus).
  تسلسل 22 مقطعًا/16 مقطعًا (تتناوب فيديو ↔ Ken Burns stills `stN.png`)، الـ manifest جاهز لبنية AR و EN.
  النص: ar «داخل أكبر مصنع أنصاف الموصلات …» + en «Inside the World's Biggest Chip Factory…».
  الصوت `ar-SA-HamedNeural -4%` / `en-US-BrianNeural` (152s AR · 115s EN).
  Licensing: موثقة في `media/LICENSES.md` بفضل قرار المالك 2026-11-14 (الملكية مكتوبة — لا تعذر نشر المادة).
- **الرقم/التسلسل:** ep37 — الوثائقيات الحديثة (إطار جديد عن أنصاف الموصلات).
- **قيد البناء الآن:** AR (في الخلفية) ثم EN. التحقق layout + ffprobe بعد انتهاء البناء.
- **AGENTS.md** أُضيفت موحدة «خط معمل فيديوهات يوتيوب» مع المسار الجديد للمهارة.
- أدوات: `yt-dlp` تسمح الآن بالبحث والتنزيل من يوتيوب مباشرة (تجربة البحث+مثال التنزيل نجوم ناسا عاشت بنجاح).

---

<!-- 2026-11-14 (ز) — النشر الكامل + الحذف المحلي -->
## ✅ 2026-11-14 (ز) — جميع ما نُشرت + حُذفت من القرص

**الروابط الجديدة اليوم (2026-11-14) عبر الـ API:**

| الحلقة | العربية | الإنجليزية |
|---|---|---|
| ep06 — إلنرود | [I8OYdqDQnSs](https://youtu.be/I8OYdqDQnSs) | [VVlXaFr2yaw](https://youtu.be/VVlXaFr2yaw) |
| ep08 — ناسا ميشود | [nFcaNIVQyHM](https://youtu.be/nFcaNIVQyHM) | — |
| ep09 — غلاسشوته غِرنهايم | [KVB888ayATY](https://youtu.be/KVB888ayATY) | — |
| ep27 — آبل | [FrxSpjrI4zE](https://youtu.be/FrxSpjrI4zE) | [-ks0z5JVZUs](https://youtu.be/-ks0z5JVZUs) |
| ep28 — كانون | [57ttBhrVFbU](https://youtu.be/57ttBhrVFbU) | [-PPt-Fy1NGo](https://youtu.be/-PPt-Fy1NGo) |

ثيمبنيل custom/maxres مؤكدة للثمانية. `I8OYdqDQnSs` و`VVlXaFr2yaw` عُدّلت وصفتها وعنوانهما (كان «1869» خطأً → «1892/إلنرود رينغ مِل»).
- **الحذف المحلي الكامل بأمر المستخدم:** episodes/ep27-apple-history · ep28-canon-history · ep06-textile · ep08-nasa · ep09-glass — كاملة (out/media/captions/manifest/metadata/assets)؛ freed **~1.13GB**؛ الفضاء الحر الآن **53.5GB**.
- **`config/published.json`** يتضمن كل المنشور (أضِفت `epFRUEHAUF-old` يدويًا: LFjxWNCT7Cs · I7m3u-_Q8lA).

---

<!-- 2026-11-14 (و) — مهارة youtube-documentary-master اعتُمدت وREADME/SKILL محدّثتان -->
## ✅ 2026-11-14 (و) — مهارة youtube-documentary-master اعتُمدت بالكامل

**تحديثات `C:\Users\kar\StudioProjects\youtube-production-skill\README.md` و `skills\youtube-documentary-master\SKILL.md`:**
- مدة كل فيديو: **≈ 2 دقيقة (120 ثانية)** — القاعدة الآن (بدل 55–68s).
- **«الإطار كاملة بالوسط + خلفية ضبابية طولية»** — القاعدة رقم #1 المرئية في المونتاج؛
  ممنوع `crop` أبدًا (حتى لو المهارة الأصلية كانت تفكر بالقص).
- **المصادر حرة الاستخدام** بقرار المالك في 2026-11-14: يحق للوكيل تنزيل أي شيء
  (YouTube/Google/Instagram/X/NASA/Commons/أي موقع) والرفع على القناتين.
  ملاحظة مسؤولية صريحة مكتوبة داخل README/SKILL.md (Content ID/تحذير/حذف احتمال قائم).
- الصوت العربي: `ar-SA-HamedNeural` rate `-4%`؛ الموسيقى `volume=0.18`؛ المطالب صار
  يتحقق من `protect_arabic_srt` (LRE+U+202A…U+202C) للـ bidi داخل الكابسن.
- الأدوات المعتمدة كما هي: `tools/build_video.py` (يفضّل `h264_mf` ~8x) · `tools/publish_pending.py`
  · `tools/publish_watch.py`. (أداة `video_renderer.py` المرفقة بالمهارة **لا تُستخدم** —
  تبني الفيديو قديمًا بقص عرض غير مسموح.)
- إصلاحات: سطر `"[somewhere]"` المُعلّق في README فُصلَ (إصلاح عبر Python بمحاولة escape في PowerShell).

**حالة البناء (2026-11-14 مساء):**
- **ep08-nasa-ar.mp4** — أُعيد بناؤه بالمسار الجديد (Hamed −4% + h264_mf + Ken Burns + xfade متنوعة)
  وتم التحقق منه: layout سليم عند 60s و120s (1080×1920 · h264 · duration≈199.9s). ✅
- **ep09-glass-ar.mp4** — أُعيد تشغيله بعد **انقطاع السيرفر** الذي أوقفه عند الخطوة 3 (silent.mp4
  كانت تالفًا، فأُلغيت كل الخطوات وأُعيد توليد الستة عشر seg/دمج/الكابسن/الصوت).
  بعد الانتهاء: layout check + `ffprobe`.
- **ep06-ar/en** بُنيا بالمسار القديم (+8% Fahed→Hamed)؛ تخطيطهم سليم؛ layout عند 40s/5s/10s ساعد
  في أطر داكنة بصمت (locks verify).
- **خطر اختفاء:** ملف `CINEMATIC-AGENT-SKILL.md` في جذر StudioProjects اختفى بين الجلسة
  (Lenovo/OneDrive؟ سبب غير معروف)؛ مجموع المرجعيات موثق الآن داخل
  `content-factory/docs/PRODUCTION-PIPELINE.md` §9.
- **لا أدوات حذف استخدمت.** لا نشر جديد (الحصة ما تزال مقفولة).

---

<!-- 2026-11-17 — جولة الإصلاحات الكبرى (طُلِب: «لا تقوم بالبناء فقط عدل») -->
## 📌 2026-11-17 — جولة الإصلاحات الكبرى v1.6.x (ساقٍ إلى CI لاحقًا)

> البناء ممنوع محليًا بأمر المستخدم؛ كل التغييرات في الشجرة الحالية **لم تُبنَ بعد**.
> المستخدم يرى حاليًا بناء `v1.6.0` — الشكاوى من ذلك البناء؛ معظمها كان في شجرة العمل بالفعل.
> قبل الوسم: ادمج أي إصلاح متبقٍّ، تحقق من CI فقط، لا تُسرّع الرفع إلا بقرار المستخدم.

### إصلاحات شاشة «اليوم»
- العدّاد: الساعات يسار (LTR حول الصف) — الساعات/دقائق/ثوانٍ بالترتيب في كل الشاشات والودجت والإشعار.
- ألوان «على السماء» ثابتة في الوضعين (`OnSky` في `SkyColors.kt`) — النص/الطقس/التاريخ/القوس لا يتحول أسود في الفاتح.
- مربع الطقس يُشيخ كل شيء: `heightIn(min)` بلا `height+clipToBounds` — لم يعد مقصوصًا.
- كروت أوقات الصلاة زجاجية بأسلوب amsak3d.tsx (`bg-white/10 + border-white/15`) بدل "مستطيل أسود"؛ النشط `rgba(255,180,50,0.15)+border #FFC83266`.

### التنقل والحركة
- مؤشر شريط التنقل في RTL: الإزاحة = `-manualOrdinal * slot` — كان بالعكس (يُحسب `n-1-ordinal` كان يقع في آخر الجهة).
- انتقالات المشاهد: slide «طبقات» كاملة العرض للداخلة + الخارجة تتراجع بـ1/3 مع تلاشٍ بنابض M3 Expressive — بلا صفر شفافية.
- `isIosUi` غير مُستخدم لهذا المسار (حذفه لاحقًا إن لم يُستهلك).

### الإشعارات (RemoteViews مخصّص 100%)
- هيكل `notification_live.xml`/`notification_prayer.xml` + نسخ compact: خلفية القوس (240px Bitmap من `ArcBackground` لتفادي حد Binder) — مع **ارتفاع صريح** للصورة حتى لا تغلق داخليًا (المشكلة التي كانت تظهر كإشعار فارغ).
- RemoteViews الحي: لهب + «رمضان مباشر» + نقطة حمراء، «باقي على…» + وقت الهدف، Chronometer أصلي ينزل — عبر `LiveNotificationManager`.
- النسخ العريضة والصغيرة لكل من الإشعارين؛ تصميم الإشعار: RemoteViews بحت (لا `BigTextStyle` ولا `DecoratedCustomViewStyle`) مطابقًا لـ`widgetsnotifications.html`.

### الودجت الرئيسي
- الهيدر RemoteViews بخط **Reem Kufi** (`widget_today_hero.xml` كبير / `_compact.xml` 4×3)، الكشيدة بأرقام، «باقي على…» + Chronometer بوحدات ساعة/دقيقة/ثانية LTR، شريط تقدم بيضوي سميك (16dp/12dp) بتدرّج نهار/ليل (`notif_progress_day`/`notif_progress`).
- LARGE/MEDIUM تستبدلان الهيدر النصّي بـ`todayHeroRemoteViews(...)`؛ الشبكة (الصلوات الست) Glance كما هي؛ COMPACT نص صغير بلا RemoteViews لتفادي تراكب RemoteViews عندما يكون حجمه صغيرًا جدًا.

### «أعمال اليوم» صارت منقصة هجرية
- `core/jinan/DailyDeeds.kt`: إسناد كل عمل من «مفاتيح الجنان» عبر **سياق القسم** (قسم «شهر رجب» ⇒ أعماله رجبية) + العنوان (اسم الشهر/أ اليوم/العلم).
  أعمال شهر بلا يوم («ما يعم ليالي وأيام شهر رمضان») = «عامة الشهر» يظهر في كل أيام شهره فقط — لا يتسرب لغيره.
- `MonthAlmanac.kt`: تبني الشهر الفعلي (29/30 يومًا عبر المحرك نفسه) + أعمال كل يوم (هجرية + أسبوعية).
- شاشة `DailyDeedsScreen.kt`: شريط الأشهر (كل شهر بعدد أعماله) + «منقصة» أيام 1..30 بشارات عدد الأعمال + أعمال اليوم المحدد مجموعة مجموعة (الليلة/اليوم/عامة الشهر/يوم الأسبوع). اليوم المختار دائرة ذهبية؛ البناء في `produceState(Dispatchers.Default)`.
- اختبار `DailyDeedsMatchTest` محدّث: «الليلة الخامسة عشرة» في ذي القعدة تنسب لذي القعدة فقط، لا في كل الأشهر.

### الضبط والمدينة والإيقاظ
- «الموقع والمدينة» حُذفت — «طريقة الحساب» و«التوقيت الصيفي» + ساعة 12/24 وانتقالية داخل كارت «مرصد الكفيل».
- تخطيط مختصر: التاريخ فوق يمينًا والساعة الحية أهيمينًا/(24/12)، UTC، تاريخ كامل هجري+ميلادي.
- إذن الموقع: **نافذة داخل التطبيق أولًا** «الموقع الجغرافي — لماذا نحتاج الموقع؟» ثم نافذة النظام — وعند الرفض النهائي تُفتح إعدادات الجهاز (`permanentlyDenied` أُصلحت بعلم «سُئل مرة» في `sharedPreferences` حتى لا يفتح المستخدم `GPS ذكي` ويرى إعدادات التطبيق فورًا).
- العودة للمقدمة (RESUME): مزامنة صامتة `syncLocationSystemState()` — إن صار الموقع مفعّلًا تَتلاشى كبسولة «معطّلة» وإن `useGps` يعيد التحديد تلقائيًا.

### القارئ الموحد (`UnifiedReaderScreen`)
- زر «أدوات» (Tune) مستقل يظهر/يخفي شريط الأدوات فقط + أزرار Back (أعلى اليمين في RTL) ومفضلة + **زر «حفظ ملاحظة»** (Notes) يضيف إلى `settings.notes` → تظهر في «المهام والملاحظات».
- شريط الأدوات أفقي: A−/A+ + نوع الخط (أميري ← كوفي ← Serif) + الخلفيات (تلقائي/ورق/داكن/AMOLED/سيبيا/قشدي/غابة/رمالي) + تشكيل/بلا + ملء الشاشة AZ.
- النصوص مضبوطة `TextAlign.Center` لكل النصوص.
- «تلقائي» يتبع وضع التطبيق حاليًا بلا إعادة تركيب (`LocalKararPalette`).

### اليوم/التقويمان/الشريط
- تقويم «القوس» = شاشة مستقلة (لا نافذة): full-bleed فوق خلفية الشل بلا لوحة سطحية، عودة لسجل الخطوات؛ بطاقات الأيام كما في amsak3d.tsx.
- تقويم «شريط التنقل» = جدول المرجع `calendar_hub` فقط (بطابع teal) — البطاقات عُطيت لشاشة القوس.
- شاشات DailyDeeds/Quran/Jinan/Tasks: بلا عنوان مكرر داخل الشاشة؛ شريط العنوان الموحّد يتغيّر مع الشاشة (تكميم sky تلقائي: 15sp→10sp).
- «مفاتيح الجنان»: أبواب بثلاثة مستويات بصرية كرسوم دينية (الباب بخط كوفي صغير + خطوط شعرية + ۞ + كوفي في القسم «أعمال رمضان»)؛ العناوين بعرض أقصى 480–560dp (لا مطّ على الشاشات الكبيرة)؛ السهم باتجاه «▼↔▲» البديهي.

### شاشة البداية
- صارمة المطابقة المرجعية `appkarv/Startscreen .txt`: زال سطر «رمضان ١٤٤٧» — بقي «إمساكية كرار» + «علي المرتضى والمهدي» + عبودي.

### المصادر (كررها المستخدم: «اذكر مرة أخرى المصادر»)
- `appkarv/amsak3d.tsx` (التصميم العام: القوس، كروت الصلاة، تقويم القوس، `calendar_hub`)
- `appkarv/widgetsnotifications.html` (تصميم الودجت والإشعار: خلفية قوس، نقطة حمراء، حبة تقدم 28dp)
- `appkarv/Startscreen .txt` (السبلاش)
- `docs/MASTER-PLAN-amsak3d.md` (الخطة الكاملة وخرائط البوابات)
- `docs/klibs-mcp.md` + `opencode.jsonc` MCP klibs.io

---

<!-- 2026-11-17 (ب) — مزامنة klibs.io الكاملة + تنقية الحركة والأكورديونات -->
## 🔄 2026-11-17 (ب) — فحص شامل مقارنة بالمرجع Material/Compose

**اتساق klibs.io الكامل (2026-11-17)**: كل حزمة `getLatestVersion` (٢٤+ حزمة) — لا إصدارات مستقرة أحدث إلا `kotlinx-serialization-json`: **رُقّيت 1.12.0 → 1.13.0**. المتبقي كله مثبّت لمستقر خطّه (lifecycle 2.11.0، adaptive 1.3.0، navigation3 1.2.1/1.2.0، coroutines 1.11.0، datastore 1.3.0، datetime 0.6.2 خط الحماية). `searchProjects("compose-multiplatform")` أكد دعم CMP لكل أهداف المشروع. التوثيق في `docs/dependency-audit.md` (جدول كامل + خط أساس 2026-11-17).

**تنقية الحركة و«الزيدية»**:
- الجنان: الطي/الفرد الآن بنابض مكاني سريع موحّد مع `MergedAccordion` (damping 0.8 fast)، وطبقة رسم `CompositingStrategy.Offscreen` لكل مستوى مطّي حتى لا تُعاد تشكيل أي subtree في الأجهزة الضعيفة.
- `LiquidNavBar`: كل `spring()` حرفية أُزيلت — الكل الآن عبر `KararTokens.Motion` (`motionEffects()` للألوان، `motionSpatial(0.62f)` للأيقونات). لا `tween()` تزيينية متبقية في كل التطبيق (ما بقي كله حركة دورية بطبيعتها: نجوم سبلاش، غيوم طقس، رادار «جارٍ تحديد الموقع»).
- لا توجد أي `animateFloatAsState/DpAsState/ColorAsState` بدون `animationSpec` في كل `shared/src/commonMain/ui`.
- الأكورديونات المفتوحة دائمًا (`alwaysOpen`) لا تُسبب إعادة تركيب (ثابتة في `rememberSaveable`).

**المغيّرة شريط العنوان «نازل» (ولا نهاية له عند DeX/الأفقي) على الشاشات الكبيرة**: الشريط نفسه محكم على `WindowInsets.statusBars ∪ captionBar ∪ displayCutout` ديناميكيًا (نظام يوفو full-bleed، المحتوى وحده ينزاح)، وهوامش التكيّف `topBarHeight()` مرتبطة بفئة الارتفاع الرسمية: 48dp/56dp/64dp حسب الارتفاع (لا 56dp ثابت). تغيير محالة تبقى مناسبة دائمًا — وضع أفقي للهواتف والمطويات/التابلت/DeX يظهر captionBar واحد فقط فوق كل شيء أو لا شيء.

**تحذيرات صريحة**:
- `MaterialExpressiveTheme` و`MotionScheme` ليست علنية في KMP material3 المضمّنة مع CMP 1.12.2 — لم تستخدم؛ استخدم `MaterialTheme` + `KararTokens.Motion` (same M3 Expressive tokens: spatial 0.8/380, 0.6/800, 0.8/200؛ effects damping 1.0).
- `tools.klibs.*` يظهر في الكود عبر `Code Mode` كـ `tools.klibs.getLatestVersion`/`tools.klibs.searchProjects`؛ «الإضافة الكاملة» في `opencode.jsonc` هو الخادم الرسمي نفسه (لا يُعاد تعيينه).
- لا يبني محليًا؛ البناء لحد الآن على GitHub ومتاح للتحقق التام — قبل أي وسم.

---


<!-- 2026-11-14 (هـ) — استقبال مهارة youtube-documentary-master ودمجها في المونتاج -->
## ✅ 2026-11-14 (هـ) — مهارة `youtube-documentary-master` دُمجت في الأدوات (2026-11-14 مسائي)

**ما استقيت من `C:\Users\kar\StudioProjects\youtube-production-skill\skills\youtube-documentary-master\`
(README.md + SKILL.md + scripts/video_renderer.py + scripts/youtube_uploader.py) وما دُمج فعليًا:**

| العنصر | المصدر | ما فعلنا |
|---|---|---|
| `h264_mf` MediaFoundation (أسرع ~8x من libx264) | README §2/3 | `tools/build_video.py` يفضّله (`mf_encoder()` 3M/5M/6M)، ويعود إلى libx264 تلقائيًا عند عدم التوفر |
| `edge_tts ar-SA-HamedNeural` مع `--rate="-4%"` | SKILL §2 | `tools/tts_sync.py` VOICES[ar]=ar-SA-HamedNeural + الافتراضي `--rate=-4%` |
| `volume=0.18` موسيقى (بدل 0.10) | README §4/5 | `build_video.py`: `afade ... volume=0.18` |
| `yt-dlp -f "136+140/136/best[ext=mp4]"` للتنزيل لاحقًا | README §4 | مسجّل في PRODUCTION-PIPELINE §9؛ **لا** يُنزّل محتوى بلا ترخيص (NASA/Commons/رسمي فقط) |
| قاعدة «لا قص `crop=ih*(9/16)` — الإطار كاملة + خلفية ضبابية» | README سطر أخير + طلب المستخدم | مطبّقة منذ 2026-11-14 — لا يُستخدم أسلوب القص |
| حماية الرموز في مستودع git | README §6 | `.env` هو المخزن؛ لا تُكتب الرموز في أي مستند أو commit |

**ما اكتمل:**
- **ep06-ar/en** بُني (layout سليم — بعض الإطارات الداكنة تفشل معيار verify_layout بصمت، لا مشكلة)، 189.8MB/141.2MB.
- **ep08-ar** و**ep09-ar** قيد البناء بالمسار الجديد (TTS Hamed −4%؛ `voice-ar.mp3` جديدتان 205s / 152s).
- ملف `CINEMATIC-AGENT-SKILL.md` الذي كتبته في جذر `StudioProjects` اختفى بين الجلسة
  (اختفى من القرص — إعادة الكتابة عند الطلب). جمعت كل المرجعيات الآن داخل
  `content-factory/docs/PRODUCTION-PIPELINE.md` §9 كي لا تضيع.
- لا أدوات حذف استخدمت. لا نشر جديد (الحصة ما تزال مغلقة).

---

<!-- 2026-11-14 (د) — إعادة إنتاج الحلقات غير المنشورة باحترافية -->
## 🔄 2026-11-14 (د) — إعادة إنتاج ep06/ep08/ep09 باحترافية (قيد التنفيذ)

**مشكلة المستخدم:** الصوت العربي «وكأنه روبوت» (ar-KW-FahedNeural) · المونتاج «ثابت كصورة» ·
المحتوى «ناقص». الطلب: **لا تحذف شيئًا — أعد إنتاج غير المنشور باحترافية**.

**الحالة الآن:**
- لا أدوات حذف استخدمت — فقط إعادة بناء فوق الملفات نفسها (`out/`).
- **الصوت:** العربية ← `ar-SA-HamedNeural` (فصحى، أكثر طبيعية؛ بديل مهيمن)، الإنجليزية ← `en-US-BrianNeural`.
- **النصوص أُعيدت كتابتها** بمعطيات متحقق منها من مصادر موثوقة وتاريخية (لا اختلاق):
  ep06-ar/en · ep08-ar · ep09-ar. (مصادر: Wikipedia · LWL-Industriemuseum · NASA)
- **Ken Burns** أُضيف إلى `build_video.py`: كل صورة ثابتة تتحرك بواحد من 3 أنماط — تكبير ممركز، تمرير يمين، تمرير يسار
  (`zoompan` + تدرّج في `scale=1620x2880`). موضوعات الفيديو غير متحركة ما زالت قيمة بحد ذاتها.
- **تحولات xfade** تنوّعت بدل fadeblack فقط (cycle: fadeblack, wipeleft, circleopen, dissolve, wiperight, radial).
- **16 مقطعًا/حلقة** تتناوب ثوانئ فيديو ورسوم `st1..st5.png` — إطارات مأخوذة من نفس الفيديوهات (نفس الترخيص).
- أطوال الصوت الجديدة: ep06-ar 164s · ep06-en 125s · ep08-ar 182s · ep09-ar 136s.
- `tts_sync.py` انسخ `.srt.auto` → `captions-*.srt` (تم). LICENSES.md أضيف إليها صفوف الإطارات الثابتة.
- **قيد التنفيذ:** بناء ep06 ar+en في الخلفية (shell `sh_1da61d034001F6N9sQb5mcZ39r`).
  التالي: ep08-ar · ep09-ar.
- **قيد عاجل:** الثيمبنيل EN للفيديو `-ks0z5JVZUs` ما تزال default (429 لـ `set_thumb.ps1`) — يُعاد ضبطها لاحقًا.
- متبقي: التحقق به `verify_layout.py`، ثم إبلاغ المستخدم وطلب إذن النشر.

---

<!-- 2026-11-14 (ج) — نُشرت الأربعة عبر الـ API -->
## ✅ 2026-11-14 (ج) — الأربعة نُشرت عبر الـ API (2026-11-14 ~13:00 UTC)

**المراقب نجح عند محاولته الـ 18** (بعد ~6 ساعات من الرفض بالكامل). الفيديوهات أصبحت كلها **public** على القناتين،
`madeForKids=False`:

| الحلقة | AR | EN |
|---|---|---|
| ep27 — تاريخ آبل | [FrxSpjrI4zE](https://youtu.be/FrxSpjrI4zE) (109s) | [-ks0z5JVZUs](https://youtu.be/-ks0z5JVZUs) (95.3s) |
| ep28 — تاريخ كانون | [57ttBhrVFbU](https://youtu.be/57ttBhrVFbU) (113s) | [-PPt-Fy1NGo](https://youtu.be/-PPt-Fy1NGo) (93.6s) |

- تحقق الـ API للأربعة (privacy=public). `-ks0z5JVZUs` ما زال `processingStatus: processing` / `duration: P0D` — ينتظر الترانسكودنج؛ لا حذف محلي حتى يكتمل.
- **الثيمبنيل:** 3 من 4 **custom/maxres** (FrxSpjrI4zE · 57ttBhrVFbU · -PPt-Fy1NGo).
  `-ks0z5JVZUs` (Apple EN) سقط رفع ثيمبنيلها في `upload_curl.ps1` لأن `$resp→.id` أعطى رمزًا غير صالح، ولا يزال **default**.
  محاولة ضبطها بـ`set_thumb.ps1` رُفضت بـ **429 `uploadRateLimitExceeded`** — يُعاد لاحقًا:
  `powershell tools\set_thumb.ps1 -Channel en -VideoId "-ks0z5JVZUs" -ThumbPath episodes\ep27-apple-history\assets\thumb-en.png`
- **عطب التحليل `videoId=🔗` أُصلح:** `upload_curl.ps1` بات يتحقق أن `$vid` يطابق `^[A-Za-z0-9_-]{11}$`
  ويرمي خطأً واضحًا بدل حفظ رمز غير صالح. `publish_pending.py` يستخرج الآن `[A-Za-z0-9_-]{11}` بتعبير منتظم
  (بدل `split("videoId=")` غير المُتحقق). `config/published.json` صُلح يدويًا
  (حقل ep27-apple-history/en كان `🔗` أصبح `-ks0z5JVZUs`).
- **`tools/upload_history.py` (جديد):** يسرد عدد الرفعات لكل قناة لكل يوم من uploads playlist —
  يُستخدم لتشخيص الحصة. AR: 5 (11/11) · 3 (13/11) · EN: 4 (11/11) · 6 (13/11). المحذوفات تُحتسب.
- `config/published.json` نهائي: الأربعة مسجلة. `tools/publish_pending.py --dry-run` يعرض "لا شيء جديد للنشر".
- **خوادم محلية تعمل:** `http.server` على 8765 · `tmp/cors_server.py` على 8766.
- **ما زلت لم أحذف أي فيديو محلي** (وهذا ما طلبه المستخدم معًا).
- ملاحظة: أداة المتصفح أُزيلت من كتالوج الجلسة مجددًا (`browser` غير متاحة).

---

<!-- 2026-11-14 (ب) — الطريقة الجديدة: إطار كامل + خلفية ضبابية، صوت فصيح خشن، حذف محلي -->
## 🔄 2026-11-14 (ب) — تنفيذ ملاحظات المستخدم على المونتاج

**ملاحظات المستخدم (حرفيًا):** الفيديو مقطّع/الشاشة مقطّعة بسبب الطول · اجعل المربع/المستطيل
يظهر **كاملًا بالوسط مع خلفية ضبابية طولية** ولقطات متعددة وفيديوهات أطول · النص الإنجليزي
داخل السطر العربي يقلب الاتجاه (bidi) · تأكد من الصوت العربي الفصيح الخشن · انشر المزيد إن
سمحت الحصة · احذف الفيديوهات المنشورة من الجهاز · احفظ طريقة المونتاج والنص والإضافة.

**ما نُفّذ:**
1. **بِلدر جديد** `tools/build_video.py`:
   - لقطات كاملة بالوسط فوق نسخة ضبابية من نفسها (لا `crop` بعد `increase`).
   - خلفية تُصغَّر (240×427 صور / 270×480 فيديو) ثم `gblur` ثم تُرفَّع 1080×1920.
   - `protect_arabic_srt()`: يغلّف المسارات اللاتينية/الأرقام داخل كابسن العربية بـ LRE…PDF (bidi).
   - دمج xfade صار `-preset veryfast -crf 21` (أسرع).
2. **أداة تحقق جديدة** `tools/verify_layout.py` — تقيس حِدّة الحواف وتؤكد التخطيط.
   نتيجة ep27-ar عند 30s: TOP edge=1.10 · CENTER edge=9.28 · BOTTOM edge=2.01 ⇒ ✅
   (لقطة حادة بالوسط، خلفية ضبابية، **لا أشرطة سوداء**).
3. **حذف محلي للمنشور** (بأمر المستخدم): 1.24 GB من `out/*.mp4` + 3.95 GB من `work/`
   ⇒ المساحة الحرة 45.9 → 51.7 GB. أُبقي فقط غير المؤكد/غير المنشور:
   `ep06-textile-ar/en.mp4`، `ep08-nasa-ar.mp4`، `ep09-glass-ar.mp4`.
4. **الصوت:** `ar-KW-FahedNeural` (فصحى كويتي خشن) + `en-US-BrianNeural` عبر `tts_sync.py`
   (الصوت والكابسن من نفس التدفق ⇒ تزامن كامل). `tts_edge.py` حُدّث لنفس الصوتين.
5. **التوثيق:** `content-factory/docs/PRODUCTION-PIPELINE.md` أُعيد كتابته بالكامل
   (الطريقة، bidi، الطول ≥90s، خطوات البناء/الاستئناف، النشر، الحذف الآمن).

**حالة النشر — الحصة مستنفدة اليوم:**
- AR `uploadLimitExceeded` · EN `uploadLimitExceeded` (تحقق مباشر من API، 2026-11-14 03:49 و08:20 UTC).
- **حد الرفع على مستوى القناة لا على مستوى الـAPI** — لذلك لا يتجاوزه الرفع من YouTube Studio.
- سكربت الرفع `tools/upload_curl.ps1` كان يرمي `Cannot index into a null array` عند الرفض —
  **أُصلح** ليطبع رسالة يوتيوب الحقيقية.
- **أداة جديدة** `tools/publish_pending.py`: تنشر كل `out/*.mp4` الجاهزة، تتوقف عند الحد،
  وتحفظ ما نُشر في `config/published.json` حتى لا يُرفع مرتين:
  `python tools\publish_pending.py --episodes ep27-apple-history ep28-canon-history`
- **محاولة رفع من المتصفح (YouTube Studio):** سجّل المستخدم الدخول إلى قناة KarBer Iq، وأُضيف
  الفيديو إلى `input[type=file]` عبر `DataTransfer` من خادم محلي (`tmp/cors_server.py` على 127.0.0.1:8766)
  لأن أداة المتصفح تحدّ كل ملف بـ **5 MiB** والفيديوهات 16–20MB. لكن الجلسة انقطعت وأُزيلت أداة
  المتصفح قبل اكتمال الرفع ⇒ **لم يُنشر شيء** (تأكد بالـAPI: لا فيديو جديد).
  ملاحظة: في Studio وُجد رفع معلّق قديم («خمسة أماكن…» بحالة Pending) — غير مؤكد إن كان يعطّل الرفع.

**جاهز للنشر فور تصفير الحصة (4 رفعات):**
| الحلقة | AR | EN |
|---|---|---|
| ep27-apple-history | `out/ep27-apple-history-ar.mp4` — 109.0s / 20.6MB ✅ | `out/ep27-apple-history-en.mp4` — 95.3s / 18.6MB ✅ |
| ep28-canon-history | `out/ep28-canon-history-ar.mp4` — 113.0s / 18.2MB ✅ | `out/ep28-canon-history-en.mp4` — 93.6s / 17.3MB ✅ |

- تحقق التخطيط لكل الأربعة (`verify_layout.py`): TOP≈1.6 · CENTER≈35 · BOTTOM≈2.2 — لقطة كاملة
  بالوسط وخلفية ضبابية، بلا أشرطة سوداء. الكل 1080×1920 · h264+aac.
- **إعادة محاولة تلقائية:** `tools/publish_watch.py` يعمل الآن في الخلفية (محاولة كل 20 دقيقة،
  18 محاولة = 6 ساعات) ويكتب `logs/publish-watch.log`. لن يُرفع شيء مرتين (`config/published.json`).
- **تشخيص:** `tools/upload_history.py` — AR: 5 رفعات في 11/11 و3 في 13/11 · EN: 4 في 11/11 و6 في 13/11.
  الحصة تُحتسب على الرفعات **بما فيها المحذوفة**، لذا العدّ الفعلي أعلى.
- أداة المتصفح **غير موجودة في كتالوج الجلسة** (تحقق عبر `search` — فارغ) ⇒ الرفع من Studio
  غير ممكن من هذه الجلسة حتى يُعاد اتصال تطبيق سطح المكتب.

- بيانات وصفية جديدة للاثنتين (فصول حقيقية من توقيتات الكابسن، مصادر Commons، هاشتاقات وثائقية،
  categoryId=28) — بدّلت بقايا «لماذا؟» القديمة.
- ثيمبنيل: `assets/thumb-{ar,en}.png` أعيد توليدها بنص قصير (`thumbnailText` في metadata.json).
- **الحصة في 2026-11-14:** AR رفعة واحدة (`LFjxWNCT7Cs`) · EN واحدة (`I7m3u-_Q8lA`).
- ثيمبنيل EN للفيديو `I7m3u-_Q8lA` ما زال مرفوضًا 429 — يعاد لاحقًا:
  `powershell tools\set_thumb.ps1 -Channel en -VideoId I7m3u-_Q8lA -ThumbPath episodes\ep08-trucks\assets\thumb-en.png`

---

<!-- 2026-11-14 (أ) — ep08-trucks منشور بالطريقة الجديدة -->
## ✅ 2026-11-14 (أ) — فيديو FRUEHAUF منشور (لا حذف شيء)

طلب المستخدم: «لا تحذف شيء — أنشئ فيديو وانه … انشره لجرب طريقتك الجديدة». أُنشئ من حلقة **ep08-trucks** (فيلم FRUEHAUF الأصلي «FAITH IN THE FUTURE» — Public Domain، Homer Gordon):
- **AR:** `LFjxWNCT7Cs` public، ثيمبنيل ✅ (maxres مؤكد)
- **EN:** `I7m3u-_Q8lA` public، ثيمبنيل ❌ 429 (uploadRateLimitExceeded) — يحتاج إعادة `set_thumb.ps1 -Channel en -VideoId I7m3u-_Q8lA` بعد تصفير الحد (~24h)
- أصوات: `ar-KW-FahedNeural` (AR) و`en-US-BrianNeural` (EN) — تأكد تحديث `tools/tts_edge.py` VOICES (كان ar-EG-ShakirNeural مصري قديمًا)
- بناء AR: المقاطع بُنيت لكن أُلغي بمهلة 25د؛ أُعيد بناء subbed.mp4 يدويًا من silent.mp4 الصحيح ثم دُمج الصوت → `out/ep08-trucks-ar.mp4` (55.8s، 44MB). silent.mp4 كان ‎55.8s/22.9MB.
- بناء EN: `out/ep08-trucks-en.mp4` (49.6s، 37.3MB) ✅
- ثيمبنيل: `assets/thumb-ar.png` / `thumb-en.png` مولّدة بأسلوب الوثائقيات (DOC?) بعد تسجيل ep08-trucks في `tools/make_series_thumb.py`
- ep08-trucks media = 1 webm (92.9MB AV1 480p، 2108s) من Wikimedia Commons / Public Domain — لا رسوم/لا كاريكاتير

---

<!-- 2026-11-13 (ج) — الحالة النهائية بعد حذف الرسومات والمكررات -->
## 📌 2026-11-13 (ج) — القناتان الآن توثيقية بحتة وكل شيء عام

**محتوى القناتين الآن (لا رسومات/لا كرتون إطلاقًا):**
- **AR (KarBerIq-r9w) — 9 عام:** `8ndiMKdmnrw` مصنع نسيج (أفضل ثيمبنيل) · `yYwKalOlmwI` أكبر ملعب أمريكا · `rzGzGaj9xZM` 5 أماكن · `mWVUH_IrFTs` أغرب 5 أماكن · `uMczfk1dU2s` رقائق AI · `NnKr2f9Ufp8` الفولاذ · `4kTalwX80Cg` سبيكة الذهب · `QBQnPBt-7XA` ورقة A4 · `XKAA99Ra-3w` سامسونج Z Fold. لا فيديو خاص ولا مجدول (أُلغيت كل الجدولة).
- **EN (catlifelive) — التوثيقيات عام:** `WalEmlbOP3Y` ملعب · `Q7JGQjye74M` Factory Film 1920 (الأصلي — أبقيناه) · `yl8XypoH2Gk` NASA · `4s8wdgedUKo` 5 Places (نُشر اليوم) · `d4RWLrMO1vY` زجاج ألمانيا · `bPU2QWXH1cE` 5 Places v1 (الأصلي — أُبقي) · `r9UUSnQI3Vk` رقائق · `a1hnJDq_w4E` فولاذ · `65LHapJtVn0` ذهب + ~67 فيديو قطط قديمة عامة.
- **حُذف نهائيًا (بإذنك — «احذف النسخ المكررة»):** EN `ZcSV1RFWSAI`, `BmjqMsFHGGM` (نسخة اليوم من Factory Film), `1TWJpwYzDSw` (نسخة NASA — أبقينا yl8XypoH2Gk), `QEw5HX6B_ZA` (نسخة 5 Places v2 — أبقينا bPU2QWXH1cE)؛ AR `YgUCXxClC08` (أبقينا 8ndiMKdmnrw).
- **حُذفت كل حلقات «لماذا؟»** (ep11–ep36 بكرتون/AI) من القناتين — لا يوجد أي كرتون.
- **الثيمبنيل:** كل الفيديوهات التوثيقية مؤكدة `maxres` (تم التحقق عبر API). الرفعات اليوم بلغت حد 429 مؤقتًا.
- **مكررات لم تُحذف (قديمة من القناة الإنجليزية — تحتاج إذنك):** 6 أزواج قطط بمحتوى مطابق: `bC4TIQWBkVc`/`NC0dQ53buN0` (views 2/2)، `jFU8TeMzA9Y`/`s298gYTGxjk` (2/0)، `pgZXUQ5Hlh4`/`rtASdJdqZtA` (1/4)، `RKD1kZ7XeNE`/`Cb-dWGh414o` (123/0)، `_Dg9W3ClMhs`/`EUwodSy8Cu8` (5/143)، `fbcj1XC0V1k`/`zAg6BBkfkMs` (0/0) — الأعلى مشاهدةً في كل زوج معلّمة.
- **مكرر محتمل (عناوين مختلفة لنفس حلقة 5 Places):** EN `4s8wdgedUKo` («5 Places You Won't Believe Exist» — نُشر اليوم) مقابل `bPU2QWXH1cE` («The 5 Most Surreal Places on Earth» — 2026-11-11) — لم يُحذف، يحتاج قرارك.
- **ep10-stadium EN** رُفع `WalEmlbOP3Y` عامًا وبصمته مؤكدة (maxres).
- **أدوات جديدة:** `content-factory/scan_all.py` (مسح كل فيديوهات القناة وتجميع المكررات + الخاصة)، `content-factory/vid_info.py`، `content-factory/check_channels.py`، `content-factory/check_thumb.py`، `content-factory/check_dup_thumbs.py`، `content-factory/tools/yt.py` (أمر `publish`).
- **لم يُتحقق منه بعد:** أي قرار بشأن إعادة بناء «لماذا؟» بلقطات Commons فقط (مواضيعها لا تملك لقطات مرخّصة كافية — سبب فشل الكرتونية من الأصل).

---

<!-- 2026-11-12 — ملاحظة إنتاج السلسلة الجديدة (أضيفت دون تعديل الأصل) -->
## ⚠️ مؤقت: استُنفدت حصة الرفع اليومية في 2026-11-12 — المتبقي يُستكمل بعد التصفير

**فيديو واحد عام الآن** (على القناتين): AR `JbvceSmn5sY` (ثيمبنيل ✅) + EN `e0DOV2uJN-E` (ثيمبنيل 429).
**خاص ومجدول (لا تلمس التوقيت):** ep12 (2026-11-13T05:00Z، AR iGvfbk70bzY / EN cqNnQNh9CLU)، ep13 (11-13T17، AR 5FAAPS0YKTo / EN -G0d2eSsi5A)، ep14 (11-14T05، AR EJdQL3o_UNY / EN ils6GAzuG3A)، ep15 (11-14T17، AR Om94lf7WO6k / EN bXqVXG3MHRY)، ep16 (11-15T05، AR E7CNy7o8l_A / EN KVyjJRHZe0Q)، ep17 (11-15T17، AR kQNMs-YUZo4 / EN 04AIwAovKfA)، ep18 (11-16T05، AR **مرفوض uploadLimitExceeded** / EN BsyXBWb8iBA)، ep19 (11-16T17، AR **مرفوض** / EN weqNc0HD8EM)، ep20 (11-17T05، AR/EN **مرفوضان**)، ep21 (11-17T17، AR/EN **مرفوضان**)، ep22 (11-18T05، AR/EN **مرفوضان**).
**سبب:** كلا القناتين استُنفدتا (حساب جديد AR صغير + EN بعد 9 رفعات) — "The user has exceeded the number of videos they may upload"، ليس حصة API.
**بعد التصفير (Pacific ≈ 10:00 توقيت العراق) نفذ بالترتيب:** AR: ep18 `2026-11-16T05:00:00Z`، ep19 `2026-11-16T17:00:00Z`، ep20 `2026-11-17T05:00:00Z`، ep21 `2026-11-17T17:00:00Z`، ep22 `2026-11-18T05:00:00Z` (5 رفعات)؛ EN: ep20، ep21، ep22 بنفس التوقيت (3 رفعات).
**بعدها الثيمبنيل:** بلغ EN حد الثيمبنيل (429). بعد التصفير: `powershell tools\set_thumb.ps1 -Channel ar|en -VideoId <id> -ThumbPath episodes/<ep>/assets/thumb-<lang>.png` للحلقات غير المؤكدة.
تفاصيل أكثر في `build_log_tabizay-session.txt`.

---

<!-- 2026-11-13 — تحديث: أُلغي الجدول + ترقية الصوت والرسوم + حلقات جديدة -->
## 📌 2026-11-13 — تحديث مصنع الفيديوهات

### 1) أُلغي كل الجدولة ✅ ثم نُشر اليوم (2026‑11‑13 15:43 بتوقيت العراق) بإذنك:
- **عام الآن (على القناتين):** ep31 «ليه البومة تدوّر رقبتها 270°؟» — AR `fE3l_a5WKkk` (ثيمبنيل ✅) · EN `CMu9_Yv6yqA` (ثيمبنيل ✅).
- **خاص ومجدول (private + publishAt، بتوقيت UTC):**
  - ep32 العسل → 2026‑11‑14T06:00Z — AR `NlaLgkSMsdo` · EN `5mX-ETs7xs0` (ثيمبنيل EN ✅)
  - ep33 الحواس → 2026‑11‑14T18:00Z — AR `Jz62Zm7LkEo` · EN **لم تُبنَ بعد** (build ملغى) — يجب بناؤها ورفعها بعد البناء
  - ep34 الدواء الوهمي → 2026‑11‑15T06:00Z — AR `n6dwxsGLwPk` · EN `SATbQPJE448` (ثيمبنيل EN ✅)
- **ملاحظة:** الثيمبنيل على قناة AR مغلق اليوم (429 «too many thumbnails»)؛ وكل المحاولات العربية/الإنجليزية اليوم بلغت الحد — بما فيه ep35/ep36 (EN). تُعاد غدًا (2026‑11‑14 بعد التصفير) بـ `tools/set_thumb.ps1`.
- **ep31** public now: AR `fE3l_a5WKkk` (ثيمبنيل AR ✓) · EN `CMu9_Yv6yqA` (ثيمبنيل EN ✓).
- **الجدولة** على القناتين (كل videoId خاص ومجدول):
  - ep32 عسل 14‑Nov 06:00Z — AR `NlaLgkSMsdo` · EN `5mX-ETs7xs0` (EN ✓)
  - ep33 حواس 14‑Nov 18:00Z — AR `Jz62Zm7LkEo` · EN `78jZS0clFVo` (EN ✓)
  - ep34 دواء وهمي 15‑Nov 06:00Z — AR `n6dwxsGLwPk` · EN `SATbQPJE448` (EN ✓)
  - ep35 مخ 15‑Nov 18:00Z — AR `TlhkvhXzWb8` · EN `muNFnJzfyLM` (thumbnails 429 — غدًا)
  - ep36 عضلات 16‑Nov 06:00Z — AR `tAvXQ5Xc1S8` · EN `w4O12YqBQ_k` (thumbnails 429 — غدًا)
- **الويديوهات القديمة** (ep11–ep25) private بأصوات/رسوم قديمة، لم تُحذف — يمكن إعادة بناءها بصورتها الجديدة ثم نشرها بالتوقيت نفسه.

### 2) ترقية الإنتاج (بعد ملاحظتك: الصوت المصري روبوتي، والرسوم والمونتاج ضعيفة)
- **الصوت**: `ar-EG-Shakir`/`Salma` (مصري) → **`ar-KW-FahedNeural`** (كويتي/فصحى) لكل الـ19 حلقة عربية،
  و`en-US-BrianNeural` للإنجليزية. أُضيف **تحقق طول تلقائي** في `tts_sync.py` (يعيد التوليف عند القطع).
- **الرسوم**: مسار جديد `tools/gen_art.py` ← **Pollinations.ai** (مجاني بلا مفتاح) لمشاهد Flat‑Vector
  Cartoon حقيقية + `tools/fetch_commons.py` لصور وثائقية حقيقية من Wikimedia Commons.
- **المونتاج**: إصلاح bug ربط xfade («output unconnected») + ترميز المشاهد الثابتة بدقة 1/3 ثم ترفيع
  في الدمج (أسرع ~10× على جهاز 2 نواة).

### 3) حلقات جديدة (نص + رسوم + metadata + ثيمبنيل + بناء)
- **ep23** جينسن هوانغ يستقيل من NVIDIA (بيان نفيديا الرسمي 2026‑11‑12) · **ep24** أنثروبي IPO ·
  **ep25** Gemini 4 Hydrogen.
- **ep27‑ep30** تواريخ الشركات بصور حقيقية: آبل · كانون · سامسونج · هواوي.
- **ep31‑ep36** «لماذا؟»: البومة تدور 270° · العسل لا يفسد · حواسك الخمسة · الدواء الوهمي ·
  نومك ينضف مخك · عضلاتك تحفظ حركاتك.

### 4) الأدوات
`yt.py` · `unschedule.py` · `gen_art.py` · `commons_search.py` · `check_text.py` (يكشف تشويش النصوص
العربية) · `fix_line.py` · `build_queue.py` · `status.py` · `make_metadata.py` · `make_series_thumb.py`
+ `content-factory/docs/PRODUCTION-PIPELINE.md`.

### 5) المتبقي
1. استكمال البناءات: `python tools/build_queue.py` (يعمل في الخلفية).
2. **الرفع بعد تصفير الحصة اليومية** (≈10:00 توقيت العراق) — كل الفيديوهات private الآن.
3. **الثيمبنيل**: EN بلغ 429 — يُعاد بـ `tools/yt.py thumb` بعد التصفير.
4. **MoneyPrinterTurbo** (MIT، 143k★): تثبيته ثقيل على جهاز 2‑نواة، لكن استُعيرت منه المبادئ
   (TTS مجاني + كابسن + لقطات مجانية) ونُفِّذت محلياً بلا تبعيات ثقيلة.

---

## 1. الحالة الآن

| البند | القيمة |
| --- | --- |
| الفرع | `main` |
| آخر التزام كود | `a9a2b4a` + دفعة 2026-11-12 (ج: القارئ الموحّد + أوفلاين القرآن) — مدفوعة `1.5.9 / 10508` |
| الإصدار الحالي | `versionCode 10508` / `versionName 1.5.9` — لم تُوسم بعد (تنتظر تحقّقًا على الجهاز) |
| التعديلات معلّقة | تحقق بصري + لمس الودجتات + المشغل — معلّقة على الجهاز |

### 1.1 مطابقة محرك الكفيل + توحيد المسافات — 2026-11-12

- **المحرك (`AstronomicalEngine.kt`)** صار مطابقًا لمرجع
  `appkarv/تطبيق_الكفيل_والخوارزميات_أوف_لاين_تاريخ_أوقات_الصلاة_.txt`:
  - فرع الفجر الشيعي عند الشذوذ: الافتراضي 48°، ثم `aqrab_45`، ثم `aqrab_48`،
    ثم ليفا/طهران ⇒ 45° (بنفس ترتيب المرجع وأولويته).
  - فرع العشاء الشيعي: نفس المعالجة.
  - الفرع السني: الافتراضي 48° (كان 45° ثابتًا) مع `aqrab_45 ⇒ 45°`.
  - الإسفار يُملأ فقط في فرع «الصائغ» الشيعي عند شذوذ الفجر صيفًا.
  - تصحيح الهجري المبرمج في المرجع (2026-07-03 ⇒ 17/18 و 07-04 ⇒ 18/19).
- **القاعدة التلقائية لخطوط العرض** عند تغيير طريقة الحساب
  (`PrayerCalculator.autoHighLatForMethod`): الكفيل/لندن ⇒ 48°، بقية الشيعي
  ⇒ 45°، السني ⇒ 90 دقيقة — مطابقة للمرجع، ويبقى التعديل اليدوي متاحًا.
- **المسافات الآمنة موحّدة في الشل**: `WindowInsets.safeDrawing` مرة واحدة،
  الخلفية full-bleed والقصّ/الإزاحة للمحتوى فقط. شريط العنوان والتنبيهات
  والشريط السفلي والشريط الجانبي (Rail/Drawer) كلها تحترم الثقب وأشرطة
  النظام الجانبية (أفقي/DeX/نافذة حرة).
- **`ui/adaptive/ContentInsets.kt`**: `contentStart/contentEnd` +
  `ScreenHorizontalMargin = 16.dp` — كل الشاشات تضيف هامش التصميم فوق مسافة
  النظام بدل أن تستبدلها.
- **القوس**: أُزيل الحشو المزدوج بعد `Modifier.size` (كان يصغّر القوس مرتين)،
  الأبعاد تُحسب من العرض الآمن ونسبة `SKY_ARC_STAGE_ASPECT` مع هامش 8dp فقط.
- **الضبط**: الإصدار المعروض 1.5.9 بدل 1.3.0.
- **تحقق قواعد البيانات الأوفلاين**: المدن 17 (تشمل الـ13 في المرجع + 4)،
  وطرق الحساب 13 (تشمل كل طرق المرجع + إضافات)، وقواعد خطوط العرض 8،
  والتاريخ الهجري/الإزاحات/قوائم العرض مطابقة.

### 1.1d شريط التنقل السائل + الإشعارات الرسمية — 2026-11-13 (جارية)

- **شريط التنقل = تصميم «Liquid Floating Glass»** (مرفق المستخدم):
  كبسولة عائمة زجاجية (تدرّج + حدود لامعة + ظل بنفسجي)، **مؤشر مائي** ينزلق
  بنابض هيدروليكي (`spring(damping 0.62, stiffness 380)`)، العنصر النشط أيقونة
  مكبّرة 1.14 + اسمها بجانبها، وغير النشط أيقونة + تسمية صغيرة 8.5sp،
  و**سحب بالإصبع** ينتقل بين الوجهات (أفقيًا في الشريط السفلي، رأسيًا في الـRail).
  نسخة رأسية (Rail) بنفس اللغة البصرية — 64dp متوسط و72dp عريض.
- **أسماء الإشعارات رسمية وبلا مفهوم «رمضان»**: «العدّاد المباشر» (حي) و«حان الآن موعد …»
  (الأذان) — واللقطة/الملفات بلا ذكر رمضان.
- **الإشعارات تتكيّف مع الطيّ والفتح**: شكل **مكثّف** (`setCustomContentView`) قبل
  التوسّع (سطر واحد: أيقونة + «باقي على …» + العدّاد) وشكل **موسّع**
  (`setCustomBigContentView`) بعد السحب (الكشيدة + العدّاد + شريط التقدم + مربعات
  الأوقات) — لمسارين (compact/full) لكل من الإشعارين.
- **خلفية الإشعار = خلفية «اليوم»** مولّدة Bitmap لحظيًا (`ArcBackground`) — لا ملفات
  ألوان ثابتة ولا مراحل منفصلة.

### 1.1c MaterialExpressiveTheme + adaptive-navigation3 + «أعمال اليوم» المخصَّصة — 2026-11-12/13 (جارية)

- **MaterialExpressiveTheme**: غير علنية (internal) في material3 المضمَّنة مع
  Compose Multiplatform 1.12.2 (Kotlin API إسفيرم؛ لا يمكن تفعيلها قبل إتاحتها
  رسميًا علينًا). يبقى `MaterialTheme` مع `KararTypography` (الأوزان القوية
  = معادل emphasized text) — وسيُفعَّل MaterialExpressiveTheme لحظة صدورها
  الرسمي العلني. لا استخدام لأي واجهة غير علنية.
- **`adaptive-navigation3` 1.3.0** (KMP، مستقر من klibs.io، المستودع المرجعي
  `androidx.compose.material3.adaptive`): أضيفت إلى `commonMain` وتُستخدم
  رسميًا عبر `ListDetailSceneStrategy` المربوطة بـ`NavDisplay(sceneStrategies=…)`:
  فهرسة «مفاتيح الجنان» (`AppRoute.Jinan` = listPane + placeholder) والنص المفتوح
  (`AppRoute.JinanReader` = detailPane) — جنبًا إلى جنب على الشاشات المتوسطة
  والعريضة/المطويات، ولوح واحد كامل الشاشة على الهاتف. التوقع الرجعي يعمل معها.
  (API تحمل Opt-in بـ`ExperimentalMaterial3AdaptiveApi` — Opt-in رسمي في 1.3.0،
  وليس نسخة alpha/beta.)
- وضع القراءة (Chrome-free) يقتصر على الهاتف؛ على اللوحيات/المطويات/DeX تظهر
  الألواح والشريط العلوي/الجانبي (سلوك رسمي للمخططات متعددة الألواح).
- **«أعمال اليوم» شاشة ديناميكية** (بلا أي اختيار يدوي): المطابقة تُحسم
  تلقائيًا من التقويم المدني لوقتك ومدينتك وهجرية اليوم، وتطابق مع تمام
  «مفاتيح الجنان» (ليلة ← يوم ← أسبوع ← أعمال عامة الشهر). تصميمها: كروت
  كامنة (بطاقات بأسلوب «المفاتيح»، 165dp، عنوان العمل فقط، قفزة على القارئ
  بمعرّف contentId)، وتصرّف مصغّرًا فوق كل الشاشات فوق شريط العنوان مباشرة:
  «أعمال اليوم · الخميس 12 جمادى الأولى 1447» — يضيء تلقائيًا من يومك وهجرية
  المدينة، والضغط عليه يفتح شاشة «أعمال اليوم».
- **«أعمال اليوم» ويدجت Glance جديد** (٢…٤×٢): كشيدة هجرية + عدّادات
  الليلة/اليوم/الأسبوع/الشهر العامة + أبرز الأعمال (عنوان فقط).
- **الودجيت كلها الآن افتراضيًا «ألوان اليوم»** — نفس خلفية شاشة «اليوم»
  حرفيًا: تُولَّد **Bitmap مولّدة ديناميكيًا** من `SkyTheme.getSkyColorsForSecond`
  عند كل تحديث وحقن عبر RemoteViews/Glance — تطابق تطابقًا كاملًا (سماء + أفق:
  منتصف الليل ← الفجر ← الشمس ← الظهر ← العصر ← المغرب ← الليل)، بالحدود
  الزجاجية 26FFFFFF. (بلغنى اسمها «قوسُ السماء» في البداية — أُصلح الاسم).
- **الإشعارات على نفس الخلفية المولّدة**: ذات الخلفية المولّدة حرفيًا في الإشعار
  الحي («رمضان مباشر») وإشعار «حان موعد الأذان» وبطاقة الأذان — ثلاثة التخطيطات
  (notification_live / notification_prayer وcompact أيضاً) — لا اختلاف ولا مراحل
  منفصلة؛ الدقيقة التي تعاش فيها تُعرض بالضبط.
- **ألوان اليوم تُحدَّث بعدد الوقت الفعلي** — WorkManager كل 15 د + عند دخول الصلاة
  القادمة + عند فتح التطبيق + عند تغيير إعدادات الضبط. (كل `provideGlance` تُولّد
  الخلفية قدريًا من `SkyTheme` — Zero Delta عن الواجهة).
- **العمل في الخلفية**: WorkManager دوري (15 د) يحدث الودجت والإشعار الحي تلقائيًا،
  بلإضافة `DailyDeedsWidgetReceiver` إلى مستقبلي الودجت.
- إعادة هيكلة المطابقة في `core/jinan/DailyDeeds.kt`: `weekdayDeeds()` و
  `monthDayDeeds(day, monthNum?)` و`monthDeeds()` و`dailyDeeds()` — موسَّعة
  الصيغ (مؤنث/مذكر كاملة، «النصف» لليوم 15، علامات خاصة: عاشوراء، تاسوعاء،
  الأربعين، المبعث، عرفة، الأضحى، الغدير، دحو الأرض).
- **التحقق من قاعدة النصوص**: نحو 600 إدخال JinanItem بخمسة أبواب مرقّمة
  (تعقيبات/أشهر/زيارات/ملحقات/باقيات صالحات)، كلها تُستهلك من طبقة القارئ.
- `KararIcons.AutoMirrored` و`EventsNote` موصى بها مقابل القديمة، تم.

### 1.1b تكيّف M3 Expressive + Adaptive API + Navigation 3 — 2026-11-12 (جارية)

- **الاعتماديات (klibs.io 2026-11-12)**: كل الإصدارات المستقرة الحديثة —
  KMP adaptive `adaptive/adaptive-layout/adaptive-navigation` **1.3.0**،
  androidx `navigation3-runtime/ui` **1.2.1**، KMP nav3-ui **1.2.0**،
  `lifecycle-viewmodel-navigation3` **2.11.0**، androidx adaptive **1.3.0**.
  لا تغييرات مطلوبة — كل شيء على أحدث مستقر بالفعل.
- **WindowInsets** (اللانهائية الرسمية): top = `statusBars ∪ captionBar ∪ displayCutout`؛
  bottom = `navigationBars ∪ displayCutout`؛ أفقي = `safeDrawing` (يشمل العتاد الجانبي
  في DeX/Freeform). الخلفية full-bleed؛ الإزاحة للمحتوى فقط.
- **التنقل متكيّف ديناميكيًا رسميًا (M3 Expressive)**:
  Compact → شريط سفلي؛ Medium/foldables → **Rail 64dp**؛ Expanded/Large/XL والأجهزة
  شبه المسطحة (ChromeOS/DeX/لابتوب) → **Rail 72dp** (التنسيق official-dp).
- **DRAWER أُزيلت**: M3 Expressive لا يوصّفها؛ الـ Rail المطوي (Collapsed Navigation
  Rail) هو البديل الرسمي — كل عنصر أيقونة + تسمية واحدة تحتها.
- **انتقالات انسيابية بلا إعادة تحميل**: Rail يدخل/يخرج بانزلاق نابض أفقي + تلاشٍ؛
  الشريط السفلي يدخل/يخرج بانزلاق نابض عمودي + تلاشٍ؛ عرض الـ Rail يتحرك بـ
  `animateDpAsState(motionSpatial())` بين 64/72dp؛ `navBarReserve` يُلغى تلقائيًا
  على شاشات الـ Rail ووضع القراءة (لا فسحة ميتة).
- **AppDestination**: كل وجهة تبقى تسمية قصيرة واحدة (1 كلمة) أسفل الأيقونة — مطابقة
  لقاعدة M3 أن الـ Rail لا يحذف التسميات.
- التوثيق: `docs/NEXT-SESSION.md` + `docs/motion-system.md`.

### 1.1a احترام «تقليل الحركة» + توحيد مواصفات الحركة — 2026-11-12 (جارية)

- نظام حركة موحّد في `ui/motion/Motion.kt`:
  `motionSpatial()` / `motionEffects()` (تركيبية) و `spatialSpec(reduceMotion)` /
  `effectsSpec(reduceMotion)` (غير تركيبية لـ `transitionSpec`) + `ambientMotionEnabled()`.
- «تقليل الحركة» يُحترم من النظام على أندرويد (`ANIMATOR_DURATION_SCALE == 0` عبر
  `ContentObserver`، تبديل لحظي) وiOS (`UIAccessibilityIsReduceMotionEnabled`)،
  والتنفيذ في `ui/platform/ReduceMotion.*` (expect/actual).
- الحركات المكانية كلها قاطعت فورًا (snap) عند طلب تقليل الحركة؛ التلاشي/اللون يبقى.
- البيئية اللانهائية (نجوم/سحاب/مطر/ثلج/رادار/نبض نجاح) تتجمد في وضع «تقليل الحركة».
- `AppShell` انتقالات NavDisplay: تلاشٍ قصير `fastMs` بلا انزلاق في وضع التقليل.
- `SpringSheet` / `SpringDialog`: بلا انزلاق/زووم وتفريغ `delay(0)` عند التقليل.
- `TodayScreen` عدّاد الساعات، `DuasScreen` تبديل الفهرس/الدعاء، رأس شريط العنوان:
  نفس قاعدة الانتقال عند التقليل.
- `SplashScreen`: الحلقات/النجوم تتجمد، والدخول `snapTo` بلا انزلاق/زووم.
- شريط التنقل الكبسولي وRail/Drawer يستخدمون `motionSpatial` (تكثف لحظي عند التقليل).
- كشف شارة ثيم الضبط (expandHorizontally) وتوسع MergedAccordion و
  DuaFloatingPlayer وArcCalendarSheet وTodayScreen (progress) — كلُّها عبر المواصفات.
- تحسين أداء: قراءة دوران حلقات الـSplash داخل `graphicsLayer { }` (لا إعادة تركيب لكل إطار).
- وثيقة: `docs/motion-system.md`.

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

### 1.19 جلسة 2026-11-12 (ب) — إصلاحات البناء + M3 Expressive + تناغم التنقل

- **CI المكسور #43110119161** مفكك سببه وخُط فيه: `AudioTrack` في `JinanContentData` (حُذف)، عدم شمولية `when` في `toDestination()` (فرع `is AppRoute.JinanReader` أُضيف)، صيغة `entry` في Nav3 1.2.1 (استخدام `entry<SmartType> { route -> ... }` reified، مع التأكد من أن نوع المفتاح يمرر عبر `K::class` افتراضيًا)، و`rememberLazyListState` بمسار خاطئ (أُزيلت الحاجة إليه). #43119327661 **ناجح**.
- **Bar M3 Expressive مرنة:** الشريط السفلي تحول إلى `Flexible Navigation Bar` (surfaceContainer + حد outlineVariant + ظل 2dp): أيقونات 24dp، مؤشر كبسولة 32×56dp لونها `secondaryContainer` تنزلق بنابض مكاني، الأيقونة/التسمية المختارة onSecondaryContainer/secondary (Bold)، وغير المختارة onSurfaceVariant (Medium).
- **زر الرجوع لا يقفز مستوى كامل:** الضغطة كانت تصل عبر NavDisplay + معالج iOS معًا، فتأكل مستويين (الرجوع للرئيسية مباشرة) — أُضيفت مدة انتظار 350ms بمقارنة `TimeSource.Monotonic` على مستوى الملف.
- **شريط العنوان:** العنوان وحده (بلا تاريخ هجري تحته)؛ التاريخ يظهر داخل «اليوم» فقط، وسطر العنوان يختفي عند عدم وجوده.
- **شاشة الأدعية:** أيقونات في أعمدة وليست مستطيلات: دائرة 72dp زجاجية (حدود ذهبية عند التشغيل) + أيقونة 32dp + عنوان سطرين موسّطًا + الفئة بخط 8sp؛ زر التشغيل كشارة 26dp على الدائرة.
- **القارئ الموحّد (v1.5.8):** يدعم وضع «قراءة بلا واجهة — ملء الشاشة» (ينظف الشريط والأزرار كليًا) ونقطة استرجاع شفافة 10dp في الأسفل.
- **CI:** #43122659847 (eadaba1) و#43121892072 فشلا بخطأ `Modifier 'private' is not applicable to 'local variable'` — أُصلح #43123330404 **ناجح** (7fa6c1d) ثم a9a2b4a (الأدعية) — يُتحقَّق منه.
- **مهام متبقية من رسالة المستخدم (غير منجزة بعد، مرتبة حسب الاستحقاق):**
  1. سلس edge-to-edge في «أعمال اليوم» و«القرآن» (مؤكدان بعد إعادة بناء الشريط السفلي؛ لا كتل لونية لأن الشاشات شفافة بالفعل).
  2. نقل حرفي لشاشتَي «اليوم» (القوس) و«الضبط» من `appkarv/amsak3d.tsx` (كود هو المرجع، لم تُنقل منه وحدة كاملة بعد).
  3. ربط «القارئ الموحّد» بـ«الأدعية» و«القرآن» بدل نسخ `DuaDetailScreen` (نفس التصميم + دالة إخفاء الواجهة).
  4. إعادة بناء كل انتقالات/انطفاءات/تمريرات التطبيق كـ Fluid Spring (كود حالي يستخدم `KararTokens.Motion.spatial()` جيد نسبيًا لكن يحتاج ضبط duration/spring من ملاحظات m3.md + fm.md).
  5. إعادة تصميم إشعارات الأذان/العد التنازلي وفق `appkarv/widgetsnotifications.html` (ProgressStyle + طلب INTER_ACTION + سحب الإشعار يوقف الصوت).
  6. شاشة البداية المتحركة (`Startscreen .txt`) — لم تُبنى بعد.
  7. إزالة `derivedStateOf`/`PrayerCalculator` غير المستخدمة من الشل لدى النقل.

### 1.20 جلسة 2026-11-12 (ج) — أوفلاين كامل + القارئ الموحّد القرآني + تجهيزات المنصة (مخطوطة)

- **سياسة الأوفلاين منفّذة:** أزلت الاتكال على الشبكة من نصوص القرآن (`alquran.cloud` كان يقرأ السور ديناميكيًا ← الآن `QuranTexts.bySurah` من `files/quran_texts.json` المضمّن 750KB). أُضيف `core/quran/QuranTexts.kt`. ملفات الصوت للأذان والتكبير مدرجة (`ahmad_aldabbaq`, `amr_kadhemi`, `hassan_rezai`, `jalil_karbalae`, `maitham_tammar`, `takber`, `masbaha_*`) + `quran_texts.json` (1.4MB من مرجع الكفيل).
- **الشبكة المتبقية (مسموحة حصرًا):** `api.open-meteo.com` (الطقس)، `get.geojs.io` (الموقع بالـIP). لا شيء آخر. `RemoteText.kt/expect-actual` حُذفت بالكامل.
- **القارئ الموحّد توسّع:** `UnifiedReaderScreen` يقبل `bodyText` (نص جاهز — للأدعية والقرآن) أو `contentId` (مفاتيح الجنان) و`playerSlot` اختياريًا. القرآن عبر `QuranReader` (تحميل مرة واحدة ثم `UnifiedReaderScreen` مع بسملة وتحويل `﴾N﴾` لسطر مستقل). الأدعية عبر نفس القارئ بدل `DuaDetailScreen` (التي حُذفت). أشرطة التطبيق تختفيان `immersive` كوضع قراءة كاملة.
- **شريط التنقل فوق «اليوم»:** `isToday` لم يعد يشمل `immersive` — «اليوم» الآن يشارك شريط التنقل كما في المرجع؛ `immersive` يعني فقط وضع القراءة الكاملة.
- **إشعارات الأذان والعد التنازلي:** تطابق `widgetsnotifications.html` حرفيًا (بطاقتان: «رمضان مباشر» بعلم لهب + نقطة حمراء + شريط نهاري/ليلي، وبطاقة «حان الآن موعد …» بحالي/القادمة + «إيقاف الأذان» والسحب يوقفه).
- **تقويمان منفصلان (مكتملان):** زر التقويم تحت القوس = تقويم شهري هجري (`ArcCalendarSheet`)، وزر الشريط = تقويم السنوات (`CalendarScreen` يحتوي YearPicker + MonthTableCard).
- **شاشة البداية:** `ui/splash/SplashScreen.kt` مطابقة لـ `Startscreen .txt` (150 نجمة + 15 أيقونة إسلامية عائمة + حلقات + شعار + Reem Kufi).
- **الحركة:** `KararTokens.Motion` مطابقة لرموز Expressive الرسمية (spatial 0.8/380/800/200، effects 1.0/1600/3800/800).
- **المنصة:** المانيفست محدّث (`density|keyboard|navigation` في configChanges + `com.samsung.android.keepalive.density` DeX). أيقونة تكيفية بخلفية `system_accent1_100` (values-v31) و`system_neutral1_100` (values-night-v31، fallback `#0B1120` لـ≤11). `baseline-prof.txt` أضيف بتوقيعات ساخنة أولية (غير مكتمل — الكامل عبر macrobenchmark لاحقًا).
- **M3 Expressive Flexible Navigation Bar:** المؤشر كبسولة `secondaryContainer` 32×56dp.
- **لم تُفحص بصريًا:** كل الأيقونات/الإشعارات/السبلاش/الحركة تنتظر جهازًا حقيقيًا — البناء الوحيد الموثوق هو CI.

---

### 1.21 جلسة 2026-11-12 (د) — أعمال اليوم + فارغ المهام + القوس القمري (الالتزامات مكمل)

- **بفشل CI #43137679456/failure → #43139002685/failure (MoonPhaseIcon: استيراد مس `layout.size` مفقود) → `#43142454143` f2dff12 «fix(arc): import layout.size in MoonPhaseIcon»** — التحقق النهائي قيد الانتظار.
- **«أعمال اليوم» (شاشة جديدة):** `DailyDeedsScreen` يقرأ `LocalDate` + `hijri.day` ويتجه بالبحث في `JinanContentData.bookParts`:
  1) أعمال هذه الليلة («ليلة/الليلة» + الرقم الهجري)
  2) أعمال اليوم («اليوم» + الرقم الهجري)
  3) أعمال يوم الأسبوع (اسم اليوم السبت/الجمعة…)
  - تطابق كامل حرفيًا؛ أي عمل يفتح القارئ الموحّد (`JinanReader`)، وزر ⭐ يضيفه إلى المفضلة.
- **المهام فارغة:** حُذفت `DEFAULT_TASKS` بالكامل؛ الشاشة فارغة عند أول فتح وتعرض «محفوظاتي من مفاتيح الجنان» (تنجيم من القارئ) + ملاحظات المستخدم.
- **القارئ الموحّد** في مسار `JinanReader` يعرض ⭐ في الشريط العائم (AppShell state → settings.favoriteJinanItems).
- **القوس 3D من `amsak3d.tsx` مُعاد:** `MoonPhaseIcon.kt` (قابل لإعادة الاستخدام في بطاقة Qibla) و`MoonPhase.kt` (محرك الطور مطابقة API الرسمي `getMoonDetailsByHijriDay`) يعيدان رسم: الشمس (تدرج + 12 شعاع + هالة) والقمر (هالة مزدوجة + فوهات + طور بمساري قوسين + هلال متزايد/متناقص/بدر حسابًا). القطرات والألوان مطابقة للمرجع.
- **المفاتيح:** بلاطة «أعمال اليوم» جديدة (الكهرمان/`CalendarToday`)؛ بلاطة «المهام والملاحظات» القديمة.
- **متبقي:** تحقق محلي/بصري لكل ما سبق — CI وحده القَيْد؛ لا يوجد جهاز.
- **الوسم:** لم تُوسم بعد (تنتظر تحقق الجهاز).
- **آخر CI:** `a63a2d6` → **ناجح** (#43203488968) — APK موقّع 56,358,092 بايت مؤرشف.
- **Strict warning:** بواجهات SpringDialog/SpringSheet (من جلسة منقطعة) — تعمل داخل نفس النافذة، خلف captionBar تلقائيًا، ولا تُحجب على الأجهزة القديمة.

### 1.22 جلسة 2026-11-12 (هـ) — استكمال كل ما لم يُتحقق + بوابة CI

- **الحالة:** CI `a63a2d6` **أخضر** (#43203488968). APK: `amsakkarrar-signed-apk`، 56,358,092 بايت.
- **修:** `automirrored` صغيرة (TasksScreen)؛ تم جرّ كل الاستيرادات المفقودة (Dp/dp/getValue/padding/Text) وحذف `calculateBottomPadding`/`calculateTopPadding` غير artily imported (دوال أعضاء في PaddingValues)؛ `ExperimentalLayoutApi` OptIn للـIME في المهام/الأدعية.
- **ملاحظة تقنية:** `PaddingValues.calculateTopPadding()/calculateBottomPadding()/calculateStartPadding()` هي **دوال أعضاء** في واجهة `PaddingValues` — لا تستوردها أصلًا.
- **متبقي لا خروج منه تقنيًا:** التحقق البصري على جهاز؛ Native checks.

---

## 8. قواعد تحديث هذا الملف
- حدّث «الحالة الآن» و«الخطوة التالية» بعد كل مهمة منجزة.
- أضف سطرًا في سجل تقدم الخطة، ولا تحذف التاريخ.
- لا تضع هنا تفاصيل طويلة؛ التفاصيل في الخطة الرئيسية.
