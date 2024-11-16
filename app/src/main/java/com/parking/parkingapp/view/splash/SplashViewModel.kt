package com.parking.parkingapp.view.splash

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.parking.parkingapp.common.BaseViewModel
import com.parking.parkingapp.common.State
import com.parking.parkingapp.common.success
import com.parking.parkingapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
): BaseViewModel() {
    fun checkCurrentUser() =
        viewModelScope.launch {
            delay(2000L)
            authRepository.getCurrentUser<FirebaseUser>()
                .success {
                    if (it != null) {
                        sendSingleEvent(State.Success())
                    } else {
                        sendSingleEvent(State.Error())
                    }
                }
        }
}