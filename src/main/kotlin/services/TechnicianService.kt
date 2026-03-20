package org.delcom.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.data.TechnicianRequest
import org.delcom.helpers.ServiceHelper
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.ITechnicianRepository
import org.delcom.repositories.IUserRepository
import java.io.File
import java.util.*

class TechnicianService(
    private val userRepo: IUserRepository,
    private val technicianRepo: ITechnicianRepository
) {
    suspend fun getAll(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)

        val search = call.request.queryParameters["search"] ?: ""

        // Ambil query parameter untuk pagination & filter
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val perPage = call.request.queryParameters["perPage"]?.toIntOrNull() ?: 10

        // Filter baru menggunakan status dan divisi
        val status = call.request.queryParameters["status"]
        val teknisi = call.request.queryParameters["teknisi"]

        // Panggil fungsi getAll yang baru
        val technicians = technicianRepo.getAll(user.id, search, page, perPage, status, teknisi)

        val response = DataResponse(
            "success",
            "Berhasil mengambil daftar kegiatan",
            mapOf(Pair("technicians", technicians))
        )
        call.respond(response)
    }

    suspend fun getStats(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)
        val stats = technicianRepo.getHomeStats(user.id)

        val response = DataResponse(
            "success",
            "Berhasil mengambil statistik kegiatan",
            mapOf(Pair("stats", stats))
        )
        call.respond(response)
    }

    // Mengambil data kegiatan berdasarkan id
    suspend fun getById(call: ApplicationCall) {
        val technicianId = call.parameters["id"]
            ?: throw AppException(400, "Data kegiatan tidak valid!")

        val user = ServiceHelper.getAuthUser(call, userRepo)

        val technician = technicianRepo.getById(technicianId)
        if (technician == null || technician.userId != user.id) {
            throw AppException(404, "Data kegiatan tidak tersedia!")
        }

        val response = DataResponse(
            "success",
            "Berhasil mengambil data kegiatan",
            mapOf(Pair("technician", technician))
        )
        call.respond(response)
    }

    // Ubah cover kegiatan
    suspend fun putCover(call: ApplicationCall) {
        val technicianId = call.parameters["id"]
            ?: throw AppException(400, "Data kegiatan tidak valid!")

        val user = ServiceHelper.getAuthUser(call, userRepo)

        // Ambil data request
        val request = TechnicianRequest()
        request.userId = user.id

        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)
        multipartData.forEachPart { part ->
            when (part) {
                // Upload file
                is PartData.FileItem -> {
                    val ext = part.originalFileName
                        ?.substringAfterLast('.', "")
                        ?.let { if (it.isNotEmpty()) ".$it" else "" }
                        ?: ""

                    val fileName = UUID.randomUUID().toString() + ext
                    val filePath = "uploads/technicians/$fileName" // Ubah folder tujuan

                    withContext(Dispatchers.IO) {
                        val file = File(filePath)
                        file.parentFile.mkdirs() // pastikan folder ada

                        part.provider().copyAndClose(file.writeChannel())
                        request.cover = filePath
                    }
                }

                else -> {}
            }

            part.dispose()
        }

        if (request.cover == null) {
            throw AppException(404, "Cover kegiatan tidak tersedia!")
        }

        val newFile = File(request.cover!!)
        // Cek apakah gambar berhasil diunggah
        if (!newFile.exists()) {
            throw AppException(404, "Cover kegiatan gagal diunggah!")
        }

        val oldTechnician = technicianRepo.getById(technicianId)
        if (oldTechnician == null || oldTechnician.userId != user.id) {
            throw AppException(404, "Data kegiatan tidak tersedia!")
        }

        // Pertahankan data lama
        request.title = oldTechnician.title
        request.description = oldTechnician.description
        request.status = oldTechnician.status
        request.tanggalDiterima = oldTechnician.tanggalDiterima
        request.namaPemilik = oldTechnician.namaPemilik
        request.estimasiBiaya = oldTechnician.estimasiBiaya
        request.teknisi = oldTechnician.teknisi

        val isUpdated = technicianRepo.update(
            user.id,
            technicianId,
            request.toEntity()
        )
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui cover kegiatan!")
        }

        // Hapus cover lama
        if (oldTechnician.cover != null) {
            val oldFile = File(oldTechnician.cover!!)
            if (oldFile.exists()) {
                oldFile.delete()
            }
        }

        val response = DataResponse(
            "success",
            "Berhasil mengubah cover kegiatan",
            null
        )
        call.respond(response)
    }

    // Menambahkan data kegiatan
    suspend fun post(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)

        // Ambil data request
        val request = call.receive<TechnicianRequest>()
        request.userId = user.id

        // Validasi request
        val validator = ValidatorHelper(request.toMap())
        validator.required("title", "Judul kegiatan tidak boleh kosong")
        validator.required("description", "Deskripsi tidak boleh kosong")
        validator.required("status", "Status tidak boleh kosong")
        validator.required("tanggalDiterima", "Tanggal pelaksanaan tidak boleh kosong")
        validator.required("namaPemilik", "Nama Pemilik tidak boleh kosong")
        validator.required("estimasiBiaya", "Estimasi biaya tidak boleh kosong")
        validator.required("teknisi", "Teknisi tidak boleh kosong")
        validator.validate()

        // Tambahkan kegiatan
        val technicianId = technicianRepo.create(
            request.toEntity()
        )

        val response = DataResponse(
            "success",
            "Berhasil menambahkan data kegiatan",
            mapOf(Pair("technicianId", technicianId))
        )
        call.respond(response)
    }

    // Mengubah data kegiatan
    suspend fun put(call: ApplicationCall) {
        val technicianId = call.parameters["id"]
            ?: throw AppException(400, "Data kegiatan tidak valid!")

        val user = ServiceHelper.getAuthUser(call, userRepo)

        // Ambil data request
        val request = call.receive<TechnicianRequest>()
        request.userId = user.id

        // Validasi request
        val validator = ValidatorHelper(request.toMap())
        validator.required("title", "Judul kegiatan tidak boleh kosong")
        validator.required("description", "Deskripsi tidak boleh kosong")
        validator.required("status", "Status tidak boleh kosong")
        validator.required("tanggalDiterima", "Tanggal pelaksanaan tidak boleh kosong")
        validator.required("namaPemilik", "Nama Pemilik tidak boleh kosong")
        validator.required("estimasiBiaya", "Estimasi biaya tidak boleh kosong")
        validator.required("teknisi", "Teknisi tidak boleh kosong")
        validator.validate()

        val oldTechnician = technicianRepo.getById(technicianId)
        if (oldTechnician == null || oldTechnician.userId != user.id) {
            throw AppException(404, "Data kegiatan tidak tersedia!")
        }
        request.cover = oldTechnician.cover // Pertahankan cover lama

        val isUpdated = technicianRepo.update(
            user.id,
            technicianId,
            request.toEntity()
        )
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui data kegiatan!")
        }

        val response = DataResponse(
            "success",
            "Berhasil mengubah data kegiatan",
            null
        )
        call.respond(response)
    }

    // Menghapus data kegiatan
    suspend fun delete(call: ApplicationCall) {
        val technicianId = call.parameters["id"]
            ?: throw AppException(400, "Data kegiatan tidak valid!")

        val user = ServiceHelper.getAuthUser(call, userRepo)

        val oldTechnician = technicianRepo.getById(technicianId)
        if (oldTechnician == null || oldTechnician.userId != user.id) {
            throw AppException(404, "Data kegiatan tidak tersedia!")
        }

        val isDeleted = technicianRepo.delete(user.id, technicianId)
        if (!isDeleted) {
            throw AppException(400, "Gagal menghapus data kegiatan!")
        }

        if (oldTechnician.cover != null) {
            val oldFile = File(oldTechnician.cover!!)

            // Hapus data gambar jika data kegiatan sudah dihapus
            if (oldFile.exists()) {
                oldFile.delete()
            }
        }

        val response = DataResponse(
            "success",
            "Berhasil menghapus data kegiatan",
            null
        )
        call.respond(response)
    }

    // Mengambil gambar kegiatan
    suspend fun getCover(call: ApplicationCall) {
        val technicianId = call.parameters["id"]
            ?: throw AppException(400, "Data kegiatan tidak valid!")

        val technician = technicianRepo.getById(technicianId)
            ?: return call.respond(HttpStatusCode.NotFound)

        if (technician.cover == null) {
            throw AppException(404, "Kegiatan belum memiliki cover")
        }

        val file = File(technician.cover!!)
        if (!file.exists()) {
            throw AppException(404, "Cover kegiatan tidak tersedia")
        }

        call.respondFile(file)
    }
}