package com.parking.parkingapp.data.entity

import java.io.Serializable

data class ParkEntity(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val detailAddress: String = "",
    val image: String = "",
    val maxSlot: Int = 0,
    val currentSlot: Int = 0,
    val lat: Double = 0.0,
    val long: Double = 0.0,
    val pricePerHour: Int = 0,
    val openTime: Double = 0.0,
    val closeTime: Double = 0.0
): Serializable