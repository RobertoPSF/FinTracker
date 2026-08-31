package com.rfilh.fintracker.data.repo

import com.rfilh.fintracker.data.db.RawNotificationDao
import com.rfilh.fintracker.data.db.TransactionDao
import com.rfilh.fintracker.domain.model.BankSummary
import com.rfilh.fintracker.domain.model.MonthBalance
import com.rfilh.fintracker.domain.model.PersonBalance
import com.rfilh.fintracker.domain.model.Transaction
import com.rfilh.fintracker.domain.repo.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FinanceRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val rawDao: RawNotificationDao
) : FinanceRepository {

    override fun monthBalance(yearMonth: String): Flow<MonthBalance> =
        transactionDao.observeMonthTotals(yearMonth)
            .map { MonthBalance(yearMonth, it.incomeCents, it.expenseCents) }

    override fun bankSummaries(yearMonth: String): Flow<List<BankSummary>> =
        transactionDao.observeBankSummaries(yearMonth).map { rows -> rows.map { it.toDomain() } }

    override fun transactions(yearMonth: String): Flow<List<Transaction>> =
        transactionDao.observeByMonth(yearMonth).map { list -> list.map { it.toDomain() } }

    override fun transactionsByBank(yearMonth: String, bankPackage: String): Flow<List<Transaction>> =
        transactionDao.observeByMonthAndBank(yearMonth, bankPackage).map { list -> list.map { it.toDomain() } }

    override fun reviewQueue(): Flow<List<Transaction>> =
        transactionDao.observeReviewQueue().map { list -> list.map { it.toDomain() } }

    override fun reviewCount(): Flow<Int> = transactionDao.observeReviewCount()

    override fun receivables(): Flow<List<PersonBalance>> =
        transactionDao.observeReceivables().map { rows -> rows.map { it.toDomain() } }

    override fun capturedCount(): Flow<Int> = rawDao.observeCount()

    override suspend fun byId(id: Long): Transaction? = transactionDao.byId(id)?.toDomain()

    override suspend fun save(transaction: Transaction) {
        if (transaction.id == 0L) transactionDao.insert(transaction.toEntity())
        else transactionDao.update(transaction.toEntity())
    }

    override suspend fun delete(id: Long) = transactionDao.delete(id)

    override suspend fun purgeRawContent(): Int = rawDao.purgeParsedContent()

    override suspend fun exportAll(): List<Transaction> = transactionDao.all().map { it.toDomain() }
}
