package com.rfilh.fintracker.parser

import com.rfilh.fintracker.core.Money
import com.rfilh.fintracker.core.TextNormalizer
import com.rfilh.fintracker.domain.model.Confidence
import com.rfilh.fintracker.domain.model.TransactionType

/**
 * Classificação híbrida: keywords -> tipo, regex -> valor/contraparte.
 * Puro Kotlin (sem dependência de Android) para permitir teste unitário direto.
 */
class TransactionClassifier(private val rules: ParserRules) {

    fun classify(packageName: String, title: String?, body: String?): ParseResult {
        val bank = rules.bank(packageName)
        val bankName = bank?.name ?: packageName
        val rawText = listOfNotNull(title, body).joinToString(" ")
        if (rawText.isBlank()) return ParseResult.Ignored
        val text = TextNormalizer.normalize(rawText)

        val income = (bank?.income.orEmpty() + rules.generic.income).map { TextNormalizer.normalize(it) }
        val expense = (bank?.expense.orEmpty() + rules.generic.expense).map { TextNormalizer.normalize(it) }

        val incomeHit = income.filter { text.contains(it) }.minByOrNull { text.indexOf(it) }
        val expenseHit = expense.filter { text.contains(it) }.minByOrNull { text.indexOf(it) }

        // desempate: keyword que aparece antes no texto vence
        val type: TransactionType
        val matched: String?
        when {
            incomeHit != null && expenseHit != null ->
                if (text.indexOf(incomeHit) <= text.indexOf(expenseHit)) {
                    type = TransactionType.INCOME; matched = incomeHit
                } else {
                    type = TransactionType.EXPENSE; matched = expenseHit
                }
            incomeHit != null -> { type = TransactionType.INCOME; matched = incomeHit }
            expenseHit != null -> { type = TransactionType.EXPENSE; matched = expenseHit }
            else -> { type = TransactionType.UNKNOWN; matched = null }
        }

        // sem tipo reconhecido: descarta ruído (marketing, fatura, saldo)
        if (type == TransactionType.UNKNOWN) {
            val ignore = (bank?.ignore.orEmpty() + rules.generic.ignore).map { TextNormalizer.normalize(it) }
            if (ignore.any { text.contains(it) }) return ParseResult.Ignored
        }

        val amount = extractAmount(text, bank)
        if (type == TransactionType.UNKNOWN && amount == null) return ParseResult.Ignored

        val counterparty = extractCounterparty(rawText, text, bank)

        val confidence = when {
            type != TransactionType.UNKNOWN && amount != null && bank != null -> Confidence.HIGH
            type != TransactionType.UNKNOWN && amount != null -> Confidence.MEDIUM
            else -> Confidence.LOW
        }

        return ParseResult.Parsed(bankName, type, amount, counterparty, confidence, matched)
    }

    private fun amountPatterns(bank: BankRules?): List<Regex> =
        (bank?.amountPatterns.orEmpty() + rules.generic.amountPatterns)
            .map { Regex(it, RegexOption.IGNORE_CASE) }

    private fun extractAmount(text: String, bank: BankRules?): Long? {
        for (pattern in amountPatterns(bank)) {
            val group = pattern.find(text)?.groupValues?.getOrNull(1) ?: continue
            Money.parseBrlToCents(group)?.let { return it }
        }
        return null
    }

    private fun extractCounterparty(rawText: String, normalized: String, bank: BankRules?): String? {
        val patterns = (bank?.counterpartyPatterns.orEmpty() + rules.generic.counterpartyPatterns)
        for (raw in patterns) {
            val match = Regex(raw, setOf(RegexOption.IGNORE_CASE)).find(normalized) ?: continue
            val candidate = match.groupValues.getOrNull(1)?.trim() ?: continue
            val cleaned = cleanCounterparty(candidate)
            if (cleaned != null) return restoreCase(cleaned, rawText)
        }
        return null
    }

    private fun cleanCounterparty(candidate: String): String? {
        var value = candidate
            .replace(Regex("r\\$\\s?[0-9.,]+"), " ")
            .replace(Regex("\\b(hoje|agora|ontem|as \\d{1,2}:\\d{2})\\b"), " ")
            .replace(Regex("\\s+"), " ")
            .trim(' ', '.', ',', ':', ';', '-')
        if (value.length < 2 || value.length > 40) return null
        if (value.all { it.isDigit() }) return null
        return value
    }

    /** Recupera a grafia original (acentos/maiúsculas) quando possível. */
    private fun restoreCase(normalizedName: String, rawText: String): String {
        val words = normalizedName.split(" ").size
        val tokens = rawText.split(Regex("\\s+"))
        for (start in tokens.indices) {
            if (start + words > tokens.size) break
            val slice = tokens.subList(start, start + words).joinToString(" ")
            val cleanSlice = slice.trim('.', ',', ':', ';', '!', '-')
            if (TextNormalizer.normalize(cleanSlice) == normalizedName) return cleanSlice
        }
        return normalizedName
    }
}
