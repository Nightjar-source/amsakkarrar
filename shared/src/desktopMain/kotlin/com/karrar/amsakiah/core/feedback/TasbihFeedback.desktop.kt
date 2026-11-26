package com.karrar.amsakiah.core.feedback

import java.awt.Toolkit

actual fun playTasbihClick() {
    runCatching { Toolkit.getDefaultToolkit().beep() }
}

actual fun playTasbihComplete() {
    runCatching { Toolkit.getDefaultToolkit().beep() }
}
