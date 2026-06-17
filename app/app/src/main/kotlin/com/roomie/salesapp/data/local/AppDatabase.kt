package com.roomie.salesapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.roomie.salesapp.data.model.Hotel

@Database(entities = [Hotel::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hotelDao(): HotelDao
}
