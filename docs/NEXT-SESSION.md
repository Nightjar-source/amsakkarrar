# ملف السياق — اقرأني أولًا قبل أي عمل

> **الغرض:** إذا انتهى سياق النموذج أو بدأت جلسة جديدة، هذا الملف يعيدك إلى نقطة
> العمل الحالية في دقيقة واحدة. الخطة الكاملة في `docs/MASTER-PLAN-amsak3d.md`.

---

## 1. الحالة الآن

| البند | القيمة |
| --- | --- |
| الفرع | `main` |
| آخر التزام | `568286b` — نسخ احتياطي مشفّر لتلغرام |
| المرحلة | **المرحلة 1 — شاشة اليوم** (المرحلة 0 مكتملة برمجيًا) |
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

## 3. الخطوة التالية فورًا

**المرحلة 1 — شاشة اليوم:**

1. إعادة بناء رأس «اليوم» من `amsak3d.tsx`: المدينة + الطقس + كشيدة الشهر في سطر
   واحد متجاوب.
2. `SkyArc` مطابق للمرجع: الحجم، الزخارف، القمر/الشمس، بلا وميض.
3. `PrayerStrip` يمرّر الصلاة التالية إلى المنتصف بسلاسة.
4. `Countdown` بشكله الأصلي، ويُخفى عند عدم اختيار أي صلاة.
5. `WeatherEffects` خلف القوس بلا وميض.

**المرحلة 0 (منجزة برمجيًا):**
- `KararTokens.kt` و`KararIcons.kt`.
- `KararTheme` فاتح/داكن + Typography + Shapes.
- `GlassSurface.kt`.
- `AppShell.kt` بـNavigation 3 + Adaptive + Edge-to-edge.
- حذف `AdaptiveAppScaffold.kt` القديم.
- بناء `:shared:compileAndroidMain` و`:androidApp:assembleDebug` ناجح.

**بوابة المرحلة 0 المتبقي:** اختبار بصري على جهاز/محاكي عند توفره
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
| `shared/src/commonMain/.../ui/navigation/AdaptiveAppScaffold.kt` | الشل الحالي (يُستبدل) |
| `shared/src/commonMain/.../ui/theme/KararTheme.kt` | الثيم الحالي |
| `shared/src/commonMain/.../ui/theme/SkyColors.kt` | تدرجات السماء الحالية |
| `shared/src/commonMain/.../ui/screens/TodayScreen.kt` | شاشة اليوم الحالية |
| `shared/src/commonMain/.../ui/screens/SettingsScreen.kt` | الضبط الحالي |
| `shared/src/commonMain/.../core/settings/AppSettings.kt` | نموذج الإعدادات |
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

---

## 7. القيود الحالية

- لا جهاز/محاكي: لا يمكن التحقق الفعلي من الودجت/الإشعار/الصوت/Edge-to-edge.
- iOS غير قابل للبناء على Windows.
- الصوت لم يُؤكد سبب فشله على جهاز حقيقي.
- Navigation 3 وAdaptive موجودان في التبعيات لكن غير مستخدمين فعليًا.

---

## 8. قواعد تحديث هذا الملف

- حدّث «الحالة الآن» و«الخطوة التالية» بعد كل مهمة منجزة.
- أضف سطرًا في سجل تقدم الخطة، ولا تحذف التاريخ.
- لا تضع هنا تفاصيل طويلة؛ التفاصيل في الخطة الرئيسية.
