package com.leona.controlepagamentos.domain.capture

import com.leona.controlepagamentos.data.model.ParseConfidence
import com.leona.controlepagamentos.data.model.TransactionType

interface NotificationParser {
    fun canParse(sourcePackage: String, text: NotificationText): Boolean
    fun parse(text: NotificationText): ParseResult
}

data class ParseResult(
    val amountInCents: Long?,
    val merchant: String?,
    val transactionType: TransactionType?,
    val confidence: ParseConfidence
)
