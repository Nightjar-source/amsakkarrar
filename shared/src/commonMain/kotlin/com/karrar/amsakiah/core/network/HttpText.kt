package com.karrar.amsakiah.core.network

/**
 * طلب HTTP GET نصي بسيط لاستشعار الموقع عبر IP (كما في تطبيق الكفيل).
 *
 * - لا يُستخدم إلا عند ضغط المستخدم على «عبر IP».
 * - مهلة قصيرة، ولا يؤثر على عمل التطبيق الأوفلاين إذا فشل.
 */
expect suspend fun httpGetText(url: String, timeoutMillis: Int = 8_000): String?
