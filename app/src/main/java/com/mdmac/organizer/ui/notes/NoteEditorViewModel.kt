package com.mdmac.organizer.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mdmac.organizer.data.notes.*
import kotlinx.coroutines.launch

class NoteEditorViewModel(private val repository: NoteRepository) : ViewModel() {

    var noteId: Long = 0L
    var folderId: Long? = null
    var title: String = ""
    val blocks = mutableListOf<NoteBlock>()
    private var loaded = false

    fun loadIfNeeded(id: Long, incomingFolderId: Long?, onLoaded: () -> Unit) {
        if (loaded) { onLoaded(); return }
        loaded = true
        noteId = id
        folderId = incomingFolderId
        if (id == 0L) {
            onLoaded()
            return
        }
        viewModelScope.launch {
            repository.getNoteById(id)?.let { note ->
                title = note.title
                folderId = note.folderId
                blocks.clear()
                blocks.addAll(NoteContentSerializer.deserialize(note.contentJson))
            }
            onLoaded()
        }
    }

    fun save(onDone: () -> Unit) = viewModelScope.launch {
        val contentJson = NoteContentSerializer.serialize(blocks)
        val note = Note(
            id = noteId,
            folderId = folderId,
            title = title.trim(),
            contentJson = contentJson,
            lastEditedMillis = System.currentTimeMillis()
        )
        if (note.title.isBlank() && blocks.isEmpty()) { onDone(); return@launch }
        if (noteId == 0L) noteId = repository.insertNote(note) else repository.updateNote(note.copy(id = noteId))
        onDone()
    }

    class Factory(private val repository: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = NoteEditorViewModel(repository) as T
    }
}
