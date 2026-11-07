# ملف السياق — اقرأني أولًا قبل أي عمل

> **الغرض:** إذا انتهى سياق النموذج أو بدأت جلسة جديدة، هذا الملف يعيدك إلى نقطة
> العمل الحالية في دقيقة واحدة. الخطة الكاملة في `docs/MASTER-PLAN-amsak3d.md`.

---

## 1. الحالة الآن

| البند | القيمة |
| --- | --- |
| الفرع | `main` |
| آخر التزام كود | `25df67d` — المرحلة 1 (شاشة اليوم) |
| المرحلة | **المرحلة 1 — شاشة اليوم** مكتملة برمجيًا، بانتظار Gate بصري على جهاز |
| المرحلة التالية | **المرحلة 2 — الضبط المدمج + التقويم M3 Expressive** |
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

## 3. الخطوة التالية فورًا — المرحلة 2

1. دمج ضبط الكفيل وV7 في `SettingsScreen` واحد بأقسام منسدلة، دون تكرار البحث أو
   تقويم PDF:
   - الموقع (GPS/IP/مدن أوفلاين) والتوقيت الصيفي.
   - الطريقة والزوايا وقواعد خطوط العرض العليا (في شاشة الفقه فقط للتنبيه).
   - التعديلات اليدوية لكل وقت.
   - اختيار الأذان/التذكير/العدّ (محدد أو الكل) + `imsakMode`
     (`ramadan_auto` / `always` / `hidden`).
2. تحويل «الإمساكية» إلى «التقويم» M3 Expressive: تغيير السنة/الشهر الهجري
   والميلادي كلًّا على حدة، ماضٍ/قادم، وبطاقات أيام متجاوبة.
3. **Gate 2**: كل الإعدادات تعمل وتنعكس فورًا على اليوم والودجت والإشعار.

**المرحلة 1 (منجزة برمجيًا — تحتاج Gate جهاز):**
- `SkyArc.kt` مطابق للمرجع (viewBox `22 2 356 298`، مسار `M 30 260 A 170 240...`،
  شمس/قمر بطور، هالة mist، طرفا القوس، شارة المرحلة).
- `WeatherEffects.kt`: سحاب/مطر/ثلج داخل `Canvas` بلا إعادة تركيب.
- `TodayScreen.kt`: رأس متجاوب (مدينة/طقس + كشيدة/تاريخ)، قوس، عدّاد مشروط
  بـ`liveIds` مع نقاط وسحب، شريط صلوات موسّط تلقائيًا، زر تقويم واحد.
- `AppSettings.imsakMode` + `PrayerCalculator.visibleIds/liveIds` + تطبيقها على
  التقويم و`PrayerSnapshot` (الودجت والإشعار).
- بناء ناجح: `:shared:compileAndroidMain` ثم `:androidApp:assembleDebug`
  (4m 28s).

**بوابة المرحلة 1 المتبقي:** اختبار بصري على جهاز/محاكي عند توفره
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
| `androidApp/.../notifications/AdhanService.kt` | الصوت |
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

---

## 7. القيود الحالية

- لا جهاز/محاكي: لا يمكن التحقق الفعلي من الودجت/الإشعار/الصوت/Edge-to-edge
  ولا بوابات 320/600/840dp.
- iOS غير قابل للبناء على Windows.
- الصوت لم يُؤكد سبب فشله على جهاز حقيقي.
- `offsetHours` يستخدم `Clock.System.now()` لكل التواريخ؛ DST لكل تاريخ في
  المرحلة 6.

---

## 8. قواعد تحديث هذا الملف

- حدّث «الحالة الآن» و«الخطوة التالية» بعد كل مهمة منجزة.
- أضف سطرًا في سجل تقدم الخطة، ولا تحذف التاريخ.
- لا تضع هنا تفاصيل طويلة؛ التفاصيل في الخطة الرئيسية.
