package com.rfilh.fintracker.data.repo

import com.rfilh.fintracker.data.db.BankSummaryRow
import com.rfilh.fintracker.data.db.CategoryBudgetEntity
import com.rfilh.fintracker.data.db.CounterpartyTotalRow
import com.rfilh.fintracker.data.db.MonthlyTrendRow
import com.rfilh.fintracker.data.db.PersonBalanceRow
import com.rfilh.fintracker.data.db.RawNotificationEntity
import com.rfilh.fintracker.data.db.TransactionEntity
import com.rfilh.fintracker.domain.model.BankSummary
import com.rfilh.fintracker.domain.model.CategoryBudget
import com.rfilh.fintracker.domain.model.CounterpartyTotal
import com.rfilh.fintracker.domain.model.MonthBalance
import com.rfilh.fintracker.domain.model.PersonBalance
import com.rfilh.fintracker.domain.model.RawCapture
import com.rfilh.fintracker.domain.model.Transaction

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    bankPackage = bankPackage,
    bankName = bankName,
    type = type,
    amountCents = amountCents,
    counterparty = counterparty,
    occurredAt = occurredAt,
    yearMonth = yearMonth,
    confidence = confidence,
    reviewed = reviewed,
    category = category,
    note = note,
    rawId = rawId,
    loanStatus = loanStatus,
    loanPerson = loanPerson,
    loanAmountCents = loanAmountCents,
    manual = manual,
    cardType = cardType
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    bankPackage = bankPackage,
    bankName = bankName,
    type = type,
    amountCents = amountCents,
    counterparty = counterparty,
    occurredAt = occurredAt,
    yearMonth = yearMonth,
    confidence = confidence,
    reviewed = reviewed,
    category = category,
    note = note,
    rawId = rawId,
    loanStatus = loanStatus,
    loanPerson = loanPerson,
    loanAmountCents = loanAmountCents,
    manual = manual,
    cardType = cardType
)

fun BankSummaryRow.toDomain() = BankSummary(bankPackage, bankName, incomeCents, expenseCents, count)

fun PersonBalanceRow.toDomain() = PersonBalance(person, pendingCents, count)

fun CounterpartyTotalRow.toDomain() = CounterpartyTotal(name, cents, count)

fun MonthlyTrendRow.toDomain() = MonthBalance(yearMonth, incomeCents, expenseCents)

fun CategoryBudgetEntity.toDomain() = CategoryBudget(category, limitCents)

fun RawNotificationEntity.toDomain() = RawCapture(packageName, title, body, postedAt)
