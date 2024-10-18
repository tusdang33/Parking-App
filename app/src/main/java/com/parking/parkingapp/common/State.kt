package com.parking.parkingapp.common

sealed class State {
    object Loading: State()
    data class Error(val errorMessage: String? = null): State()
    object Success: State()
    object Idle: State()
}