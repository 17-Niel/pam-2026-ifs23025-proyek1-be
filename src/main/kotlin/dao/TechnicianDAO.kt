package org.delcom.dao

import org.delcom.tables.TechnicianTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.UUID

class TechnicianDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, TechnicianDAO>(TechnicianTable)

    var userId by TechnicianTable.userId
    var title by TechnicianTable.title
    var description by TechnicianTable.description
    var cover by TechnicianTable.cover

    // Properti Baru
    var status by TechnicianTable.status
    var tanggalDiterima by TechnicianTable.tanggalDiterima
    var namaPemilik by TechnicianTable.namaPemilik
    var estimasiBiaya by TechnicianTable.estimasiBiaya
    var teknisi by TechnicianTable.teknisi

    var createdAt by TechnicianTable.createdAt
    var updatedAt by TechnicianTable.updatedAt
}

//Technician