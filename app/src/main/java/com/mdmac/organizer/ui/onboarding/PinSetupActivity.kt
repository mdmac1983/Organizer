package com.mdmac.organizer.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mdmac.organizer.MainActivity
import com.mdmac.organizer.databinding.ActivityPinSetupBinding
import com.mdmac.organizer.security.PinManager

class PinSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinSetupBinding
    private lateinit var pinManager: PinManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pinManager = PinManager(this)

        binding.btnSavePin.setOnClickListener {
            val new = binding.inputNewPin.text.toString()
            val confirm = binding.inputConfirmPin.text.toString()

            when {
                new.length < 4 -> showError("PIN must be at least 4 digits")
                new != confirm -> showError("PINs don't match")
                else -> {
                    pinManager.setPin(new)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }

    private fun showError(message: String) {
        binding.setupError.visibility = android.view.View.VISIBLE
        binding.setupError.text = message
    }
}
