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
import kotlinx.coroutines.flow.update
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

    private val _smartPark: MutableStateFlow<List<SmartParkModel>> = MutableStateFlow(listOf())
    val smartPark = _smartPark.asStateFlow()

    private val _mapPrioritize: MutableStateFlow<MapPrioritize> = MutableStateFlow(MapPrioritize())
    val mapPrioritize = _mapPrioritize.asStateFlow()

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
        _smartPark.value = listSmartPark.map { smartParkModel ->
            smartParkModel.copy(
                score = calculateScore(
                    prioritize = _mapPrioritize.value,
                    emptySlot = smartParkModel.park.maxSlot - smartParkModel.park.currentSlot,
                    distance = smartParkModel.distance,
                    price = smartParkModel.park.pricePerHour,
                    capacity = smartParkModel.park.maxSlot,
                    smallestPrice = listSmartPark.minOf { it.park.pricePerHour },
                    largestPrice = listSmartPark.maxOf { it.park.pricePerHour },
                    shortestDistance = listSmartPark.minOf { it.distance },
                    longestDistance = listSmartPark.maxOf { it.distance },
                    smallestCapacity = listSmartPark.minOf { it.park.maxSlot },
                    largestCapacity = listSmartPark.maxOf { it.park.maxSlot },
                )
            )
        }
    }

    fun updateMapPrioritize(
        priority: SmartPrioritize? = null,
        isSmart: Boolean? = null,
        isOpening: Boolean? = null,
    ) {
        _mapPrioritize.update { prioritize ->
            prioritize.copy(
                priority = priority ?: prioritize.priority,
                isSmart = isSmart ?: prioritize.isSmart,
                isOpening = isOpening ?: prioritize.isOpening,
            )
        }
        smartClassifyPark(_smartPark.value)
    }

    private fun calculateScore(
        prioritize: MapPrioritize,
        emptySlot: Int,
        distance: Double,
        price: Int,
        capacity: Int,
        smallestPrice: Int,
        largestPrice: Int,
        shortestDistance: Double,
        longestDistance: Double,
        smallestCapacity: Int,
        largestCapacity: Int
    ): Double {
        val emptySlotWeight: Double
        val distanceWeight: Double
        val priceWeight: Double
        val capacityWeight: Double
        when (prioritize.priority) {
            SmartPrioritize.SLOT -> {
                emptySlotWeight = 0.5
                distanceWeight = 0.3
                priceWeight = 0.1
                capacityWeight = 0.1
            }

            SmartPrioritize.PRICE -> {
                emptySlotWeight = 0.2
                distanceWeight = 0.2
                priceWeight = 0.5
                capacityWeight = 0.1
            }

            SmartPrioritize.DISTANCE -> {
                emptySlotWeight = 0.2
                distanceWeight = 0.5
                priceWeight = 0.2
                capacityWeight = 0.1
            }
        }

        return if (prioritize.isSmart) {
            (emptySlotWeight * emptySlot / capacity.toDouble()
                    + priceWeight * (1 - (price - smallestPrice) / largestPrice.toDouble())
                    + distanceWeight * (1 - (distance - shortestDistance) / longestDistance)
                    + capacityWeight * (capacity - smallestCapacity) / largestCapacity.toDouble())
        } else when (prioritize.priority) {
            SmartPrioritize.SLOT -> emptySlot / capacity.toDouble()
            SmartPrioritize.PRICE -> price.toDouble()
            SmartPrioritize.DISTANCE -> 1 / (distance / 1000.0)
        }
    }
}

data class MapPrioritize(
    val priority: SmartPrioritize = SmartPrioritize.SLOT,
    val isSmart: Boolean = true,
    val isOpening: Boolean = true
)