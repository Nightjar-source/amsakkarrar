"""إرسال APK + روابط الإصدار إلى «الرسائل المحفوظة» في حسابك الشخصي.

الاستخدام:
   python telegram_send.py <apk_path> <release_url> <download_url>

يقرأ الإعدادات والجلسة من %USERPROFILE%\\.amsakkarrar-telegram\\
(خارج المستودع) — لا تُرفع الأسرار إلى Git.
"""
import asyncio
import json
import os
import sys
from pathlib import Path

from telethon import TelegramClient

CONF_DIR = Path(os.environ.get("AMS_TELEGRAM_DIR", Path.home() / ".amsakkarrar-telegram"))
CONF_FILE = CONF_DIR / "config.json"
SESSION = CONF_DIR / "user"


async def main() -> None:
    if len(sys.argv) < 3:
        print("USAGE: telegram_send.py <apk> <release_url> [download_url]")
        sys.exit(2)

    apk = Path(sys.argv[1])
    release_url = sys.argv[2]
    download_url = sys.argv[3] if len(sys.argv) > 3 else ""

    if not apk.exists():
        print(f"APK_MISSING:{apk}")
        sys.exit(3)

    cfg = json.loads(CONF_FILE.read_text(encoding="utf-8"))
    target = cfg.get("target", "me")

    client = TelegramClient(str(SESSION), cfg["api_id"], cfg["api_hash"])
    await client.connect()
    if not await client.is_user_authorized():
        print("NOT_AUTHORIZED")
        await client.disconnect()
        sys.exit(4)

    size_mb = apk.stat().st_size / (1024 * 1024)
    caption = (
        "🌙 إمساكية كرار — إصدار جديد\n"
        f"📦 {apk.name} ({size_mb:.2f} MB)\n"
        "🔏 APK موقّع وجاهز للتثبيت"
    )

    await client.send_file(target, str(apk), caption=caption, force_document=True)

    lines = ["🔗 روابط الإصدار:", release_url]
    if download_url:
        lines.append(download_url)
    await client.send_message(target, "\n".join(lines))

    print(f"SENT:{apk.name}:{size_mb:.2f}MB")
    await client.disconnect()


asyncio.run(main())
