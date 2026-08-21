package com.mdmac.organizer.ui.passwords

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mdmac.organizer.data.passwords.PasswordCrypto
import com.mdmac.organizer.data.passwords.PasswordEntry
import com.mdmac.organizer.databinding.DialogPasswordBinding

class AddEditPasswordDialog(
    private val existing: PasswordEntry? = null,
    private val onSave: (PasswordEntry) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): android.app.Dialog {
        val binding = DialogPasswordBinding.inflate(layoutInflater)

        binding.inputSiteName.setText(existing?.siteName ?: "")
        binding.inputUsername.setText(existing?.username ?: "")
        binding.inputPassword.setText(existing?.encryptedPassword?.let { PasswordCrypto.decrypt(it) } ?: "")
        binding.inputUrl.setText(existing?.url ?: "")
        binding.inputNotes.setText(existing?.notes ?: "")

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existing == null) "New password" else "Edit password")
            .setView(binding.root)
            .setPositiveButton("Save") { _, _ ->
                val siteName = binding.inputSiteName.text?.toString()?.trim().orEmpty()
                val plainPassword = binding.inputPassword.text?.toString().orEmpty()
                if (siteName.isNotEmpty() && plainPassword.isNotEmpty()) {
                    onSave(
                        PasswordEntry(
                            id = existing?.id ?: 0,
                            siteName = siteName,
                            username = binding.inputUsername.text?.toString()?.trim().orEmpty(),
                            encryptedPassword = PasswordCrypto.encrypt(plainPassword),
                            url = binding.inputUrl.text?.toString()?.trim().orEmpty(),
                            notes = binding.inputNotes.text?.toString()?.trim().orEmpty()
                        )
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
}
