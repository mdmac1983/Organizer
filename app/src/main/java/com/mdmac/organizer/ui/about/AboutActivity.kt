package com.mdmac.organizer.ui.about

import android.os.Build
import android.os.Bundle
import android.view.View
import com.mdmac.organizer.R
import com.mdmac.organizer.databinding.ActivityAboutBinding
import com.mdmac.organizer.theme.BaseActivity
import java.io.BufferedReader
import java.io.InputStreamReader

class AboutActivity : BaseActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }

        binding.appNameText.text = getString(R.string.app_name)
        binding.versionText.text = "Version ${packageInfo.versionName} ($versionCode)"
        binding.packageNameText.text = packageName
        binding.footerText.text = getString(R.string.settings_footer)

        binding.readmeContent.text = readRawText(R.raw.readme)
        binding.licenseContent.text = readRawText(R.raw.license)

        setupToggle(binding.readmeHeaderRow, binding.readmeContent, binding.readmeToggleIcon)
        setupToggle(binding.licenseHeaderRow, binding.licenseContent, binding.licenseToggleIcon)
    }

    private fun setupToggle(headerRow: View, content: View, icon: android.widget.TextView) {
        headerRow.setOnClickListener {
            val expanding = content.visibility != View.VISIBLE
            content.visibility = if (expanding) View.VISIBLE else View.GONE
            icon.text = if (expanding) "▾" else "▸"
        }
    }

    private fun readRawText(resId: Int): String {
        return runCatching {
            BufferedReader(InputStreamReader(resources.openRawResource(resId))).use { it.readText() }
        }.getOrDefault("")
    }
}
