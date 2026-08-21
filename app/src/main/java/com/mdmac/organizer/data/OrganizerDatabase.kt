package com.mdmac.organizer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mdmac.organizer.data.contacts.Contact
import com.mdmac.organizer.data.contacts.ContactDao
import com.mdmac.organizer.data.notes.Note
import com.mdmac.organizer.data.notes.NoteDao
import com.mdmac.organizer.data.notes.NoteFolder
import com.mdmac.organizer.data.passwords.PasswordDao
import com.mdmac.organizer.data.passwords.PasswordEntry
import com.mdmac.organizer.data.planner.PlannerDao
import com.mdmac.organizer.data.planner.PlannerEntry

@Database(
    entities = [
        PlannerEntry::class,
        Contact::class,
        PasswordEntry::class,
        Note::class,
        NoteFolder::class
    ],
    version = 1,
    exportSchema = false
)
abstract class OrganizerDatabase : RoomDatabase() {

    abstract fun plannerDao(): PlannerDao
    abstract fun contactDao(): ContactDao
    abstract fun passwordDao(): PasswordDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile private var INSTANCE: OrganizerDatabase? = null

        fun getInstance(context: Context): OrganizerDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    OrganizerDatabase::class.java,
                    "organizer.db"
                ).build().also { INSTANCE = it }
            }
    }
}
