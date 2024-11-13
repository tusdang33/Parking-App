package com.parking.parkingapp.data.entity.mapbox

data class Context(
    val country: Country,
    val locality: Locality,
    val neighborhood: Neighborhood,
    val place: Place,
    val postcode: Postcode,
    val region: Region
)