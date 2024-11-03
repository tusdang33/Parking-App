package com.parking.parkingapp.data.entity.mapbox

data class Feature(
    val geometry: Geometry,
    val id: String,
    val properties: Properties,
    val type: String
)