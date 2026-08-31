package com.rfilh.fintracker.parser

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GenericRules(
    val ignore: List<String> = emptyList(),
    val income: List<String> = emptyList(),
    val expense: List<String> = emptyList(),
    val amountPatterns: List<String> = emptyList(),
    val counterpartyPatterns: List<String> = emptyList()
)

@Serializable
data class BankRules(
    val `package`: String,
    val name: String,
    val ignore: List<String> = emptyList(),
    val income: List<String> = emptyList(),
    val expense: List<String> = emptyList(),
    val amountPatterns: List<String> = emptyList(),
    val counterpartyPatterns: List<String> = emptyList()
)

@Serializable
data class ParserRules(
    val version: Int = 1,
    val generic: GenericRules = GenericRules(),
    val banks: List<BankRules> = emptyList()
) {
    private val byPackage: Map<String, BankRules> by lazy { banks.associateBy { it.`package` } }

    fun bank(pkg: String): BankRules? = byPackage[pkg]

    fun isKnownBank(pkg: String): Boolean = byPackage.containsKey(pkg)

    fun packages(): Set<String> = byPackage.keys

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        fun fromJson(raw: String): ParserRules = json.decodeFromString(serializer(), raw)
    }
}
