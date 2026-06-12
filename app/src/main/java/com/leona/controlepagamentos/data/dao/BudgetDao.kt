package com.leona.controlepagamentos.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.leona.controlepagamentos.data.model.BudgetEntity
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE monthStart = :monthStart AND isActive = 1 ORDER BY categoryId IS NOT NULL, name ASC")
    fun observeActiveForMonth(monthStart: LocalDate): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets ORDER BY monthStart DESC, name ASC")
    suspend fun getAll(): List<BudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity)

    @Update
    suspend fun update(budget: BudgetEntity)

    @Query("UPDATE budgets SET isActive = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun deactivate(id: String, updatedAt: LocalDateTime)
}
