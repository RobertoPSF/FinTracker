package com.rfilh.fintracker.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfilh.fintracker.core.Dates
import com.rfilh.fintracker.domain.model.BankSummary
import com.rfilh.fintracker.domain.model.Confidence
import com.rfilh.fintracker.domain.model.LoanStatus
import com.rfilh.fintracker.domain.model.MonthBalance
import com.rfilh.fintracker.domain.model.PersonBalance
import com.rfilh.fintracker.domain.model.Transaction
import com.rfilh.fintracker.domain.model.TransactionType
import com.rfilh.fintracker.domain.repo.FinanceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(private val repo: FinanceRepository) : ViewModel() {

    private val _month = MutableStateFlow(Dates.currentYearMonth())
    val month: StateFlow<String> = _month.asStateFlow()

    val balance: StateFlow<MonthBalance> = _month
        .flatMapLatest { repo.monthBalance(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MonthBalance(_month.value, 0, 0))

    val banks: StateFlow<List<BankSummary>> = _month
        .flatMapLatest { repo.bankSummaries(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val reviewCount: StateFlow<Int> = repo.reviewCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val capturedCount: StateFlow<Int> = repo.capturedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun shiftMonth(delta: Int) { _month.value = Dates.shift(_month.value, delta) }
}

@OptIn(ExperimentalCoroutinesApi::class)
class StatementViewModel(
    private val repo: FinanceRepository,
    private val bankPackage: String?
) : ViewModel() {

    private val _month = MutableStateFlow(Dates.currentYearMonth())
    val month: StateFlow<String> = _month.asStateFlow()

    val transactions: StateFlow<List<Transaction>> = _month
        .flatMapLatest { ym ->
            if (bankPackage.isNullOrBlank()) repo.transactions(ym)
            else repo.transactionsByBank(ym, bankPackage)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun shiftMonth(delta: Int) { _month.value = Dates.shift(_month.value, delta) }
}

class ReviewViewModel(private val repo: FinanceRepository) : ViewModel() {

    val queue: StateFlow<List<Transaction>> = repo.reviewQueue()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun approve(transaction: Transaction) = viewModelScope.launch {
        repo.save(transaction.copy(reviewed = true, confidence = Confidence.HIGH))
    }

    fun setType(transaction: Transaction, type: TransactionType) = viewModelScope.launch {
        repo.save(transaction.copy(type = type))
    }

    fun discard(id: Long) = viewModelScope.launch { repo.delete(id) }
}

class EditViewModel(private val repo: FinanceRepository, private val id: Long) : ViewModel() {

    private val _state = MutableStateFlow<Transaction?>(null)
    val state: StateFlow<Transaction?> = _state.asStateFlow()

    init { viewModelScope.launch { _state.value = repo.byId(id) } }

    fun update(block: (Transaction) -> Transaction) {
        _state.value = _state.value?.let(block)
    }

    fun save(onDone: () -> Unit) = viewModelScope.launch {
        _state.value?.let { repo.save(it.copy(reviewed = true)) }
        onDone()
    }

    fun delete(onDone: () -> Unit) = viewModelScope.launch {
        repo.delete(id)
        onDone()
    }
}

class ReceivablesViewModel(repo: FinanceRepository) : ViewModel() {
    val people: StateFlow<List<PersonBalance>> = repo.receivables()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

/** Helpers de UI para a feature de empréstimo. */
object LoanOptions {
    val all = listOf(
        LoanStatus.NONE to "Nenhum",
        LoanStatus.LENT_PENDING to "Emprestei (a receber)",
        LoanStatus.LENT_PAID to "Emprestei (recebido)",
        LoanStatus.BORROWED_PENDING to "Peguei emprestado",
        LoanStatus.BORROWED_PAID to "Peguei (quitado)"
    )
}
