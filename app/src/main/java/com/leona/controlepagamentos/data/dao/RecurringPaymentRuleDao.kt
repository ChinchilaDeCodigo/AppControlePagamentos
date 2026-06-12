package com.leona.controlepagamentos.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.leona.controlepagamentos.data.model.RecurringPaymentRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringPaymentRuleDao {
    @Query("SELECT * FROM recurring_payment_rules WHERE isActive = 1 ORDER BY dayOfMonth ASC, title ASC")
    fun observeActive(): Flow<List<RecurringPaymentRuleEntity>>

    @Query("SELECT * FROM recurring_payment_rules ORDER BY isActive DESC, dayOfMonth ASC, title ASC")
    fun observeAll(): Flow<List<RecurringPaymentRuleEntity>>

    @Query("SELECT * FROM recurring_payment_rules ORDER BY isActive DESC, dayOfMonth ASC, title ASC")
    suspend fun getAll(): List<RecurringPaymentRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: RecurringPaymentRuleEntity)

    @Update
    suspend fun update(rule: RecurringPaymentRuleEntity)
}
