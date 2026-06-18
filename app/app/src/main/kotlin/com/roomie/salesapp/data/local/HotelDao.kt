package com.roomie.salesapp.data.local

import androidx.room.*
import com.roomie.salesapp.data.model.Hotel
import kotlinx.coroutines.flow.Flow

@Dao
interface HotelDao {
    @Query("SELECT * FROM hotels WHERE city = :city ORDER BY name")
    fun getHotelsByCity(city: String): Flow<List<Hotel>>

    // El backend es la fuente de verdad: REPLACE sobrescribe los datos (incluido visited)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(hotels: List<Hotel>)

    // Marca optimista local: refleja el visitado al instante hasta el próximo sync con el backend
    @Query("UPDATE hotels SET visited = 1 WHERE id = :hotelId")
    suspend fun markVisited(hotelId: String)

    @Transaction
    suspend fun syncAll(hotels: List<Hotel>) {
        // visited viene calculado del backend (true si el hotel tiene visitas en la BD)
        upsert(hotels)
    }
}
