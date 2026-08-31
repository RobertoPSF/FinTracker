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
    val loanAmountCents: Long? = null
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
    val pendingCents: Long,
    val count: Int
)
