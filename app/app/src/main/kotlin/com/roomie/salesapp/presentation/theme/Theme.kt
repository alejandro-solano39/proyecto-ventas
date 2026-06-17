package com.roomie.salesapp.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Paleta Verde Esmeralda (ventas / crecimiento) ───
val Emerald900 = Color(0xFF064E3B)  // verde muy profundo (headers, texto fuerte)
val Emerald700 = Color(0xFF047857)  // verde principal
val Emerald600 = Color(0xFF059669)  // verde vibrante (botones, acentos)
val Emerald500 = Color(0xFF10B981)  // verde claro vivo
val Emerald100 = Color(0xFFD1FAE5)  // verde claro (contenedores)
val Emerald50  = Color(0xFFECFDF5)  // fondo suave

// ─── Acento dorado (lujo hotelero) ───
val Gold500   = Color(0xFFF59E0B)
val Gold400   = Color(0xFFFBBF24)
val Gold600   = Color(0xFFD97706)

// ─── Compatibilidad: Green600 = éxito/visitado (apunta al esmeralda) ───
val Green600 = Emerald600
val Green50  = Emerald50

// ─── Alias de compatibilidad (la app migró de índigo a esmeralda) ───
val Indigo900 = Emerald900
val Indigo700 = Emerald700

// ─── Neutros ───
val Gray50  = Color(0xFFF8FAFC)
val Gray100 = Color(0xFFF1F5F9)
val Gray400 = Color(0xFF94A3B8)
val Slate800 = Color(0xFF1E293B)
val Slate500 = Color(0xFF64748B)

private val LightColors = lightColorScheme(
    primary            = Emerald700,
    onPrimary          = Color.White,
    primaryContainer   = Emerald100,
    onPrimaryContainer = Emerald900,
    secondary          = Emerald600,
    onSecondary        = Color.White,
    tertiary           = Gold500,
    onTertiary         = Color.White,
    background         = Gray50,
    onBackground       = Slate800,
    surface            = Color.White,
    onSurface          = Slate800,
    surfaceVariant     = Emerald50,
    onSurfaceVariant   = Slate500,
    outline            = Color(0xFFE2E8F0),
)

@Composable
fun SalesAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
