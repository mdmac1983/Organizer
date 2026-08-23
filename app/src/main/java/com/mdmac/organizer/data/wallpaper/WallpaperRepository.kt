package com.mdmac.organizer.data.wallpaper

import android.content.Context

/** Selected wallpaper: either a bundled drawable resource, or a gallery-picked image URI. */
class WallpaperRepository(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getBundledResId(): Int? {
        val id = prefs.getInt(KEY_BUNDLED_RES, 0)
        return if (id != 0) id else null
    }

    fun getCustomUri(): String? = prefs.getString(KEY_CUSTOM_URI, null)

    fun setBundled(resId: Int) {
        prefs.edit()
            .putInt(KEY_BUNDLED_RES, resId)
            .remove(KEY_CUSTOM_URI)
            .apply()
    }

    fun setCustomUri(uri: String) {
        prefs.edit()
            .putString(KEY_CUSTOM_URI, uri)
            .remove(KEY_BUNDLED_RES)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "wallpaper_prefs"
        private const val KEY_BUNDLED_RES = "bundled_res_id"
        private const val KEY_CUSTOM_URI = "custom_uri"
    }
}
