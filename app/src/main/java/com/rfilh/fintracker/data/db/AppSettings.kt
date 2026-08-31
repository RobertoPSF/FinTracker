package com.rfilh.fintracker.data.db

import android.content.Context

/** Flags de comportamento, não sensíveis (RF21). */
object AppSettings {
    private const val FILE = "fintracker_prefs"
    private const val AUTO_PURGE_RAW = "auto_purge_raw"
    private const val DARK_MODE = "dark_mode"

    private fun prefs(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun autoPurgeRaw(context: Context): Boolean = prefs(context).getBoolean(AUTO_PURGE_RAW, false)

    fun setAutoPurgeRaw(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(AUTO_PURGE_RAW, enabled).apply()
    }

    fun darkMode(context: Context): Boolean = prefs(context).getBoolean(DARK_MODE, false)

    fun setDarkMode(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(DARK_MODE, enabled).apply()
    }
}
