package com.leona.controlepagamentos.domain.recurrence

import com.leona.controlepagamentos.data.model.PaymentMethod
import com.leona.controlepagamentos.data.model.RecurringPaymentRuleEntity
import java.time.LocalDate
import java.time.YearMonth

data class RecurringOccurrence(
    val ruleId: String,
    val title: String,
    val amountInCents: Long,
    val categoryId: String?,
    val dueDate: LocalDate,
    val paymentMethod: PaymentMethod?,
    val notes: String?
)

class RecurrenceService {
    fun occurrenceForMonth(rule: RecurringPaymentRuleEntity, month: YearMonth): RecurringOccurrence? {
        if (!rule.isActive) return null

        val firstDay = month.atDay(1)
        val lastDay = month.atEndOfMonth()
        if (rule.startDate > lastDay) return null
        if (rule.endDate != null && rule.endDate < firstDay) return null

        val dueDay = rule.dayOfMonth.coerceIn(1, month.lengthOfMonth())
        val dueDate = month.atDay(dueDay)
        if (dueDate < rule.startDate) return null
        if (rule.endDate != null && dueDate > rule.endDate) return null

        return RecurringOccurrence(
            ruleId = rule.id,
            title = rule.title,
            amountInCents = rule.amountInCents,
            categoryId = rule.categoryId,
            dueDate = dueDate,
            paymentMethod = rule.paymentMethod,
            notes = rule.notes
        )
    }
}
