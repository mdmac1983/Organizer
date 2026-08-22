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

        pinManager = PinManager
