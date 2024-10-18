package com.parking.parkingapp.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.parking.parkingapp.R
import com.parking.parkingapp.common.State
import com.parking.parkingapp.databinding.FragmentHomeBinding
import com.parking.parkingapp.view.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment: BaseFragment<FragmentHomeBinding>() {
    private val viewModel: HomeViewModel by viewModels()
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.homeLogoutBtn.setOnClickListener {
            viewModel.logout()
        }
        lifecycleScope.launch {
            viewModel.singleEvent.collect {
                when (it) {
                    is State.Error -> {}
                    State.Idle -> {}
                    State.Loading -> {}
                    State.Success -> {
                        findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
                    }
                }
            }
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false)
}