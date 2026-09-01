package com.rfilh.fintracker.data.repo

import com.rfilh.fintracker.domain.model.Confidence
import com.rfilh.fintracker.domain.model.LoanStatus
import com.rfilh.fintracker.domain.model.Transaction
import com.rfilh.fintracker.domain.model.TransactionType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * RF20: backup/restauração em JSON. DTO próprio (enums como String) para não acoplar
 * o modelo de domínio a kotlinx.serialization.
 */
object BackupCodec {
    private const val VERSION = 1
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    @Serializable
    private data class TransactionDto(
        val bankPackage: String,
        val bankName: String,
        val type: String,
        val amountCents: Long,
        val counterparty: String?,
        val occurredAt: Long,
        val yearMonth: String,
        val confidence: String,
        val reviewed: Boolean,
        val category: String?,
        val note: String?,
        val loanStatus: String,
        val loanPerson: String?,
        val loanAmountCents: Long?,
        val manual: Boolean = false
    )

    @Serializable
    private data class BackupFile(
        val version: Int = VERSION,
        val exportedAt: Long,
        val transactions: List<TransactionDto>
    )

    fun encode(transactions: List<Transaction>, exportedAt: Long = System.currentTimeMillis()): String {
        val dtos = transactions.map { t ->
            TransactionDto(
                bankPackage = t.bankPackage,
                bankName = t.bankName,
                type = t.type.name,
                amountCents = t.amountCents,
                counterparty = t.counterparty,
                occurredAt = t.occurredAt,
                yearMonth = t.yearMonth,
                confidence = t.confidence.name,
                reviewed = t.reviewed,
                category = t.category,
                note = t.note,
                loanStatus = t.loanStatus.name,
                loanPerson = t.loanPerson,
                loanAmountCents = t.loanAmountCents,
                manual = t.manual
            )
        }
        return json.encodeToString(BackupFile.serializer(), BackupFile(exportedAt = exportedAt, transactions = dtos))
    }

    fun decode(raw: String): List<Transaction> {
        val file = json.decodeFromString(BackupFile.serializer(), raw)
        return file.transactions.map { d ->
            Transaction(
                bankPackage = d.bankPackage,
                bankName = d.bankName,
                type = runCatching { TransactionType.valueOf(d.type) }.getOrDefault(TransactionType.UNKNOWN),
                amountCents = d.amountCents,
                counterparty = d.counterparty,
                occurredAt = d.occurredAt,
                yearMonth = d.yearMonth,
                confidence = runCatching { Confidence.valueOf(d.confidence) }.getOrDefault(Confidence.LOW),
                reviewed = d.reviewed,
                category = d.category,
                note = d.note,
                loanStatus = runCatching { LoanStatus.valueOf(d.loanStatus) }.getOrDefault(LoanStatus.NONE),
                loanPerson = d.loanPerson,
                loanAmountCents = d.loanAmountCents,
                manual = d.manual
            )
        }
    }
}
