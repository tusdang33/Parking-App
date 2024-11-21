package com.parking.parkingapp.data.repository

import com.parking.parkingapp.common.Resource
import com.parking.parkingapp.data.entity.Park
import com.parking.parkingapp.data.model.ParkModel
import kotlinx.coroutines.flow.Flow

interface ParkRepository {
    fun getPark(): Flow<Resource<List<ParkModel>>>
}