package com.parking.parkingapp.view.park

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import com.parking.parkingapp.R
import com.parking.parkingapp.databinding.FragmentParkReservationBinding
import com.parking.parkingapp.view.BaseFragment

class ParkReservationFragment: BaseFragment<FragmentParkReservationBinding>() {
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentParkReservationBinding = FragmentParkReservationBinding.inflate(inflater, container, false)

    override fun initViews() {

    }

    override fun initActions() {

    }

    override fun intiData() {

    }

    override fun obverseFromViewModel(scope: LifecycleCoroutineScope) {

    }
}