package com.parking.parkingapp.view.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import com.parking.parkingapp.R
import com.parking.parkingapp.databinding.FragmentHistoryBinding
import com.parking.parkingapp.view.BaseFragment

class HistoryFragment: BaseFragment<FragmentHistoryBinding>() {
    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHistoryBinding = FragmentHistoryBinding.inflate(inflater,container,false)

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