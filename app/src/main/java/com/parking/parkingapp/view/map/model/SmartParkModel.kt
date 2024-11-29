package com.parking.parkingapp.view.map.model

import android.os.Parcelable
import com.parking.parkingapp.data.model.ParkModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class SmartParkModel(
    val park: ParkModel,
    val distance: Double,
    val score: Double
): Parcelable