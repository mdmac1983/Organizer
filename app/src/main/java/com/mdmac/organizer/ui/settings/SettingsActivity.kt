package com.mdmac.organizer.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mdmac.organizer.data.OrganizerDatabase
import com.mdmac.organizer.databinding.ActivitySettingsBinding
import com.mdmac.organizer.security.PinManager
import androidx.appcompat.app.AlertDialog
import android.widget.LinearLayout
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var pinManager: PinManager

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        Toast.makeText(
            this,
            if (granted) "Storage permission granted" else "Storage permission denied",
            Toast.LENGTH_SHORT
        ).show()
        updateStorageStatus()
    }

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri -> if (uri != null) exportBackup(uri) }

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) importBackup(uri) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        pinManager = PinManager(this)

        binding.btnChangePin.setOnClickListener { showChangePinDialog() }
        binding.btnRequestStorage.setOnClickListener { requestStoragePermission() }
        binding.btnExportBackup.setOnClickListener {
            exportLauncher.launch("simple-planner-backup.zip")
        }
        binding.btnImportBackup.setOnClickListener {
            importLauncher.launch(arrayOf("application/zip"))
        }

        updateStorageStatus()
    }

    private fun exportBackup(uri: Uri) {
        try {
            OrganizerDatabase.getInstance(this).close()
            val dbFile = getDatabasePath("organizer.db")
            val prefsFile = File(filesDir.parentFile, "shared_prefs/${PinManager.PREFS_NAME}.xml")

            contentResolver.openOutputStream(uri)?.use { out ->
                ZipOutputStream(out).use { zip ->
                    zip.putNextEntry(ZipEntry("organizer.db"))
                    dbFile.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()

                    if (prefsFile.exists()) {
                        zip.putNextEntry(ZipEntry("pin_prefs.xml"))
                        prefsFile.inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
                }
            }
            Toast.makeText(this, "Backup exported — close and reopen the app now", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun importBackup(uri: Uri) {
        try {
            OrganizerDatabase.getInstance(this).close()
            val dbFile = getDatabasePath("organizer.db")
            val prefsDir = File(filesDir.parentFile, "shared_prefs").apply { mkdirs() }
            val prefsFile = File(prefsDir, "${PinManager.PREFS_NAME}.xml")

            contentResolver.openInputStream(uri)?.use { input ->
                ZipInputStream(input).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        val target = when (entry.name) {
                            "organizer.db" -> dbFile
                            "pin_prefs.xml" -> prefsFile
                            else -> null
                        }
                        target?.outputStream()?.use { out -> zip.copyTo(out) }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }
            Toast.makeText(this, "Backup imported — close and reopen the app now", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(this, "No permission needed on this Android version", Toast.LENGTH_SHORT).show()
            updateStorageStatus()
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun updateStorageStatus() {
        val granted = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED
        binding.storagePermissionStatus.text =
            if (granted) "Storage permission: granted" else "Storage permission: not granted"
    }

    private fun showChangePinDialog() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
        }

        val currentPinLayout = TextInputLayout(this).apply { hint = "Current PIN" }
        val currentPinInput = TextInputEditText(currentPinLayout.context)
        currentPinLayout.addView(currentPinInput)

        val newPinLayout = TextInputLayout(this).apply { hint = "New PIN" }
        val newPinInput = TextInputEditText(newPinLayout.context)
        newPinLayout.addView(newPinInput)

        val confirmPinLayout = TextInputLayout(this).apply { hint = "Confirm new PIN" }
        val confirmPinInput = TextInputEditText(confirmPinLayout.context)
        confirmPinLayout.addView(confirmPinInput)

        if (pinManager.isPinSet()) {
            container.addView(currentPinLayout)
        }
        container.addView(newPinLayout)
        container.addView(confirmPinLayout)

        AlertDialog.Builder(this)
            .setTitle(if (pinManager.isPinSet()) "Change app PIN" else "Set app PIN")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val current = currentPinInput.text.toString()
                val new = newPinInput.text.toString()
                val confirm = confirmPinInput.text.toString()

                when {
                    pinManager.isPinSet() && !pinManager.verifyPin(current) ->
                        showError("Current PIN is incorrect")
                    new.length < 4 ->
                        showError("New PIN must be at least 4 digits")
                    new != confirm ->
                        showError("New PIN doesn't match confirmation")
                    else -> {
                        pinManager.setPin(new)
                        Toast.makeText(this, "PIN saved", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this).setMessage(message).setPositiveButton("OK", null).show()
    }
}
