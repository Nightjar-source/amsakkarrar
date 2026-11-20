# سياق المهمة — 2026-11-13 (بعد جلسة إنتاج الفيديوهات وإعادة ضبط القناتين)

## الخط الزمني للطلبات
1. «اكمل بناء» → أُكمل بناء البثوث كلها (ep11-ep36: صوت Fahed، كابسن مزامنة ميكروثانية، رسوم Pollinations/Commons)
2. «انشر الآن وجرب» → نُشر ep31 كـعام (AR fE3l_a5WKkk + EN CMu9_Yv6yqA) ورُفع الكثير خاص
3. «الفيديو رسومات البومة سيء → احذف كل الفيدوهات الخاص وعدل الفيدوهات» → حُذفت كل نسخ الرسوم، وكل الفيديوهات الخاصة على القناتين عادت إلى **توثيقيات تصويرية حقيقية** فقط.
4. اليوم (2026-11-13): القناتان تعرضان **حصريًا محتوى Tavaqqus/وثائقي** (مصانع، سنا، NASA، زجاج، غربة → Bayern، حكايات) — كله لقطات Wikimedia Commons مرخّصة.

## المحتوى على القناتين الآن (كافح)
### KarBerIq-r9w (AR):
- A4 — كيف تُصنع ورقة A4
- الذهب — من الصخور إلى سبيكة
- الفولاذ — كيف يُصنع المعدن
- الرقائق — كيف تُصنع رقائق الذكاء الاصطناعي
- خمسة أماكن لا تصدقها (ep05 v2) + «وري @base.profitنا**
- كيف تصنع سامسونج؟ (Z Fold)
- مصنع نسيج (#tid) —ep06
- F-مile ميل فرنسا (ep07-france-mill) — AR ID 8ndiMKdmnrw
- أكبر ملعب في أمريكا (ep10) — AR videoId yYwKalOlmwI

### catlifelive (EN):
- The Best Factory Film (Cotton Mill 1920) — صور مكررة لعدد كبير من الجماهير
- How a Handmade Glass is Born (German factory since 1979)
- Inside NASA Rocket Factory
- 5 Places You Won't Believe Exist
- How AI Chips Are Made (رقائق الذكاء)
- How Steel Is Made
- From Rock to Gold Bar
- How A4 Paper Is Actually Made
- Inside the Secret Robot (Samsung Z Fold)
- French Mill (ep07-france-mill) — EN videoId BmjqMsFHGGM

## الحالة التقنية النهائية للمصنع
- **صوت:** `ar-KW-FahedNeural` للعربي و `en-US-BrianNeural` للإنجليزي (لا مزيد من المصري/المولّد)
- **المزامنة:** ميكروثانية WordBoundary — عرض الكابسن مطابقة ميكروثانية لكل كلمة (فيديو دقة 1080x1920)
- **الرسوم المولّدة VS حقيقي:** حُذفت جميع مقاطع الكرتون (المنحرفة) من القناتين. لم توضع بدائل كرتونية — فقط لقطات Wikimedia Commons.
- **الأدوات:** `tools/yt.py` (يتحقق/يرفع/يغيّر خصوصية/يحذف) + `tools/check_channels.py` + `tools/delete_videos.py` + كل أدوات البناء الأصلي.
- **MoneyPrinterTurbo:** مشروع مفتوح 143k★ (MIT) — يحل محل هذا المشروع في المرة التالية
 تستعقد
## TODO للمستخدم
- ep10-stadium (أكبر ملعب) — إنجليزي لا يزال يُبنى في الخلفية — سيرفع عام عند الانتهاء.
- إقناع المستخدم بـ **MoneyPrinterTurbo** بدلاً من هذا المشروع (يتيح مشاهد أجمل، صوت Wendying الفاخر، موسيقى أصولية).
