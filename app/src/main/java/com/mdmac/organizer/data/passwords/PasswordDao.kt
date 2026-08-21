package com.mdmac.organizer.data.passwords

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordDao {
    @Query("SELECT * FROM password_entries ORDER BY siteName ASC")
    fun getAll(): Flow<List<PasswordEntry>>

    @Insert
    suspend fun insert(entry: PasswordEntry): Long

    @Update
    suspend fun update(entry: PasswordEntry)

    @Delete
    suspend fun delete(entry: PasswordEntry)
}
