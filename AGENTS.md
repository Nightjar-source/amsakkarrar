# AGENTS.md — دليل الوكلاء لمشروع إمساكية كرار

## نظرة عامة

تطبيق **إمساكية كرار 2026** (رمضان) مبني بـ **Kotlin Multiplatform + Compose
Multiplatform**. الواجهة والمنطق الفلكي في `shared`، وتغليف أندرويد والودجت في
`androidApp`. توجد أهداف iOS أيضًا (`iosX64`, `iosArm64`, `iosSimulatorArm64`).

```
shared/src/commonMain   الواجهات، المحرك الفلكي، الأدعية، الثيم
shared/src/androidMain  تنفيذ الصوت على أندرويد
shared/src/iosMain      تنفيذ الصوت وMainViewController على iOS
androidApp/             MainActivity، Glance widget، الموارد
gradle/libs.versions.toml  المصدر الوحيد لإصدارات الاعتماديات
```

## أوامر البناء

```powershell
.\gradlew.bat :shared:compileDebugKotlinAndroid --stacktrace
.\gradlew.bat :androidApp:assembleDebug --stacktrace
.\gradlew.bat :androidApp:assembleRelease --stacktrace
```

## كوتلن وCompose

- Kotlin `2.4.21` — Compose Multiplatform `1.12.2` — AGP `9.2.1`
- Gradle Wrapper `9.4.1` **مثبّت**؛ لا تغيّره دون طلب صريح.
- `compileSdk`/`targetSdk` 37، `minSdk` 24، JVM target 21.
- استخدم `libs.*` دائمًا؛ لا تكتب إحداثيات حرفية في ملفات البناء.
- كل اعتمادية في `commonMain` يجب أن تدعم Android وأهداف iOS الثلاثة.
- `kotlinx-datetime` يبقى على خط `0.6.x` (0.7+ تغييرات كاسرة).

## klibs.io MCP

المشروع موصول بخادم klibs.io الرسمي (`opencode.jsonc` → `mcp.servers.klibs`).
قبل أي إضافة أو ترقية اعتمادية:

1. استخدم أداة `getLatestVersion` للتحقق من `latestStableVersion`.
2. استخدم `searchProjects` للتأكد من دعم المنصات عند إضافة مكتبة جديدة.
3. اعتمد الإصدارات المستقرة فقط.
4. سجّل النتيجة في `docs/dependency-audit.md`.

اقرأ `docs/klibs-mcp.md` للتفاصيل، أو نفّذ `/klibs-check`.

## قواعد عامة

- حافظ على تعليقات الكود العربية الموجودة وأسلوب التسمية الحالي.
- لا ترفع `local.properties` أو `androidApp/Nightjar.keystore` أو أي أسرار.
- ملف `keystore_base64.txt` مخصص لـ CI فقط؛ لا تضع التوكنات في المستودع.
- بعد أي تغيير في الاعتماديات، شغّل على الأقل `:shared:compileDebugKotlinAndroid`.
