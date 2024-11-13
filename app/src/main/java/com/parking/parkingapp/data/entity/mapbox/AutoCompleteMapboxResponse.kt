package com.parking.parkingapp.data.entity.mapbox

data class AutoCompleteMapboxResponse(
    val attribution: String,
    val features: List<Feature>,
    val type: String
)