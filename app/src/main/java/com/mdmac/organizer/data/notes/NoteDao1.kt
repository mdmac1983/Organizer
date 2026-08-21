package com.mdmac.organizer.data.notes

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM note_folders ORDER BY name ASC")
    fun getFolders(): Flow<List<NoteFolder>>

    @Query("SELECT * FROM notes WHERE folderId = :folderId ORDER BY lastEditedMillis DESC")
    fun getNotesInFolder(folderId: Long): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE folderId IS NULL ORDER BY lastEditedMillis DESC")
    fun getUnfiledNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes ORDER BY lastEditedMillis DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): Note?

    @Insert
    suspend fun insertFolder(folder: NoteFolder): Long

    @Insert
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Delete
    suspend fun deleteFolder(folder: NoteFolder)
}
