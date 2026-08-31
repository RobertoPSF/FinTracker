package com.rfilh.fintracker.data.repo

import com.rfilh.fintracker.domain.model.Confidence
import com.rfilh.fintracker.domain.model.LoanStatus
import com.rfilh.fintracker.domain.model.Transaction
import com.rfilh.fintracker.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupCodecTest {

    @Test
    fun `encode e decode preservam os dados da transacao`() {
        val original = listOf(
            Transaction(
                bankPackage = "com.nu.production",
                bankName = "Nubank",
                type = TransactionType.EXPENSE,
                amountCents = 3990,
                counterparty = "Netflix",
                occurredAt = 1_700_000_000_000,
                yearMonth = "2026-08",
                confidence = Confidence.HIGH,
                reviewed = true,
                category = "Assinaturas",
                note = "mensal",
                loanStatus = LoanStatus.NONE,
                loanPerson = null,
                loanAmountCents = null
            )
        )

        val json = BackupCodec.encode(original, exportedAt = 1_700_000_000_000)
        val decoded = BackupCodec.decode(json)

        assertEquals(1, decoded.size)
        assertEquals(original[0].copy(id = 0), decoded[0])
    }
}
