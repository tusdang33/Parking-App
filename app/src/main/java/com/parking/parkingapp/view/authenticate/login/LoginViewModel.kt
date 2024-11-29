package com.parking.parkingapp.view.authenticate.login

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.parking.parkingapp.common.BaseViewModel
import com.parking.parkingapp.common.ErrorDataState
import com.parking.parkingapp.common.State
import com.parking.parkingapp.common.fail
import com.parking.parkingapp.common.success
import com.parking.parkingapp.data.repository.AuthRepository
import com.parking.parkingapp.domain.usecase.ValidateEmailUseCase
import com.parking.parkingapp.domain.usecase.ValidatePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
): BaseViewModel() {

    private var loginFailCount = 0

    fun login(
        email: String,
        password: String
    ) = viewModelScope.launch {
        sendSingleEvent(State.Loading())
        delay(200L)
        val emailValidateResult = validateEmailUseCase(email)
        val passwordValidateResult = validatePasswordUseCase(password)

        val hasError = listOf(
            emailValidateResult,
            passwordValidateResult,
        ).any { !it.successful }

        if (hasError) {
            sendSingleEvent(
                State.Error(
                    FormatLoginError(
                        emailError = emailValidateResult.errorMessage,
                        passwordError = passwordValidateResult.errorMessage,
                    )
                )
            )
            return@launch
        }

        authRepository.login(email, password)
            .success {
                sendSingleEvent(State.Success())
            }
            .fail {
                sendSingleEvent(
                    State.Error(
                        FormatLoginError(
                            commonError = it,
                            isAllowResetPassword = ++loginFailCount >= 3
                        )
                    )
                )
            }
    }
}

internal data class FormatLoginError(
    val emailError: String? = null,
    val passwordError: String? = null,
    val commonError: String? = null,
    val isAllowResetPassword: Boolean = false
) : ErrorDataState