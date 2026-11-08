/**
 * تصحيح أسماء المدن العربية في cities.json دون إعادة توليد كامل.
 *
 * لماذا سكربت منفصل؟
 * - GeoNames يختار أول بديل عربي وقد يكون مشوّهًا (كةربالا، الرمادى، السماوه).
 * - بعض المدن الكبرى بلا بديل عربي أصلًا (Lusail، Hafar Al-Batin).
 * - إعادة التوليد بـPowerShell بطيئة جدًا (ترتيب 70 ألف صف).
 *
 * السكربت idempotent: تشغيله مرتين لا يغيّر شيئًا في المرة الثانية.
 * الخرائط نفسها موجودة في scripts/build-cities-json.ps1 لتوليد مستقبلي.
 */
const fs = require('fs');
const path = require('path');

const FILE = path.resolve(
  __dirname,
  '..',
  'shared',
  'src',
  'commonMain',
  'composeResources',
  'files',
  'cities.json'
);

const IRAQ = 'العراق';

/** أسماء مشوّهة أو لاتينية — المفتاح: الدولة|الاسم الحالي */
const nameFixes = new Map([
  // ---------- العراق ----------
  [`${IRAQ}|كةربالا`, 'كربلاء'],
  [`${IRAQ}|الرمادى`, 'الرمادي'],
  [`${IRAQ}|السماوه`, 'السماوة'],
  [`${IRAQ}|Abū al-Kahṣīb`, 'أبو الخصيب'],
  [`${IRAQ}|Hajiawa`, 'حاجياوة'],
  [`${IRAQ}|Shelādiz`, 'شيلاديز'],
  [`${IRAQ}|Ad Dujayl`, 'الدجيل'],
  [`${IRAQ}|Nāḩiyat al Fuhūd`, 'الفهود'],
  [`${IRAQ}|AlZwya`, 'الزوية'],
  [`${IRAQ}|قلعة سکر`, 'قلعة سكر'],
  [`${IRAQ}|سەلاحەدين`, 'صلاح الدين'],
  [`${IRAQ}|ئاكرێ`, 'عقرة'],
  [`${IRAQ}|بيوك`, 'عقرة'],
  [`${IRAQ}|كفرى`, 'كفري'],
  [`${IRAQ}|دیرلوك منطقه نهیلي`, 'ديرلوك'],
  [`${IRAQ}|ابي غرق`, 'أبو غرق'],
  [`${IRAQ}|كستران ستي اسماعيل`, 'كسنزان'],

  // ---------- أسماء بأحرف كردية/فارسية/أردية ----------
  ['البحرين|شهرک عیسی', 'مدينة عيسى'],
  ['سوريا|اردوگاه یرموک', 'مخيم اليرموك'],
  ['فلسطين|تلکرم', 'طولكرم'],
  ['مصر|ئەسکەندەریە', 'الإسكندرية'],
  ['تونس|بنڤردان', 'بنقردان'],
  ['الجزائر|مسکره', 'معسكر'],
  ['السودان|رباک', 'ربك'],
  ['السودان|ام کداده', 'أم كدادة'],
  ['الصومال|مرکه', 'مركة'],
  ['الصومال|یگوری', 'يغوري'],
  ['الصومال|اسکوشوبان', 'أسكوشوبان'],
  ['الإمارات|دبئی', 'دبي'],
  ['الإمارات|رأس الخیمه', 'رأس الخيمة'],
  ['الإمارات|دبی مارینا', 'دبي مارينا'],
  ['الكويت|احمدی', 'الأحمدي'],
  ['جيبوتي|جبوتی', 'جيبوتي'],
  ['مصر|استان فیوم', 'الفيوم'],
  ['الجزائر|قسطنطینہ', 'قسنطينة'],
  ['الجزائر|ستیف', 'سطيف'],
  ['الجزائر|البلیده', 'البليدة'],
  ['الجزائر|تبسہ', 'تبسة'],
  ['السودان|واد مدنی', 'ود مدني'],
  ['السودان|الدویم', 'الدويم'],
  ['السودان|زالنجای', 'زالنجي'],
  ['السودان|تالودی', 'تلودي'],
  ['قطر|ارریان', 'الريان'],
  ['قطر|اززاعین', 'الظعاين'],
  ['قطر|اش شہانیہ', 'الشحانية'],
  ['موريتانيا|نوادھیبو', 'نواذيبو'],
  ['موريتانيا|تفراغ زینه', 'تفرغ زينة'],
  ['السعودية|طریف', 'طريف'],
  ['جزر القمر|مرمانی', 'مرماني'],

  // ---------- مدن كبرى بلا بديل عربي في GeoNames ----------
  ['السعودية|Sulţānah', 'السلطانة'],
  ['السعودية|Hafar Al-Batin', 'حفر الباطن'],
  ['السعودية|Unaizah', 'عنيزة'],
  ['الإمارات|Business Bay', 'الخليج التجاري'],
  ['الإمارات|Dubai Investments Park', 'مجمع دبي للاستثمار'],
  ['الإمارات|International City', 'المدينة الدولية'],
  ['الإمارات|Al Majaz', 'المجاز'],
  ['قطر|Lusail', 'لوسيل'],
  ['عُمان|Şaḩam', 'صحم'],
  ['لبنان|Ra’s Bayrūt', 'رأس بيروت'],
  ['مصر|Madīnat an Naşr', 'مدينة نصر'],
  ['مصر|Al Khuşūş', 'الخصوص'],
  ['مصر|Kom Ombo', 'كوم أمبو'],
  ['مصر|6th of October City', 'مدينة السادس من أكتوبر'],
  ['مصر|Al Ḩawāmidīyah', 'الحوامدية'],
  ['مصر|Sinnūris', 'سنورس'],
  ['ليبيا|Qaşr Bin Ghashīr', 'قصر بن غشير'],
  ['ليبيا|Tājūrā’', 'تاجوراء'],
  ['تونس|Sīdī Ḥusayn', 'سيدي حسين'],
  ['تونس|Sakiet ed Daier', 'ساقية الدائر'],
  ['تونس|El Mourouj', 'المروج'],
  ['تونس|Sakiet ez Zit', 'ساقية الزيت'],
  ['الجزائر|El Eulma', 'العلمة'],
  ['الجزائر|Aïn Beïda', 'عين البيضاء'],
  ['المغرب|Temara', 'تمارة'],
  ['المغرب|Salé Al Jadida', 'سلا الجديدة'],
  ['المغرب|Ait Melloul', 'أيت ملول'],
  ['المغرب|Dar Bouazza', 'دار بوعزة'],
  ['المغرب|Fès al Bali', 'فاس البالي'],
  ['المغرب|Berrechid', 'برشيد'],
  ['المغرب|Khemisset', 'الخميسات'],
  ['المغرب|Inezgane', 'إنزكان'],
  ['المغرب|Ksar El Kebir', 'القصر الكبير'],
  ['المغرب|Taourirt', 'تاوريرت'],
  ['المغرب|Bouskoura', 'بوسكورة'],
  ['المغرب|Al Fqih Ben Çalah', 'الفقيه بن صالح'],
  ['المغرب|Dchira El Jihadia', 'الدشيرة الجهادية'],
  ['المغرب|El Kelaa des Srarhna', 'قلعة السراغنة'],
  ['المغرب|Sidi Slimane', 'سيدي سليمان'],
  ['السودان|El Geneina Fort', 'الجنينة'],
  ['السودان|Al Manāqil', 'المناقل'],
  ['الصومال|El Dibir', 'الديبير'],
  ['الصومال|Ruqi', 'روقي'],
  ['الصومال|Cabudwaaq', 'عبد الواق'],
]);

/** صفوف مكررة: Simele مكرر مع سميل، ومركز عقرة 23k مكرر مع مدينة عقرة 105k. */
const dropKeys = new Set([
  `${IRAQ}|Simele`,
  // في الملف الأصلي اسم الصف 23k هو «ئاكرێ»، وبعد التصحيح يصبح «عقرة».
  `${IRAQ}|ئاكرێ`,
  `${IRAQ}|عقرة|36.7604|43.8943`,
]);

/** أسماء المحافظات العراقية (الصفوف k=1). */
const iraqProvinces = new Map([
  ['Anbar', 'الأنبار'],
  ['Basra', 'البصرة'],
  ['Muthanna', 'المثنى'],
  ['Al-Qadisiyah', 'القادسية'],
  ['Sulaymaniyah', 'السليمانية'],
  ['Babil', 'بابل'],
  ['Baghdad', 'بغداد'],
  ['Duhok', 'دهوك'],
  ['Dhi Qar', 'ذي قار'],
  ['Diyala', 'ديالى'],
  ['Erbil', 'أربيل'],
  ['Karbala', 'كربلاء'],
  ['Kirkuk', 'كركوك'],
  ['Maysan', 'ميسان'],
  ['Nineveh', 'نينوى'],
  ['Wasit', 'واسط'],
  ['Najaf', 'النجف'],
  ['Salah ad Din', 'صلاح الدين'],
  ['Halabja', 'حلبجة'],
]);

const raw = fs.readFileSync(FILE, 'utf8');
const cities = JSON.parse(raw);
const totalBefore = cities.length;

let renamed = 0;
let dropped = 0;
let regionsFixed = 0;
let provincesFixed = 0;

const out = [];
for (const city of cities) {
  const key = `${city.c}|${city.n}`;
  const dropKey = `${key}|${city.la}|${city.lo}`;
  if (dropKeys.has(key) || dropKeys.has(dropKey)) {
    dropped++;
    continue;
  }

  let n = city.n;
  const fix = nameFixes.get(key);
  if (fix && fix !== n) {
    n = fix;
    renamed++;
  }

  let r = city.r;
  if (city.c === IRAQ) {
    const province = iraqProvinces.get(n);
    if (city.k === 1 && province) {
      n = province;
      provincesFixed++;
    }
    const region = iraqProvinces.get(r);
    if (region) {
      r = region;
      regionsFixed++;
    }
  }

  out.push({ ...city, n, r });
}

fs.writeFileSync(FILE, JSON.stringify(out), 'utf8');

console.log(`cities: ${totalBefore} -> ${out.length}`);
console.log(`renamed: ${renamed}, dropped: ${dropped}`);
console.log(`iraq regions: ${regionsFixed}, iraq provinces: ${provincesFixed}`);
