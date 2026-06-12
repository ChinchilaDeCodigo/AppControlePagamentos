package com.leona.controlepagamentos.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "payments",
    indices = [
        Index("dueDate"),
        Index("categoryId"),
        Index("status"),
        Index("capturedTransactionId"),
        Index("installmentGroupId")
    ]
)
data class PaymentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val amountInCents: Long,
    val categoryId: String?,
    val dueDate: LocalDate,
    val paidAt: LocalDateTime?,
    val status: PaymentStatus,
    val type: PaymentType,
    val paymentMethod: PaymentMethod?,
    val source: PaymentSource,
    val notes: String?,
    val capturedTransactionId: String?,
    val installmentGroupId: String?,
    val installmentNumber: Int?,
    val totalInstallments: Int?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
