package com.mdmac.organizer.data.notes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_folders")
data class NoteFolder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)
