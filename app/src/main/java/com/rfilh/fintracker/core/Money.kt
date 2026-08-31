package com.rfilh.fintracker.core

import java.text.NumberFormat
import java.util.Locale

object Money {
    private val BR = Locale("pt", "BR")

    fun format(cents: Long): String =
        NumberFormat.getCurrencyInstance(BR).format(cents / 100.0)

    /** "1.234,56" | "1234,56" | "1234.56" -> 123456 */
    fun parseBrlToCents(raw: String): Long? {
        val cleaned = raw.trim().replace("R$", "").trim()
        val normalized = when {
            cleaned.contains(',') -> cleaned.replace(".", "").replace(',', '.')
            else -> cleaned
        }
        return normalized.toDoubleOrNull()?.let { Math.round(it * 100.0) }
    }
}
