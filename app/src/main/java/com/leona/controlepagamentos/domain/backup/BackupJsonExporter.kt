package com.leona.controlepagamentos.domain.backup

import com.leona.controlepagamentos.data.model.BudgetEntity
import com.leona.controlepagamentos.data.model.CapturedTransactionEntity
import com.leona.controlepagamentos.data.model.CategoryEntity
import com.leona.controlepagamentos.data.model.InstallmentGroupEntity
import com.leona.controlepagamentos.data.model.PaymentEntity
import com.leona.controlepagamentos.data.model.RecurringPaymentRuleEntity
import java.time.LocalDateTime
import org.json.JSONArray
import org.json.JSONObject

class BackupJsonExporter {
    fun export(
        payments: List<PaymentEntity>,
        budgets: List<BudgetEntity>,
        categories: List<CategoryEntity>,
        capturedTransactions: List<CapturedTransactionEntity>,
        recurringRules: List<RecurringPaymentRuleEntity>,
        installmentGroups: List<InstallmentGroupEntity>
    ): String = JSONObject()
        .put("version", 1)
        .put("exportedAt", LocalDateTime.now().toString())
        .put("payments", payments.toJsonArray { it.toJson() })
        .put("budgets", budgets.toJsonArray { it.toJson() })
        .put("categories", categories.toJsonArray { it.toJson() })
        .put("capturedTransactions", capturedTransactions.toJsonArray { it.toJson() })
        .put("recurringRules", recurringRules.toJsonArray { it.toJson() })
        .put("installmentGroups", installmentGroups.toJsonArray { it.toJson() })
        .toString(2)

    private fun <T> List<T>.toJsonArray(mapper: (T) -> JSONObject): JSONArray =
        JSONArray().also { array -> forEach { array.put(mapper(it)) } }

    private fun PaymentEntity.toJson() = JSONObject()
        .put("id", id)
        .put("title", title)
        .put("amountInCents", amountInCents)
        .put("categoryId", categoryId)
        .put("dueDate", dueDate.toString())
        .put("paidAt", paidAt?.toString())
        .put("status", status.name)
        .put("type", type.name)
        .put("paymentMethod", paymentMethod?.name)
        .put("source", source.name)
        .put("notes", notes)
        .put("capturedTransactionId", capturedTransactionId)
        .put("installmentGroupId", installmentGroupId)
        .put("installmentNumber", installmentNumber)
        .put("totalInstallments", totalInstallments)
        .put("createdAt", createdAt.toString())
        .put("updatedAt", updatedAt.toString())

    private fun BudgetEntity.toJson() = JSONObject()
        .put("id", id)
        .put("name", name)
        .put("monthStart", monthStart.toString())
        .put("categoryId", categoryId)
        .put("limitInCents", limitInCents)
        .put("alertAtPercent", alertAtPercent)
        .put("isActive", isActive)
        .put("createdAt", createdAt.toString())
        .put("updatedAt", updatedAt.toString())

    private fun CategoryEntity.toJson() = JSONObject()
        .put("id", id)
        .put("name", name)
        .put("colorHex", colorHex)
        .put("icon", icon)
        .put("isDefault", isDefault)
        .put("createdAt", createdAt.toString())

    private fun CapturedTransactionEntity.toJson() = JSONObject()
        .put("id", id)
        .put("sourcePackage", sourcePackage)
        .put("sourceAppName", sourceAppName)
        .put("rawTitle", rawTitle)
        .put("rawText", rawText)
        .put("rawSubText", rawSubText)
        .put("rawBigText", rawBigText)
        .put("amountInCents", amountInCents)
        .put("merchant", merchant)
        .put("transactionType", transactionType?.name)
        .put("occurredAt", occurredAt.toString())
        .put("capturedAt", capturedAt.toString())
        .put("suggestedCategoryId", suggestedCategoryId)
        .put("confidence", confidence.name)
        .put("status", status.name)
        .put("notificationHash", notificationHash)

    private fun RecurringPaymentRuleEntity.toJson() = JSONObject()
        .put("id", id)
        .put("title", title)
        .put("amountInCents", amountInCents)
        .put("categoryId", categoryId)
        .put("dayOfMonth", dayOfMonth)
        .put("startDate", startDate.toString())
        .put("endDate", endDate?.toString())
        .put("paymentMethod", paymentMethod?.name)
        .put("notes", notes)
        .put("isActive", isActive)
        .put("createdAt", createdAt.toString())
        .put("updatedAt", updatedAt.toString())

    private fun InstallmentGroupEntity.toJson() = JSONObject()
        .put("id", id)
        .put("title", title)
        .put("totalAmountInCents", totalAmountInCents)
        .put("totalInstallments", totalInstallments)
        .put("categoryId", categoryId)
        .put("firstDueDate", firstDueDate.toString())
        .put("paymentMethod", paymentMethod?.name)
        .put("notes", notes)
        .put("createdAt", createdAt.toString())
}
