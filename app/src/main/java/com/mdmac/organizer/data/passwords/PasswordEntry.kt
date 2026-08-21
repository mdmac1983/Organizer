package com.mdmac.organizer.data.passwords

import androidx.room.Entity
import androidx.room.PrimaryKey

// NOTE: `password` here holds ciphertext, not plaintext.
// Encryption/decryption is handled in Step 7 via the Android Keystore —
// this entity just stores whatever encrypted blob it's given.
@Entity(tableName = "password_entries")
data class PasswordEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val siteName: String,
    val username: String = "",
    val encryptedPassword: String,
    val url: String = "",
    val notes: String = ""
)
