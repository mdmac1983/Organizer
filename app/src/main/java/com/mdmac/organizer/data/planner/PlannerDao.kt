package com.mdmac.organizer.data.planner

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannerDao {
    @Query("SELECT * FROM planner_entries ORDER BY dateTimeMillis ASC")
    fun getAll(): Flow<List<PlannerEntry>>

    @Query("SELECT * FROM planner_entries WHERE dateTimeMillis BETWEEN :start AND :end ORDER BY dateTimeMillis ASC")
    fun getBetween(start: Long, end: Long): Flow<List<PlannerEntry>>

    @Insert
    suspend fun insert(entry: PlannerEntry): Long

    @Update
    suspend fun update(entry: PlannerEntry)

    @Delete
    suspend fun delete(entry: PlannerEntry)
}
