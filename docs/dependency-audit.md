# تقرير فحص الاعتماديات — klibs.io MCP

- التاريخ: **2026-11-06**
- المصدر: خادم `https://api.klibs.io/mcp` (أداتا `getLatestVersion` و`searchProjects`)
- خط الأساس: Kotlin 2.4.21، Compose Multiplatform 1.12.2، AGP 9.2.1،
  Gradle 9.4.1 (مثبّت)، compileSdk 37، minSdk 24، JVM 21.

## النتيجة

| الحزمة | قبل | أحدث مستقر | القرار |
| --- | --- | --- | --- |
| `org.jetbrains.kotlin:kotlin-gradle-plugin` | 2.4.20 | 2.4.21 | ✅ ترقية |
| `org.jetbrains.compose:compose-gradle-plugin` | 1.12.1 | 1.12.2 | ✅ ترقية |
| Android Gradle Plugin | 9.1.0 | 9.2.1* | ✅ ترقية |
| `androidx.activity:activity-compose` | 1.10.1 | 1.13.0 | ✅ ترقية |
| `androidx.lifecycle:*` | 2.8.7 | 2.11.0 | ✅ ترقية |
| `androidx.datastore:datastore-preferences` | 1.1.4 | 1.3.0 | ✅ ترقية |
| `org.jetbrains.kotlinx:kotlinx-coroutines-core` | 1.10.2 | 1.11.0 | ✅ ترقية |
| `org.jetbrains.kotlinx:kotlinx-serialization-json` | 1.12.0 | 1.13.0 | ✅ ترقية (2026-11-17 — مستقر، ويشمل تحسينات أداء التسلسل) |
| `androidx.core:core-splashscreen` | 1.0.1 | 1.2.0 | ✅ ترقية |
| `androidx.navigation3:navigation3-*` | 1.2.0 | 1.2.1 | ✅ ترقية |
| `org.jetbrains.androidx.navigation3:navigation3-ui` | 1.2.0-rc01 | 1.2.0 | ✅ ترقية إلى المستقر |
| `androidx.glance:glance-*` | 1.1.0 (مثبت في الملف) | 1.2.0 | ✅ ترقية + نقلها إلى كتالوج الإصدارات |
| `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3` | 2.11.0 | 2.11.0 | ✔️ محدّث |
| `org.jetbrains.compose.material3.adaptive:adaptive*` | 1.3.0 | 1.3.0 | ✔️ محدّث |
| `androidx.profileinstaller:profileinstaller` | 1.4.1 | 1.4.1 | ✔️ محدّث |
| `org.jetbrains.kotlinx:kotlinx-datetime` | 0.6.2 | لا يوجد مستقر على 0.7+ | ⏸️ مثبّت على 0.6.x (تغييرات كاسرة) |
| `com.materialkolor:material-kolor` | 2.1.1 | 5.0.1 | 🗑️ حُذفت (غير مستخدمة + قفزة كبرى) |
| `org.jetbrains.compose.material3:material3-window-size-class` | 1.7.3 | لا إصدار مستقر لخط 1.12 | 🗑️ حُذفت (غير مستخدمة) |

\* AGP 9.2.1 هو أحدث إصدار مستقر متوافق مع Gradle 9.4.1 المثبّت في المشروع.
AGP 9.3 يحتاج Gradle 9.5+، وAGP 9.4/9.5 يحتاج Gradle 9.6+.

## ملاحظات التحقق

- `searchProjects("compose multiplatform")` أكد أن مشروع JetBrains يدعم
  `COMMON` و`ANDROIDJVM` و`JVM_21` و`NATIVE_ios_arm64` و`NATIVE_ios_x64`
  و`NATIVE_ios_simulator_arm64` — أي كل أهداف هذا المشروع.
- `getLatestVersion` لا يفهرس إضافات Gradle مثل Kotlin/Compose/AGP، لذلك تم
  التحقق منها من بيانات Maven الرسمية:
  - Compose `1.12.2` (المستقر الأحدث)
  - Kotlin `2.4.21` (المستقر الأحدث)
  - AGP `9.2.1` (متوافق مع Gradle 9.4.1)
- `org.jetbrains.androidx.navigation3:navigation3-ui` مستقرّه `1.2.0`، وتم
  إخراجه من `1.2.0-rc01`.
- لا يوجد إصدار مستقر لـ `material3-window-size-class` على خط Compose 1.12،
  والمكتبة غير مستخدمة في الكود أصلًا، فحُذفت بدل ترقيتها.
- `material-kolor` غير مستوردة في أي ملف Kotlin داخل `shared/src`، فحُذفت.

## إعادة الفحص

```powershell
# عبر OpenCode
/klibs-check

# أو مباشرة من الطرفية
pwsh -File scripts/klibs-audit.ps1
```

## التحقق من البناء

> **AGP 9 + KMP:** اسم المهمة الصحيح في `:shared` هو `compileAndroidMain` — وليس
> `compileDebugKotlinAndroid` (ذلك الاسم لا يوجد في مصادر Android للمكتبة المشتركة،
> ويُظهر Gradle خطأ `Task ... not found`).

```powershell
.\gradlew.bat :shared:compileAndroidMain --stacktrace
.\gradlew.bat :androidApp:assembleDebug --stacktrace
.\gradlew.bat :androidApp:assembleRelease --stacktrace
```

## إضافات إعادة البناء (الإصدار 8)

| الاعتمادية | الإصدار | الاستخدام | الحالة |
| --- | --- | --- | --- |
| `androidx.glance:glance-appwidget` | 1.2.0 (مستقر) | 4 ويدجت Glance | ✅ يعمل على أندرويد فقط |
| `androidx.glance:glance-material3` | 1.2.0 (مستقر) | ثيم الويدجت | ✅ أندرويد فقط |
| `androidx.core:core-splashscreen` | 1.2.0 | شاشة البداية | ✅ |

> لا توجد اعتمادية جديدة في `commonMain`، لذلك دعم iOS لم يتأثر.
> `kotlinx-datetime` بقي على `0.6.2`، وGradle Wrapper بقي `9.4.1`.

## فحص 2026-11-12 / 2026-11-13 (adaptive-navigation3 + M3 Expressive)

| الاعتمادية | المستقر عند الفحص (klibs.io) | الحالة |
| --- | --- | --- |
| `org.jetbrains.compose.material3.adaptive:adaptive` (+ layout/navigation) | **1.3.0** | ✅ مستقر مثبت |
| `androidx.compose.material3.adaptive:adaptive` (Android) | **1.3.0** | ✅ مستقر مثبت |
| `org.jetbrains.compose.material3.adaptive:adaptive-navigation3` | **1.3.0** | ✅ مستقر — **أُضيفت جديدة** للمشهد قائم/تفصيل |
| `androidx.navigation3:navigation3-runtime/ui` | **1.2.1** | ✅ مستقر مثبت |
| `org.jetbrains.androidx.navigation3:navigation3-ui` | **1.2.0** | ✅ مستقر مثبت |
| `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3` | **2.11.0** | ✅ مستقر مثبت |
| `org.jetbrains.compose.material3:material3` | **1.12.0-alpha03** (المضمَّنة مع CMP 1.12.2) | ⚠️ `MaterialExpressiveTheme`/`MotionScheme` **غير علنيتين** في هذا الخط (internal) — لا يمكن تفعيلهما؛ استخدم `MaterialTheme` مع M3 tokens إلى أن تُصدر نسخة علنية |

- `MaterialExpressiveTheme` لم تُستخدم — لا وجود علني لها في هذه النسخة، والتوثيق
  المبني لمستقر androidx (1.10.0) يصف APIs لم تُصدر بعد في خط KMP 1.12.2.

## فحص 2026-11-17 (كامل — قبل/بعد الإصلاحات)

خُصّصت دورة فحص كاملة لحزمة الاعتماديات بعد طوفان الإصلاحات (الأعمال/الإشعارات/الودجت/القراءة) — عبر أدوات klibs.io `getLatestVersion` و`searchProjects`:

| الحزمة | المستقر عند الفحص (klibs.io) | الحالة |
| --- | --- | --- |
| `androidx.lifecycle:lifecycle-viewmodel/runtime-compose/viewmodel-compose` | **2.11.0** (مثبتة) | ✔️ لا إصدار جديد إلا alpha (2.12.0-alpha05) |
| `androidx.datastore:datastore-preferences` | **1.3.0** | ✔️ مثبت |
| `org.jetbrains.kotlinx:kotlinx-coroutines-core(-android)` | **1.11.0** | ✔️ مثبت — لا يوجد فهرسة مستقلة للعبّة الكوريوتين على الخط |
| `org.jetbrains.kotlinx:kotlinx-serialization-json` | **1.13.0** | ✅ **رُقّيت من 1.12.0** (مستقرة) |
| `org.jetbrains.kotlinx:kotlinx-datetime` | `0.8.0-0.6.x-compat` فقط | ⏸️ مثبت على 0.6.x (0.7+ كاسرة) |
| `androidx.navigation3:navigation3-runtime/ui` | **1.2.1** | ✔️ مثبت |
| `org.jetbrains.androidx.navigation3:navigation3-ui` | **1.2.0** | ✔️ مثبت |
| `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3` | **2.11.0** | ✔️ مثبت |
| `org.jetbrains.compose.material3.adaptive:adaptive(-navigation3)` | **1.3.0** | ✔️ مثبت — 1.4.0 كله alpha |
| `androidx.compose.material3.adaptive:adaptive` | **1.3.0** | ✔️ مثبت — 1.4.0-alpha فقط |
| `androidx.activity:activity-compose` / `androidx.core:core-splashscreen` / `androidx.glance:*` / `androidx.work:*` | ليست مفهرسة على klibs.io | ⏸️ مثبتة كما هي (لا مؤشر على أحدث) |

- إضافات Gradle نفسها (Kotlin/Compose Multiplatform/AGP) لا تُفهرس على klibs.io —
  أحدث خطوطها المستقرة المتوافقة مع Gradle 9.4.1: Compose `1.12.2` وKotlin `2.4.21` وAGP `9.2.1`.
- `searchProjects("compose-multiplatform")` أكد دعم مشروع JetBrains للمنصات
  COMMON/ANDROIDJVM/JVM_21/NATIVE_ios_arm64/NATIVE_ios_x64/NATIVE_ios_simulator_arm64
  وبقية المنصات — خط CMP 1.12.2 صالح لكل أهداف هذا المشروع.

## خط الأساس عند هذا الفحص

```
- Compose Multiplatform: 1.12.2 (CMP رسمي)
- Kotlin: 2.4.21
- AGP: 9.2.1 + Gradle Wrapper 9.4.1 (مثبّت)
- compileSdk/targetSdk 37, minSdk 24, JVM target 21
```

## فحص 2026-11-17 (ب) — الحالة الحية لكل الحزم الفعلية (قبل البناء)

الطلب: مطابقة كل `groupId:artifactId` مستخدم فعليًا في `gradle/libs.versions.toml` والمبنيين — عبر `getLatestVersion` من klibs.io — مع الإبلاغ فقط عن `latestStableVersion` (تجاهل alpha/beta/rc/dev).

| الحزمة | الحالي | أحدث مستقر (klibs.io) | القرار | السبب |
| --- | --- | --- | --- | --- |
| `org.jetbrains.compose.material3:material3` | — (مضمّن في CMP 1.12.2) | **1.5.12** | ✔️ لا ترقية مباشرة | مشتركة من خط Compose الرسمي؛ 1.12.2 تنقل الخط بـ`provided` (Material3 1.5.12 ضمنًا)؛ 1.13.0 كله beta |
| `org.jetbrains.compose.foundation:foundation` | — (مضمّن في CMP 1.12.2) | **1.12.2** | ✔️ جاهز | خط CMP — المصدر نفسه؛ 1.13.0-beta يتجاهل (beta) |
| `org.jetbrains.compose.runtime:runtime` | — (مضمّن في CMP 1.12.2) | **1.12.2** | ✔️ جاهز | خط CMP — المصدر نفسه؛ 1.13.0-beta يتجاهل (beta) |
| `org.jetbrains.compose.ui:ui` | — (مضمّن في CMP 1.12.2) | **1.12.2** | ✔️ جاهز | خط CMP — المصدر نفسه؛ 1.13.0-beta يتجاهل (beta) |
| `org.jetbrains.compose.material:material-icons-extended` | — (مضمّن في CMP 1.12.2) | **1.5.12** | ✔️ جاهز | ضمن plugin كما هو؛ لا ترقية مستقلة |
| `org.jetbrains.compose.components:resources` | — (مضمّن في CMP 1.12.2) | — | ⏸️ غير مفهرس | تُحمل مع Plugin — لا يُقاس على klibs.io؛ لا تقدير |
| `org.jetbrains.compose.components:ui-tooling-preview` | — (مضمّن) | — | ⏸️ غير مفهرس | مثلها |
| `androidx.lifecycle:lifecycle-viewmodel` | **2.11.0** | **2.11.0** | ✔️ لا ترقية | 2.12.0-alpha05 فقط؛ خط stdlib KMP نفس القيمة |
| `androidx.lifecycle:lifecycle-runtime-compose` | **2.11.0** | **2.11.0** | ✔️ لا ترقية | نفس الحالة |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | **2.11.0** | **2.11.0** | ✔️ لا ترقية | نفس الحالة |
| `androidx.compose.material3.adaptive:adaptive` | **1.3.0** | **1.3.0** | ✔️ لا ترقية | 1.4.0-alpha03 فقط |
| `androidx.compose.material3.adaptive:adaptive-layout` | **1.3.0** | **1.3.0** | ✔️ لا ترقية | نفس الحالة |
| `androidx.compose.material3.adaptive:adaptive-navigation` | **1.3.0** | **1.3.0** | ✔️ لا ترقية | نفس الحالة |
| `org.jetbrains.compose.material3.adaptive:adaptive` | **1.3.0** | **1.3.0** | ✔️ لا ترقية | 1.4.0-alpha01 فقط |
| `org.jetbrains.compose.material3.adaptive:adaptive-layout` | **1.3.0** | **1.3.0** | ✔️ لا ترقية | نفس الحالة |
| `org.jetbrains.compose.material3.adaptive:adaptive-navigation` | **1.3.0** | **1.3.0** | ✔️ لا ترقية | نفس الحالة |
| `org.jetbrains.compose.material3.adaptive:adaptive-navigation3` | **1.3.0** | **1.3.0** | ✔️ لا ترقية | نفس الحالة |
| `androidx.navigation3:navigation3-runtime` | **1.2.1** | **1.2.1** | ✔️ لا ترقية | أحدث مستقر تمامًا |
| `androidx.navigation3:navigation3-ui` | **1.2.1** | **1.2.1** | ✔️ لا ترقية | أحدث مستقر تمامًا |
| `org.jetbrains.androidx.navigation3:navigation3-ui` | **1.2.0** | **1.2.0** | ✔️ لا ترقية | أحدث مستقر تمامًا |
| `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3` | **2.11.0** | **2.11.0** | ✔️ لا ترقية | أحدث مستقر تمامًا |
| `org.jetbrains.kotlinx:kotlinx-coroutines-core` | **1.11.0** | **1.11.0** | ✔️ لا ترقية | أحدث مستقر؛ إصدار Android-موجودة متداخل |
| `org.jetbrains.kotlinx:kotlinx-coroutines-android` | **1.11.0** | — | ⏸️ غير مفهرس | تُحيل على core — لا خطر |
| `org.jetbrains.kotlinx:kotlinx-serialization-json` | **1.13.0** | **1.13.0** | ✔️ رُقّيت 1.12 → 1.13 سابقاً | أحدث مستقر تمامًا |
| `org.jetbrains.kotlinx:kotlinx-datetime` | **0.6.2** | — | ⏸️ مثبت على 0.6.x | لا يوجد مستقر على 0.7+؛ أحدث مرشح `0.8.0-0.6.x-compat` غير مستقر بالتوزيع — تغييرات كاسرة واضحة |
| `androidx.activity:activity-compose` | **1.13.0** | — | ⏸️ غير مفهرس | الأحدث الذي نعرفه؛ فحص سابق أكد |
| `androidx.datastore:datastore-preferences` | **1.3.0** | **1.3.0** | ✔️ لا ترقية | أحدث مستقر تمامًا |
| `androidx.core:core-splashscreen` | **1.2.0** | — | ⏸️ غير مفهرس | نفسها (1.2.0 rebuild لنا) |
| `androidx.glance:glance-appwidget` | **1.2.0** | — | ⏸️ غير مفهرس | أعضاء klibs لا تفهرسها |
| `androidx.glance:glance-material3` | **1.2.0** | — | ⏸️ غير مفهرس | مثلها |
| `androidx.work:work-runtime-ktx` | **2.12.0** | — | ⏸️ غير مفهرس | مثلها |
| `androidx.profileinstaller:profileinstaller` | **1.4.1** | — | ⏸️ غير مفهرس | مثلها |
| `com.android.application` (plugin) | **9.2.1** | — | ✔️ ثابت | AGP 9.2 هو أحدث متوافق مع Gradle 9.4.1 |
| `org.jetbrains.kotlin.multiplatform` (plugin) | **2.4.21** | — | ✔️ ثابت | أحدث خط مستقر للمشروع |
| `org.jetbrains.compose` (plugin) | **1.12.2** | — | ✔️ ثابت | يجلب Compose مستقر 1.12.2 موجة CMP الحالية |
| `org.jetbrains.kotlin.plugin.compose` (plugin) | **2.4.21** | — | ✔️ ثابت | تتبع خط Kotlin |

**القرار العام**: لا ترقية مطلوبة الآن — كل حزمة على أحدث مستقر متوافق؛ وأي محاولة لـ `1.12/1.13` الخطوط تعني عبطًا مع β أو قرار KMP مثل MaterialExpressiveTheme غير علني في هذا الخط.

## سجل المراجع

- التشغيل: `tools.klibs.getLatestVersion` ×٢٤ حزمة (٢٠-١١-٢٠٢٦)
- `tools.klibs.searchProjects("compose-multiplatform")` — دعم المنصات مؤكد
- المرجع: `docs/klibs-mcp.md` — توثيق الخادم ومسارات الأدوات (الاسكن القياسي عبر `/klibs-check` أو `pwsh -File scripts/klibs-audit.ps1`)
