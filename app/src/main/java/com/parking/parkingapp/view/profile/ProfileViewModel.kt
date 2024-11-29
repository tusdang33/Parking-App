package com.parking.parkingapp.view.profile

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.parking.parkingapp.common.BaseViewModel
import com.parking.parkingapp.common.State
import com.parking.parkingapp.common.success
import com.parking.parkingapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel() {
    var pickedImage: Uri? = null

    inner class UserProfile(
        val image: Uri? = null,
        val username: String? = null,
        val email: String? = null,
    )

    private val _userData: MutableStateFlow<UserProfile> = MutableStateFlow(UserProfile())
    val userData = _userData.asStateFlow()
    fun fetchUserData() = viewModelScope.launch {
        authRepository.getCurrentUser<FirebaseUser>().success { fuser ->
            fuser?.let { user ->
                _userData.value = UserProfile(
                    user.photoUrl,
                    user.displayName,
                    user.email
                )
            }
        }
    }

    fun updateUserData(username: String?) = viewModelScope.launch(NonCancellable) {
        sendSingleEvent(State.Loading())
        authRepository.updateProfile(
            name = username,
            email = null,
            image = pickedImage.toString()
        ).success {
            sendSingleEvent(State.Success())
        }
    }
}