package com.leona.controlepagamentos.domain

import com.leona.controlepagamentos.data.model.InstallmentGroupEntity
import com.leona.controlepagamentos.domain.installment.InstallmentService
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class InstallmentServiceTest {
    private val service = InstallmentService()

    @Test
    fun createsMaterializedInstallments() {
        val payments = service.createPayments(
            group = InstallmentGroupEntity(
                id = "group-1",
                title = "Ingresso",
                totalAmountInCents = 90000,
                totalInstallments = 3,
                categoryId = "lazer",
                firstDueDate = LocalDate.of(2026, 5, 10),
                paymentMethod = null,
                notes = null,
                createdAt = LocalDateTime.parse("2026-05-01T10:00:00")
            ),
            now = LocalDateTime.parse("2026-05-01T10:00:00")
        )

        assertEquals(3, payments.size)
        assertEquals(30000L, payments[0].amountInCents)
        assertEquals(LocalDate.of(2026, 6, 10), payments[1].dueDate)
        assertEquals("Ingresso 3/3", payments[2].title)
    }

    @Test
    fun distributesRemainderCents() {
        val payments = service.createPayments(
            group = InstallmentGroupEntity(
                id = "group-1",
                title = "Compra",
                totalAmountInCents = 10000,
                totalInstallments = 3,
                categoryId = null,
                firstDueDate = LocalDate.of(2026, 5, 10),
                paymentMethod = null,
                notes = null,
                createdAt = LocalDateTime.parse("2026-05-01T10:00:00")
            ),
            now = LocalDateTime.parse("2026-05-01T10:00:00")
        )

        assertEquals(listOf(3334L, 3333L, 3333L), payments.map { it.amountInCents })
    }
}
