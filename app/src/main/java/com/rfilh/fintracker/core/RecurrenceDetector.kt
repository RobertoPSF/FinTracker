package com.rfilh.fintracker.core

import com.rfilh.fintracker.domain.model.Transaction
import com.rfilh.fintracker.domain.model.TransactionType

data class RecurringItem(
    val counterparty: String,
    val avgAmountCents: Long,
    val monthsSeen: List<String>,
    /** Dia do mês típico em que a cobrança cai (mediana), para a agenda de contas previstas. */
    val typicalDayOfMonth: Int = 1,
    /** Instante da ocorrência mais recente do cluster. */
    val lastOccurredAt: Long = 0L,
    /** Valores individuais das ocorrências, em ordem cronológica — base para detectar reajuste. */
    val amountsChrono: List<Long> = emptyList()
)

/**
 * RF24: heurística simples, sem persistência — agrupa despesas por contraparte normalizada
 * com valor dentro de uma tolerância, e considera recorrente quem aparece em >= 2 meses
 * consecutivos.
 */
object RecurrenceDetector {
    private const val TOLERANCE = 0.05

    fun detect(transactions: List<Transaction>): List<RecurringItem> {
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE && !it.counterparty.isNullOrBlank() }
        val byCounterparty = expenses.groupBy { TextNormalizer.normalize(it.counterparty.orEmpty()) }

        return byCounterparty.mapNotNull { (_, group) ->
            val clusters = clusterByAmount(group)
            clusters.filter { hasConsecutiveMonths(it) }
                .maxByOrNull { it.size }
                ?.let { cluster ->
                    val chrono = cluster.sortedBy { it.occurredAt }
                    val days = chrono.map { Dates.dayOfMonth(it.occurredAt) }.filter { it in 1..31 }.sorted()
                    RecurringItem(
                        counterparty = cluster.first().counterparty.orEmpty(),
                        avgAmountCents = cluster.map { it.amountCents }.average().toLong(),
                        monthsSeen = cluster.map { it.yearMonth }.distinct().sorted(),
                        typicalDayOfMonth = if (days.isEmpty()) 1 else days[days.size / 2],
                        lastOccurredAt = chrono.last().occurredAt,
                        amountsChrono = chrono.map { it.amountCents }
                    )
                }
        }.sortedByDescending { it.monthsSeen.size }
    }

    private fun clusterByAmount(group: List<Transaction>): List<List<Transaction>> {
        val remaining = group.sortedBy { it.amountCents }.toMutableList()
        val clusters = mutableListOf<List<Transaction>>()
        while (remaining.isNotEmpty()) {
            val anchor = remaining.removeAt(0)
            val cluster = mutableListOf(anchor)
            val tolerance = anchor.amountCents * TOLERANCE
            remaining.removeAll { candidate ->
                (kotlin.math.abs(candidate.amountCents - anchor.amountCents) <= tolerance).also {
                    if (it) cluster.add(candidate)
                }
            }
            clusters.add(cluster)
        }
        return clusters
    }

    private fun hasConsecutiveMonths(cluster: List<Transaction>): Boolean {
        val months = cluster.map { it.yearMonth }.distinct().sorted()
        if (months.size < 2) return false
        return months.zipWithNext().any { (a, b) -> Dates.shift(a, 1) == b }
    }
}
