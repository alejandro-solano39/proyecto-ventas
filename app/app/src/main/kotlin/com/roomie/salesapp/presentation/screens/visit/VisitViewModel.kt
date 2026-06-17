package com.roomie.salesapp.presentation.screens.visit

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roomie.salesapp.data.model.OpportunityDto
import com.roomie.salesapp.data.repository.VisitRepository
import com.roomie.salesapp.util.AudioRecorder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class SubmitState {
    object Idle : SubmitState()
    object Transcribing : SubmitState()      // procesando audio -> texto
    object Reviewing : SubmitState()         // mostrando transcripción editable
    object Loading : SubmitState()           // creando oportunidad con IA
    data class Success(val opportunity: OpportunityDto) : SubmitState()
    data class Error(val message: String) : SubmitState()
}

data class VisitUiState(
    val isRecording: Boolean = false,
    val recordingSeconds: Int = 0,
    val audioFile: File? = null,
    val transcription: String = "",          // texto detectado (editable)
    val submitState: SubmitState = SubmitState.Idle
)

@HiltViewModel
class VisitViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val visitRepository: VisitRepository
) : ViewModel() {

    private val _state = MutableStateFlow(VisitUiState())
    val state: StateFlow<VisitUiState> = _state.asStateFlow()

    private val audioRecorder = AudioRecorder(context)
    private var timerJob: Job? = null

    fun startRecording() {
        val file = try {
            audioRecorder.start()
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isRecording = false,
                    submitState = SubmitState.Error(
                        "No se pudo iniciar la grabación. Verifica el permiso de micrófono."
                    )
                )
            }
            return
        }
        _state.update {
            it.copy(isRecording = true, recordingSeconds = 0, audioFile = file, submitState = SubmitState.Idle)
        }

        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _state.update { it.copy(recordingSeconds = it.recordingSeconds + 1) }
            }
        }
    }

    /** Detiene la grabación y transcribe el audio para que el usuario revise el texto. */
    fun stopRecording() {
        timerJob?.cancel()
        audioRecorder.stop()
        _state.update { it.copy(isRecording = false) }

        val audioFile = _state.value.audioFile
        if (audioFile != null && audioFile.exists()) {
            transcribeRecording(audioFile)
        }
    }

    private fun transcribeRecording(audioFile: File) {
        viewModelScope.launch {
            _state.update { it.copy(submitState = SubmitState.Transcribing) }
            visitRepository.transcribe(audioFile).fold(
                onSuccess = { text ->
                    _state.update {
                        it.copy(transcription = text, submitState = SubmitState.Reviewing)
                    }
                },
                onFailure = { err ->
                    _state.update {
                        it.copy(submitState = SubmitState.Error(err.message ?: "Error al transcribir"))
                    }
                }
            )
        }
    }

    /** Actualiza el texto mientras el usuario lo corrige. */
    fun updateTranscription(text: String) {
        _state.update { it.copy(transcription = text) }
    }

    /** Vuelve a grabar desde cero (descarta la transcripción actual). */
    fun reRecord() {
        _state.value.audioFile?.delete()
        _state.update {
            it.copy(audioFile = null, transcription = "", recordingSeconds = 0, submitState = SubmitState.Idle)
        }
    }

    /**
     * Crea la oportunidad. Envía el TEXTO (transcripción editada + notas) como manual_notes,
     * para que se respete lo que el usuario corrigió. No reenvía el audio.
     */
    fun submitVisit(hotelId: String, extraNotes: String) {
        val transcription = _state.value.transcription.trim()
        val notes = listOf(transcription, extraNotes.trim())
            .filter { it.isNotBlank() }
            .joinToString("\n\n")

        if (notes.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(submitState = SubmitState.Loading) }
            val result = visitRepository.submitVisit(
                hotelId = hotelId,
                audioFile = null,            // ya se transcribió; enviamos el texto final
                manualNotes = notes
            )
            result.fold(
                onSuccess = { opp ->
                    _state.value.audioFile?.delete()
                    _state.update { it.copy(submitState = SubmitState.Success(opp)) }
                },
                onFailure = { err ->
                    _state.update { it.copy(submitState = SubmitState.Error(err.message ?: "Error desconocido")) }
                }
            )
        }
    }

    fun resetToReview() {
        _state.update { it.copy(submitState = SubmitState.Reviewing) }
    }

    override fun onCleared() {
        super.onCleared()
        audioRecorder.cancel()
    }
}
