package com.mdmac.organizer.data.planner

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planner_entries")
data class PlannerEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dateTimeMillis: Long,   // start date/time of the entry
    val notes: String = ""
)
