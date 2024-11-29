package com.parking.parkingapp.view.authenticate.login

import androidx.lifecycle.viewModelScope
import com.parking.parkingapp.common.BaseViewModel
import com.parking.parkingapp.common.ErrorDataState
import com.parking.parkingapp.common.State
import com.parking.parkingapp.common.fail
import com.parking.parkingapp.common.success
import com.parking.parkingapp.data.repository.AuthRepository
import com.parking.parkingapp.domain.usecase.ValidateEmailUseCase
import com.parking.parkingapp.domain.usecase.ValidatePasswordUseCase
import com.parking.parkingapp.domain.usecase.ValidateRetypePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val validateRetypePasswordUseCase: ValidateRetypePasswordUseCase,
): BaseViewModel() {

    fun register(
        email: String,
        password: String,
        retypePassword: String,
    ) = viewModelScope.launch() {
        sendSingleEvent(State.Loading())
        delay(200L)
        val emailValidateResult = validateEmailUseCase(email)
        val passwordValidateResult = validatePasswordUseCase(password)
        val retypePasswordValidateResult = validateRetypePasswordUseCase(
            password,
            retypePassword
        )

        val hasError = listOf(
            emailValidateResult,
            passwordValidateResult,
            retypePasswordValidateResult
        ).any { !it.successful }

        if (hasError) {
            sendSingleEvent(
                State.Error(
                    FormatRegisterError(
                        emailError = emailValidateResult.errorMessage,
                        passwordError = passwordValidateResult.errorMessage,
                        retypePasswordError = retypePasswordValidateResult.errorMessage,
                    )
                )
            )
            return@launch
        }

        authRepository.register(
            email,
            password
        )
            .success {
                sendSingleEvent(State.Success())
            }
            .fail {
                sendSingleEvent(
                    State.Error(
                        FormatRegisterError(
                            commonError = it
                        )
                    )
                )
            }
    }
}

internal data class FormatRegisterError(
    val emailError: String? = null,
    val passwordError: String? = null,
    val retypePasswordError: String? = null,
    val commonError: String? = null
) : ErrorDataState