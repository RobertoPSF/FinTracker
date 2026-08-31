package com.rfilh.fintracker.data.db

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

/** Chave do SQLCipher: 32 bytes aleatórios guardados em EncryptedSharedPreferences (Keystore). */
object Passphrase {
    private const val FILE = "fintracker_secure_prefs"
    private const val KEY = "db_passphrase"

    fun getOrCreate(context: Context): ByteArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val prefs = EncryptedSharedPreferences.create(
            context,
            FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        prefs.getString(KEY, null)?.let { return Base64.decode(it, Base64.NO_WRAP) }

        val generated = ByteArray(32).also { SecureRandom().nextBytes(it) }
        prefs.edit().putString(KEY, Base64.encodeToString(generated, Base64.NO_WRAP)).apply()
        return generated
    }
}
