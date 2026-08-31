package com.rfilh.fintracker.core

/**
 * RF26: mascara PII (dígitos, nomes prováveis) de notificações não reconhecidas antes de
 * exportar, mantendo palavras-chave/estrutura úteis para calibrar o parser_rules.json.
 */
object Anonymizer {
    private val DIGITS = Regex("\\d{2,}")
    private val LIKELY_NAME = Regex("\\b(?:[A-ZÀ-Ý][a-zà-ÿ]+(?:\\s+[A-ZÀ-Ý][a-zà-ÿ]+){1,4})\\b")

    fun anonymize(text: String?): String? {
        text ?: return null
        return LIKELY_NAME.replace(DIGITS.replace(text, "#"), "[NOME]")
    }
}
