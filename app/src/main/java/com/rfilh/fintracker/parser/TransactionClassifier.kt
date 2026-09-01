package com.rfilh.fintracker.parser

import com.rfilh.fintracker.core.Money
import com.rfilh.fintracker.core.TextNormalizer
import com.rfilh.fintracker.domain.model.Confidence
import com.rfilh.fintracker.domain.model.TransactionType

/**
 * Classificação híbrida: keywords -> tipo, regex -> valor/contraparte.
 * Puro Kotlin (sem dependência de Android) para permitir teste unitário direto.
 *
 * Conhecer o app via `overrides` é uma otimização (nome bonito, padrões mais precisos), nunca
 * um pré-requisito: um app sem entrada em `parser_rules.json` ainda é classificado normalmente
 * pelas regras de `GenericRules`, só com confiança MEDIUM em vez de HIGH.
 */
class TransactionClassifier(private val rules: ParserRules) {

    /**
     * @param appLabel nome do app resolvido pelo `PackageManager` (ex. via `getApplicationLabel`)
     *   — usado como nome de exibição quando não há override cadastrado, em vez do package cru.
     */
    fun classify(packageName: String, title: String?, body: String?, appLabel: String? = null): ParseResult {
        val override = rules.overrideFor(packageName)
        val displayName = override?.name ?: appLabel ?: packageName
        val rawText = listOfNotNull(title, body).joinToString(" ")
        if (rawText.isBlank()) return ParseResult.Ignored
        val text = TextNormalizer.normalize(rawText)

        // Sinal de "não efetivado" (agendado/recusado/pendente/cancelado) vence qualquer keyword
        // de tipo — evita registrar como transação real algo que nunca chegou a acontecer.
        val notCompleted = (override?.notCompleted.orEmpty() + rules.generic.notCompleted).map { TextNormalizer.normalize(it) }
        if (notCompleted.any { text.contains(it) }) return ParseResult.Ignored

        val income = (override?.income.orEmpty() + rules.generic.income).map { TextNormalizer.normalize(it) }
        val expense = (override?.expense.orEmpty() + rules.generic.expense).map { TextNormalizer.normalize(it) }

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
            val ignore = (override?.ignore.orEmpty() + rules.generic.ignore).map { TextNormalizer.normalize(it) }
            if (ignore.any { text.contains(it) }) return ParseResult.Ignored
        }

        val amount = extractAmount(text, override)
        if (type == TransactionType.UNKNOWN && amount == null) return ParseResult.Ignored

        val counterparty = extractCounterparty(rawText, text, override)
        val category = extractCategory(text)
        val cardType = extractCardType(text)

        val confidence = when {
            type != TransactionType.UNKNOWN && amount != null && override != null -> Confidence.HIGH
            type != TransactionType.UNKNOWN && amount != null -> Confidence.MEDIUM
            else -> Confidence.LOW
        }

        return ParseResult.Parsed(displayName, type, amount, counterparty, confidence, matched, category, cardType)
    }

    /** "Crédito"/"Débito" quando o texto menciona a modalidade do cartão, em qualquer ordem
     *  ("cartão ... crédito" ou "crédito no cartão") — vocabulário fixo, não depende de banco. */
    private fun extractCardType(text: String): String? {
        val match = CARD_TYPE_PATTERN.find(text) ?: CARD_TYPE_PATTERN_REVERSED.find(text)
        return when (match?.groupValues?.getOrNull(1)) {
            "credito" -> "Crédito"
            "debito" -> "Débito"
            else -> null
        }
    }

    /** Primeira categoria cuja keyword aparece no texto vence (ordem do parser_rules.json). */
    private fun extractCategory(text: String): String? =
        rules.generic.categories.firstOrNull { rule ->
            rule.keywords.any { text.contains(TextNormalizer.normalize(it)) }
        }?.name

    private fun amountPatterns(override: AppOverride?): List<Regex> =
        (override?.amountPatterns.orEmpty() + rules.generic.amountPatterns)
            .map { Regex(it, RegexOption.IGNORE_CASE) }

    private fun extractAmount(text: String, override: AppOverride?): Long? {
        for (pattern in amountPatterns(override)) {
            val group = pattern.find(text)?.groupValues?.getOrNull(1) ?: continue
            Money.parseBrlToCents(group)?.let { return it }
        }
        return null
    }

    private fun extractCounterparty(rawText: String, normalized: String, override: AppOverride?): String? {
        val patterns = (override?.counterpartyPatterns.orEmpty() + rules.generic.counterpartyPatterns)
        for (raw in patterns) {
            val regex = Regex(raw, setOf(RegexOption.IGNORE_CASE))
            // Tenta todas as ocorrências do padrão, não só a primeira: sem o "$" no fim (removido
            // pra funcionar com texto que continua depois do nome, ex. ponto final), a primeira
            // ocorrência pode ser lixo (ex. "de" colado no valor) e a de verdade vir depois.
            for (match in regex.findAll(normalized)) {
                val candidate = match.groupValues.getOrNull(1)?.trim() ?: continue
                val cleaned = cleanCounterparty(candidate)
                if (cleaned != null) return restoreCase(cleaned, rawText)
            }
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
        // "(na/a/sua) conta Corrente/Poupança" (a própria conta do usuário) não é contraparte,
        // não importa o que vem antes de "conta" — "na sua conta" é tão inválido quanto "conta".
        if (value.contains(CONTA_WORD)) return null
        // "seu Cartão Mercado Pago Crédito" (modalidade do pagamento, não quem recebeu/enviou).
        if (value.contains(CARTAO_WORD)) return null
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

    private companion object {
        /** "cartão ... crédito" (ou vice-versa) — vocabulário fixo, não varia por banco. */
        val CARD_TYPE_PATTERN = Regex("\\bcartao\\b.{0,40}?\\b(credito|debito)\\b")
        val CARD_TYPE_PATTERN_REVERSED = Regex("\\b(credito|debito)\\b.{0,40}?\\bcartao\\b")
        val CONTA_WORD = Regex("\\bconta\\b")
        val CARTAO_WORD = Regex("\\bcartao\\b")
    }
}
