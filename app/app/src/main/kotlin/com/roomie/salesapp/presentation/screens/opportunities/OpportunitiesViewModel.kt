package com.roomie.salesapp.presentation.screens.opportunities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roomie.salesapp.data.model.OpportunityDto
import com.roomie.salesapp.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OpportunitiesState(
    val opportunities: List<OpportunityDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class OpportunitiesViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow(OpportunitiesState())
    val state: StateFlow<OpportunitiesState> = _state.asStateFlow()

    init {
        startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                fetchOpportunities()
                delay(10_000) // Cada 10 segundos
            }
        }
    }

    private suspend fun fetchOpportunities() {
        try {
            val response = api.getOpportunities()
            if (response.isSuccessful) {
                _state.update {
                    it.copy(
                        opportunities = response.body()?.opportunities ?: emptyList(),
                        isLoading = false,
                        error = null
                    )
                }
            } else {
                _state.update { it.copy(error = "Error ${response.code()}") }
            }
        } catch (e: Exception) {
            _state.update { it.copy(error = e.message, isLoading = false) }
        }
    }
}
