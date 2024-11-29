package com.parking.parkingapp.view.authenticate.reset_password

import androidx.lifecycle.viewModelScope
import com.parking.parkingapp.common.BaseViewModel
import com.parking.parkingapp.common.ErrorDataState
import com.parking.parkingapp.common.State
import com.parking.parkingapp.common.fail
import com.parking.parkingapp.common.success
import com.parking.parkingapp.data.repository.AuthRepository
import com.parking.parkingapp.domain.usecase.ValidateEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val authRepository: AuthRepository
) : BaseViewModel() {

    fun resetPassword(
        email: String
    ) = viewModelScope.launch {
        sendSingleEvent(State.Loading())
        val emailValidateResult = validateEmailUseCase(email)

        if (!emailValidateResult.successful) {
            sendSingleEvent(
                State.Error(
                    ResetPasswordError(
                        emailError = emailValidateResult.errorMessage,
                    )
                )
            )
            return@launch
        }
        authRepository.resetPassword(email).success {
            sendSingleEvent(State.Success())
        }.fail {
            sendSingleEvent(
                State.Error(
                    ResetPasswordError(
                        commonError = it,
                    )
                )
            )
        }
    }
}

data class ResetPasswordError(
    val emailError: String? = null,
    val commonError: String? = null
) : ErrorDataState