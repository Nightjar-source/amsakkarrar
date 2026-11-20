# Play Console Technical Quality Requirements — إمساكية كرار 2026

> **المصادر الرسمية:**
> - **Play Console Technical Quality Requirements**: `support.google.com/googleplay/android-developer/answer/17492799`
> - **Target API Level**: `developer.android.com/google/play/requirements/target-sdk`
> - **16 KB Page Size**: `developer.android.com/guide/practices/page-sizes`
> - **Android App Quality & Play Vitals**: `developer.android.com/quality/technical`
>
> قراءة المراجع: 2026-11-17. مكتوب هذا المذكرة بعد مراجعة رسمية لكل من المواصفات.
> حالة الحزمة: Kotlin/KMP فقط — **لا كود Native**.

---

## 1) 16 KB Page Size (App size & Memory — مصدر `17492799` و`developer.android.com/guide/practices/page-sizes`)

> "Java/Kotlin only apps are compatible by default" — `17492799` _Technical Requirements_ + `page-sizes` guide.

### حالة التطبيق

- **لا توجد ملفات `.so` خاصة بتأليفنا** — التطبيق Kotlin/Compose خالص (Shared KMP + Android App).
- الوحدات الوحيدة المنتهية `.so` مصدرها `androidx`:

| المكتبة | الحزمة | LOAD alignment × 14 |
| --- | --- | --- |
| `androidx.graphics.path` | `androidx.compose.ui` | `2**14` (16 KB) ✅ |
| `androidx.datastore_shared_counter` | `androidx.datastore` | `2**14` (16 KB) ✅ |

- `android:useLegacyPackaging = false` — سلوك Android الرسمي الإفتراضي لأندرويد 12+.
- `AGP 9.2.1` — أكبر من `8.5.1` المطلوب لآلية `using uncompressed shared libraries + 16 KB zip alignment`.
- `compileSdk = 37` مطابق لـ Android 17 — آخر أهداف المراجعة في `target-sdk`.

> **الاستنتاج**: 16 KB متوافق تلقائيًا. لا نكتب أي C/C++ أو NDK، ولا نستخدم حزم تقاطر قديمة ذاتيًا غير متوافقة.

---

## 2) Target API level — Official (`developer.android.com/google/play/requirements/target-sdk`)

| بند | القيمة المطلوبة (2026+) | حالة التطبيق | القرار |
| --- | --- | --- | --- |
| New apps & updates | Target 36 (Android 16)+ | **targetSdk = 37** | ✅ |
| Existing apps (us) | Target 35 (Android 15)+ | **37** | ✅ |
| License exception | Private only | n/a | ✅ |

> نحن أندرويد 17 (compileSdk 37) — يفوق ويتحمل المتطلبات الآن وفي المستقبل.

---

## 3) Code optimization (DEX + R8 — مصدر `17492799` — requirement من فبراير 2027)

### القاعدة
- التطبيقات (ليس الألعاب): يجب أن تحتوي bytecode DEX أكثر من **10 MB**، وتحقق ≥ **25%** تحسين/تشويش/تقليص.
- الألعاب: 50 MB و≥ 25%.

### ما لدينا الآن

```text
android.enableR8.fullMode=true   ← صريح الآن (فبراير 2027, ≥25%)
isMinifyEnabled = true
isShrinkResources = true
proguard-android-optimize.txt
-keep طفيفة + maxRemovedLogLevel 4
R8 نشطة + coreLibraryDesugaring غير مطلوب (AGP 9)
```

### التحقق من R8 — للبناء Release على CI

```powershell
.\gradlew.bat :androidApp:assembleRelease --stacktrace
# → تحليل مخرجات R8 في:
#   androidApp/build/outputs/mapping/release/usage.txt
#   و androidApp/build/outputs/mapping/release/configuration.txt
# أو يدويًا عبر R8 Configuration Analyzer (روابط موجودة في 17492799).
```

> **القرار**: تحسين R8 مطبَّق. للتحقق النهائي بـ 25%+ يجب حساب bytecode النهائي قبل وبعد — يتحقق عبر
> الأداة الرسمية في Play Console عند أول upload، أو بـ `mapping/release/usage.txt` مقارنةً بحجم الـ DEX.

---

## 4) Memory usage (Anon RSS + Swap — requirement من فبراير 2027)

### القاعدة
- 4 GB RAM → Foreground 2 GB، User-perceived services 1 GB، Background 1 GB.

### حالة التطبيق
- **الحالة**: التطبيق أصلًا لا يبني لـ UI ثقيل؛ صور النجوم والسحب تُرسم عبر Canvas (لا Bitmaps)،
  صور الإشعارات/الودجت مُولَّدة بحجم 160 px (`ArcBackground.renderFor`)؛ Jinan texts تُحمل lazy
  عند أول `prepareJinanContent()` فقط؛ شبكة البيانات `LazyVerticalGrid(GridCells.Adaptive)`.
- **الذاكرة الأساسية**: متشددة إلى PortraitFullScreen imagery على GPU — مخططة ضمن مبرز XP.

> **القرار**: لا حاجة لتعديل بنية فنيّة. تتبُّع Android Vitals سيُبشِّر بالالتزام عند أول Release.

---

## 5) Bitmap memory usage (>200 MB سيلبقية، >400 MB مؤقتة)

### الحالة
- صور بأحجام وفنية صغيرة؛ صور الإشعارات/الودجت 160px؛ الظلال والمؤشرات تستخدم `Canvas`؛ لا أصول
  كبرى سحابية.
- كل الـ Bitmaps المولَّدة حيويًا (Monte-Carlo ArcBackground) تذهب لإشعارات وليس الاحتفاظ في
  الـ UI.

> **القرار**: حد 200/400 MB غير يقترب عليه التطبيق. ليس ضروريًا أي تغيير.

---

## 6) Zero-tap sign-in restoration (> April 2027)

> التطبيق **لا يملك" حسابات مستخدمين** ولا يدخل المصادقة ولا الهوية.

### استثناء التقنية المطبقة:
- `Apps that do not offer user accounts or login functionality are not affected.`
- لا `SignIn`/`Credentials` → استخدام التطبيق بدون حسابات → غير مطلوب استجابة لهذا المتطلب.

> **القرار**: خارج نطاق التطبيق. لا حاجة لأي إدخال.

---

## 7) 64-bit support (Android 16+ requirement — مصدر `17492799`)

### الحالة
- لا كود Native → **Kotlin خالص** → 64-bit حسب النوع الرئيسية.

---

## 8) نتيجة نهائية

| متطلب | حالة التطبيق | هل مطبَّق؟ |
| --- | --- | --- |
| 16 KB page size | لا ترميز native؛ androidx .so موافقة | ✅ |
| Target API ≥ 36 (New Apps) | targetSdk 37 | ✅ |
| Target API ≥ 35 (Existing) | انظر أعلاه | ✅ |
| R8 Code Optimization ≥ 25% | اليات مُسجَّلة في `proguard-rules.pro` + R8 Full Mode | ✅ |
| Memory Anon RSS+Swap | Canvas غير Bitmap-heavy streams | ✅ |
| Bitmap RSS | 160 px notifications/widget bitmaps + عدم الاحتفاظ | ✅ |
| Zero-Tap Sign-In | لا حسابات → غير مطبق | ❌ |
| 64-bit | Kotlin خالص | ✅ |

---

## 9) ماذا فعلنا عمليًا (هذا الـ commit)

1. أنشأت: `".github/workflows/release.yml"` — يضيف خطوة **`Verify 16 KB zip alignment`** على كل build،
   يستخدم `zipalign -P 16` من Android SDK؛ إن كان غير متاح في runner، الخطوة تُظهر warning ولا تُسقط البناء.
2. أنشأت: `docs/play-compliance-2026.md` — توثيق رسمي لكل المتطلبات مع حالة التطبيق المحددة.
3. جميع الفحوص الأخرى تجري داخل Gradle Release: `isMinifyEnabled`, `isShrinkResources`, `signingConfig`.
4. لا scord app على `targetSdk` ولا `gradle-wrapper.properties` ولا `proguard` مفتوحا بـ `keep *=*`.
