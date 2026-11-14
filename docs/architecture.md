# معمارية إمساكية كرار — فصل «النواة بلا واجهة» (UI-Free Core)
> معايير Kotlin/Compose Multiplatform الرسمية (نوفمبر 2026): Unidirectional Data Flow —
> الحالة تتدفق لأسفل، الأحداث تتدفق لأعلى، والمنطق البرمجي كامل في الطبقة السفلى.

## 1. الطبقات

```
shared/
  commonMain/kotlin/com/karrar/amsakiah/
    core/          ← النواة: Kotlin خالص، صفر Compose، صفر مرجع واجهة
      astronomy/     PrayerCalculator · AstronomicalEngine · CityDatabase · OfflineCities
      arc/           ArcPhase — منطق مرحلة القوس (مستخرج من الشاشة رقمًا)
      jinan/         JinanContentData · DailyDeeds · JinanNormalizer · JinanContentTexts
      state/         SettingsChangeBus (pure) · PrayerCalculator · PrayerMeta
      settings/      AppSettings · SettingsStorage (expect/actual)
      data/          ShiaDuasLibrary · TasbihPresets · HolySites · DuaTexts
      weather/       WeatherInfo · fetchWeather
      location/      LocationProvider (expect/actual) · IpLocation
      quran|sensors|net|network|feedback|widgets|audio   ← كلها pure/expect
    ui/            ← Compose + حافة الواجهة
      state/         AppState — مخزن اللقطة (Compose snapshot) المتعمد عند حافة الواجهة
      permissions/   PermissionController (@Composable — يحتاج دورة الحياة)
      screens/  components/  navigation/  theme/  splash/  motion/
  commonTest/      ← اختبارات النواة: DailyDeedsMatchTest (نجح)
```

## 2. القواعد الصارمة (يُمنع خرقها)

1. **`core/` لا تستورد** `androidx.compose.*` أو `com.karrar.amsakiah.ui.*`.
   كل منطق فلكي/مطابقة/فلترة/تنسيق/تخزين يعيش هنا ويُختبر بلا Compose.
2. **`ui/` لا تحسب مجالًا** (حسابات فلكية، مطابقة هجرية، تطبيع نصوص) —
   كل ما تستهلكه يأتي من `core/` جاهزًا.
3. **الحالة اتجاه واحد**: المستخدم → حدث (`onClick` / `state.update { … }`)
   → حالة جديدة → تُعاد تركيب القراء فقط.
4. **من لا يقرأ الحالة لا يُعاد تركيبه** — لا نخفي Float/Lane خلف شاشات.
5. **زمن التطبيق**: `state.now` يتغير كل ثانية بدون إعادة حساب المواقيت؛
   تحديثات الأوقات عبر `SettingsChangeBus` والودجت أثناء تشغيل التطبيق.
6. الضبط/الإعدادات تسري فورًا على بقية التطبيق لأن مصدر الحقيقة واحد:
   `AppSettings` — لا موازٍ لها في أي شاشة.

## 3. حالة النواة بعد التنظيف (2026-11)

| البند | الحالة | السبب المعماري |
|---|---|---|
| `AppState` (مخزن اللقطة: settings·now·toast·nearby…) | انتقلت إلى `ui/state` | Compose snapshot هو الحل الأنسب عند حافة الواجهة بكلفة صفرية على التركيب القصير؛ كل العمليات الحسابية تقع في `core/` وسهلة الاختبار |
| `PermissionController` (`@Composable` + expect/actual) | انتقلت إلى `ui/permissions` | تحتاج دورة حياة للتذكير بالصلاحيات والتخطي بالـLifecycle |
| `ArcPhase` + `secondsOf` + `computeArcPhase` | انتقلت إلى `core/arc` | منطق فلكي خالص يجب أن يكون قابلًا للاختبار بلا Compose |
| `DailyDeeds` | `core/jinan` بمطابقة كاملة | اختُبر ضد عناوين «مفاتيح الجنان» الحقيقية — تمرير كامل |
| `SettingsChangeBus` | بقي في `core/state` | Kotlin خالص، لا تبعية منصة ولا Compose |
| `PrayerCalculator` / `AstronomicalEngine` | بقيا كما هما | pure في كامل أساسهما؛ الشاشة تستهلكهما مباشرة |

## 4. المتبقي بوضوح (بدون تجميل)

- `AppState` لا تزال مخزنًا منسوخًا عند حافة الواجهة وليس `ViewModel + StateFlow`
  كامتداد كامل التجهيز؛ هو موضع التفاعل المتعمد للتطبيق الكامل. التحويل الكامل
  قرار مستقل (انظر MASTER-PLAN) وليس شرطًا لصحّة UDF طالما:
  - الحدث الوحيد المعروض على الشاشات هو دوال نقية (`update`, `selectCity`,
    `detectLocation`…)، و
  - لا تملك أي شاشة حالتها أو مصدرًا بديلًا موازيًا.
- شاشة التقويم (`CalendarScreen`) لا تزال تملك بعض الحساب المكاني المحلي —
  هُندس وعيُه إلى `core` لاحقًا عند أول تعديل عليها.
- لا يوجد Detekt/lint آلي يمنع استيراد `ui` من `core` في هذا المستودع بعد؛
  حتى صدوره، تُفرض القاعدة بمراجعة ولّد واحدًا بسيطًا:

```
Get-ChildItem -Recurse shared\src\commonMain\kotlin\com\karrar\amsakiah\core -Filter *.kt `
  | Select-String 'androidx\.compose|com\.karrar\.amsakiah\.ui\.'
```
يجب أن يُرجع **لا شيء**.

## 5. كيفية إضافة منطق جديد (Checklist)

1. هل هو حساب/مطابقة/تنسيق/تخزين/خطأ؟ → `core/<scope>/` مباشرة (وظيفة أو فئة).
2. أضف اختبارًا في `shared/src/commonTest` يثبت السلوك بمادة حقيقية (عناوين الجنان مثلًا).
3. لا تستورد `compose` في `core` — إن احتاجت خطوة المستخدم UI ضعها في `ui/`.
4. أي شاشة/ودجت تستهلك الناتج كبيانات، لا ككود.
