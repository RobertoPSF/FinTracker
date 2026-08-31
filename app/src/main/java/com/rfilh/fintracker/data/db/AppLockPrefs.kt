package com.rfilh.fintracker.data.db

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/** Flag de bloqueio biométrico/PIN (RF19), no mesmo arquivo cifrado da passphrase do banco. */
object AppLockPrefs {
    private const val FILE = "fintracker_secure_prefs"
    private const val KEY = "app_lock_enabled"

    private fun prefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        FILE,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY, enabled).apply()
    }
}
