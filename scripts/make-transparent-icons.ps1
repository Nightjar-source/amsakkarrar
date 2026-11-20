# ============================================================================
#  إزالة الخلفية السوداء المربعة من أيقونة شاشة البداية
#  - يحوّل الصورة إلى 32bpp ARGB أولاً حتى لا تُفقد قناة الشفافية عند الحفظ
#  - يعبّئ الخلفية السوداء المتصلة بالحواف فقط (flood fill)
#  - يحذف النقاط الصغيرة الشاردة التي توسّع حدود القصّ
#  - يقصّ الشعار على حدوده الفعلية → أيقونة غير مربّعة بخلفية شفافة
#  الاستخدام: powershell -File scripts/make-transparent-icons.ps1
# ============================================================================
param(
    [string]$Source = "",
    [string]$ResDir = "",
    [string]$ComposeDir = ""
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

$root = Split-Path -Parent $PSScriptRoot
if (-not $Source) { $Source = Join-Path $root 'assets\images\icon.png' }
if (-not $ResDir) { $ResDir = Join-Path $root 'androidApp\src\main\res' }
if (-not $ComposeDir) { $ComposeDir = Join-Path $root 'shared\src\commonMain\composeResources\drawable' }
if (-not (Test-Path $Source)) { throw "الأيقونة غير موجودة: $Source" }

Add-Type -ReferencedAssemblies @([System.Drawing.Bitmap].Assembly.Location) -TypeDefinition @"
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.Runtime.InteropServices;

public static class TransparentIcon {
    public static void Make(string srcPath, string dstPath, int size, double fill) {
        // 1) نسخ الصورة إلى 32bpp ARGB — LockBits على صورة 24bpp يفقد قناة alpha
        //    عند UnlockBits لأن الصيغة الأصلية لا تحتوي alpha.
        using (var original = new Bitmap(srcPath))
        using (var src = new Bitmap(original.Width, original.Height, PixelFormat.Format32bppArgb)) {
            using (var gg = Graphics.FromImage(src)) {
                gg.Clear(Color.Transparent);
                gg.DrawImage(original, 0, 0, original.Width, original.Height);
            }

            int w = src.Width, h = src.Height;
            var rect = new Rectangle(0, 0, w, h);
            var data = src.LockBits(rect, ImageLockMode.ReadWrite, PixelFormat.Format32bppArgb);
            int stride = data.Stride;
            byte[] buf = new byte[stride * h];
            Marshal.Copy(data.Scan0, buf, 0, buf.Length);

            // 2) تعبئة الخلفية السوداء المتصلة بالحواف
            int threshold = 30;
            bool[] bg = new bool[w * h];
            var stack = new Stack<int>();
            Action<int, int> push = (x, y) => {
                if (x < 0 || y < 0 || x >= w || y >= h) return;
                int p = y * w + x;
                if (bg[p]) return;
                int i = y * stride + x * 4;
                int b = buf[i], g = buf[i + 1], r = buf[i + 2];
                if (Math.Max(r, Math.Max(g, b)) > threshold) return;
                bg[p] = true;
                stack.Push(p);
            };

            for (int x = 0; x < w; x++) { push(x, 0); push(x, h - 1); }
            for (int y = 0; y < h; y++) { push(0, y); push(w - 1, y); }
            while (stack.Count > 0) {
                int p = stack.Pop();
                int x = p % w, y = p / w;
                push(x + 1, y); push(x - 1, y); push(x, y + 1); push(x, y - 1);
            }

            for (int y = 0; y < h; y++) {
                int row = y * stride;
                for (int x = 0; x < w; x++) {
                    if (bg[y * w + x]) buf[row + x * 4 + 3] = 0;
                }
            }

            // 3) حذف المكوّنات الصغيرة (نقاط شاردة) قبل حساب حدود القصّ.
            //    نُبقي الشعار الكبير والهلال والتفاصيل المتصلة به.
            bool[] seen = new bool[w * h];
            var component = new List<int>();
            var work = new Stack<int>();
            int minKeep = 300;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int p0 = y * w + x;
                    if (seen[p0]) continue;
                    if (buf[(y * stride) + x * 4 + 3] == 0) { seen[p0] = true; continue; }
                    component.Clear();
                    work.Clear();
                    work.Push(p0);
                    seen[p0] = true;
                    while (work.Count > 0) {
                        int p = work.Pop();
                        component.Add(p);
                        int px = p % w, py = p / w;
                        Action<int, int> visit = (nx, ny) => {
                            if (nx < 0 || ny < 0 || nx >= w || ny >= h) return;
                            int np = ny * w + nx;
                            if (seen[np]) return;
                            if (buf[(ny * stride) + nx * 4 + 3] == 0) { seen[np] = true; return; }
                            seen[np] = true;
                            work.Push(np);
                        };
                        visit(px + 1, py); visit(px - 1, py); visit(px, py + 1); visit(px, py - 1);
                    }
                    if (component.Count < minKeep) {
                        foreach (int p in component) buf[(p / w) * stride + (p % w) * 4 + 3] = 0;
                    }
                }
            }

            Marshal.Copy(buf, 0, data.Scan0, buf.Length);
            src.UnlockBits(data);

            // 4) حدود الشعار الفعلية
            int minX = w, minY = h, maxX = -1, maxY = -1;
            for (int y = 0; y < h; y++) {
                int row = y * stride;
                for (int x = 0; x < w; x++) {
                    if (buf[row + x * 4 + 3] > 8) {
                        if (x < minX) minX = x;
                        if (x > maxX) maxX = x;
                        if (y < minY) minY = y;
                        if (y > maxY) maxY = y;
                    }
                }
            }
            if (maxX < minX) { minX = 0; minY = 0; maxX = w - 1; maxY = h - 1; }
            int cw = maxX - minX + 1, ch = maxY - minY + 1;

            // 5) القصّ والرسم على كانفس شفاف مربّع
            var cropRect = new Rectangle(minX, minY, cw, ch);
            using (var crop = src.Clone(cropRect, PixelFormat.Format32bppArgb))
            using (var canvas = new Bitmap(size, size, PixelFormat.Format32bppArgb))
            using (var g = Graphics.FromImage(canvas)) {
                g.Clear(Color.Transparent);
                g.InterpolationMode = System.Drawing.Drawing2D.InterpolationMode.HighQualityBicubic;
                g.SmoothingMode = System.Drawing.Drawing2D.SmoothingMode.HighQuality;
                g.PixelOffsetMode = System.Drawing.Drawing2D.PixelOffsetMode.HighQuality;
                double scale = Math.Min((double)size / cw, (double)size / ch) * fill;
                int dw = (int)Math.Round(cw * scale);
                int dh = (int)Math.Round(ch * scale);
                g.DrawImage(crop, (size - dw) / 2, (size - dh) / 2, dw, dh);
                canvas.Save(dstPath, ImageFormat.Png);
            }
        }
    }
}
"@

$splashPath = Join-Path $ResDir 'drawable\splash_icon.png'
[TransparentIcon]::Make($Source, $splashPath, 512, 0.90)
Write-Host "splash_icon.png (شفاف) -> 512x512"

$composePath = Join-Path $ComposeDir 'app_icon.png'
[TransparentIcon]::Make($Source, $composePath, 512, 0.90)
Write-Host "app_icon.png (شفاف) -> 512x512"

# أيقونة الواجهة التكيفية: شعار شفاف داخل منطقة آمنة 66%
$foregroundPath = Join-Path $ResDir 'drawable\ic_launcher_foreground.png'
[TransparentIcon]::Make($Source, $foregroundPath, 432, 0.62)
Write-Host "ic_launcher_foreground.png (شفاف) -> 432x432"

Write-Host "تم إنشاء الأيقونات الشفافة بنجاح."
