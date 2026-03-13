package org.delcom.repositories

import org.delcom.dao.TechnicianDAO
import org.delcom.entities.Technician
import org.delcom.helpers.suspendTransaction
import org.delcom.helpers.technicianDAOToModel // Pastikan import diubah
import org.delcom.tables.TechnicianTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import java.util.*

class TechnicianRepository : ITechnicianRepository {
    override suspend fun getAll(userId: String, search: String, page: Int, perPage: Int, status: String?, teknisi: String?): List<Technician> = suspendTransaction {
        val query = if (search.isBlank()) {
            TechnicianDAO.find {
                var op: org.jetbrains.exposed.sql.Op<Boolean> = (TechnicianTable.userId eq UUID.fromString(userId))
                if (status != null) op = op and (TechnicianTable.status eq status)
                if (teknisi != null) op = op and (TechnicianTable.teknisi eq teknisi)
                op
            }
        } else {
            val keyword = "%${search.lowercase()}%"
            TechnicianDAO.find {
                var op: org.jetbrains.exposed.sql.Op<Boolean> = (TechnicianTable.userId eq UUID.fromString(userId)) and (TechnicianTable.title.lowerCase() like keyword)
                if (status != null) op = op and (TechnicianTable.status eq status)
                if (teknisi != null) op = op and (TechnicianTable.teknisi eq teknisi)
                op
            }
        }

        // Terapkan Pengurutan berdasarkan Waktu Dibuat (Terbaru)
        query.orderBy(TechnicianTable.createdAt to SortOrder.DESC)
            .limit(perPage).offset(((page - 1) * perPage).toLong()).map(::technicianDAOToModel)
    }

    override suspend fun getHomeStats(userId: String): Map<String, Long> = suspendTransaction {
        val total = TechnicianDAO.find { TechnicianTable.userId eq UUID.fromString(userId) }.count()
        val completed = TechnicianDAO.find { (TechnicianTable.userId eq UUID.fromString(userId)) and (TechnicianTable.status eq "sudah terlaksana") }.count()
        val canceled = TechnicianDAO.find { (TechnicianTable.userId eq UUID.fromString(userId)) and (TechnicianTable.status eq "dibatalkan") }.count()
        val active = TechnicianDAO.find { (TechnicianTable.userId eq UUID.fromString(userId)) and (TechnicianTable.status eq "belum terlaksana") }.count()

        mapOf("total" to total, "complete" to completed, "active" to active, "canceled" to canceled)
    }

    override suspend fun getById(technicianId: String): Technician? = suspendTransaction {
        TechnicianDAO
            .find {
                (TechnicianTable.id eq UUID.fromString(technicianId))
            }
            .limit(1)
            .map(::technicianDAOToModel)
            .firstOrNull()
    }

    override suspend fun create(technician: Technician): String = suspendTransaction {
        val technicianDAO = TechnicianDAO.new {
            userId = UUID.fromString(technician.userId)
            title = technician.title
            description = technician.description
            cover = technician.cover
            status = technician.status
            tanggalDiterima = technician.tanggalDiterima
            namaPemilik = technician.namaPemilik
            estimasiBiaya = technician.estimasiBiaya
            teknisi = technician.teknisi
            createdAt = technician.createdAt
            updatedAt = technician.updatedAt
        }

        technicianDAO.id.value.toString()
    }

    override suspend fun update(userId: String, technicianId: String, newTechnician: Technician): Boolean = suspendTransaction {
        val technicianDAO = TechnicianDAO
            .find {
                (TechnicianTable.id eq UUID.fromString(technicianId)) and
                        (TechnicianTable.userId eq UUID.fromString(userId))
            }
            .limit(1)
            .firstOrNull()

        if (technicianDAO != null) {
            technicianDAO.title = newTechnician.title
            technicianDAO.description = newTechnician.description
            technicianDAO.cover = newTechnician.cover
            technicianDAO.status = newTechnician.status
            technicianDAO.tanggalDiterima = newTechnician.tanggalDiterima
            technicianDAO.namaPemilik = newTechnician.namaPemilik
            technicianDAO.estimasiBiaya = newTechnician.estimasiBiaya
            technicianDAO.teknisi = newTechnician.teknisi
            technicianDAO.updatedAt = newTechnician.updatedAt
            true
        } else {
            false
        }
    }


    override suspend fun delete(userId: String, technicianId: String): Boolean = suspendTransaction {
        val rowsDeleted = TechnicianTable.deleteWhere {
            (TechnicianTable.id eq UUID.fromString(technicianId)) and
                    (TechnicianTable.userId eq UUID.fromString(userId))
        }
        rowsDeleted >= 1
    }
}