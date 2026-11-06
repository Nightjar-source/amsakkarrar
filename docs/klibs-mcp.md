# klibs.io MCP Server — تكامل كامل مع المشروع

هذا المشروع موصول بخادم **klibs.io MCP** الرسمي من JetBrains، وهو فهرس حي لمكتبات
Kotlin Multiplatform. يُستخدم للتحقق من أحدث الإصدارات المستقرة، ومن دعم المكتبات
لمنصات المشروع (Android + iOS)، قبل إضافة أي اعتمادية أو ترقيتها.

- الخادم: `https://api.klibs.io/mcp`
- النقل: MCP Streamable HTTP (stateless)
- المصادقة: لا يحتاج مصادقة
- المصدر الرسمي: <https://github.com/JetBrains/klibs-io/blob/master/integrations/mcp/README.md>
- صفحة Compose Multiplatform: <https://klibs.io/project/JetBrains/compose-multiplatform>

## ما تمت إضافته

| الملف | الدور |
| --- | --- |
| `opencode.jsonc` | تعريف الخادم داخل OpenCode تحت `mcp.servers.klibs` مع `oauth: false` |
| `.opencode/skills/klibs-multiplatform/SKILL.md` | مهارة تُخبر الوكيل متى وكيف يستخدم klibs (تُكتشف تلقائيًا) |
| `.opencode/commands/klibs-check.md` | أمر `/klibs-check` لفحص كل الاعتماديات |
| `.opencode/agents/dependency-auditor.md` | وكيل فرعي للمراجعة فقط، بلا صلاحية تعديل أو تنفيذ أوامر |
| `.vscode/mcp.json` | إعداد VS Code |
| `.cursor/mcp.json` | إعداد Cursor |
| `.junie/mcp/mcp.json` | إعداد Junie على مستوى المشروع |
| `scripts/klibs-audit.ps1` | فحص سريع من سطر الأوامر عبر نفس الخادم |
| `docs/dependency-audit.md` | نتيجة الفحص الأخير وقرارات الترقية |

> IntelliJ IDEA / AI Assistant: أضف نفس الكتلة يدويًا من
> **Settings | Tools | AI Assistant | Model Context Protocol (MCP)**:
>
> ```json
> { "mcpServers": { "klibs": { "url": "https://api.klibs.io/mcp" } } }
> ```

## الأدوات المتاحة

| الأداة | الوظيفة |
| --- | --- |
| `searchProjects` | بحث بالكلمات المفتاحية مع فلاتر `platforms` و`targetGroupFilters`، وإرجاع الحزم مع `latestVersion` و`latestStableVersion` |
| `getLatestVersion` | أحدث إصدار وأحدث إصدار مستقر لحزمة `groupId:artifactId` |

في OpenCode (Code Mode) تُستدعى كـ:

```js
await tools.klibs.getLatestVersion({ groupId: "org.jetbrains.kotlinx", artifactId: "kotlinx-coroutines-core" })
await tools.klibs.searchProjects({ query: "calendar", platforms: ["common", "androidJvm", "native"] })
```

## الاستخدام داخل OpenCode

1. `/klibs-check` — فحص شامل وتحديث `docs/dependency-audit.md`.
2. اطلب من الوكيل: «استخدم مهارة klibs-multiplatform للتحقق من آخر إصدار مستقر لـ …».
3. الوكيل الفرعي `dependency-auditor` متاح للمراجعة القراءة فقط.

تأكد من الاتصال عبر:

```powershell
opencode mcp list
```

يجب أن يظهر `klibs  connected`.

## الفحص من سطر الأوامر

```powershell
pwsh -File scripts/klibs-audit.ps1
```

السكربت يقرأ قائمة الحزم من `gradle/libs.versions.toml` قدر الإمكان ثم يستدعي
`getLatestVersion` ويعرض جدولًا بالإصدار الحالي وأحدث إصدار مستقر.

## سياسة الإصدارات

- نعتمد `latestStableVersion` فقط، ولا نرقّي إلى alpha/beta/rc دون طلب صريح.
- Gradle Wrapper مثبّت على **9.4.1**، ولذلك AGP الأحدث المتوافق هو **9.2.1**؛
  AGP 9.3+ يتطلب Gradle 9.5+ وAGP 9.4+ يتطلب Gradle 9.6+.
- `kotlinx-datetime` يبقى على خط **0.6.x** لأن 0.7+ يتضمن تغييرات كاسرة.
- كل اعتمادية في `commonMain` يجب أن تدعم `androidJvm` و`iosArm64`
  و`iosX64` و`iosSimulatorArm64`.

## استكشاف الأخطاء

| العرض | الحل |
| --- | --- |
| `needs authentication` | الخادم بلا مصادقة؛ تأكد من `"oauth": false` ثم أعد التحميل بـ `/reload` |
| الأدوات لا تظهر | نفّذ `opencode mcp list` وتأكد من حالة `connected` |
| `packageFound: false` | تحقق من `groupId` و`artifactId`؛ الخادم يفهرس حزم KMP المنشورة فقط |
| `latestStableVersion: null` | لا يوجد إصدار مستقر لهذه الحزمة؛ أبقِ الإصدار الحالي |
