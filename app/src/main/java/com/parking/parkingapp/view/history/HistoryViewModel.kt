package com.parking.parkingapp.view.history

import android.annotation.SuppressLint
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.parking.parkingapp.common.BaseViewModel
import com.parking.parkingapp.common.success
import com.parking.parkingapp.data.model.MyRentedPark
import com.parking.parkingapp.data.model.RentStatus
import com.parking.parkingapp.data.repository.AuthRepository
import com.parking.parkingapp.data.repository.ParkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val parkRepository: ParkRepository,
    private val authRepository: AuthRepository,
): BaseViewModel() {
    private val _historyPark = MutableStateFlow<List<MyRentedPark>>(listOf())
    val historyPark = _historyPark.asStateFlow()

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
                                _historyPark.value = validList.toList()
                                    .filter { it.status == RentStatus.RENTED }
                            }
                        }
                    }
                }
            }
        }
    }
}