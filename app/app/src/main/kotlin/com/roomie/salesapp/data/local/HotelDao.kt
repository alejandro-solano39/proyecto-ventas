package com.roomie.salesapp.data.local

import androidx.room.*
import com.roomie.salesapp.data.model.Hotel
import kotlinx.coroutines.flow.Flow

@Dao
interface HotelDao {
    @Query("SELECT * FROM hotels WHERE city = :city ORDER BY name")
    fun getHotelsByCity(city: String): Flow<List<Hotel>>

    // Inserta solo hoteles nuevos; IGNORE evita pisar el flag visited de los ya existentes
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(hotels: List<Hotel>)

    // Actualiza datos del hotel SIN tocar visited (sincronización con el backend)
    @Query("""
        UPDATE hotels SET name = :name, city = :city, latitude = :lat,
        longitude = :lng, stars = :stars, avgMonthlyGuests = :guests
        WHERE id = :id
    """)
    suspend fun updateInfo(
        id: String, name: String, city: String,
        lat: Double, lng: Double, stars: Int, guests: Int
    )

    @Query("UPDATE hotels SET visited = 1 WHERE id = :hotelId")
    suspend fun markVisited(hotelId: String)

    @Transaction
    suspend fun syncAll(hotels: List<Hotel>) {
        // Inserta los nuevos (sin tocar visited) y actualiza info de los existentes
        insertIgnore(hotels)
        hotels.forEach {
            updateInfo(it.id, it.name, it.city, it.latitude, it.longitude, it.stars, it.avgMonthlyGuests)
        }
    }
}
