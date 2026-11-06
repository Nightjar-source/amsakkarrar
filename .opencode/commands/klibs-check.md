---
description: فحص اعتماديات المشروع مقابل أحدث إصدار مستقر على klibs.io
---

افحص اعتماديات Kotlin Multiplatform في هذا المشروع باستخدام خادم klibs.io MCP
(`getLatestVersion` و `searchProjects` عند الحاجة).

الملفات المطلوب قراءتها:

- `gradle/libs.versions.toml`
- `shared/build.gradle.kts`
- `androidApp/build.gradle.kts`
- `gradle/wrapper/gradle-wrapper.properties`

المطلوب:

1. استخرج كل `groupId:artifactId` مستخدم فعليًا.
2. استدعِ `getLatestVersion` لكل حزمة.
3. قارن الإصدار الحالي بـ `latestStableVersion` فقط. تجاهل alpha/beta/rc/dev.
4. تحقق من توافق أي ترقية مع: Kotlin 2.4.21، Compose Multiplatform 1.12.2،
   AGP 9.2.1، Gradle 9.4.1 (ثابت)، compileSdk 37، وminSdk 24.
5. حدّث `docs/dependency-audit.md` بجدول: الحزمة | الحالي | أحدث مستقر | القرار | السبب.
6. طبّق فقط الترقيات الآمنة (patch/minor متوافقة)، واترك الترقيات الكاسرة
   موثّقة كتوصية دون تنفيذ.
7. تحقق من النتيجة:

```powershell
.\gradlew.bat :shared:compileDebugKotlinAndroid --stacktrace
```

لا تلمس `gradle/wrapper/gradle-wrapper.properties` ولا ترفع `local.properties`.
