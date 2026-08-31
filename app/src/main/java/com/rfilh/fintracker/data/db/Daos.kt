package com.rfilh.fintracker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RawNotificationDao {

    /** IGNORE + índice único em hash = dedup barato. Retorna -1 se duplicado. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: RawNotificationEntity): Long

    @Query("SELECT * FROM raw_notification WHERE hash = :hash LIMIT 1")
    suspend fun findByHash(hash: String): RawNotificationEntity?

    @Query("UPDATE raw_notification SET parsed = 1 WHERE id = :id")
    suspend fun markParsed(id: Long)

    @Query("UPDATE raw_notification SET title = NULL, body = NULL, purged = 1 WHERE parsed = 1 AND purged = 0")
    suspend fun purgeParsedContent(): Int

    @Query("SELECT * FROM raw_notification ORDER BY postedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 200): Flow<List<RawNotificationEntity>>

    @Query("SELECT COUNT(*) FROM raw_notification")
    fun observeCount(): Flow<Int>
}

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(entity: TransactionEntity): Long

    @Update
    suspend fun update(entity: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun byId(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE yearMonth = :yearMonth ORDER BY occurredAt DESC")
    fun observeByMonth(yearMonth: String): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions WHERE yearMonth = :yearMonth AND bankPackage = :bankPackage " +
            "ORDER BY occurredAt DESC"
    )
    fun observeByMonthAndBank(yearMonth: String, bankPackage: String): Flow<List<TransactionEntity>>

    @Query(
        "SELECT bankPackage, bankName, " +
            "SUM(CASE WHEN type = 'INCOME' THEN amountCents ELSE 0 END) AS incomeCents, " +
            "SUM(CASE WHEN type = 'EXPENSE' THEN amountCents ELSE 0 END) AS expenseCents, " +
            "COUNT(*) AS count " +
            "FROM transactions WHERE yearMonth = :yearMonth GROUP BY bankPackage, bankName ORDER BY bankName"
    )
    fun observeBankSummaries(yearMonth: String): Flow<List<BankSummaryRow>>

    @Query(
        "SELECT " +
            "IFNULL(SUM(CASE WHEN type = 'INCOME' THEN amountCents ELSE 0 END), 0) AS incomeCents, " +
            "IFNULL(SUM(CASE WHEN type = 'EXPENSE' THEN amountCents ELSE 0 END), 0) AS expenseCents " +
            "FROM transactions WHERE yearMonth = :yearMonth"
    )
    fun observeMonthTotals(yearMonth: String): Flow<MonthTotalsRow>

    @Query(
        "SELECT * FROM transactions WHERE reviewed = 0 AND (confidence != 'HIGH' OR type = 'UNKNOWN' OR amountCents = 0) " +
            "ORDER BY occurredAt DESC"
    )
    fun observeReviewQueue(): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions WHERE reviewed = 0 AND (confidence != 'HIGH' OR type = 'UNKNOWN' OR amountCents = 0)")
    fun observeReviewCount(): Flow<Int>

    @Query(
        "SELECT loanPerson AS person, " +
            "IFNULL(SUM(IFNULL(loanAmountCents, amountCents)), 0) AS pendingCents, " +
            "COUNT(*) AS count " +
            "FROM transactions WHERE loanStatus = 'LENT_PENDING' AND loanPerson IS NOT NULL " +
            "GROUP BY loanPerson ORDER BY pendingCents DESC"
    )
    fun observeReceivables(): Flow<List<PersonBalanceRow>>

    @Query("SELECT * FROM transactions ORDER BY occurredAt DESC")
    suspend fun all(): List<TransactionEntity>
}
