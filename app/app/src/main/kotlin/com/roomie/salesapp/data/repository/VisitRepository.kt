package com.roomie.salesapp.data.repository

import com.roomie.salesapp.data.local.HotelDao
import com.roomie.salesapp.data.model.OpportunityDto
import com.roomie.salesapp.data.remote.ApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VisitRepository @Inject constructor(
    private val api: ApiService,
    private val hotelDao: HotelDao
) {

    // Paso 1: transcribe el audio y devuelve el texto para revisión
    suspend fun transcribe(audioFile: File): Result<String> {
        return try {
            val body = audioFile.asRequestBody("audio/mpeg".toMediaType())
            val part = MultipartBody.Part.createFormData("audio", audioFile.name, body)
            val response = api.transcribe(part)
            if (response.isSuccessful) {
                Result.success(response.body()!!.transcription)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al transcribir"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitVisit(
        hotelId: String,
        audioFile: File?,
        manualNotes: String?
    ): Result<OpportunityDto> {
        return try {
            val hotelIdBody = hotelId.toRequestBody("text/plain".toMediaType())
            val notesBody = manualNotes?.toRequestBody("text/plain".toMediaType())

            val audioPart = audioFile?.let {
                val body = it.asRequestBody("audio/mpeg".toMediaType())
                MultipartBody.Part.createFormData("audio", it.name, body)
            }

            val response = api.submitVisitReport(audioPart, hotelIdBody, notesBody)
            if (response.isSuccessful) {
                // Marca el hotel como visitado en Room → la ruta y el progreso se actualizan solos (Flow)
                hotelDao.markVisited(hotelId)
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
