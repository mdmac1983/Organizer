package com.mdmac.organizer.ui.wallpaper

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.setPadding
import com.mdmac.organizer.R
import com.mdmac.organizer.data.wallpaper.WallpaperRepository
import com.mdmac.organizer.databinding.ActivityWallpaperPickerBinding
import com.mdmac.organizer.theme.BaseActivity
import java.io.File
import java.io.FileOutputStream

class WallpaperPickerActivity : BaseActivity() {

    private lateinit var binding: ActivityWallpaperPickerBinding
    private lateinit var wallpaperRepository: WallpaperRepository

    private var pendingSelection: WallpaperOption = WallpaperOption.None

    private sealed class WallpaperOption {
        object None : WallpaperOption()
        data class Bundled(val resId: Int) : WallpaperOption()
        data class Custom(val path: String) : WallpaperOption()
    }

    private val bundledOptions = listOf(
        R.drawable.wallpaper_bubblegum,
        R.drawable.wallpaper_flora,
        R.drawable.wallpaper_canyon,
        R.drawable.wallpaper_escape,
        R.drawable.wallpaper_kepler,
        R.drawable.wallpaper_outofthebox,
        R.drawable.wallpaper_work,
        R.drawable.wallpaper_chroma,
        R.drawable.wallpaper_architecture
    )

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) copyToInternalStorageAndPreview(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWallpaperPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wallpaperRepository = WallpaperRepository(this)

        binding.setWallpaperButton.setOnClickListener { confirmSelection() }
        buildThumbnailStrip()
        loadCurrentAsPreview()
    }

    private fun loadCurrentAsPreview() {
        val customPath = wallpaperRepository.getCustomUri()
        val bundledRes = wallpaperRepository.getBundledResId()
        when {
            customPath != null -> {
                pendingSelection = WallpaperOption.Custom(customPath)
                binding.previewImage.setImageURI(Uri.fromFile(File(customPath)))
            }
            bundledRes != null -> {
                pendingSelection = WallpaperOption.Bundled(bundledRes)
                binding.previewImage.setImageResource(bundledRes)
            }
            else -> {
                pendingSelection = WallpaperOption.Bundled(bundledOptions.first())
                binding.previewImage.setImageResource(bundledOptions.first())
            }
        }
    }

    private fun buildThumbnailStrip() {
        binding.thumbnailContainer.removeAllViews()

        bundledOptions.forEach { resId ->
            val thumb = ImageView(this).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(72.dp(), 72.dp()).apply {
                    marginEnd = 8.dp()
                }
                setImageResource(resId)
                scaleType = ImageView.ScaleType.CENTER_CROP
                setPadding(0)
                setOnClickListener {
                    pendingSelection = WallpaperOption.Bundled(resId)
                    binding.previewImage.setImageResource(resId)
                }
            }
            binding.thumbnailContainer.addView(thumb)
        }

        val galleryTile = ImageView(this).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(72.dp(), 72.dp())
            setImageResource(android.R.drawable.ic_menu_gallery)
            scaleType = ImageView.ScaleType.CENTER
            setBackgroundColor(resources.getColor(R.color.gray_200, theme))
            setOnClickListener { pickImageLauncher.launch("image/*") }
        }
        binding.thumbnailContainer.addView(galleryTile)
    }

    private fun copyToInternalStorageAndPreview(uri: Uri) {
        runCatching {
            val input = contentResolver.openInputStream(uri) ?: return
            val bitmap = BitmapFactory.decodeStream(input)
            input.close()

            val file = File(filesDir, "wallpaper_custom.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
            }
            pendingSelection = WallpaperOption.Custom(file.absolutePath)
            binding.previewImage.setImageBitmap(bitmap)
        }.onFailure {
            Toast.makeText(this, "Couldn't load that image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmSelection() {
        when (val selection = pendingSelection) {
            is WallpaperOption.Bundled -> wallpaperRepository.setBundled(selection.resId)
            is WallpaperOption.Custom -> wallpaperRepository.setCustomUri(selection.path)
            WallpaperOption.None -> {}
        }
        finish()
    }

    private fun Int.dp(): Int = (this * resources.displayMetrics.density).toInt()
}
