package com.leona.controlepagamentos.domain

import com.leona.controlepagamentos.data.model.ParseConfidence
import com.leona.controlepagamentos.data.model.TransactionType
import com.leona.controlepagamentos.domain.capture.GenericNotificationParser
import com.leona.controlepagamentos.domain.capture.NotificationText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TransactionParserTest {
    private val parser = GenericNotificationParser()

    @Test
    fun extractsAmountAndMerchantFromCardPurchase() {
        val result = parser.parse(
            NotificationText(
                title = null,
                text = "Compra aprovada de R$ 42,90 em PADARIA XPTO",
                subText = null,
                bigText = null
            )
        )

        assertEquals(4290L, result.amountInCents)
        assertEquals("PADARIA XPTO", result.merchant)
        assertEquals(TransactionType.CARD_PURCHASE, result.transactionType)
        assertEquals(ParseConfidence.HIGH, result.confidence)
    }

    @Test
    fun extractsPixTargetAsMerchant() {
        val result = parser.parse(
            NotificationText(
                title = null,
                text = "Voce fez um Pix de R$ 100,00 para JOAO SILVA",
                subText = null,
                bigText = null
            )
        )

        assertEquals(10000L, result.amountInCents)
        assertEquals("JOAO SILVA", result.merchant)
        assertEquals(TransactionType.PIX, result.transactionType)
        assertEquals(ParseConfidence.HIGH, result.confidence)
    }

    @Test
    fun failsWhenAmountIsMissing() {
        val result = parser.parse(
            NotificationText(
                title = null,
                text = "Compra no cartao final 1234",
                subText = null,
                bigText = null
            )
        )

        assertNull(result.amountInCents)
        assertEquals(ParseConfidence.FAILED, result.confidence)
    }
}
