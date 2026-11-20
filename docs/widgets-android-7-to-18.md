# الودجتات من Android 7 إلى Android 18 — مصفوفة التوافق وطرق 2026

> تاريخ التحقق: **2026-11-08**. أرقام الإصدارات وأسماء الـAPI من مصادر
> Android  الرسمية فقط. أي بند لم يُتحقق منه على جهاز حقيقي مذكور صراحة.

---

## 1. نطاق الدعم الفعلي

| النظام | API | الحالة | ما يخص الودجتات |
| --- | --- | --- | --- |
| Android 7.0 / 7.1 | 24 / 25 | مدعوم رسميًا (`minSdk = 24`) | RemoteViews كلاسيكي عبر Glance، `Chronometer` + `TextClock` |
| Android 8–11 | 26–30 | مدعوم | `requestPinAppWidget` متاح من 26 (مُقيَّد بفحص إصدار) |
| Android 12–14 | 31–34 | مدعوم | أبعاد دقيقة للودجت، `RemoteViews` محسّن، صور المعاينة |
| Android 15 | 35 | مدعوم | Edge-to-Edge إلزامي، لا أثر على الودجت |
| Android 16 | 36 | مدعوم | Live Updates (إشعار فقط)، لا تغيير في API الودجت |
| Android 17 | 37 | مستهدف (`targetSdk = 37`) | **حد ذاكرة RemoteViews = 6 × العرض × الارتفاع** |
| Android 18 | — | **لم يُعلن بعد** | لا صفحة رسمية (`/about/versions/18` = 404 في 2026-11-08) |

### عن Android 18
لا يوجد إصدار بهذا الاسم منشور. أحدث ما نشرته Google هو Android 17 (API 37)
وصفحات تغييرات السلوك الخاصة به. لذلك لا يمكن «التحقق» منه؛ ما فعلناه هو
**تحصين استباقي**:

1. لا نستخدم أي opt-out قديم (`windowOptOutEdgeToEdgeEnforcement`، تثبيت
   الاتجاه، `resizeableActivity`) لأن الإصدارات القادمة تلغيه.
2. لا نعتمد على `RemoteViews` ثقيل أو `partiallyUpdateAppWidget`؛ كل تحديث
   كامل وبحجم صغير، فلا يتأثر بأي تقليص مستقبلي للحد.
3. كل استدعاء لنظام أحدث محمي بـ`Build.VERSION.SDK_INT` أو عبر واجهات
   `NotificationCompat`/`Glance` المتوافقة، فلا يوجد `NoSuchMethodError` عند
   تشغيل APK واحد على إصدار أحدث أو أقدم.

---

## 2. الاعتماديات و«أحدث الطرق» (Compose/Glance/Kotlin)

| العنصر | الإصدار | ملاحظة |
| --- | --- | --- |
| Glance (`glance-appwidget`, `glance-material3`) | **1.2.0** (مستقر، 26 أغسطس 2026) | `minSdk` صار 23، لذا يغطي Android 7 |
| Kotlin | 2.4.21 | لا تغيير |
| Compose Multiplatform | 1.12.2 | لا تغيير |
| WorkManager | 2.12.0 | جدولة التحديث الدوري فقط |

القواعد الحديثة المطبَّقة:

- **لا نمزج** `androidx.compose` مع `androidx.glance` داخل تركيب الودجت
  (تحذير رسمي في وثائق Glance).
- `SizeMode.Exact` لكل الودجتات السبعة، مع `LocalSize` لحساب ثلاث درجات
  (COMPACT/MEDIUM/LARGE) بدل ثلاث تهيئات منفصلة.
- `onCompositionError` مُنفَّذ في الودجتات السبعة (أُضيف في Glance 1.1.0)
  بدل الاعتماد على شاشة الخطأ الإنجليزية الافتراضية.
- العدّاد ليس مؤقتًا في العملية: `Chronometer` و`TextClock` أصليان داخل
  `RemoteViews`، لذا يستمران بعد نوم العامل وبعد Doze.
- لا استخدام لـ`currentCompositeKeyHash` المهجور (استُبدل في 1.2.0).

---

## 3. ما يعتمد على إصدار النظام (ومحمي بفحص)

| الاستدعاء | الحد الأدنى | الحماية |
| --- | --- | --- |
| `RemoteViews.setChronometerCountDown` | API 24 | `minSdk = 24`، لا يحتاج فحصًا |
| `Chronometer` / `TextClock` / `format12Hour` / `layoutDirection` | API 17 | آمن |
| `requestPinAppWidget` | API 26 | `if (SDK_INT >= O && isRequestPinAppWidgetSupported)` |
| `widgetFeatures` / `targetCellWidth` / `description` في XML | API 31 | تُهمَل تلقائيًا في الإصدارات الأقدم |
| `setShortCriticalText` / `setRequestPromotedOngoing` | API 36 | عبر `NotificationCompat` (لا أثر قبل 36) |
| حد ذاكرة RemoteViews | API 37 | التصميم نصي/أشكال بلا صور نقطية |

---

## 4. حد ذاكرة الودجت في Android 17

Android 17 يفرض على التطبيقات المستهدفة 37 حدًا لكل تحديث `RemoteViews`
يساوي `6 × العرض × الارتفاع` بكسل، ويَرفض التحديث بـ`IllegalArgumentException`
عند تجاوزه.

وضع الودجتات هنا:

- ودجتات Glance الستة: `Text` و`Column/Row/Box` وخلفيات لونية فقط، بلا صور.
- ودجتا العدّاد والساعة: `RemoteViews` من `TextViews + Chronometer/TextClock
  + ProgressBar`. لا صور، لا `ListView`، لا `WebView`.
- **لا `partiallyUpdateAppWidget`** في المشروع (تحقق بـgrep).
- عند أي فشل تركيب: `WidgetErrorViews.show()` ينشر `widget_error.xml` (نص
  فقط، بضع كيلوبايتات) ويسجّل الخطأ مرة واحدة لكل ودجت.

قياس فعلي (عند توفر جهاز):

```
adb shell dumpsys appwidget | Select-String -Pattern "karar|memory"
adb shell dumpsys activity service androidx.work
```

---

## 5. ما تغيّر في هذه الجولة

1. `onCompositionError` في: `TodayWidget`, `TimesWidget`, `CountdownWidget`,
   `HijriWidget`, `ClockWidget`, `JourneyWidget`, `TasbihWidget`.
2. `WidgetErrorViews` + `res/layout/widget_error.xml` — بديل نصي خفيف
   وقابل للنقر لفتح التطبيق.
3. `rememberWidgetTier()`: عند وصول حجم صفري/غير معروف من مشغّل قديم
   (Android 7–11)، نرجع `MEDIUM` بدل `COMPACT` حتى لا تُخفى الصلوات.
4. الإشعار الحي: `NotificationCompat.ProgressStyle` موحّد على كل الإصدارات
   بلا RemoteViews مخصص، مع `setProgress` قياسي للإصدارات الأقدم.

---

## 6. اختبارات القبول (تحتاج جهازًا/محاكيًا)

| # | الاختبار | النظام |
| --- | --- | --- |
| 1 | إضافة كل ودجت من المنتقي، ثم تصغيره وتكبيره | 7، 12، 17 |
| 2 | بقاء العدّاد يعمل بعد قفل الشاشة 20 دقيقة | 7، 17 |
| 3 | تغيير المدينة من التطبيق ⇒ تحديث الودجت فورًا | 7، 17 |
| 4 | إعادة تشغيل الجهاز ⇒ إعادة بناء الودجت | 7، 17 |
| 5 | تجاوز حد الذاكرة (ودجت بحجم كبير + صورة) لا يحدث | 17 |
| 6 | سحب الإشعار الحي ثم عدم عودته قبل الصلاة القادمة | 7، 16، 17 |

> الحالة: لم تُنفَّذ بعد — `adb devices` فارغ في بيئة العمل الحالية.
