package com.rfilh.fintracker.domain.repo

import com.rfilh.fintracker.domain.model.BackupImportSummary
import com.rfilh.fintracker.domain.model.BankSummary
import com.rfilh.fintracker.domain.model.CategoryBudget
import com.rfilh.fintracker.domain.model.CategorySpend
import com.rfilh.fintracker.domain.model.CounterpartyTotal
import com.rfilh.fintracker.domain.model.MonthBalance
import com.rfilh.fintracker.domain.model.MonthComparison
import com.rfilh.fintracker.domain.model.PersonBalance
import com.rfilh.fintracker.domain.model.PredictedBill
import com.rfilh.fintracker.domain.model.SpendingAlert
import com.rfilh.fintracker.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface FinanceRepository {
    fun monthBalance(yearMonth: String): Flow<MonthBalance>
    fun bankSummaries(yearMonth: String): Flow<List<BankSummary>>
    fun transactions(yearMonth: String): Flow<List<Transaction>>
    fun transactionsByBank(yearMonth: String, bankPackage: String): Flow<List<Transaction>>
    fun searchTransactions(query: String): Flow<List<Transaction>>
    fun reviewQueue(): Flow<List<Transaction>>
    fun reviewCount(): Flow<Int>
    fun receivables(): Flow<List<PersonBalance>>
    fun capturedCount(): Flow<Int>
    fun monthlyTrend(months: Int = 6): Flow<List<MonthBalance>>
    fun yearlyTrend(year: Int): Flow<List<MonthBalance>>
    fun recentTransactions(months: Int): Flow<List<Transaction>>
    fun categoryBudgets(): Flow<List<CategoryBudget>>

    /** Gasto do mês por categoria, já com o total do mês anterior para o comparativo. */
    fun categorySpend(yearMonth: String): Flow<List<CategorySpend>>

    /** Ranking de contrapartes por total movimentado (histórico completo). */
    fun counterpartyRanking(expense: Boolean): Flow<List<CounterpartyTotal>>

    /** Balanço do mês vs. mês anterior e vs. mesmo mês do ano passado. */
    fun monthComparison(yearMonth: String): Flow<MonthComparison>

    /** Agenda de contas previstas a partir dos gastos recorrentes detectados. */
    fun predictedBills(): Flow<List<PredictedBill>>

    /** Alertas (reajuste, gasto atípico, resumo semanal) sobre os gastos recentes. */
    fun spendingAlerts(): Flow<List<SpendingAlert>>

    suspend fun byId(id: Long): Transaction?
    suspend fun save(transaction: Transaction)
    suspend fun delete(id: Long)
    suspend fun addLoanPayment(person: String, amountCents: Long)
    suspend fun exportCsv(): String
    suspend fun purgeRawContent(): Int
    suspend fun exportAll(): List<Transaction>
    suspend fun exportBackupJson(): String
    suspend fun importBackup(json: String): BackupImportSummary
    suspend fun saveCategoryBudget(category: String, limitCents: Long)
    suspend fun deleteCategoryBudget(category: String)
    suspend fun unrecognizedSamplesJson(): String
}
