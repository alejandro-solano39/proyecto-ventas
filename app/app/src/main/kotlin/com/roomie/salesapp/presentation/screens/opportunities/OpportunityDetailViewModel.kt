package com.roomie.salesapp.presentation.screens.opportunities

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roomie.salesapp.data.model.OpportunityDetail
import com.roomie.salesapp.data.model.VisitDto
import com.roomie.salesapp.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OpportunityDetailState(
    val isLoading: Boolean = true,
    val opportunity: OpportunityDetail? = null,
    val visit: VisitDto? = null,
    val error: String? = null,
    val isDeleting: Boolean = false,
    val deleted: Boolean = false
)

@HiltViewModel
class OpportunityDetailViewModel @Inject constructor(
    private val api: ApiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val opportunityId: String = savedStateHandle["opportunityId"] ?: ""

    private val _state = MutableStateFlow(OpportunityDetailState())
    val state: StateFlow<OpportunityDetailState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = api.getOpportunityDetail(opportunityId)
                if (response.isSuccessful) {
                    val body = response.body()!!
                    _state.update {
                        it.copy(
                            isLoading = false,
                            opportunity = body.opportunity,
                            visit = body.relatedVisits.firstOrNull()
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Error ${response.code()}") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true) }
            try {
                val response = api.deleteOpportunity(opportunityId)
                if (response.isSuccessful) {
                    _state.update { it.copy(isDeleting = false, deleted = true) }
                } else {
                    _state.update { it.copy(isDeleting = false, error = "Error ${response.code()}") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isDeleting = false, error = e.message) }
            }
        }
    }
}
