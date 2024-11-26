package com.parking.parkingapp.data.entity

import java.io.Serializable

data class RentParkEntity(
    val id: String = "",
    val userId: String = "",
    val park: ParkEntity = ParkEntity(),
    val startTime: String = "",
    val endTime: String = "",
    val totalPay: Int = 0,
    val rentedDate: String = "",
    val status: String = ""
): Serializable