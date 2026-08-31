package com.rfilh.fintracker.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class DatesTest {
    @Test fun `shift atravessa ano`() {
        assertEquals("2025-12", Dates.shift("2026-01", -1))
        assertEquals("2027-01", Dates.shift("2026-12", 1))
        assertEquals("2026-08", Dates.shift("2026-08", 0))
    }
}

class HashingTest {
    @Test fun `mesma notificacao na mesma janela gera hash igual`() {
        val a = Hashing.notificationHash("com.nu.production", "Nubank", "Pix R$ 10,00", 1_700_000_000_000)
        val b = Hashing.notificationHash("com.nu.production", "Nubank", "Pix R$ 10,00", 1_700_000_005_000)
        assertEquals(a, b)
    }

    @Test fun `conteudo diferente gera hash diferente`() {
        val a = Hashing.notificationHash("com.nu.production", "Nubank", "Pix R$ 10,00", 1_700_000_000_000)
        val b = Hashing.notificationHash("com.nu.production", "Nubank", "Pix R$ 20,00", 1_700_000_000_000)
        assertNotEquals(a, b)
    }
}

class TextNormalizerTest {
    @Test fun `remove acentos e caixa`() {
        assertEquals("transferencia recebida", TextNormalizer.normalize("  Transferência   RECEBIDA "))
    }
}
