package com.parking.parkingapp.view.park

import android.annotation.SuppressLint
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.parking.parkingapp.common.BaseViewModel
import com.parking.parkingapp.common.ErrorDataState
import com.parking.parkingapp.common.State
import com.parking.parkingapp.common.SuccessDataState
import com.parking.parkingapp.common.fail
import com.parking.parkingapp.common.success
import com.parking.parkingapp.data.model.MyRentedPark
import com.parking.parkingapp.data.model.ParkModel
import com.parking.parkingapp.data.repository.AuthRepository
import com.parking.parkingapp.data.repository.ParkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ParkDetailViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val parkRepository: ParkRepository,
) : BaseViewModel() {

    @SuppressLint("NewApi")
    fun submitCheckout(
        parkModel: ParkModel,
        startTime: String,
        endTime: String,
        totalHour: Double,
        isSubmitInOpenTime: Boolean
    ) =
        viewModelScope.launch {
            sendSingleEvent(State.Loading)
            authRepository.getCurrentUser<FirebaseUser>().success { user ->
                user?.also {
                    parkRepository.rentPark(
                        MyRentedPark(
                            id = "",
                            userId = it.uid,
                            park = parkModel,
                            startTime = startTime,
                            endTime = endTime,
                            totalPay = (totalHour * parkModel.pricePerHour).toInt(),
                            rentedDate = if (isSubmitInOpenTime) LocalDate.now().toString()
                            else LocalDate.now().plusDays(1).toString()
                        )
                    ).success { result ->
                        sendSingleEvent(State.Success(RentSuccess(result)))
                    }.fail { error ->
                        sendSingleEvent(State.Error(RentError(error)))
                    }
                }
            }
        }
}

data class RentSuccess(
    val result: MyRentedPark?
): SuccessDataState

data class RentError(
    val errorMessage: String?
): ErrorDataState