package com.roomie.salesapp.data.repository

import com.roomie.salesapp.data.local.HotelDao
import com.roomie.salesapp.data.model.Hotel
import com.roomie.salesapp.data.model.toEntity
import com.roomie.salesapp.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HotelRepository @Inject constructor(
    private val api: ApiService,
    private val hotelDao: HotelDao
) {
    fun getHotelsForCity(city: String): Flow<List<Hotel>> =
        hotelDao.getHotelsByCity(city)

    suspend fun syncHotels(city: String): Result<Unit> {
        return try {
            val response = api.getHotels(city)
            if (response.isSuccessful) {
                val entities = response.body()!!.hotels.map { it.toEntity() }
                hotelDao.syncAll(entities)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markVisited(hotelId: String) = hotelDao.markVisited(hotelId)
}
