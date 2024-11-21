package com.parking.parkingapp.view.map

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.mapbox.geojson.Point
import com.parking.parkingapp.common.BaseViewModel
import com.parking.parkingapp.common.success
import com.parking.parkingapp.data.model.AutoCompleteModel
import com.parking.parkingapp.data.model.ParkModel
import com.parking.parkingapp.data.model.toAutoCompleteModel
import com.parking.parkingapp.data.repository.MapRepository
import com.parking.parkingapp.data.repository.ParkRepository
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
    private val parkRepository: ParkRepository,
): BaseViewModel() {
    private var debounceJob: Job? = null

    private val _searchSuggestion: MutableStateFlow<List<AutoCompleteModel>> = MutableStateFlow(
        listOf()
    )
    val searchSuggestion = _searchSuggestion.asStateFlow()

    private val _park = mutableListOf<ParkModel>()
    private val _parkInRange: MutableStateFlow<List<ParkModel>> = MutableStateFlow(listOf())
    val parkInRange = _parkInRange.asStateFlow()

    init {
        viewModelScope.launch {
            parkRepository.getPark().collect { result ->
                result.success {
                    _park.clear()
                    _park.addAll(it?.toList() ?: listOf())
                }
            }
        }
    }

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
                _searchSuggestion.value = _park.filter { park ->
                    park.name.contains(query, true)
                }.map { park -> park.toAutoCompleteModel() } + (it ?: listOf())
            }
        }
    }

    fun getParkInRange(
        currentCoordinate: Point,
        distance: Int = 10000
    ) {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(500)
            _parkInRange.value = _park.filter {
                val e = currentCoordinate.distanceTo(
                    Point.fromLngLat(it.long, it.lat)
                )
                 e <= distance
            }.toList()
        }
    }
}