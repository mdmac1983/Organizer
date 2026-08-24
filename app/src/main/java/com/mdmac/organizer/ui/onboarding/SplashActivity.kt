package com.mdmac.organizer.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.mdmac.organizer.MainActivity
import com.mdmac.organizer.databinding.ActivitySplashBinding
import com.mdmac.organizer.onboarding.OnboardingPreference
import com.mdmac.organizer.security.PinManager
import com.mdmac.organizer.theme.BaseActivity

class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            val pinManager = PinManager(this)
            val onboardingPreference = OnboardingPreference(this)

            val next = when {
                !pinManager.isPinSet() -> Intent(this, PinSetupActivity::class.java)
                !onboardingPreference.isComplete() -> Intent(this, OnboardingActivity::class.java)
                else -> Intent(this, MainActivity::class.java)
            }
            startActivity(next)
            finish()
        }, 1200)
    }
}
