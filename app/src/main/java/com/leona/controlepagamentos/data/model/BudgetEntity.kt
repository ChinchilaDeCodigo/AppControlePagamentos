package com.leona.controlepagamentos.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "budgets",
    indices = [
        Index("monthStart"),
        Index("categoryId"),
        Index("isActive")
    ]
)
data class BudgetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val monthStart: LocalDate,
    val categoryId: String?,
    val limitInCents: Long,
    val alertAtPercent: Int,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
