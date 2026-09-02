package com.rfilh.fintracker.data.db

import android.content.Context

/** Flags de comportamento, não sensíveis (RF21). */
object AppSettings {
    private const val FILE = "fintracker_prefs"
    private const val AUTO_PURGE_RAW = "auto_purge_raw"
    private const val DARK_MODE = "dark_mode"
    private const val DEV_NOTICE_DISMISSED = "dev_notice_dismissed"
    private const val SAVINGS_GOAL_CENTS = "savings_goal_cents"

    private fun prefs(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun autoPurgeRaw(context: Context): Boolean = prefs(context).getBoolean(AUTO_PURGE_RAW, false)

    fun setAutoPurgeRaw(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(AUTO_PURGE_RAW, enabled).apply()
    }

    fun darkMode(context: Context): Boolean = prefs(context).getBoolean(DARK_MODE, false)

    fun setDarkMode(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(DARK_MODE, enabled).apply()
    }

    /** Aviso de "app em desenvolvimento" na Home — dispensável, não deve incomodar pra sempre. */
    fun devNoticeDismissed(context: Context): Boolean = prefs(context).getBoolean(DEV_NOTICE_DISMISSED, false)

    fun setDevNoticeDismissed(context: Context, dismissed: Boolean) {
        prefs(context).edit().putBoolean(DEV_NOTICE_DISMISSED, dismissed).apply()
    }

    /** Meta de economia mensal em centavos; 0 = sem meta definida. */
    fun savingsGoalCents(context: Context): Long = prefs(context).getLong(SAVINGS_GOAL_CENTS, 0L)

    fun setSavingsGoalCents(context: Context, cents: Long) {
        prefs(context).edit().putLong(SAVINGS_GOAL_CENTS, cents).apply()
    }
}
