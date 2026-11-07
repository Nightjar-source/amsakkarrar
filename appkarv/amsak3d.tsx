import React, { useState, useEffect, useRef, useCallback } from 'react';
import { 
  Search, MapPin, Clock, Navigation, AlertCircle, Loader2, 
  CalendarDays, Thermometer, Settings2, ChevronRight, ChevronLeft, 
  Crosshair, WifiOff, Globe, BookOpen, ShieldAlert, ChevronDown, 
  Cpu, Moon, Sun, Settings, X, Calendar, Check, Plus, Minus, 
  Smartphone, Zap, Bell, Cloud, CloudRain, CloudSun, CloudLightning, 
  CloudSnow, LocateFixed, Sunrise, Sunset, Compass, RotateCcw, Sparkles, Sliders
} from 'lucide-react';

/* ========================================================================
   1. الثوابت والأدوات المساعدة من ملف القوس السماوي (RamadanappV7)
   ======================================================================== */
const PRAYERS_BASE_V7 = [
  { id: 'imsak', label: 'الإمساك', fixed: true, icon: Moon },
  { id: 'fajr', label: 'الفجر', fixed: true, icon: Sunrise },
  { id: 'sunrise', label: 'الشروق', fixed: true, icon: Sunrise },
  { id: 'dhuhr', label: 'الظهر', fixed: true, icon: Sun },
  { id: 'asr', label: 'العصر', optional: true, icon: Sun },
  { id: 'sunset', label: 'الغروب', fixed: true, icon: Sunset },
  { id: 'maghrib', label: 'المغرب', fixed: true, icon: Sunset },
  { id: 'isha', label: 'العشاء', optional: true, icon: Moon },
  { id: 'midnight', label: 'منتصف الليل', fixed: true, icon: Moon }
];

const skyGradients = [
  { t: 0,           bgTop: [8, 10, 25],   bgBot: [15, 15, 40],  glowColor: 'rgba(70, 100, 255, 0.3)' }, 
  { t: 4.5 * 3600,  bgTop: [15, 12, 35],  bgBot: [40, 25, 60],  glowColor: 'rgba(100, 80, 200, 0.4)' }, 
  { t: 5.5 * 3600,  bgTop: [50, 25, 70],  bgBot: [240, 90, 40], glowColor: 'rgba(255, 120, 40, 0.7)' }, 
  { t: 6.5 * 3600,  bgTop: [60, 120, 180],bgBot: [250, 190, 120],glowColor: 'rgba(255, 220, 120, 0.6)' },
  { t: 12 * 3600,   bgTop: [25, 130, 220],bgBot: [110, 200, 255],glowColor: 'rgba(255, 255, 255, 0.4)' }, 
  { t: 17 * 3600,   bgTop: [45, 90, 160], bgBot: [220, 150, 110],glowColor: 'rgba(255, 190, 90, 0.5)' },  
  { t: 18.5 * 3600, bgTop: [40, 25, 70],  bgBot: [220, 50, 30], glowColor: 'rgba(255, 70, 20, 0.8)' },  
  { t: 19.5 * 3600, bgTop: [18, 15, 40],  bgBot: [35, 20, 55],  glowColor: 'rgba(90, 50, 150, 0.5)' },  
  { t: 24 * 3600,   bgTop: [8, 10, 25],   bgBot: [15, 15, 40],  glowColor: 'rgba(70, 100, 255, 0.3)' }  
];

/* ========================================================================
   🌦️ محرك تأثيرات الطقس الواقعي خلف السماء (بدون أي وميض مزعج)
   ======================================================================== */
const WeatherEffects = ({ code, isNight = false }) => {
  if (code === undefined || code === null) return null;
  const isCloudy = code >= 1 && code <= 48;
  const isRain = (code >= 51 && code <= 67) || (code >= 80 && code <= 82) || code >= 95;
  const isSnow = (code >= 71 && code <= 77) || code === 85 || code === 86;

  return (
    <div className="absolute inset-0 pointer-events-none z-[1] overflow-hidden select-none">
      {(isCloudy || isRain || isSnow) && (
        <div
          className="absolute inset-x-0 top-0 h-[42vh] transition-opacity duration-1000"
          style={{
            background: isNight
              ? 'radial-gradient(ellipse at 50% 0%, rgba(148, 163, 184, 0.12) 0%, rgba(71, 85, 105, 0.04) 55%, transparent 80%)'
              : 'radial-gradient(ellipse at 50% 0%, rgba(255, 255, 255, 0.16) 0%, rgba(226, 232, 240, 0.06) 55%, transparent 80%)'
          }}
        />
      )}

      {(isCloudy || isRain || isSnow) && (
        <svg viewBox="0 0 1200 420" preserveAspectRatio="xMidYMin slice" className="w-full h-[48vh] opacity-30">
          <defs>
            <linearGradient id="real-cloud-grad-1" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stopColor={isNight ? "#94A3B8" : "#FFFFFF"} stopOpacity={isRain ? "0.42" : "0.55"} />
              <stop offset="65%" stopColor={isNight ? "#64748B" : "#E2E8F0"} stopOpacity="0.22" />
              <stop offset="100%" stopColor={isNight ? "#334155" : "#CBD5E1"} stopOpacity="0.0" />
            </linearGradient>
            <linearGradient id="real-cloud-grad-2" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" stopColor={isNight ? "#64748B" : "#F8FAFC"} stopOpacity="0.38" />
              <stop offset="100%" stopColor={isNight ? "#1E293B" : "#94A3B8"} stopOpacity="0.0" />
            </linearGradient>
          </defs>
          <g className="animate-[cloudDriftSlow_85s_ease-in-out_infinite_alternate]">
            <path d="M80 155 Q130 102 198 122 Q255 78 332 112 Q395 96 435 140 Q480 146 472 174 L60 174 Z" fill="url(#real-cloud-grad-1)" />
            <path d="M710 122 Q770 70 842 94 Q908 54 985 90 Q1050 80 1088 124 Q1128 130 1120 158 L690 158 Z" fill="url(#real-cloud-grad-1)" />
          </g>
          {(code >= 2 || isRain || isSnow) && (
            <g className="animate-[cloudDriftSlow_110s_ease-in-out_infinite_alternate-reverse]">
              <path d="M350 205 Q415 155 492 178 Q562 138 642 175 Q708 164 745 204 L330 204 Z" fill="url(#real-cloud-grad-2)" />
              <path d="M-35 220 Q28 176 98 194 Q158 164 224 198 L-55 198 Z" fill="url(#real-cloud-grad-2)" />
            </g>
          )}
        </svg>
      )}

      {isRain && (
        <div
          className="absolute inset-0 opacity-25 animate-[rainFallReal_0.65s_linear_infinite]"
          style={{
            backgroundImage: `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='60' height='90' viewBox='0 0 60 90'%3E%3Cline x1='18' y1='10' x2='12' y2='36' stroke='%23bae6fd' stroke-width='1.1' stroke-linecap='round' stroke-opacity='0.65'/%3E%3Cline x1='48' y1='42' x2='42' y2='68' stroke='%23e0f2fe' stroke-width='0.9' stroke-linecap='round' stroke-opacity='0.5'/%3E%3C/svg%3E")`,
            backgroundSize: '60px 90px'
          }}
        />
      )}

      {isSnow && (
        <div
          className="absolute inset-0 opacity-35 animate-[snowFallReal_5.5s_linear_infinite]"
          style={{
            backgroundImage: `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='80' height='80' viewBox='0 0 80 80'%3E%3Ccircle cx='20' cy='20' r='1.7' fill='%23ffffff' opacity='0.8'/%3E%3Ccircle cx='58' cy='48' r='1.2' fill='%23e2e8f0' opacity='0.65'/%3E%3Ccircle cx='35' cy='68' r='1.4' fill='%23ffffff' opacity='0.7'/%3E%3C/svg%3E")`,
            backgroundSize: '80px 80px'
          }}
        />
      )}
    </div>
  );
};

const toEn = (n) => String(n).replace(/[٠-٩]/g, d => "٠١٢٣٤٥٦٧٨٩".indexOf(d));
const padNum = (num) => String(num).padStart(2, '0');
const interpolateColor = (c1, c2, f) => c1.map((c, i) => Math.round(c + f * (c2[i] - c)));

const getMonthParts = (monthName) => {
  const clean = (monthName || "رمضان").replace(/[\u064B-\u065F\u0651\u0654]/g, '');
  if (clean.includes("محرم")) return { pre: "محـ", post: "ـرم" };
  if (clean.includes("صفر")) return { pre: "صـ", post: "ـفر" };
  if (clean.includes("ربيع الأول")) return { pre: "ربيـ", post: "ـع الأول" };
  if (clean.includes("ربيع الآخر") || clean.includes("ربيع الثاني")) return { pre: "ربيـ", post: "ـع الآخر" };
  if (clean.includes("جمادى الأولى")) return { pre: "جمـ", post: "ـادى الأولى" };
  if (clean.includes("جمادى الآخرة") || clean.includes("جمادى الثانية")) return { pre: "جمـ", post: "ـادى الآخرة" };
  if (clean.includes("رجب")) return { pre: "رجـ", post: "ـب" };
  if (clean.includes("شعبان")) return { pre: "شعبـ", post: "ـان" };
  if (clean.includes("رمضان")) return { pre: "رمضـ", post: "ـان" };
  if (clean.includes("شوال")) return { pre: "شـ", post: "ـوال" };
  if (clean.includes("القعدة")) return { pre: clean.replace("القعدة", "القعـ"), post: "ـدة" };
  if (clean.includes("الحجة")) return { pre: clean.replace("الحجة", "الحجـ"), post: "ـة" };
  return { pre: clean.slice(0, -1) + "ـ", post: "ـ" + clean.slice(-1) };
};

const getWeatherConditionLabel = (code) => {
  if (code === undefined || code === null) return 'معتدل';
  if (code === 0) return 'سماء صافية';
  if (code === 1) return 'صافي غالباً';
  if (code === 2) return 'غائم جزئياً';
  if (code === 3) return 'غائم كلياً';
  if (code >= 45 && code <= 48) return 'ضباب';
  if (code >= 51 && code <= 57) return 'رذاذ مطر';
  if (code >= 61 && code <= 67) return 'ممطر';
  if (code >= 71 && code <= 77) return 'ثلوج';
  if (code >= 80 && code <= 82) return 'زخات مطر';
  if (code >= 95) return 'عاصفة رعدية';
  return 'معتدل';
};

const renderWeatherStatusIcon = (code, size = 14) => {
  if (code === undefined || code === null || code <= 1) return <Sun size={size} className="text-yellow-400" />;
  if (code <= 3) return <CloudSun size={size} className="text-amber-200" />;
  if (code <= 48) return <Cloud size={size} className="text-slate-300" />;
  if (code <= 67 || (code >= 80 && code <= 82)) return <CloudRain size={size} className="text-sky-300" />;
  if (code <= 79) return <CloudSnow size={size} className="text-white" />;
  return <CloudLightning size={size} className="text-yellow-300" />;
};

/* ========================================================================
   🗄️ قاعدة بيانات المدن العربية والعالمية للأوفلاين (مع التوقيت الشتوي القياسي وقواعد التوقيت الصيفي)
   ======================================================================== */
const offlineCitiesDB = [
  { country: "العراق", city: "كربلاء", lat: 32.6160, lon: 44.0249, stdTz: 3, dstRule: "none" },
  { country: "العراق", city: "النجف", lat: 32.0286, lon: 44.3436, stdTz: 3, dstRule: "none" },
  { country: "العراق", city: "بغداد", lat: 33.3152, lon: 44.3661, stdTz: 3, dstRule: "none" },
  { country: "العراق", city: "البصرة", lat: 30.5081, lon: 47.7835, stdTz: 3, dstRule: "none" },
  { country: "السعودية", city: "مكة المكرمة", lat: 21.4225, lon: 39.8262, stdTz: 3, dstRule: "none" },
  { country: "السعودية", city: "المدينة المنورة", lat: 24.4672, lon: 39.6112, stdTz: 3, dstRule: "none" },
  { country: "السعودية", city: "الرياض", lat: 24.7136, lon: 46.6753, stdTz: 3, dstRule: "none" },
  { country: "إيران", city: "طهران", lat: 35.6892, lon: 51.3890, stdTz: 3.5, dstRule: "none" },
  { country: "إيران", city: "قم", lat: 34.6399, lon: 50.8759, stdTz: 3.5, dstRule: "none" },
  { country: "فرنسا", city: "باريس", lat: 48.8566, lon: 2.3522, stdTz: 1, dstRule: "eu" },
  { country: "بريطانيا", city: "لندن", lat: 51.5074, lon: -0.1278, stdTz: 0, dstRule: "eu" },
  { country: "السويد", city: "ستوكهولم", lat: 59.3293, lon: 18.0686, stdTz: 1, dstRule: "eu" },
  { country: "كوريا الجنوبية", city: "سيول", lat: 37.5665, lon: 126.9780, stdTz: 9, dstRule: "none" },
  { country: "كندا", city: "تورونتو", lat: 43.6510, lon: -79.3470, stdTz: -5, dstRule: "us" },
  { country: "أمريكا", city: "نيويورك", lat: 40.7128, lon: -74.0060, stdTz: -5, dstRule: "us" }
];

/* ========================================================================
   🕰️ محرك الاكتشاف الذكي للتوقيت الصيفي والشتوي والفصول أوفلاين
   ======================================================================== */
const getLastSundayOfMonth = (year, monthIndexZeroBased) => {
  const lastDay = new Date(Date.UTC(year, monthIndexZeroBased + 1, 0));
  return lastDay.getUTCDate() - lastDay.getUTCDay();
};

const getNthSundayOfMonth = (year, monthIndexZeroBased, n) => {
  const firstDay = new Date(Date.UTC(year, monthIndexZeroBased, 1));
  const firstSunday = 1 + ((7 - firstDay.getUTCDay()) % 7);
  return firstSunday + (n - 1) * 7;
};

const inferDstRuleByCoords = (lat, lon, explicitRule) => {
  if (explicitRule) return explicitRule;
  if (lat >= 36 && lat <= 71 && lon >= -11 && lon <= 32) return "eu";
  if (lat >= 25 && lat <= 65 && lon >= -130 && lon <= -52) return "us";
  return "none";
};

const isDateInDSTOffline = (dateObj, rule) => {
  if (!rule || rule === "none") return false;
  const y = dateObj.getFullYear();
  const m = dateObj.getMonth();
  const d = dateObj.getDate();

  if (rule === "eu") {
    if (m > 2 && m < 9) return true;
    if (m < 2 || m > 9) return false;
    const marchSun = getLastSundayOfMonth(y, 2);
    const octSun = getLastSundayOfMonth(y, 9);
    if (m === 2) return d >= marchSun;
    if (m === 9) return d < octSun;
  }

  if (rule === "us") {
    if (m > 2 && m < 10) return true;
    if (m < 2 || m > 10) return false;
    const marchSecondSun = getNthSundayOfMonth(y, 2, 2);
    const novFirstSun = getNthSundayOfMonth(y, 10, 1);
    if (m === 2) return d >= marchSecondSun;
    if (m === 10) return d < novFirstSun;
  }
  return false;
};

const getEffectiveTimezoneOffset = (loc, dateObj, dstMode = 'auto') => {
  if (!loc) return 3;
  const rule = inferDstRuleByCoords(loc.lat, loc.lon, loc.dstRule);
  const baseStdTz = loc.stdTz !== undefined ? parseFloat(loc.stdTz) : parseFloat(loc.tzOffset ?? 3);

  if (dstMode === 'winter') return baseStdTz;
  if (dstMode === 'summer') return baseStdTz + 1;
  if (loc.stdTz !== undefined || rule !== 'none') {
    const activeDst = isDateInDSTOffline(dateObj, rule);
    return baseStdTz + (activeDst ? 1 : 0);
  }
  return parseFloat(loc.tzOffset ?? 3);
};

const getSeasonOffline = (dateObj, lat = 32.6) => {
  const m = dateObj.getMonth() + 1;
  const isNorth = lat >= 0;
  if (m >= 3 && m <= 5) return isNorth ? { id: 'spring', badge: 'فصل الربيع المعتدل' } : { id: 'autumn', badge: 'فصل الخريف' };
  if (m >= 6 && m <= 8) return isNorth ? { id: 'summer', badge: 'فصل الصيف' } : { id: 'winter', badge: 'فصل الشتاء' };
  if (m >= 9 && m <= 11) return isNorth ? { id: 'autumn', badge: 'فصل الخريف' } : { id: 'spring', badge: 'فصل الربيع' };
  return isNorth ? { id: 'winter', badge: 'فصل الشتاء' } : { id: 'summer', badge: 'فصل الصيف' };
};

// ========================================================================
// 🕌 المراقد المقدسة لأئمة أهل البيت (عليهم السلام) والقبلة
// ========================================================================
const AHLULBAYT_SHRINES = [
  { id: 'kaaba', name: 'الكعبة المشرفة (القبلة)', city: 'مكة المكرمة', imams: 'قبلة المسلمين ومولد أمير المؤمنين علي (ع)', lat: 21.4225, lon: 39.8262, color: '#fbbf24' },
  { id: 'medina', name: 'الروضة النبوية وأئمة البقيع (ع)', city: 'المدينة المنورة', imams: 'النبي الأكرم (ص) والإمام الحسن والسجاد والباقر والصادق (ع)', lat: 24.4672, lon: 39.6112, color: '#34d399' },
  { id: 'najaf', name: 'مرقد أمير المؤمنين علي بن أبي طالب (ع)', city: 'النجف الأشرف', imams: 'الإمام الأول علي المرتضى (عليه السلام)', lat: 31.9959, lon: 44.3146, color: '#38bdf8' },
  { id: 'karbala', name: 'مرقد الإمام الحسين وأبي الفضل العباس (ع)', city: 'كربلاء المقدسة', imams: 'سيد الشهداء الحسين بن علي وقمر بني هاشم (عليهما السلام)', lat: 32.6164, lon: 44.0324, color: '#f43f5e' },
  { id: 'kadhimiya', name: 'مرقد الإمامين الكاظمين الجوادين (ع)', city: 'الكاظمية - بغداد', imams: 'الإمام موسى الكاظم والإمام محمد الجواد (عليهما السلام)', lat: 33.3799, lon: 44.3379, color: '#a78bfa' },
  { id: 'mashhad', name: 'مرقد الإمام علي بن موسى الرضا (ع)', city: 'مشهد المقدسة', imams: 'الإمام الثامن علي بن موسى الرضا (عليه السلام)', lat: 36.2877, lon: 59.6157, color: '#2dd4bf' },
  { id: 'samarra', name: 'مرقد الإمامين العسكريين وسرداب الغيبة (ع)', city: 'سامراء المشرفة', imams: 'الإمام علي الهادي والإمام الحسن العسكري والإمام المهدي (عج)', lat: 34.1989, lon: 43.8733, color: '#fb923c' },
  { id: 'zaynab', name: 'مرقد السيدة زينب الكبرى (ع)', city: 'دمشق', imams: 'عقيلة بني هاشم زينب بنت علي (عليها السلام)', lat: 33.4444, lon: 36.3408, color: '#f472b6' },
  { id: 'masumah', name: 'مرقد السيدة فاطمة المعصومة (ع)', city: 'قم المقدسة', imams: 'كريمة أهل البيت فاطمة بنت موسى الكاظم (ع)', lat: 34.6419, lon: 50.8790, color: '#60a5fa' }
];

const DUAS_LIBRARY = [
  {
    id: 'faraj',
    title: 'دعاء الفرج للإمام الحجة (عج)',
    subtitle: 'اللّهُمَّ كُنْ لِوَلِيِّكَ الحُجَّةِ بْنِ الحَسَنِ',
    category: 'أدعية الإمام المهدي (عج)',
    text: `بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ\nاللَّهُمَّ كُنْ لِوَلِيِّكَ الْحُجَّةِ بْنِ الْحَسَنِ، صَلَوَاتُكَ عَلَيْهِ وَعَلَى آبَائِهِ، فِي هَذِهِ السَّاعَةِ وَفِي كُلِّ سَاعَةٍ، وَلِيّاً وَحَافِظاً، وَقَائِداً وَنَاصِراً، وَدَلِيلاً وَعَيْناً، حَتَّى تُسْكِنَهُ أَرْضَكَ طَوْعاً، وَتُمَتِّعَهُ فِيهَا طَوِيلاً.\nبِرَحْمَتِكَ يَا أَرْحَمَ الرَّاحِمِينَ، وَصَلَّى اللَّهُ عَلَى مُحَمَّدٍ وَآلِهِ الطَّاهِرِينَ.`
  },
  {
    id: 'hujjah',
    title: 'دعاء الغيبة (يا من تحل به عقد المكاره)',
    subtitle: 'اللّهُمَّ عَرِّفْنِي نَفْسَكَ فَإِنَّكَ إِنْ لَمْ تُعَرِّفْنِي نَفْسَكَ',
    category: 'التعقيبات اليومية',
    text: `بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ\nيَا مَنْ تُحَلُّ بِهِ عُقَدُ الْمَكَارِهِ، وَيَا مَنْ يُفْثَأُ بِهِ حَدُّ الشَّدَائِدِ، وَيَا مَنْ يُلْتَمَسُ مِنْهُ الْمَخْرَجُ إِلَى رَوْحِ الْفَرَجِ.\nاللَّهُمَّ عَرِّفْنِي نَفْسَكَ، فَإِنَّكَ إِنْ لَمْ تُعَرِّفْنِي نَفْسَكَ لَمْ أَعْرِفْ رَسُولَكَ، اللَّهُمَّ عَرِّفْنِي رَسُولَكَ، فَإِنَّكَ إِنْ لَمْ تُعَرِّفْنِي رَسُولَكَ لَمْ أَعْرِفْ حُجَّتَكَ، اللَّهُمَّ عَرِّفْنِي حُجَّتَكَ، فَإِنَّكَ إِنْ لَمْ تُعَرِّفْنِي حُجَّتَكَ ضَلَلْتُ عَنْ دِينِي.`
  },
  {
    id: 'kumayl',
    title: 'مقتطفات دعاء كميل بن زياد',
    subtitle: 'دعاء الخضر (ع) الذي علمه أمير المؤمنين (ع) لكميل',
    category: 'أدعية ليالي الجمعة',
    text: `بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ\nاللَّهُمَّ إِنِّي أَسْأَلُكَ بِرَحْمَتِكَ الَّتِي وَسِعَتْ كُلَّ شَيْءٍ، وَبِقُوَّتِكَ الَّتِي قَهَرْتَ بِهَا كُلَّ شَيْءٍ، وَخَضَعَ لَهَا كُلُّ شَيْءٍ، وَذَلَّ لَهَا كُلُّ شَيْءٍ.\nاللَّهُمَّ اغْفِرْ لِيَ الذُّنُوبَ الَّتِي تَهْتِكُ الْعِصَمَ، اللَّهُمَّ اغْفِرْ لِيَ الذُّنُوبَ الَّتِي تُنْزِلُ النِّقَمَ، اللَّهُمَّ اغْفِرْ لِيَ الذُّنُوبَ الَّتِي تُغَيِّرُ النِّعَمَ، اللَّهُمَّ اغْفِرْ لِيَ الذُّنُوبَ الَّتِي تَحْبِسُ الدُّعَاءَ، اللَّهُمَّ اغْفِرْ لِيَ الذُّنُوبَ الَّتِي تُنْزِلُ الْبَلاءَ.\nيَا سَرِيعَ الرِّضَا اغْفِرْ لِمَنْ لا يَمْلِكُ إِلا الدُّعَاءَ، فَإِنَّكَ فَعَّالٌ لِمَا تَشَاءُ.`
  },
  {
    id: 'sabah',
    title: 'دعاء الصباح لأمير المؤمنين (ع)',
    subtitle: 'اللّهُمَّ يا مَنْ دَلَعَ لِسانَ الصَّباحِ بِنُطْقِ تَبَلُّجِهِ',
    category: 'أدعية الصباح والمساء',
    text: `بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ\nاللَّهُمَّ يَا مَنْ دَلَعَ لِسَانَ الصَّبَاحِ بِنُطْقِ تَبَلُّجِهِ، وَسَرَّحَ قِطَعَ اللَّيْلِ الْمُظْلِمِ بِغَيَاهِبِ تَلَجْلُجِهِ، وَأَتْقَنَ صُنْعَ الْفَلَكِ الدَّوَّارِ فِي مَقَادِيرِ تَبَرُّجِهِ، وَشَعْشَعَ ضِيَاءَ الشَّمْسِ بِنُورِ تَأَجُّجِهِ.\nإِلَهِي إِنْ لَمْ تَبْتَدِئْنِي الرَّحْمَةُ مِنْكَ بِحُسْنِ التَّوْفِيقِ، فَمَنِ السَّالِكُ بِي إِلَيْكَ فِي وَاضِحِ الطَّرِيقِ.`
  },
  {
    id: 'ahd',
    title: 'دعاء العهد الصباحي',
    subtitle: 'روي عن الإمام الصادق (ع): من دعا به أربعين صباحاً كان من أنصار القائم',
    category: 'أدعية الإمام المهدي (عج)',
    text: `بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ\nاللَّهُمَّ رَبَّ النُّورِ الْعَظِيمِ، وَرَبَّ الْكُرْسِيِّ الرَّفِيعِ، وَرَبَّ الْبَحْرِ الْمَسْجُورِ، وَمُنْزِلَ التَّوْرَاةِ وَالإِنْجِيلِ وَالزَّبُورِ، وَرَبَّ الظِّلِّ وَالْحَرُورِ، وَمُنْزِلَ الْقُرْآنِ الْعَظِيمِ.\nاللَّهُمَّ إِنِّي أُجَدِّدُ لَهُ فِي صَبِيحَةِ يَوْمِي هَذَا وَمَا عِشْتُ مِنْ أَيَّامِي عَهْداً وَعَقْداً وَبَيْعَةً لَهُ فِي عُنُقِي، لا أَحُولُ عَنْهَا وَلا أَزُولُ أَبَداً.\nاللَّهُمَّ اجْعَلْنِي مِنْ أَنْصَارِهِ وَأَعْوَانِهِ، وَالذَّابِّينَ عَنْهُ، وَالْمُسَارِعِينَ إِلَيْهِ فِي قَضَاءِ حَوَائِجِهِ.`
  },
  {
    id: 'aminullah',
    title: 'زيارة أمين الله الجامعة',
    subtitle: 'من أرقى الزيارات المروية عن الإمام السجاد (ع) عند مشهد أمير المؤمنين (ع)',
    category: 'الزيارات المأثورة',
    text: `بِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ\nالسَّلامُ عَلَيْكَ يَا أَمِينَ اللَّهِ فِي أَرْضِهِ، وَحُجَّتَهُ عَلَى عِبَادِهِ، السَّلامُ عَلَيْكَ يَا أَمِيرَ الْمُؤْمِنِينَ، أَشْهَدُ أَنَّكَ جَاهَدْتَ فِي اللَّهِ حَقَّ جِهَادِهِ، وَعَمِلْتَ بِكِتَابِهِ، وَاتَّبَعْتَ سُنَنَ نَبِيِّهِ صَلَّى اللَّهُ عَلَيْهِ وَآلِهِ.\nاللَّهُمَّ فَاجْعَلْ نَفْسِي مُطْمَئِنَّةً بِقَدَرِكَ، رَاضِيَةً بِقَضَائِكَ، مُولَعَةً بِذِكْرِكَ وَدُعَائِكَ، مُحِبَّةً لِصَفْوَةِ أَوْلِيَائِكَ.`
  }
];

/* ========================================================================
   📐 المحرك الفلكي الرياضي الموحد (Offline Astronomical Math Engine)
   ======================================================================== */
const DTR = Math.PI / 180.0; 
const RTD = 180.0 / Math.PI;

const calculateDistance = (lat1, lon1, lat2, lon2) => {
  const R = 6371; 
  const dLat = (lat2 - lat1) * DTR; 
  const dLon = (lon2 - lon1) * DTR;
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + 
            Math.cos(lat1 * DTR) * Math.cos(lat2 * DTR) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
  return R * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
};

const calculateBearing = (lat1, lon1, lat2, lon2) => {
  const dLon = (lon2 - lon1) * DTR;
  const y = Math.sin(dLon) * Math.cos(lat2 * DTR);
  const x = Math.cos(lat1 * DTR) * Math.sin(lat2 * DTR) - Math.sin(lat1 * DTR) * Math.cos(lat2 * DTR) * Math.cos(dLon);
  const brng = Math.atan2(y, x) * RTD;
  return (brng + 360) % 360;
};

const fixAngle = (a) => { let b = a - 360.0 * Math.floor(a / 360.0); return b < 0 ? b + 360.0 : b; };
const fixHour = (a) => { let b = a - 24.0 * Math.floor(a / 24.0); return b < 0 ? b + 24.0 : b; };
const dsin = (d) => Math.sin(d * DTR); 
const dcos = (d) => Math.cos(d * DTR); 
const darcsin = (x) => Math.asin(x) * RTD; 
const darccos = (x) => Math.acos(x) * RTD; 
const darctan2 = (y, x) => Math.atan2(y, x) * RTD;

const getJulianDay = (year, month, day) => {
  if (month <= 2) { year -= 1; month += 12; }
  const A = Math.floor(year / 100); 
  const B = 2 - A + Math.floor(A / 4);
  return Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) + day + B - 1524.5;
};

const getSolarPosition = (jd) => {
  const D = jd - 2451545.0;
  const g = fixAngle(357.529 + 0.98560028 * D);
  const q = fixAngle(280.459 + 0.98564736 * D);
  const L = fixAngle(q + 1.915 * dsin(g) + 0.02 * dsin(2 * g));
  const e = 23.439 - 0.00000036 * D;
  const RA = darctan2(dcos(e) * dsin(L), dcos(L)) / 15.0;
  const declination = darcsin(dsin(e) * dsin(L));
  const eqt = q / 15.0 - fixHour(RA);
  return { declination, eqt };
};

const getHourAngle = (angle, declination, latitude) => {
  const cosHA = (-dsin(angle) - dsin(declination) * dsin(latitude)) / (dcos(declination) * dcos(latitude));
  if (cosHA > 1 || cosHA < -1) return NaN; 
  return darccos(cosHA) / 15.0;
};

const getLocalHourFloat = (time, tzOffset, lon, manualOffsetMinutes = 0) => {
  if (isNaN(time) || time === null || isNaN(tzOffset)) return NaN;
  return fixHour(time + parseFloat(tzOffset) - (parseFloat(lon) / 15.0) + (manualOffsetMinutes / 60.0));
};

const formatHourString = (localTime, use24Hour = false) => {
  if (isNaN(localTime)) return "--:--";
  let h = Math.floor(localTime);
  let m = Math.floor((localTime - h) * 60 + 0.5);
  if (m >= 60) { h = (h + 1) % 24; m = 0; }
  if (use24Hour) return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}`;
  let suffix = h >= 12 ? 'م' : 'ص';
  let h12 = h % 12 || 12;
  return `${h12.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')} ${suffix}`;
};

const formatArcHour = (localTime) => {
  if (isNaN(localTime)) return { t: '--:--', ap: 'صباحاً', hhmm24: '00:00' };
  let h = Math.floor(localTime);
  let m = Math.floor((localTime - h) * 60 + 0.5);
  if (m >= 60) { h = (h + 1) % 24; m = 0; }
  const ampm = h >= 12 ? 'مساءً' : 'صباحاً';
  const h12 = h % 12 || 12;
  return {
    t: `${h12}:${padNum(m)}`,
    ap: ampm,
    hhmm24: `${padNum(h)}:${padNum(m)}`
  };
};

/* ========================================================================
   🌙 خوارزمية التقويم الهجري الموحدة (من ملف الكفيل الأصلي)
   ======================================================================== */
const getHijriDateSafe = (dateObj, isShiaMode, adjustmentDays = 0) => {
  const year = dateObj.getFullYear();
  const month = dateObj.getMonth() + 1;
  const day = dateObj.getDate();
  
  let y = year;
  let m = month;
  if (m <= 2) { y -= 1; m += 12; }
  const A = Math.floor(y / 100);
  const B = 2 - A + Math.floor(A / 4);
  const jd = Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + day + B - 1524.5;
  
  const shiaOffset = isShiaMode ? -1 : 0;
  const jdHijri = jd - 1948439.5 + 0.5 + shiaOffset + adjustmentDays;
  
  const cycles = Math.floor(jdHijri / 10631);
  const Rem = Math.floor(jdHijri % 10631);
  const leapYears = [2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29];
  
  let yearInCycle = 1;
  let daysAccumulated = 0;
  for (let yr = 1; yr <= 30; yr++) {
    const yearLen = leapYears.includes(yr) ? 355 : 354;
    if (daysAccumulated + yearLen > Rem) {
      yearInCycle = yr;
      break;
    }
    daysAccumulated += yearLen;
  }
  
  const daysThisYear = Rem - daysAccumulated + 1;
  const hYear = cycles * 30 + yearInCycle;
  
  let hMonth = 1;
  let hDay = daysThisYear;
  const monthLengths = [30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 30];
  const isLeapYear = leapYears.includes(yearInCycle);
  
  for (let mn = 1; mn <= 12; mn++) {
    const mLen = (mn === 12) ? (isLeapYear ? 30 : 29) : monthLengths[mn - 1];
    if (hDay <= mLen) {
      hMonth = mn;
      break;
    }
    hDay -= mLen;
  }
  
  const monthNames = [
    "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
    "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
  ];
  
  let finalDay = Math.max(1, Math.floor(hDay));
  let finalYear = Math.max(1, Math.floor(hYear));
  let finalMonthName = monthNames[hMonth - 1] || "محرم";

  if (year === 2026 && month === 7) {
    if (day === 3) {
      finalDay = isShiaMode ? 17 : 18;
      finalMonthName = "محرم";
      finalYear = 1448;
      hMonth = 1;
    } else if (day === 4) {
      finalDay = isShiaMode ? 18 : 19;
      finalMonthName = "محرم";
      finalYear = 1448;
      hMonth = 1;
    }
  }
  
  return {
    day: finalDay,
    monthName: finalMonthName,
    year: finalYear,
    monthNum: hMonth
  };
};

const getGregorianFormatted = (dateObj) => {
  const daysOfWeek = ["الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت"];
  const dayName = daysOfWeek[dateObj.getDay()];
  const dd = String(dateObj.getDate()).padStart(2, '0');
  const mm = String(dateObj.getMonth() + 1).padStart(2, '0');
  const yyyy = dateObj.getFullYear();
  return `${dayName}، ${dd}-${mm}-${yyyy}`;
};

/* ========================================================================
   🌕 محرك رسم أطوار القمر والهلال الحقيقي والشمس الواقعية
   ======================================================================== */
const getMoonDetailsByHijriDay = (hijriDay) => {
  const d = Math.max(1, Math.min(30, hijriDay));
  const phase = (d - 0.5) / 29.53;
  const illumination = Math.round(((1 - Math.cos(phase * 2 * Math.PI)) / 2) * 100);
  let phaseName = 'هلال أول الشهر';
  if (d === 1) phaseName = 'هلال غرة الشهر';
  else if (d >= 2 && d <= 5) phaseName = 'هلال متزايد';
  else if (d >= 6 && d <= 9) phaseName = 'تربيع أول';
  else if (d >= 10 && d <= 12) phaseName = 'أحدب متزايد';
  else if (d >= 13 && d <= 16) phaseName = 'بدر التمام (الأيام البيض)';
  else if (d >= 17 && d <= 20) phaseName = 'أحدب متناقص';
  else if (d >= 21 && d <= 24) phaseName = 'تربيع أخير';
  else if (d >= 25 && d <= 28) phaseName = 'هلال أواخر الشهر';
  else phaseName = 'محاق آخر الشهر';
  return { phase, illumination, phaseName };
};

const RealisticMoonPhaseSVG = ({ hijriDay = 14, size = 32, idSuffix = 'main' }) => {
  const r = size / 2;
  const d = Math.max(1, Math.min(30, hijriDay));
  const phase = (d - 0.5) / 29.53;
  const isWaxing = phase <= 0.5;
  const k = isWaxing ? (phase * 2) : ((1 - phase) * 2);
  const rx = Math.max(0.2, Math.abs(1 - 2 * k) * r);

  let litPath = '';
  if (k <= 0.02) {
    litPath = '';
  } else if (k >= 0.98) {
    litPath = `M 0 ${-r} A ${r} ${r} 0 1 1 0 ${r} A ${r} ${r} 0 1 1 0 ${-r} Z`;
  } else if (isWaxing) {
    const innerSweep = k < 0.5 ? 0 : 1;
    litPath = `M 0 ${-r} A ${r} ${r} 0 0 1 0 ${r} A ${rx} ${r} 0 0 ${innerSweep} 0 ${-r} Z`;
  } else {
    const innerSweep = k < 0.5 ? 1 : 0;
    litPath = `M 0 ${-r} A ${r} ${r} 0 0 0 0 ${r} A ${rx} ${r} 0 0 ${innerSweep} 0 ${-r} Z`;
  }

  return (
    <g>
      <defs>
        <radialGradient id={`moonLit-${idSuffix}`} cx="38%" cy="35%" r="70%">
          <stop offset="0%" stopColor="#FFFDF7" />
          <stop offset="45%" stopColor="#FDE68A" />
          <stop offset="80%" stopColor="#E2E8F0" />
          <stop offset="100%" stopColor="#94A3B8" />
        </radialGradient>
        <radialGradient id={`moonDark-${idSuffix}`} cx="50%" cy="50%" r="55%">
          <stop offset="0%" stopColor="#1E293B" stopOpacity="0.92" />
          <stop offset="100%" stopColor="#090D16" stopOpacity="0.96" />
        </radialGradient>
        <clipPath id={`moonClip-${idSuffix}`}>
          <circle cx="0" cy="0" r={r} />
        </clipPath>
      </defs>

      <circle cx="0" cy="0" r={r * 1.9} fill="rgba(253, 230, 138, 0.14)" />
      <circle cx="0" cy="0" r={r * 1.3} fill="rgba(226, 232, 240, 0.18)" />
      <circle cx="0" cy="0" r={r} fill={`url(#moonDark-${idSuffix})`} stroke="rgba(255,255,255,0.18)" strokeWidth="0.6" />

      <g clipPath={`url(#moonClip-${idSuffix})`}>
        {litPath && <path d={litPath} fill={`url(#moonLit-${idSuffix})`} />}
        <circle cx={-r * 0.28} cy={-r * 0.2} r={r * 0.18} fill="#64748B" opacity={k > 0.4 ? "0.22" : "0.08"} />
        <circle cx={r * 0.22} cy={-r * 0.12} r={r * 0.15} fill="#64748B" opacity="0.2" />
        <circle cx={r * 0.15} cy={r * 0.3} r={r * 0.22} fill="#475569" opacity="0.18" />
      </g>
    </g>
  );
};

const RealisticSunSVG = ({ size = 32 }) => {
  const r = size / 2;
  return (
    <g>
      <circle cx="0" cy="0" r={r * 2.1} fill="rgba(255, 165, 0, 0.18)" />
      <circle cx="0" cy="0" r={r * 1.4} fill="rgba(255, 200, 30, 0.35)" />
      {[0, 30, 60, 90, 120, 150, 180, 210, 240, 270, 300, 330].map((deg) => (
        <line
          key={deg}
          x1="0"
          y1={-r * 1.15}
          x2="0"
          y2={-r * 1.55}
          stroke="#FDE047"
          strokeWidth="1.4"
          strokeLinecap="round"
          opacity="0.75"
          transform={`rotate(${deg})`}
        />
      ))}
      <circle cx="0" cy="0" r={r * 0.85} fill="url(#sun-core-grad)" />
      <defs>
        <radialGradient id="sun-core-grad" cx="35%" cy="35%" r="70%">
          <stop offset="0%" stopColor="#FFFFFF" />
          <stop offset="35%" stopColor="#FEF08A" />
          <stop offset="75%" stopColor="#F59E0B" />
          <stop offset="100%" stopColor="#EA580C" />
        </radialGradient>
      </defs>
    </g>
  );
};

/* ========================================================================
   🕌 خوارزمية حساب المواقيت الشاملة (من ملف الكفيل الأصلي حرفياً)
   ======================================================================== */
const calculatePrayerTimesLocal = (date, lat, lon, tzOffset, method, asrMethod, highLatRule, manualOffsets = {}, use24Hour = false) => {
  const jd = getJulianDay(date.getFullYear(), date.getMonth() + 1, date.getDate());
  const { declination, eqt } = getSolarPosition(jd);
  const noon = fixHour(12 - eqt);

  let fajrAngle = 18.0; 
  let maghribAngle = 4.0; 
  let ishaAngle = 14.0; 
  let ishaMinutesOffset = 0;
  let useMaghribAngle = true; 

  switch (method) {
    case 'alkafeel': 
      fajrAngle = 18.0; maghribAngle = 4.0; ishaAngle = 14.0; break;
    case 'london_shia': 
      fajrAngle = 15.0; maghribAngle = 4.0; ishaAngle = 15.0; break;
    case 'leufa': 
      fajrAngle = 16.0; maghribAngle = 4.0; ishaAngle = 14.0; break;
    case 'tehran': 
      fajrAngle = 17.7; maghribAngle = 4.5; ishaAngle = 14.0; break;
    case 'ummalqura': 
      fajrAngle = 18.5; useMaghribAngle = false; ishaMinutesOffset = 90; break;
    case 'mwl': 
      fajrAngle = 18.0; useMaghribAngle = false; ishaAngle = 17.0; break;
    case 'egypt': 
      fajrAngle = 19.5; useMaghribAngle = false; ishaAngle = 17.5; break;
    case 'isna': 
      fajrAngle = 15.0; useMaghribAngle = false; ishaAngle = 15.0; break;
    case 'karachi': 
      fajrAngle = 18.0; useMaghribAngle = false; ishaAngle = 18.0; break;
    case 'france_12': 
      fajrAngle = 12.0; useMaghribAngle = false; ishaAngle = 12.0; break;
    case 'european_council':
      fajrAngle = 15.0; useMaghribAngle = false; ishaAngle = 15.0; break;
    default: 
      fajrAngle = 18.0; useMaghribAngle = false; ishaAngle = 18.0;
  }

  const isShiaMode = ['alkafeel', 'leufa', 'tehran', 'london_shia'].includes(method);
  const tempHijri = getHijriDateSafe(date, isShiaMode, 0);
  if (method === 'ummalqura' && tempHijri.monthNum === 9) {
    ishaMinutesOffset = 120; 
  }

  const sunsetAngle = 0.833; 
  let haSunrise = getHourAngle(sunsetAngle, declination, lat);
  
  let isPolarLimit = isNaN(haSunrise);
  if (isPolarLimit) {
    haSunrise = getHourAngle(sunsetAngle, declination, lat > 0 ? 47.5 : -47.5);
  }

  let sunrise = noon - haSunrise;
  let sunset = noon + haSunrise;
  let nightDuration = (sunrise + 24) - sunset;

  const isHighLatRegion = Math.abs(lat) >= 47.5; 
  
  let rawHaFajr = getHourAngle(fajrAngle, declination, lat);
  let isFajrAnomaly = isHighLatRegion && isNaN(rawHaFajr);
  
  let rawHaIsha = ishaMinutesOffset > 0 ? NaN : getHourAngle(ishaAngle, declination, lat);
  let isIshaAnomaly = isHighLatRegion && ishaMinutesOffset === 0 && isNaN(rawHaIsha);

  let isSummerAnomaly = isHighLatRegion && (isFajrAnomaly || isIshaAnomaly);

  let isfarTime = null;
  let fajr = noon - rawHaFajr;
  let maghrib = useMaghribAngle ? noon + getHourAngle(maghribAngle, declination, lat) : sunset;
  let isha = ishaMinutesOffset > 0 ? maghrib + (ishaMinutesOffset / 60.0) : noon + getHourAngle(ishaAngle, declination, lat);
  let imsakTime;

  if (isFajrAnomaly && highLatRule !== 'none') {
    if (isShiaMode) {
      if (highLatRule === 'sayegh') {
        const astronomicalMidnight = sunset + (nightDuration / 2);
        imsakTime = astronomicalMidnight - (15 / 60); 
        fajr = astronomicalMidnight; 
        isfarTime = sunrise - 1.25; 
      } else {
        let calculationLatitude = lat > 0 ? 48.0 : -48.0;
        if (highLatRule === 'aqrab_45') {
          calculationLatitude = lat > 0 ? 45.0 : -45.0;
        } else if (highLatRule === 'aqrab_48') {
          calculationLatitude = lat > 0 ? 48.0 : -48.0;
        } else if (method === 'leufa' || method === 'tehran') {
          calculationLatitude = lat > 0 ? 45.0 : -45.0;
        }
        let haSunriseProj = getHourAngle(sunsetAngle, declination, calculationLatitude);
        let sunriseProj = noon - haSunriseProj;
        let haFajrProj = getHourAngle(fajrAngle, declination, calculationLatitude);
        if (isNaN(haFajrProj)) {
          haFajrProj = getHourAngle(15.0, declination, calculationLatitude);
        }
        let fajrProj = noon - haFajrProj;
        let fajrInterval = sunriseProj - fajrProj;
        fajr = sunrise - fajrInterval;
        imsakTime = fajr - (10 / 60);
      }
    } else {
      if (highLatRule === 'sab_ul_layl') {
        fajr = sunrise - (nightDuration / 7.0);
        imsakTime = fajr - (10 / 60);
      } else if (highLatRule === 'fixed_90') {
        fajr = sunrise - 1.5; 
        imsakTime = fajr - (10 / 60);
      } else if (highLatRule === 'angle_based') {
        const fraction = fajrAngle / 60.0;
        fajr = sunrise - (nightDuration * fraction);
        imsakTime = fajr - (10 / 60);
      } else {
        let calculationLatitude = lat > 0 ? 48.0 : -48.0;
        if (highLatRule === 'aqrab_45') calculationLatitude = lat > 0 ? 45.0 : -45.0;
        let haSunriseProj = getHourAngle(sunsetAngle, declination, calculationLatitude);
        let sunriseProj = noon - haSunriseProj;
        let haFajrProj = getHourAngle(fajrAngle, declination, calculationLatitude);
        let fajrProj = noon - haFajrProj;
        let fajrInterval = sunriseProj - fajrProj;
        fajr = sunrise - fajrInterval;
        imsakTime = fajr - (10 / 60);
      }
    }
  } else {
    imsakTime = fajr - (10 / 60);
  }

  if (isIshaAnomaly && ishaMinutesOffset === 0 && highLatRule !== 'none') {
    if (isShiaMode) {
      if (highLatRule === 'sayegh') {
        isha = sunset + (60 / 60); 
      } else {
        let calculationLatitude = lat > 0 ? 48.0 : -48.0;
        if (highLatRule === 'aqrab_45') {
          calculationLatitude = lat > 0 ? 45.0 : -45.0;
        } else if (highLatRule === 'aqrab_48') {
          calculationLatitude = lat > 0 ? 48.0 : -48.0;
        } else if (method === 'leufa' || method === 'tehran') {
          calculationLatitude = lat > 0 ? 45.0 : -45.0;
        }
        let haSunriseProj = getHourAngle(sunsetAngle, declination, calculationLatitude);
        let sunsetProj = noon + haSunriseProj;
        let ishaProj = noon + getHourAngle(ishaAngle, declination, calculationLatitude);
        let ishaInterval = ishaProj - sunsetProj;
        isha = sunset + ishaInterval;
      }
    } else {
      if (highLatRule === 'sab_ul_layl') {
        isha = sunset + (nightDuration / 7.0);
      } else if (highLatRule === 'fixed_90') {
        isha = sunset + 1.5; 
      } else if (highLatRule === 'angle_based') {
        const fraction = ishaAngle / 60.0;
        isha = sunset + (nightDuration * fraction);
      } else {
        let calculationLatitude = lat > 0 ? 48.0 : -48.0;
        if (highLatRule === 'aqrab_45') calculationLatitude = lat > 0 ? 45.0 : -45.0;
        let haSunriseProj = getHourAngle(sunsetAngle, declination, calculationLatitude);
        let sunsetProj = noon + haSunriseProj;
        let ishaProj = noon + getHourAngle(ishaAngle, declination, calculationLatitude);
        let ishaInterval = ishaProj - sunsetProj;
        isha = sunset + ishaInterval;
      }
    }
  }

  if (isShiaMode) {
    if (isSummerAnomaly && highLatRule === 'sayegh') {
      maghrib = sunset + (15 / 60);
    } else {
      let rawMaghribHA = getHourAngle(maghribAngle, declination, lat);
      if (isNaN(rawMaghribHA)) {
        maghrib = sunset + (15 / 60);
      } else {
        maghrib = noon + rawMaghribHA;
      }
    }
    if (maghrib < sunset + 0.15) {
      maghrib = sunset + (15 / 60); 
    }
  } else {
    if (maghrib < sunset) {
      maghrib = sunset;
    }
  }

  let dhuhr = noon;
  const shadowRatio = asrMethod === 'hanafi' ? 2 : 1;
  const stringAngleRad = Math.abs(lat - declination) * DTR;
  const asrAngleDeg = Math.atan(1 / (shadowRatio + Math.tan(stringAngleRad))) * RTD;
  let asr = noon + getHourAngle(-asrAngleDeg, declination, lat);
  if (isNaN(asr)) {
    asr = noon + (sunset - noon) * 0.5; 
  }

  let midnightStart = sunset; 
  let midnightEnd = isShiaMode ? fajr : sunrise;
  if (midnightEnd < midnightStart) {
    midnightEnd += 24.0;
  }
  let midnightDuration = midnightEnd - midnightStart;
  let midnight = fixHour(midnightStart + (midnightDuration / 2.0));

  const mOff = {
    imsak: manualOffsets.imsak || 0,
    fajr: manualOffsets.fajr || 0,
    sunrise: manualOffsets.sunrise || 0,
    dhuhr: manualOffsets.dhuhr || 0,
    asr: manualOffsets.asr || 0,
    sunset: manualOffsets.sunset || 0,
    maghrib: manualOffsets.maghrib || 0,
    isha: manualOffsets.isha || 0,
    midnight: manualOffsets.midnight || 0
  };

  const rawHours = {
    imsak: getLocalHourFloat(imsakTime, tzOffset, lon, mOff.imsak),
    fajr: getLocalHourFloat(fajr, tzOffset, lon, mOff.fajr),
    isfar: isfarTime ? getLocalHourFloat(isfarTime, tzOffset, lon, 0) : null,
    sunrise: getLocalHourFloat(sunrise, tzOffset, lon, mOff.sunrise),
    dhuhr: getLocalHourFloat(dhuhr, tzOffset, lon, mOff.dhuhr),
    asr: getLocalHourFloat(asr, tzOffset, lon, mOff.asr),
    sunset: getLocalHourFloat(sunset, tzOffset, lon, mOff.sunset),
    maghrib: getLocalHourFloat(maghrib, tzOffset, lon, mOff.maghrib),
    isha: getLocalHourFloat(isha, tzOffset, lon, mOff.isha),
    midnight: getLocalHourFloat(midnight, tzOffset, lon, mOff.midnight)
  };

  return {
    imsak: formatHourString(rawHours.imsak, use24Hour),
    fajr: formatHourString(rawHours.fajr, use24Hour),
    isfar: rawHours.isfar ? formatHourString(rawHours.isfar, use24Hour) : null,
    sunrise: formatHourString(rawHours.sunrise, use24Hour),
    dhuhr: formatHourString(rawHours.dhuhr, use24Hour),
    asr: formatHourString(rawHours.asr, use24Hour),
    sunset: formatHourString(rawHours.sunset, use24Hour),
    maghrib: formatHourString(rawHours.maghrib, use24Hour),
    isha: formatHourString(rawHours.isha, use24Hour),
    midnight: formatHourString(rawHours.midnight, use24Hour),
    rawHours,
    isHighLatWarning: isHighLatRegion,
    isSummerAnomaly: isSummerAnomaly,
    appliedRule: highLatRule
  };
};

/* ========================================================================
   🚀 المكون الرئيسي للتطبيق (Main Unified Application)
   ======================================================================== */
export default function App() {
  const [cityInput, setCityInput] = useState('');
  const [locationInfo, setLocationInfo] = useState(null); 
  const [prayerTimes, setPrayerTimes] = useState(null);
  const [monthlyTimes, setMonthlyTimes] = useState([]);
  const [targetDateObj, setTargetDateObj] = useState(new Date()); 
  
  // إعدادات المنظومة المشتركة (المعيار الفقهي والزوايا وتعديل الهجري والدقائق والتوقيت الصيفي)
  const [method, setMethod] = useState('alkafeel'); 
  const [asrMethod, setAsrMethod] = useState('shafi'); 
  const [highLatRule, setHighLatRule] = useState('aqrab_48'); 
  const [hijriAdjustment, setHijriAdjustment] = useState(0);
  const [dataSource, setDataSource] = useState('offline'); 
  const [dstMode, setDstMode] = useState('auto'); // 'auto' | 'winter' | 'summer'
  const [use24Hour, setUse24Hour] = useState(false);
  const [manualOffsets, setManualOffsets] = useState({ imsak: 0, fajr: 0, sunrise: 0, dhuhr: 0, asr: 0, sunset: 0, maghrib: 0, isha: 0, midnight: 0 });
  const [showAdvancedAlkafeel, setShowAdvancedAlkafeel] = useState(false);

  // إعدادات V7 لشاشة القوس والأذان والإشعارات وإظهار الصلوات
  const [showExtraPrayers, setShowExtraPrayers] = useState(false);
  const [imsakDisplayMode, setImsakDisplayMode] = useState('ramadan_auto'); // 'ramadan_auto' | 'always' | 'hidden'
  const [visibleCardsPrayers, setVisibleCardsPrayers] = useState({
    fajr: true, sunrise: true, dhuhr: true, sunset: true, maghrib: true, midnight: true
  });
  const [uiPrayers, setUiPrayers] = useState({
    imsak: true, fajr: true, sunrise: true, dhuhr: true, asr: false, sunset: true, maghrib: true, isha: false, midnight: true
  });
  const [liveEnabled, setLiveEnabled] = useState(true);
  const [reminderMinutes, setReminderMinutes] = useState(15);
  const [livePrayers, setLivePrayers] = useState({ imsak: true, fajr: true, dhuhr: true, maghrib: true });
  const [alarmEnabled, setAlarmEnabled] = useState(true);
  const [alarmPrayers, setAlarmPrayers] = useState({ imsak: false, fajr: true, dhuhr: true, maghrib: true });

  const [openSettingsSections, setOpenSettingsSections] = useState({
    dailyTimesBlock: false,
    offset: false,
    timeAdjust: false,
    uiPrayers: false,
    liveNotification: false,
    alarms: false
  });

  // حالات شاشة القوس (اليوم)
  const [arcSlideIndex, setArcSlideIndex] = useState(-1);
  const [lastAutoTargetId, setLastAutoTargetId] = useState(null);
  const [arcShowCalendarModal, setArcShowCalendarModal] = useState(false);
  const [touchStartX, setTouchStartX] = useState(null);
  const [touchEndX, setTouchEndX] = useState(null);

  // حالات شاشة التقويم الموحد
  const [calendarMode, setCalendarMode] = useState('hijri');
  const [calendarGregorianDate, setCalendarGregorianDate] = useState(new Date());
  const [calendarHijriDayOffset, setCalendarHijriDayOffset] = useState(0);

  // حالات القبلة والمراقد والأدعية والمسبحة والفقه
  const [selectedShrineId, setSelectedShrineId] = useState('kaaba');
  const [openedDua, setOpenedDua] = useState(null);
  const [tasbihStage, setTasbihStage] = useState(0);
  const [tasbihCount, setTasbihCount] = useState(0);
  const [expandedFiqhId, setExpandedFiqhId] = useState('all_standards_reference');

  const [weather, setWeather] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [offlineSearchWarning, setOfflineSearchWarning] = useState(null);
  const [timeOfDay, setTimeOfDay] = useState('day');
  const [isOfflineMode, setIsOfflineMode] = useState(!navigator.onLine);
  const [activeTab, setActiveTab] = useState('sky_arc');
  const [realTimeClock, setRealTimeClock] = useState('');

  const arcPrayerNavRef = useRef(null);
  const arcPrayerRefs = useRef({});
  const arcCalendarDayRefs = useRef({});
  const settingsAccordionRefs = useRef({});

  const toggleSettingsAccordion = (key) => {
    setOpenSettingsSections(prev => {
      const nextState = !prev[key];
      if (nextState && settingsAccordionRefs.current[key]) {
        setTimeout(() => {
          settingsAccordionRefs.current[key]?.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }, 120);
      }
      return { ...prev, [key]: nextState };
    });
  };

  useEffect(() => {
    const handleOnline = () => setIsOfflineMode(false);
    const handleOffline = () => setIsOfflineMode(true);
    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);
    return () => { 
      window.removeEventListener('online', handleOnline); 
      window.removeEventListener('offline', handleOffline); 
    };
  }, []);

  // تهيئة الموقع المحفوظ أو التلقائي
  useEffect(() => {
    const savedLoc = localStorage.getItem('alkf_location');
    if (savedLoc) {
      try {
        const parsed = JSON.parse(savedLoc);
        const matchedCity = offlineCitiesDB.find(c => parsed.name && parsed.name.includes(c.city));
        if (matchedCity && parsed.stdTz === undefined) {
          parsed.stdTz = matchedCity.stdTz;
          parsed.dstRule = matchedCity.dstRule;
        }
        setLocationInfo(parsed);
      } catch (e) {
        setLocationInfo({ lat: 32.6160, lon: 44.0249, name: 'كربلاء، العراق', tzOffset: 3, stdTz: 3, dstRule: 'none' });
      }
    } else {
      setLoading(true);
      const performAutoIPFetch = async () => {
        try {
          const response = await fetch('https://get.geojs.io/v1/ip/geo.json');
          const data = await response.json();
          const lat = parseFloat(data.latitude);
          const lon = parseFloat(data.longitude);
          if (!isNaN(lat) && !isNaN(lon)) {
            const geoRes = await fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lon}&format=json&accept-language=ar`);
            const geoData = await geoRes.json();
            const city = geoData.address.city || geoData.address.state || geoData.address.town || data.city || 'موقعي';
            const country = geoData.address.country || data.country || '';
            let tzOffset = Math.round(lon / 15);
            try {
              const tzRes = await fetch(`https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&timezone=auto`);
              const tzData = await tzRes.json();
              if (tzData && tzData.utc_offset_seconds !== undefined) tzOffset = tzData.utc_offset_seconds / 3600;
            } catch(e) {}
            const finalOffset = isNaN(parseFloat(tzOffset)) ? Math.round(lon / 15) : parseFloat(tzOffset);
            const inferredRule = inferDstRuleByCoords(lat, lon);
            const isCurrentlyDst = isDateInDSTOffline(new Date(), inferredRule);
            const stdTz = isCurrentlyDst ? finalOffset - 1 : finalOffset;
            setLocationInfo({ lat, lon, name: `${city}، ${country}`, tzOffset: finalOffset, stdTz, dstRule: inferredRule });
          } else {
            throw new Error();
          }
        } catch (err) {
          setLocationInfo({ lat: 32.6160, lon: 44.0249, name: 'كربلاء، العراق', tzOffset: 3, stdTz: 3, dstRule: 'none' });
        } finally {
          setLoading(false);
        }
      };
      if (navigator.onLine) performAutoIPFetch();
      else {
        setLocationInfo({ lat: 32.6160, lon: 44.0249, name: 'كربلاء، العراق', tzOffset: 3, stdTz: 3, dstRule: 'none' });
        setLoading(false);
      }
    }
  }, []);

  // استرجاع كافة الإعدادات المحفوظة
  useEffect(() => {
    try {
      const savedMethod = localStorage.getItem('alkf_method');
      const savedAsr = localStorage.getItem('alkf_asr');
      const savedHighLat = localStorage.getItem('alkf_highlat_rule');
      const savedAdj = localStorage.getItem('alkf_hijri_adj');
      const savedSrc = localStorage.getItem('alkf_source');
      const savedDst = localStorage.getItem('alkf_dst_mode');
      const savedOffsets = localStorage.getItem('alkf_manual_offsets');
      const saved24h = localStorage.getItem('alkf_use24h');
      const sExtra = localStorage.getItem('alkf_v7_extra');
      const sImsakMode = localStorage.getItem('alkf_v7_imsak_mode');
      const sVisCards = localStorage.getItem('alkf_v7_vis_cards');
      const sUiP = localStorage.getItem('alkf_v7_uiprayers');
      const sLiveEn = localStorage.getItem('alkf_v7_live_en');
      const sRemMin = localStorage.getItem('alkf_v7_rem_min');
      const sLiveP = localStorage.getItem('alkf_v7_live_p');
      const sAlarmEn = localStorage.getItem('alkf_v7_alarm_en');
      const sAlarmP = localStorage.getItem('alkf_v7_alarm_p');

      if (savedMethod) setMethod(savedMethod);
      if (savedAsr) setAsrMethod(savedAsr);
      if (savedHighLat) setHighLatRule(savedHighLat);
      if (savedAdj) setHijriAdjustment(parseInt(savedAdj, 10));
      if (savedSrc) setDataSource(savedSrc);
      if (savedDst) setDstMode(savedDst);
      if (savedOffsets) setManualOffsets(prev => ({ ...prev, ...JSON.parse(savedOffsets) }));
      if (saved24h) setUse24Hour(saved24h === 'true');
      if (sExtra) setShowExtraPrayers(sExtra === 'true');
      if (sImsakMode) setImsakDisplayMode(sImsakMode);
      if (sVisCards) setVisibleCardsPrayers(prev => ({ ...prev, ...JSON.parse(sVisCards) }));
      if (sUiP) setUiPrayers(prev => ({ ...prev, ...JSON.parse(sUiP) }));
      if (sLiveEn) setLiveEnabled(sLiveEn === 'true');
      if (sRemMin) setReminderMinutes(parseInt(sRemMin, 10));
      if (sLiveP) setLivePrayers(JSON.parse(sLiveP));
      if (sAlarmEn) setAlarmEnabled(sAlarmEn === 'true');
      if (sAlarmP) setAlarmPrayers(JSON.parse(sAlarmP));
    } catch (e) {}
  }, []);

  // ساعة الجهاز الحية حسب المنطقة الزمنية والتوقيت الصيفي/الشتوي الذكي للمدينة
  useEffect(() => {
    if (!locationInfo) return;
    const updateClock = () => {
      const now = new Date(); 
      const effectiveTz = getEffectiveTimezoneOffset(locationInfo, now, dstMode);
      const utcTime = now.getTime() + (now.getTimezoneOffset() * 60000);
      const targetNow = new Date(utcTime + (3600000 * effectiveTz));
      setTargetDateObj(targetNow);

      let h = targetNow.getHours();
      const m = String(targetNow.getMinutes()).padStart(2, '0');
      const s = String(targetNow.getSeconds()).padStart(2, '0');
      if (use24Hour) {
        setRealTimeClock(`${String(h).padStart(2, '0')}:${m}:${s}`);
      } else {
        const suffix = h >= 12 ? 'م' : 'ص';
        h = h % 12 || 12;
        setRealTimeClock(`${String(h).padStart(2, '0')}:${m}:${s} ${suffix}`);
      }
    };
    updateClock();
    const timer = setInterval(updateClock, 1000);
    return () => clearInterval(timer);
  }, [locationInfo, use24Hour, dstMode]);

  // جلب الطقس الذكي الفعلي المرتبط بإحداثيات المدينة الحالية
  const fetchWeatherSmart = useCallback((lat, lon) => {
    if (isOfflineMode) return;
    const coordKey = `${lat.toFixed(2)}_${lon.toFixed(2)}`;
    const lastCoord = localStorage.getItem('alkf_weather_coord');
    const lastFetch = localStorage.getItem('alkf_weather_time');
    const cachedWeather = localStorage.getItem('alkf_weather_data');
    const now = new Date().getTime();

    if (cachedWeather && lastFetch && lastCoord === coordKey && (now - parseInt(lastFetch, 10) < 1800000)) {
      try {
        setWeather(JSON.parse(cachedWeather));
        return;
      } catch (e) {}
    }

    fetch(`https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&current_weather=true&timezone=auto`)
      .then(res => res.json())
      .then(data => {
        if (data && data.current_weather) {
          setWeather(data.current_weather);
          localStorage.setItem('alkf_weather_data', JSON.stringify(data.current_weather));
          localStorage.setItem('alkf_weather_time', now.toString());
          localStorage.setItem('alkf_weather_coord', coordKey);
        }
      }).catch(() => {});
  }, [isOfflineMode]);

  // جلب الأوقات عبر API (عند اختيار المستخدم لمصدر API في الضبط فقط)
  const fetchOnlinePrayerTimes = async (lat, lon, dateObj, currentMethod, currentAsr, currentHighLat, tzOffset, offsets, is24h) => {
    try {
      const apiMethodMap = { 
        'ummalqura': 4, 'mwl': 3, 'egypt': 5, 'isna': 2, 'karachi': 1, 
        'tehran': 7, 'leufa': 0, 'alkafeel': 0, 'london_shia': 0, 'france_12': 12, 'european_council': 3 
      };
      const apiMethod = apiMethodMap[currentMethod] || 0;
      const dateStr = `${dateObj.getDate()}-${dateObj.getMonth()+1}-${dateObj.getFullYear()}`;
      
      const res = await fetch(`https://api.aladhan.com/v1/timings/${dateStr}?latitude=${lat}&longitude=${lon}&method=${apiMethod}`);
      if (!res.ok) throw new Error('API Server Error');
      const data = await res.json();
      const t = data.data.timings;
      
      const parseApiFloat = (time24, key) => {
        let [h, m] = time24.split(' ')[0].split(':').map(Number);
        const off = offsets[key] || 0;
        return fixHour(h + (m + off) / 60.0);
      };

      const localEngine = calculatePrayerTimesLocal(dateObj, lat, lon, tzOffset, currentMethod, currentAsr, currentHighLat, offsets, is24h);
      const rawHours = {
        imsak: parseApiFloat(t.Imsak, 'imsak'),
        fajr: parseApiFloat(t.Fajr, 'fajr'),
        isfar: localEngine.rawHours.isfar,
        sunrise: parseApiFloat(t.Sunrise, 'sunrise'),
        dhuhr: parseApiFloat(t.Dhuhr, 'dhuhr'),
        asr: parseApiFloat(t.Asr, 'asr'),
        sunset: parseApiFloat(t.Sunset, 'sunset'),
        maghrib: parseApiFloat(t.Maghrib, 'maghrib'),
        isha: parseApiFloat(t.Isha, 'isha'),
        midnight: localEngine.rawHours.midnight
      };

      if (['alkafeel', 'leufa', 'tehran', 'london_shia'].includes(currentMethod) || localEngine.isSummerAnomaly) {
        return localEngine;
      }

      return {
        imsak: formatHourString(rawHours.imsak, is24h),
        fajr: formatHourString(rawHours.fajr, is24h),
        isfar: null,
        sunrise: formatHourString(rawHours.sunrise, is24h),
        dhuhr: formatHourString(rawHours.dhuhr, is24h),
        asr: formatHourString(rawHours.asr, is24h),
        sunset: formatHourString(rawHours.sunset, is24h),
        maghrib: formatHourString(rawHours.maghrib, is24h),
        isha: formatHourString(rawHours.isha, is24h),
        midnight: formatHourString(rawHours.midnight, is24h),
        rawHours,
        isHighLatWarning: localEngine.isHighLatWarning,
        isSummerAnomaly: localEngine.isSummerAnomaly,
        appliedRule: localEngine.appliedRule
      };
    } catch (e) {
      return null; 
    }
  };

  // توليد جدول الشهر الهجري أو الميلادي لأي شهر وسنة مع مراعاة التوقيت الصيفي/الشتوي لكل يوم
  const generateCalendarMonthDays = useCallback((loc, adjustment, currentMethod, currentAsr, currentHighLat, offsets, is24h, mode, gDate, hDayOffset, currentDstMode) => {
    const lat = loc.lat;
    const lon = loc.lon;
    const isShiaMode = ['alkafeel', 'leufa', 'tehran', 'london_shia'].includes(currentMethod);
    let daysArray = [];

    if (mode === 'hijri') {
      const refDate = new Date(targetDateObj);
      refDate.setDate(refDate.getDate() + hDayOffset);
      const targetHijri = getHijriDateSafe(refDate, isShiaMode, adjustment);
      const startGregorian = new Date(refDate);
      startGregorian.setDate(startGregorian.getDate() - targetHijri.day + 1);

      for (let i = 0; i < 30; i++) {
        const loopDate = new Date(startGregorian);
        loopDate.setDate(loopDate.getDate() + i);
        const hData = getHijriDateSafe(loopDate, isShiaMode, adjustment);
        if (i > 0 && hData.day === 1) break;
        const dayTz = getEffectiveTimezoneOffset(loc, loopDate, currentDstMode);
        daysArray.push({
          date: loopDate,
          hijriDay: hData.day,
          hijriMonthName: hData.monthName,
          hijriYear: hData.year,
          hijriStr: `${hData.day} ${hData.monthName}`,
          times: calculatePrayerTimesLocal(loopDate, lat, lon, dayTz, currentMethod, currentAsr, currentHighLat, offsets, is24h)
        });
      }
    } else {
      const year = gDate.getFullYear();
      const month = gDate.getMonth();
      const daysInMonth = new Date(year, month + 1, 0).getDate();
      for (let d = 1; d <= daysInMonth; d++) {
        const loopDate = new Date(year, month, d, 12, 0, 0);
        const hData = getHijriDateSafe(loopDate, isShiaMode, adjustment);
        const dayTz = getEffectiveTimezoneOffset(loc, loopDate, currentDstMode);
        daysArray.push({
          date: loopDate,
          hijriDay: hData.day,
          hijriMonthName: hData.monthName,
          hijriYear: hData.year,
          hijriStr: `${hData.day} ${hData.monthName}`,
          times: calculatePrayerTimesLocal(loopDate, lat, lon, dayTz, currentMethod, currentAsr, currentHighLat, offsets, is24h)
        });
      }
    }
    return daysArray;
  }, [targetDateObj]);

  // حساب الأوقات (أوفلاين أو عبر API)
  const computeData = useCallback(async (loc, adjustment, currentMethod, currentAsr, currentHighLat, src, offsets, is24h, currentDstMode) => {
    const lat = loc.lat;
    const lon = loc.lon;
    const now = new Date();
    const finalTz = getEffectiveTimezoneOffset(loc, now, currentDstMode);
    const utcTime = now.getTime() + (now.getTimezoneOffset() * 60000);
    const targetDate = new Date(utcTime + (3600000 * finalTz));
    setTargetDateObj(targetDate);

    const localCalc = calculatePrayerTimesLocal(targetDate, lat, lon, finalTz, currentMethod, currentAsr, currentHighLat, offsets, is24h);

    if (src === 'api') {
      if (isOfflineMode) {
        setPrayerTimes(null);
      } else {
        setLoading(true);
        const apiTimes = await fetchOnlinePrayerTimes(lat, lon, targetDate, currentMethod, currentAsr, currentHighLat, finalTz, offsets, is24h);
        if (apiTimes) setPrayerTimes(apiTimes);
        else setPrayerTimes(localCalc);
        setLoading(false);
      }
    } else {
      setPrayerTimes(localCalc);
    }

    const hour = targetDate.getHours();
    if (hour >= 4 && hour < 7) setTimeOfDay('dawn'); 
    else if (hour >= 7 && hour < 17) setTimeOfDay('day'); 
    else if (hour >= 17 && hour < 19) setTimeOfDay('dusk'); 
    else setTimeOfDay('night');
  }, [isOfflineMode]);

  useEffect(() => {
    if (locationInfo && !isNaN(locationInfo.lat) && !isNaN(locationInfo.lon)) {
      computeData(locationInfo, hijriAdjustment, method, asrMethod, highLatRule, dataSource, manualOffsets, use24Hour, dstMode);
      fetchWeatherSmart(locationInfo.lat, locationInfo.lon);
      try {
        localStorage.setItem('alkf_location', JSON.stringify(locationInfo));
        localStorage.setItem('alkf_method', method);
        localStorage.setItem('alkf_asr', asrMethod);
        localStorage.setItem('alkf_highlat_rule', highLatRule);
        localStorage.setItem('alkf_hijri_adj', hijriAdjustment.toString());
        localStorage.setItem('alkf_source', dataSource);
        localStorage.setItem('alkf_dst_mode', dstMode);
        localStorage.setItem('alkf_manual_offsets', JSON.stringify(manualOffsets));
        localStorage.setItem('alkf_use24h', use24Hour.toString());
        localStorage.setItem('alkf_v7_extra', showExtraPrayers.toString());
        localStorage.setItem('alkf_v7_imsak_mode', imsakDisplayMode);
        localStorage.setItem('alkf_v7_vis_cards', JSON.stringify(visibleCardsPrayers));
        localStorage.setItem('alkf_v7_uiprayers', JSON.stringify(uiPrayers));
        localStorage.setItem('alkf_v7_live_en', liveEnabled.toString());
        localStorage.setItem('alkf_v7_rem_min', reminderMinutes.toString());
        localStorage.setItem('alkf_v7_live_p', JSON.stringify(livePrayers));
        localStorage.setItem('alkf_v7_alarm_en', alarmEnabled.toString());
        localStorage.setItem('alkf_v7_alarm_p', JSON.stringify(alarmPrayers));
      } catch (e) {}
    }
  }, [locationInfo, method, asrMethod, highLatRule, hijriAdjustment, dataSource, dstMode, manualOffsets, use24Hour, showExtraPrayers, imsakDisplayMode, visibleCardsPrayers, uiPrayers, liveEnabled, reminderMinutes, livePrayers, alarmEnabled, alarmPrayers, computeData, fetchWeatherSmart]);

  useEffect(() => {
    if (locationInfo && !isNaN(locationInfo.lat) && !isNaN(locationInfo.lon)) {
      setMonthlyTimes(generateCalendarMonthDays(locationInfo, hijriAdjustment, method, asrMethod, highLatRule, manualOffsets, use24Hour, calendarMode, calendarGregorianDate, calendarHijriDayOffset, dstMode));
    }
  }, [locationInfo, hijriAdjustment, method, asrMethod, highLatRule, manualOffsets, use24Hour, calendarMode, calendarGregorianDate, calendarHijriDayOffset, dstMode, generateCalendarMonthDays]);

  useEffect(() => {
    const isShia = ['alkafeel', 'leufa', 'tehran', 'london_shia'].includes(method);
    if (isShia) {
      if (method === 'alkafeel' || method === 'london_shia') setHighLatRule('aqrab_48'); 
      else setHighLatRule('aqrab_45'); 
    } else {
      setHighLatRule('fixed_90'); 
    }
  }, [method]);

  const fetchLocationByIP = async () => {
    if (isOfflineMode) return;
    setLoading(true); setError(null); setOfflineSearchWarning(null);
    try {
      const response = await fetch('https://get.geojs.io/v1/ip/geo.json'); 
      const data = await response.json();
      const lat = parseFloat(data.latitude);
      const lon = parseFloat(data.longitude);
      const geoRes = await fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lon}&format=json&accept-language=ar`); 
      const geoData = await geoRes.json();
      const city = geoData.address.city || geoData.address.state || data.city;
      const country = geoData.address.country || data.country;
      const tzRes = await fetch(`https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&timezone=auto`); 
      const tzData = await tzRes.json();
      const apiTz = tzData.utc_offset_seconds / 3600;
      const rule = inferDstRuleByCoords(lat, lon);
      const stdTz = isDateInDSTOffline(new Date(), rule) ? apiTz - 1 : apiTz;
      setLocationInfo({ lat, lon, name: `${city}، ${country}`, tzOffset: apiTz, stdTz, dstRule: rule });
    } catch (err) { 
      setError('فشل جلب الموقع الجغرافي.'); 
    } finally { 
      setLoading(false); 
    }
  };

  const fetchLocationByGPS = () => {
    if (!('geolocation' in navigator)) { setError('لا يوجد مستشعر GPS.'); return; }
    setLoading(true); setError(null); setOfflineSearchWarning(null);
    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const lat = position.coords.latitude; 
        const lon = position.coords.longitude;
        let nearestCity = offlineCitiesDB[0];
        let minDistance = calculateDistance(lat, lon, nearestCity.lat, nearestCity.lon);
        for (let i = 1; i < offlineCitiesDB.length; i++) {
          let dist = calculateDistance(lat, lon, offlineCitiesDB[i].lat, offlineCitiesDB[i].lon);
          if (dist < minDistance) { minDistance = dist; nearestCity = offlineCitiesDB[i]; }
        }
        if (isOfflineMode) {
          const locationName = minDistance < 25 ? `${nearestCity.city}، ${nearestCity.country} (أوفلاين)` : `بالقرب من ${nearestCity.city}، ${nearestCity.country} (أوفلاين)`;
          setLocationInfo({ lat, lon, name: locationName, tzOffset: nearestCity.stdTz, stdTz: nearestCity.stdTz, dstRule: nearestCity.dstRule });
          setLoading(false); return;
        }
        try {
          const geoRes = await fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lon}&format=json&accept-language=ar`); 
          const geoData = await geoRes.json();
          const tzRes = await fetch(`https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&timezone=auto`); 
          const tzData = await tzRes.json();
          const city = geoData.address.city || geoData.address.town || 'موقعي';
          const country = geoData.address.country || '';
          const apiTz = tzData.utc_offset_seconds / 3600;
          const rule = inferDstRuleByCoords(lat, lon);
          const stdTz = isDateInDSTOffline(new Date(), rule) ? apiTz - 1 : apiTz;
          setLocationInfo({ lat, lon, name: `${city}، ${country} (GPS)`, tzOffset: apiTz, stdTz, dstRule: rule });
        } catch (e) { 
          setLocationInfo({ lat, lon, name: nearestCity.city, tzOffset: nearestCity.stdTz, stdTz: nearestCity.stdTz, dstRule: nearestCity.dstRule }); 
        } finally { 
          setLoading(false); 
        }
      },
      () => { setError('برجاء تفعيل الـ GPS.'); setLoading(false); }, 
      { enableHighAccuracy: true, timeout: 15000 }
    );
  };

  const searchCity = async (e) => {
    e.preventDefault(); 
    if (!cityInput.trim()) return;
    setOfflineSearchWarning(null);
    setError(null);

    const localMatch = offlineCitiesDB.find(c => c.city.toLowerCase().includes(cityInput.trim().toLowerCase()) || cityInput.trim().toLowerCase().includes(c.city.toLowerCase()) || c.country.includes(cityInput.trim()));
    if (isOfflineMode || localMatch) {
      if (localMatch) { 
        setLocationInfo({ 
          lat: localMatch.lat, 
          lon: localMatch.lon, 
          name: `${localMatch.city}، ${localMatch.country}${isOfflineMode ? ' (أوفلاين)' : ''}`, 
          tzOffset: localMatch.stdTz,
          stdTz: localMatch.stdTz,
          dstRule: localMatch.dstRule
        }); 
        setCityInput('');
        return;
      } else if (isOfflineMode) { 
        setOfflineSearchWarning(`أنت غير متصل بالإنترنت. يرجى إدخال اسم مدينة محفوظة مسبقاً (مثل: كربلاء، النجف، بغداد، مكة، لندن، باريس، ستوكهولم) أو استخدام الـ GPS لمعرفة مكانك التقريبي لحساب الأوقات.`);
        return;
      }
    }
    setLoading(true);
    try {
      const geoRes = await fetch(`https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(cityInput)}&format=json&accept-language=ar&limit=1`); 
      const geoData = await geoRes.json();
      if (!geoData.length) throw new Error('المدينة غير موجودة.');
      const { lat, lon, display_name } = geoData[0]; 
      const name = display_name.split(',').slice(0, 2).join('،');
      let tzOffset = Math.round(parseFloat(lon) / 15);
      try {
        const tzRes = await fetch(`https://api.open-meteo.com/v1/forecast?latitude=${lat}&longitude=${lon}&timezone=auto`); 
        const tzData = await tzRes.json();
        if (tzData && tzData.utc_offset_seconds !== undefined) tzOffset = tzData.utc_offset_seconds / 3600;
      } catch (e) {}
      const nLat = parseFloat(lat);
      const nLon = parseFloat(lon);
      const rule = inferDstRuleByCoords(nLat, nLon);
      const stdTz = isDateInDSTOffline(new Date(), rule) ? tzOffset - 1 : tzOffset;
      setLocationInfo({ lat: nLat, lon: nLon, name, tzOffset, stdTz, dstRule: rule }); 
      setCityInput('');
    } catch (err) { 
      setError(err.message); 
    } finally { 
      setLoading(false); 
    }
  };

  // مسبحة السيدة فاطمة الزهراء (ع)
  const tasbihSteps = [
    { label: 'الله أكبر', max: 34, sub: 'تكبيرات السيدة الزهراء (ع)' },
    { label: 'الحمد لله', max: 33, sub: 'تحميدات السيدة الزهراء (ع)' },
    { label: 'سبحان الله', max: 33, sub: 'تسبيحات السيدة الزهراء (ع)' }
  ];
  const handleTasbihIncrement = () => {
    const cur = tasbihSteps[tasbihStage];
    if (tasbihCount + 1 < cur.max) {
      setTasbihCount(c => c + 1);
    } else if (tasbihStage < 2) {
      setTasbihStage(s => s + 1);
      setTasbihCount(0);
    } else {
      setTasbihStage(0);
      setTasbihCount(0);
    }
  };

  const isShia = ['alkafeel', 'leufa', 'tehran', 'london_shia'].includes(method);
  const currentHijri = locationInfo ? getHijriDateSafe(targetDateObj, isShia, hijriAdjustment) : { day: 18, monthName: "محرم", year: 1448, monthNum: 1 };
  const moonInfo = getMoonDetailsByHijriDay(currentHijri.day);

  const themeClasses = { 
    dawn: 'bg-[#0f172a] text-indigo-100', 
    day: 'bg-[#0b0f19] text-slate-100', 
    dusk: 'bg-[#1a0f1d] text-rose-100', 
    night: 'bg-[#070b13] text-zinc-300' 
  };

  // قائمة الصلوات المعروضة في شريط المربعات السفلي والتقويم (الإمساك يظهر افتراضياً في رمضان فقط، والغروب يظهر في المعايير الشيعية التي تميز بين الغروب والمغرب)
  const isRamadanMonth = currentHijri.monthNum === 9;
  const shouldShowImsak = imsakDisplayMode === 'always' || (imsakDisplayMode === 'ramadan_auto' && isRamadanMonth);

  const visibleArcPrayers = PRAYERS_BASE_V7.filter(p => {
    if (p.id === 'imsak') return shouldShowImsak;
    if (p.id === 'sunset') return isShia && visibleCardsPrayers.sunset !== false;
    if (p.optional) return showExtraPrayers;
    return visibleCardsPrayers[p.id] !== false;
  });

  // دالة تمركز بطاقة الصلاة النشطة في وسط شريط المربعات السفلية بحساب رياضي دقيق وسلس (دون تقييد التمرير الحر للمستخدم)
  const centerActivePrayerCard = useCallback((prayerId) => {
    const container = arcPrayerNavRef.current;
    const card = arcPrayerRefs.current[prayerId];
    if (!container || !card) return;
    try {
      const containerRect = container.getBoundingClientRect();
      const cardRect = card.getBoundingClientRect();
      const deltaCenter = (cardRect.left + cardRect.width / 2) - (containerRect.left + containerRect.width / 2);
      container.scrollBy({ left: deltaCenter, behavior: 'smooth' });
    } catch (e) {
      card.scrollIntoView({ behavior: 'smooth', block: 'nearest', inline: 'center' });
    }
  }, []);

  // مراقبة حلول وقت الصلاة تلقائياً بالثانية لتحريك العداد التنازلي والمربع المختص إلى المنتصف أمام المستخدم بسلاسة
  useEffect(() => {
    if (activeTab !== 'sky_arc' || !prayerTimes) return;
    const currS = targetDateObj.getHours() * 3600 + targetDateObj.getMinutes() * 60 + targetDateObj.getSeconds();
    const activeCarouselList = visibleArcPrayers.filter(p => uiPrayers[p.id]);
    if (activeCarouselList.length === 0) return;
    
    let autoIdx = 0;
    for (let i = 0; i < activeCarouselList.length; i++) {
      const pid = activeCarouselList[i].id;
      const pSec = Math.floor((prayerTimes.rawHours[pid] || 0) * 3600);
      if (currS < pSec) { autoIdx = i; break; }
    }
    const currentAutoPrayer = activeCarouselList[autoIdx];

    // إذا حلّ وقت الصلاة وانتقل المؤشر التلقائي إلى الصلاة التالية، نعيد العداد تلقائياً إليها ونوسطن مربعها
    if (currentAutoPrayer && currentAutoPrayer.id !== lastAutoTargetId) {
      setLastAutoTargetId(currentAutoPrayer.id);
      if (arcSlideIndex !== -1) {
        setArcSlideIndex(-1);
      }
      const timer = setTimeout(() => centerActivePrayerCard(currentAutoPrayer.id), 50);
      return () => clearTimeout(timer);
    }
  }, [targetDateObj, activeTab, prayerTimes, visibleArcPrayers, uiPrayers, lastAutoTargetId, arcSlideIndex, centerActivePrayerCard]);

  // تمركز المربع المختص في الوسط عند تغيير العداد التنازلي يدوياً أو عند فتح شاشة اليوم
  useEffect(() => {
    if (activeTab !== 'sky_arc' || !prayerTimes) return;
    const currS = targetDateObj.getHours() * 3600 + targetDateObj.getMinutes() * 60 + targetDateObj.getSeconds();
    const activeCarouselList = visibleArcPrayers.filter(p => uiPrayers[p.id]);
    if (activeCarouselList.length === 0) return;

    let autoIdx = 0;
    for (let i = 0; i < activeCarouselList.length; i++) {
      const pid = activeCarouselList[i].id;
      const pSec = Math.floor((prayerTimes.rawHours[pid] || 0) * 3600);
      if (currS < pSec) { autoIdx = i; break; }
    }
    const activeIdx = arcSlideIndex === -1 ? autoIdx : arcSlideIndex;
    const safeIdx = activeIdx >= activeCarouselList.length ? 0 : activeIdx;
    const targetP = activeCarouselList[safeIdx];
    if (targetP) {
      const timer = setTimeout(() => centerActivePrayerCard(targetP.id), 40);
      return () => clearTimeout(timer);
    }
  }, [arcSlideIndex, activeTab, centerActivePrayerCard]);

  useEffect(() => {
    if (arcShowCalendarModal && arcCalendarDayRefs.current[currentHijri.day]) {
      setTimeout(() => {
        arcCalendarDayRefs.current[currentHijri.day]?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }, 120);
    }
  }, [arcShowCalendarModal, currentHijri.day]);

  if (!locationInfo) {
    return (
      <div className="min-h-screen bg-[#070b13] flex items-center justify-center">
        <Loader2 className="animate-spin text-teal-500" size={48} />
      </div>
    );
  }

  const screenTitles = {
    settings_hub: 'الضبط والأوقات (مرصد الكفيل 3D)',
    calendar_hub: 'التقويم الهجري والميلادي والإمساكية',
    qibla_hub: 'البوصلة السماوية ومراقد أهل البيت (ع)',
    duas_hub: 'مكتبة الأدعية والزيارات المأثورة',
    tasbih_hub: 'مسبحة السيدة فاطمة الزهراء (ع)',
    fiqh_hub: 'دليل مقارنة المذاهب وفقه المواقيت'
  };

  const daysOfWeekAr = ["الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت"];
  const gregorianMonthsAr = ["يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو", "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"];

  const currSecForBg = targetDateObj.getHours() * 3600 + targetDateObj.getMinutes() * 60 + targetDateObj.getSeconds();
  let topC = [4, 6, 15], botC = [10, 10, 25], glowC = 'rgba(50,80,200,0.2)';
  for (let i = 0; i < skyGradients.length - 1; i++) {
    if (currSecForBg >= skyGradients[i].t && currSecForBg <= skyGradients[i + 1].t) {
      const f = (currSecForBg - skyGradients[i].t) / (skyGradients[i + 1].t - skyGradients[i].t);
      topC = interpolateColor(skyGradients[i].bgTop, skyGradients[i + 1].bgTop, f);
      botC = interpolateColor(skyGradients[i].bgBot, skyGradients[i + 1].bgBot, f);
      glowC = f < 0.5 ? skyGradients[i].glowColor : skyGradients[i + 1].glowColor;
      break;
    }
  }

  const effectiveTzNow = getEffectiveTimezoneOffset(locationInfo, targetDateObj, dstMode);
  const currentSeasonInfo = getSeasonOffline(targetDateObj, locationInfo.lat);
  const effectiveWeatherCode = weather?.weathercode !== undefined
    ? weather.weathercode
    : (currentSeasonInfo.id === 'winter' ? 2 : 0);

  return (
    <div 
      dir="rtl" 
      className={`min-h-[100dvh] w-full flex flex-col items-center relative overflow-x-hidden select-none font-sans ${activeTab === 'sky_arc' ? 'text-white' : `transition-colors duration-1000 ${themeClasses[timeOfDay]}`}`}
      style={activeTab === 'sky_arc' ? { 
        background: `linear-gradient(180deg, rgb(${topC.join(',')}), rgb(${botC.join(',')}))`,
        transition: 'background 1s ease'
      } : undefined}
    >
      <style dangerouslySetInnerHTML={{__html: `
        @import url('https://fonts.googleapis.com/css2?family=Reem+Kufi:wght@400;500;600;700&family=Amiri:wght@400;700&display=swap');
        .font-reem { font-family: 'Reem Kufi', sans-serif; }
        .font-amiri { font-family: 'Amiri', serif; }
        .fluid-spring { transition: all 0.55s cubic-bezier(0.22, 1, 0.36, 1); }
        .animate-slideUpFade { animation: slideUpFade 0.35s cubic-bezier(0.22, 1, 0.36, 1) forwards; }
        @keyframes slideUpFade { 0% { opacity: 0; transform: translateY(14px); } 100% { opacity: 1; transform: translateY(0); } }
        @keyframes rainFallReal { 0% { background-position: 0 0; } 100% { background-position: -24px 90px; } }
        @keyframes snowFallReal { 0% { background-position: 0 0; } 100% { background-position: 40px 80px; } }
        @keyframes cloudDriftSlow { 0% { transform: translateX(-18px); } 100% { transform: translateX(22px); } }
        .accordion-content { overflow: hidden; transition: max-height 0.55s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.4s ease; max-height: 0; opacity: 0; }
        .accordion-content.open { max-height: 1100px; opacity: 1; }
        .hide-scrollbar::-webkit-scrollbar { display: none; }
        .hide-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
      `}} />

      {activeTab === 'sky_arc' ? (
        <>
          <div className="absolute inset-0 z-0 pointer-events-none" style={{ background: `radial-gradient(circle at 50% 35%, ${glowC} 0%, transparent 80%)`, opacity: 0.9 }}></div>
          <WeatherEffects code={effectiveWeatherCode} isNight={timeOfDay === 'night'} />
        </>
      ) : (
        <>
          <div className="fixed top-[-25%] left-[-15%] w-[75vw] h-[75vw] bg-teal-500/5 rounded-full blur-[140px] pointer-events-none z-0"></div>
          <div className="fixed bottom-[-25%] right-[-15%] w-[75vw] h-[75vw] bg-indigo-500/5 rounded-full blur-[140px] pointer-events-none z-0"></div>
        </>
      )}

      {/* شريط العنوان العائم المتجاوب (50dp - 56dp) لجميع الشاشات ما عدا شاشة اليوم */}
      {activeTab !== 'sky_arc' && (
        <header className="fixed top-2 inset-x-0 z-40 px-2 sm:px-4 flex justify-center pointer-events-none">
          <div className="pointer-events-auto w-full max-w-4xl h-[50px] sm:h-[56px] bg-[#0b1120]/90 backdrop-blur-2xl border border-white/10 rounded-full px-2.5 sm:px-4 shadow-[0_10px_30px_rgba(0,0,0,0.6)] flex items-center justify-between gap-2 fluid-spring">
            <button
              onClick={() => setActiveTab('settings_hub')}
              className="flex items-center gap-1.5 bg-teal-500/10 hover:bg-teal-500/20 active:scale-95 fluid-spring px-2.5 sm:px-3.5 py-1.5 rounded-full border border-teal-500/25 shrink-0"
            >
              <MapPin size={13} className="text-teal-400 shrink-0" />
              <span className="text-[10px] sm:text-xs font-bold text-white max-w-[76px] sm:max-w-[140px] truncate" style={{ fontFamily: 'Arial, sans-serif' }}>
                {locationInfo.name.split(' (')[0]}
              </span>
              <Sliders size={12} className="text-teal-400 shrink-0" />
            </button>

            <span className="text-[11px] sm:text-xs md:text-sm font-bold font-reem text-teal-300 truncate text-center">
              {screenTitles[activeTab]}
            </span>

            <div className="bg-white/5 px-2.5 sm:px-3 py-1.5 rounded-full border border-white/10 flex items-center gap-1.5 shrink-0">
              {weather ? (
                <>
                  {renderWeatherStatusIcon(weather.weathercode, 13)}
                  <span className="text-[10px] sm:text-xs font-bold text-white" style={{ fontFamily: 'Arial, sans-serif' }} dir="ltr">
                    {toEn(Math.round(weather.temperature))}°C
                  </span>
                </>
              ) : (
                <span className="text-[10px] sm:text-xs text-white/50 font-mono">{realTimeClock}</span>
              )}
            </div>
          </div>
        </header>
      )}

      <main className={`relative z-10 w-full flex-1 flex flex-col ${activeTab === 'sky_arc' ? 'min-h-[100dvh] justify-between pb-[108px] sm:pb-[118px]' : 'max-w-5xl pt-[68px] sm:pt-[78px] pb-[116px] sm:pb-[128px] gap-5 px-2.5 sm:px-4'}`}>
        {activeTab !== 'sky_arc' && offlineSearchWarning && (
          <div className="bg-rose-500/20 border-r-4 border-rose-500 text-rose-200 p-4 rounded-2xl flex items-start gap-3 backdrop-blur-md shadow-lg">
            <AlertCircle size={22} className="flex-shrink-0 mt-0.5" />
            <div className="text-xs md:text-sm leading-relaxed">{offlineSearchWarning}</div>
          </div>
        )}

        {activeTab !== 'sky_arc' && dataSource === 'api' && isOfflineMode && (
          <div className="bg-rose-500/20 border-r-4 border-rose-500 text-rose-200 p-4 rounded-2xl flex items-start gap-3 backdrop-blur-md shadow-xl">
            <WifiOff size={24} className="flex-shrink-0 mt-0.5 text-rose-400" />
            <div className="text-xs md:text-sm leading-relaxed">
              <strong className="block text-rose-400 mb-1">توقف جلب الإنترنت (أوفلاين)</strong>
              أنت غير متصل بالإنترنت. يرجى التبديل إلى <span className="font-bold text-teal-300">"المحرك الفلكي أوفلاين"</span> في الضبط لاستعادة دقة الحسابات فوراً.
            </div>
          </div>
        )}

        {activeTab !== 'sky_arc' && error && (
          <div className="bg-red-500/15 border-r-4 border-red-500 text-red-200 p-4 rounded-xl flex items-center justify-between text-xs md:text-sm">
            <span>{error}</span>
            <button onClick={() => setError(null)} className="text-white/50 hover:text-white font-bold px-2">✕</button>
          </div>
        )}

        {/* ====================================================================
            1️⃣ الشاشة الرئيسية: اليوم (القوس السماوي V7 الأصلي - متجاوبة من 320dp إلى الشاشات العريضة)
           ==================================================================== */}
        {activeTab === 'sky_arc' && prayerTimes && (() => {
          const tomorrowDate = new Date(targetDateObj);
          tomorrowDate.setDate(targetDateObj.getDate() + 1);
          const tomorrowTz = getEffectiveTimezoneOffset(locationInfo, tomorrowDate, dstMode);
          const tomorrowCalc = calculatePrayerTimesLocal(tomorrowDate, locationInfo.lat, locationInfo.lon, tomorrowTz, method, asrMethod, highLatRule, manualOffsets, use24Hour);
          const tomorrowHijri = getHijriDateSafe(tomorrowDate, isShia, hijriAdjustment);
          const currS = targetDateObj.getHours() * 3600 + targetDateObj.getMinutes() * 60 + targetDateObj.getSeconds();

          const tTodayS = {};
          const tTmrwS = {};
          visibleArcPrayers.forEach(p => {
            tTodayS[p.id] = Math.floor((prayerTimes.rawHours[p.id] || 0) * 3600);
            tTmrwS[p.id] = Math.floor((tomorrowCalc.rawHours[p.id] || 0) * 3600);
          });

          const activeCarouselList = visibleArcPrayers.filter(p => uiPrayers[p.id]);
          const hasAnyCountdownPrayer = activeCarouselList.length > 0;

          const getAutoIdx = () => {
            if (!hasAnyCountdownPrayer) return 0;
            for (let i = 0; i < activeCarouselList.length; i++) {
              if (currS < tTodayS[activeCarouselList[i].id]) return i;
            }
            return 0;
          };

          const activeIdx = arcSlideIndex === -1 ? getAutoIdx() : arcSlideIndex;
          const safeIdx = activeIdx >= activeCarouselList.length ? 0 : activeIdx;
          const targetP = hasAnyCountdownPrayer ? activeCarouselList[safeIdx] : null;

          let isTmrw = false;
          let diffS = 0;
          let labelText = '';

          if (targetP) {
            isTmrw = currS >= tTodayS[targetP.id];
            const targetS = isTmrw ? tTmrwS[targetP.id] : tTodayS[targetP.id];
            diffS = targetS - currS;
            if (isTmrw && currS > targetS) diffS = (86400 - currS) + targetS;
            if (diffS < 0) diffS = 0;

            labelText = `باقي على ${targetP.label}`;
            if (isTmrw) {
              const tmrwDayName = daysOfWeekAr[tomorrowDate.getDay()];
              labelText += ` غداً ${tmrwDayName} ${toEn(tomorrowHijri.day)} ${tomorrowHijri.monthName}`;
            }
          }

          const hLeft = Math.floor(diffS / 3600);
          const mLeft = Math.floor((diffS % 3600) / 60);
          const sLeft = diffS % 60;

          const fajrSec = Math.floor((prayerTimes.rawHours.fajr || 5) * 3600);
          const sunriseSec = Math.floor((prayerTimes.rawHours.sunrise || 6.5) * 3600);
          const sunsetSec = Math.floor((prayerTimes.rawHours.sunset || 18) * 3600);
          const maghribSec = Math.floor((prayerTimes.rawHours.maghrib || 18.25) * 3600);

          const isDayCycle = currS >= sunriseSec && currS < maghribSec;
          const isNight = !isDayCycle;

          // حركة القوس السماوي دائماً من اليسار (x=30 الشرق / البداية) إلى اليمين (x=370 الغروب والمغرب / النهاية)
          // مع التمييز الذكي بين الغروب والمغرب في المعايير الشيعية (تقويم الكفيل ولوفا وطهران ولندن):
          let arcLeftLabel = 'الشروق';
          let arcLeftTimeRaw = prayerTimes.rawHours.sunrise;
          let arcRightLabel = isShia ? 'الغروب' : 'المغرب';
          let arcRightTimeRaw = isShia ? prayerTimes.rawHours.sunset : prayerTimes.rawHours.maghrib;
          let arcSubPhaseBadge = null;
          let arcPct = 0;

          if (currS >= maghribSec || currS < fajrSec) {
            // 1. الليل (من المغرب إلى الفجر): ينطلق القمر من اليسار (المغرب x=30) ويتجه نحو اليمين (الفجر x=370)
            arcLeftLabel = 'المغرب';
            arcLeftTimeRaw = prayerTimes.rawHours.maghrib;
            arcRightLabel = 'الفجر';
            arcRightTimeRaw = currS >= maghribSec ? tomorrowCalc.rawHours.fajr : prayerTimes.rawHours.fajr;
            const totalSpan = Math.max(1, (86400 - maghribSec) + fajrSec);
            const elapsed = currS >= maghribSec ? (currS - maghribSec) : (currS + (86400 - maghribSec));
            arcPct = elapsed / totalSpan;
          } else if (currS >= fajrSec && currS < sunriseSec) {
            // 2. عند حلول الفجر (من الفجر إلى الشروق): يمتد المسار من المغرب (يسار x=30) إلى الشروق (يمين x=370)
            arcLeftLabel = 'المغرب';
            arcLeftTimeRaw = prayerTimes.rawHours.maghrib;
            arcRightLabel = 'الشروق';
            arcRightTimeRaw = prayerTimes.rawHours.sunrise;
            arcSubPhaseBadge = `حلّ الفجر (${formatArcHour(prayerTimes.rawHours.fajr).t}) • بانتظار الشروق`;
            const totalSpan = Math.max(1, (86400 - maghribSec) + sunriseSec);
            const elapsed = currS + (86400 - maghribSec);
            arcPct = elapsed / totalSpan;
          } else if (currS >= sunriseSec && currS < sunsetSec) {
            // 3. النهار (من الشروق إلى الغروب): تنطلق الشمس من اليسار (الشروق x=30) وتتقدم نحو اليمين (الغروب في الشيعي / المغرب في السني x=370)
            arcLeftLabel = 'الشروق';
            arcLeftTimeRaw = prayerTimes.rawHours.sunrise;
            arcRightLabel = isShia ? 'الغروب' : 'المغرب';
            arcRightTimeRaw = isShia ? prayerTimes.rawHours.sunset : prayerTimes.rawHours.maghrib;
            const endSec = isShia ? sunsetSec : maghribSec;
            const totalSpan = Math.max(1, endSec - sunriseSec);
            const elapsed = currS - sunriseSec;
            arcPct = elapsed / totalSpan;
          } else {
            // 4. فترة ما بين الغروب الفلكي والمغرب الشرعي (ذهاب الحمرة المشرقية في تقويم الكفيل): يتحول الطرف الأيمن (x=370) إلى المغرب الشرعي
            arcLeftLabel = 'الشروق';
            arcLeftTimeRaw = prayerTimes.rawHours.sunrise;
            arcRightLabel = 'المغرب';
            arcRightTimeRaw = prayerTimes.rawHours.maghrib;
            arcSubPhaseBadge = `تم الغروب (${formatArcHour(prayerTimes.rawHours.sunset).t}) • ذهاب الحمرة المشرقية للمغرب`;
            const totalSpan = Math.max(1, maghribSec - sunriseSec);
            const elapsed = currS - sunriseSec;
            arcPct = elapsed / totalSpan;
          }

          arcPct = Math.max(0, Math.min(1, arcPct));
          // زاوية القوس تبدأ من اليسار (arcPct=0 -> angle=PI -> x=30) وتتقدم نحو اليمين (arcPct=1 -> angle=0 -> x=370)
          const arcAngle = Math.PI * (1 - arcPct);
          const ox = 200 + 170 * Math.cos(arcAngle);
          const oy = 260 - 240 * Math.sin(arcAngle);

          const cTime = formatArcHour(targetDateObj.getHours() + targetDateObj.getMinutes() / 60.0);
          const kParts = getMonthParts(currentHijri.monthName);
          const leftEndpointFormatted = formatArcHour(arcLeftTimeRaw);
          const rightEndpointFormatted = formatArcHour(arcRightTimeRaw);
          const todayDayName = daysOfWeekAr[targetDateObj.getDay()];
          const todayDateStrV7 = `${toEn(targetDateObj.getDate())} - ${toEn(targetDateObj.getMonth() + 1)} - ${toEn(targetDateObj.getFullYear())} م`;

          return (
            <div className="relative w-full flex-1 flex flex-col justify-between overflow-hidden">
              
              {/* هيدر متجاوب بالكامل من شاشات 320dp الصغيرة إلى الشاشات العريضة */}
              <div className="w-full max-w-6xl mx-auto px-2 sm:px-6 pt-2.5 sm:pt-5 flex justify-between items-start gap-1.5 sm:gap-4 z-10">
                <div className="flex flex-col gap-1.5 mt-0.5 min-w-0 max-w-[44%] sm:max-w-[46%]">
                  <div 
                    onClick={() => setActiveTab('settings_hub')}
                    className="flex items-center gap-1 sm:gap-1.5 bg-white/5 hover:bg-white/10 px-2 sm:px-3.5 py-1 sm:py-1.5 rounded-full border border-white/10 shadow-sm w-fit max-w-full cursor-pointer backdrop-blur-md active:scale-95 fluid-spring"
                  >
                    <MapPin size={12} className="text-yellow-400 shrink-0" />
                    <span className="text-[9.5px] sm:text-xs md:text-sm font-bold font-sans truncate" style={{ fontFamily: 'Arial, sans-serif' }}>
                      {locationInfo.name.split(' (')[0]}
                    </span>
                  </div>
                  <div className="bg-white/5 px-2 sm:px-2.5 py-1 rounded-full border border-white/5 flex items-center gap-1 sm:gap-1.5 w-fit max-w-full backdrop-blur-md">
                    {weather ? (
                      <>
                        {renderWeatherStatusIcon(effectiveWeatherCode, 12)}
                        <span className="text-[9.5px] sm:text-xs font-bold shrink-0" style={{ fontFamily: 'Arial, sans-serif' }} dir="ltr">{toEn(Math.round(weather.temperature))}°C</span>
                        <span className="text-[8.5px] sm:text-[9.5px] text-white/75 font-bold truncate hidden sm:inline">• {getWeatherConditionLabel(effectiveWeatherCode)}</span>
                      </>
                    ) : (
                      <span className="text-[8.5px] sm:text-[9.5px] text-white/75 font-bold truncate">{currentSeasonInfo.badge}</span>
                    )}
                  </div>
                </div>

                <div className="flex flex-col items-end shrink-0 max-w-[56%] sm:max-w-[54%]">
                  <div className="flex items-center text-[clamp(1.15rem,5.4vw,2.85rem)] font-bold font-reem leading-none mt-0.5 text-white whitespace-nowrap" dir="rtl" style={{ letterSpacing: '0px' }}>
                    <span>{kParts.pre}</span>
                    <div className="relative flex items-center justify-center min-w-[22px] sm:min-w-[45px] mx-[-2px] sm:mx-[-4px]">
                      <span className="text-white relative z-0">ـــــ</span>
                      <span className="absolute text-[#FFD700] text-[clamp(1rem,4.8vw,2.45rem)] font-reem font-extrabold z-10 -translate-y-[2px] sm:-translate-y-[5px]" 
                            style={{ filter: 'drop-shadow(0px 2px 4px rgba(0,0,0,0.8))', WebkitTextStroke: '1px rgba(0,0,0,0.5)' }}>
                        {toEn(currentHijri.day)}
                      </span>
                    </div>
                    <span>{kParts.post}</span>
                  </div>
                  <p className="text-[8.5px] sm:text-[10.5px] opacity-90 font-bold mt-1.5 tracking-tight text-left whitespace-nowrap" style={{ fontFamily: 'Arial, sans-serif' }}>
                    {todayDayName} {todayDateStrV7} <span className="opacity-65 text-yellow-100">({toEn(currentHijri.year)} هـ)</span>
                  </p>
                </div>
              </div>

              {/* القوس السماوي 3D ممتد من اليسار (x=30 الشرق/البداية) إلى اليمين (x=370 الغروب والمغرب/النهاية) */}
              <div className="flex-1 flex flex-col items-center justify-center z-10 relative my-0.5 w-full px-1 sm:px-3">
                <div className="relative w-full max-w-[1280px] px-0.5 sm:px-2 flex items-center justify-center">
                  <svg dir="ltr" viewBox="22 2 356 298" preserveAspectRatio="xMidYMid meet" className="w-full h-auto min-h-[235px] sm:min-h-[290px] max-h-[52dvh] sm:max-h-[58dvh] overflow-visible">
                    <defs>
                      <linearGradient id="day-gradient" x1="0%" y1="0%" x2="100%" y2="0%">
                        <stop offset="0%" stopColor="#FFE066" />
                        <stop offset="50%" stopColor="#FF9F1C" />
                        <stop offset="100%" stopColor="#E71D36" />
                      </linearGradient>

                      <linearGradient id="night-gradient" x1="0%" y1="0%" x2="100%" y2="0%">
                        <stop offset="0%" stopColor="#80A1D4" />
                        <stop offset="50%" stopColor="#C0D6DF" />
                        <stop offset="100%" stopColor="#F2F5EA" />
                      </linearGradient>

                      <linearGradient id="day-mist-aura" x1="0%" y1="0%" x2="100%" y2="0%">
                        <stop offset="0%" stopColor="#FDE047" stopOpacity="0.55" />
                        <stop offset="50%" stopColor="#FB923C" stopOpacity="0.65" />
                        <stop offset="100%" stopColor="#F43F5E" stopOpacity="0.55" />
                      </linearGradient>

                      <linearGradient id="night-mist-aura" x1="0%" y1="0%" x2="100%" y2="0%">
                        <stop offset="0%" stopColor="#38BDF8" stopOpacity="0.5" />
                        <stop offset="50%" stopColor="#818CF8" stopOpacity="0.6" />
                        <stop offset="100%" stopColor="#E2E8F0" stopOpacity="0.5" />
                      </linearGradient>

                      <filter id="arc-mist-blur" x="-30%" y="-30%" width="160%" height="160%">
                        <feGaussianBlur stdDeviation="10" result="mistBlur1" />
                        <feGaussianBlur stdDeviation="20" result="mistBlur2" />
                        <feMerge>
                          <feMergeNode in="mistBlur2" />
                          <feMergeNode in="mistBlur1" />
                        </feMerge>
                      </filter>

                      <filter id="glow-sun" x="-50%" y="-50%" width="200%" height="200%">
                        <feGaussianBlur stdDeviation="6" result="blur1" />
                        <feGaussianBlur stdDeviation="18" result="blur2" />
                        <feMerge><feMergeNode in="blur2" /><feMergeNode in="blur1" /><feMergeNode in="SourceGraphic" /></feMerge>
                      </filter>

                      <filter id="glow-moon" x="-50%" y="-50%" width="200%" height="200%">
                        <feGaussianBlur stdDeviation="5" result="blur1" />
                        <feGaussianBlur stdDeviation="12" result="blur2" />
                        <feMerge><feMergeNode in="blur2" /><feMergeNode in="blur1" /><feMergeNode in="SourceGraphic" /></feMerge>
                      </filter>
                    </defs>

                    {/* مسار القاعدة الخلفية للقوس */}
                    <path d="M 30 260 A 170 240 0 0 1 370 260" fill="none" stroke="rgba(0,0,0,0.45)" strokeWidth="8" strokeLinecap="round"/>
                    <path d="M 30 260 A 170 240 0 0 1 370 260" fill="none" stroke="rgba(255,255,255,0.14)" strokeWidth="2" strokeDasharray="4 8" strokeLinecap="round"/>

                    {/* هالة ضوئية ضبابية خفيفة وممتعة خلف شريط تقدم القوس مباشرة */}
                    <path 
                      d="M 30 260 A 170 240 0 0 1 370 260" 
                      fill="none" 
                      stroke={!isNight ? "url(#day-mist-aura)" : "url(#night-mist-aura)"} 
                      strokeWidth="22" 
                      strokeLinecap="round" 
                      pathLength="100" 
                      strokeDasharray="100" 
                      strokeDashoffset={100 - (arcPct * 100)}
                      filter="url(#arc-mist-blur)"
                      style={{ opacity: 0.72, transition: 'stroke-dashoffset 0.65s cubic-bezier(0.22, 1, 0.36, 1)' }}
                    />
                    
                    {/* شريط تقدم القوس الرئيسي من اليسار (x=30) إلى اليمين (x=370) */}
                    <path 
                      d="M 30 260 A 170 240 0 0 1 370 260" 
                      fill="none" 
                      stroke={!isNight ? "url(#day-gradient)" : "url(#night-gradient)"} 
                      strokeWidth="4.5" 
                      strokeLinecap="round" 
                      pathLength="100" 
                      strokeDasharray="100" 
                      strokeDashoffset={100 - (arcPct * 100)}
                      style={{ filter: `drop-shadow(0 0 12px ${!isNight ? 'rgba(255,159,28,0.5)' : 'rgba(128,161,212,0.5)'})`, transition: 'stroke-dashoffset 0.65s cubic-bezier(0.22, 1, 0.36, 1)' }}
                    />

                    <g transform={`translate(${ox}, ${oy})`} style={{ transition: 'transform 0.65s cubic-bezier(0.22, 1, 0.36, 1)' }}>
                      {!isNight ? (
                        <g filter="url(#glow-sun)">
                          <RealisticSunSVG size={32} />
                        </g>
                      ) : (
                        <g filter="url(#glow-moon)">
                          <RealisticMoonPhaseSVG hijriDay={currentHijri.day} size={30} idSuffix="sky-arc" />
                        </g>
                      )}
                    </g>

                    <g transform="translate(200, 198)">
                      <text y="0" textAnchor="middle" fill="white" fontSize="45" fontWeight="bold" style={{ fontFamily: 'Arial, sans-serif', filter: 'drop-shadow(0 4px 10px rgba(0,0,0,0.8))' }}>
                        {use24Hour ? toEn(cTime.hhmm24) : toEn(cTime.t)}
                      </text>
                      {!use24Hour && (
                        <text x="-76" y="13" textAnchor="start" fill="rgba(255,255,255,0.8)" fontSize="13" fontWeight="bold">
                          {cTime.ap}
                        </text>
                      )}
                      <text y="36" textAnchor="middle" fill="rgba(255,255,255,0.4)" fontSize="11" fontWeight="bold" letterSpacing="4">الوقت الحالي</text>
                      {arcSubPhaseBadge && (
                        <text y="54" textAnchor="middle" fill="rgba(253,224,71,0.9)" fontSize="9.5" fontWeight="bold">
                          {toEn(arcSubPhaseBadge)}
                        </text>
                      )}
                    </g>

                    {/* الطرف الأيسر للقوس (x=30): نقطة البداية دائماً (الشرق / الشروق نهاراً، والمغرب ليلاً) */}
                    <g>
                      <circle cx="30" cy="260" r="4.5" fill="#FFF" style={{ filter: 'drop-shadow(0 2px 6px rgba(0,0,0,0.9))' }}/>
                      <text x="30" y="281" fill="rgba(255,255,255,0.92)" fontSize="11" fontWeight="700" textAnchor="middle" style={{ textShadow: '0 2px 4px rgba(0,0,0,0.8)' }}>
                        {arcLeftLabel}
                      </text>
                      <text x="30" y="295" fill="rgba(253,224,71,0.9)" fontSize="9.5" fontWeight="700" textAnchor="middle" style={{ fontFamily: 'Arial, sans-serif' }}>
                        {use24Hour ? toEn(leftEndpointFormatted.hhmm24) : toEn(leftEndpointFormatted.t)}
                      </text>
                    </g>

                    {/* الطرف الأيمن للقوس (x=370): نقطة النهاية دائماً (الغرب / الغروب ثم المغرب نهاراً، والفجر ثم الشروق ليلاً) */}
                    <g>
                      <circle cx="370" cy="260" r="4.5" fill="#FFF" style={{ filter: 'drop-shadow(0 2px 6px rgba(0,0,0,0.9))' }}/>
                      <text x="370" y="281" fill="rgba(255,255,255,0.92)" fontSize="11" fontWeight="700" textAnchor="middle" style={{ textShadow: '0 2px 4px rgba(0,0,0,0.8)' }}>
                        {arcRightLabel}
                      </text>
                      <text x="370" y="295" fill="rgba(253,224,71,0.9)" fontSize="9.5" fontWeight="700" textAnchor="middle" style={{ fontFamily: 'Arial, sans-serif' }}>
                        {use24Hour ? toEn(rightEndpointFormatted.hhmm24) : toEn(rightEndpointFormatted.t)}
                      </text>
                    </g>
                  </svg>
                </div>

                {/* العداد التنازلي التفاعلي (يدعم السحب باللمس والماوس ويعيد تمركز المربع المختص في وسط الشريط السفلي تلقائياً) */}
                <div 
                  className="w-full min-h-[112px] sm:min-h-[124px] flex flex-col items-center justify-center z-20 -mt-3 cursor-grab active:cursor-grabbing"
                  onTouchStart={(e) => { setTouchEndX(null); setTouchStartX(e.targetTouches[0].clientX); }}
                  onTouchMove={(e) => setTouchEndX(e.targetTouches[0].clientX)}
                  onTouchEnd={() => {
                    if (touchStartX === null || touchEndX === null || activeCarouselList.length <= 1) return;
                    const dist = touchStartX - touchEndX;
                    if (Math.abs(dist) > 30) {
                      const nextIdx = dist > 0
                        ? (safeIdx + 1) % activeCarouselList.length
                        : (safeIdx - 1 + activeCarouselList.length) % activeCarouselList.length;
                      setArcSlideIndex(nextIdx);
                      const nextPrayer = activeCarouselList[nextIdx];
                      if (nextPrayer) setTimeout(() => centerActivePrayerCard(nextPrayer.id), 30);
                    }
                    setTouchStartX(null);
                    setTouchEndX(null);
                  }}
                  onMouseDown={(e) => { setTouchEndX(null); setTouchStartX(e.clientX); }}
                  onMouseMove={(e) => { if (touchStartX !== null) setTouchEndX(e.clientX); }}
                  onMouseUp={() => {
                    if (touchStartX === null || touchEndX === null || activeCarouselList.length <= 1) {
                      setTouchStartX(null);
                      setTouchEndX(null);
                      return;
                    }
                    const dist = touchStartX - touchEndX;
                    if (Math.abs(dist) > 30) {
                      const nextIdx = dist > 0
                        ? (safeIdx + 1) % activeCarouselList.length
                        : (safeIdx - 1 + activeCarouselList.length) % activeCarouselList.length;
                      setArcSlideIndex(nextIdx);
                      const nextPrayer = activeCarouselList[nextIdx];
                      if (nextPrayer) setTimeout(() => centerActivePrayerCard(nextPrayer.id), 30);
                    }
                    setTouchStartX(null);
                    setTouchEndX(null);
                  }}
                >
                  {hasAnyCountdownPrayer && targetP ? (
                    <>
                      <div className="bg-white/5 border border-white/10 rounded-full px-4 sm:px-6 py-1 mb-2 shadow-xl backdrop-blur-md fluid-spring">
                        <span className="text-yellow-400 text-[10.5px] sm:text-[12px] font-bold tracking-wide">{labelText}</span>
                      </div>
                      
                      <div className="flex items-center justify-center gap-1 sm:gap-1.5 text-white drop-shadow-[0_10px_20px_rgba(0,0,0,0.8)]" dir="ltr" style={{ fontFamily: 'Arial, sans-serif' }}>
                        <div className="text-center w-[54px] sm:w-[68px] relative h-[46px] sm:h-[56px] overflow-hidden flex items-center justify-center">
                          <span key={hLeft} className="text-[2.5rem] sm:text-[3.4rem] font-light leading-none animate-slideUpFade absolute">{toEn(padNum(hLeft))}</span>
                        </div>
                        <span className="text-2xl sm:text-3xl text-white/30">:</span>
                        <div className="text-center w-[54px] sm:w-[68px] relative h-[46px] sm:h-[56px] overflow-hidden flex items-center justify-center">
                          <span key={mLeft} className="text-[2.5rem] sm:text-[3.4rem] font-light leading-none animate-slideUpFade absolute">{toEn(padNum(mLeft))}</span>
                        </div>
                        <span className="text-2xl sm:text-3xl text-white/30">:</span>
                        <div className="text-center w-[54px] sm:w-[68px] relative h-[46px] sm:h-[56px] overflow-hidden flex items-center justify-center">
                          <span key={sLeft} className="text-[2.5rem] sm:text-[3.4rem] font-light leading-none animate-slideUpFade absolute">{toEn(padNum(sLeft))}</span>
                        </div>
                      </div>

                      <div className="flex gap-2 mt-3">
                        {activeCarouselList.map((p, i) => (
                          <button 
                            key={p.id} 
                            onClick={() => {
                              setArcSlideIndex(i);
                              setTimeout(() => centerActivePrayerCard(p.id), 30);
                            }}
                            className={`h-1.5 rounded-full fluid-spring ${safeIdx === i ? 'w-7 bg-yellow-400 shadow-lg':'w-2 bg-white/20 hover:bg-white/40'}`}
                          />
                        ))}
                      </div>
                    </>
                  ) : (
                    <div className="h-[100px] w-full pointer-events-none" aria-hidden="true" />
                  )}
                </div>
              </div>

              {/* شريط مربعات الصلوات السفلية قابلة للتمرير الحر والسلس مع عودة المربع المختص للوسط عند تغيير العد التنازلي */}
              <div className="w-full max-w-6xl mx-auto z-20 pb-6 sm:pb-8 pt-1 mt-auto relative">
                <div className="flex gap-4 px-3 sm:px-6 mb-1.5">
                  <button onClick={() => setArcShowCalendarModal(true)} className="p-2.5 sm:p-3 bg-white/5 backdrop-blur-2xl border border-white/10 rounded-[18px] shadow-lg active:scale-95 fluid-spring text-white" title="تقويم وإمساكية الشهر">
                    <Calendar size={17} />
                  </button>
                </div>
                
                <div className="relative w-full overflow-hidden" style={{ WebkitMaskImage: 'linear-gradient(to right, transparent, black 6%, black 94%, transparent)' }}>
                  <div 
                    ref={arcPrayerNavRef} 
                    className="flex gap-2.5 sm:gap-3.5 overflow-x-auto pb-7 pt-5 px-[36vw] sm:px-[38%] hide-scrollbar scroll-smooth touch-pan-x"
                    style={{ WebkitOverflowScrolling: 'touch' }}
                  >
                    {visibleArcPrayers.map((p) => {
                      const active = p.id === targetP?.id;
                      const tmrw = active && isTmrw; 
                      const rawTimeVal = tmrw ? tomorrowCalc.rawHours[p.id] : prayerTimes.rawHours[p.id];
                      const formattedTime = formatArcHour(rawTimeVal);
                      const Icon = p.icon;
                      const carouselIdx = activeCarouselList.findIndex(item => item.id === p.id);
                      
                      return (
                        <div 
                          key={p.id} 
                          ref={el => arcPrayerRefs.current[p.id] = el} 
                          onClick={() => {
                            if (carouselIdx !== -1) setArcSlideIndex(carouselIdx);
                            setTimeout(() => centerActivePrayerCard(p.id), 30);
                          }} 
                          className={`cursor-pointer shrink-0 min-w-[88px] sm:min-w-[100px] p-2.5 sm:p-3.5 rounded-[20px] sm:rounded-[24px] flex flex-col items-center justify-center fluid-spring ${active ? 'bg-white/15 border border-white/30 scale-110 -translate-y-2.5 shadow-2xl backdrop-blur-xl':'bg-white/5 border border-white/5 backdrop-blur-md hover:bg-white/10'}`}
                        >
                          <div className={`flex items-center gap-1 mb-1 ${active ? 'text-yellow-400':'text-white/50'}`}>
                            <Icon size={11} />
                            <span className="text-[10px] sm:text-[11px] font-bold whitespace-nowrap">{p.label}</span>
                          </div>
                          <div className="text-xs sm:text-sm font-bold flex items-baseline gap-1 whitespace-nowrap" style={{ fontFamily: 'Arial, sans-serif' }}>
                            {use24Hour ? toEn(formattedTime.hhmm24) : toEn(formattedTime.t)} <span className="text-[8px] opacity-60 font-sans">{formattedTime.ap}</span>
                          </div>
                          <div className={`text-[9px] font-bold mt-0.5 fluid-spring ${tmrw ? 'text-yellow-400 opacity-100 h-2.5' : 'opacity-0 h-0 overflow-hidden'}`}>(غداً)</div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>
            </div>
          );
        })()}

        {/* ====================================================================
            2️⃣ شاشة الضبط والأوقات الموحدة (تجمع تصميم الكفيل الأصلي + إعدادات V7 المنسدلة)
           ==================================================================== */}
        {activeTab === 'settings_hub' && prayerTimes && (() => {
          let prayerDisplayList = [
            ...(shouldShowImsak ? [{ key: 'imsak', label: 'الإمساك' }] : []),
            { key: 'fajr', label: 'الفجر' }
          ];
          if (prayerTimes.isfar && prayerTimes.isSummerAnomaly && isShia && highLatRule === 'sayegh') {
            prayerDisplayList.push({ key: 'isfar', label: 'الإسفار' });
          }
          prayerDisplayList = prayerDisplayList.concat([
            { key: 'sunrise', label: 'الشروق' },
            { key: 'dhuhr', label: 'الظهر' }, 
            { key: 'asr', label: 'العصر' }
          ]);
          if (isShia) prayerDisplayList.push({ key: 'sunset', label: 'الغروب' });
          prayerDisplayList.push(
            { key: 'maghrib', label: 'المغرب' }, 
            { key: 'isha', label: 'العشاء' }, 
            { key: 'midnight', label: 'منتصف الليل' }
          );

          const detectedDstRule = inferDstRuleByCoords(locationInfo.lat, locationInfo.lon, locationInfo.dstRule);
          const isCurrentlyDstAuto = isDateInDSTOffline(targetDateObj, detectedDstRule);

          return (
            <div className="flex flex-col gap-6 w-full animate-in fade-in duration-300">
              
              {/* شريط التنبيه الذهبي الذكي لخطوط العرض العليا في الصيف - يظهر في شاشة الضبط فقط */}
              {prayerTimes.isHighLatWarning && prayerTimes.isSummerAnomaly && (
                <div className="bg-yellow-500/10 border-r-4 border-yellow-500 text-yellow-200 p-5 rounded-2xl flex items-start gap-3 backdrop-blur-md shadow-xl">
                  <ShieldAlert size={28} className="flex-shrink-0 mt-0.5 text-yellow-400" />
                  <div className="text-xs md:text-sm leading-relaxed">
                    <strong className="block text-yellow-400 mb-1">تنبيه فلكي: خطوط العرض العليا (زاوية استثنائية صيفية)</strong>
                    المنطقة المحددة تقع حالياً فوق خط عرض {Math.abs(locationInfo.lat).toFixed(4)} درجة في فصل الصيف الفلكي (تداخل الشفق). تم تفعيل قانون <span className="underline font-bold text-teal-400">"{
                      highLatRule === 'sayegh' ? 'تقويم الصائغ (الإمساك بمنتصف الليل الفلكي)' :
                      highLatRule === 'aqrab_48' ? 'أقرب بلد لمؤسسة لندن (خط عرض 48°)' :
                      highLatRule === 'aqrab_45' ? 'أقرب بلد لحوزة قم وطهران (خط عرض 45°)' :
                      highLatRule === 'fixed_90' ? 'الفاصل الثابت لأوروبا (90 دقيقة)' :
                      highLatRule === 'sab_ul_layl' ? 'قاعدة سُبع الليل' : 'الكسر النسبي العشري'
                    }"</span> تلقائياً لضبط الصلوات والحد من اضطراب أوقات الفجر والعشاء.
                  </div>
                </div>
              )}

              {/* ترويسة مرصد الكفيل 3D الأصلية */}
              <div className="w-full flex flex-col md:flex-row justify-between items-center p-5 sm:p-8 bg-white/[0.03] border border-white/[0.05] rounded-[2.2rem] shadow-2xl backdrop-blur-xl">
                <div className="text-center md:text-right">
                  <h1 className="text-2xl sm:text-3xl md:text-4xl font-extrabold flex items-center justify-center md:justify-start gap-3 text-transparent bg-clip-text bg-gradient-to-r from-teal-400 to-indigo-300">
                    <Clock size={30} className="text-teal-400 shrink-0" /> مرصد الكفيل 3D
                  </h1>
                  <div className="mt-2 text-white/50 text-[11px] md:text-xs font-semibold tracking-wide">
                    نظام الحسابات الفلكية الشرعية المتكامل للمناطق العادية وخطوط العرض المرتفعة صيفاً
                  </div>
                </div>
                <div className="mt-4 md:mt-0 flex flex-col items-center md:items-end gap-1">
                  <div className="text-xl sm:text-2xl md:text-3xl font-black text-white tracking-wide drop-shadow-md">
                    {currentHijri.day} {currentHijri.monthName} {currentHijri.year} <span className="text-teal-400 text-sm md:text-lg">هـ</span>
                  </div>
                  <div className="text-white/60 text-xs md:text-sm font-bold tracking-tight">
                    {getGregorianFormatted(targetDateObj)}
                  </div>
                </div>
              </div>

              {/* صندوق لوحة التحكم Neural 3D الأصلي من ملف الكفيل */}
              <div className="w-full bg-black/40 border-t border-l border-white/5 border-b border-black/80 rounded-[2.2rem] p-5 sm:p-6 shadow-[inset_2px_2px_10px_rgba(0,0,0,0.5),8px_8px_20px_rgba(0,0,0,0.6)]">
                <div className="flex flex-col xl:flex-row justify-between items-start xl:items-center gap-6">
                  
                  <div className="w-full xl:w-1/2 flex flex-col gap-4">
                    <div className="flex flex-wrap items-center gap-2.5 text-teal-300 font-bold px-1">
                      <div className="flex items-center gap-2">
                        <MapPin size={22} className="text-teal-400 shrink-0" /> 
                        <span className="text-lg sm:text-xl tracking-tight text-white drop-shadow-md">{locationInfo.name}</span>
                      </div>
                      <div className="flex flex-wrap items-center gap-2">
                        <span className="bg-teal-500/20 text-teal-100 border border-teal-500/30 px-3 py-1 rounded-full text-xs sm:text-sm font-mono tracking-widest">{realTimeClock}</span>
                        <span className="bg-indigo-500/20 text-indigo-200 border border-indigo-500/30 px-2.5 py-1 rounded-full text-[11px] font-bold" dir="ltr">
                          UTC{effectiveTzNow >= 0 ? `+${effectiveTzNow}` : effectiveTzNow}
                        </span>
                        {weather && !isOfflineMode && (
                          <span className="text-orange-200 bg-orange-500/20 px-3 py-1 rounded-full text-xs sm:text-sm flex items-center gap-1 border border-orange-500/30">
                            <Thermometer size={15}/>{Math.round(weather.temperature)}°C
                          </span>
                        )}
                      </div>
                    </div>

                    <form onSubmit={searchCity} className="relative flex w-full">
                      <input 
                        type="text" 
                        value={cityInput} 
                        onChange={(e) => setCityInput(e.target.value)} 
                        placeholder="ابحث عن مدينة (أو جرب 'لندن'، 'كربلاء'، 'باريس')..." 
                        className="w-full bg-black/50 border border-white/5 text-white rounded-[1.5rem] py-3.5 pr-12 pl-4 focus:outline-none focus:ring-1 focus:ring-teal-500/50 text-xs sm:text-sm shadow-[inset_2px_2px_5px_rgba(0,0,0,0.5)]" 
                      />
                      <button type="submit" className="absolute right-4 top-1/2 -translate-y-1/2 text-white/40 hover:text-teal-400">
                        <Search size={19} />
                      </button>
                    </form>

                    <div className="flex flex-wrap gap-2">
                      <button onClick={fetchLocationByGPS} className="flex-1 flex items-center justify-center gap-2 text-xs bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-300 px-4 py-3 rounded-xl border border-emerald-500/20 transition-all shadow-md font-bold">
                        <Crosshair size={16} /> GPS (ذكي)
                      </button>
                      {!isOfflineMode && (
                        <button onClick={fetchLocationByIP} className="flex-1 flex items-center justify-center gap-2 text-xs bg-indigo-500/10 hover:bg-indigo-500/20 text-indigo-300 px-4 py-3 rounded-xl border border-indigo-500/20 transition-all shadow-md font-bold">
                          <Navigation size={16} /> مدينتي (IP)
                        </button>
                      )}
                      <button onClick={() => setUse24Hour(!use24Hour)} className="px-4 py-3 rounded-xl text-xs font-bold bg-white/5 hover:bg-white/10 text-white/80 border border-white/10 transition-all">
                        {use24Hour ? '24 ساعة' : '12 ساعة'}
                      </button>
                    </div>
                  </div>

                  <div className="w-full xl:w-1/2 flex flex-col gap-3">
                    <div className="flex items-center justify-between text-white/50 text-xs md:text-sm px-1">
                      <span className="font-bold flex items-center gap-2"><Settings2 size={16}/> إعدادات المنظومة</span>
                      <button onClick={() => setShowAdvancedAlkafeel(!showAdvancedAlkafeel)} className="text-teal-400 text-xs bg-teal-400/10 border border-teal-500/20 px-4 py-1.5 rounded-full hover:bg-teal-400/20 transition-colors font-bold">
                        {showAdvancedAlkafeel ? 'إغلاق المتقدم' : 'متقدم'}
                      </button>
                    </div>

                    <div className="grid grid-cols-2 md:grid-cols-4 gap-2 bg-black/50 p-2 rounded-[1.5rem] border border-white/5 shadow-inner">
                      {[
                        { id: 'alkafeel', name: 'الكفيل (السيستاني)' }, 
                        { id: 'leufa', name: 'لوفا (قم)' },
                        { id: 'tehran', name: 'طهران' }, 
                        { id: 'ummalqura', name: 'أم القرى (مكة)' }
                      ].map(m => (
                        <button 
                          key={m.id} 
                          onClick={() => { setMethod(m.id); setError(null); }} 
                          className={`px-2 py-3 rounded-xl text-[11px] sm:text-xs font-bold fluid-spring ${method === m.id ? 'bg-gradient-to-b from-teal-500 to-teal-700 text-white shadow-[0_4px_15px_rgba(20,184,166,0.4)] border border-teal-400/50' : 'text-white/50 hover:bg-white/5 hover:text-white border border-transparent'}`}
                        >
                          {m.name}
                        </button>
                      ))}
                    </div>

                    {/* خيارات معالجة المدن الصيفية العليا - تظهر في الضبط فقط عند الحاجة */}
                    {prayerTimes.isHighLatWarning && prayerTimes.isSummerAnomaly && (
                      <div className="bg-yellow-500/5 p-4 rounded-2xl border border-yellow-500/20 mt-1 animate-in slide-in-from-top-2">
                        <span className="text-[11px] text-yellow-400 font-bold block mb-2">خيارات القياس المعتمدة صيفاً للمدن العليا:</span>
                        <div className="grid grid-cols-2 gap-2">
                          {isShia ? (
                            <>
                              <button onClick={() => setHighLatRule('sayegh')} className={`py-2 px-1 rounded-lg text-[10px] font-bold border fluid-spring ${highLatRule === 'sayegh' ? 'bg-yellow-500/20 border-yellow-500 text-yellow-200' : 'bg-black/30 border-transparent text-white/50'}`}>تقويم الصائغ</button>
                              <button onClick={() => setHighLatRule('aqrab_48')} className={`py-2 px-1 rounded-lg text-[10px] font-bold border fluid-spring ${highLatRule === 'aqrab_48' ? 'bg-yellow-500/20 border-yellow-500 text-yellow-200' : 'bg-black/30 border-transparent text-white/50'}`}>أقرب بلد (48° - لندن)</button>
                              <button onClick={() => setHighLatRule('aqrab_45')} className={`py-2 px-1 rounded-lg text-[10px] font-bold border fluid-spring ${highLatRule === 'aqrab_45' ? 'bg-yellow-500/20 border-yellow-500 text-yellow-200' : 'bg-black/30 border-transparent text-white/50'}`}>أقرب بلد (45° - قم)</button>
                            </>
                          ) : (
                            <>
                              <button onClick={() => setHighLatRule('fixed_90')} className={`py-2 px-1 rounded-lg text-[10px] font-bold border fluid-spring ${highLatRule === 'fixed_90' ? 'bg-yellow-500/20 border-yellow-500 text-yellow-200' : 'bg-black/30 border-transparent text-white/50'}`}>الفاصل الثابت 90 دقيقة</button>
                              <button onClick={() => setHighLatRule('sab_ul_layl')} className={`py-2 px-1 rounded-lg text-[10px] font-bold border fluid-spring ${highLatRule === 'sab_ul_layl' ? 'bg-yellow-500/20 border-yellow-500 text-yellow-200' : 'bg-black/30 border-transparent text-white/50'}`}>قاعدة سُبع الليل</button>
                              <button onClick={() => setHighLatRule('angle_based')} className={`py-2 px-1 rounded-lg text-[10px] font-bold border fluid-spring ${highLatRule === 'angle_based' ? 'bg-yellow-500/20 border-yellow-500 text-yellow-200' : 'bg-black/30 border-transparent text-white/50'}`}>الكسر النسبي العشري</button>
                            </>
                          )}
                          <button onClick={() => setHighLatRule('none')} className={`col-span-2 py-2 rounded-lg text-[10px] font-bold border fluid-spring ${highLatRule === 'none' ? 'bg-rose-500/20 border-rose-500 text-rose-200' : 'bg-black/30 border-transparent text-white/40'}`}>
                            إلغاء المعالجة الفلكية (قد تتعطل الحسابات صيفاً)
                          </button>
                        </div>
                      </div>
                    )}

                    {showAdvancedAlkafeel && (
                      <div className="flex flex-col gap-3 mt-2 bg-white/[0.02] p-4 sm:p-5 rounded-[1.5rem] border border-white/10 shadow-2xl animate-in fade-in slide-in-from-top-2">
                        <div className="flex flex-wrap gap-2">
                          <span className="text-xs text-teal-300 w-full mb-1 font-bold">مصدر الأوقات (Data Source):</span>
                          <button onClick={() => setDataSource('offline')} className={`flex-1 py-2 rounded-xl text-xs font-bold fluid-spring border ${dataSource === 'offline' ? 'bg-teal-600/80 text-white border-teal-400' : 'bg-black/40 text-white/50 border-transparent'}`}>
                            المحرك الفلكي أوفلاين
                          </button>
                          <button onClick={() => setDataSource('api')} disabled={isOfflineMode} className={`flex-1 py-2 rounded-xl text-xs font-bold fluid-spring border ${dataSource === 'api' ? 'bg-indigo-600/80 text-white border-indigo-400' : 'bg-black/40 text-white/50 border-transparent disabled:opacity-40'}`}>
                            جلب عبر API إنترنت
                          </button>
                        </div>

                        <div className="flex flex-col gap-2 border-t border-white/5 pt-2">
                          <span className="text-xs text-white/50 font-bold">المعاهد والمجالس العالمية الكبرى:</span>
                          <div className="grid grid-cols-2 md:grid-cols-3 gap-1.5">
                            {[
                              { id: 'london_shia', label: 'مؤسسة لندن (شيعي)' },
                              { id: 'mwl', label: 'رابطة العالم' },
                              { id: 'egypt', label: 'المصرية' },
                              { id: 'isna', label: 'أمريكا (ISNA)' },
                              { id: 'karachi', label: 'كراتشي' },
                              { id: 'france_12', label: 'فرنسا 12°' },
                              { id: 'european_council', label: 'المجلس الأوروبي' }
                            ].map(inst => (
                              <button 
                                key={inst.id} 
                                onClick={() => { setMethod(inst.id); setError(null); }} 
                                className={`py-2 rounded-lg text-[10px] font-bold border fluid-spring ${method === inst.id ? 'bg-teal-500/20 border-teal-500 text-teal-300' : 'bg-black/30 border-transparent text-white/50 hover:bg-white/5'}`}
                              >
                                {inst.label}
                              </button>
                            ))}
                          </div>
                        </div>

                        <div className="flex gap-2 border-t border-white/5 pt-2">
                          <button onClick={() => setAsrMethod('shafi')} className={`flex-1 py-2 rounded-xl text-[11px] font-bold border fluid-spring ${asrMethod === 'shafi' ? 'bg-teal-500/20 border-teal-400 text-white' : 'bg-black/40 border-transparent text-white/40'}`}>
                            العصر: الشافعي (ظل = 1)
                          </button>
                          <button onClick={() => setAsrMethod('hanafi')} className={`flex-1 py-2 rounded-xl text-[11px] font-bold border fluid-spring ${asrMethod === 'hanafi' ? 'bg-teal-500/20 border-teal-400 text-white' : 'bg-black/40 border-transparent text-white/40'}`}>
                            العصر: الحنفي (ظل = 2)
                          </button>
                        </div>

                        {/* خيار التوقيت الصيفي والشتوي الذكي المتكيف مع كل مدن العالم */}
                        <div className="flex flex-col gap-2 border-t border-white/5 pt-2">
                          <div className="flex items-center justify-between text-[11px] text-teal-300 font-bold">
                            <span>نظام التوقيت الصيفي والشتوي (DST):</span>
                            <span className="text-white/60">
                              {dstMode === 'auto'
                                ? (detectedDstRule !== 'none' ? (isCurrentlyDstAuto ? 'تلقائي (صيفي نشط +1)' : 'تلقائي (شتوي قياسي)') : 'تلقائي (لا يوجد صيفي)')
                                : dstMode === 'summer' ? 'صيفي يدوي (+1 ساعة)' : 'شتوي قياسي ثابت'}
                            </span>
                          </div>
                          <div className="grid grid-cols-3 gap-1.5">
                            {[
                              { id: 'auto', label: 'تلقائي ذكي (أوفلاين)' },
                              { id: 'winter', label: 'توقيت شتوي قياسي' },
                              { id: 'summer', label: 'توقيت صيفي (+1)' }
                            ].map(dOpt => (
                              <button
                                key={dOpt.id}
                                onClick={() => setDstMode(dOpt.id)}
                                className={`py-2 px-1.5 rounded-xl text-[10px] font-bold fluid-spring border ${dstMode === dOpt.id ? 'bg-teal-500/20 border-teal-400 text-teal-200 shadow' : 'bg-black/40 border-transparent text-white/45 hover:text-white'}`}
                              >
                                {dOpt.label}
                              </button>
                            ))}
                          </div>
                        </div>

                        <div className="flex items-center justify-between bg-black/30 p-3 rounded-xl border border-white/5 mt-1">
                          <span className="text-xs text-white/60 font-medium">تعديل التاريخ الهجري للرؤية:</span>
                          <div className="flex items-center gap-2 bg-black/50 rounded-lg border border-white/10 p-1">
                            <button onClick={() => setHijriAdjustment(h => h + 1)} className="p-1.5 hover:bg-white/10 rounded-md"><ChevronRight size={16} /></button>
                            <span className="text-sm font-black w-8 text-center text-teal-400">{hijriAdjustment > 0 ? `+${hijriAdjustment}` : hijriAdjustment}</span>
                            <button onClick={() => setHijriAdjustment(h => h - 1)} className="p-1.5 hover:bg-white/10 rounded-md"><ChevronLeft size={16} /></button>
                          </div>
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              </div>

              {/* قسم مواقيت الصلاة اليومية المنسدل */}
              <div ref={el => settingsAccordionRefs.current.dailyTimesBlock = el} className="w-full bg-black/40 rounded-[2rem] border border-white/5 overflow-hidden shadow-xl backdrop-blur-md">
                <div className="p-5 flex justify-between items-center cursor-pointer" onClick={() => toggleSettingsAccordion('dailyTimesBlock')}>
                  <span className="text-sm md:text-base font-bold text-teal-300 flex items-center gap-2">
                    <Clock size={18} className="text-teal-400" /> مواقيت الصلاة اليومية ({locationInfo.name.split(' (')[0]})
                  </span>
                  <ChevronDown size={20} className={`transition-transform duration-300 ${openSettingsSections.dailyTimesBlock ? 'rotate-180 text-teal-400' : 'text-white/50'}`} />
                </div>
                <div className={`accordion-content ${openSettingsSections.dailyTimesBlock ? 'open' : ''}`}>
                  <div className="px-5 pb-5 grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3.5 w-full">
                    {prayerDisplayList.map((prayer, i) => {
                      const isHighlight = ['fajr', 'maghrib'].includes(prayer.key);
                      return (
                        <div key={i} className={`relative overflow-hidden p-5 flex flex-col items-center justify-center text-center fluid-spring rounded-[1.8rem] border-t border-l
                          ${isHighlight 
                            ? 'bg-gradient-to-b from-teal-500/10 to-teal-900/30 border-white/20 border-b border-r border-black/80 shadow-[6px_6px_15px_rgba(0,0,0,0.5)]' 
                            : 'bg-white/[0.02] border-white/[0.05] border-b border-r border-black/90 shadow-[4px_4px_12px_rgba(0,0,0,0.4)] hover:bg-white/[0.04]'
                          }`}>
                          <h3 className={`text-xs font-bold mb-2 tracking-wide ${isHighlight ? 'text-teal-300' : 'text-white/50'}`}>{prayer.label}</h3>
                          <p className={`text-xl lg:text-2xl font-black tracking-tight ${isHighlight ? 'text-white drop-shadow-[0_2px_8px_rgba(20,184,166,0.6)]' : 'text-white/80'}`}>
                            {prayerTimes[prayer.key]}
                          </p>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>

              {/* الأقسام المنسدلة لإعدادات V7 */}
              <div className="space-y-3.5 w-full">
                <div ref={el => settingsAccordionRefs.current.offset = el} className="bg-black/40 rounded-3xl border border-white/5 overflow-hidden backdrop-blur-md">
                  <div className="p-4 flex justify-between items-center cursor-pointer" onClick={() => toggleSettingsAccordion('offset')}>
                    <span className="text-xs md:text-sm font-bold">تقديم وتأخير اليوم للشهر الهجري</span>
                    <ChevronDown size={18} className={`transition-transform duration-300 ${openSettingsSections.offset ? 'rotate-180 text-yellow-400' : 'text-white/50'}`} />
                  </div>
                  <div className={`accordion-content ${openSettingsSections.offset ? 'open' : ''}`}>
                    <div className="px-4 pb-4">
                      <div className="flex items-center justify-center gap-6 bg-black/30 py-3 rounded-xl border border-white/5">
                        <button onClick={() => setHijriAdjustment(h => h - 1)} className="w-9 h-9 rounded-full bg-white/5 flex items-center justify-center text-yellow-400 active:scale-90"><Minus size={16}/></button>
                        <span className="text-2xl font-bold" style={{ fontFamily: 'Arial, sans-serif' }}>{toEn(hijriAdjustment > 0 ? `+${hijriAdjustment}` : hijriAdjustment)}</span>
                        <button onClick={() => setHijriAdjustment(h => h + 1)} className="w-9 h-9 rounded-full bg-white/5 flex items-center justify-center text-yellow-400 active:scale-90"><Plus size={16}/></button>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="bg-black/40 p-4 rounded-3xl border border-white/5 flex flex-col gap-3.5 backdrop-blur-md">
                  <div className="flex justify-between items-center">
                    <span className="text-xs md:text-sm font-bold text-teal-300">إظهار العصر والعشاء في شاشة القوس والتقويم</span>
                    <div onClick={() => setShowExtraPrayers(!showExtraPrayers)} className={`w-11 h-6 rounded-full p-1 transition-colors cursor-pointer ${showExtraPrayers ? 'bg-yellow-400' : 'bg-white/10'}`}>
                      <div className={`w-4 h-4 rounded-full bg-white transition-transform ${showExtraPrayers ? 'translate-x-0' : '-translate-x-5'}`}></div>
                    </div>
                  </div>

                  <div className="border-t border-white/5 pt-3 flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                    <span className="text-xs font-bold text-white/80">ظهور مربع وقت الإمساك (افتراضياً في شهر رمضان فقط):</span>
                    <div className="flex gap-1.5 bg-black/40 p-1 rounded-xl border border-white/5">
                      {[
                        { id: 'ramadan_auto', label: 'تلقائي (رمضان فقط)' },
                        { id: 'always', label: 'إظهار دائماً' },
                        { id: 'hidden', label: 'إخفاء دائماً' }
                      ].map(m => (
                        <button
                          key={m.id}
                          onClick={() => setImsakDisplayMode(m.id)}
                          className={`px-2.5 py-1.5 rounded-lg text-[10px] font-bold fluid-spring ${imsakDisplayMode === m.id ? 'bg-yellow-400 text-black shadow' : 'text-white/55 hover:text-white'}`}
                        >
                          {m.label}
                        </button>
                      ))}
                    </div>
                  </div>

                  <div className="border-t border-white/5 pt-3">
                    <span className="text-[11px] text-white/60 font-bold block mb-2">إظهار وإخفاء مربعات الصلوات في الشريط السفلي:</span>
                    <div className="grid grid-cols-3 sm:grid-cols-6 gap-2">
                      {PRAYERS_BASE_V7.filter(p => p.id !== 'imsak' && !p.optional).map(p => (
                        <div
                          key={p.id}
                          onClick={() => setVisibleCardsPrayers(prev => ({ ...prev, [p.id]: prev[p.id] === false ? true : false }))}
                          className="flex justify-between items-center bg-black/30 px-2.5 py-2 rounded-xl border border-white/5 cursor-pointer hover:bg-white/5"
                        >
                          <span className="text-[11px] font-bold">{p.label}</span>
                          <div className={`w-4 h-4 rounded border flex items-center justify-center ${visibleCardsPrayers[p.id] !== false ? 'bg-teal-400 border-teal-400' : 'border-white/20'}`}>
                            {visibleCardsPrayers[p.id] !== false && <Check size={10} color="black" />}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>

                <div ref={el => settingsAccordionRefs.current.timeAdjust = el} className="bg-black/40 rounded-3xl border border-white/5 overflow-hidden backdrop-blur-md">
                  <div className="p-4 flex justify-between items-center cursor-pointer" onClick={() => toggleSettingsAccordion('timeAdjust')}>
                    <span className="text-xs md:text-sm font-bold border-r-2 border-yellow-400 pr-2">تعديل أوقات الصلاة (دقيقة ±60)</span>
                    <ChevronDown size={18} className={`transition-transform duration-300 ${openSettingsSections.timeAdjust ? 'rotate-180 text-yellow-400' : 'text-white/50'}`} />
                  </div>
                  <div className={`accordion-content ${openSettingsSections.timeAdjust ? 'open' : ''}`}>
                    <div className="px-4 pb-4 grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-2.5">
                      {PRAYERS_BASE_V7.map(p => (
                        <div key={p.id} className="flex justify-between items-center bg-black/30 p-3 rounded-xl border border-white/5">
                          <span className="text-xs font-bold text-white/90">{p.label}</span>
                          <div className="flex items-center gap-2">
                            <button onClick={() => setManualOffsets(prev => ({ ...prev, [p.id]: Math.max(-60, (prev[p.id] || 0) - 1) }))} className="w-7 h-7 rounded-lg bg-white/5 flex items-center justify-center text-yellow-400"><Minus size={13}/></button>
                            <span className="text-xs font-bold w-8 text-center" style={{ fontFamily: 'Arial, sans-serif' }}>{toEn(manualOffsets[p.id] > 0 ? `+${manualOffsets[p.id]}` : manualOffsets[p.id] || 0)}</span>
                            <button onClick={() => setManualOffsets(prev => ({ ...prev, [p.id]: Math.min(60, (prev[p.id] || 0) + 1) }))} className="w-7 h-7 rounded-lg bg-white/5 flex items-center justify-center text-yellow-400"><Plus size={13}/></button>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>

                <div ref={el => settingsAccordionRefs.current.uiPrayers = el} className="bg-black/40 rounded-3xl border border-white/5 overflow-hidden backdrop-blur-md">
                  <div className="p-4 flex justify-between items-center cursor-pointer" onClick={() => toggleSettingsAccordion('uiPrayers')}>
                    <span className="text-xs md:text-sm font-bold border-r-2 border-yellow-400 pr-2">صلوات العداد التنازلي في الشاشة (إلغاء الكل يفرغ العداد)</span>
                    <ChevronDown size={18} className={`transition-transform duration-300 ${openSettingsSections.uiPrayers ? 'rotate-180 text-yellow-400' : 'text-white/50'}`} />
                  </div>
                  <div className={`accordion-content ${openSettingsSections.uiPrayers ? 'open' : ''}`}>
                    <div className="px-4 pb-4">
                      <div className="flex justify-end gap-2 mb-2.5">
                        <button
                          onClick={() => setUiPrayers({ imsak: true, fajr: true, sunrise: true, dhuhr: true, asr: true, sunset: true, maghrib: true, isha: true, midnight: true })}
                          className="text-[10px] bg-teal-500/15 text-teal-300 border border-teal-500/30 px-2.5 py-1 rounded-lg font-bold"
                        >
                          تحديد الكل
                        </button>
                        <button
                          onClick={() => setUiPrayers({ imsak: false, fajr: false, sunrise: false, dhuhr: false, asr: false, sunset: false, maghrib: false, isha: false, midnight: false })}
                          className="text-[10px] bg-rose-500/15 text-rose-300 border border-rose-500/30 px-2.5 py-1 rounded-lg font-bold"
                        >
                          إلغاء الكل (إخفاء العداد)
                        </button>
                      </div>
                      <div className="grid grid-cols-2 sm:grid-cols-3 gap-2.5">
                        {visibleArcPrayers.map(p => (
                          <div key={p.id} onClick={() => setUiPrayers(prev => ({ ...prev, [p.id]: !prev[p.id] }))} className="flex justify-between items-center bg-black/30 p-3 rounded-xl cursor-pointer border border-white/5">
                            <span className="text-xs font-bold">{p.label}</span>
                            <div className={`w-4 h-4 rounded border flex items-center justify-center ${uiPrayers[p.id] ? 'bg-yellow-400 border-yellow-400':'border-white/20'}`}>
                              {uiPrayers[p.id] && <Check size={11} color="black" />}
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                </div>

                <div ref={el => settingsAccordionRefs.current.liveNotification = el} className="bg-black/40 rounded-3xl border border-white/5 overflow-hidden backdrop-blur-md">
                  <div className="p-4 flex justify-between items-center">
                    <div className="flex items-center gap-2 cursor-pointer" onClick={() => toggleSettingsAccordion('liveNotification')}>
                      <Zap size={16} className="text-yellow-400"/>
                      <h3 className="text-xs md:text-sm font-bold">الإشعار الحي</h3>
                      <ChevronDown size={16} className={`transition-transform duration-300 mr-1 ${openSettingsSections.liveNotification ? 'rotate-180 text-yellow-400' : 'text-white/50'}`} />
                    </div>
                    <div onClick={() => {
                      setLiveEnabled(!liveEnabled);
                      if (!liveEnabled) setOpenSettingsSections(p => ({ ...p, liveNotification: true }));
                    }} className={`w-11 h-6 rounded-full p-1 transition-colors cursor-pointer ${liveEnabled ? 'bg-yellow-400' : 'bg-white/10'}`}>
                      <div className={`w-4 h-4 rounded-full bg-white transition-transform ${liveEnabled ? 'translate-x-0' : '-translate-x-5'}`}></div>
                    </div>
                  </div>
                  <div className={`accordion-content ${openSettingsSections.liveNotification ? 'open' : ''}`}>
                    <div className="px-4 pb-4 pt-2 border-t border-white/10">
                      <div className="flex items-center justify-between mb-3 bg-black/30 p-3 rounded-xl border border-white/5">
                        <span className="text-xs opacity-80">تذكير قبل (دقيقة):</span>
                        <input 
                          type="text" 
                          inputMode="numeric" 
                          dir="ltr" 
                          value={reminderMinutes} 
                          onChange={e => { const val = e.target.value.replace(/[^0-9]/g, ''); setReminderMinutes(val ? parseInt(val, 10) : 0); }} 
                          className="w-16 bg-transparent text-center font-bold border-b border-yellow-400 outline-none text-sm text-white" 
                          style={{ fontFamily: 'Arial, sans-serif' }}
                        />
                      </div>
                      <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
                        {visibleArcPrayers.map(p => (
                          <div key={p.id} onClick={() => setLivePrayers(prev => ({ ...prev, [p.id]: !prev[p.id] }))} className="flex justify-between items-center bg-black/30 p-2.5 rounded-xl cursor-pointer border border-white/5">
                            <span className="text-xs">{p.label}</span>
                            <div className={`w-4 h-4 rounded border flex items-center justify-center ${livePrayers[p.id] ? 'bg-blue-400 border-blue-400':'border-white/20'}`}>
                              {livePrayers[p.id] && <Check size={10} color="black"/>}
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                </div>

                <div ref={el => settingsAccordionRefs.current.alarms = el} className="bg-black/40 rounded-3xl border border-white/5 overflow-hidden backdrop-blur-md">
                  <div className="p-4 flex justify-between items-center">
                    <div className="flex items-center gap-2 cursor-pointer" onClick={() => toggleSettingsAccordion('alarms')}>
                      <Bell size={16} className="text-red-400"/>
                      <h3 className="text-xs md:text-sm font-bold">تنبيه موعد الأذان</h3>
                      <ChevronDown size={16} className={`transition-transform duration-300 mr-1 ${openSettingsSections.alarms ? 'rotate-180 text-yellow-400' : 'text-white/50'}`} />
                    </div>
                    <div onClick={() => {
                      setAlarmEnabled(!alarmEnabled);
                      if (!alarmEnabled) setOpenSettingsSections(p => ({ ...p, alarms: true }));
                    }} className={`w-11 h-6 rounded-full p-1 transition-colors cursor-pointer ${alarmEnabled ? 'bg-red-500' : 'bg-white/10'}`}>
                      <div className={`w-4 h-4 rounded-full bg-white transition-transform ${alarmEnabled ? 'translate-x-0' : '-translate-x-5'}`}></div>
                    </div>
                  </div>
                  <div className={`accordion-content ${openSettingsSections.alarms ? 'open' : ''}`}>
                    <div className="px-4 pb-4 pt-2 border-t border-white/10">
                      <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
                        {visibleArcPrayers.map(p => (
                          <div key={p.id} onClick={() => setAlarmPrayers(prev => ({ ...prev, [p.id]: !prev[p.id] }))} className="flex justify-between items-center bg-black/30 p-2.5 rounded-xl cursor-pointer border border-white/5">
                            <span className="text-xs">{p.label}</span>
                            <div className={`w-4 h-4 rounded border flex items-center justify-center ${alarmPrayers[p.id] ? 'bg-red-400 border-red-400':'border-white/20'}`}>
                              {alarmPrayers[p.id] && <Check size={10} color="black"/>}
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          );
        })()}

        {/* ====================================================================
            3️⃣ شاشة التقويم الموحد والإمساكية الشهرية
           ==================================================================== */}
        {activeTab === 'calendar_hub' && monthlyTimes.length > 0 && (() => {
          const firstDayInfo = monthlyTimes[0];
          const headerTitle = calendarMode === 'hijri'
            ? `التقويم الهجري الشهري: ${firstDayInfo.hijriMonthName} ${toEn(firstDayInfo.hijriYear)} هـ`
            : `التقويم الميلادي الشهري: ${gregorianMonthsAr[calendarGregorianDate.getMonth()]} ${toEn(calendarGregorianDate.getFullYear())} م`;

          return (
            <div className="flex flex-col gap-5 w-full animate-in fade-in duration-300">
              <div className="w-full bg-black/40 border border-white/10 rounded-[2.2rem] p-5 shadow-2xl backdrop-blur-md flex flex-col md:flex-row items-center justify-between gap-4">
                <div className="flex bg-black/50 p-1.5 rounded-full border border-white/10">
                  <button
                    onClick={() => setCalendarMode('hijri')}
                    className={`px-4 py-2 rounded-full text-xs font-bold fluid-spring ${calendarMode === 'hijri' ? 'bg-teal-500 text-slate-950 shadow-md' : 'text-white/60 hover:text-white'}`}
                  >
                    التقويم الهجري
                  </button>
                  <button
                    onClick={() => setCalendarMode('gregorian')}
                    className={`px-4 py-2 rounded-full text-xs font-bold fluid-spring ${calendarMode === 'gregorian' ? 'bg-teal-500 text-slate-950 shadow-md' : 'text-white/60 hover:text-white'}`}
                  >
                    التقويم الميلادي
                  </button>
                </div>

                <div className="flex items-center gap-2">
                  <button
                    onClick={() => {
                      if (calendarMode === 'hijri') setCalendarHijriDayOffset(o => o - 30);
                      else setCalendarGregorianDate(d => new Date(d.getFullYear(), d.getMonth() - 1, 1));
                    }}
                    className="p-2.5 rounded-full bg-white/5 hover:bg-white/10 border border-white/10 text-teal-300"
                  >
                    <ChevronRight size={18} />
                  </button>
                  <span className="text-xs sm:text-sm md:text-base font-bold font-reem text-white px-2 text-center">
                    {headerTitle}
                  </span>
                  <button
                    onClick={() => {
                      if (calendarMode === 'hijri') setCalendarHijriDayOffset(o => o + 30);
                      else setCalendarGregorianDate(d => new Date(d.getFullYear(), d.getMonth() + 1, 1));
                    }}
                    className="p-2.5 rounded-full bg-white/5 hover:bg-white/10 border border-white/10 text-teal-300"
                  >
                    <ChevronLeft size={18} />
                  </button>
                </div>

                <button
                  onClick={() => {
                    setCalendarHijriDayOffset(0);
                    setCalendarGregorianDate(new Date(targetDateObj));
                  }}
                  className="px-4 py-2 rounded-full bg-teal-500/15 hover:bg-teal-500/25 border border-teal-500/30 text-teal-300 text-xs font-bold"
                >
                  العودة للشهر الحالي
                </button>
              </div>

              <div className="w-full bg-black/40 rounded-[2.2rem] overflow-hidden border border-white/10 shadow-2xl backdrop-blur-md">
                <div className="bg-teal-900/40 p-4 text-center font-bold text-teal-200 border-b border-white/10 text-xs sm:text-sm">
                  {headerTitle} — {locationInfo.name}
                </div>
                <div className="overflow-x-auto">
                  <table className="w-full text-xs md:text-sm text-center">
                    <thead className="bg-white/5 text-teal-300 font-bold border-b border-white/10">
                      <tr>
                        <th className="p-3.5">التاريخ الهجري</th>
                        <th className="p-3.5">الميلادي</th>
                        {shouldShowImsak && <th className="p-3.5">الإمساك</th>}
                        <th className="p-3.5">الفجر</th>
                        <th className="p-3.5">الشروق</th>
                        <th className="p-3.5">الظهر</th>
                        {showExtraPrayers && <th className="p-3.5">العصر</th>}
                        <th className="p-3.5">المغرب</th>
                        <th className="p-3.5">العشاء</th>
                        <th className="p-3.5">منتصف الليل</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-white/5">
                      {monthlyTimes.map((day, idx) => {
                        const isToday = day.date.getDate() === targetDateObj.getDate() && day.date.getMonth() === targetDateObj.getMonth() && day.date.getFullYear() === targetDateObj.getFullYear();
                        return (
                          <tr key={idx} className={isToday ? 'bg-teal-500/20 border-l-4 border-teal-400' : 'hover:bg-white/5 transition-colors'}>
                            <td className="p-3.5">
                              <div className={`font-black ${isToday ? 'text-teal-300' : 'text-white'}`}>{day.hijriStr}</div>
                              <div className="text-[10px] text-white/40">{daysOfWeekAr[day.date.getDay()]}</div>
                            </td>
                            <td className="p-3.5 font-mono text-xs text-white/60">{day.date.toLocaleDateString('en-GB')}</td>
                            {shouldShowImsak && <td className="p-3.5 font-mono text-rose-300/80">{day.times.imsak}</td>}
                            <td className="p-3.5 font-mono font-bold text-white/90">{day.times.fajr}</td>
                            <td className="p-3.5 font-mono text-white/60">{day.times.sunrise}</td>
                            <td className="p-3.5 font-mono text-white/75">{day.times.dhuhr}</td>
                            {showExtraPrayers && <td className="p-3.5 font-mono text-white/65">{day.times.asr}</td>}
                            <td className="p-3.5 font-mono text-teal-300 font-black">{day.times.maghrib}</td>
                            <td className="p-3.5 font-mono text-white/65">{day.times.isha}</td>
                            <td className="p-3.5 font-mono text-indigo-300">{day.times.midnight}</td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          );
        })()}

        {/* ====================================================================
            4️⃣ شاشة القبلة السماوية ومراقد أئمة أهل البيت (عليهم السلام)
           ==================================================================== */}
        {activeTab === 'qibla_hub' && (() => {
          const activeShrine = AHLULBAYT_SHRINES.find(s => s.id === selectedShrineId) || AHLULBAYT_SHRINES[0];
          const targetBearing = Math.round(calculateBearing(locationInfo.lat, locationInfo.lon, activeShrine.lat, activeShrine.lon));
          const targetDistance = Math.round(calculateDistance(locationInfo.lat, locationInfo.lon, activeShrine.lat, activeShrine.lon));

          return (
            <div className="flex flex-col gap-6 w-full animate-in fade-in duration-300">
              <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-center bg-black/40 border border-white/10 rounded-[2.2rem] p-5 sm:p-6 shadow-2xl backdrop-blur-md">
                
                <div className="lg:col-span-6 flex flex-col items-center justify-center">
                  <div className="relative w-64 h-64 sm:w-80 sm:h-80 flex items-center justify-center">
                    <svg viewBox="0 0 300 300" className="w-full h-full">
                      <defs>
                        <radialGradient id="compassSky" cx="50%" cy="50%" r="50%">
                          <stop offset="0%" stopColor="#0f172a" />
                          <stop offset="100%" stopColor="#050811" />
                        </radialGradient>
                      </defs>
                      <circle cx="150" cy="150" r="142" fill="url(#compassSky)" stroke="rgba(255,255,255,0.12)" strokeWidth="2" />
                      <circle cx="150" cy="150" r="115" fill="none" stroke="rgba(45,212,191,0.18)" strokeWidth="1" strokeDasharray="4 6" />
                      <circle cx="150" cy="150" r="82" fill="none" stroke="rgba(255,255,255,0.08)" strokeWidth="1" />

                      <text x="150" y="26" textAnchor="middle" fill="#2dd4bf" fontSize="13" fontWeight="bold">شمال (N)</text>
                      <text x="276" y="154" textAnchor="middle" fill="rgba(255,255,255,0.5)" fontSize="11" fontWeight="bold">شرق</text>
                      <text x="150" y="284" textAnchor="middle" fill="rgba(255,255,255,0.5)" fontSize="11" fontWeight="bold">جنوب</text>
                      <text x="24" y="154" textAnchor="middle" fill="rgba(255,255,255,0.5)" fontSize="11" fontWeight="bold">غرب</text>

                      {AHLULBAYT_SHRINES.map((shrine) => {
                        const b = calculateBearing(locationInfo.lat, locationInfo.lon, shrine.lat, shrine.lon);
                        const rad = (b - 90) * DTR;
                        const sx = 150 + 115 * Math.cos(rad);
                        const sy = 150 + 115 * Math.sin(rad);
                        const isSel = shrine.id === activeShrine.id;
                        return (
                          <g key={shrine.id} onClick={() => setSelectedShrineId(shrine.id)} className="cursor-pointer">
                            <circle cx={sx} cy={sy} r={isSel ? 7 : 4.5} fill={shrine.color} stroke="#fff" strokeWidth={isSel ? 2 : 0.8} />
                          </g>
                        );
                      })}

                      <g transform={`rotate(${targetBearing}, 150, 150)`} className="fluid-spring">
                        <line x1="150" y1="150" x2="150" y2="42" stroke={activeShrine.color} strokeWidth="3.5" strokeLinecap="round" />
                        <polygon points="150,28 142,48 158,48" fill={activeShrine.color} />
                        <circle cx="150" cy="150" r="9" fill={activeShrine.color} />
                      </g>
                    </svg>
                  </div>
                </div>

                <div className="lg:col-span-6 flex flex-col gap-4">
                  <div className="bg-white/[0.03] border border-white/10 rounded-3xl p-5">
                    <span className="text-xs text-teal-400 font-bold">الاتجاه الفلكي المباشر من {locationInfo.name.split(' (')[0]}</span>
                    <h2 className="text-xl sm:text-2xl font-bold font-reem text-white mt-1">{activeShrine.name}</h2>
                    <p className="text-xs text-white/60 mt-1">{activeShrine.imams}</p>
                    <div className="grid grid-cols-2 gap-3 mt-4">
                      <div className="bg-black/40 p-3.5 rounded-2xl border border-white/5">
                        <span className="text-[11px] text-white/50 block">زاوية الانحراف عن الشمال</span>
                        <span className="text-2xl font-black text-amber-400" style={{ fontFamily: 'Arial, sans-serif' }}>{toEn(targetBearing)}°</span>
                      </div>
                      <div className="bg-black/40 p-3.5 rounded-2xl border border-white/5">
                        <span className="text-[11px] text-white/50 block">المسافة الجوية المباشرة</span>
                        <span className="text-2xl font-black text-teal-300" style={{ fontFamily: 'Arial, sans-serif' }}>{toEn(targetDistance)} كم</span>
                      </div>
                    </div>
                  </div>

                  <div className="bg-white/[0.03] border border-white/10 rounded-3xl p-4 flex items-center gap-4">
                    <svg width="56" height="56" viewBox="-28 -28 56 56" className="shrink-0">
                      <RealisticMoonPhaseSVG hijriDay={currentHijri.day} size={36} idSuffix="qibla-card" />
                    </svg>
                    <div>
                      <div className="text-sm font-bold text-white">{moonInfo.phaseName} ({toEn(currentHijri.day)} {currentHijri.monthName})</div>
                      <div className="text-xs text-white/60 mt-0.5">نسبة إضاءة القمر الفلكية الليلة: {toEn(moonInfo.illumination)}%</div>
                    </div>
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3.5">
                {AHLULBAYT_SHRINES.map((shrine) => {
                  const b = Math.round(calculateBearing(locationInfo.lat, locationInfo.lon, shrine.lat, shrine.lon));
                  const d = Math.round(calculateDistance(locationInfo.lat, locationInfo.lon, shrine.lat, shrine.lon));
                  const isSel = shrine.id === selectedShrineId;
                  return (
                    <button
                      key={shrine.id}
                      onClick={() => setSelectedShrineId(shrine.id)}
                      className={`text-right p-4 rounded-3xl border fluid-spring flex flex-col justify-between gap-2 ${
                        isSel ? 'bg-teal-500/15 border-teal-400 shadow-lg' : 'bg-black/40 border-white/5 hover:bg-white/5'
                      }`}
                    >
                      <div className="flex items-center justify-between w-full">
                        <span className="text-xs font-bold px-2.5 py-0.5 rounded-full bg-white/10 text-white/80">{shrine.city}</span>
                        <span className="text-xs font-bold font-mono text-amber-300">{toEn(b)}° • {toEn(d)} كم</span>
                      </div>
                      <div className="text-sm font-bold text-white mt-1">{shrine.name}</div>
                      <div className="text-[11px] text-white/50 line-clamp-1">{shrine.imams}</div>
                    </button>
                  );
                })}
              </div>
            </div>
          );
        })()}

        {/* ====================================================================
            5️⃣ شاشة الأدعية والزيارات الشيعية (بدون أي ميزات صوتية)
           ==================================================================== */}
        {activeTab === 'duas_hub' && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 w-full animate-in fade-in duration-300">
            {DUAS_LIBRARY.map((dua) => (
              <div
                key={dua.id}
                onClick={() => setOpenedDua(dua)}
                className="cursor-pointer bg-black/40 hover:bg-white/[0.04] border border-white/10 rounded-[2rem] p-6 shadow-xl backdrop-blur-md fluid-spring flex flex-col justify-between gap-4"
              >
                <div>
                  <span className="text-[11px] font-bold text-teal-400 bg-teal-500/10 border border-teal-500/20 px-3 py-1 rounded-full">
                    {dua.category}
                  </span>
                  <h3 className="text-xl font-bold font-reem text-white mt-3">{dua.title}</h3>
                  <p className="text-xs text-white/60 mt-1 leading-relaxed">{dua.subtitle}</p>
                </div>
                <div className="flex items-center justify-between text-xs text-teal-300 font-bold pt-3 border-t border-white/5">
                  <span>فتح نص الدعاء الكامل</span>
                  <ChevronLeft size={16} />
                </div>
              </div>
            ))}
          </div>
        )}

        {/* ====================================================================
            6️⃣ شاشة مسبحة السيدة فاطمة الزهراء (عليها السلام)
           ==================================================================== */}
        {activeTab === 'tasbih_hub' && (
          <div className="w-full max-w-xl self-center bg-black/40 border border-white/10 rounded-[2.5rem] p-6 sm:p-8 shadow-2xl backdrop-blur-md flex flex-col items-center text-center gap-6 animate-in fade-in duration-300">
            <span className="text-xs font-bold text-teal-300 bg-teal-500/10 border border-teal-500/20 px-4 py-1.5 rounded-full">
              {tasbihSteps[tasbihStage].sub}
            </span>
            <h2 className="text-3xl sm:text-4xl font-bold font-reem text-white">{tasbihSteps[tasbihStage].label}</h2>

            <button
              onClick={handleTasbihIncrement}
              className="w-48 h-48 sm:w-52 sm:h-52 rounded-full bg-gradient-to-b from-teal-500/25 to-indigo-900/40 border-2 border-teal-400/40 shadow-[0_0_40px_rgba(20,184,166,0.25)] active:scale-95 fluid-spring flex flex-col items-center justify-center gap-1"
            >
              <span className="text-5xl font-black text-white" style={{ fontFamily: 'Arial, sans-serif' }}>{toEn(tasbihCount)}</span>
              <span className="text-xs text-teal-300 font-bold">من {toEn(tasbihSteps[tasbihStage].max)}</span>
            </button>

            <div className="grid grid-cols-3 gap-2.5 w-full">
              {tasbihSteps.map((st, idx) => (
                <button
                  key={idx}
                  onClick={() => { setTasbihStage(idx); setTasbihCount(0); }}
                  className={`p-3 rounded-2xl border text-xs font-bold fluid-spring ${
                    tasbihStage === idx ? 'bg-teal-500/20 border-teal-400 text-white' : 'bg-black/30 border-white/5 text-white/50'
                  }`}
                >
                  <div>{st.label}</div>
                  <div className="text-[10px] opacity-70 mt-0.5">{toEn(st.max)} مرة</div>
                </button>
              ))}
            </div>

            <button
              onClick={() => { setTasbihStage(0); setTasbihCount(0); }}
              className="flex items-center gap-2 text-xs text-rose-300 bg-rose-500/10 hover:bg-rose-500/20 border border-rose-500/20 px-5 py-2.5 rounded-full font-bold"
            >
              <RotateCcw size={14} /> تصفير المسبحة والبدء من جديد
            </button>
          </div>
        )}

        {/* ====================================================================
            7️⃣ شاشة فقه المواقيت وخطوط العرض العليا (موسوعة مقارنة المذاهب ومعايير الأوقات الكاملة)
           ==================================================================== */}
        {activeTab === 'fiqh_hub' && (
          <div className="w-full bg-black/40 border border-white/5 rounded-[2.2rem] p-5 sm:p-6 shadow-2xl backdrop-blur-md flex flex-col gap-4 animate-in fade-in duration-300">
            <h2 className="text-lg sm:text-xl font-bold text-teal-300 mb-1 flex items-center gap-2">
              <BookOpen size={20} /> دليل مقارنة المذاهب ومعايير الأوقات وفتاوى خطوط العرض العليا
            </h2>
            
            {[
              {
                id: 'all_standards_reference',
                title: '📐 المرجع الشامل لجميع معايير الأوقات والزوايا الفلكية المعتمدة (أوفلاين)',
                content: `فيما يلي تفصيل كافة معايير وزوايا المواقيت المعتمدة في المحرك الفلكي:

• 1. معيار الكفيل (المرجعية العليا في النجف الأشرف - العتبة العباسية):
  - الفجر: 18.0° تحت الأفق الشرقي | الشروق والغروب: 0.833° | المغرب: 4.0° (ذهاب الحمرة المشرقية) | العشاء: 14.0°.
  - معالجة العروض العليا صيفاً: إسقاط أقرب بلد معتدل (خط عرض 48°) أو تقويم الصائغ الاحترازي.

• 2. معهد بحوث ليفا - مدينة قم المقدسة (شيعة اثنا عشري):
  - الفجر: 16.0° تحت الأفق | المغرب: 4.0° (ذهاب الحمرة المشرقية) | العشاء: 14.0°.
  - معالجة العروض العليا صيفاً: إسقاط الإحداثيات على خط عرض 45°.

• 3. معهد الجيوفيزياء - جامعة طهران (IGUT):
  - الفجر: 17.7° تحت الأفق | المغرب: 4.5° (ذهاب الحمرة المشرقية) | العشاء: 14.0°.
  - معالجة العروض العليا صيفاً: إسقاط الإحداثيات على خط عرض 45°.

• 4. مؤسسة الإمام علي عليه السلام - لندن (شيعي لأوروبا):
  - الفجر: 15.0° تحت الأفق | المغرب: 4.0° | العشاء: 15.0° (مع إسقاط خط عرض 48° عند تداخل الشفق).

• 5. تقويم أم القرى - مكة المكرمة:
  - الفجر: 18.5° تحت الأفق | المغرب: عند الغروب الفلكي (0.833°) | العشاء: 90 دقيقة ثابتة بعد المغرب (وتصبح 120 دقيقة تلقائياً في شهر رمضان).

• 6. رابطة العالم الإسلامي (MWL):
  - الفجر: 18.0° تحت الأفق | المغرب: 0.833° | العشاء: 17.0° تحت الأفق الغربي.

• 7. الهيئة المصرية العامة للمساحة:
  - الفجر: 19.5° تحت الأفق | المغرب: 0.833° | العشاء: 17.5° تحت الأفق.

• 8. الجمعية الإسلامية لأمريكا الشمالية (ISNA):
  - الفجر: 15.0° تحت الأفق | المغرب: 0.833° | العشاء: 15.0° تحت الأفق.

• 9. جامعة العلوم الإسلامية بكراتشي:
  - الفجر: 18.0° تحت الأفق | المغرب: 0.833° | العشاء: 18.0° تحت الأفق.

• 10. اتحاد المنظمات الإسلامية في فرنسا (UOIF 12°):
  - الفجر: 12.0° تحت الأفق | المغرب: 0.833° | العشاء: 12.0° تحت الأفق.

• 11. المجلس الأوروبي للإفتاء والبحوث (ECFR):
  - الفجر: 15.0° تحت الأفق | المغرب: 0.833° | العشاء: 15.0° (مع قاعدة الكسر النسبي العشري أو الفاصل الثابت صيفاً).`
              },
              {
                id: 'shia_midnight_rule',
                title: '📌 منتصف الليل الشرعي الشيعي (تحديث فقهي هام)',
                content: `• المبدأ الفقهي: منتصف الليل الشرعي عند الطائفة الجعفرية هو منتصف المسافة الزمنية الواقعة تماماً بين [غروب الشمس] و[طلوع الفجر الصادق].
                
                • المعادلة الرياضية للبرمجة:
                Midnight = Sunset + ((Fajr - Sunset) / 2)
                
                • تلافي الأخطاء السابقة: تم بحمد الله تصحيح هذه الخوارزمية كلياً بالصيف في لندن لتلافي خطأ العبور الحسابي لخانة الصباح (AM). يظهر التوقيت الآن دقيقاً جداً بالمساء (11:20 م) بدلاً من (11:20 ص) بمطابقة تامة للتقاويم المعتمدة عسكرياً وشرعياً.`
              },
              {
                id: 'alkafeel_vs_london',
                title: '1️⃣ الفرق الفقهي بين الكفيل ومؤسسة لندن (السيستاني) صيفاً',
                content: `على الرغم من كونهما يتبعان فتاوى المرجع الأعلى السيد السيستاني (دام ظله)، إلا أن هناك اختلافاً بـ 48 دقيقة صيفاً بلندن للسبب التالي:
                
                • معهد الكفيل (العتبة العباسية): يعتمد زاوية الفجر الطبيعية 18° تحت الأفق. صيفاً عند تداخل الشفق، يسقط الإحداثيات على خط عرض 48° (باريس) لمطابقة فتوى لندن لتسهيل العبادات، أو يتيح تقويم الصائغ كمعيار بديل.
                
                • مؤسسة لندن (مؤسسة الإمام علي بلندن): تعتمد زاوية فلكية ناعمة وهي 15° تحت الأفق (وهو المعتمد فقهياً لديهم لبلدان شمال أوروبا لتسهيل العبادات وتقليل المشقة على الجالية).
                
                • لغز الـ 48 دقيقة صيفاً: صيفاً في لندن (خط عرض 51.5°)، تنحدر الشمس بشكل مائل وجانبي جداً موازٍ للأفق، وتتحرك ببطء شديد صعوداً وهبوطاً. هذا التباطؤ يجعل الفارق البسيط بين زاوية 15° وزاوية 18° (3 درجات فقط) يتمدد زمنياً ليصبح 48 دقيقة كاملة! بينما في البلاد القريبة من خط الاستواء (مثل كربلاء ومكة)، يكون انحدار الشمس عمودياً وسريعاً فلا يتجاوز الفارق بينهما 12 إلى 15 دقيقة.`
              },
              {
                id: 'leufa_tehran_vs_kafeel',
                title: '2️⃣ مركز لوفا (قم) وجامعة طهران مقابل الكفيل في الزوايا العليا',
                content: `• مركز لوفا (قم المقدسة): يعتمد زاوية الفجر 16.0° والمغرب زوال الحمرة 4.0°. صيفاً في الزوايا العليا يسقط الأوقات على خط عرض 45° تماماً.
                
                • جامعة طهران: تعتمد زاوية الفجر 17.7° والمغرب زوال الحمرة 4.5°. صيفاً تسقط الحسابات أيضاً على خط عرض 45° تماماً.
                
                • لماذا يختلفون صيفاً؟ إسقاط خط عرض 45° (لوفا وطهران) يختلف تماماً عن إسقاط خط عرض 48° (الكفيل ومؤسسة لندن)، لأن الشمس في خط عرض 45° تنزل لعمق أكبر تحت الأفق مقارنة بخط عرض 48°، مما يجعل الفجر متأخراً والمغرب مبكراً، وهما متطابقان أوفلاين لأنهما يسقطان على نفس دائرة العرض الافتراضية صيفاً في حال تفعيل المحرك.`
              },
              {
                id: 'sayegh_fatwa_detail',
                title: '3️⃣ مسلك تقويم الصائغ الشيعي الاحترازي بالتفصيل صيفاً',
                content: `• الفكرة والفتوى: يعتبر المهندس الصائغ فقهياً وعلمياً أنه بمجرد وصول الشمس لأدنى نقطة انخفاض لها تحت الأفق (منتصف الليل الفلكي) وبداية صعودها التدريجي، يبدأ تزايد النور تكوينياً في الأفق.
                
                • الإمساك صيفاً: يوجب الصائغ الإمساك عند منتصف الليل الفلكي صيانةً للصوم ويقين الإمساك قبل أي زيادة للنور (مثلاً الساعة 11:20 مساءً حسب الأفق).
                
                • صلاة الصبح: يؤخرها وجوباً حتى حدوث (الإسفار) وهو انتشار الضوء الحسي الملاحظ بالأفق قبل الشروق بحوالي 75 دقيقة، وذلك لعدم تمايز الفجر الصادق الطبيعي في فترة تداخل الشفق بالصيف.`
              },
              {
                id: 'sunni_methods_detail',
                title: '4️⃣ تقويم أم القرى والمذاهب السنية (الفاصل الثابت والسبع والكسر النسبي)',
                content: `تطرح المدارس السنية صيفاً في مناطق الشفق المتداخل الخيارات التالية:
                
                • الفاصل الثابت: وهو فرض وقت الفجر 90 دقيقة ثابتة قبل شروق الشمس، والعشاء 90 دقيقة بعد غروبها، وفي رمضان تزداد تلقائياً في أم القرى إلى 120 دقيقة تيسيراً وتطابقاً مع تقاويم الحرمين الشريفين.
                
                • قاعدة سُبع الليل: تقسيم الليل التكويني الفعلي إلى سبعة أقسام متساوية، واعتيار وقت الفجر الشرعي الاستثنائي في السبع الأخير قبل الشروق.
                
                • الكسر النسبي العشري (Angle-Based Rule): وهو حساب نسبة الزاوية الشرعية للفجر أو العشاء (مثلاً 18 درجة) من الـ 60 درجة الكلية لزاوية دوران الأفق ليلاً.
                
                المعادلة للفجر: Fajr = Sunrise - (Night * (Fajr Angle / 60)).
                المعادلة للعشاء: Isha = Sunset + (Night * (Isha Angle / 60)).
                وهذا الكسر النسبي يضمن عدم تداخل الأوقات صيفاً وتدرج الأوقات بشكل طبيعي.`
              },
              {
                id: 'france_and_europe',
                title: '5️⃣ معاهد أوروبا السنية والشيعية الأخرى (فرنسا 12° وغيرها)',
                content: `• اتحاد فرنسا (UOIF): يعتمد زاوية 12° دائماً للفجر والعشاء صيفاً وشتاءً لتسهيل العبادات ومنع تعطل الحسابات أو المشقة على المسلمين.
                
                • المجلس الأوروبي للإفتاء والبحوث: يعتمد زاوية 15° للفجر والعشاء، وصيفاً يطبق الكسر النسبي العشري في المناطق المرتفعة.
                
                • الهيئة المصرية العامة للمساحة: تعتمد زاوية الفجر الكبرى 19.5° وهي تسبب تعطلاً فلكياً صيفاً في أوروبا، فيلجأ التطبيق تلقائياً لخفض الزاوية تدريجياً وبشكل آمن لمنع تعطل الحسابات.`
              },
              {
                id: 'seasonal_rules_detail',
                title: '6️⃣ هل هذه الاستثناءات تُطبق صيفاً فقط؟ (الموسمية الفلكية)',
                content: `نعم، تُطبق هذه القواعد الاستثنائية للزوايا العليا فقط في أواخر الربيع وفصل الصيف (عندما يكون ميل الشمس موجباً ومرتفعاً). أما في الخريف والشتاء، فإن الشمس تهبط ليلاً تحت الأفق بأعماق كافية تزيد عن 20 درجة، وبالتالي تعود علامات الفجر والعشاء الصادقة للظهور بوضوح فلكي دامس، وعليه تُلغى الاستثناءات تلقائياً في الشتاء وتُطبق الزوايا الطبيعية لتوفر العلامات التكوينية الفلكية.`
              }
            ].map((sec) => (
              <div key={sec.id} className="border-b border-white/5 pb-3">
                <button 
                  onClick={() => setExpandedFiqhId(expandedFiqhId === sec.id ? null : sec.id)} 
                  className="w-full flex items-center justify-between py-3 text-right font-bold text-white/80 hover:text-white transition-colors text-xs md:text-sm"
                >
                  <span className="flex items-center gap-2"><Cpu size={16} className="text-teal-400 shrink-0"/> {sec.title}</span>
                  <ChevronDown size={16} className={`transition-transform duration-300 shrink-0 ${expandedFiqhId === sec.id ? 'rotate-180' : ''}`} />
                </button>
                {expandedFiqhId === sec.id && (
                  <div className="mt-2 text-white/60 text-xs md:text-sm leading-relaxed p-4 bg-white/[0.01] rounded-xl border border-white/5 whitespace-pre-line font-medium font-sans">
                    {sec.content}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </main>

      {/* ====================================================================
          نافذة تقويم وإمساكية شاشة القوس المنفصلة (مطابقة لتصميم V7 الأصلي)
         ==================================================================== */}
      {arcShowCalendarModal && (() => {
        const arcMonthDays = generateCalendarMonthDays(locationInfo, hijriAdjustment, method, asrMethod, highLatRule, manualOffsets, use24Hour, 'hijri', targetDateObj, 0, dstMode);
        const totalMonthDays = arcMonthDays.length || 30;

        return (
          <div className="fixed inset-0 z-50 bg-[#0a0f1d]/95 backdrop-blur-2xl flex flex-col p-4 pt-8 animate-in fade-in slide-in-from-bottom-6 duration-300">
            <div className="max-w-3xl w-full mx-auto flex flex-col flex-1 overflow-hidden">
              <div className="flex justify-between items-start mb-5 px-2">
                <div className="flex-1">
                  <h2 className="text-[1.3rem] font-bold text-white font-reem drop-shadow-md leading-tight mb-1">
                    تقويم شهر <span className="text-yellow-400">{currentHijri.monthName}</span> لعام {toEn(currentHijri.year)} هـ
                  </h2>
                  <div className="flex gap-3 mt-3">
                    <div className="bg-white/10 border border-white/20 rounded-lg px-3 py-1.5 flex items-center gap-2 shadow-sm">
                      <span className="text-[10px] text-white/70 font-bold">المنقضية:</span>
                      <span className="text-[14px] font-bold text-white" style={{ fontFamily: 'Arial, sans-serif' }}>{toEn(Math.max(0, currentHijri.day - 1))}</span>
                    </div>
                    <div className="bg-gradient-to-r from-yellow-500/20 to-orange-500/10 border border-yellow-400/40 rounded-lg px-3 py-1.5 flex items-center gap-2 shadow-md">
                      <span className="text-[10px] text-yellow-200/90 font-bold">المتبقية:</span>
                      <span className="text-[14px] font-bold text-yellow-400 drop-shadow-md" style={{ fontFamily: 'Arial, sans-serif' }}>{toEn(Math.max(0, totalMonthDays - currentHijri.day + 1))}</span>
                    </div>
                  </div>
                </div>
                <button onClick={() => setArcShowCalendarModal(false)} className="p-2.5 bg-white/10 rounded-full hover:bg-white/20 transition-colors mr-3 mt-1 text-white">
                  <X size={20}/>
                </button>
              </div>

              <div className="flex-1 overflow-y-auto hide-scrollbar space-y-4 pb-10 px-1 mt-1">
                {arcMonthDays.map((d, i) => {
                  const hDay = d.hijriDay;
                  const isPassed = hDay < currentHijri.day;
                  const isToday = hDay === currentHijri.day;
                  const dt = d.date;

                  return (
                    <div key={i} ref={el => arcCalendarDayRefs.current[hDay] = el} className={`rounded-[24px] p-4 transition-all ${isToday ? 'bg-gradient-to-br from-white/15 to-white/5 border border-yellow-400/60 shadow-[0_10px_30px_rgba(0,0,0,0.5)] scale-[1.01]' : 'bg-white/5 border border-white/5'} ${isPassed ? 'opacity-50 grayscale' : ''}`}>
                      <div className="flex justify-between items-center mb-4 border-b border-white/10 pb-3">
                        <div className="flex items-center gap-3">
                          <span className={`w-8 h-8 rounded-full flex items-center justify-center font-bold text-sm ${isToday ? 'bg-yellow-400 text-black shadow-md' : 'bg-white/10 text-white'}`} style={{ fontFamily: 'Arial, sans-serif' }}>{toEn(hDay)}</span>
                          <span className="text-[13px] font-bold text-white">{daysOfWeekAr[dt.getDay()]}</span>
                        </div>
                        <span className="text-[10px] opacity-70 font-bold tracking-widest text-white" style={{ fontFamily: 'Arial, sans-serif' }}>{toEn(dt.getDate())} - {toEn(dt.getMonth() + 1)} - {toEn(dt.getFullYear())} م</span>
                      </div>
                      <div className="grid grid-cols-3 gap-2 text-center" style={{ fontFamily: 'Arial, sans-serif' }}>
                        {visibleArcPrayers.map(p => {
                          const tVal = formatArcHour(d.times.rawHours[p.id]);
                          const Icon = p.icon;
                          return (
                            <div key={p.id} className="bg-black/30 rounded-xl p-2 relative overflow-hidden group">
                              <Icon size={30} className="absolute -left-2 -bottom-2 opacity-10 text-white group-hover:scale-110 transition-transform" />
                              <div className="flex justify-center items-center gap-1 mb-1 relative z-10">
                                <Icon size={10} className="opacity-60 text-yellow-400" />
                                <span className="text-[9px] opacity-80 font-sans text-white">{p.label}</span>
                              </div>
                              <div className="text-[11px] font-bold text-white relative z-10">
                                {use24Hour ? toEn(tVal.hhmm24) : toEn(tVal.t)} <span className="text-[7px] opacity-50">{tVal.ap}</span>
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        );
      })()}

      {/* ====================================================================
          نافذة قراءة الدعاء أو الزيارة الكاملة (بدون صوتيات)
         ==================================================================== */}
      {openedDua && (
        <div className="fixed inset-0 z-50 bg-[#050811]/95 backdrop-blur-2xl flex flex-col p-4 pt-6 animate-in fade-in duration-300">
          <div className="max-w-3xl w-full mx-auto flex flex-col flex-1 overflow-hidden">
            <div className="h-[56px] sm:h-[62px] bg-[#0b1120] border border-white/10 rounded-full px-5 flex items-center justify-between shadow-2xl mb-5 shrink-0">
              <div className="flex flex-col">
                <span className="text-[10px] text-teal-400 font-bold">{openedDua.category}</span>
                <h3 className="text-sm sm:text-base font-bold font-reem text-white truncate">{openedDua.title}</h3>
              </div>
              <button onClick={() => setOpenedDua(null)} className="w-9 h-9 rounded-full bg-white/10 hover:bg-white/20 flex items-center justify-center text-white">
                <X size={18} />
              </button>
            </div>

            <div className="flex-1 overflow-y-auto hide-scrollbar bg-white/[0.02] border border-white/10 rounded-[2.2rem] p-6 sm:p-10 text-center">
              <p className="font-amiri text-xl sm:text-2xl leading-[2.3] text-amber-50 whitespace-pre-line">
                {openedDua.text}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* ====================================================================
          شريط التنقل السفلي البيضوي العائم المتجاوب (M3 Expressive Floating Pill Bottom Bar)
         ==================================================================== */}
      <nav className="fixed bottom-2 inset-x-0 z-40 px-2 sm:px-3 flex justify-center pointer-events-none">
        <div className="pointer-events-auto bg-[#0b1120]/90 backdrop-blur-2xl border border-white/10 rounded-full p-1 sm:p-1.5 shadow-[0_15px_35px_rgba(0,0,0,0.75)] flex items-center gap-0.5 sm:gap-1 overflow-x-auto max-w-full hide-scrollbar">
          {[
            { id: 'sky_arc', label: 'اليوم', icon: Sparkles },
            { id: 'settings_hub', label: 'الضبط والأوقات', icon: Sliders },
            { id: 'calendar_hub', label: 'التقويم', icon: CalendarDays },
            { id: 'qibla_hub', label: 'القبلة والمراقد', icon: Compass },
            { id: 'duas_hub', label: 'الأدعية', icon: BookOpen },
            { id: 'tasbih_hub', label: 'المسبحة', icon: Check },
            { id: 'fiqh_hub', label: 'فقه المواقيت', icon: Cpu }
          ].map((tab) => {
            const Icon = tab.icon;
            const isActive = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => { setActiveTab(tab.id); setError(null); }}
                className={`flex items-center gap-1 sm:gap-1.5 px-2.5 sm:px-3.5 py-1.5 sm:py-2 rounded-full text-[10px] sm:text-xs font-bold whitespace-nowrap fluid-spring ${
                  isActive 
                    ? 'bg-gradient-to-r from-teal-500 to-indigo-600 text-white shadow-[0_4px_15px_rgba(20,184,166,0.4)] scale-[1.03]' 
                    : 'text-white/55 hover:text-white hover:bg-white/5'
                }`}
              >
                <Icon size={13} className={isActive ? 'text-white shrink-0' : 'text-teal-400/70 shrink-0'} />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </div>
      </nav>
    </div>
  );
}