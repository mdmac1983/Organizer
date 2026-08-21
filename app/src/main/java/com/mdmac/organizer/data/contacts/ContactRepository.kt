package com.mdmac.organizer.data.contacts

import kotlinx.coroutines.flow.Flow

class ContactRepository(private val dao: ContactDao) {
    fun getAll(): Flow<List<Contact>> = dao.getAll()
    suspend fun insert(contact: Contact) = dao.insert(contact)
    suspend fun update(contact: Contact) = dao.update(contact)
    suspend fun delete(contact: Contact) = dao.delete(contact)
}
