package com.rfilh.fintracker.core

import com.rfilh.fintracker.domain.model.AlertKind
import com.rfilh.fintracker.domain.model.SpendingAlert
import com.rfilh.fintracker.domain.model.Transaction
import com.rfilh.fintracker.domain.model.TransactionType
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Heurísticas de alerta sobre os gastos recentes, sem persistência — recalculadas a cada
 * abertura da tela. Puro Kotlin, testável.
 *
 * - REAJUSTE: uma recorrência cujo último valor subiu bem acima do que vinha cobrando.
 * - ATÍPICO: uma saída do mês muito acima da média daquela categoria.
 * - RESUMO: quanto gastou nos últimos 7 dias vs. os 7 anteriores.
 */
object SpendingAlerts {

    private const val WEEK_MS = 7 * 86_400_000L

    fun compute(
        transactions: List<Transaction>,
        recurring: List<RecurringItem>,
        now: Long
    ): List<SpendingAlert> {
        val alerts = mutableListOf<SpendingAlert>()
        alerts += reajustes(recurring, now)
        alerts += atipicos(transactions, now)
        resumo(transactions, now)?.let { alerts += it }
        return alerts.sortedByDescending { it.atMillis }
    }

    private fun reajustes(recurring: List<RecurringItem>, now: Long): List<SpendingAlert> =
        recurring.mapNotNull { item ->
            val amounts = item.amountsChrono
            if (amounts.size < 3) return@mapNotNull null
            val previous = amounts.dropLast(1)
            val last = amounts.last()
            val baseline = median(previous)
            if (baseline <= 0) return@mapNotNull null
            val jump = (last - baseline).toDouble() / baseline
            if (jump < 0.15 || last - baseline < 300) return@mapNotNull null
            SpendingAlert(
                kind = AlertKind.REAJUSTE,
                text = "${item.counterparty} subiu de ${Money.format(baseline)} para ${Money.format(last)}.",
                atMillis = item.lastOccurredAt.takeIf { it > 0 } ?: now
            )
        }

    private fun atipicos(transactions: List<Transaction>, now: Long): List<SpendingAlert> {
        val currentMonth = Dates.yearMonth(now)
        val expenses = transactions.filter {
            it.type == TransactionType.EXPENSE && !it.category.isNullOrBlank()
        }
        val historyByCategory = expenses
            .filter { it.yearMonth != currentMonth }
            .groupBy { it.category!! }
            .mapValues { (_, list) -> list.map { abs(it.amountCents) } }

        return expenses
            .filter { it.yearMonth == currentMonth }
            .mapNotNull { tx ->
                val history = historyByCategory[tx.category] ?: return@mapNotNull null
                if (history.size < 4) return@mapNotNull null
                val avg = history.average()
                if (avg <= 0) return@mapNotNull null
                val ratio = abs(tx.amountCents) / avg
                if (ratio < 2.5) return@mapNotNull null
                val label = tx.counterparty ?: tx.bankName
                SpendingAlert(
                    kind = AlertKind.ATIPICO,
                    text = "$label: ${Money.format(abs(tx.amountCents))} é cerca de ${ratio.roundToInt()}x sua média em ${tx.category}.",
                    atMillis = tx.occurredAt
                )
            }
            .sortedByDescending { it.atMillis }
            .take(3)
    }

    private fun resumo(transactions: List<Transaction>, now: Long): SpendingAlert? {
        val thisWeek = transactions.filter {
            it.type == TransactionType.EXPENSE && it.occurredAt in (now - WEEK_MS) until now
        }
        if (thisWeek.isEmpty()) return null
        val lastWeek = transactions.filter {
            it.type == TransactionType.EXPENSE && it.occurredAt in (now - 2 * WEEK_MS) until (now - WEEK_MS)
        }
        val thisTotal = thisWeek.sumOf { abs(it.amountCents) }
        val lastTotal = lastWeek.sumOf { abs(it.amountCents) }
        val biggest = thisWeek.maxByOrNull { abs(it.amountCents) }
        val biggestLabel = biggest?.counterparty ?: biggest?.bankName

        val comparison = when {
            lastTotal <= 0 -> ""
            else -> {
                val delta = ((thisTotal - lastTotal) * 100.0 / lastTotal).roundToInt()
                when {
                    delta > 0 -> " — $delta% a mais que na semana passada"
                    delta < 0 -> " — ${-delta}% a menos que na semana passada"
                    else -> " — mesmo patamar da semana passada"
                }
            }
        }
        val tail = biggestLabel?.let { ". Maior gasto: $it." } ?: "."
        return SpendingAlert(
            kind = AlertKind.RESUMO,
            text = "Você gastou ${Money.format(thisTotal)} nos últimos 7 dias$comparison$tail",
            atMillis = now
        )
    }

    private fun median(values: List<Long>): Long {
        if (values.isEmpty()) return 0
        val sorted = values.sorted()
        val mid = sorted.size / 2
        return if (sorted.size % 2 == 1) sorted[mid] else (sorted[mid - 1] + sorted[mid]) / 2
    }
}
