package com.parking.parkingapp.data.repository

import com.parking.parkingapp.common.Resource
import com.parking.parkingapp.data.model.MyRentedPark
import com.parking.parkingapp.data.model.ParkModel
import kotlinx.coroutines.flow.Flow

interface ParkRepository {
    fun getPark(): Flow<Resource<List<ParkModel>>>
    suspend fun getParkById(parkId: String): Resource<ParkModel?>
    suspend fun rentPark(myRentedPark: MyRentedPark): Resource<MyRentedPark>
    fun getMyRentedPark(userId: String): Flow<Resource<List<MyRentedPark>>>
    suspend fun updateParkCurrentSlot(parkId: String, slot: Int) : Resource<Unit>
    fun upDate()

    suspend fun addRentTime(
        myParkId: String,
        userId: String,
        newTotalPay: Int,
        newEndTime: String
    ): Resource<Unit>

    suspend fun checkin(
        userId: String,
        myRentedParkId: String
    ): Resource<Unit>

    suspend fun cancel(
        userId: String,
        myRentedParkId: String
    ): Resource<Unit>

    suspend fun stopRent(
        userId: String,
        myRentedParkId: String
    ): Resource<Unit>

    suspend fun updateOvertimeRent(list: List<MyRentedPark>) : Resource<Unit>
}