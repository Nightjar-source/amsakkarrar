# ============================================================================
#  توليد أيقونات التطبيق من assets/images/icon.png
#  الاستخدام: powershell -File scripts/generate-icons.ps1
# ============================================================================
param(
    [string]$Source = "",
    [string]$ResDir = ""
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

$root = Split-Path -Parent $PSScriptRoot
if (-not $Source) { $Source = Join-Path $root 'assets\images\icon.png' }
if (-not $ResDir) { $ResDir = Join-Path $root 'androidApp\src\main\res' }
if (-not (Test-Path $Source)) { throw "الأيقونة غير موجودة: $Source" }

$src = [System.Drawing.Image]::FromFile($Source)

function New-Resized([System.Drawing.Image]$image, [int]$size, [bool]$circle) {
    $bmp = New-Object System.Drawing.Bitmap($size, $size)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.Clear([System.Drawing.Color]::Transparent)

    if ($circle) {
        $path = New-Object System.Drawing.Drawing2D.GraphicsPath
        $path.AddEllipse(0, 0, $size, $size)
        $g.SetClip($path)
    }

    $g.DrawImage($image, 0, 0, $size, $size)
    $g.Dispose()
    return $bmp
}

$densities = @{ 'mdpi' = 48; 'hdpi' = 72; 'xhdpi' = 96; 'xxhdpi' = 144; 'xxxhdpi' = 192 }

foreach ($d in $densities.Keys) {
    $size = $densities[$d]
    $dir = Join-Path $ResDir "mipmap-$d"
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Force -Path $dir | Out-Null }

    $square = New-Resized $src $size $false
    $square.Save((Join-Path $dir 'ic_launcher.png'), [System.Drawing.Imaging.ImageFormat]::Png)
    $square.Dispose()

    $round = New-Resized $src $size $true
    $round.Save((Join-Path $dir 'ic_launcher_round.png'), [System.Drawing.Imaging.ImageFormat]::Png)
    $round.Dispose()
    Write-Host "mipmap-$d : $size px"
}

# صورة خلفية الأيقونة التكيفية (108dp @ xxxhdpi)
$drawable = Join-Path $ResDir 'drawable'
if (-not (Test-Path $drawable)) { New-Item -ItemType Directory -Force -Path $drawable | Out-Null }

$adaptiveBg = New-Resized $src 432 $false
$adaptiveBg.Save((Join-Path $drawable 'ic_launcher_background_image.png'), [System.Drawing.Imaging.ImageFormat]::Png)
$adaptiveBg.Dispose()

# أيقونة شاشة البداية (Android 12 Splash)
$splash = New-Resized $src 768 $false
$splash.Save((Join-Path $drawable 'splash_icon.png'), [System.Drawing.Imaging.ImageFormat]::Png)
$splash.Dispose()

# نسخة للواجهة المشتركة (شاشة البداية داخل Compose)
$composeDir = Join-Path $root 'shared\src\commonMain\composeResources\drawable'
if (-not (Test-Path $composeDir)) { New-Item -ItemType Directory -Force -Path $composeDir | Out-Null }
$uiIcon = New-Resized $src 512 $false
$uiIcon.Save((Join-Path $composeDir 'app_icon.png'), [System.Drawing.Imaging.ImageFormat]::Png)
$uiIcon.Dispose()

$src.Dispose()
Write-Host "تم توليد كل الأيقونات بنجاح."
