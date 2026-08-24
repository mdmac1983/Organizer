package com.mdmac.organizer.ui.about

import android.os.Build
import android.os.Bundle
import com.mdmac.organizer.databinding.ActivityAboutBinding
import com.mdmac.organizer.theme.BaseActivity

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

        binding.appNameText.text = getString(com.mdmac.organizer.R.string.app_name)
        binding.versionText.text = "Version ${packageInfo.versionName} ($versionCode)"
        binding.packageNameText.text = packageName
        binding.footerText.text = getString(com.mdmac.organizer.R.string.settings_footer)
    }
}
