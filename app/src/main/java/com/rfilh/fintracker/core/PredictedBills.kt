package com.rfilh.fintracker.core

import com.rfilh.fintracker.domain.model.PredictedBill
import java.util.Calendar
import java.util.TimeZone

/**
 * Transforma os gastos recorrentes já detectados ([RecurrenceDetector]) numa agenda de contas
 * previstas: para cada recorrência, projeta a próxima data de cobrança a partir do dia do mês
 * típico e calcula quantos dias faltam. Puro Kotlin (só `java.util.Calendar`), testável.
 */
object PredictedBills {

    fun forecast(recurring: List<RecurringItem>, now: Long): List<PredictedBill> {
        val startOfToday = atStartOfDay(now)
        return recurring.map { item ->
            val due = nextDue(item.typicalDayOfMonth, startOfToday)
            PredictedBill(
                name = item.counterparty,
                amountCents = item.avgAmountCents,
                dueAt = due,
                daysUntil = ((due - startOfToday) / DAY_MS).toInt()
            )
        }.sortedBy { it.dueAt }
    }

    private fun nextDue(dayOfMonth: Int, startOfToday: Long): Long {
        val c = Calendar.getInstance(TimeZone.getDefault()).apply { timeInMillis = startOfToday }
        fun setDay(cal: Calendar) {
            val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth.coerceIn(1, maxDay))
        }
        setDay(c)
        if (c.timeInMillis < startOfToday) {
            c.timeInMillis = startOfToday
            c.add(Calendar.MONTH, 1)
            setDay(c)
        }
        return c.timeInMillis
    }

    private fun atStartOfDay(millis: Long): Long =
        Calendar.getInstance(TimeZone.getDefault()).apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private const val DAY_MS = 86_400_000L
}
