package com.mdmac.organizer.ui.pinned

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mdmac.organizer.databinding.ActivityPinnedBinding

class PinnedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityPinnedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }
}
