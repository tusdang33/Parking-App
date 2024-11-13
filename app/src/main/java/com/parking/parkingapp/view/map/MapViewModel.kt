package com.parking.parkingapp.view.map

import androidx.lifecycle.viewModelScope
import com.parking.parkingapp.common.BaseViewModel
import com.parking.parkingapp.data.model.AutoCompleteModel
import com.parking.parkingapp.data.repository.MapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val directionRepository: MapRepository,
): BaseViewModel() {
    private var debounceJob: Job? = null

    private val _searchSuggestion: MutableStateFlow<List<AutoCompleteModel>> = MutableStateFlow(
        listOf()
    )
    val searchSuggestion = _searchSuggestion.asStateFlow()

    fun cancelSearch() {
        debounceJob?.cancel()
    }

    fun getPlacePredictions(
        query: String
    ) {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(500)
            directionRepository.getSearchSuggestion(query).collect {
                if (it != null) {
                    _searchSuggestion.value = it
                }
            }
        }
    }
}