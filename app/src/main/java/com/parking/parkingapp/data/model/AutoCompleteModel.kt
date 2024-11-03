package com.parking.parkingapp.data.model

import com.mapbox.geojson.Point
import com.parking.parkingapp.data.entity.mapbox.Feature

data class AutoCompleteModel(
    val id: String,
    val coordinates: Point,
    val addressName: String,
    val placeName: String
)

fun Feature.toAutoCompleteModel(): AutoCompleteModel {
    return AutoCompleteModel(
        this.id,
        Point.fromLngLat(this.geometry.coordinates.first(), this.geometry.coordinates.last()),
        this.properties.name,
        this.properties.place_formatted
    )
}