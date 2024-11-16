package com.parking.parkingapp.view.my_parking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import com.parking.parkingapp.databinding.FragmentMyParkingBinding
import com.parking.parkingapp.view.BaseFragment

class MyParkingFragment: BaseFragment<FragmentMyParkingBinding>() {
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMyParkingBinding = FragmentMyParkingBinding.inflate(inflater, container, false)

    override fun initViews() {

    }

    override fun initActions() {
        binding.mapMenu.setOnClickListener {
            getDrawerMenu()?.open()
        }
    }

    override fun intiData() {
    }

    override fun obverseFromViewModel(scope: LifecycleCoroutineScope) {

    }

}