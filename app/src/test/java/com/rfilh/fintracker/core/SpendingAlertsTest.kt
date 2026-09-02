package com.rfilh.fintracker.core

import com.rfilh.fintracker.domain.model.AlertKind
import com.rfilh.fintracker.domain.model.Confidence
import com.rfilh.fintracker.domain.model.Transaction
import com.rfilh.fintracker.domain.model.TransactionType
import org.junit.Assert.assertTrue
import org.junit.Test

class SpendingAlertsTest {

    private val now = 1_756_000_000_000L // instante fixo de referência
    private val dayMs = 86_400_000L

    private fun expense(cents: Long, at: Long, category: String? = null, counterparty: String? = "Loja") = Transaction(
        bankPackage = "com.nu.production",
        bankName = "Nubank",
        type = TransactionType.EXPENSE,
        amountCents = cents,
        counterparty = counterparty,
        occurredAt = at,
        yearMonth = Dates.yearMonth(at),
        confidence = Confidence.HIGH,
        reviewed = true,
        category = category
    )

    @Test
    fun `reajuste de recorrente vira alerta`() {
        val recurring = listOf(
            RecurringItem(
                counterparty = "Spotify",
                avgAmountCents = 2600,
                monthsSeen = listOf("2026-06", "2026-07", "2026-08"),
                lastOccurredAt = now,
                amountsChrono = listOf(2190, 2190, 3490)
            )
        )
        val alerts = SpendingAlerts.compute(emptyList(), recurring, now)
        val reajuste = alerts.single { it.kind == AlertKind.REAJUSTE }
        assertTrue(reajuste.text.contains("Spotify"))
    }

    @Test
    fun `variacao pequena nao vira reajuste`() {
        val recurring = listOf(
            RecurringItem(
                counterparty = "Netflix",
                avgAmountCents = 3990,
                monthsSeen = listOf("2026-06", "2026-07", "2026-08"),
                amountsChrono = listOf(3990, 3990, 4090)
            )
        )
        assertTrue(SpendingAlerts.compute(emptyList(), recurring, now).none { it.kind == AlertKind.REAJUSTE })
    }

    @Test
    fun `resumo semanal aparece quando ha gasto na semana`() {
        val txs = listOf(
            expense(10000, now - 2 * dayMs),
            expense(30000, now - 3 * dayMs, counterparty = "Mercado"),
            expense(20000, now - 10 * dayMs)
        )
        val resumo = SpendingAlerts.compute(txs, emptyList(), now).single { it.kind == AlertKind.RESUMO }
        assertTrue(resumo.text.contains("Mercado"))
    }

    @Test
    fun `gasto muito acima da media da categoria vira atipico`() {
        val history = (1..5).map { expense(5000, now - (40 + it) * dayMs, category = "Alimentação") }
        val outlier = expense(30000, now - 1 * dayMs, category = "Alimentação", counterparty = "Pão de Açúcar")
        val alerts = SpendingAlerts.compute(history + outlier, emptyList(), now)
        val atipico = alerts.single { it.kind == AlertKind.ATIPICO }
        assertTrue(atipico.text.contains("Pão de Açúcar"))
        assertTrue(atipico.text.contains("Alimentação"))
    }
}
