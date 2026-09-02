package com.rfilh.fintracker.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.rfilh.fintracker.domain.model.Confidence
import com.rfilh.fintracker.domain.model.LoanStatus
import com.rfilh.fintracker.domain.model.TransactionType

@Entity(
    tableName = "raw_notification",
    indices = [Index(value = ["hash"], unique = true), Index("postedAt")]
)
data class RawNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val title: String?,
    val body: String?,
    val postedAt: Long,
    val hash: String,
    val parsed: Boolean = false,
    /** Fase 6: conteúdo pode ser apagado após parse mantendo o hash para dedup. */
    val purged: Boolean = false
)

@Entity(
    tableName = "transactions",
    indices = [
        Index("yearMonth"),
        Index("bankPackage"),
        Index("occurredAt"),
        Index("reviewed"),
        Index("loanPerson")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bankPackage: String,
    val bankName: String,
    val type: TransactionType,
    @ColumnInfo(name = "amountCents") val amountCents: Long,
    val counterparty: String?,
    val occurredAt: Long,
    val yearMonth: String,
    val confidence: Confidence,
    val reviewed: Boolean = false,
    val category: String? = null,
    val note: String? = null,
    val rawId: Long? = null,
    val loanStatus: LoanStatus = LoanStatus.NONE,
    val loanPerson: String? = null,
    val loanAmountCents: Long? = null,
    /** Registrado manualmente pelo usuário (ex. Pix enviado em banco que não notifica saída). */
    val manual: Boolean = false,
    /** "Crédito" ou "Débito", quando a notificação de compra no cartão menciona a modalidade. */
    val cardType: String? = null
)

@Entity(tableName = "category_budget")
data class CategoryBudgetEntity(
    @PrimaryKey val category: String,
    val limitCents: Long,
    /** yyyy-MM do último mês em que o alerta de estouro já foi disparado, evita repetir. */
    val alertedYearMonth: String? = null
)

/**
 * Pagamento parcial de um empréstimo, registrado à mão pelo usuário. Não é uma transação
 * (não veio de notificação, não entra em extrato/balanço) — só abate o saldo devedor mostrado
 * em "A cobrar".
 */
@Entity(tableName = "loan_payment", indices = [Index("person")])
data class LoanPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val person: String,
    /** Reservado (sempre false hoje) — mantido no schema para não forçar migração no dev. */
    val borrowed: Boolean = false,
    val amountCents: Long,
    val createdAt: Long
)

data class BankSummaryRow(
    val bankPackage: String,
    val bankName: String,
    val incomeCents: Long,
    val expenseCents: Long,
    val count: Int
)

data class MonthTotalsRow(
    val incomeCents: Long,
    val expenseCents: Long
)

data class MonthlyTrendRow(
    val yearMonth: String,
    val incomeCents: Long,
    val expenseCents: Long
)

data class PersonBalanceRow(
    val person: String,
    val pendingCents: Long,
    val count: Int
)

data class PersonPaidRow(
    val person: String,
    val paidCents: Long
)

data class CategorySpendRow(
    val category: String,
    val cents: Long
)

data class CounterpartyTotalRow(
    val name: String,
    val cents: Long,
    val count: Int
)
