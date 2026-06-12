package com.leona.controlepagamentos.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "captured_transactions",
    indices = [
        Index("status"),
        Index("capturedAt"),
        Index(value = ["notificationHash"], unique = true)
    ]
)
data class CapturedTransactionEntity(
    @PrimaryKey val id: String,
    val sourcePackage: String,
    val sourceAppName: String?,
    val rawTitle: String?,
    val rawText: String?,
    val rawSubText: String?,
    val rawBigText: String?,
    val amountInCents: Long?,
    val merchant: String?,
    val transactionType: TransactionType?,
    val occurredAt: LocalDateTime,
    val capturedAt: LocalDateTime,
    val suggestedCategoryId: String?,
    val confidence: ParseConfidence,
    val status: CaptureStatus,
    val notificationHash: String
)
