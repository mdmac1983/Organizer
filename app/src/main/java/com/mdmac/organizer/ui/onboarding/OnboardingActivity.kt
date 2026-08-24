package com.mdmac.organizer.ui.onboarding

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.mdmac.organizer.accessibility.TouchBlockerService
import com.mdmac.organizer.admin.PlannerDeviceAdminReceiver
import com.mdmac.organizer.databinding.ActivityOnboardingBinding
import com.mdmac.organizer.databinding.ItemOnboardingRowBinding
import com.mdmac.organizer.onboarding.OnboardingPreference
import com.mdmac.organizer.theme.BaseActivity

private data class OnboardingRow(
    val title: String,
    val subtitleProvider: () -> String,
    val buttonText: String,
    val onClick: () -> Unit
)

class OnboardingActivity : BaseActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var deviceAdminComponent: ComponentName
    private val rowBindings = mutableListOf<Pair<OnboardingRow, ItemOnboardingRowBinding>>()

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refreshRows() }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refreshRows() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        devicePolicyManager = getSystemService(DevicePolicyManager::class.java)
        deviceAdminComponent = ComponentName(this, PlannerDeviceAdminReceiver::class.java)

        buildRows()
        binding.continueButton.setOnClickListener { finishOnboarding() }
    }

    override fun onResume() {
        super.onResume()
        refreshRows()
    }

    private fun buildRows() {
        val rows = mutableListOf<OnboardingRow>()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            rows.add(
                OnboardingRow(
                    "Storage access",
                    { if (hasStoragePermission()) "Granted" else "Needed for backups and custom wallpapers" },
                    "Grant"
                ) { storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE) }
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            rows.add(
                OnboardingRow(
                    "Notifications",
                    { if (hasNotificationPermission()) "Granted" else "Needed to show the touch-block disable button" },
                    "Grant"
                ) { notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
            )
        }

        rows.add(
            OnboardingRow(
                "Accessibility service",
                { if (TouchBlockerService.instance != null) "Enabled" else "Needed for touch-block and some gestures" },
                "Enable"
            ) { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        )

        rows.add(
            OnboardingRow(
                "Device admin",
                { if (devicePolicyManager.isAdminActive(deviceAdminComponent)) "Active" else "Needed for lock-screen and uninstall protection" },
                "Enable"
            ) { requestDeviceAdmin() }
        )

        rows.forEach { row ->
            val rowBinding = ItemOnboardingRowBinding.inflate(layoutInflater, binding.rowContainer, false)
            rowBinding.rowTitle.text = row.title
            rowBinding.rowButton.text = row.buttonText
            rowBinding.rowButton.setOnClickListener { row.onClick() }
            binding.rowContainer.addView(rowBinding.root)
            rowBindings.add(row to rowBinding)
        }
        refreshRows()
    }

    private fun refreshRows() {
        rowBindings.forEach { (row, rowBinding) ->
            rowBinding.rowSubtitle.text = row.subtitleProvider()
        }
    }

    private fun hasStoragePermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED

    private fun hasNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

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

    private fun finishOnboarding() {
        startActivity(Intent(this, LauncherSetupActivity::class.java))
        finish()
    }
}
