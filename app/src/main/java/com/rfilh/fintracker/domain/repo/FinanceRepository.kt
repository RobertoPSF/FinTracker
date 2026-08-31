package com.rfilh.fintracker.domain.repo

import com.rfilh.fintracker.domain.model.BankSummary
import com.rfilh.fintracker.domain.model.MonthBalance
import com.rfilh.fintracker.domain.model.PersonBalance
import com.rfilh.fintracker.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface FinanceRepository {
    fun monthBalance(yearMonth: String): Flow<MonthBalance>
    fun bankSummaries(yearMonth: String): Flow<List<BankSummary>>
    fun transactions(yearMonth: String): Flow<List<Transaction>>
    fun transactionsByBank(yearMonth: String, bankPackage: String): Flow<List<Transaction>>
    fun reviewQueue(): Flow<List<Transaction>>
    fun reviewCount(): Flow<Int>
    fun receivables(): Flow<List<PersonBalance>>
    fun capturedCount(): Flow<Int>

    suspend fun byId(id: Long): Transaction?
    suspend fun save(transaction: Transaction)
    suspend fun delete(id: Long)
    suspend fun purgeRawContent(): Int
    suspend fun exportAll(): List<Transaction>
}
