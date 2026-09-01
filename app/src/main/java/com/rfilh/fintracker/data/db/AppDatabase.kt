package com.rfilh.fintracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [RawNotificationEntity::class, TransactionEntity::class, CategoryBudgetEntity::class],
    version = 4,
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

        private val M2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN manual INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val M3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN cardType TEXT")
            }
        }

        val MIGRATIONS: Array<Migration> = arrayOf(M1_2, M2_3, M3_4)
    }
}
