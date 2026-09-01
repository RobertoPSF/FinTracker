package com.rfilh.fintracker.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rfilh.fintracker.AppContainer
import com.rfilh.fintracker.core.Dates
import com.rfilh.fintracker.core.RecurrenceDetector
import com.rfilh.fintracker.core.RecurringItem
import com.rfilh.fintracker.domain.model.BackupImportSummary
import com.rfilh.fintracker.domain.model.BankSummary
import com.rfilh.fintracker.domain.model.CategoryBudget
import com.rfilh.fintracker.domain.model.Confidence
import com.rfilh.fintracker.domain.model.LoanStatus
import com.rfilh.fintracker.domain.model.MonthBalance
import com.rfilh.fintracker.domain.model.PersonBalance
import com.rfilh.fintracker.domain.model.Transaction
import com.rfilh.fintracker.domain.model.TransactionType
import com.rfilh.fintracker.domain.repo.FinanceRepository
import com.rfilh.fintracker.ui.theme.ToastBus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /** Busca por pessoa/instituição ignora o mês selecionado — visão de auditoria, não fica
     *  presa ao mês corrente (útil pra conferir todo o histórico com um cliente/fornecedor). */
    val transactions: StateFlow<List<Transaction>> = combine(_month, _query) { ym, q -> ym to q }
        .flatMapLatest { (ym, q) ->
            when {
                q.isNotBlank() -> repo.searchTransactions(q)
                bankPackage.isNullOrBlank() -> repo.transactions(ym)
                else -> repo.transactionsByBank(ym, bankPackage)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val title: StateFlow<String> = combine(transactions, _query) { list, q ->
        when {
            q.isNotBlank() -> "Busca: \"$q\""
            bankPackage.isNullOrBlank() -> "Extrato geral"
            else -> list.firstOrNull()?.bankName ?: "Extrato"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), if (bankPackage.isNullOrBlank()) "Extrato geral" else "Extrato")

    fun shiftMonth(delta: Int) { _month.value = Dates.shift(_month.value, delta) }
    fun setQuery(q: String) { _query.value = q }
}

class ReviewViewModel(private val repo: FinanceRepository) : ViewModel() {

    val queue: StateFlow<List<Transaction>> = repo.reviewQueue()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun approve(transaction: Transaction) = viewModelScope.launch {
        repo.save(transaction.copy(reviewed = true, confidence = Confidence.HIGH))
        ToastBus.show("Aprovado")
    }

    fun setType(transaction: Transaction, type: TransactionType) = viewModelScope.launch {
        repo.save(transaction.copy(type = type))
    }

    fun discard(id: Long) = viewModelScope.launch {
        repo.delete(id)
        ToastBus.show("Descartado")
    }
}

class EditViewModel(private val repo: FinanceRepository, container: AppContainer, private val id: Long) : ViewModel() {

    val categoryOptions: List<String> = container.rules().generic.categories.map { it.name }

    private val _state = MutableStateFlow<Transaction?>(null)
    val state: StateFlow<Transaction?> = _state.asStateFlow()

    init { viewModelScope.launch { _state.value = repo.byId(id) } }

    fun update(block: (Transaction) -> Transaction) {
        _state.value = _state.value?.let(block)
    }

    fun save(onDone: () -> Unit) = viewModelScope.launch {
        _state.value?.let { repo.save(it.copy(reviewed = true)) }
        ToastBus.show("Lançamento salvo")
        onDone()
    }

    fun delete(onDone: () -> Unit) = viewModelScope.launch {
        repo.delete(id)
        ToastBus.show("Lançamento excluído")
        onDone()
    }
}

/**
 * Registro manual de Pix enviado — pra bancos/fintechs que não notificam saída de Pix
 * (ex. Mercado Pago, Nubank em certos casos), o usuário informa valor, banco de origem e
 * destinatário direto, sem depender da captura automática.
 */
class ManualPixViewModel(private val repo: FinanceRepository, container: AppContainer) : ViewModel() {

    /** Bancos já cadastrados em parser_rules.json — permite reaproveitar o mesmo bankPackage
     *  de um banco que já captura outras transações (ícone e agrupamento ficam corretos). */
    val bankOptions: List<Pair<String, String>> =
        container.rules().overrides.map { it.`package` to it.name }.sortedBy { it.second }

    private val _state = MutableStateFlow(
        Transaction(
            bankPackage = "",
            bankName = "",
            type = TransactionType.EXPENSE,
            amountCents = 0,
            counterparty = null,
            occurredAt = System.currentTimeMillis(),
            yearMonth = Dates.currentYearMonth(),
            confidence = Confidence.HIGH,
            reviewed = true,
            manual = true
        )
    )
    val state: StateFlow<Transaction> = _state.asStateFlow()

    fun update(block: (Transaction) -> Transaction) { _state.value = block(_state.value) }

    fun setOccurredAt(epochMillis: Long) {
        _state.value = _state.value.copy(occurredAt = epochMillis, yearMonth = Dates.yearMonth(epochMillis))
    }

    fun save(onDone: () -> Unit) {
        val t = _state.value
        if (t.amountCents <= 0 || t.bankName.isBlank() || t.counterparty.isNullOrBlank()) {
            ToastBus.show("Preencha valor, banco e destinatário")
            return
        }
        viewModelScope.launch {
            repo.save(t)
            ToastBus.show("Pix enviado registrado")
            onDone()
        }
    }

    companion object {
        /** bankPackage sintético e estável pra bancos digitados livremente (fora da lista
         *  conhecida) — mesmo nome sempre gera o mesmo id, então agrupa corretamente na Home. */
        fun manualPackageFor(bankName: String): String =
            "manual:" + bankName.trim().lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
    }
}

class ReceivablesViewModel(repo: FinanceRepository) : ViewModel() {
    val people: StateFlow<List<PersonBalance>> = repo.receivables()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

class InsightsViewModel(repo: FinanceRepository) : ViewModel() {

    val currentYear: Int = Dates.currentYearMonth().substring(0, 4).toInt()

    private val _tab = MutableStateFlow(GraficoTab.MENSAL)
    val tab: StateFlow<GraficoTab> = _tab.asStateFlow()
    fun selectTab(tab: GraficoTab) { _tab.value = tab }

    val trend: StateFlow<List<MonthBalance>> = repo.monthlyTrend(6)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val yearlyTrend: StateFlow<List<MonthBalance>> = repo.yearlyTrend(currentYear)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recurring: StateFlow<List<RecurringItem>> = repo.recentTransactions(6)
        .map { RecurrenceDetector.detect(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

enum class GraficoTab { MENSAL, ANUAL }

class SettingsViewModel(private val repo: FinanceRepository, private val container: AppContainer) : ViewModel() {

    val categoryOptions: List<String> = container.rules().generic.categories.map { it.name }

    private val _appLockEnabled = MutableStateFlow(container.isAppLockEnabled())
    val appLockEnabled: StateFlow<Boolean> = _appLockEnabled.asStateFlow()
    val biometricAvailable: Boolean get() = container.canUseBiometricLock()

    private val _autoPurgeRaw = MutableStateFlow(container.isAutoPurgeRawEnabled())
    val autoPurgeRaw: StateFlow<Boolean> = _autoPurgeRaw.asStateFlow()

    val categoryBudgets: StateFlow<List<CategoryBudget>> = repo.categoryBudgets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _darkMode = container.darkMode
    val darkMode: StateFlow<Boolean> = _darkMode

    fun setAppLockEnabled(enabled: Boolean) {
        container.setAppLockEnabled(enabled)
        _appLockEnabled.value = enabled
    }

    fun setAutoPurgeRaw(enabled: Boolean) {
        container.setAutoPurgeRawEnabled(enabled)
        _autoPurgeRaw.value = enabled
    }

    fun setDarkMode(enabled: Boolean) = container.setDarkMode(enabled)

    fun purgeNow() = viewModelScope.launch {
        val count = repo.purgeRawContent()
        ToastBus.show("$count registro(s) de conteúdo bruto apagados")
    }

    fun saveCategoryBudget(category: String, limitCents: Long) = viewModelScope.launch {
        repo.saveCategoryBudget(category, limitCents)
        ToastBus.show("Limite adicionado")
    }

    fun deleteCategoryBudget(category: String) = viewModelScope.launch {
        repo.deleteCategoryBudget(category)
    }

    suspend fun exportBackupJson(): String = repo.exportBackupJson()

    fun importBackup(json: String) = viewModelScope.launch {
        val summary: BackupImportSummary = repo.importBackup(json)
        ToastBus.show("${summary.imported} importada(s), ${summary.skipped} ignorada(s)")
    }

    suspend fun exportUnrecognizedJson(): String = repo.unrecognizedSamplesJson()
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
