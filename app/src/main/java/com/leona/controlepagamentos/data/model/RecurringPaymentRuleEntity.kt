package com.leona.controlepagamentos.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "recurring_payment_rules",
    indices = [Index("isActive"), Index("categoryId")]
)
data class RecurringPaymentRuleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val amountInCents: Long,
    val categoryId: String?,
    val dayOfMonth: Int,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val paymentMethod: PaymentMethod?,
    val notes: String?,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
