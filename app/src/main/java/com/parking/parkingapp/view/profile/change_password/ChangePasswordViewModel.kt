package com.parking.parkingapp.view.profile.change_password

import androidx.lifecycle.viewModelScope
import com.parking.parkingapp.common.BaseViewModel
import com.parking.parkingapp.common.ErrorDataState
import com.parking.parkingapp.common.State
import com.parking.parkingapp.common.fail
import com.parking.parkingapp.common.success
import com.parking.parkingapp.data.repository.AuthRepository
import com.parking.parkingapp.domain.usecase.ValidatePasswordUseCase
import com.parking.parkingapp.domain.usecase.ValidateRetypePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val validateRetypePasswordUseCase: ValidateRetypePasswordUseCase,
): BaseViewModel() {
    fun submitPassword(
        email: String,
        oldPassword: String,
        newPassword: String,
        retypePassword: String,
    ) = viewModelScope.launch(NonCancellable) {
        val passwordValidateResult = validatePasswordUseCase(newPassword)
        val retypePasswordValidateResult = validateRetypePasswordUseCase(
            newPassword,
            retypePassword
        )

        val hasError = listOf(
            passwordValidateResult,
            retypePasswordValidateResult
        ).any { !it.successful }
        val changePasswordError = FormatChangePasswordError()
        if (hasError) {
            sendSingleEvent(
                State.Error(
                    changePasswordError.copy(
                        passwordError = passwordValidateResult.errorMessage,
                        retypePasswordError = retypePasswordValidateResult.errorMessage
                    )
                )
            )
            return@launch
        }
        authRepository.login(email, oldPassword).fail {
            sendSingleEvent(
                State.Error(
                    changePasswordError.copy(
                        oldPasswordError = it
                    )
                )
            )
        }.success {
            updatePass(newPassword)
        }
    }

    private fun updatePass(newPass: String) = viewModelScope.launch(NonCancellable) {
        sendSingleEvent(State.Loading())
        authRepository.updatePass(newPass).success {
            sendSingleEvent(State.Success())
        }.fail {
            sendSingleEvent(
                State.Error(
                    FormatChangePasswordError(
                        commonError = it
                    )
                )
            )
        }
    }
}

data class FormatChangePasswordError(
    val oldPasswordError: String? = null,
    val passwordError: String? = null,
    val retypePasswordError: String? = null,
    val commonError: String? = null
): ErrorDataState