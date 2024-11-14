package com.parking.parkingapp.view.splash

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.parking.parkingapp.R
import com.parking.parkingapp.common.State
import com.parking.parkingapp.databinding.FragmentSplashBinding
import com.parking.parkingapp.view.BaseFragment
import com.parking.parkingapp.view.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashScreen: BaseFragment<FragmentSplashBinding>() {
    private val viewModel: SplashViewModel by viewModels()

    override fun initViews() {
        (activity as? MainActivity)?.apply {
            isShowHeader(false)
            isFirstTimeLogin = true
        }
    }

    override fun initActions() {
        //suppress
    }

    override fun intiData() {
        lifecycleScope.launch {
            delay(2000L)
            viewModel.checkCurrentUser()
        }
    }

    override fun obverseFromViewModel(scope: LifecycleCoroutineScope) {
        scope.launch {
            viewModel.singleEvent.collect { state ->
                when (state) {
                    is State.Error -> navigateToNextScreen(false)
                    State.Idle -> {
                        // suppress
                    }

                    State.Loading -> {
                        // suppress
                    }

                    is State.Success -> navigateToNextScreen(true)
                }
            }
        }
    }

    private fun navigateToNextScreen(isLogged: Boolean) {
        val navOption = NavOptions.Builder()
            .setPopUpTo(R.id.splashFragment, true)
            .build()
        if (isLogged) {
            findNavController().navigate(R.id.action_splashFragment_to_mapboxFragment, null, navOption)
        } else {
            findNavController().navigate(R.id.action_splashFragment_to_loginFragment,null, navOption)

        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSplashBinding = FragmentSplashBinding.inflate(inflater, container, false)
}