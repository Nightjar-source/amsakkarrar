"""تسجيل دخول تلغرام (MTProto) عبر Telethon — مرحلتان:
   1) python telegram_login.py request
   2) python telegram_login.py submit <CODE> [2FA_PASSWORD]

تُحفظ الجلسة والإعدادات خارج المستودع في:
   %USERPROFILE%\\.amsakkarrar-telegram\\
ولا تُرفع إلى Git إطلاقًا.
"""
import asyncio
import json
import os
import sys
from pathlib import Path

from telethon import TelegramClient
from telethon.errors import SessionPasswordNeededError

CONF_DIR = Path(os.environ.get("AMS_TELEGRAM_DIR", Path.home() / ".amsakkarrar-telegram"))
CONF_FILE = CONF_DIR / "config.json"
STATE_FILE = CONF_DIR / "login.json"
SESSION = CONF_DIR / "user"


def load_config() -> dict:
    if not CONF_FILE.exists():
        print("CONF_MISSING")
        sys.exit(2)
    return json.loads(CONF_FILE.read_text(encoding="utf-8-sig"))


async def main() -> None:
    mode = sys.argv[1] if len(sys.argv) > 1 else ""
    cfg = load_config()
    client = TelegramClient(str(SESSION), cfg["api_id"], cfg["api_hash"])
    await client.connect()

    if await client.is_user_authorized():
        me = await client.get_me()
        print(f"ALREADY_AUTHORIZED:{me.first_name or ''}:{me.username or me.id}")
        await client.disconnect()
        return

    if mode == "request":
        sent = await client.send_code_request(cfg["phone"])
        STATE_FILE.write_text(
            json.dumps(
                {"phone": cfg["phone"], "phone_code_hash": sent.phone_code_hash},
                ensure_ascii=False,
            ),
            encoding="utf-8",
        )
        print("CODE_SENT")
    elif mode == "submit":
        if len(sys.argv) < 3:
            print("CODE_REQUIRED")
            await client.disconnect()
            return
        code = sys.argv[2]
        state = json.loads(STATE_FILE.read_text(encoding="utf-8"))
        try:
            await client.sign_in(state["phone"], code, phone_code_hash=state["phone_code_hash"])
        except SessionPasswordNeededError:
            password = sys.argv[3] if len(sys.argv) > 3 else ""
            if not password:
                print("NEED_PASSWORD")
                await client.disconnect()
                return
            await client.sign_in(password=password)
        except Exception as exc:  # noqa: BLE001
            print(f"LOGIN_ERROR:{type(exc).__name__}:{exc}")
            await client.disconnect()
            return
        me = await client.get_me()
        STATE_FILE.unlink(missing_ok=True)
        print(f"OK:{me.first_name or ''}:{me.username or me.id}")
    else:
        print("UNKNOWN_MODE")

    await client.disconnect()


asyncio.run(main())
