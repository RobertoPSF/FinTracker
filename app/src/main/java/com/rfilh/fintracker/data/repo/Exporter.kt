package com.rfilh.fintracker.data.repo

import com.rfilh.fintracker.domain.model.Transaction

/** Fase 7: serialização simples, sem dependência de Android (testável). */
object Exporter {

    private const val HEADER =
        "id,bankPackage,bankName,type,amountCents,counterparty,occurredAt,yearMonth," +
            "confidence,reviewed,category,note,loanStatus,loanPerson,loanAmountCents"

    fun toCsv(transactions: List<Transaction>): String = buildString {
        appendLine(HEADER)
        transactions.forEach { t ->
            appendLine(
                listOf(
                    t.id.toString(), t.bankPackage, t.bankName, t.type.name, t.amountCents.toString(),
                    t.counterparty.orEmpty(), t.occurredAt.toString(), t.yearMonth, t.confidence.name,
                    t.reviewed.toString(), t.category.orEmpty(), t.note.orEmpty(), t.loanStatus.name,
                    t.loanPerson.orEmpty(), t.loanAmountCents?.toString().orEmpty()
                ).joinToString(",") { escape(it) }
            )
        }
    }

    private fun escape(value: String): String =
        if (value.any { it == ',' || it == '"' || it == '\n' })
            "\"" + value.replace("\"", "\"\"") + "\""
        else value
}
