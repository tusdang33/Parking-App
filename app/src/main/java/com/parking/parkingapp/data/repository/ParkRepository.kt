package com.parking.parkingapp.data.repository

import com.parking.parkingapp.common.Resource
import com.parking.parkingapp.data.model.MyRentedPark
import com.parking.parkingapp.data.model.ParkModel
import kotlinx.coroutines.flow.Flow

interface ParkRepository {
    fun getPark(): Flow<Resource<List<ParkModel>>>
    suspend fun rentPark(myRentedPark: MyRentedPark): Resource<Unit>
    fun getMyRentedPark(userId: String): Flow<Resource<List<MyRentedPark>>>
}