package com.rfilh.fintracker.domain.model

enum class TransactionType { INCOME, EXPENSE, UNKNOWN }

enum class Confidence { HIGH, MEDIUM, LOW }

enum class LoanStatus { NONE, LENT_PENDING, LENT_PAID, BORROWED_PENDING, BORROWED_PAID }

data class Transaction(
    val id: Long = 0,
    val bankPackage: String,
    val bankName: String,
    val type: TransactionType,
    val amountCents: Long,
    val counterparty: String?,
    val occurredAt: Long,
    val yearMonth: String,
    val confidence: Confidence,
    val reviewed: Boolean,
    val category: String? = null,
    val note: String? = null,
    val rawId: Long? = null,
    val loanStatus: LoanStatus = LoanStatus.NONE,
    val loanPerson: String? = null,
    val loanAmountCents: Long? = null,
    val manual: Boolean = false,
    val cardType: String? = null
)

data class MonthBalance(
    val yearMonth: String,
    val incomeCents: Long,
    val expenseCents: Long
) {
    val netCents: Long get() = incomeCents - expenseCents
}

data class BankSummary(
    val bankPackage: String,
    val bankName: String,
    val incomeCents: Long,
    val expenseCents: Long,
    val count: Int
)

data class PersonBalance(
    val person: String,
    /** Total original do empréstimo (soma das transações), antes de abater pagamentos parciais. */
    val pendingCents: Long,
    val count: Int,
    /** Soma dos pagamentos parciais já registrados à mão para essa pessoa. */
    val paidCents: Long = 0
) {
    val remainingCents: Long get() = (pendingCents - paidCents).coerceAtLeast(0)
    val partiallyPaid: Boolean get() = paidCents > 0
}

data class CategorySpend(
    val category: String,
    val cents: Long,
    /** Mesmo gasto no mês anterior, para o comparativo (0 se não havia). */
    val previousCents: Long
) {
    /** Variação percentual vs. mês anterior; null quando não há base de comparação. */
    val deltaPercent: Int? get() =
        if (previousCents <= 0) null else Math.round((cents - previousCents) * 100.0 / previousCents).toInt()
}

data class CounterpartyTotal(
    val name: String,
    val cents: Long,
    val count: Int
)

data class MonthComparison(
    val current: MonthBalance,
    val previous: MonthBalance,
    /** Mesmo mês do ano anterior; null quando ainda não há dados. */
    val sameMonthLastYear: MonthBalance?
)

data class PredictedBill(
    val name: String,
    val amountCents: Long,
    val dueAt: Long,
    val daysUntil: Int
) {
    val soon: Boolean get() = daysUntil in 0..5
}

enum class AlertKind { REAJUSTE, ATIPICO, RESUMO }

data class SpendingAlert(
    val kind: AlertKind,
    val text: String,
    val atMillis: Long
)

data class CategoryBudget(
    val category: String,
    val limitCents: Long
)

data class BackupImportSummary(
    val imported: Int,
    val skipped: Int
)

data class RawCapture(
    val packageName: String,
    val title: String?,
    val body: String?,
    val postedAt: Long
)
