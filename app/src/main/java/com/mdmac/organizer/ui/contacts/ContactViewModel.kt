package com.mdmac.organizer.ui.contacts

import androidx.lifecycle.*
import com.mdmac.organizer.data.contacts.Contact
import com.mdmac.organizer.data.contacts.ContactRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactViewModel(private val repository: ContactRepository) : ViewModel() {

    val contacts: StateFlow<List<Contact>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(contact: Contact) = viewModelScope.launch {
        if (contact.id == 0L) repository.insert(contact) else repository.update(contact)
    }

    fun delete(contact: Contact) = viewModelScope.launch { repository.delete(contact) }

    class Factory(private val repository: ContactRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ContactViewModel(repository) as T
    }
}
