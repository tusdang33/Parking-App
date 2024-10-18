package com.parking.parkingapp.common

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

open class BaseViewModel: ViewModel() {
    private val _singleEvent = Channel<State>(Channel.UNLIMITED)
    val singleEvent = _singleEvent.receiveAsFlow()

    fun sendSingleEvent(state: State) {
        _singleEvent.trySend(state)
    }
}