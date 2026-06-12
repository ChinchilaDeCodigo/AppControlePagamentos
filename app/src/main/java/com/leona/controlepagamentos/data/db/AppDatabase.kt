package com.leona.controlepagamentos.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.leona.controlepagamentos.data.dao.BudgetDao
import com.leona.controlepagamentos.data.dao.CapturedTransactionDao
import com.leona.controlepagamentos.data.dao.CategoryDao
import com.leona.controlepagamentos.data.dao.InstallmentGroupDao
import com.leona.controlepagamentos.data.dao.NotificationSourceDao
import com.leona.controlepagamentos.data.dao.PaymentDao
import com.leona.controlepagamentos.data.dao.RecurringPaymentRuleDao
import com.leona.controlepagamentos.data.model.BudgetEntity
import com.leona.controlepagamentos.data.model.CapturedTransactionEntity
import com.leona.controlepagamentos.data.model.CategoryEntity
import com.leona.controlepagamentos.data.model.InstallmentGroupEntity
import com.leona.controlepagamentos.data.model.NotificationSourceEntity
import com.leona.controlepagamentos.data.model.PaymentEntity
import com.leona.controlepagamentos.data.model.RecurringPaymentRuleEntity

@Database(
    entities = [
        PaymentEntity::class,
        BudgetEntity::class,
        CapturedTransactionEntity::class,
        CategoryEntity::class,
        NotificationSourceEntity::class,
        RecurringPaymentRuleEntity::class,
        InstallmentGroupEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(AppConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun paymentDao(): PaymentDao
    abstract fun budgetDao(): BudgetDao
    abstract fun capturedTransactionDao(): CapturedTransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun notificationSourceDao(): NotificationSourceDao
    abstract fun recurringPaymentRuleDao(): RecurringPaymentRuleDao
    abstract fun installmentGroupDao(): InstallmentGroupDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "controle_pagamentos.db")
                .addMigrations(AppMigrations.MIGRATION_1_2)
                .build()
    }
}
