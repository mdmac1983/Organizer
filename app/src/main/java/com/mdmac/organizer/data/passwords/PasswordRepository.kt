package com.mdmac.organizer.data.passwords

import kotlinx.coroutines.flow.Flow

class PasswordRepository(private val dao: PasswordDao) {
    fun getAll(): Flow<List<PasswordEntry>> = dao.getAll()
    suspend fun insert(entry: PasswordEntry) = dao.insert(entry)
    suspend fun update(entry: PasswordEntry) = dao.update(entry)
    suspend fun delete(entry: PasswordEntry) = dao.delete(entry)
}
