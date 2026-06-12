package com.leona.controlepagamentos.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object AppMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS budgets (
                    id TEXT NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    monthStart TEXT NOT NULL,
                    categoryId TEXT,
                    limitInCents INTEGER NOT NULL,
                    alertAtPercent INTEGER NOT NULL,
                    isActive INTEGER NOT NULL,
                    createdAt TEXT NOT NULL,
                    updatedAt TEXT NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_budgets_monthStart ON budgets(monthStart)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_budgets_categoryId ON budgets(categoryId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_budgets_isActive ON budgets(isActive)")
        }
    }
}
