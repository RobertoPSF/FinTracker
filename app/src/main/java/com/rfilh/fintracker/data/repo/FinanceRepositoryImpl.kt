package com.rfilh.fintracker.data.repo

import com.rfilh.fintracker.core.Anonymizer
import com.rfilh.fintracker.core.Dates
import com.rfilh.fintracker.data.db.CategoryBudgetDao
import com.rfilh.fintracker.data.db.CategoryBudgetEntity
import com.rfilh.fintracker.data.db.RawNotificationDao
import com.rfilh.fintracker.data.db.TransactionDao
import com.rfilh.fintracker.domain.model.BackupImportSummary
import com.rfilh.fintracker.domain.model.BankSummary
import com.rfilh.fintracker.domain.model.CategoryBudget
import com.rfilh.fintracker.domain.model.MonthBalance
import com.rfilh.fintracker.domain.model.PersonBalance
import com.rfilh.fintracker.domain.model.RawCapture
import com.rfilh.fintracker.domain.model.Transaction
import com.rfilh.fintracker.domain.repo.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class FinanceRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val rawDao: RawNotificationDao,
    private val categoryBudgetDao: CategoryBudgetDao
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

    override fun monthlyTrend(months: Int): Flow<List<MonthBalance>> {
        val from = Dates.shift(Dates.currentYearMonth(), -(months - 1))
        return transactionDao.observeMonthlyTotals(from).map { rows -> rows.map { it.toDomain() } }
    }

    override fun yearlyTrend(year: Int): Flow<List<MonthBalance>> =
        transactionDao.observeYear(year.toString()).map { rows ->
            val byMonth = rows.associateBy { it.yearMonth }
            (1..12).map { month ->
                val ym = "%04d-%02d".format(year, month)
                byMonth[ym]?.toDomain() ?: MonthBalance(ym, 0, 0)
            }
        }

    override fun recentTransactions(months: Int): Flow<List<Transaction>> {
        val from = Dates.shift(Dates.currentYearMonth(), -(months - 1))
        return transactionDao.observeSince(from).map { list -> list.map { it.toDomain() } }
    }

    override fun categoryBudgets(): Flow<List<CategoryBudget>> =
        categoryBudgetDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun byId(id: Long): Transaction? = transactionDao.byId(id)?.toDomain()

    override suspend fun save(transaction: Transaction) {
        if (transaction.id == 0L) transactionDao.insert(transaction.toEntity())
        else transactionDao.update(transaction.toEntity())
    }

    override suspend fun delete(id: Long) = transactionDao.delete(id)

    override suspend fun purgeRawContent(): Int = rawDao.purgeParsedContent()

    override suspend fun exportAll(): List<Transaction> = transactionDao.all().map { it.toDomain() }

    override suspend fun exportBackupJson(): String = BackupCodec.encode(exportAll())

    override suspend fun importBackup(json: String): BackupImportSummary {
        var imported = 0
        var skipped = 0
        BackupCodec.decode(json).forEach { t ->
            val exists = transactionDao.countExact(t.bankPackage, t.amountCents, t.occurredAt) > 0
            if (exists) {
                skipped++
            } else {
                transactionDao.insert(t.toEntity())
                imported++
            }
        }
        return BackupImportSummary(imported, skipped)
    }

    override suspend fun saveCategoryBudget(category: String, limitCents: Long) {
        categoryBudgetDao.upsert(CategoryBudgetEntity(category, limitCents))
    }

    override suspend fun deleteCategoryBudget(category: String) = categoryBudgetDao.delete(category)

    override suspend fun unrecognizedSamplesJson(): String {
        val samples = rawDao.unrecognized().map {
            val capture: RawCapture = it.toDomain()
            AnonymizedSample(capture.packageName, Anonymizer.anonymize(capture.title), Anonymizer.anonymize(capture.body))
        }
        return Json { prettyPrint = true }.encodeToString(ListSerializer(AnonymizedSample.serializer()), samples)
    }

    @Serializable
    private data class AnonymizedSample(val packageName: String, val title: String?, val body: String?)
}
