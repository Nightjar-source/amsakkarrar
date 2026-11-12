# النظام الحركي — Spring / M3 Expressive

آخر تحديث: 2026-11-12 (v1.5.9 + احترام «تقليل الحركة»)

## المبدأ

كل حركة غير عرضية في التطبيق نابضية (Spring) بنوعين حسب دلالة M3 Expressive:

- **Spatial** — للانتقالات والأحجام والانزلاق (مرونة 0.8).
- **Effects** — للتلاشي والشفافية والألوان (بلا ارتداد، استجابة أسرع).

القيم مطابقة لأفكار رموز Expressive في Material 3 (Springs سريع/بطيء):

| الرمز | القيمة | الاستخدام |
|---|---|---|
| `SpatialStiffnessDefault` | 380 | الانتقال العام |
| `SpatialStiffnessFast` | 800 | ضغط/ضغطات سريعة، دخول/خروج |
| `SpatialStiffnessSlow` | 200 | ورقة/حوار (بطيئة وهادئة) |
| `EffectsStiffnessDefault` | 1600 | لون/تلاشٍ عام |
| `EffectsStiffnessFast` | 3800 | تلاشٍ سريع |
| `EffectsStiffnessSlow` | 800 | تلاشي بطيء |
| `SpatialDamping` / `EffectsDamping` | 0.8 / 1.0 | مكاني مرن / تأثيرات بلا ارتداد |

المنحنى المخصص للحالات الخاصة: `Fluid = cubic-bezier(0.22, 1, 0.36, 1)`.

## احترام «تقليل الحركة» (Reduce Motion) — إلزامي

- **أندرويد**: `Settings.Global.ANIMATOR_DURATION_SCALE == 0` عبر `ContentObserver`
  (يتفعّل لحظيًا، أو「إزالة الحركات」من خيارات المطوّر).
- **iOS**: `UIAccessibilityIsReduceMotionEnabled()`.
- القاعدة: الحركات المكانية والبيئية اللانهائية تُقطع فورًا (snap) أو تُجمّد؛
  التلاشي/تغيّر اللون يبقى (لا يُسبب دوارًا).
- التنفيذ:
  - `rememberReduceMotion()` — توقع/تنفيذ لكل منصة (`ui/platform/ReduceMotion.*`).
  - `motionSpatial()` / `motionEffects()` — بدائل تركيبية جاهزة للاستدعاء المباشر في مواضع التركيب.
  - `spatialSpec(reduceMotion)` / `effectsSpec(reduceMotion)` — نسخ lambda غير تركيبية
    (`transitionSpec` في `AnimatedContent`).
  - `ambientMotionEnabled()` — للحركات البيئية اللانهائية (نجوم/سحاب/مطر/رادار/نبض).
- ممنوع استدعاء دوال `@Composable` داخل lambdas غير تركيبية مثل `transitionSpec`.

## قواعد الأداء

1. كل الحركة البصرية عبر `graphicsLayer { }` مع قراءة `State.value` **داخل** الكتلة
   (لا قراءات state في التركيب) حتى لا تُعاد عملية التركيب 60fps.
2. مرّر دوال `rotation = { ring.value }` بدل قيم ممررة جاهزة للمحيطات المتحركة.
3. لا `scale` في انتقالات الشاشة (shared-axis): انزلاق قصير + تلاشٍ فقط — أقل بكسل يُعاد رسمه.
4. مؤشرات الطقس تُبنى فقط عند الحاجة (لا رسوم مطر في يوم صافٍ غائم فقط).
5. `AnimatedVisibility` بلا شروط isNotEmpty — دائمًا نفس نمط enter/exit إذا أمكن.

## مراجع مصمَّمة

- `kar tokens` في `KararTokens.Motion` — المصدر الوحيد للقيم.
- M3 Expressive motion scheme: نابضان Spatial/Effects بتسميات Default/Fast/Slow.
- `kmp-compose-animation` skill: تقدير التكرار — لا حركة للمهام المتكررة 100+ مرة/يوم؛
  الحركات الدخول/الخروج للأوراق/الحوارات فقط.
- skydoves/compose-animations: MotionMaterial وثبات النوافذ `Dialog(decorFitsSystemWindows = false)`.

## نقاط معتادة

- شريط التنقل/الجانبي، الكبسولات العائمة، الأدعية، الودجت، الحوارات، الشيت،
  القوس السماوي، العدّاد، قاعدة التبويق، المشاركة عبر التوابع المذكورة أعلاه.
