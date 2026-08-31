package com.rfilh.fintracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [RawNotificationEntity::class, TransactionEntity::class, CategoryBudgetEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rawDao(): RawNotificationDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryBudgetDao(): CategoryBudgetDao

    companion object {
        const val NAME = "fintracker.db"

        private val M1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS category_budget (" +
                        "category TEXT NOT NULL PRIMARY KEY, " +
                        "limitCents INTEGER NOT NULL, " +
                        "alertedYearMonth TEXT)"
                )
            }
        }

        val MIGRATIONS: Array<Migration> = arrayOf(M1_2)
    }
}
