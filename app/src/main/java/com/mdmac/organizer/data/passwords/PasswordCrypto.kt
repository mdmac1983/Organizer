package com.mdmac.organizer.data.passwords

import android.util.Base64
import com.mdmac.organizer.security.PinManager
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

// Encrypts/decrypts password values using a key derived from the user's
// PIN (PBKDF2 + the same salt PinManager stores), rather than a
// hardware-locked Keystore key — this makes encrypted values portable
// across devices/reinstalls, as long as the same PIN is re-entered.
// Call unlock() once the PIN is verified; the derived key is cached in
// memory only for that session.
object PasswordCrypto {

    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val PBKDF2_ITERATIONS = 100_000

    private var cachedKey: SecretKeySpec? = null

    fun unlock(pin: String, pinManager: PinManager) {
        val salt = pinManager.getSaltBytes() ?: return
        val spec = PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        cachedKey = SecretKeySpec(keyBytes, "AES")
    }

    fun lock() {
        cachedKey = null
    }

    fun encrypt(plainText: String): String {
        val key = cachedKey ?: error("PasswordCrypto.unlock() must be called first")
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" +
            Base64.encodeToString(cipherBytes, Base64.NO_WRAP)
    }

    fun decrypt(stored: String): String {
        val key = cachedKey ?: error("PasswordCrypto.unlock() must be called first")
        val parts = stored.split(":")
        if (parts.size != 2) return ""
        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val cipherBytes = Base64.decode(parts[1], Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        return String(cipher.doFinal(cipherBytes), Charsets.UTF_8)
    }
}
