package com.parking.parkingapp.data.model

import android.os.Parcelable
import com.parking.parkingapp.data.entity.ParkEntity
import kotlinx.parcelize.Parcelize

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

fun ParkEntity.toParkModel(): ParkModel {
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

fun ParkModel.toParkEntity(): ParkEntity {
    return ParkEntity(
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