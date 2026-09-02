package com.rfilh.fintracker.data.repo

import com.rfilh.fintracker.core.Anonymizer
import com.rfilh.fintracker.core.Dates
import com.rfilh.fintracker.core.PredictedBills
import com.rfilh.fintracker.core.RecurrenceDetector
import com.rfilh.fintracker.core.SpendingAlerts
import com.rfilh.fintracker.core.TextNormalizer
import com.rfilh.fintracker.data.db.CategoryBudgetDao
import com.rfilh.fintracker.data.db.CategoryBudgetEntity
import com.rfilh.fintracker.data.db.LoanPaymentDao
import com.rfilh.fintracker.data.db.LoanPaymentEntity
import com.rfilh.fintracker.data.db.RawNotificationDao
import com.rfilh.fintracker.data.db.TransactionDao
import com.rfilh.fintracker.domain.model.BackupImportSummary
import com.rfilh.fintracker.domain.model.BankSummary
import com.rfilh.fintracker.domain.model.CategoryBudget
import com.rfilh.fintracker.domain.model.CategorySpend
import com.rfilh.fintracker.domain.model.CounterpartyTotal
import com.rfilh.fintracker.domain.model.MonthBalance
import com.rfilh.fintracker.domain.model.MonthComparison
import com.rfilh.fintracker.domain.model.PersonBalance
import com.rfilh.fintracker.domain.model.PredictedBill
import com.rfilh.fintracker.domain.model.RawCapture
import com.rfilh.fintracker.domain.model.SpendingAlert
import com.rfilh.fintracker.domain.model.Transaction
import com.rfilh.fintracker.domain.model.TransactionType
import com.rfilh.fintracker.domain.repo.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class FinanceRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val rawDao: RawNotificationDao,
    private val categoryBudgetDao: CategoryBudgetDao,
    private val loanPaymentDao: LoanPaymentDao
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

    /** Busca por contraparte/banco ignorando acento e maiúsculas — o `LIKE` do SQLite só
     *  ignora maiúsculas, então a comparação real é feita aqui em Kotlin (ver [TextNormalizer]),
     *  não no banco. */
    override fun searchTransactions(query: String): Flow<List<Transaction>> {
        val normalizedQuery = TextNormalizer.normalize(query)
        return transactionDao.observeAll().map { list ->
            list.filter { entity ->
                TextNormalizer.normalize(entity.counterparty.orEmpty()).contains(normalizedQuery) ||
                    TextNormalizer.normalize(entity.bankName).contains(normalizedQuery)
            }.map { it.toDomain() }
        }
    }

    override fun reviewQueue(): Flow<List<Transaction>> =
        transactionDao.observeReviewQueue().map { list -> list.map { it.toDomain() } }

    override fun reviewCount(): Flow<Int> = transactionDao.observeReviewCount()

    /** Junta o saldo a receber das transações com os pagamentos parciais lançados à mão. */
    override fun receivables(): Flow<List<PersonBalance>> =
        combine(transactionDao.observeReceivables(), loanPaymentDao.observePaid()) { balances, paid ->
            val paidByPerson = paid.associate { it.person to it.paidCents }
            balances.map { it.toDomain().copy(paidCents = paidByPerson[it.person] ?: 0L) }
        }

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

    override fun categorySpend(yearMonth: String): Flow<List<CategorySpend>> =
        combine(
            transactionDao.observeCategorySpend(yearMonth),
            transactionDao.observeCategorySpend(Dates.shift(yearMonth, -1))
        ) { current, previous ->
            val prevByCategory = previous.associate { it.category to it.cents }
            current
                .map { CategorySpend(it.category, it.cents, prevByCategory[it.category] ?: 0L) }
                .sortedByDescending { it.cents }
        }

    override fun counterpartyRanking(expense: Boolean): Flow<List<CounterpartyTotal>> {
        val type = if (expense) TransactionType.EXPENSE else TransactionType.INCOME
        return transactionDao.observeCounterpartyTotals(type).map { rows -> rows.map { it.toDomain() } }
    }

    override fun monthComparison(yearMonth: String): Flow<MonthComparison> {
        val prev = Dates.shift(yearMonth, -1)
        val lastYear = Dates.shift(yearMonth, -12)
        return combine(
            transactionDao.observeMonthTotals(yearMonth),
            transactionDao.observeMonthTotals(prev),
            transactionDao.observeMonthTotals(lastYear)
        ) { cur, pr, ly ->
            val lastYearBalance = MonthBalance(lastYear, ly.incomeCents, ly.expenseCents)
            MonthComparison(
                current = MonthBalance(yearMonth, cur.incomeCents, cur.expenseCents),
                previous = MonthBalance(prev, pr.incomeCents, pr.expenseCents),
                sameMonthLastYear = lastYearBalance.takeIf { it.incomeCents > 0 || it.expenseCents > 0 }
            )
        }
    }

    override fun predictedBills(): Flow<List<PredictedBill>> =
        recentTransactions(6).map { txs ->
            PredictedBills.forecast(RecurrenceDetector.detect(txs), System.currentTimeMillis())
        }

    override fun spendingAlerts(): Flow<List<SpendingAlert>> =
        recentTransactions(6).map { txs ->
            SpendingAlerts.compute(txs, RecurrenceDetector.detect(txs), System.currentTimeMillis())
        }

    override suspend fun byId(id: Long): Transaction? = transactionDao.byId(id)?.toDomain()

    override suspend fun save(transaction: Transaction) {
        if (transaction.id == 0L) transactionDao.insert(transaction.toEntity())
        else transactionDao.update(transaction.toEntity())
    }

    override suspend fun delete(id: Long) = transactionDao.delete(id)

    override suspend fun addLoanPayment(person: String, amountCents: Long) {
        if (amountCents <= 0) return
        loanPaymentDao.insert(
            LoanPaymentEntity(
                person = person,
                amountCents = amountCents,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun purgeRawContent(): Int = rawDao.purgeParsedContent()

    override suspend fun exportAll(): List<Transaction> = transactionDao.all().map { it.toDomain() }

    override suspend fun exportCsv(): String = Exporter.toCsv(exportAll())

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
