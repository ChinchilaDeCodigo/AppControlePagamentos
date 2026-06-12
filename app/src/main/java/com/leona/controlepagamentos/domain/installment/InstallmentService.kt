package com.leona.controlepagamentos.domain.installment

import com.leona.controlepagamentos.data.model.InstallmentGroupEntity
import com.leona.controlepagamentos.data.model.PaymentEntity
import com.leona.controlepagamentos.data.model.PaymentSource
import com.leona.controlepagamentos.data.model.PaymentStatus
import com.leona.controlepagamentos.data.model.PaymentType
import java.time.LocalDateTime
import java.util.UUID

class InstallmentService {
    fun createPayments(group: InstallmentGroupEntity, now: LocalDateTime): List<PaymentEntity> {
        require(group.totalInstallments > 0) { "totalInstallments must be greater than zero" }
        require(group.totalAmountInCents > 0) { "totalAmountInCents must be greater than zero" }

        val baseAmount = group.totalAmountInCents / group.totalInstallments
        val remainder = (group.totalAmountInCents % group.totalInstallments).toInt()

        return (1..group.totalInstallments).map { installmentNumber ->
            val extraCent = if (installmentNumber <= remainder) 1 else 0
            PaymentEntity(
                id = UUID.randomUUID().toString(),
                title = "${group.title} $installmentNumber/${group.totalInstallments}",
                amountInCents = baseAmount + extraCent,
                categoryId = group.categoryId,
                dueDate = group.firstDueDate.plusMonths((installmentNumber - 1).toLong()),
                paidAt = null,
                status = PaymentStatus.PENDING,
                type = PaymentType.INSTALLMENT,
                paymentMethod = group.paymentMethod,
                source = PaymentSource.INSTALLMENT_GROUP,
                notes = group.notes,
                capturedTransactionId = null,
                installmentGroupId = group.id,
                installmentNumber = installmentNumber,
                totalInstallments = group.totalInstallments,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}
