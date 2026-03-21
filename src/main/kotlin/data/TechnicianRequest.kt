package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.Technician

@Serializable
data class TechnicianRequest(
    var userId: String = "",
    var title: String = "",
    var description: String = "",
    var cover: String? = null,

    // Field Baru (Pengganti isDone & urgency)
    var status: String = "Kerusakan Ringan", // Default value
    var tanggalDiterima: String = "",
    var namaPemilik: String = "",
    var estimasiBiaya: String = "",
    var teknisi: String = ""
){
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "title" to title,
            "description" to description,
            "cover" to cover,
            "status" to status,
            "tanggalDiterima" to tanggalDiterima,
            "namaPemilik" to namaPemilik,
            "estimasiBiaya" to estimasiBiaya,
            "teknisi" to teknisi
        )
    }

    fun toEntity(): Technician {
        return Technician(
            userId = userId,
            title = title,
            description = description,
            cover = cover,
            status = status,
            tanggalDiterima = tanggalDiterima,
            namaPemilik = namaPemilik,
            estimasiBiaya = estimasiBiaya,
            teknisi = teknisi,
            updatedAt = Clock.System.now()
        )
    }
}