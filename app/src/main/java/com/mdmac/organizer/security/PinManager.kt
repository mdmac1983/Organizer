package com.mdmac.organizer.security

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

// Stores a salted hash of the PIN in plain SharedPreferences (portable —
// not hardware-Keystore-locked), so salt+hash can travel in an export
// and be restored on any device. The PIN itself is never stored.
class PinManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isPinSet(): Boolean = prefs.contains(KEY_HASH)

    fun setPin(pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        prefs.edit()
            .putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_HASH, hash(pin, salt))
            .apply()
    }

    fun verifyPin(pin: String): Boolean {
        val salt = getSaltBytes() ?: return false
        val storedHash = prefs.getString(KEY_HASH, null) ?: return false
        return hash(pin, salt) == storedHash
    }

    // Exposed so PasswordCrypto can derive its AES key from the same salt.
    fun getSaltBytes(): ByteArray? {
        val saltStr = prefs.getString(KEY_SALT, null) ?: return null
        return Base64.decode(saltStr, Base64.NO_WRAP)
    }

    private fun hash(pin: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        val bytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    companion object {
        const val PREFS_NAME = "organizer_pin_prefs"
        private const val KEY_SALT = "pin_salt"
        private const val KEY_HASH = "pin_hash"
    }
}
