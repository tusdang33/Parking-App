package com.parking.parkingapp.view.home

import androidx.lifecycle.viewModelScope
import com.parking.parkingapp.common.BaseViewModel
import com.parking.parkingapp.common.State
import com.parking.parkingapp.common.fail
import com.parking.parkingapp.common.success
import com.parking.parkingapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository
): BaseViewModel() {
    fun logout() = viewModelScope.launch {
        authRepository.logout().success {sendSingleEvent(State.Success()) }
    }
}