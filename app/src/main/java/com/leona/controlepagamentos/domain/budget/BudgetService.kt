package com.leona.controlepagamentos.domain.budget

import com.leona.controlepagamentos.data.model.BudgetEntity
import com.leona.controlepagamentos.data.model.PaymentEntity
import com.leona.controlepagamentos.data.model.PaymentSource
import com.leona.controlepagamentos.data.model.PaymentStatus
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

enum class BudgetHealth {
    HEALTHY,
    ATTENTION,
    CRITICAL,
    EXCEEDED
}

data class BudgetProgress(
    val budgetId: String,
    val name: String,
    val categoryId: String?,
    val categoryName: String,
    val limitInCents: Long,
    val spentInCents: Long,
    val remainingInCents: Long,
    val percentUsed: Int,
    val alertAtPercent: Int,
    val health: BudgetHealth
)

data class SpendingInsight(
    val paidInCents: Long,
    val capturedInCents: Long,
    val manualInCents: Long,
    val averageDailyInCents: Long,
    val projectedMonthInCents: Long,
    val topCategoryName: String?,
    val topCategoryInCents: Long,
    val topCategorySharePercent: Int
)

class BudgetService {
    fun buildProgress(
        budgets: List<BudgetEntity>,
        payments: List<PaymentEntity>,
        categoryNames: Map<String, String>
    ): List<BudgetProgress> {
        val paidPayments = payments.filter { it.status == PaymentStatus.PAID }

        return budgets.map { budget ->
            val spent = paidPayments
                .filter { payment -> budget.categoryId == null || payment.categoryId == budget.categoryId }
                .sumOf { it.amountInCents }
            val percent = percent(spent, budget.limitInCents)
            BudgetProgress(
                budgetId = budget.id,
                name = budget.name,
                categoryId = budget.categoryId,
                categoryName = budget.categoryId?.let { categoryNames[it] } ?: "Geral",
                limitInCents = budget.limitInCents,
                spentInCents = spent,
                remainingInCents = budget.limitInCents - spent,
                percentUsed = percent,
                alertAtPercent = budget.alertAtPercent,
                health = healthFor(percent, budget.alertAtPercent)
            )
        }.sortedWith(compareBy<BudgetProgress> { it.categoryId != null }.thenBy { it.name })
    }

    fun buildInsight(
        payments: List<PaymentEntity>,
        categoryNames: Map<String, String>,
        selectedMonth: YearMonth,
        today: LocalDate
    ): SpendingInsight {
        val paidPayments = payments.filter { it.status == PaymentStatus.PAID }
        val total = paidPayments.sumOf { it.amountInCents }
        val captured = paidPayments
            .filter { it.source == PaymentSource.CAPTURED_NOTIFICATION }
            .sumOf { it.amountInCents }
        val manual = total - captured
        val elapsedDays = elapsedDaysForProjection(selectedMonth, today)
        val average = if (elapsedDays == 0) 0L else total / elapsedDays
        val projected = average * selectedMonth.lengthOfMonth()
        val topCategory = paidPayments
            .groupBy { it.categoryId }
            .map { (categoryId, items) ->
                val name = categoryId?.let { categoryNames[it] } ?: "Sem categoria"
                name to items.sumOf { it.amountInCents }
            }
            .maxByOrNull { it.second }

        return SpendingInsight(
            paidInCents = total,
            capturedInCents = captured,
            manualInCents = manual,
            averageDailyInCents = average,
            projectedMonthInCents = projected,
            topCategoryName = topCategory?.first,
            topCategoryInCents = topCategory?.second ?: 0L,
            topCategorySharePercent = percent(topCategory?.second ?: 0L, total)
        )
    }

    private fun elapsedDaysForProjection(selectedMonth: YearMonth, today: LocalDate): Int =
        when {
            YearMonth.from(today) == selectedMonth -> today.dayOfMonth.coerceAtLeast(1)
            selectedMonth.atEndOfMonth().isBefore(today) -> selectedMonth.lengthOfMonth()
            else -> 0
        }

    private fun percent(value: Long, limit: Long): Int {
        if (limit <= 0L) return 0
        return ((value.toDouble() / limit.toDouble()) * 100).roundToInt()
    }

    private fun healthFor(percentUsed: Int, alertAtPercent: Int): BudgetHealth = when {
        percentUsed > 100 -> BudgetHealth.EXCEEDED
        percentUsed >= 90 -> BudgetHealth.CRITICAL
        percentUsed >= alertAtPercent -> BudgetHealth.ATTENTION
        else -> BudgetHealth.HEALTHY
    }
}
