package com.mdmac.organizer.data.notes

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

// Copies a user-picked image into the app's own private storage so it
// survives independent of any temporary gallery permission, and gives
// each saved image a unique filename referenced from a NoteBlock's text field.
object NoteImageStore {

    private const val DIR_NAME = "note_images"

    fun save(context: Context, uri: Uri): String? {
        return try {
            val dir = File(context.filesDir, DIR_NAME).apply { mkdirs() }
            val fileName = "${UUID.randomUUID()}.jpg"
            val outFile = File(dir, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                outFile.outputStream().use { output -> input.copyTo(output) }
            }
            outFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun delete(path: String) {
        File(path).delete()
    }
}
