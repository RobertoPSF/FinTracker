package com.rfilh.fintracker.core

import java.text.Normalizer
import java.util.Locale

object TextNormalizer {
    private val COMBINING = Regex("\\p{InCombiningDiacriticalMarks}+")

    /** minúsculas, sem acentos, espaços colapsados */
    fun normalize(input: String): String {
        val decomposed = Normalizer.normalize(input, Normalizer.Form.NFD)
        return COMBINING.replace(decomposed, "")
            .lowercase(Locale.ROOT)
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
