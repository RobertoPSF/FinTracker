package com.rfilh.fintracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration

@Database(
    entities = [RawNotificationEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rawDao(): RawNotificationDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        const val NAME = "fintracker.db"

        /**
         * Migrações versionadas desde o início. Modelo para a próxima:
         * val M1_2 = object : Migration(1, 2) {
         *     override fun migrate(db: SupportSQLiteDatabase) {
         *         db.execSQL("ALTER TABLE transactions ADD COLUMN tag TEXT")
         *     }
         * }
         */
        val MIGRATIONS: Array<Migration> = emptyArray()
    }
}
