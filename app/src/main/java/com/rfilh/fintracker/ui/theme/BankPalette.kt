package com.rfilh.fintracker.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Cor por banco: o protótipo só define cor pros 3 bancos de exemplo (Nubank/Inter/Itaú).
 * Para os ~16 bancos reais do parser_rules.json, deriva determinística por hash do pacote
 * sobre uma paleta fixa — sem precisar mudar o schema do JSON.
 */
object BankPalette {
    private val KNOWN = mapOf(
        "com.nu.production" to Color(0xFF8A05BE),
        "br.com.intermedium" to Color(0xFFFF7A00),
        "com.itau" to Color(0xFFEC7000),
        "com.itau.iaccount" to Color(0xFFEC7000)
    )

    private val PALETTE = listOf(
        Color(0xFF1B3A5C), Color(0xFF2E7D6B), Color(0xFFB3541E), Color(0xFF6B4EA0),
        Color(0xFF1E7A8C), Color(0xFF9C4A6B), Color(0xFF4A7A2E), Color(0xFF8A5A2E),
        Color(0xFF5A5A9E), Color(0xFF2E8A6B)
    )

    fun colorFor(bankPackage: String): Color =
        KNOWN[bankPackage] ?: PALETTE[Math.floorMod(bankPackage.hashCode(), PALETTE.size)]

    fun initialFor(bankName: String): String =
        bankName.trim().split(Regex("\\s+")).take(2).joinToString("") { it.take(1).uppercase() }.ifEmpty { "?" }
}
