package com.leona.controlepagamentos.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val colorHex: String?,
    val icon: String?,
    val isDefault: Boolean,
    val createdAt: LocalDateTime
)
