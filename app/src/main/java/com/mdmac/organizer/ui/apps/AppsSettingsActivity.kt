package com.mdmac.organizer.ui.apps

import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.mdmac.organizer.data.apps.AppCustomizationPreference
import com.mdmac.organizer.data.apps.AppsRepository
import com.mdmac.organizer.data.apps.InstalledApp
import com.mdmac.organizer.databinding.ActivityAppsSettingsBinding
import com.mdmac.organizer.databinding.ItemAppManageRowBinding
import com.mdmac.organizer.theme.BaseActivity
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class AppsSettingsActivity : BaseActivity() {

    private lateinit var binding: ActivityAppsSettingsBinding
    private lateinit var appsRepository: AppsRepository
    private lateinit var customization: AppCustomizationPreference
    private var pendingIconTargetPackage: String? = null

    private val pickIconLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val target = pendingIconTargetPackage
        if (uri != null && target != null) saveCustomIcon(target, uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppsSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appsRepository = AppsRepository(this)
        customization = AppCustomizationPreference(this)

        binding.toolbar.setNavigationOnClickListener { finish() }
        loadApps()
    }

    private fun loadApps() {
        lifecycleScope.launch {
            val apps = appsRepository.getLaunchableApps()
            binding.rowContainer.removeAllViews()
            apps.forEach { app -> addRow(app) }
        }
    }

    private fun addRow(app: InstalledApp) {
        val rowBinding = ItemAppManageRowBinding.inflate(layoutInflater, binding.rowContainer, false)
        rowBinding.appIcon.setImageDrawable(app.icon)
        rowBinding.appLabel.text = app.label
        rowBinding.hideSwitch.isChecked = app.isHidden

        rowBinding.appIcon.setOnClickListener {
            pendingIconTargetPackage = app.packageName
            pickIconLauncher.launch("image/*")
        }
        rowBinding.appLabel.setOnClickListener { showRenameDialog(app) }
        rowBinding.hideSwitch.setOnCheckedChangeListener { switchView, checked ->
            if (switchView.isPressed) {
                appsRepository.setHidden(app.packageName, checked)
            }
        }

        binding.rowContainer.addView(rowBinding.root)
    }

    private fun showRenameDialog(app: InstalledApp) {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            setText(app.label)
            setSelection(app.label.length)
        }
        AlertDialog.Builder(this)
            .setTitle("Rename app")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newLabel = input.text.toString().trim()
                customization.setCustomLabel(app.packageName, newLabel.ifEmpty { null })
                loadApps()
            }
            .setNeutralButton("Reset") { _, _ ->
                customization.setCustomLabel(app.packageName, null)
                loadApps()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveCustomIcon(packageName: String, uri: Uri) {
        runCatching {
            val input = contentResolver.openInputStream(uri) ?: return
            val bitmap = android.graphics.BitmapFactory.decodeStream(input)
            input.close()

            val file = File(filesDir, "icon_$packageName.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
            }
            customization.setCustomIconPath(packageName, file.absolutePath)
            loadApps()
        }
    }
}
