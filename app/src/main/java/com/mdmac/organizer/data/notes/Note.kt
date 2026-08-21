package com.mdmac.organizer.data.notes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folderId: Long?,          // null = unfiled
    val title: String,
    val contentJson: String,      // serialized rich content: text blocks + checklist items
    val lastEditedMillis: Long
)
