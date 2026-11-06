# ============================================================================
#  توليد قاعدة المدن العالمية أوفلاين (cities.json)
#  المصدر: GeoNames cities15000 (CC BY 4.0) — https://download.geonames.org
#  الاستخدام: powershell -File scripts/build-cities-json.ps1
# ============================================================================
param(
    [string]$Source = "$env:TEMP\geonames\cities15000.txt",
    [string]$OutFile = ""
)

$ErrorActionPreference = 'Stop'

if (-not $OutFile -or $OutFile -eq '') {
    $root = Split-Path -Parent $PSScriptRoot
    $OutFile = Join-Path $root 'shared\src\commonMain\composeResources\files\cities.json'
}

if (-not (Test-Path $Source)) {
    throw "ملف المصدر غير موجود: $Source (نزّل cities15000.zip من download.geonames.org وفك ضغطه)"
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

$arabicOnly = [regex]'^[\u0600-\u06FF\u0640\u0670\s\-\.\'’]+$'
$hasArabic = [regex]'[\u0600-\u06FF]'

function Get-ArabicName([string]$alts) {
    if ([string]::IsNullOrWhiteSpace($alts)) { return $null }
    foreach ($token in ($alts -split ',')) {
        $t = $token.Trim()
        if ($t.Length -lt 2 -or $t.Length -gt 40) { continue }
        if (-not $hasArabic.IsMatch($t)) { continue }
        if (-not $arabicOnly.IsMatch($t)) { continue }
        return $t
    }
    return $null
}

Write-Host "قراءة الملف: $Source"
$rows = Import-Csv -Path $Source -Delimiter "`t" -Header id,name,ascii,alts,lat,lon,fclass,fcode,cc,cc2,adm1,adm2,adm3,adm4,pop,elev,dem,tz,mod |
    Where-Object { [long]$_.pop -ge 50000 -or $_.fcode -eq 'PPLC' }

Write-Host ("المدن المرشحة: {0}" -f $rows.Count)

$seen = @{}
$list = New-Object System.Collections.Generic.List[object]

foreach ($r in ($rows | Sort-Object { [long]$_.pop } -Descending)) {
    $cc = $r.cc
    $country = $countryAr[$cc]
    if (-not $country) { $country = $cc }
    $ar = Get-ArabicName $r.alts
    if (-not $ar) { $ar = $r.name }
    $key = "$ar|$country"
    if ($seen.ContainsKey($key)) { continue }
    $seen[$key] = 1
    $list.Add([ordered]@{
        n  = $ar
        c  = $country
        la = [math]::Round([double]$r.lat, 4)
        lo = [math]::Round([double]$r.lon, 4)
        t  = $r.tz
        p  = [long]$r.pop
    })
}

Write-Host ("عدد المدن بعد إزالة التكرار: {0}" -f $list.Count)

$json = ConvertTo-Json -InputObject $list -Compress -Depth 3
$dir = Split-Path -Parent $OutFile
if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Force -Path $dir | Out-Null }
[System.IO.File]::WriteAllText($OutFile, $json, (New-Object System.Text.UTF8Encoding($false)))

$fi = Get-Item $OutFile
Write-Host ("تم إنشاء: {0} ({1} KB)" -f $fi.FullName, [math]::Round($fi.Length / 1KB, 0))
