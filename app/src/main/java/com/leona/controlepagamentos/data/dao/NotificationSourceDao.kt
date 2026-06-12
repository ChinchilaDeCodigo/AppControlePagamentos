package com.leona.controlepagamentos.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.leona.controlepagamentos.data.model.NotificationSourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationSourceDao {
    @Query("SELECT * FROM notification_sources ORDER BY appName ASC")
    fun observeAll(): Flow<List<NotificationSourceEntity>>

    @Query("SELECT * FROM notification_sources WHERE packageName = :packageName")
    suspend fun getByPackage(packageName: String): NotificationSourceEntity?

    @Query("SELECT COUNT(*) FROM notification_sources")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(sources: List<NotificationSourceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(source: NotificationSourceEntity)

    @Update
    suspend fun update(source: NotificationSourceEntity)
}
