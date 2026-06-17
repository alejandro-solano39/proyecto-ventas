package com.roomie.salesapp.presentation.screens.visit

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.roomie.salesapp.data.model.OpportunityDto
import com.roomie.salesapp.presentation.theme.Gold500
import com.roomie.salesapp.presentation.theme.Green600
import com.roomie.salesapp.presentation.theme.Indigo900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitReportScreen(
    hotelId: String,
    hotelName: String,
    onBack: () -> Unit,
    viewModel: VisitViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var notes by remember { mutableStateOf("") }
    val context = LocalContext.current

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) viewModel.startRecording() }

    fun toggleRecording() {
        if (state.isRecording) {
            viewModel.stopRecording()
        } else {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) viewModel.startRecording()
            else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Visita", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HotelHeader(hotelName)
            Spacer(Modifier.height(24.dp))

            when (val s = state.submitState) {
                is SubmitState.Idle, is SubmitState.Error -> {
                    RecordingSection(
                        isRecording = state.isRecording,
                        seconds = state.recordingSeconds,
                        notes = notes,
                        onNotesChange = { notes = it },
                        onMicClick = { toggleRecording() },
                        error = (s as? SubmitState.Error)?.message,
                        onSubmitNotesOnly = { viewModel.submitVisit(hotelId, notes) },
                        canSubmitNotes = notes.isNotBlank() && !state.isRecording
                    )
                }

                is SubmitState.Transcribing -> StatusView(
                    icon = Icons.Default.Mic,
                    title = "Transcribiendo tu audio…",
                    subtitle = "Convirtiendo voz a texto con IA"
                )

                is SubmitState.Reviewing -> ReviewSection(
                    transcription = state.transcription,
                    onTranscriptionChange = { viewModel.updateTranscription(it) },
                    extraNotes = notes,
                    onExtraNotesChange = { notes = it },
                    onReRecord = { viewModel.reRecord() },
                    onConfirm = { viewModel.submitVisit(hotelId, notes) }
                )

                is SubmitState.Loading -> StatusView(
                    icon = Icons.Default.Send,
                    title = "Procesando con IA…",
                    subtitle = "Extrayendo contacto, productos y oportunidad"
                )

                is SubmitState.Success -> SuccessView(opportunity = s.opportunity, onBack = onBack)
            }
        }
    }
}

@Composable
private fun HotelHeader(hotelName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Hotel, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "Hotel seleccionado",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    hotelName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Indigo900
                )
            }
        }
    }
}

@Composable
private fun RecordingSection(
    isRecording: Boolean,
    seconds: Int,
    notes: String,
    onNotesChange: (String) -> Unit,
    onMicClick: () -> Unit,
    error: String?,
    onSubmitNotesOnly: () -> Unit,
    canSubmitNotes: Boolean
) {
    Text(
        "Reporte de voz",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = Indigo900,
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        "Dicta tu reporte: contacto, productos mostrados y percepción del cliente",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(24.dp))
    MicButton(isRecording = isRecording, seconds = seconds, onClick = onMicClick)

    if (error != null) {
        Spacer(Modifier.height(16.dp))
        ErrorCard(error)
    }

    Spacer(Modifier.height(28.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    Spacer(Modifier.height(20.dp))

    Text(
        "O escribe notas manuales",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = Indigo900,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(10.dp))
    OutlinedTextField(
        value = notes,
        onValueChange = onNotesChange,
        label = { Text("Notas de la visita (opcional)") },
        placeholder = { Text("Ej: Hablé con el gerente, le interesan las amenidades…") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        maxLines = 6,
        shape = RoundedCornerShape(12.dp)
    )

    if (canSubmitNotes) {
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onSubmitNotesOnly,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Enviar notas", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Pantalla de revisión: muestra el texto transcrito, editable, antes de confirmar. */
@Composable
private fun ReviewSection(
    transcription: String,
    onTranscriptionChange: (String) -> Unit,
    extraNotes: String,
    onExtraNotesChange: (String) -> Unit,
    onReRecord: () -> Unit,
    onConfirm: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            "Revisa lo que detectamos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Indigo900
        )
    }
    Spacer(Modifier.height(4.dp))
    Text(
        "Corrige el texto si hay algún error antes de crear la oportunidad",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(14.dp))
    OutlinedTextField(
        value = transcription,
        onValueChange = onTranscriptionChange,
        label = { Text("Transcripción") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 4,
        maxLines = 12,
        shape = RoundedCornerShape(12.dp)
    )

    Spacer(Modifier.height(14.dp))
    OutlinedTextField(
        value = extraNotes,
        onValueChange = onExtraNotesChange,
        label = { Text("Agregar notas extra (opcional)") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        maxLines = 4,
        shape = RoundedCornerShape(12.dp)
    )

    Spacer(Modifier.height(20.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onReRecord,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Regrabar")
        }
        Button(
            onClick = onConfirm,
            enabled = transcription.isNotBlank(),
            modifier = Modifier.weight(1.4f).height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Confirmar", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun StatusView(icon: ImageVector, title: String, subtitle: String) {
    Spacer(Modifier.height(40.dp))
    CircularProgressIndicator(modifier = Modifier.size(60.dp), strokeWidth = 3.dp)
    Spacer(Modifier.height(20.dp))
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
    Text(
        subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, null, tint = Color(0xFFC62828), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(message, color = Color(0xFFC62828), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun MicButton(isRecording: Boolean, seconds: Int, onClick: () -> Unit) {
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val scale by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.12f else 1f,
        animationSpec = infiniteRepeatable(tween(700, easing = EaseInOut), RepeatMode.Reverse),
        label = "scale"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            if (isRecording) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(scale)
                        .background(Color(0xFFEF5350).copy(alpha = 0.2f), CircleShape)
                )
            }
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .shadow(if (isRecording) 8.dp else 4.dp, CircleShape)
                    .background(
                        if (isRecording) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onClick, modifier = Modifier.size(86.dp)) {
                    Icon(
                        if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Grabar",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        when {
            isRecording -> {
                Text("Grabando · ${seconds}s", fontWeight = FontWeight.SemiBold, color = Color(0xFFD32F2F), fontSize = 15.sp)
                Text("Toca para detener y transcribir", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> {
                Text("Toca para grabar", fontWeight = FontWeight.Medium, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Habla sobre el contacto, productos y percepción",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SuccessView(opportunity: OpportunityDto, onBack: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFFE8F5E9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CheckCircle, null, tint = Green600, modifier = Modifier.size(48.dp))
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "¡Oportunidad creada!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Indigo900
        )
        Text(
            "La oportunidad ya aparece en el dashboard web",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val value = opportunity.estimatedValue ?: opportunity.estimatedMonthlyValue
                if (value != null && value > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Potencial mensual", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "$ %,.0f MXN".format(value),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
                InfoRow("Contacto", opportunity.contactName ?: "—")
                InfoRow("Puesto", opportunity.contactRole ?: "—")
                SentimentRow(opportunity.sentiment)
                opportunity.nextAction?.let { InfoRow("Próxima acción", it) }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Volver a la ruta", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private data class Sentiment(val label: String, val icon: ImageVector, val color: Color)

private fun sentimentInfo(sentiment: String?): Sentiment = when (sentiment) {
    "VERY_INTERESTED" -> Sentiment("Muy interesado", Icons.Default.Whatshot, Color(0xFFEA580C))
    "INTERESTED"      -> Sentiment("Interesado", Icons.Default.ThumbUp, Green600)
    "NOT_INTERESTED"  -> Sentiment("No interesado", Icons.Default.ThumbDown, Color(0xFFB91C1C))
    "FOLLOW_UP"       -> Sentiment("Seguimiento", Icons.Outlined.Event, Gold500)
    else              -> Sentiment(sentiment ?: "—", Icons.Outlined.Event, Color(0xFF64748B))
}

@Composable
private fun SentimentRow(sentiment: String?) {
    val s = sentimentInfo(sentiment)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            "Sentimiento",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.45f)
        )
        Row(
            modifier = Modifier.weight(0.55f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(s.icon, null, tint = s.color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(s.label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = s.color)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.45f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.55f),
            textAlign = TextAlign.End
        )
    }
}
