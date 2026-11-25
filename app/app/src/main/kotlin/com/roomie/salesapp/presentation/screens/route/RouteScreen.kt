package com.roomie.salesapp.presentation.screens.route

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.webkit.WebViewAssetLoader
import com.roomie.salesapp.data.model.Hotel
import com.roomie.salesapp.presentation.theme.Gold500
import com.roomie.salesapp.presentation.theme.Green50
import com.roomie.salesapp.presentation.theme.Green600
import com.roomie.salesapp.presentation.theme.Indigo700
import com.roomie.salesapp.presentation.theme.Indigo900

// Centro aproximado de cada ciudad, para reencuadrar el mapa al cambiar de ciudad.
private val CITY_CENTERS = mapOf(
    "Guadalajara" to (20.6597 to -103.3496),
    "CDMX" to (19.4326 to -99.1332),
    "Monterrey" to (25.6866 to -100.3161),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteScreen(
    onNavigateToVisit: (hotelId: String, hotelName: String) -> Unit,
    viewModel: RouteViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var cityDropdownExpanded by remember { mutableStateOf(false) }

    val visited = state.hotels.count { it.visited }
    val total = state.hotels.size
    val pending = total - visited
    var mapExpanded by remember { mutableStateOf(false) }
    val visibleHotels = state.visibleHotels

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ─── Header: título + ciudad + progreso del día ───
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 18.dp)
            ) {
                Text("Mi Ruta", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text(
                    state.selectedCity,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(14.dp))
                ExposedDropdownMenuBox(
                    expanded = cityDropdownExpanded,
                    onExpandedChange = { cityDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = state.selectedCity,
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = Color.White.copy(alpha = 0.9f)) },
                        label = { Text("Ciudad", color = Color.White.copy(alpha = 0.7f)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White.copy(alpha = 0.6f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTrailingIconColor = Color.White,
                            unfocusedTrailingIconColor = Color.White.copy(alpha = 0.7f),
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = cityDropdownExpanded,
                        onDismissRequest = { cityDropdownExpanded = false }
                    ) {
                        viewModel.cities.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city) },
                                onClick = {
                                    viewModel.selectCity(city)
                                    cityDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Progreso del día (ahora dentro del header, bajo la ciudad)
                if (total > 0) {
                    Spacer(Modifier.height(16.dp))
                    HeaderProgress(visited = visited, total = total)
                }
            }

            // ─── Contenido scrolleable ───
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Mapa con botón de expandir
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box {
                                LeafletMapView(
                                    hotels = visibleHotels,
                                    selectedHotel = state.selectedHotel,
                                    selectedCity = state.selectedCity,
                                    onMarkerClick = { viewModel.selectHotel(it) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(if (mapExpanded) 440.dp else 200.dp)
                                )
                                // Botón flotante de expandir/encoger
                                FilledIconButton(
                                    onClick = { mapExpanded = !mapExpanded },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(10.dp)
                                        .size(38.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = Color.White,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        if (mapExpanded) Icons.Default.CloseFullscreen else Icons.Default.OpenInFull,
                                        contentDescription = if (mapExpanded) "Reducir mapa" else "Ampliar mapa",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Banner IA: SOLO cuando la ruta no está optimizada
                    if (!state.routeOptimized) {
                        item {
                            RouteOptimizerBanner(
                                isOptimizing = state.isOptimizing,
                                onOptimize = { viewModel.optimizeRoute() }
                            )
                        }
                    } else {
                        // Resumen de ruta optimizada
                        item {
                            RouteSummaryCard(
                                stops = state.hotels.count { it.stars >= 4 },
                                distanceKm = state.routeDistanceKm,
                                minutes = state.estimatedMinutes
                            )
                        }
                    }

                    // Contador visitados / pendientes
                    if (total > 0) {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                CountBadge("$visited visitados", Green600, Green50, Modifier.weight(1f))
                                CountBadge("$pending pendientes", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer, Modifier.weight(1f))
                            }
                        }
                    }

                    // Encabezado de sección con "Ver todos" y filtro de estrellas
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (state.routeOptimized) "Ruta de visitas" else "Hoteles disponibles",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Indigo900
                                )
                                if (state.routeOptimized) {
                                    TextButton(onClick = { viewModel.clearRoute() }, contentPadding = PaddingValues(horizontal = 8.dp)) {
                                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Ver todos", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            // Filtro de estrellas (solo cuando no es ruta)
                            if (!state.routeOptimized) {
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    StarFilterChip("Todos", state.starFilter == 0) { viewModel.setStarFilter(0) }
                                    StarFilterChip("4★", state.starFilter == 4) { viewModel.setStarFilter(4) }
                                    StarFilterChip("5★", state.starFilter == 5) { viewModel.setStarFilter(5) }
                                }
                            }
                        }
                    }

                    if (visibleHotels.isEmpty()) {
                        item {
                            Text(
                                if (state.routeOptimized) "No hay hoteles 4-5★ en esta ciudad para armar una ruta premium."
                                else "No hay hoteles con ese filtro.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            )
                        }
                    } else {
                        items(visibleHotels, key = { it.id }) { hotel ->
                            HotelCard(
                                hotel = hotel,
                                index = state.hotels.indexOf(hotel) + 1,
                                isSelected = state.selectedHotel?.id == hotel.id,
                                routeMode = state.routeOptimized,
                                onSelect = { viewModel.selectHotel(hotel) },
                                onVisit = { onNavigateToVisit(hotel.id, hotel.name) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Barra de progreso compacta para el header (texto blanco sobre verde). */
@Composable
private fun HeaderProgress(visited: Int, total: Int) {
    val fraction = if (total > 0) visited.toFloat() / total else 0f
    val animated by animateFloatAsState(targetValue = fraction, animationSpec = tween(700), label = "hp")
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Progreso del día", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
            Text("$visited / $total", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { animated },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = Gold500,
            trackColor = Color.White.copy(alpha = 0.25f),
        )
    }
}

/** Banner de "Ruta inteligente" — solo visible antes de optimizar. */
@Composable
fun RouteOptimizerBanner(
    isOptimizing: Boolean,
    onOptimize: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Ruta inteligente", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Indigo900)
                    Text(
                        "La IA ordena los hoteles 4-5★ por cercanía",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onOptimize,
                enabled = !isOptimizing,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isOptimizing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.width(10.dp))
                    Text("Analizando con IA…", fontWeight = FontWeight.SemiBold)
                } else {
                    Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Organizar mejor ruta", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/** Resumen de la ruta optimizada: paradas, distancia y tiempo estimado. */
@Composable
fun RouteSummaryCard(stops: Int, distanceKm: Double, minutes: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Green50),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            SummaryStat(Icons.Default.LocationOn, "$stops", "paradas")
            SummaryStat(Icons.Default.Straighten, "${"%.1f".format(distanceKm)} km", "distancia")
            SummaryStat(Icons.Default.Schedule, "~$minutes min", "estimado")
        }
    }
}

@Composable
private fun SummaryStat(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(6.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Indigo900)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CountBadge(text: String, fg: Color, bg: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = fg, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
private fun StarFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = FontWeight.Medium) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = Color.White,
        )
    )
}

/**
 * Mapa con calles reales (OpenStreetMap vía Leaflet en WebView).
 * Usa WebViewAssetLoader para servir leaflet.js/css locales bajo un dominio https:// virtual,
 * lo que da al WebView un origen válido y permite cargar los tiles de OSM sin bloqueos.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LeafletMapView(
    hotels: List<Hotel>,
    selectedHotel: Hotel?,
    selectedCity: String,
    onMarkerClick: (Hotel) -> Unit,
    modifier: Modifier = Modifier
) {
    val webViewRef = remember { mutableStateOf<WebView?>(null) }
    val mapReady = remember { mutableStateOf(false) }

    // Al cambiar de ciudad, salta de inmediato a su centro (no espera a que lleguen los marcadores,
    // así el mapa nunca se queda mostrando la ciudad anterior).
    LaunchedEffect(selectedCity, mapReady.value) {
        if (!mapReady.value) return@LaunchedEffect
        val center = CITY_CENTERS[selectedCity] ?: return@LaunchedEffect
        val webView = webViewRef.value ?: return@LaunchedEffect
        webView.post {
            webView.evaluateJavascript("centerCity(${center.first}, ${center.second});", null)
        }
    }

    // Inyectar marcadores cuando el mapa está listo y hay hoteles
    LaunchedEffect(hotels, mapReady.value, selectedHotel) {
        if (hotels.isEmpty() || !mapReady.value) return@LaunchedEffect
        val webView = webViewRef.value ?: return@LaunchedEffect

        val markersJs = hotels.mapIndexed { i, h ->
            val name = h.name.replace("'", " ").replace("\"", " ")
            val info = "${h.stars}★ · ${h.avgMonthlyGuests} huésp/mes"
            val visited = if (h.visited) "true" else "false"
            """addMarker(${h.latitude}, ${h.longitude}, "$name", "$info", ${i + 1}, $visited);"""
        }.joinToString(" ")

        webView.post {
            webView.evaluateJavascript("refreshSize(); clearMarkers(); $markersJs fitMap();", null)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            // Sirve /assets/ bajo https://appassets.androidplatform.net/assets/
            val assetLoader = WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(ctx))
                .build()

            WebView(ctx).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
                setLayerType(WebView.LAYER_TYPE_HARDWARE, null)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView,
                        request: WebResourceRequest
                    ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)

                    override fun onPageFinished(view: WebView, url: String) {
                        mapReady.value = true
                        // El WebView ya tiene tamaño aquí; fuerza a Leaflet a recalcular
                        // dimensiones varias veces para que pinte los tiles ya descargados.
                        view.postDelayed({ view.evaluateJavascript("refreshSize();", null) }, 300)
                        view.postDelayed({ view.evaluateJavascript("refreshSize();", null) }, 800)
                        view.postDelayed({ view.evaluateJavascript("refreshSize();", null) }, 1600)
                    }
                }
                webViewRef.value = this
                // Cargar el HTML desde el dominio virtual https:// (origen válido para tiles OSM)
                loadUrl("https://appassets.androidplatform.net/assets/map.html")
            }
        }
    )
}

/** Tarjeta de progreso del día con porcentaje grande, barra animada y mensaje motivacional. */
@Composable
fun ProgressCard(visited: Int, total: Int) {
    val fraction = if (total > 0) visited.toFloat() / total else 0f
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(700),
        label = "progress"
    )
    val pct = (fraction * 100).toInt()
    val done = visited == total && total > 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Progreso del día",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (done) {
                            Icon(Icons.Default.CheckCircle, null, tint = Green600, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(
                            if (done) "¡Ruta completada!" else "$visited de $total visitados",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (done) Green600 else Indigo900
                        )
                    }
                }
                // Porcentaje grande con acento
                Text(
                    "$pct%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (done) Green600 else Indigo700
                )
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { animatedFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = if (done) Green600 else Indigo700,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Composable
fun HotelCard(
    hotel: Hotel,
    index: Int,
    isSelected: Boolean,
    routeMode: Boolean = false,
    onSelect: () -> Unit,
    onVisit: () -> Unit
) {
    val elevation = if (isSelected) 8.dp else 1.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation, RoundedCornerShape(16.dp))
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hotel.visited) Green50 else Color.White
        ),
        border = if (isSelected) CardDefaults.outlinedCardBorder() else null
    ) {
        Column {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Número / check con anillo
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            if (hotel.visited) Green600 else MaterialTheme.colorScheme.primaryContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (hotel.visited) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            "$index",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    if (routeMode && !hotel.visited) {
                        Text(
                            "PARADA $index",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        hotel.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Indigo900
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(hotel.stars) {
                            Icon(Icons.Default.Star, null, tint = Gold500, modifier = Modifier.size(14.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "${hotel.avgMonthlyGuests} huésp/mes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (hotel.visited) {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = Green600, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Visitado",
                                style = MaterialTheme.typography.labelMedium,
                                color = Green600,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Botón de registrar visita (full width cuando está seleccionado y sin visitar)
            if (isSelected && !hotel.visited) {
                Button(
                    onClick = onVisit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp)
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Hotel, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Registrar visita", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
