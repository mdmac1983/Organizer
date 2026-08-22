package com.mdmac.organizer.ui.settings

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mdmac.organizer.R
import com.mdmac.organizer.accessibility.TouchBlockerService
import com.mdmac.organizer.admin.PlannerDeviceAdminReceiver
import com.mdmac.organizer.data.apps.AppsRepository
import com.mdmac.organizer.databinding.ActivitySettingsBinding
import com.mdmac.organizer.security.PinManager
import com.mdmac.organizer.theme.ThemeMode
import com.mdmac.organizer.theme.ThemePreference
import androidx.appcompat.app.AlertDialog
import android.widget.LinearLayout

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var pinManager: PinManager
    private lateinit var appsRepository: AppsRepository
    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var deviceAdminComponent: ComponentName
    private lateinit var themePreference: ThemePreference

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

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            TouchBlockerService.instance?.setBlocking(true)
        } else {
            binding.switchTouchBlocker.isChecked = false
            Toast.makeText(
                this,
                "Notification permission is needed so you can see the Disable action",
                Toast.LENGTH_LONG
            ).show()
        }
        updateTouchBlockerDisplay()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        pinManager = PinManager(this)
        appsRepository = AppsRepository(this)
        devicePolicyManager = getSystemService(DevicePolicyManager::class.java)
        deviceAdminComponent = ComponentName(this, PlannerDeviceAdminReceiver::class.java)
        themePreference = ThemePreference(this)

        setupAppearanceSection()

        binding.btnChangePin.setOnClickListener { showChangePinDialog() }

        binding.btnRequestStorage.setOnClickListener { requestStoragePermission() }

        binding.btnDockSlotsMinus.setOnClickListener { adjustDockSlots(-1) }
        binding.btnDockSlotsPlus.setOnClickListener { adjustDockSlots(1) }
        updateDockSlotsDisplay()

        binding.btnOpenAccessibilitySettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        binding.switchTouchBlocker.setOnCheckedChangeListener { switchView, checked ->
            if (!switchView.isPressed) return@setOnCheckedChangeListener // ignore programmatic updates
            if (checked) enableTouchBlocking() else TouchBlockerService.instance?.setBlocking(false)
        }

        binding.btnEnableDeviceAdmin.setOnClickListener { requestDeviceAdmin() }
        binding.btnLockScreenNow.setOnClickListener { lockScreenNow() }

        updateStorageStatus()
    }

    override fun onResume() {
        super.onResume()
        // Accessibility/device-admin state changes happen in system Settings
        // screens outside our control, so re-check whenever we come back.
        updateTouchBlockerDisplay()
        updateDeviceAdminDisplay()
    }

    // --- Appearance ---

    private fun setupAppearanceSection() {
        val checkedId = when (themePreference.getMode()) {
            ThemeMode.LIGHT -> R.id.btnThemeLight
            ThemeMode.DARK -> R.id.btnThemeDark
            ThemeMode.SYSTEM -> R.id.btnThemeSystem
        }
        binding.appearanceToggleGroup.check(checkedId)

        binding.appearanceToggleGroup.addOnButtonCheckedListener { _, id, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val mode = when (id) {
                R.id.btnThemeLight -> ThemeMode.LIGHT
                R.id.btnThemeDark -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }
            themePreference.setMode(mode) // recreates this Activity via AppCompatDelegate
        }
    }

    // --- Apps tab dock slots ---

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

    // --- Touch Blocker (accessibility) ---

    private fun isAccessibilityServiceEnabled(): Boolean = TouchBlockerService.instance != null

    private fun updateTouchBlockerDisplay() {
        val enabled = isAccessibilityServiceEnabled()
        binding.accessibilityStatus.text = if (enabled) {
            "Accessibility service: enabled"
        } else {
            "Accessibility service: not enabled — tap below to turn it on"
        }
        binding.switchTouchBlocker.isEnabled = enabled
        binding.switchTouchBlocker.isChecked = enabled && TouchBlockerService.instance?.isBlocking() == true
    }

    private fun enableTouchBlocking() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            TouchBlockerService.instance?.setBlocking(true)
        }
        updateTouchBlockerDisplay()
    }

    // --- Device Admin ---

    private fun updateDeviceAdminDisplay() {
        val active = devicePolicyManager.isAdminActive(deviceAdminComponent)
        binding.deviceAdminStatus.text = if (active) {
            "Device admin: active — screen-lock enforcement and uninstall protection enabled"
        } else {
            "Device admin: not active"
        }
        binding.btnEnableDeviceAdmin.isEnabled = !active
        binding.btnLockScreenNow.isEnabled = active
    }

    private fun requestDeviceAdmin() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminComponent)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Enables locking the screen on demand and blocks uninstalling Simple Planner while active."
            )
        }
        startActivity(intent)
    }

    private fun lockScreenNow() {
        runCatching { devicePolicyManager.lockNow() }
            .onFailure {
                Toast.makeText(this, "Couldn't lock screen — is device admin still active?", Toast.LENGTH_SHORT).show()
            }
    }

    // --- Storage permission ---

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

    // --- PIN ---

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
