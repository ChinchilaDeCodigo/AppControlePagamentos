package com.leona.controlepagamentos.domain.capture

data class NotificationText(
    val title: String?,
    val text: String?,
    val subText: String?,
    val bigText: String?
) {
    fun combined(): String = listOfNotNull(title, text, subText, bigText)
        .joinToString(" ")
        .replace(Regex("\\s+"), " ")
        .trim()
}
