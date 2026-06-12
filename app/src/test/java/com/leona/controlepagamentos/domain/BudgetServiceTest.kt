package com.leona.controlepagamentos.domain

import com.leona.controlepagamentos.data.model.BudgetEntity
import com.leona.controlepagamentos.data.model.PaymentEntity
import com.leona.controlepagamentos.data.model.PaymentSource
import com.leona.controlepagamentos.data.model.PaymentStatus
import com.leona.controlepagamentos.data.model.PaymentType
import com.leona.controlepagamentos.domain.budget.BudgetHealth
import com.leona.controlepagamentos.domain.budget.BudgetService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetServiceTest {
    private val service = BudgetService()
    private val now = LocalDateTime.parse("2026-06-12T10:00:00")

    @Test
    fun calculatesGeneralBudgetProgress() {
        val progress = service.buildProgress(
            budgets = listOf(budget(categoryId = null, limit = 100000)),
            payments = listOf(payment(25000, "alimentacao"), payment(35000, "transporte")),
            categoryNames = mapOf("alimentacao" to "Alimentacao", "transporte" to "Transporte")
        ).single()

        assertEquals(60000L, progress.spentInCents)
        assertEquals(40000L, progress.remainingInCents)
        assertEquals(60, progress.percentUsed)
        assertEquals(BudgetHealth.HEALTHY, progress.health)
    }

    @Test
    fun calculatesCategoryBudgetProgress() {
        val progress = service.buildProgress(
            budgets = listOf(budget(categoryId = "alimentacao", limit = 50000)),
            payments = listOf(payment(25000, "alimentacao"), payment(35000, "transporte")),
            categoryNames = mapOf("alimentacao" to "Alimentacao", "transporte" to "Transporte")
        ).single()

        assertEquals(25000L, progress.spentInCents)
        assertEquals("Alimentacao", progress.categoryName)
        assertEquals(50, progress.percentUsed)
    }

    @Test
    fun marksBudgetAsExceeded() {
        val progress = service.buildProgress(
            budgets = listOf(budget(categoryId = null, limit = 10000)),
            payments = listOf(payment(12000, "alimentacao")),
            categoryNames = emptyMap()
        ).single()

        assertEquals(-2000L, progress.remainingInCents)
        assertEquals(120, progress.percentUsed)
        assertEquals(BudgetHealth.EXCEEDED, progress.health)
    }

    @Test
    fun buildsSpendingInsight() {
        val insight = service.buildInsight(
            payments = listOf(
                payment(12000, "alimentacao", PaymentSource.CAPTURED_NOTIFICATION),
                payment(8000, "alimentacao", PaymentSource.MANUAL),
                payment(4000, "transporte", PaymentSource.MANUAL)
            ),
            categoryNames = mapOf("alimentacao" to "Alimentacao", "transporte" to "Transporte"),
            selectedMonth = YearMonth.of(2026, 6),
            today = LocalDate.of(2026, 6, 12)
        )

        assertEquals(24000L, insight.paidInCents)
        assertEquals(12000L, insight.capturedInCents)
        assertEquals(12000L, insight.manualInCents)
        assertEquals(2000L, insight.averageDailyInCents)
        assertEquals(60000L, insight.projectedMonthInCents)
        assertEquals("Alimentacao", insight.topCategoryName)
        assertEquals(83, insight.topCategorySharePercent)
    }

    private fun budget(categoryId: String?, limit: Long) = BudgetEntity(
        id = "budget-${categoryId ?: "general"}",
        name = categoryId ?: "Teto mensal",
        monthStart = LocalDate.of(2026, 6, 1),
        categoryId = categoryId,
        limitInCents = limit,
        alertAtPercent = 75,
        isActive = true,
        createdAt = now,
        updatedAt = now
    )

    private fun payment(
        amount: Long,
        categoryId: String?,
        source: PaymentSource = PaymentSource.MANUAL
    ) = PaymentEntity(
        id = "payment-$amount-$categoryId-$source",
        title = "Pagamento",
        amountInCents = amount,
        categoryId = categoryId,
        dueDate = LocalDate.of(2026, 6, 10),
        paidAt = now,
        status = PaymentStatus.PAID,
        type = PaymentType.SINGLE,
        paymentMethod = null,
        source = source,
        notes = null,
        capturedTransactionId = null,
        installmentGroupId = null,
        installmentNumber = null,
        totalInstallments = null,
        createdAt = now,
        updatedAt = now
    )
}
