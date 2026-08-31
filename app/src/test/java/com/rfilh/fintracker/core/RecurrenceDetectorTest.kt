package com.rfilh.fintracker.core

import com.rfilh.fintracker.domain.model.Confidence
import com.rfilh.fintracker.domain.model.Transaction
import com.rfilh.fintracker.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecurrenceDetectorTest {

    private fun expense(counterparty: String, amountCents: Long, yearMonth: String) = Transaction(
        bankPackage = "com.nu.production",
        bankName = "Nubank",
        type = TransactionType.EXPENSE,
        amountCents = amountCents,
        counterparty = counterparty,
        occurredAt = 0L,
        yearMonth = yearMonth,
        confidence = Confidence.HIGH,
        reviewed = true
    )

    @Test
    fun `assinatura em 3 meses consecutivos e detectada como recorrente`() {
        val transactions = listOf(
            expense("Netflix", 3990, "2026-06"),
            expense("Netflix", 3990, "2026-07"),
            expense("Netflix", 3990, "2026-08")
        )
        val result = RecurrenceDetector.detect(transactions)
        assertEquals(1, result.size)
        assertEquals("Netflix", result[0].counterparty)
        assertEquals(listOf("2026-06", "2026-07", "2026-08"), result[0].monthsSeen)
    }

    @Test
    fun `compra unica nao e recorrente`() {
        val transactions = listOf(expense("Loja X", 5000, "2026-08"))
        assertTrue(RecurrenceDetector.detect(transactions).isEmpty())
    }

    @Test
    fun `mesmo mes nao conta como consecutivo`() {
        val transactions = listOf(
            expense("Farmacia", 2000, "2026-08"),
            expense("Farmacia", 2000, "2026-08")
        )
        assertTrue(RecurrenceDetector.detect(transactions).isEmpty())
    }

    @Test
    fun `valor com pequena variacao ainda conta como recorrente`() {
        val transactions = listOf(
            expense("Academia", 10000, "2026-06"),
            expense("Academia", 10200, "2026-07")
        )
        assertEquals(1, RecurrenceDetector.detect(transactions).size)
    }
}
