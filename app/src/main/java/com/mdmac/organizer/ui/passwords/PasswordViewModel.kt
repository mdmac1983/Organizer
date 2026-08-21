package com.mdmac.organizer.ui.passwords

import androidx.lifecycle.*
import com.mdmac.organizer.data.passwords.PasswordEntry
import com.mdmac.organizer.data.passwords.PasswordRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PasswordViewModel(private val repository: PasswordRepository) : ViewModel() {

    val entries: StateFlow<List<PasswordEntry>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(entry: PasswordEntry) = viewModelScope.launch {
        if (entry.id == 0L) repository.insert(entry) else repository.update(entry)
    }

    fun delete(entry: PasswordEntry) = viewModelScope.launch { repository.delete(entry) }

    class Factory(private val repository: PasswordRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PasswordViewModel(repository) as T
    }
}
