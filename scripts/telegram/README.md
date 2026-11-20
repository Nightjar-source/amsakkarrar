# أدوات تلغرام — النشر والنسخ الاحتياطي

هذه الأدوات تُستخدم لنشر إصدارات التطبيق إلى قناة تلغرام، مع حفظ جلسة الدخول
بشكل آمن خارج المستودع.

## الملفات

| الملف | الوظيفة |
|---|---|
| `telegram_login.py` | تسجيل الدخول عبر MTProto (مرحلتان: رمز + كلمة مرور 2FA إن وُجدت) |
| `telegram_send.py` | إرسال APK + روابط الإصدار إلى القناة |
| `telegram_backup.ps1` | نسخ/استرجاع/عرض نسخ جلسة الدخول المشفّرة |

## مكان الجلسة

تُحفظ الجلسة والإعدادات خارج المستودع في:

```
%USERPROFILE%\.amsakkarrar-telegram\
  config.json      # api_id / api_hash / target (سري)
  user.session     # جلسة Telethon (سرية)
```

> لا تُرفع `config.json` أو `user.session` أو النسخ الاحتياطية إلى Git إطلاقًا.

## النسخ الاحتياطي المشفّر

النسخ تُشفّر بواسطة **Windows DPAPI** (نطاق `CurrentUser`)، أي أنها قابلة
للفك فقط بنفس مستخدم ويندوز على نفس الجهاز. مكانها الافتراضي:

```
%USERPROFILE%\.amsakkarrar-telegram\backups\telegram-login-YYYYMMDD-HHMMSS.dpapi
```

### إنشاء نسخة

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/telegram/telegram_backup.ps1 backup
```

### عرض النسخ

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/telegram/telegram_backup.ps1 list
```

### استرجاع آخر نسخة

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/telegram/telegram_backup.ps1 restore
```

### استرجاع نسخة محددة

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/telegram/telegram_backup.ps1 restore -File "C:\path\to\telegram-login-....dpapi"
```

- يتطلب الاسترجاع `-Force` إذا كانت هناك جلسة موجودة مسبقًا.
- لتجربة الاسترجاع في مجلد مؤقت بدل استبدال الجلسة:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/telegram/telegram_backup.ps1 restore -Destination "$env:TEMP\tg-test"
```

## ملاحظات أمنية

- النسخة مشفّرة ومرتبطة بمستخدم ويندوز الحالي؛ لا يمكن فكها على جهاز آخر.
- لنقل النشر إلى جهاز/CI آخر، سجّل الدخول من جديد هناك ولا تنسخ الجلسة.
- يمكن حذف النسخ القديمة يدويًا من مجلد `backups` عند الحاجة.
