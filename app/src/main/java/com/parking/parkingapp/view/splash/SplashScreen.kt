package com.parking.parkingapp.view.splash

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.parking.parkingapp.R
import com.parking.parkingapp.common.State
import com.parking.parkingapp.databinding.FragmentSplashBinding
import com.parking.parkingapp.view.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashScreen: BaseFragment<FragmentSplashBinding>() {
    private val viewModel: SplashViewModel by viewModels()
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            viewModel.singleEvent.collect { state ->
                when (state) {
                    is State.Error -> navigateToNextScreen(false)
                    State.Idle -> {
                        // suppress
                    }

                    State.Loading -> {
                        // suppress
                    }

                    State.Success -> navigateToNextScreen(true)
                }
            }
        }
        lifecycleScope.launch {
            delay(2000L)
            viewModel.checkCurrentUser()
        }
    }

    private fun navigateToNextScreen(isLogged: Boolean) {
        val navOption = NavOptions.Builder()
            .setPopUpTo(R.id.splashFragment, true)
            .build()
        if (isLogged) {
            findNavController().navigate(R.id.action_splashFragment_to_homeFragment, null, navOption)
        } else {
            findNavController().navigate(R.id.action_splashFragment_to_loginFragment,null, navOption)

        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSplashBinding = FragmentSplashBinding.inflate(inflater, container, false)
}