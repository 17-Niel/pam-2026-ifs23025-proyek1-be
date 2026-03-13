package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object TechnicianTable : UUIDTable("events") {
    val userId = uuid("user_id")
    val title = varchar("title", 100)
    val description = text("description")
    val cover = text("cover").nullable()

    // Properti Baru (Pengganti isDone & urgency)
    val status = varchar("status", 50).default("belum terlaksana")
    val tanggalDiterima = varchar("tanggal_diterima", 100)
    val namaPemilik = varchar("nama_pemilik", 255)
    val estimasiBiaya = varchar("estimasi_biaya", 100)
    val teknisi = varchar("teknisi", 100)

    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}