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
| `org.jetbrains.kotlinx:kotlinx-serialization-json` | 1.8.1 | 1.12.0 | ✅ ترقية |
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

```powershell
# AGP 9 لمشروع KMP: اسم المهمة هو compileAndroidMain وليس compileDebugKotlinAndroid
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
