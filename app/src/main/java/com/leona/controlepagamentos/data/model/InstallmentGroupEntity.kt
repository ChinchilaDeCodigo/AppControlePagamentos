package com.leona.controlepagamentos.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "installment_groups",
    indices = [Index("categoryId"), Index("firstDueDate")]
)
data class InstallmentGroupEntity(
    @PrimaryKey val id: String,
    val title: String,
    val totalAmountInCents: Long,
    val totalInstallments: Int,
    val categoryId: String?,
    val firstDueDate: LocalDate,
    val paymentMethod: PaymentMethod?,
    val notes: String?,
    val createdAt: LocalDateTime
)
