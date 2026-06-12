package com.leona.controlepagamentos.domain

import com.leona.controlepagamentos.domain.capture.DuplicateDetectionService
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class DuplicateDetectionTest {
    private val service = DuplicateDetectionService()

    @Test
    fun createsSameHashInsideSameMinute() {
        val first = service.hash(
            sourcePackage = "com.nu.production",
            title = "Compra",
            text = "Compra aprovada de R$ 42,90 em PADARIA XPTO",
            amountInCents = 4290,
            merchant = "PADARIA XPTO",
            occurredAt = LocalDateTime.parse("2026-05-29T10:15:02")
        )
        val second = service.hash(
            sourcePackage = "com.nu.production",
            title = " Compra ",
            text = "Compra aprovada de R$ 42,90 em PADARIA XPTO",
            amountInCents = 4290,
            merchant = "padaria xpto",
            occurredAt = LocalDateTime.parse("2026-05-29T10:15:55")
        )

        assertEquals(first, second)
    }

    @Test
    fun changesHashAcrossMinutes() {
        val first = service.hash("pkg", "t", "text", 1000, "Loja", LocalDateTime.parse("2026-05-29T10:15:02"))
        val second = service.hash("pkg", "t", "text", 1000, "Loja", LocalDateTime.parse("2026-05-29T10:16:00"))

        assertNotEquals(first, second)
    }
}
