package com.parking.parkingapp.view.park

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.parking.parkingapp.common.BaseViewModel
import com.parking.parkingapp.common.fail
import com.parking.parkingapp.common.success
import com.parking.parkingapp.data.model.MyRentedPark
import com.parking.parkingapp.data.model.ParkModel
import com.parking.parkingapp.data.repository.AuthRepository
import com.parking.parkingapp.data.repository.ParkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class ParkDetailViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val parkRepository: ParkRepository,
) : BaseViewModel() {

    @SuppressLint("NewApi")
    fun submitCheckout(parkModel: ParkModel, startTime: String, endTime: String, totalPay: Double) =
        viewModelScope.launch {
            authRepository.getCurrentUser<FirebaseUser>().success { user ->
                user?.also {
                    parkRepository.rentPark(
                        MyRentedPark(
                            id = "",
                            userId = it.uid,
                            park = parkModel,
                            startTime = startTime,
                            endTime = endTime,
                            totalPay = totalPay.toInt(),
                            rentedDate = LocalDate.now().toString()
                        )
                    ).success {
                        Log.e("tudm", "submitCheckout: ", )
                    }.fail {
                        Log.e("tudm", "fail:$it ", )
                    }
                }
            }
        }
}