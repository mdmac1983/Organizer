package com.mdmac.organizer.data.planner

import kotlinx.coroutines.flow.Flow

class PlannerRepository(private val dao: PlannerDao) {
    fun getBetween(start: Long, end: Long): Flow<List<PlannerEntry>> = dao.getBetween(start, end)
    suspend fun insert(entry: PlannerEntry) = dao.insert(entry)
    suspend fun update(entry: PlannerEntry) = dao.update(entry)
    suspend fun delete(entry: PlannerEntry) = dao.delete(entry)
}
