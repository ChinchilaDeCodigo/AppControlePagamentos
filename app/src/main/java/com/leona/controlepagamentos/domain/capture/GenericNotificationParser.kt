package com.leona.controlepagamentos.domain.capture

import com.leona.controlepagamentos.data.model.ParseConfidence
import com.leona.controlepagamentos.data.model.TransactionType
import java.text.Normalizer

class GenericNotificationParser : NotificationParser {
    private val amountRegex = Regex("""R\$\s?(\d{1,3}(\.\d{3})*,\d{2}|\d+,\d{2})""")
    private val merchantRegex = Regex("""(?i)(?:\bem\b|\bno\b|\bna\b|\bpara\b)\s+(.+)$""")

    override fun canParse(sourcePackage: String, text: NotificationText): Boolean =
        text.combined().isNotBlank()

    override fun parse(text: NotificationText): ParseResult {
        val combined = text.combined()
        val amount = amountRegex.find(combined)?.groupValues?.get(1)?.toCents()
        val merchant = extractMerchant(combined)
        val transactionType = detectTransactionType(combined)

        val confidence = when {
            amount != null && !merchant.isNullOrBlank() -> ParseConfidence.HIGH
            amount != null -> ParseConfidence.MEDIUM
            combined.contains("compra", ignoreCase = true) ||
                combined.contains("pix", ignoreCase = true) -> ParseConfidence.LOW
            else -> ParseConfidence.FAILED
        }

        return ParseResult(
            amountInCents = amount,
            merchant = merchant,
            transactionType = transactionType,
            confidence = if (amount == null) ParseConfidence.FAILED else confidence
        )
    }

    private fun String.toCents(): Long =
        replace(".", "")
            .replace(",", ".")
            .toBigDecimal()
            .movePointRight(2)
            .toLong()

    private fun extractMerchant(text: String): String? {
        val merchant = merchantRegex.find(text)?.groupValues?.getOrNull(1)
            ?.replace(amountRegex, "")
            ?.replace(Regex("""\s+"""), " ")
            ?.trim(' ', '.', ',', ';', ':')
        return merchant?.takeIf { it.isNotBlank() }
    }

    private fun detectTransactionType(text: String): TransactionType = when {
        text.normalized().contains("pix") -> TransactionType.PIX
        text.normalized().contains("boleto") -> TransactionType.BANK_SLIP
        text.normalized().contains("transfer") -> TransactionType.BANK_TRANSFER
        text.normalized().contains("cartao") ||
            text.normalized().contains("compra") -> TransactionType.CARD_PURCHASE
        else -> TransactionType.UNKNOWN
    }

    private fun String.normalized(): String =
        Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase()
}
