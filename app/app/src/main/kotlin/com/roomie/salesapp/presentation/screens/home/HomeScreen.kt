package com.roomie.salesapp.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.roomie.salesapp.presentation.screens.opportunities.OpportunitiesViewModel
import com.roomie.salesapp.presentation.theme.Emerald600
import com.roomie.salesapp.presentation.theme.Emerald900
import com.roomie.salesapp.presentation.theme.Gold500
import java.util.Calendar

private fun greeting(): String {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        h < 12 -> "Buenos días"
        h < 19 -> "Buenas tardes"
        else -> "Buenas noches"
    }
}

@Composable
fun HomeScreen(
    onGoToRoute: () -> Unit,
    onGoToOpportunities: () -> Unit,
    viewModel: OpportunitiesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val opps = state.opportunities

    val totalValue = opps.sumOf { it.estimatedMonthlyValue ?: it.estimatedValue ?: 0.0 }
    val won = opps.count { it.stage == "CLOSED_WON" }
    val open = opps.count { it.stage == "OPEN" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Encabezado con degradado (edge-to-edge, respetando la status bar)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Emerald900, Emerald600)
                    )
                )
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 28.dp)
        ) {
            Text(greeting(), color = Color.White.copy(alpha = 0.85f), fontSize = 15.sp)
            Text(
                "Vendedor Demo",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(20.dp))

            // Tarjeta de valor total destacada
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AttachMoney, null, tint = Gold500, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("Valor potencial total", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        Text(
                            "$ %,.0f MXN".format(totalValue),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text("${opps.size} oportunidades activas", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            // KPIs rápidos
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    value = "$open",
                    label = "Abiertas",
                    tint = Emerald600
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.EmojiEvents,
                    value = "$won",
                    label = "Ganadas",
                    tint = Gold500
                )
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "Acciones rápidas",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Emerald900
            )
            Spacer(Modifier.height(12.dp))

            ActionCard(
                icon = Icons.Default.Map,
                title = "Planear mi ruta",
                subtitle = "Organiza con IA los mejores hoteles 4-5★ por cercanía",
                onClick = onGoToRoute
            )
            Spacer(Modifier.height(12.dp))
            ActionCard(
                icon = Icons.Default.Storefront,
                title = "Ver mis ventas",
                subtitle = "Revisa tus oportunidades y su detalle",
                onClick = onGoToOpportunities
            )

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    tint: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Emerald900)
            Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Emerald900)
                Text(
                    subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
