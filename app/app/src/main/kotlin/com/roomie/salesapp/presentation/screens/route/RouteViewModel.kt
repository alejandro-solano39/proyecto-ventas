package com.roomie.salesapp.presentation.screens.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roomie.salesapp.data.model.Hotel
import com.roomie.salesapp.data.repository.HotelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class RouteState(
    val hotels: List<Hotel> = emptyList(),          // lista completa o ruta (sin filtro de estrellas)
    val selectedHotel: Hotel? = null,
    val selectedCity: String = "Guadalajara",
    val isLoading: Boolean = false,
    val isOptimizing: Boolean = false,              // "IA" calculando ruta
    val routeOptimized: Boolean = false,            // ya se organizó la ruta
    val routeDistanceKm: Double = 0.0,              // distancia total estimada
    val starFilter: Int = 0,                        // 0 = todos, 4 = solo 4★, 5 = solo 5★
    val error: String? = null
) {
    /** Hoteles visibles tras aplicar el filtro de estrellas. */
    val visibleHotels: List<Hotel>
        get() = if (starFilter == 0) hotels else hotels.filter { it.stars == starFilter }

    /** Minutos estimados de recorrido: ~2.5 min/km manejando + 15 min por visita. */
    val estimatedMinutes: Int
        get() = (routeDistanceKm * 2.5 + hotels.count { it.stars >= 4 } * 15).toInt()
}

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val hotelRepository: HotelRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RouteState())
    val state: StateFlow<RouteState> = _state.asStateFlow()

    val cities = listOf("Guadalajara", "CDMX", "Monterrey")

    // Centro aproximado de cada ciudad (punto de partida del vendedor al llegar)
    private val cityCenters = mapOf(
        "Guadalajara" to (20.6597 to -103.3496),
        "CDMX" to (19.4326 to -99.1332),
        "Monterrey" to (25.6866 to -100.3161),
    )

    // Guardamos la lista original (sin ordenar) para poder restaurarla
    private var rawHotels: List<Hotel> = emptyList()

    init {
        loadHotels("Guadalajara")
    }

    fun selectCity(city: String) {
        _state.update {
            it.copy(
                selectedCity = city,
                selectedHotel = null,
                routeOptimized = false,
                routeDistanceKm = 0.0
            )
        }
        loadHotels(city)
    }

    fun selectHotel(hotel: Hotel) {
        _state.update { it.copy(selectedHotel = hotel) }
    }

    fun setStarFilter(stars: Int) {
        _state.update { it.copy(starFilter = if (it.starFilter == stars) 0 else stars) }
    }

    /** Selecciona la próxima parada sin visitar (siguiente hotel pendiente de la ruta). */
    fun goToNextStop() {
        val next = _state.value.hotels.firstOrNull { !it.visited }
        if (next != null) _state.update { it.copy(selectedHotel = next) }
    }

    private fun loadHotels(city: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            hotelRepository.syncHotels(city)
            hotelRepository.getHotelsForCity(city).collect { hotels ->
                rawHotels = hotels
                // Si ya estaba optimizada, re-aplica el orden; si no, muestra tal cual
                val display = if (_state.value.routeOptimized) buildRoute(hotels, city).first else hotels
                _state.update { it.copy(hotels = display, isLoading = false) }
            }
        }
    }

    /**
     * "Organiza la mejor ruta con IA": filtra hoteles premium (4-5★) y los ordena
     * por cercanía usando vecino más próximo desde el centro de la ciudad.
     * Simula un breve procesamiento para dar feedback visual.
     */
    fun optimizeRoute() {
        if (_state.value.isOptimizing) return
        viewModelScope.launch {
            _state.update { it.copy(isOptimizing = true) }
            delay(1400) // simula el análisis de la "IA"
            val (ordered, distance) = buildRoute(rawHotels, _state.value.selectedCity)
            _state.update {
                it.copy(
                    hotels = ordered,
                    isOptimizing = false,
                    routeOptimized = true,
                    routeDistanceKm = distance,
                    selectedHotel = ordered.firstOrNull { h -> !h.visited }
                )
            }
        }
    }

    /** Restaura la lista completa sin orden de ruta. */
    fun clearRoute() {
        _state.update {
            it.copy(
                hotels = rawHotels,
                routeOptimized = false,
                routeDistanceKm = 0.0,
                selectedHotel = null
            )
        }
    }

    /**
     * Construye la ruta: solo hoteles de 4-5★, ordenados por vecino más próximo
     * empezando desde el centro de la ciudad. Devuelve (lista ordenada, distancia total km).
     */
    private fun buildRoute(hotels: List<Hotel>, city: String): Pair<List<Hotel>, Double> {
        val premium = hotels.filter { it.stars >= 4 }.toMutableList()
        if (premium.isEmpty()) return emptyList<Hotel>() to 0.0

        val (startLat, startLng) = cityCenters[city] ?: (premium[0].latitude to premium[0].longitude)

        val ordered = mutableListOf<Hotel>()
        var curLat = startLat
        var curLng = startLng
        var totalKm = 0.0

        while (premium.isNotEmpty()) {
            // Elige el más cercano al punto actual; a igualdad, el de más estrellas
            val next = premium.minByOrNull { h ->
                haversine(curLat, curLng, h.latitude, h.longitude) - (h.stars * 0.0001)
            }!!
            totalKm += haversine(curLat, curLng, next.latitude, next.longitude)
            curLat = next.latitude
            curLng = next.longitude
            ordered.add(next)
            premium.remove(next)
        }
        return ordered to totalKm
    }

    /** Distancia en km entre dos coordenadas (fórmula de Haversine). */
    private fun haversine(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLng / 2) * sin(dLng / 2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
