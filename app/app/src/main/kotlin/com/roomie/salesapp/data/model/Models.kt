package com.roomie.salesapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// ─── API / Network models ────────────────────────────────────────────────────

data class LoginRequest(val email: String, val password: String)

data class LoginResponse(
    val token: String,
    val user: UserDto
)

data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String
)

data class HotelDto(
    val id: String,
    val name: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val stars: Int,
    @SerializedName("avg_monthly_guests") val avgMonthlyGuests: Int
)

data class HotelsResponse(val hotels: List<HotelDto>)

data class TranscriptionResponse(val transcription: String)

data class OpportunityDto(
    // La lista devuelve "id"; el POST de visita devuelve "opportunity_id". Soportamos ambos.
    @SerializedName("id") val id: String? = null,
    @SerializedName("opportunity_id") val opportunityIdRaw: String? = null,
    @SerializedName("hotel_id") val hotelId: String,
    @SerializedName("hotel_name") val hotelName: String? = null,
    val city: String? = null,
    val transcription: String? = null,
    @SerializedName("contact_name") val contactName: String?,
    @SerializedName("contact_role") val contactRole: String?,
    @SerializedName("products_mentioned") val productsMentioned: List<String>?,
    val sentiment: String?,
    @SerializedName("estimated_value") val estimatedValue: Double?,
    @SerializedName("estimated_monthly_value") val estimatedMonthlyValue: Double? = null,
    val stage: String,
    @SerializedName("next_action") val nextAction: String?,
    @SerializedName("created_at") val createdAt: String
) {
    /** Id no nulo: usa el de la lista o el del POST de visita. */
    val opportunityId: String
        get() = id ?: opportunityIdRaw ?: ""
}

data class OpportunitiesResponse(
    val opportunities: List<OpportunityDto>,
    val total: Int
)

// ─── Detalle de oportunidad (incluye transcripción de la visita) ──────────────

data class OpportunityDetailResponse(
    val opportunity: OpportunityDetail,
    @SerializedName("related_visits") val relatedVisits: List<VisitDto> = emptyList()
)

data class OpportunityDetail(
    val id: String,
    @SerializedName("hotel_id") val hotelId: String,
    @SerializedName("hotel_name") val hotelName: String? = null,
    val city: String? = null,
    val stars: Int? = null,
    @SerializedName("seller_name") val sellerName: String? = null,
    @SerializedName("contact_name") val contactName: String? = null,
    @SerializedName("contact_role") val contactRole: String? = null,
    @SerializedName("products_mentioned") val productsMentioned: List<String>? = null,
    val sentiment: String? = null,
    @SerializedName("estimated_monthly_value") val estimatedMonthlyValue: Double? = null,
    val stage: String = "OPEN",
    @SerializedName("next_action") val nextAction: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class VisitDto(
    val id: String,
    @SerializedName("audio_transcription") val audioTranscription: String? = null,
    @SerializedName("manual_notes") val manualNotes: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

// ─── Room entities ────────────────────────────────────────────────────────────

@Entity(tableName = "hotels")
data class Hotel(
    @PrimaryKey val id: String,
    val name: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val stars: Int,
    val avgMonthlyGuests: Int,
    var visited: Boolean = false
)

fun HotelDto.toEntity() = Hotel(
    id = id,
    name = name,
    city = city,
    latitude = latitude,
    longitude = longitude,
    stars = stars,
    avgMonthlyGuests = avgMonthlyGuests
)
