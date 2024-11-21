package com.parking.parkingapp.data.model

import android.os.Parcelable
import com.parking.parkingapp.data.entity.Park
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class ParkModel(
    val id: String,
    val name: String,
    val address: String,
    val image: String,
    val maxSlot: Int,
    val currentSlot: Int,
    val lat: Double,
    val long: Double,
    val pricePerHour: Int,
    val openTime: Double,
    val closeTime: Double
): Parcelable

fun Park.toParkModel(): ParkModel {
    return ParkModel(
        this.id,
        this.name,
        this.address,
        this.image,
        this.maxSlot,
        this.currentSlot,
        this.lat,
        this.long,
        this.pricePerHour,
        this.openTime,
        this.closeTime,
    )
}