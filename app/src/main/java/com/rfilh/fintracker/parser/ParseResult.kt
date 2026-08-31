package com.rfilh.fintracker.parser

import com.rfilh.fintracker.domain.model.Confidence
import com.rfilh.fintracker.domain.model.TransactionType

sealed interface ParseResult {
    /** Notificação irrelevante (marketing, fatura, saldo). Não vira transação. */
    data object Ignored : ParseResult

    data class Parsed(
        val bankName: String,
        val type: TransactionType,
        val amountCents: Long?,
        val counterparty: String?,
        val confidence: Confidence,
        val matchedKeyword: String?,
        val category: String? = null
    ) : ParseResult
}
