package com.parking.parkingapp.data.model

import com.parking.parkingapp.data.entity.RentParkEntity

data class MyRentedPark(
    val id: String,
    val userId: String,
    val park: ParkModel,
    val startTime: String,
    val endTime: String,
    val totalPay: Int,
    val rentedDate: String
)

fun RentParkEntity.toMyRentedPark(): MyRentedPark {
    return MyRentedPark(
        id = this.id,
        userId = this.userId,
        park = this.park.toParkModel(),
        startTime = this.startTime,
        endTime = this.endTime,
        totalPay = this.totalPay,
        rentedDate = this.rentedDate,
    )
}

fun MyRentedPark.toRentParkEntity(): RentParkEntity {
    return RentParkEntity(
        id = this.id,
        userId = this.userId,
        park = this.park.toParkEntity(),
        startTime = this.startTime,
        endTime = this.endTime,
        totalPay = this.totalPay,
        rentedDate = this.rentedDate,
    )
}