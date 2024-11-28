package com.parking.parkingapp.view.map

import androidx.lifecycle.viewModelScope
import com.mapbox.geojson.Point
import com.parking.parkingapp.common.BaseViewModel
import com.parking.parkingapp.common.success
import com.parking.parkingapp.data.model.AutoCompleteModel
import com.parking.parkingapp.data.model.ParkModel
import com.parking.parkingapp.data.model.toAutoCompleteModel
import com.parking.parkingapp.data.repository.MapRepository
import com.parking.parkingapp.data.repository.ParkRepository
import com.parking.parkingapp.view.map.model.SmartParkModel
import com.parking.parkingapp.view.map.model.SmartPrioritize
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
    private var priority: SmartPrioritize = SmartPrioritize.SLOT

    private val _searchSuggestion: MutableStateFlow<List<AutoCompleteModel>> = MutableStateFlow(
        listOf()
    )
    val searchSuggestion = _searchSuggestion.asStateFlow()

    private val _park = mutableListOf<ParkModel>()
    private val _parkInRange: MutableStateFlow<List<ParkModel>> = MutableStateFlow(listOf())
    val parkInRange = _parkInRange.asStateFlow()

    private val _smartPark: MutableStateFlow<List<SmartParkModel>> = MutableStateFlow(listOf())
    val smartPark = _smartPark.asStateFlow()

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
                    park.name.contains(query, true) || park.address.contains(query, true)
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
            delay(1000)
            _parkInRange.value = _park.filter {
                val e = currentCoordinate.distanceTo(
                    Point.fromLngLat(it.long, it.lat)
                )
                e <= distance
            }.toList()
        }
    }

    fun smartClassifyPark(listSmartPark: List<SmartParkModel>) {
        _smartPark.value = listSmartPark.map {
            it.copy(
                score = calculatePoint(
                    priority,
                    it.park.currentSlot,
                    it.distance,
                    it.park.pricePerHour,
                    it.park.maxSlot
                )
            )
        }
    }

    fun changePriority(prioritize: SmartPrioritize) {
        priority = prioritize
        smartClassifyPark(_smartPark.value)
    }

    private fun calculatePoint(
        prioritize: SmartPrioritize,
        emptySlot: Int,
        distance: Double,
        price: Int,
        capacity: Int
    ): Double {
        val emptySlotWeight: Double
        val distanceWeight: Double
        val priceWeight: Double
        val capacityWeight: Double
        when (prioritize) {
            SmartPrioritize.SLOT -> {
                emptySlotWeight = 0.4
                distanceWeight = 0.3
                priceWeight = 0.2
                capacityWeight = 0.1
            }

            SmartPrioritize.PRICE -> {
                priceWeight = 0.4
                emptySlotWeight = 0.3
                distanceWeight = 0.2
                capacityWeight = 0.1
            }

            SmartPrioritize.DISTANCE -> {
                distanceWeight = 0.4
                emptySlotWeight = 0.3
                priceWeight = 0.2
                capacityWeight = 0.1
            }
        }

        return emptySlotWeight * emptySlot / capacity + priceWeight * price + distanceWeight * 1 / (distance / 1000) + capacityWeight * capacity
    }
}