package com.parking.parkingapp.common

sealed class State {
    data class Loading(val loading: LoadingDataState? = null): State()
    data class Error(val error: ErrorDataState? = null): State()
    data class Success(val success: SuccessDataState? = null): State()
    data object Idle: State()
}

interface ErrorDataState
interface SuccessDataState
interface LoadingDataState