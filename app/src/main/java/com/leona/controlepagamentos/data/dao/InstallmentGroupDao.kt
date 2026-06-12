package com.leona.controlepagamentos.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.leona.controlepagamentos.data.model.InstallmentGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstallmentGroupDao {
    @Query("SELECT * FROM installment_groups ORDER BY firstDueDate DESC")
    fun observeAll(): Flow<List<InstallmentGroupEntity>>

    @Query("SELECT * FROM installment_groups ORDER BY firstDueDate DESC")
    suspend fun getAll(): List<InstallmentGroupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: InstallmentGroupEntity)
}
