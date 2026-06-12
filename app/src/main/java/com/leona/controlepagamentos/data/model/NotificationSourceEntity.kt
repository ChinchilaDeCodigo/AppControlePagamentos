package com.leona.controlepagamentos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "notification_sources")
data class NotificationSourceEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isEnabled: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
