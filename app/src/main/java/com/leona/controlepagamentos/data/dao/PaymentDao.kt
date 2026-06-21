package com.leona.controlepagamentos.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.leona.controlepagamentos.data.model.PaymentEntity
import com.leona.controlepagamentos.data.model.PaymentStatus
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE dueDate BETWEEN :start AND :end ORDER BY dueDate ASC, title ASC")
    fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE dueDate BETWEEN :start AND :end ORDER BY dueDate ASC, title ASC")
    suspend fun getBetween(start: LocalDate, end: LocalDate): List<PaymentEntity>

    @Query("SELECT * FROM payments ORDER BY dueDate ASC, title ASC")
    suspend fun getAll(): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE dueDate BETWEEN :start AND :end AND status = :status ORDER BY dueDate ASC")
    fun observeBetweenByStatus(start: LocalDate, end: LocalDate, status: PaymentStatus): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE status = :status AND dueDate BETWEEN :start AND :end ORDER BY dueDate ASC LIMIT :limit")
    fun observeUpcoming(status: PaymentStatus, start: LocalDate, end: LocalDate, limit: Int): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE id = :id")
    suspend fun getById(id: String): PaymentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: PaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(payments: List<PaymentEntity>)

    @Update
    suspend fun update(payment: PaymentEntity)

    @Query("UPDATE payments SET status = :status, paidAt = :paidAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: PaymentStatus, paidAt: LocalDateTime?, updatedAt: LocalDateTime)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun delete(id: String)
}
