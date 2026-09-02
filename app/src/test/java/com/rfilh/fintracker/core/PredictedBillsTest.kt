package com.rfilh.fintracker.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class PredictedBillsTest {

    private fun millis(year: Int, month1: Int, day: Int): Long =
        Calendar.getInstance(TimeZone.getDefault()).apply {
            clear()
            set(year, month1 - 1, day, 12, 0)
        }.timeInMillis

    private fun recurring(name: String, day: Int) = RecurringItem(
        counterparty = name,
        avgAmountCents = 3990,
        monthsSeen = listOf("2026-06", "2026-07", "2026-08"),
        typicalDayOfMonth = day,
        lastOccurredAt = millis(2026, 8, day)
    )

    @Test
    fun `dia ainda por vir no mes corrente e projetado para este mes`() {
        val now = millis(2026, 9, 3)
        val bill = PredictedBills.forecast(listOf(recurring("Netflix", 8)), now).single()
        assertEquals(5, bill.daysUntil)
        assertTrue(bill.soon)
    }

    @Test
    fun `dia ja passou no mes vira o proximo mes`() {
        val now = millis(2026, 9, 20)
        val bill = PredictedBills.forecast(listOf(recurring("Aluguel", 5)), now).single()
        assertEquals(Calendar.OCTOBER, monthOf(bill.dueAt))
        assertTrue(bill.daysUntil in 14..16)
    }

    @Test
    fun `ordena por data de vencimento`() {
        val now = millis(2026, 9, 1)
        val bills = PredictedBills.forecast(
            listOf(recurring("C", 25), recurring("A", 5), recurring("B", 12)),
            now
        )
        assertEquals(listOf("A", "B", "C"), bills.map { it.name })
    }

    private fun monthOf(millis: Long): Int =
        Calendar.getInstance(TimeZone.getDefault()).apply { timeInMillis = millis }.get(Calendar.MONTH)
}
