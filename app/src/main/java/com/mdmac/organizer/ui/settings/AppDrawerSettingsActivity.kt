package com.mdmac.organizer.ui.settings

import android.content.Context
import android.os.Bundle
import android.widget.SeekBar
import com.mdmac.organizer.databinding.ActivityAppDrawerSettingsBinding
import com.mdmac.organizer.theme.BaseActivity

/** Stores drawer opacity/brightness/column-count; AppsFragment reads these on next open. */
class DrawerSettingsPreference(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getColumns(): Int = prefs.getInt(KEY_COLUMNS, DEFAULT_COLUMNS)
    fun setColumns(value: Int) = prefs.edit().putInt(KEY_COLUMNS, value.coerceIn(COLUMNS_MIN, COLUMNS_MAX)).apply()

    fun getOpacity(): Int = prefs.getInt(KEY_OPACITY, DEFAULT_OPACITY)
    fun setOpacity(value: Int) = prefs.edit().putInt(KEY_OPACITY, value.coerceIn(0, 100)).apply()

    fun getBrightness(): Int = prefs.getInt(KEY_BRIGHTNESS, DEFAULT_BRIGHTNESS)
    fun setBrightness(value: Int) = prefs.edit().putInt(KEY_BRIGHTNESS, value.coerceIn(0, 100)).apply()

    companion object {
        private const val PREFS_NAME = "drawer_settings_prefs"
        private const val KEY_COLUMNS = "drawer_columns"
        private const val KEY_OPACITY = "drawer_opacity"
        private const val KEY_BRIGHTNESS = "drawer_brightness"
        const val COLUMNS_MIN = 3
        const val COLUMNS_MAX = 6
        const val DEFAULT_COLUMNS = 5
        const val DEFAULT_OPACITY = 100
        const val DEFAULT_BRIGHTNESS = 100
    }
}

class AppDrawerSettingsActivity : BaseActivity() {

    private lateinit var binding: ActivityAppDrawerSettingsBinding
    private lateinit var prefs: DrawerSettingsPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppDrawerSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = DrawerSettingsPreference(this)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.columnsSeekBar.max = DrawerSettingsPreference.COLUMNS_MAX - DrawerSettingsPreference.COLUMNS_MIN
        binding.columnsSeekBar.progress = prefs.getColumns() - DrawerSettingsPreference.COLUMNS_MIN
        binding.opacitySeekBar.progress = prefs.getOpacity()
        binding.brightnessSeekBar.progress = prefs.getBrightness()
        updateLabels()

        binding.columnsSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = updateLabels()
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                prefs.setColumns(DrawerSettingsPreference.COLUMNS_MIN + (seekBar?.progress ?: 0))
            }
        })
        binding.opacitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = updateLabels()
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                prefs.setOpacity(seekBar?.progress ?: 100)
            }
        })
        binding.brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) = updateLabels()
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                prefs.setBrightness(seekBar?.progress ?: 100)
            }
        })
    }

    private fun updateLabels() {
        binding.columnsValue.text = (DrawerSettingsPreference.COLUMNS_MIN + binding.columnsSeekBar.progress).toString()
        binding.opacityValue.text = "${binding.opacitySeekBar.progress}%"
        binding.brightnessValue.text = "${binding.brightnessSeekBar.progress}%"
    }
}
