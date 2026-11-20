package com.karrar.amsakiah.core.state

/** نوع التنبيه العائم — تستخدمه الواجهة لاختيار اللون والأيقونة والحركة. */
enum class ToastKind { INFO, ERROR, SUCCESS, LOCATION }

/**
 * تنبيه واحد عائم — نموذج بيانات فقط، بلا أي رسم/Compose.
 * يوفَّر هنا ضمن النواة حتى تتمكن من حساب محتوى رسالة الخطأ في أي منصة،
 * دون أي اتصال بالواجهة.
 */
data class ToastMessage(
    val id: Long,
    val text: String,
    val kind: ToastKind,
    val actionLabel: String? = null
)
