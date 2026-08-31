package com.rfilh.fintracker.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AnonymizerTest {

    @Test
    fun `mascara sequencias de digitos com 2 ou mais algarismos`() {
        assertEquals(
            "Valor R\$ 1.#,# pago em #/#/#",
            Anonymizer.anonymize("Valor R\$ 1.234,56 pago em 31/08/2026")
        )
    }

    @Test
    fun `mascara nome com duas palavras capitalizadas seguidas`() {
        assertEquals(
            "[NOME] enviou R\$ #,#",
            Anonymizer.anonymize("João Silva enviou R\$ 50,00")
        )
    }

    @Test
    fun `palavra capitalizada isolada nao e tratada como nome`() {
        assertEquals(
            "Compra aprovada no MERCADO",
            Anonymizer.anonymize("Compra aprovada no MERCADO")
        )
    }

    @Test
    fun `texto nulo retorna nulo`() {
        assertNull(Anonymizer.anonymize(null))
    }
}
