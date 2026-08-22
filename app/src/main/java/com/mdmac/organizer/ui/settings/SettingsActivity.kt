package com.mdmac.organizer.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mdmac.organizer.data.apps.AppsRepository
import com.mdmac.organizer.databinding.ActivitySettingsBinding
import com.mdmac.organizer.security.PinManager
import androidx.appcompat.app.AlertDialog
import android.widget.LinearLayout

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var pinManager: PinManager
    private lateinit var appsRepository: AppsRepository

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        pinManager = PinManager(this)
        appsRepository = AppsRepository(this)

        binding.btnChangePin.setOnClickListener { showChangePinDialog() }

        binding.btnRequestStorage.setOnClickListener { requestStoragePermission() }

        binding.btnDockSlotsMinus.setOnClickListener { adjustDockSlots(-1) }
        binding.btnDockSlotsPlus.setOnClickListener { adjustDockSlots(1) }
        updateDockSlotsDisplay()

        updateStorageStatus()
    }

    private fun adjustDockSlots(delta: Int) {
        val newCount = appsRepository.getDockSlotCount() + delta
        appsRepository.setDockSlotCount(newCount)
        updateDockSlotsDisplay()
    }

    private fun updateDockSlotsDisplay() {
        val count = appsRepository.getDockSlotCount()
        binding.dockSlotsValue.text = count.toString()
        binding.btnDockSlotsMinus.isEnabled = count > DOCK_SLOTS_MIN
        binding.btnDockSlotsPlus.isEnabled = count < DOCK_SLOTS_MAX
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(
                this,
                "No permission needed on this Android version",
                Toast.LENGTH_SHORT
            ).show()
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

    companion object {
        // Must match the coerceIn bounds in AppsRepository.setDockSlotCount
        private const val DOCK_SLOTS_MIN = 1
        private const val DOCK_SLOTS_MAX = 7
    }
}
