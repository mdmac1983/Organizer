package com.mdmac.organizer.data.notes

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val dao: NoteDao) {
    fun getFolders(): Flow<List<NoteFolder>> = dao.getFolders()
    fun getAllNotes(): Flow<List<Note>> = dao.getAllNotes()
    fun getUnfiledNotes(): Flow<List<Note>> = dao.getUnfiledNotes()
    fun getNotesInFolder(folderId: Long): Flow<List<Note>> = dao.getNotesInFolder(folderId)
    suspend fun getNoteById(id: Long): Note? = dao.getNoteById(id)
    suspend fun insertFolder(folder: NoteFolder): Long = dao.insertFolder(folder)
    suspend fun deleteFolder(folder: NoteFolder) = dao.deleteFolder(folder)
    suspend fun insertNote(note: Note): Long = dao.insertNote(note)
    suspend fun updateNote(note: Note) = dao.updateNote(note)
    suspend fun deleteNote(note: Note) = dao.deleteNote(note)
}
