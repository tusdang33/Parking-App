package com.parking.parkingapp.view.drawer_menu

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.parking.parkingapp.common.BaseViewModel
import com.parking.parkingapp.common.State
import com.parking.parkingapp.common.success
import com.parking.parkingapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrawerMenuViewModel @Inject constructor(
    private val authRepository: AuthRepository
): BaseViewModel() {
    inner class UserProfile(
        val image: Uri? = null,
        val username: String? = null,
        val email: String? = null,
    )

    private val _userData: MutableStateFlow<UserProfile> = MutableStateFlow(UserProfile())
    val userData = _userData.asStateFlow()
    fun logout() = viewModelScope.launch {
        authRepository.logout().success { sendSingleEvent(State.Success()) }
    }

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
}