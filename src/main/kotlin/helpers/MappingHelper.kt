package org.delcom.helpers

import kotlinx.coroutines.Dispatchers
import org.delcom.dao.TechnicianDAO
import org.delcom.dao.RefreshTokenDAO
import org.delcom.dao.UserDAO
import org.delcom.entities.Technician
import org.delcom.entities.RefreshToken
import org.delcom.entities.User
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun userDAOToModel(dao: UserDAO) = User(
    dao.id.value.toString(),
    dao.name,
    dao.username,
    dao.password,
    dao.photo,
    dao.about,
    dao.createdAt,
    dao.updatedAt
).apply { about = dao.about }

fun refreshTokenDAOToModel(dao: RefreshTokenDAO) = RefreshToken(
    dao.id.value.toString(),
    dao.userId.toString(),
    dao.refreshToken,
    dao.authToken,
    dao.createdAt,
)

// Mengubah nama fungsi dan mapping data sesuai dengan struktur Event yang baru
fun technicianDAOToModel(dao: TechnicianDAO) = Technician(
    id = dao.id.value.toString(),
    userId = dao.userId.toString(),
    title = dao.title,
    description = dao.description,
    cover = dao.cover,

    // Mapping field baru
    status = dao.status,
    tanggalDiterima = dao.tanggalDiterima,
    namaPemilik = dao.namaPemilik,
    estimasiBiaya = dao.estimasiBiaya,
    teknisi = dao.teknisi,

    createdAt = dao.createdAt,
    updatedAt = dao.updatedAt
)