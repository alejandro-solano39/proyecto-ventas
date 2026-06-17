package com.roomie.salesapp.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.roomie.salesapp.presentation.theme.Emerald600
import com.roomie.salesapp.presentation.theme.Emerald900
import com.roomie.salesapp.presentation.theme.Gold500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val name = viewModel.userName
    val initials = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Encabezado con avatar (edge-to-edge, respetando la status bar)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Emerald900, Emerald600)))
                .statusBarsPadding()
                .padding(top = 28.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))
            Text(name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.WorkspacePremium, null, tint = Gold500, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Ejecutivo de Ventas", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Text("Cuenta", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Emerald900)
            Spacer(Modifier.height(10.dp))
            InfoCard {
                ProfileRow(Icons.Default.Email, "Correo", "vendedor@ventas.com")
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                ProfileRow(Icons.Default.Badge, "Rol", "Vendedor (seller)")
            }

            Spacer(Modifier.height(24.dp))
            Text("Preferencias", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Emerald900)
            Spacer(Modifier.height(10.dp))
            InfoCard {
                ProfileRow(Icons.Default.Notifications, "Notificaciones", "Activadas")
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                ProfileRow(Icons.Default.Settings, "Ajustes", "Configuración general")
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                ProfileRow(Icons.AutoMirrored.Filled.HelpOutline, "Ayuda y soporte", "")
            }

            Spacer(Modifier.height(28.dp))
            Text(
                "Ventas Hoteleras · v1.0",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun InfoCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Emerald600, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            if (value.isNotBlank()) {
                Text(value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
