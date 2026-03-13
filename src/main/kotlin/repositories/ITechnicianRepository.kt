package org.delcom.repositories

import org.delcom.entities.Technician

interface ITechnicianRepository {
    // Parameter isComplete dan urgency diganti menjadi status dan divisi
    suspend fun getAll(userId: String, search: String, page: Int, perPage: Int, status: String?, divisi: String?): List<Technician>

    // Fungsi baru untuk statistik Home
    suspend fun getHomeStats(userId: String): Map<String, Long>

    // Parameter todoId diubah menjadi TechnicianId
    suspend fun getById(technicianId: String): Technician?

    // Parameter todo diubah menjadi Technician
    suspend fun create(technician: Technician): String

    // Parameter newTodo diubah menjadi newTechnician
    suspend fun update(userId: String, technicianId: String, newTechnician: Technician): Boolean

    suspend fun delete(userId: String, technicianId: String) : Boolean
}