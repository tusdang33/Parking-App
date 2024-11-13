package com.parking.parkingapp.view.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import com.parking.parkingapp.databinding.FragmentProfileBinding
import com.parking.parkingapp.view.BaseFragment

class ProfileFragment: BaseFragment<FragmentProfileBinding>() {

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProfileBinding = FragmentProfileBinding.inflate(inflater, container, false)

    override fun initViews() {
        TODO("Not yet implemented")
    }

    override fun initActions() {
        TODO("Not yet implemented")
    }

    override fun intiData() {
        TODO("Not yet implemented")
    }

    override fun obverseFromViewModel(scope: LifecycleCoroutineScope) {
        TODO("Not yet implemented")
    }
}