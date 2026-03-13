package org.delcom.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Technician(
    var id: String = UUID.randomUUID().toString(),
    var userId: String,
    var title: String,
    var description: String,
    var cover: String?,

    // Field baru pengganti isDone dan urgency
    var status: String = "belum terlaksana",
    var tanggalDiterima: String,
    var namaPemilik: String,
    var estimasiBiaya: String,
    var teknisi: String,

    @Contextual
    val createdAt: Instant = Clock.System.now(),
    @Contextual
    var updatedAt: Instant = Clock.System.now(),
)