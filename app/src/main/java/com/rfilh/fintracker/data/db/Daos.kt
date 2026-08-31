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

    @Query("UPDATE raw_notification SET title = NULL, body = NULL, purged = 1 WHERE id = :id AND purged = 0")
    suspend fun purgeOne(id: Long)

    @Query("SELECT * FROM raw_notification ORDER BY postedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 200): Flow<List<RawNotificationEntity>>

    @Query("SELECT COUNT(*) FROM raw_notification")
    fun observeCount(): Flow<Int>

    /** Capturado mas sem transação vinculada = notificação não reconhecida pelo classificador. */
    @Query(
        "SELECT raw_notification.* FROM raw_notification " +
            "LEFT JOIN transactions ON transactions.rawId = raw_notification.id " +
            "WHERE raw_notification.parsed = 1 AND raw_notification.purged = 0 AND transactions.id IS NULL " +
            "ORDER BY raw_notification.postedAt DESC LIMIT :limit"
    )
    suspend fun unrecognized(limit: Int = 500): List<RawNotificationEntity>
}

@Dao
interface CategoryBudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CategoryBudgetEntity)

    @Query("DELETE FROM category_budget WHERE category = :category")
    suspend fun delete(category: String)

    @Query("SELECT * FROM category_budget ORDER BY category")
    fun observeAll(): Flow<List<CategoryBudgetEntity>>

    @Query("SELECT * FROM category_budget WHERE category = :category LIMIT 1")
    suspend fun byCategory(category: String): CategoryBudgetEntity?

    @Query("UPDATE category_budget SET alertedYearMonth = :yearMonth WHERE category = :category")
    suspend fun markAlerted(category: String, yearMonth: String)
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

    @Query("SELECT * FROM transactions WHERE yearMonth >= :from ORDER BY occurredAt DESC")
    fun observeSince(from: String): Flow<List<TransactionEntity>>

    @Query(
        "SELECT yearMonth, " +
            "IFNULL(SUM(CASE WHEN type = 'INCOME' THEN amountCents ELSE 0 END), 0) AS incomeCents, " +
            "IFNULL(SUM(CASE WHEN type = 'EXPENSE' THEN amountCents ELSE 0 END), 0) AS expenseCents " +
            "FROM transactions WHERE yearMonth >= :from GROUP BY yearMonth ORDER BY yearMonth"
    )
    fun observeMonthlyTotals(from: String): Flow<List<MonthlyTrendRow>>

    @Query(
        "SELECT yearMonth, " +
            "IFNULL(SUM(CASE WHEN type = 'INCOME' THEN amountCents ELSE 0 END), 0) AS incomeCents, " +
            "IFNULL(SUM(CASE WHEN type = 'EXPENSE' THEN amountCents ELSE 0 END), 0) AS expenseCents " +
            "FROM transactions WHERE yearMonth LIKE :yearPrefix || '-%' GROUP BY yearMonth ORDER BY yearMonth"
    )
    fun observeYear(yearPrefix: String): Flow<List<MonthlyTrendRow>>

    @Query(
        "SELECT COUNT(*) FROM transactions " +
            "WHERE bankPackage = :bankPackage AND amountCents = :amountCents AND occurredAt = :occurredAt"
    )
    suspend fun countExact(bankPackage: String, amountCents: Long, occurredAt: Long): Int

    @Query(
        "SELECT IFNULL(SUM(amountCents), 0) FROM transactions " +
            "WHERE type = 'EXPENSE' AND category = :category AND yearMonth = :yearMonth"
    )
    suspend fun categorySpend(category: String, yearMonth: String): Long
}
