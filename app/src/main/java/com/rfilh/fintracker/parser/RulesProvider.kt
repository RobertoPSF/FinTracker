package com.rfilh.fintracker.parser

import android.content.Context
import java.io.File

/**
 * Carrega regras de: (1) arquivo externo em filesDir (atualizável sem release),
 * (2) fallback para o asset embarcado.
 */
object RulesProvider {
    private const val ASSET = "parser_rules.json"
    private const val OVERRIDE = "parser_rules_override.json"

    @Volatile private var cached: ParserRules? = null

    fun get(context: Context): ParserRules = cached ?: synchronized(this) {
        cached ?: load(context).also { cached = it }
    }

    fun invalidate() { cached = null }

    fun overrideFile(context: Context): File = File(context.filesDir, OVERRIDE)

    private fun load(context: Context): ParserRules {
        val override = overrideFile(context)
        val raw = runCatching {
            if (override.exists()) override.readText()
            else context.assets.open(ASSET).bufferedReader().use { it.readText() }
        }.getOrNull() ?: return ParserRules()
        return runCatching { ParserRules.fromJson(raw) }.getOrElse { ParserRules() }
    }
}
