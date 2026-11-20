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
      state/         AppKernel · ToastModels · SettingsChangeBus · PrayerCalculator · PrayerMeta
      settings/      AppSettings · SettingsStorage (expect/actual)
      data/          ShiaDuasLibrary · TasbihPresets · HolySites · DuaTexts
      weather/       WeatherInfo · fetchWeather
      location/      LocationProvider (expect/actual) · IpLocation
      quran|sensors|net|network|feedback|widgets|audio   ← كلها pure/expect
    ui/            ← Compose + حافة الواجهة
      state/         AppState — واجهة Compose Snapshot يربط AppKernel بلقطاتها (by mutableStateOf)
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
| `AppKernel` + `ToastModels` | `core/state` — **النواة التشغيلية للدولة والعمليات، بلا Compose إطلاقًا** | State/events/errors/location/IP/cities/toasts — Kotlin خالص، يستدعى من أي منصة |
| `AppState` (طنّاف UI: snapshot) | `ui/state` — **facade رقيق** يربط `AppKernel` بقراء Compose (`by mutableStateOf`) | Snapshot هو الحل الأقصر للتركيب التفاعلي عند حافة الواجهة؛ وفّرنا مرآة واحدة تزامن كل نتيجة kernel → UI |
| `AppState` → ليس UI فقط | **لا تملك أي منطق تشغيلي**؛ فقط تفوّض إلى `AppKernel` | يرشد الطبقة |

هذا يعني كل الحسابات/القرارات/التخزين في `core`، والواجهة تقرأ لقطة قرائتها فقط.
واجهة `AppKernel` يستدعىها `AppState`، والواجهة (الشاشات) لا تزال تستهلك `AppState` كما كانت.

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
