package com.leona.controlepagamentos.domain

import com.leona.controlepagamentos.data.model.RecurringPaymentRuleEntity
import com.leona.controlepagamentos.domain.recurrence.RecurrenceService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecurrenceServiceTest {
    private val service = RecurrenceService()
    private val now = LocalDateTime.parse("2026-05-29T10:00:00")

    @Test
    fun createsOccurrenceForSelectedMonth() {
        val occurrence = service.occurrenceForMonth(
            rule = rule(dayOfMonth = 10),
            month = YearMonth.of(2026, 6)
        )

        assertEquals(LocalDate.of(2026, 6, 10), occurrence?.dueDate)
        assertEquals(14000L, occurrence?.amountInCents)
    }

    @Test
    fun clampsDayToLastDayOfShortMonth() {
        val occurrence = service.occurrenceForMonth(
            rule = rule(dayOfMonth = 31),
            month = YearMonth.of(2026, 2)
        )

        assertEquals(LocalDate.of(2026, 2, 28), occurrence?.dueDate)
    }

    @Test
    fun skipsInactiveRule() {
        val occurrence = service.occurrenceForMonth(
            rule = rule(dayOfMonth = 10, isActive = false),
            month = YearMonth.of(2026, 6)
        )

        assertNull(occurrence)
    }

    private fun rule(dayOfMonth: Int, isActive: Boolean = true) = RecurringPaymentRuleEntity(
        id = "rule-1",
        title = "Academia",
        amountInCents = 14000,
        categoryId = "saude",
        dayOfMonth = dayOfMonth,
        startDate = LocalDate.of(2026, 1, 1),
        endDate = null,
        paymentMethod = null,
        notes = null,
        isActive = isActive,
        createdAt = now,
        updatedAt = now
    )
}
