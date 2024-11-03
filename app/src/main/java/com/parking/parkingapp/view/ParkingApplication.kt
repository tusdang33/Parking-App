package com.parking.parkingapp.view

import android.app.Application
import com.google.android.libraries.places.api.Places
import com.parking.parkingapp.common.APIConst.MAP_API_KEY
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ParkingApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Places.initialize(applicationContext, MAP_API_KEY)
    }
}