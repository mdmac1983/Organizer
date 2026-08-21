package com.mdmac.organizer.ui.notes

import androidx.lifecycle.*
import com.mdmac.organizer.data.notes.Note
import com.mdmac.organizer.data.notes.NoteFolder
import com.mdmac.organizer.data.notes.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotesViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _selection = MutableStateFlow<FolderSelection>(FolderSelection.All)
    val selection: StateFlow<FolderSelection> = _selection

    val folders: StateFlow<List<NoteFolder>> = repository.getFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<Note>> = _selection.flatMapLatest { sel ->
        when (sel) {
            is FolderSelection.All -> repository.getAllNotes()
            is FolderSelection.Unfiled -> repository.getUnfiledNotes()
            is FolderSelection.Specific -> repository.getNotesInFolder(sel.folderId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun select(sel: FolderSelection) { _selection.value = sel }

    fun addFolder(name: String) = viewModelScope.launch { repository.insertFolder(NoteFolder(name = name)) }

    fun deleteFolder(folder: NoteFolder) = viewModelScope.launch {
        repository.deleteFolder(folder)
        if (_selection.value is FolderSelection.Specific &&
            (_selection.value as FolderSelection.Specific).folderId == folder.id
        ) {
            _selection.value = FolderSelection.All
        }
    }

    fun deleteNote(note: Note) = viewModelScope.launch { repository.deleteNote(note) }

    class Factory(private val repository: NoteRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = NotesViewModel(repository) as T
    }
}
