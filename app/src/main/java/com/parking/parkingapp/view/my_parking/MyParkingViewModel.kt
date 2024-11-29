package com.parking.parkingapp.view.my_parking

import android.annotation.SuppressLint
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.parking.parkingapp.common.BaseViewModel
import com.parking.parkingapp.common.ErrorDataState
import com.parking.parkingapp.common.LoadingDataState
import com.parking.parkingapp.common.State
import com.parking.parkingapp.common.SuccessDataState
import com.parking.parkingapp.common.fail
import com.parking.parkingapp.common.success
import com.parking.parkingapp.data.model.MyRentedPark
import com.parking.parkingapp.data.model.RentStatus
import com.parking.parkingapp.data.repository.AuthRepository
import com.parking.parkingapp.data.repository.ParkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class MyParkingViewModel @Inject constructor(
    private val parkRepository: ParkRepository,
    private val authRepository: AuthRepository,
) : BaseViewModel() {
    private val _myPark = MutableStateFlow<List<MyRentedPark>>(listOf())
    val myPark = _myPark.asStateFlow()

    @SuppressLint("NewApi")
    fun fetchData() {
        viewModelScope.launch {
            authRepository.getCurrentUser<FirebaseUser>().success { user ->
                user?.let { fuser ->
                    parkRepository.getMyRentedPark(fuser.uid).collect { result ->
                        result.success { listRentedPark ->
                            listRentedPark?.partition {
                                val delimiterEndTime = it.endTime.split(":")
                                LocalTime.now() > LocalTime.of(
                                    delimiterEndTime.first().toInt(),
                                    delimiterEndTime.last().toInt()
                                )
                            }?.let { (invalidList, validList) ->
                                parkRepository.updateOvertimeRent(invalidList)
                                _myPark.value = validList.toList()
                                    .filter { it.status != RentStatus.RENTED }
                            }
                        }
                    }
                }
            }
        }
    }

    fun addTime(
        addTime: Double,
        endTime: String,
        rentedPark: MyRentedPark
    ) = viewModelScope.launch {
        sendSingleEvent(State.Loading(UpdateAddTimeLoading))
        delay(300L)
        val delimiterEndTime = endTime.split(":")
        parkRepository.addRentTime(
            myParkId = rentedPark.id,
            userId = rentedPark.userId,
            newTotalPay = rentedPark.totalPay + (addTime * rentedPark.park.pricePerHour).toInt(),
            newEndTime = "${
                (delimiterEndTime.first()
                    .toInt() + addTime.toInt())
            }:${delimiterEndTime.last()}"
        ).success {
            sendSingleEvent(State.Success(UpdateAddTimeSuccess(addTime)))
        }.fail {
            sendSingleEvent(State.Error(UpdateAddTimeError(it)))
        }
    }

    fun checkin(myRentedPark: MyRentedPark) = viewModelScope.launch {
        sendSingleEvent(State.Loading(CheckinLoading))
        delay(200L)
        parkRepository.checkin(myRentedPark.userId, myRentedPark.id).success {
            sendSingleEvent(State.Success(CheckinSuccess))
        }.fail {
            sendSingleEvent(State.Error(CheckinError(it)))
        }
    }

    fun stopRent(myRentedPark: MyRentedPark) = viewModelScope.launch {
        sendSingleEvent(State.Loading(CheckinLoading))
        delay(200L)
        parkRepository.stopRent(myRentedPark.userId, myRentedPark.id).success {
            sendSingleEvent(State.Success(CheckinSuccess))
        }.fail {
            sendSingleEvent(State.Error(CheckinError(it)))
        }
    }

    fun cancel(myRentedPark: MyRentedPark) = viewModelScope.launch {
        sendSingleEvent(State.Loading(CancelLoading))
        delay(200L)
        parkRepository.cancel(myRentedPark.userId, myRentedPark.id).success {
            sendSingleEvent(State.Success(CancelSuccess))
        }.fail {
            sendSingleEvent(State.Error(CancelError(it)))
        }
    }
}

object UpdateAddTimeLoading: LoadingDataState
data class UpdateAddTimeSuccess(
    val addedTime: Double
): SuccessDataState

data class UpdateAddTimeError(
    val errorMessage: String?
): ErrorDataState

object CheckinLoading: LoadingDataState

object CheckinSuccess: SuccessDataState

data class CheckinError(
    val errorMessage: String?
): ErrorDataState

object CancelLoading: LoadingDataState
object CancelSuccess: SuccessDataState

data class CancelError(
    val errorMessage: String?
): ErrorDataState