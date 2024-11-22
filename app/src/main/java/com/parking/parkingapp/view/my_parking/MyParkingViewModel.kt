package com.parking.parkingapp.view.my_parking

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.parking.parkingapp.common.BaseViewModel
import com.parking.parkingapp.common.success
import com.parking.parkingapp.data.model.MyRentedPark
import com.parking.parkingapp.data.repository.AuthRepository
import com.parking.parkingapp.data.repository.ParkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyParkingViewModel @Inject constructor(
    private val parkRepository: ParkRepository,
    private val authRepository: AuthRepository,
) : BaseViewModel() {
    private val _myPark = MutableStateFlow<List<MyRentedPark>>(listOf())
    val myPark = _myPark.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.getCurrentUser<FirebaseUser>().success { user ->
                user?.let { fuser ->
                    parkRepository.getMyRentedPark(fuser.uid).collect { result ->
                        result.success {
                            _myPark.value = it ?: listOf()
                        }
                    }
                }
            }
        }
    }
}