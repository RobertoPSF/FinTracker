package com.rfilh.fintracker.parser

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class CategoryRule(
    val name: String,
    val keywords: List<String> = emptyList()
)

@Serializable
data class GenericRules(
    val ignore: List<String> = emptyList(),
    /** Sinais fortes de que a transação não se efetivou (agendada/recusada/pendente) — bloqueiam
     *  a classificação mesmo que um keyword de income/expense também bata. */
    val notCompleted: List<String> = emptyList(),
    val income: List<String> = emptyList(),
    val expense: List<String> = emptyList(),
    val amountPatterns: List<String> = emptyList(),
    val counterpartyPatterns: List<String> = emptyList(),
    val categories: List<CategoryRule> = emptyList()
)

/**
 * Regra específica de um app (`package`) já conhecido. É uma otimização do parser genérico
 * (nome bonito, padrões de contraparte mais precisos), nunca um pré-requisito: um app sem
 * entrada aqui ainda é processado normalmente pelas regras de `GenericRules`.
 */
@Serializable
data class AppOverride(
    val `package`: String,
    val name: String,
    val ignore: List<String> = emptyList(),
    val notCompleted: List<String> = emptyList(),
    val income: List<String> = emptyList(),
    val expense: List<String> = emptyList(),
    val amountPatterns: List<String> = emptyList(),
    val counterpartyPatterns: List<String> = emptyList()
)

@Serializable
data class ParserRules(
    val version: Int = 1,
    val generic: GenericRules = GenericRules(),
    val overrides: List<AppOverride> = emptyList()
) {
    private val byPackage: Map<String, AppOverride> by lazy { overrides.associateBy { it.`package` } }

    fun overrideFor(pkg: String): AppOverride? = byPackage[pkg]

    fun hasOverride(pkg: String): Boolean = byPackage.containsKey(pkg)

    fun packages(): Set<String> = byPackage.keys

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        fun fromJson(raw: String): ParserRules = json.decodeFromString(serializer(), raw)
    }
}
