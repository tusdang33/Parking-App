package com.parking.parkingapp.common

sealed class State {
    object Loading: State()
    data class Error(val error: ErrorDataState? = null): State()
    object Success: State()
    object Idle: State()
}

interface ErrorDataState