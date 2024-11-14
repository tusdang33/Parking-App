package com.parking.parkingapp.view.home

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.parking.parkingapp.R
import com.parking.parkingapp.common.State
import com.parking.parkingapp.databinding.FragmentHomeBinding
import com.parking.parkingapp.view.BaseFragment
import com.parking.parkingapp.view.MainActivity
import com.parking.parkingapp.view.drawer_menu.DrawerMenuFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment: BaseFragment<FragmentHomeBinding>() {
    private val viewModel: HomeViewModel by viewModels()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false)

    override fun initViews() {
        (activity as? MainActivity)?.apply {
            isShowHeader(false)
        }
    }

    override fun initActions() {
        binding.homeMapBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_mapboxFragment)
        }
        binding.homeMenuBtn.setOnClickListener {
            (activity as? MainActivity)?.apply {
                val drawerMenu = ((supportFragmentManager
                    .findFragmentById(R.id.drawer_menu) as? NavHostFragment)
                    ?.childFragmentManager
                    ?.primaryNavigationFragment as? DrawerMenuFragment)
                 drawerMenu?.open()
            }
        }
    }

    override fun intiData() {
        //suppress
    }

    override fun obverseFromViewModel(scope: LifecycleCoroutineScope) {
        scope.launch {
            viewModel.singleEvent.collect {
                when (it) {
                    is State.Error -> {
                        //suppress
                    }

                    State.Idle -> {
                        //suppress
                    }

                    State.Loading -> {
                        //suppress
                    }

                    is State.Success -> {
                        findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
                    }
                }
            }
        }
    }
}