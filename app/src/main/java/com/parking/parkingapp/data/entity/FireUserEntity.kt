package com.parking.parkingapp.data.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FireUserEntity(
    val id: String = "",
    val email: String = ""
): Parcelable