package com.karrar.amsakiah.core.feedback

import platform.AudioToolbox.AudioServicesPlaySystemSound

actual fun playTasbihClick() {
    // 1104 = نغمة نقر خفيفة من نغمات النظام
    runCatching { AudioServicesPlaySystemSound(1104u) }
}

actual fun playTasbihComplete() {
    // 1057 = نغمة تأكيد
    runCatching { AudioServicesPlaySystemSound(1057u) }
}
