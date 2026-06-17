package com.roomie.salesapp.di

import android.content.Context
import androidx.room.Room
import com.roomie.salesapp.data.local.AppDatabase
import com.roomie.salesapp.data.local.HotelDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "sales_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideHotelDao(db: AppDatabase): HotelDao = db.hotelDao()
}
