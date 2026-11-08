# ============================================================================
#  توليد قاعدة المدن العالمية أوفلاين (cities.json)
#  المصدر: GeoNames
#    - cities5000.txt        (كل المدن التي يزيد سكانها عن 5000 نسمة + العواصم)
#    - admin1CodesASCII.txt  (المحافظات/الولايات/الأقاليم في العالم)
#  الترخيص: CC BY 4.0 — https://download.geonames.org
#  الاستخدام: powershell -File scripts/build-cities-json.ps1
# ============================================================================
param(
    [string]$Source = "$env:TEMP\geonames\cities5000.txt",
    [string]$Admin1Source = "$env:TEMP\geonames\admin1CodesASCII.txt",
    [string]$OutFile = ""
)

$ErrorActionPreference = 'Stop'

if (-not $OutFile -or $OutFile -eq '') {
    $root = Split-Path -Parent $PSScriptRoot
    $OutFile = Join-Path $root 'shared\src\commonMain\composeResources\files\cities.json'
}

if (-not (Test-Path $Source)) {
    throw "ملف المصدر غير موجود: $Source (نزّل cities5000.zip من download.geonames.org وفك ضغطه)"
}

# رمز الدولة -> الاسم العربي
$countryAr = @{
    'AD'='أندورا'; 'AE'='الإمارات'; 'AF'='أفغانستان'; 'AG'='أنتيغوا وبربودا'; 'AI'='أنغويلا'
    'AL'='ألبانيا'; 'AM'='أرمينيا'; 'AO'='أنغولا'; 'AR'='الأرجنتين'; 'AS'='ساموا الأمريكية'
    'AT'='النمسا'; 'AU'='أستراليا'; 'AW'='أروبا'; 'AX'='جزر أولاند'; 'AZ'='أذربيجان'
    'BA'='البوسنة والهرسك'; 'BB'='بربادوس'; 'BD'='بنغلاديش'; 'BE'='بلجيكا'; 'BF'='بوركينا فاسو'
    'BG'='بلغاريا'; 'BH'='البحرين'; 'BI'='بوروندي'; 'BJ'='بنين'; 'BL'='سان بارتيلمي'
    'BM'='برمودا'; 'BN'='بروناي'; 'BO'='بوليفيا'; 'BQ'='الجزر الكاريبية الهولندية'; 'BR'='البرازيل'
    'BS'='الباهاما'; 'BT'='بوتان'; 'BW'='بوتسوانا'; 'BY'='روسيا البيضاء'; 'BZ'='بليز'
    'CA'='كندا'; 'CC'='جزر كوكوس'; 'CD'='الكونغو الديمقراطية'; 'CF'='أفريقيا الوسطى'; 'CG'='الكونغو'
    'CH'='سويسرا'; 'CI'='ساحل العاج'; 'CK'='جزر كوك'; 'CL'='تشيلي'; 'CM'='الكاميرون'
    'CN'='الصين'; 'CO'='كولومبيا'; 'CR'='كوستاريكا'; 'CU'='كوبا'; 'CV'='الرأس الأخضر'
    'CW'='كوراساو'; 'CX'='جزيرة كريسماس'; 'CY'='قبرص'; 'CZ'='التشيك'; 'DE'='ألمانيا'
    'DJ'='جيبوتي'; 'DK'='الدنمارك'; 'DM'='دومينيكا'; 'DO'='الدومينيكان'; 'DZ'='الجزائر'
    'EC'='الإكوادور'; 'EE'='إستونيا'; 'EG'='مصر'; 'EH'='الصحراء الغربية'; 'ER'='إريتريا'
    'ES'='إسبانيا'; 'ET'='إثيوبيا'; 'FI'='فنلندا'; 'FJ'='فيجي'; 'FK'='جزر فوكلاند'
    'FM'='ميكرونيزيا'; 'FO'='جزر فارو'; 'FR'='فرنسا'; 'GA'='الغابون'; 'GB'='بريطانيا'
    'GD'='غرينادا'; 'GE'='جورجيا'; 'GF'='غيانا الفرنسية'; 'GG'='غيرنزي'; 'GH'='غانا'
    'GI'='جبل طارق'; 'GL'='غرينلاند'; 'GM'='غامبيا'; 'GN'='غينيا'; 'GP'='غوادلوب'
    'GQ'='غينيا الاستوائية'; 'GR'='اليونان'; 'GS'='جورجيا الجنوبية'; 'GT'='غواتيمالا'; 'GU'='غوام'
    'GW'='غينيا بيساو'; 'GY'='غيانا'; 'HK'='هونغ كونغ'; 'HN'='هندوراس'; 'HR'='كرواتيا'
    'HT'='هايتي'; 'HU'='المجر'; 'ID'='إندونيسيا'; 'IE'='إيرلندا'; 'IL'='فلسطين المحتلة'
    'IM'='جزيرة مان'; 'IN'='الهند'; 'IQ'='العراق'; 'IR'='إيران'; 'IS'='آيسلندا'
    'IT'='إيطاليا'; 'JE'='جيرزي'; 'JM'='جامايكا'; 'JO'='الأردن'; 'JP'='اليابان'
    'KE'='كينيا'; 'KG'='قيرغيزستان'; 'KH'='كمبوديا'; 'KI'='كيريباتي'; 'KM'='جزر القمر'
    'KN'='سانت كيتس ونيفيس'; 'KP'='كوريا الشمالية'; 'KR'='كوريا الجنوبية'; 'KW'='الكويت'; 'KY'='جزر كايمان'
    'KZ'='كازاخستان'; 'LA'='لاوس'; 'LB'='لبنان'; 'LC'='سانت لوسيا'; 'LI'='ليختنشتاين'
    'LK'='سريلانكا'; 'LR'='ليبيريا'; 'LS'='ليسوتو'; 'LT'='ليتوانيا'; 'LU'='لوكسمبورغ'
    'LV'='لاتفيا'; 'LY'='ليبيا'; 'MA'='المغرب'; 'MC'='موناكو'; 'MD'='مولدوفا'
    'ME'='الجبل الأسود'; 'MF'='سانت مارتن'; 'MG'='مدغشقر'; 'MH'='جزر مارشال'; 'MK'='مقدونيا'
    'ML'='مالي'; 'MM'='ميانمار'; 'MN'='منغوليا'; 'MO'='ماكاو'; 'MP'='جزر ماريانا الشمالية'
    'MQ'='مارتينيك'; 'MR'='موريتانيا'; 'MS'='مونتسرات'; 'MT'='مالطا'; 'MU'='موريشيوس'
    'MV'='المالديف'; 'MW'='مالاوي'; 'MX'='المكسيك'; 'MY'='ماليزيا'; 'MZ'='موزمبيق'
    'NA'='ناميبيا'; 'NC'='كاليدونيا الجديدة'; 'NE'='النيجر'; 'NF'='جزيرة نورفولك'; 'NG'='نيجيريا'
    'NI'='نيكاراغوا'; 'NL'='هولندا'; 'NO'='النرويج'; 'NP'='نيبال'; 'NR'='ناورو'
    'NU'='نيوي'; 'NZ'='نيوزيلندا'; 'OM'='عُمان'; 'PA'='بنما'; 'PE'='بيرو'
    'PF'='بولينيزيا الفرنسية'; 'PG'='بابوا غينيا الجديدة'; 'PH'='الفلبين'; 'PK'='باكستان'; 'PL'='بولندا'
    'PM'='سان بيير وميكلون'; 'PN'='جزر بيتكيرن'; 'PR'='بورتوريكو'; 'PS'='فلسطين'; 'PT'='البرتغال'
    'PW'='بالاو'; 'PY'='باراغواي'; 'QA'='قطر'; 'RE'='ريونيون'; 'RO'='رومانيا'
    'RS'='صربيا'; 'RU'='روسيا'; 'RW'='رواندا'; 'SA'='السعودية'; 'SB'='جزر سليمان'
    'SC'='سيشل'; 'SD'='السودان'; 'SE'='السويد'; 'SG'='سنغافورة'; 'SH'='سانت هيلينا'
    'SI'='سلوفينيا'; 'SJ'='سفالبارد'; 'SK'='سلوفاكيا'; 'SL'='سيراليون'; 'SM'='سان مارينو'
    'SN'='السنغال'; 'SO'='الصومال'; 'SR'='سورينام'; 'SS'='جنوب السودان'; 'ST'='ساو تومي وبرينسيب'
    'SV'='السلفادور'; 'SX'='سينت مارتن'; 'SY'='سوريا'; 'SZ'='إسواتيني'; 'TC'='جزر توركس وكايكوس'
    'TD'='تشاد'; 'TF'='الأراضي الفرنسية الجنوبية'; 'TG'='توغو'; 'TH'='تايلاند'; 'TJ'='طاجيكستان'
    'TL'='تيمور الشرقية'; 'TM'='تركمانستان'; 'TN'='تونس'; 'TO'='تونغا'; 'TR'='تركيا'
    'TT'='ترينيداد وتوباغو'; 'TV'='توفالو'; 'TW'='تايوان'; 'TZ'='تنزانيا'; 'UA'='أوكرانيا'
    'UG'='أوغندا'; 'US'='الولايات المتحدة'; 'UY'='أوروغواي'; 'UZ'='أوزبكستان'; 'VA'='الفاتيكان'
    'VC'='سانت فينسنت'; 'VE'='فنزويلا'; 'VG'='جزر فيرجن البريطانية'; 'VI'='جزر فيرجن الأمريكية'
    'VN'='فيتنام'; 'VU'='فانواتو'; 'WF'='واليس وفوتونا'; 'WS'='ساموا'; 'XK'='كوسوفو'
    'YE'='اليمن'; 'YT'='مايوت'; 'ZA'='جنوب أفريقيا'; 'ZM'='زامبيا'; 'ZW'='زيمبابوي'
}

$arabicOnly = [regex]'^[\u0600-\u06FF\u0640\u0670\s\-\.\u0027\u2019]+$'
$hasArabic = [regex]'[\u0600-\u06FF]'

# أحرف كردية/فارسية/أردية لا تُستخدم في العربية الفصحى.
# GeoNames يخلط بينها وبين العربية في البدائل؛ نقدّم الاسم العربي النظيف.
$nonArabicScript = [regex]'[ەێۆڕڵڤپچژگکیہٹڈڑںے]'

# «كة» الملتصقة بحرف تالٍ خطأ إملائي شائع في GeoNames (كةربالا بدل كربلاء).
$suspiciousArabic = [regex]'كة[\u0600-\u06FF]'

# تصحيحات أسماء عربية مشوّهة — المفتاح: معرّف GeoNames.
# تُطبَّق بعد اختيار الاسم من البدائل، فتبقى ثابتة مهما تغيّرت قائمة البدائل.
$nameFixesById = @{
    '94824'    = 'كربلاء'          # Karbala — كانت «كةربالا»
    '98717'    = 'الرمادي'         # Ramadi — كانت «الرمادى»
    '98530'    = 'السماوة'         # As Samawah — كانت «السماوه»
    '13631407' = 'أبو الخصيب'      # Abu al-Khasib
    '447022'   = 'حاجياوة'         # Hajiawa
    '13680586' = 'شيلاديز'         # Sheladiz
    '99759'    = 'الدجيل'          # Ad Dujayl
    '99439'    = 'الفهود'          # Nahiyat al Fuhud
    '12494059' = 'الزوية'          # AlZwya
    '92511'    = 'قلعة سكر'        # Qal'at Sukkar — كانت «قلعة سکر»
    '91696'    = 'صلاح الدين'      # Pirmam / Salahaddin — كانت «سەلاحەدين»
    '445694'   = 'عقرة'            # Akre — كانت «بيوك»
    '94298'    = 'كفري'            # Kifri — كانت «كفرى»
    '9253107'  = 'ديرلوك'          # Dereluk — كانت «دیرلوك منطقه نهیلي»
    '387345'   = 'أبو غرق'         # Abu Gharaq — كانت «ابي غرق»
    '446208'   = 'كسنزان'          # Kasnazan — كانت «كستران ستي اسماعيل»
    '290187'   = 'مدينة عيسى'      # Madinat Isa (البحرين)
    '166716'   = 'مخيم اليرموك'    # مخيم اليرموك (سوريا)
    '281577'   = 'طولكرم'          # Tulkarm (فلسطين)
    '361058'   = 'الإسكندرية'      # Alexandria (مصر)
    '2472431'  = 'بنقردان'         # Ben Gardane (تونس)
    '2490098'  = 'معسكر'           # Mascara (الجزائر)
    '368277'   = 'ربك'             # Rabak (السودان)
    '364933'   = 'أم كدادة'        # Umm Kaddadah (السودان)
    '54225'    = 'مركة'            # Marka (الصومال)
    '49636'    = 'يغوري'           # Yagoori (الصومال)
    '56505'    = 'أسكوشوبان'       # Iskushuban (الصومال)
}

# مدن كبرى بقي اسمها لاتينيًا لعدم وجود بديل عربي في GeoNames.
# المفتاح: رمز الدولة + الاسم المختار حاليًا.
$nameFixes = @{
    'SA|Sulţānah'                  = 'السلطانة'
    'SA|Hafar Al-Batin'            = 'حفر الباطن'
    'SA|Unaizah'                   = 'عنيزة'
    'AE|Business Bay'              = 'الخليج التجاري'
    'AE|Dubai Investments Park'    = 'مجمع دبي للاستثمار'
    'AE|International City'        = 'المدينة الدولية'
    'AE|Al Majaz'                  = 'المجاز'
    'QA|Lusail'                    = 'لوسيل'
    'OM|Şaḩam'                     = 'صحم'
    "LB|Ra’s Bayrūt"               = 'رأس بيروت'
    'EG|Madīnat an Naşr'           = 'مدينة نصر'
    'EG|Al Khuşūş'                 = 'الخصوص'
    'EG|Kom Ombo'                  = 'كوم أمبو'
    'EG|6th of October City'       = 'مدينة السادس من أكتوبر'
    'EG|Al Ḩawāmidīyah'            = 'الحوامدية'
    'EG|Sinnūris'                  = 'سنورس'
    'LY|Qaşr Bin Ghashīr'          = 'قصر بن غشير'
    "LY|Tājūrā’"                   = 'تاجوراء'
    'TN|Sīdī Ḥusayn'               = 'سيدي حسين'
    'TN|Sakiet ed Daier'           = 'ساقية الدائر'
    'TN|El Mourouj'                = 'المروج'
    'TN|Sakiet ez Zit'             = 'ساقية الزيت'
    'DZ|El Eulma'                  = 'العلمة'
    'DZ|Aïn Beïda'                 = 'عين البيضاء'
    'MA|Temara'                    = 'تمارة'
    'MA|Salé Al Jadida'            = 'سلا الجديدة'
    'MA|Ait Melloul'               = 'أيت ملول'
    'MA|Dar Bouazza'               = 'دار بوعزة'
    'MA|Fès al Bali'               = 'فاس البالي'
    'MA|Berrechid'                 = 'برشيد'
    'MA|Khemisset'                 = 'الخميسات'
    'MA|Inezgane'                  = 'إنزكان'
    'MA|Ksar El Kebir'             = 'القصر الكبير'
    'MA|Taourirt'                  = 'تاوريرت'
    'MA|Bouskoura'                 = 'بوسكورة'
    'MA|Al Fqih Ben Çalah'         = 'الفقيه بن صالح'
    'MA|Dchira El Jihadia'         = 'الدشيرة الجهادية'
    'MA|El Kelaa des Srarhna'      = 'قلعة السراغنة'
    'MA|Sidi Slimane'              = 'سيدي سليمان'
    'SD|El Geneina Fort'           = 'الجنينة'
    'SD|Al Manāqil'                = 'المناقل'
    'SO|El Dibir'                  = 'الديبير'
    'SO|Ruqi'                      = 'روقي'
    'SO|Cabudwaaq'                 = 'عبد الواق'
}

# صفوف مكررة/غير مناسبة من GeoNames (نفس المدينة بمعرّفين).
$dropIds = @('10303650', '98822')   # Simele مكرر، وAqrah مركز مكرر

# أسماء المحافظات العراقية بالعربية (GeoNames يستخدم أسماء إنجليزية).
$admin1Ar = @{
    'IQ.01' = 'الأنبار';     'IQ.02' = 'البصرة';      'IQ.03' = 'المثنى'
    'IQ.04' = 'القادسية';    'IQ.05' = 'السليمانية';  'IQ.06' = 'بابل'
    'IQ.07' = 'بغداد';       'IQ.08' = 'دهوك';        'IQ.09' = 'ذي قار'
    'IQ.10' = 'ديالى';       'IQ.11' = 'أربيل';       'IQ.12' = 'كربلاء'
    'IQ.13' = 'كركوك';       'IQ.14' = 'ميسان';       'IQ.15' = 'نينوى'
    'IQ.16' = 'واسط';        'IQ.17' = 'النجف';       'IQ.18' = 'صلاح الدين'
    'IQ.19' = 'حلبجة'
}

function Get-ArabicName([string]$alts) {
    if ([string]::IsNullOrWhiteSpace($alts)) { return $null }
    $firstAny = $null      # أول بديل مقبول حتى لو خطه غير عربي
    $firstClean = $null    # أول بديل عربي نظيف
    $hehEnd = $null        # بديل ينتهي بـ«ه» (قد يكون «ة» الصحيحة)
    foreach ($token in ($alts -split ',')) {
        $t = $token.Trim()
        if ($t.Length -lt 2 -or $t.Length -gt 40) { continue }
        if (-not $hasArabic.IsMatch($t)) { continue }
        if (-not $arabicOnly.IsMatch($t)) { continue }
        if ($nonArabicScript.IsMatch($t) -or $suspiciousArabic.IsMatch($t)) {
            if ($null -eq $firstAny) { $firstAny = $t }
            continue
        }
        if ($null -eq $firstClean) { $firstClean = $t }
        # التاء المربوطة هي الصيغة الأرجح لأسماء المدن العربية.
        if ($t.EndsWith([string][char]0x0629)) { return $t }
        if ($null -eq $hehEnd -and $t.EndsWith([string][char]0x0647)) { $hehEnd = $t }
    }
    if ($null -ne $hehEnd) { return $hehEnd }
    if ($null -ne $firstClean) { return $firstClean }
    return $firstAny
}

# ---------- أسماء المحافظات/الولايات ----------
$admin1Names = @{}
if (Test-Path $Admin1Source) {
    Write-Host "قراءة المحافظات: $Admin1Source"
    Import-Csv -Path $Admin1Source -Delimiter "`t" -Header code,name,ascii,geonameid |
        ForEach-Object { $admin1Names[$_.code] = $_.ascii }
    Write-Host ("عدد المحافظات: {0}" -f $admin1Names.Count)
} else {
    Write-Host "تنبيه: ملف المحافظات غير موجود — سيتم تخطيها"
}

$jsonEscapeNeeded = [regex]'["\\\x00-\x1F]'

function Json-Escape([string]$s) {
    if ($null -eq $s) { return '' }
    # الأغلبية الساحقة من الأسماء لا تحتاج ترميزًا — الفحص السريع يوفّر وقتًا هائلًا.
    if (-not $jsonEscapeNeeded.IsMatch($s)) { return $s }
    $sb = New-Object System.Text.StringBuilder($s.Length + 8)
    foreach ($ch in $s.ToCharArray()) {
        switch ($ch) {
            '"' { [void]$sb.Append('\"') }
            '\' { [void]$sb.Append('\\') }
            "`n" { [void]$sb.Append('\n') }
            "`r" { [void]$sb.Append('\r') }
            "`t" { [void]$sb.Append('\t') }
            default {
                if ([int]$ch -lt 32) { [void]$sb.Append(('\u{0:x4}' -f [int]$ch)) }
                else { [void]$sb.Append($ch) }
            }
        }
    }
    return $sb.ToString()
}

Write-Host "قراءة المدن: $Source"
$rows = Import-Csv -Path $Source -Delimiter "`t" -Header id,name,ascii,alts,lat,lon,fclass,fcode,cc,cc2,adm1,adm2,adm3,adm4,pop,elev,dem,tz,mod

Write-Host ("إجمالي المدن في المصدر: {0}" -f $rows.Count)

$seen = @{}
$provinceAnchor = @{}   # admin1 code -> (pop, lat, lon, tz)
$list = New-Object System.Collections.Generic.List[object]

# ترتيب حسب السكان لتكون أهم المدن أولًا
foreach ($r in ($rows | Sort-Object { [long]$_.pop } -Descending)) {
    $cc = $r.cc
    if ($dropIds -contains $r.id) { continue }
    $country = $countryAr[$cc]
    if (-not $country) { $country = $cc }
    $ar = Get-ArabicName $r.alts
    if (-not $ar) { $ar = $r.name }
    if ($nameFixesById.ContainsKey($r.id)) {
        $ar = $nameFixesById[$r.id]
    } else {
        $fixKey = "$cc|$ar"
        if ($nameFixes.ContainsKey($fixKey)) { $ar = $nameFixes[$fixKey] }
    }
    $regionKey = "$cc.$($r.adm1)"
    $region = if ($r.adm1 -and $admin1Names.ContainsKey($regionKey)) { $admin1Names[$regionKey] } else { "" }
    if ($admin1Ar.ContainsKey($regionKey)) { $region = $admin1Ar[$regionKey] }
    if ($r.adm1) {
        $key1 = $regionKey
        $pop = [long]$r.pop
        if (-not $provinceAnchor.ContainsKey($key1) -or $provinceAnchor[$key1].p -lt $pop) {
            $provinceAnchor[$key1] = [pscustomobject]@{
                p = $pop; la = [double]$r.lat; lo = [double]$r.lon; tz = $r.tz; n = $region; c = $country
            }
        }
    }
    $id = $r.id
    if ($seen.ContainsKey($id)) { continue }
    $seen[$id] = 1
    $list.Add([pscustomobject]@{
        n = $ar; c = $country; r = $region
        la = [math]::Round([double]$r.lat, 4); lo = [math]::Round([double]$r.lon, 4)
        t = $r.tz; p = [long]$r.pop; k = 0
    })
}

Write-Host ("عدد المدن بعد إزالة التكرار: {0}" -f $list.Count)

# ---------- إضافة المحافظات/الولايات كمداخل مستقلة ----------
if (Test-Path $Admin1Source) {
    $provinceCount = 0
    foreach ($code in $admin1Names.Keys) {
        $name = $admin1Names[$code]
        if ([string]::IsNullOrWhiteSpace($name)) { continue }
        if ($admin1Ar.ContainsKey($code)) { $name = $admin1Ar[$code] }
        $anchor = $provinceAnchor[$code]
        if ($null -eq $anchor) { continue }
        $list.Add([pscustomobject]@{
            n = $name; c = $anchor.c; r = $anchor.n
            la = [math]::Round($anchor.la, 4); lo = [math]::Round($anchor.lo, 4)
            t = $anchor.tz; p = 0; k = 1
        })
        $provinceCount++
    }
    Write-Host ("عدد المحافظات المضافة: {0}" -f $provinceCount)
}

Write-Host ("الإجمالي: {0}" -f $list.Count)

# ---------- كتابة JSON مضغوط بدون ConvertTo-Json (أسرع وأخف) ----------
$dir = Split-Path -Parent $OutFile
if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Force -Path $dir | Out-Null }

$utf8 = New-Object System.Text.UTF8Encoding($false)
$writer = New-Object System.IO.StreamWriter($OutFile, $false, $utf8, 1048576)
try {
    $writer.Write('[')
    $first = $true
    foreach ($e in $list) {
        if (-not $first) { $writer.Write(',') }
        $first = $false
        $writer.Write('{"n":"')
        $writer.Write((Json-Escape $e.n))
        $writer.Write('","c":"')
        $writer.Write((Json-Escape $e.c))
        $writer.Write('","r":"')
        $writer.Write((Json-Escape $e.r))
        $writer.Write('","la":')
        $writer.Write($e.la.ToString([System.Globalization.CultureInfo]::InvariantCulture))
        $writer.Write(',"lo":')
        $writer.Write($e.lo.ToString([System.Globalization.CultureInfo]::InvariantCulture))
        $writer.Write(',"t":"')
        $writer.Write((Json-Escape $e.t))
        $writer.Write('","p":')
        $writer.Write($e.p)
        $writer.Write(',"k":')
        $writer.Write($e.k)
        $writer.Write('}')
    }
    $writer.Write(']')
} finally {
    $writer.Dispose()
}

$fi = Get-Item $OutFile
Write-Host ("تم إنشاء: {0} ({1} KB)" -f $fi.FullName, [math]::Round($fi.Length / 1KB, 0))
Write-Host "المدن = k:0 ، المحافظات/الولايات = k:1"
