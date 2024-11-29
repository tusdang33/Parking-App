package com.parking.parkingapp.data.model

import android.os.Parcelable
import com.parking.parkingapp.data.entity.RentParkEntity
import com.parking.parkingapp.data.model.RentStatus.Companion.toRentStatus
import kotlinx.parcelize.Parcelize

@Parcelize
data class MyRentedPark(
    val id: String,
    val userId: String,
    val park: ParkModel,
    val startTime: String,
    val endTime: String,
    val totalPay: Int,
    val rentedDate: String,
    val status: RentStatus = RentStatus.RENTING
): Parcelable

fun RentParkEntity.toMyRentedPark(): MyRentedPark {
    return MyRentedPark(
        id = this.id,
        userId = this.userId,
        park = this.park.toParkModel(),
        startTime = this.startTime,
        endTime = this.endTime,
        totalPay = this.totalPay,
        rentedDate = this.rentedDate,
        status = this.status.toRentStatus()
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
        status = this.status.name
    )
}

enum class RentStatus(val value: String) {
    RENTING("renting"), CHECKED_IN("checked-in"), RENTED("rented");

    companion object {
        fun String.toRentStatus(): RentStatus {
            return when (this) {
                RENTING.value -> RENTING
                CHECKED_IN.value -> CHECKED_IN
                RENTED.value -> RENTED
                else -> RENTING
            }
        }
    }
}