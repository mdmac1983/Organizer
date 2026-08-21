package com.mdmac.organizer.ui.contacts

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mdmac.organizer.data.contacts.Contact
import com.mdmac.organizer.databinding.DialogContactBinding

class AddEditContactDialog(
    private val existing: Contact? = null,
    private val onSave: (Contact) -> Unit
) : DialogFragment() {

    private var pickedPhotoUri: String? = existing?.photoUri
    private var _binding: DialogContactBinding? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            requireContext().contentResolver.takePersistableUriPermission(
                it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            pickedPhotoUri = it.toString()
            _binding?.photoPreview?.setImageURI(it)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): android.app.Dialog {
        val binding = DialogContactBinding.inflate(layoutInflater)
        _binding = binding

        binding.inputName.setText(existing?.name ?: "")
        binding.inputPhone.setText(existing?.phone ?: "")
        binding.inputEmail.setText(existing?.email ?: "")
        binding.inputAddress.setText(existing?.address ?: "")
        binding.inputNotes.setText(existing?.notes ?: "")
        pickedPhotoUri?.let { binding.photoPreview.setImageURI(Uri.parse(it)) }

        binding.btnPickPhoto.setOnClickListener { pickImage.launch("image/*") }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existing == null) "New contact" else "Edit contact")
            .setView(binding.root)
            .setPositiveButton("Save") { _, _ ->
                val name = binding.inputName.text?.toString()?.trim().orEmpty()
                if (name.isNotEmpty()) {
                    onSave(
                        Contact(
                            id = existing?.id ?: 0,
                            name = name,
                            phone = binding.inputPhone.text?.toString()?.trim().orEmpty(),
                            email = binding.inputEmail.text?.toString()?.trim().orEmpty(),
                            address = binding.inputAddress.text?.toString()?.trim().orEmpty(),
                            notes = binding.inputNotes.text?.toString()?.trim().orEmpty(),
                            photoUri = pickedPhotoUri
                        )
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
