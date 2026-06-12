package com.leona.controlepagamentos.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.leona.controlepagamentos.data.model.CaptureStatus
import com.leona.controlepagamentos.data.model.CapturedTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CapturedTransactionDao {
    @Query("SELECT * FROM captured_transactions WHERE status = :status ORDER BY occurredAt DESC")
    fun observeByStatus(status: CaptureStatus): Flow<List<CapturedTransactionEntity>>

    @Query("SELECT * FROM captured_transactions ORDER BY occurredAt DESC")
    fun observeAll(): Flow<List<CapturedTransactionEntity>>

    @Query("SELECT * FROM captured_transactions ORDER BY occurredAt DESC")
    suspend fun getAll(): List<CapturedTransactionEntity>

    @Query("SELECT * FROM captured_transactions WHERE id = :id")
    suspend fun getById(id: String): CapturedTransactionEntity?

    @Query("SELECT COUNT(*) FROM captured_transactions WHERE notificationHash = :hash")
    suspend fun countByHash(hash: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(capturedTransaction: CapturedTransactionEntity): Long

    @Update
    suspend fun update(capturedTransaction: CapturedTransactionEntity)

    @Query("UPDATE captured_transactions SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: CaptureStatus)
}
