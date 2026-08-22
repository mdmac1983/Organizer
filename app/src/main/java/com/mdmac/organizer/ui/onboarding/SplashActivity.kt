package com.mdmac.organizer.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.mdmac.organizer.MainActivity
import com.mdmac.organizer.databinding.ActivitySplashBinding
import com.mdmac.organizer.security.PinManager

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            val pinManager = PinManager(this)
            val next = if (pinManager.isPinSet()) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, PinSetupActivity::class.java)
            }
            startActivity(next)
            finish()
        }, 1200)
    }
}
