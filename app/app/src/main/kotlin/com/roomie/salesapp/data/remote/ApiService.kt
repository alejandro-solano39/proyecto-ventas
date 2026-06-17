package com.roomie.salesapp.data.remote

import com.roomie.salesapp.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("hotels")
    suspend fun getHotels(@Query("city") city: String? = null): Response<HotelsResponse>

    @Multipart
    @POST("transcribe")
    suspend fun transcribe(
        @Part audio: MultipartBody.Part
    ): Response<TranscriptionResponse>

    @Multipart
    @POST("visit-report")
    suspend fun submitVisitReport(
        @Part audio: MultipartBody.Part?,
        @Part("hotel_id") hotelId: RequestBody,
        @Part("manual_notes") manualNotes: RequestBody? = null
    ): Response<OpportunityDto>

    @GET("opportunities")
    suspend fun getOpportunities(
        @Query("limit") limit: Int = 50
    ): Response<OpportunitiesResponse>

    @GET("opportunities/{id}")
    suspend fun getOpportunityDetail(
        @Path("id") id: String
    ): Response<OpportunityDetailResponse>
}
